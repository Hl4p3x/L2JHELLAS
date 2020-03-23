package com.l2jhellas.gameserver;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.L2GameClient;
import com.l2jhellas.gameserver.network.L2GameClient.GameClientState;
import com.l2jhellas.gameserver.network.gameserverpackets.AuthRequest;
import com.l2jhellas.gameserver.network.gameserverpackets.BlowFishKey;
import com.l2jhellas.gameserver.network.gameserverpackets.ChangeAccessLevel;
import com.l2jhellas.gameserver.network.gameserverpackets.GameServerBasePacket;
import com.l2jhellas.gameserver.network.gameserverpackets.PlayerAuthRequest;
import com.l2jhellas.gameserver.network.gameserverpackets.PlayerInGame;
import com.l2jhellas.gameserver.network.gameserverpackets.PlayerLogout;
import com.l2jhellas.gameserver.network.gameserverpackets.ServerStatus;
import com.l2jhellas.gameserver.network.loginserverpackets.AuthResponse;
import com.l2jhellas.gameserver.network.loginserverpackets.InitLS;
import com.l2jhellas.gameserver.network.loginserverpackets.KickPlayer;
import com.l2jhellas.gameserver.network.loginserverpackets.LoginServerFail;
import com.l2jhellas.gameserver.network.loginserverpackets.PlayerAuthResponse;
import com.l2jhellas.gameserver.network.serverpackets.AuthLoginFail;
import com.l2jhellas.gameserver.network.serverpackets.AuthLoginFail.FailReason;
import com.l2jhellas.gameserver.network.serverpackets.CharSelectInfo;
import com.l2jhellas.util.Rnd;
import com.l2jhellas.util.Util;
import com.l2jhellas.util.crypt.NewCrypt;

public class LoginServerThread extends Thread
{
	protected static final Logger _log = Logger.getLogger(LoginServerThread.class.getName());
	private static final int REVISION = 0x0102;
	private RSAPublicKey _publicKey;
	private final String _hostname;
	private final int _port;
	private final int _gamePort;
	private Socket _loginSocket;
	private InputStream _in;
	private OutputStream _out;
	
	private NewCrypt _blowfish;
	private byte[] _blowfishKey;
	private byte[] _hexID;
	private final boolean _acceptAlternate;
	private int _requestID;
	private int _serverID;
	private final boolean _reserveHost;
	private int _maxPlayer;
	private final Map<String, L2GameClient> _waitingClients = new ConcurrentHashMap<>();
	
	private int _status;
	private String _serverName;
	private final String _gameExternalHost;
	private final String _gameInternalHost;
	
	protected LoginServerThread()
	{
		super("LoginServerThread");
		_port = Config.GAME_SERVER_LOGIN_PORT;
		_gamePort = Config.PORT_GAME;
		_hostname = Config.GAME_SERVER_LOGIN_HOST;
		_hexID = Config.HEX_ID;
		if (_hexID == null)
		{
			_requestID = Config.REQUEST_ID;
			_hexID = generateHex(16);
		}
		else
		{
			_requestID = Config.SERVER_ID;
		}
		_acceptAlternate = Config.ACCEPT_ALTERNATE_ID;
		_reserveHost = Config.RESERVE_HOST_ON_LOGIN;
		_gameExternalHost = Config.EXTERNAL_HOSTNAME;
		_gameInternalHost = Config.INTERNAL_HOSTNAME;
		_maxPlayer = Config.MAXIMUM_ONLINE_USERS;
	}
	
	@Override
	public void run()
	{
		while (!isInterrupted())
		{
			int lengthHi = 0;
			int lengthLo = 0;
			int length = 0;
			boolean checksumOk = false;
			try
			{
				// Connection
				_log.info("Connecting to login on " + _hostname + ":" + _port);
				_loginSocket = new Socket(_hostname, _port);
				_in = _loginSocket.getInputStream();
				_out = new BufferedOutputStream(_loginSocket.getOutputStream());
				
				// init Blowfish
				_blowfishKey = generateHex(40);
				_blowfish = new NewCrypt("_;v.]05-31!|+-%xT!^[$\00");
				while (!isInterrupted())
				{
					lengthLo = _in.read();
					lengthHi = _in.read();
					length = lengthHi * 256 + lengthLo;
					
					if (lengthHi < 0)
					{
						_log.finer("LoginServerThread: Login terminated the connection.");
						break;
					}
					
					byte[] incoming = new byte[length - 2];
					
					int receivedBytes = 0;
					int newBytes = 0;
					int left = length - 2;
					
					while (newBytes != -1 && receivedBytes < length - 2)
					{
						newBytes = _in.read(incoming, receivedBytes, left);
						receivedBytes = receivedBytes + newBytes;
						left -= newBytes;
					}
					
					if (receivedBytes != length - 2)
					{
						_log.warning("Incomplete Packet is sent to the server, closing connection.(LS)");
						break;
					}
					
					// decrypt if we have a key
					byte[] decrypt = _blowfish.decrypt(incoming);
					checksumOk = NewCrypt.verifyChecksum(decrypt);
					
					if (!checksumOk)
					{
						_log.warning("Incorrect packet checksum, ignoring packet (LS)");
						break;
					}
					
					if (Config.DEBUG)
						_log.warning("[C]\n" + Util.printData(decrypt, 0));
					
					int packetType = decrypt[0] & 0xff;
					switch (packetType)
					{
						case 0x00:
							InitLS init = new InitLS(decrypt);
							if (Config.DEBUG)
								_log.info("Init received");
							
							if (init.getRevision() != REVISION)
							{
								_log.warning("/!\\ Revision mismatch between LS and GS /!\\");
								break;
							}
							
							try
							{
								KeyFactory kfac = KeyFactory.getInstance("RSA");
								BigInteger modulus = new BigInteger(init.getRSAKey());
								RSAPublicKeySpec kspec1 = new RSAPublicKeySpec(modulus, RSAKeyGenParameterSpec.F4);
								_publicKey = (RSAPublicKey) kfac.generatePublic(kspec1);
								if (Config.DEBUG)
									_log.info("RSA key set up");
							}
							catch (GeneralSecurityException e)
							{
								_log.warning("Troubles while init the public key send by login");
								break;
							}
							
							// send the blowfish key through the rsa encryption
							BlowFishKey bfk = new BlowFishKey(_blowfishKey, _publicKey);
							sendPacket(bfk);
							
							if (Config.DEBUG)
								_log.info("Sent new blowfish key");
							
							// now, only accept paket with the new encryption
							_blowfish = new NewCrypt(_blowfishKey);
							if (Config.DEBUG)
								_log.info("Changed blowfish key");
							
							AuthRequest ar = new AuthRequest(_requestID, _acceptAlternate, _hexID, _gameExternalHost, _gameInternalHost, _gamePort, _reserveHost, _maxPlayer);
							sendPacket(ar);
							if (Config.DEBUG)
								_log.info("Sent AuthRequest to login");
							break;
						case 0x01:
							LoginServerFail lsf = new LoginServerFail(decrypt);
							_log.info("Damn! Registeration Failed: " + lsf.getReasonString());
							// login will close the connection here
							break;
						case 0x02:
							AuthResponse aresp = new AuthResponse(decrypt);
							_serverID = aresp.getServerId();
							_serverName = aresp.getServerName();
							Config.saveHexid(_serverID, hexToString(_hexID));
							_log.info(LoginServerThread.class.getSimpleName() + ": Connected on login as Server " + _serverID + " : " + _serverName);
							Gui.serverStatus.setText("Server Status: On");
							ServerStatus st = new ServerStatus();
							
							if (Config.SERVER_LIST_BRACKET)
								st.addAttribute(ServerStatus.SERVER_LIST_SQUARE_BRACKET, ServerStatus.ON);
							else
								st.addAttribute(ServerStatus.SERVER_LIST_SQUARE_BRACKET, ServerStatus.OFF);
							
							if (Config.SERVER_LIST_CLOCK)
								st.addAttribute(ServerStatus.SERVER_LIST_CLOCK, ServerStatus.ON);
							else
								st.addAttribute(ServerStatus.SERVER_LIST_CLOCK, ServerStatus.OFF);
							
							if (Config.SERVER_LIST_TESTSERVER)
								st.addAttribute(ServerStatus.TEST_SERVER, ServerStatus.ON);
							else
								st.addAttribute(ServerStatus.TEST_SERVER, ServerStatus.OFF);
							
							if (Config.SERVER_GMONLY)
								st.addAttribute(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_GM_ONLY);
							else
								st.addAttribute(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_AUTO);
							
							sendPacket(st);
							
							final Collection<L2PcInstance> players = L2World.getInstance().getAllPlayers().values();
							if (!players.isEmpty())
							{
								final List<String> playerList = new ArrayList<>();
								for (L2PcInstance player : players)
									playerList.add(player.getAccountName());
								
								sendPacket(new PlayerInGame(playerList));
							}
							break;
						case 0x03:
							PlayerAuthResponse par = new PlayerAuthResponse(decrypt);
							final L2GameClient wcToRemove = _waitingClients.get(par.getAccount());
							
							if (wcToRemove != null)
							{
								if (par.isAuthed())
								{
									sendPacket(new PlayerInGame(par.getAccount()));
									
									wcToRemove.setState(GameClientState.AUTHED);
									wcToRemove.sendPacket(new CharSelectInfo(par.getAccount(), wcToRemove.getSessionId().playOkID1));
									
								}
								else
								{
									wcToRemove.sendPacket(new AuthLoginFail(FailReason.SYSTEM_ERROR_LOGIN_LATER));
									wcToRemove.closeNow();
								}
							}
							break;
						case 0x04:
							KickPlayer kp = new KickPlayer(decrypt);
							doKickPlayer(kp.getAccount());
							break;
					}
				}
			}
			catch (UnknownHostException e)
			{
				if (Config.DEBUG)
					_log.log(Level.WARNING, "", e);
			}
			catch (IOException e)
			{
				_log.info("No connection found with loginserver, next try in 10 seconds.");
			}
			finally
			{
				try
				{
					_loginSocket.close();
					if (isInterrupted())
						return;
				}
				catch (Exception e)
				{
				}
			}
			
			// 10 seconds tempo before another try
			try
			{
				Thread.sleep(10000);
			}
			catch (InterruptedException e)
			{
			}
		}
	}
	
	public void addWaitingClientAndSendRequest(String acc, L2GameClient client)
	{
		final L2GameClient existingClient = _waitingClients.putIfAbsent(acc, client);
		if (existingClient == null)
		{
			try
			{
				sendPacket(new PlayerAuthRequest(client.getAccountName(), client.getSessionId()));
			}
			catch (IOException e)
			{
				_log.warning("Error while sending player auth request");
				if (Config.DEBUG)
					_log.log(Level.WARNING, "", e);
			}
		}
		else
		{
			client.closeNow();
			existingClient.closeNow();
		}
	}
	
	public void sendLogout(String account)
	{
		if (account == null)
			return;
		
		try
		{
			sendPacket(new PlayerLogout(account));
		}
		catch (IOException e)
		{
			_log.warning("Error while sending logout packet to login");
			if (Config.DEBUG)
				_log.log(Level.WARNING, "", e);
		}
		finally
		{
			_waitingClients.remove(account);
		}
	}
	
	public void sendAccessLevel(String account, int level)
	{
		ChangeAccessLevel cal = new ChangeAccessLevel(account, level);
		try
		{
			sendPacket(cal);
		}
		catch (IOException e)
		{
			if (Config.DEBUG)
				_log.log(Level.WARNING, "", e);
		}
	}
	
	private static String hexToString(byte[] hex)
	{
		return new BigInteger(hex).toString(16);
	}
	
	public void doKickPlayer(String account)
	{
		final L2GameClient client = _waitingClients.get(account);
		if (client != null)
			client.closeNow();
	}
	
	public static byte[] generateHex(int size)
	{
		byte[] array = new byte[size];
		Rnd.nextBytes(array);
		
		if (Config.DEBUG)
			_log.fine("Generated random String:  \"" + array + "\"");
		
		return array;
	}
	
	private void sendPacket(GameServerBasePacket sl) throws IOException
	{
		byte[] data = sl.getContent();
		NewCrypt.appendChecksum(data);
		if (Config.DEBUG)
			_log.finest("[S]\n" + Util.printData(data, 0));
		
		data = _blowfish.crypt(data);
		
		int len = data.length + 2;
		synchronized (_out) // avoids tow threads writing in the mean time
		{
			_out.write(len & 0xff);
			_out.write(len >> 8 & 0xff);
			_out.write(data);
			_out.flush();
		}
	}
	
	public void setMaxPlayer(int maxPlayer)
	{
		sendServerStatus(ServerStatus.MAX_PLAYERS, maxPlayer);
		_maxPlayer = maxPlayer;
	}
	
	public int getMaxPlayer()
	{
		return _maxPlayer;
	}
	
	public void sendServerStatus(int id, int value)
	{
		ServerStatus ss = new ServerStatus();
		ss.addAttribute(id, value);
		try
		{
			sendPacket(ss);
		}
		catch (IOException e)
		{
			if (Config.DEBUG)
				e.printStackTrace();
		}
	}
	
	public String getStatusString()
	{
		return ServerStatus.STATUS_STRING[_status];
	}
	
	public boolean isClockShown()
	{
		return Config.SERVER_LIST_CLOCK;
	}
	
	public boolean isBracketShown()
	{
		return Config.SERVER_LIST_BRACKET;
	}
	
	public String getServerName()
	{
		return _serverName;
	}
	
	public int getServerStatus()
	{
		return _status;
	}
	
	public void setServerStatus(int status)
	{
		switch (status)
		{
			case ServerStatus.STATUS_AUTO:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_AUTO);
				_status = status;
				break;
			case ServerStatus.STATUS_DOWN:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_DOWN);
				_status = status;
				break;
			case ServerStatus.STATUS_FULL:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_FULL);
				_status = status;
				break;
			case ServerStatus.STATUS_GM_ONLY:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_GM_ONLY);
				_status = status;
				break;
			case ServerStatus.STATUS_GOOD:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_GOOD);
				_status = status;
				break;
			case ServerStatus.STATUS_NORMAL:
				sendServerStatus(ServerStatus.SERVER_LIST_STATUS, ServerStatus.STATUS_NORMAL);
				_status = status;
				break;
			default:
				throw new IllegalArgumentException("Status does not exists:" + status);
		}
	}
	
	public static class SessionKey
	{
		public int playOkID1;
		public int playOkID2;
		public int loginOkID1;
		public int loginOkID2;
		
		public SessionKey(int loginOK1, int loginOK2, int playOK1, int playOK2)
		{
			playOkID1 = playOK1;
			playOkID2 = playOK2;
			loginOkID1 = loginOK1;
			loginOkID2 = loginOK2;
		}
		
		@Override
		public String toString()
		{
			return "PlayOk: " + playOkID1 + " " + playOkID2 + " LoginOk:" + loginOkID1 + " " + loginOkID2;
		}
	}
	
	public static LoginServerThread getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final LoginServerThread INSTANCE = new LoginServerThread();
	}
}
