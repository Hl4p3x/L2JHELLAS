package com.l2jhellas.loginserver;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.loginserver.GameServerTable.GameServerInfo;
import com.l2jhellas.loginserver.gameserverpackets.BlowFishKey;
import com.l2jhellas.loginserver.gameserverpackets.ChangeAccessLevel;
import com.l2jhellas.loginserver.gameserverpackets.GameServerAuth;
import com.l2jhellas.loginserver.gameserverpackets.PlayerAuthRequest;
import com.l2jhellas.loginserver.gameserverpackets.PlayerInGame;
import com.l2jhellas.loginserver.gameserverpackets.PlayerLogout;
import com.l2jhellas.loginserver.gameserverpackets.ServerStatus;
import com.l2jhellas.loginserver.loginserverpackets.AuthResponse;
import com.l2jhellas.loginserver.loginserverpackets.InitLS;
import com.l2jhellas.loginserver.loginserverpackets.KickPlayer;
import com.l2jhellas.loginserver.loginserverpackets.LoginServerFail;
import com.l2jhellas.loginserver.loginserverpackets.PlayerAuthResponse;
import com.l2jhellas.loginserver.serverpackets.ServerBasePacket;
import com.l2jhellas.util.Util;
import com.l2jhellas.util.crypt.NewCrypt;

public class GameServerThread extends Thread
{
	protected static final Logger _log = Logger.getLogger(GameServerThread.class.getName());
	private final Socket _connection;
	private InputStream _in;
	private OutputStream _out;
	private final RSAPublicKey _publicKey;
	private final RSAPrivateKey _privateKey;
	private NewCrypt _blowfish;
	private byte[] _blowfishKey;
	
	private final String _connectionIp;
	
	private GameServerInfo _gsi;
	
	private final Set<String> _accountsOnGameServer = new HashSet<>();
	private String _connectionIPAddress;
	
	@Override
	public void run()
	{
		_connectionIPAddress = _connection.getInetAddress().getHostAddress();
		if (GameServerThread.isBannedGameserverIP(_connectionIPAddress))
		{
			_log.info("GameServerRegistration: IP Address " + _connectionIPAddress + " is on Banned IP list.");
			forceClose(LoginServerFail.REASON_IP_BANNED);
			// ensure no further processing for this connection
			return;
		}
		
		InitLS startPacket = new InitLS(_publicKey.getModulus().toByteArray());
		try
		{
			sendPacket(startPacket);
			
			int lengthHi = 0;
			int lengthLo = 0;
			int length = 0;
			boolean checksumOk = false;
			for (;;)
			{
				lengthLo = _in.read();
				lengthHi = _in.read();
				length = lengthHi * 256 + lengthLo;
				
				if (lengthHi < 0 || _connection.isClosed())
				{
					_log.finer("LoginServerThread: Login terminated the connection.");
					break;
				}
				
				byte[] data = new byte[length - 2];
				
				int receivedBytes = 0;
				int newBytes = 0;
				while (newBytes != -1 && receivedBytes < length - 2)
				{
					newBytes = _in.read(data, 0, length - 2);
					receivedBytes = receivedBytes + newBytes;
				}
				
				if (receivedBytes != length - 2)
				{
					_log.warning(GameServerThread.class.getName() + ": Incomplete Packet is sent to the server, closing connection.(LS)");
					break;
				}
				
				// decrypt if we have a key
				data = _blowfish.decrypt(data);
				checksumOk = NewCrypt.verifyChecksum(data);
				if (!checksumOk)
				{
					_log.warning(GameServerThread.class.getName() + ": Incorrect packet checksum, closing connection (LS)");
					return;
				}
				
				if (Config.DEBUG)
				{
					_log.warning(GameServerThread.class.getName() + ": [C]\n" + Util.printData(data, newBytes));
				}
				
				int packetType = data[0] & 0xff;
				switch (packetType)
				{
					case 00:
						onReceiveBlowfishKey(data);
						break;
					case 01:
						onGameServerAuth(data);
						break;
					case 02:
						onReceivePlayerInGame(data);
						break;
					case 03:
						onReceivePlayerLogOut(data);
						break;
					case 04:
						onReceiveChangeAccessLevel(data);
						break;
					case 05:
						onReceivePlayerAuthRequest(data);
						break;
					case 06:
						onReceiveServerStatus(data);
						break;
					default:
						_log.warning(GameServerThread.class.getName() + ": Unknown Opcode (" + Integer.toHexString(packetType).toUpperCase() + ") from GameServer, closing connection.");
						forceClose(LoginServerFail.NOT_AUTHED);
				}
				
			}
		}
		catch (IOException e)
		{
			String serverName = (getServerId() != -1 ? "[" + getServerId() + "] " + GameServerTable.getInstance().getServerNameById(getServerId()) : "(" + _connectionIPAddress + ")");
			String msg = "GameServer " + serverName + ": Connection lost: " + e.getMessage();
			_log.info(msg);
		}
		finally
		{
			if (isAuthed())
			{
				_gsi.setDown();
				_log.info("Server [" + getServerId() + "] " + GameServerTable.getInstance().getServerNameById(getServerId()) + " is now set as disconnected");
			}
			LoginServer.getInstance().getGameServerListener().removeGameServer(this);
			LoginServer.getInstance().getGameServerListener().removeFloodProtection(_connectionIp);
		}
	}
	
	private void onReceiveBlowfishKey(byte[] data)
	{
		
		BlowFishKey bfk = new BlowFishKey(data, _privateKey);
		_blowfishKey = bfk.getKey();
		_blowfish = new NewCrypt(_blowfishKey);
		if (Config.DEBUG)
		{
			_log.info("New BlowFish key received, Blowfih Engine initialized:");
		}
		
	}
	
	private void onGameServerAuth(byte[] data)
	{
		GameServerAuth gsa = new GameServerAuth(data);
		if (Config.DEBUG)
		{
			_log.info("Auth request received");
		}
		handleRegProcess(gsa);
		if (isAuthed())
		{
			AuthResponse ar = new AuthResponse(getGameServerInfo().getId());
			sendPacket(ar);
			if (Config.DEBUG)
			{
				_log.info("Authed: id: " + getGameServerInfo().getId());
			}
		}
	}
	
	private void onReceivePlayerInGame(byte[] data)
	{
		if (isAuthed())
		{
			PlayerInGame pig = new PlayerInGame(data);
			List<String> newAccounts = pig.getAccounts();
			for (String account : newAccounts)
			{
				_accountsOnGameServer.add(account);
				if (Config.DEBUG)
				{
					_log.info("Account " + account + " logged in GameServer: [" + getServerId() + "] " + GameServerTable.getInstance().getServerNameById(getServerId()));
				}
			}
		}
		else
		{
			forceClose(LoginServerFail.NOT_AUTHED);
		}
	}
	
	private void onReceivePlayerLogOut(byte[] data)
	{
		if (isAuthed())
		{
			final PlayerLogout plo = new PlayerLogout(data);
			_accountsOnGameServer.remove(plo.getAccount());
		}
		else
			forceClose(LoginServerFail.NOT_AUTHED);
	}
	
	private void onReceiveChangeAccessLevel(byte[] data)
	{
		if (isAuthed())
		{
			ChangeAccessLevel cal = new ChangeAccessLevel(data);
			LoginController.getInstance().setAccountAccessLevel(cal.getAccount(), cal.getLevel());
			_log.info("Changed " + cal.getAccount() + " access level to " + cal.getLevel());
		}
		else
		{
			forceClose(LoginServerFail.NOT_AUTHED);
		}
	}
	
	private void onReceivePlayerAuthRequest(byte[] data)
	{
		if (isAuthed())
		{
			PlayerAuthRequest par = new PlayerAuthRequest(data);
			PlayerAuthResponse authResponse;
			if (Config.DEBUG)
			{
				_log.info("auth request received for Player " + par.getAccount());
			}
			SessionKey key = LoginController.getInstance().getKeyForAccount(par.getAccount());
			if (key != null && key.equals(par.getKey()))
			{
				if (Config.DEBUG)
				{
					_log.info("auth request: OK");
				}
				LoginController.getInstance().removeAuthedLoginClient(par.getAccount());
				authResponse = new PlayerAuthResponse(par.getAccount(), true);
			}
			else
			{
				if (Config.DEBUG)
				{
					_log.info("auth request: NO");
					_log.info("session key from self: " + key);
					_log.info("session key sent: " + par.getKey());
				}
				authResponse = new PlayerAuthResponse(par.getAccount(), false);
			}
			sendPacket(authResponse);
		}
		else
		{
			forceClose(LoginServerFail.NOT_AUTHED);
		}
	}
	
	private void onReceiveServerStatus(byte[] data)
	{
		if (isAuthed())
		{
			if (Config.DEBUG)
			{
				_log.info("ServerStatus received");
			}
			new ServerStatus(data, getServerId()); // will do the actions by itself
		}
		else
		{
			forceClose(LoginServerFail.NOT_AUTHED);
		}
	}
	
	private void handleRegProcess(GameServerAuth gameServerAuth)
	{
		GameServerTable gameServerTable = GameServerTable.getInstance();
		
		int id = gameServerAuth.getDesiredID();
		byte[] hexId = gameServerAuth.getHexID();
		
		GameServerInfo gsi = gameServerTable.getRegisteredGameServerById(id);
		// is there a gameserver registered with this id?
		if (gsi != null)
		{
			// does the hex id match?
			if (Arrays.equals(gsi.getHexId(), hexId))
			{
				// check to see if this GS is already connected
				synchronized (gsi)
				{
					if (gsi.isAuthed())
					{
						forceClose(LoginServerFail.REASON_ALREADY_LOGGED8IN);
					}
					else
					{
						attachGameServerInfo(gsi, gameServerAuth);
					}
				}
			}
			else
			{
				// there is already a server registered with the desired id and different hex id
				// try to register this one with an alternative id
				if (Config.ACCEPT_NEW_GAMESERVER && gameServerAuth.acceptAlternateID())
				{
					gsi = new GameServerInfo(id, hexId, this);
					if (gameServerTable.registerWithFirstAvaliableId(gsi))
					{
						attachGameServerInfo(gsi, gameServerAuth);
						gameServerTable.registerServerOnDB(gsi);
					}
					else
					{
						forceClose(LoginServerFail.REASON_NO_FREE_ID);
					}
				}
				else
				{
					// server id is already taken, and we can't get a new one for you
					forceClose(LoginServerFail.REASON_WRONG_HEXID);
				}
			}
		}
		else
		{
			// can we register on this id?
			if (Config.ACCEPT_NEW_GAMESERVER)
			{
				gsi = new GameServerInfo(id, hexId, this);
				if (gameServerTable.register(id, gsi))
				{
					attachGameServerInfo(gsi, gameServerAuth);
					gameServerTable.registerServerOnDB(gsi);
				}
				else
				{
					// some one took this ID meanwhile
					forceClose(LoginServerFail.REASON_ID_RESERVED);
				}
			}
			else
			{
				forceClose(LoginServerFail.REASON_WRONG_HEXID);
			}
		}
	}
	
	public boolean hasAccountOnGameServer(String account)
	{
		return _accountsOnGameServer.contains(account);
	}
	
	public int getPlayerCount()
	{
		return _accountsOnGameServer.size();
	}
	
	private void attachGameServerInfo(GameServerInfo gsi, GameServerAuth gameServerAuth)
	{
		setGameServerInfo(gsi);
		gsi.setGameServerThread(this);
		gsi.setPort(gameServerAuth.getPort());
		setGameHosts(gameServerAuth.getExternalHost(), gameServerAuth.getInternalHost());
		gsi.setMaxPlayers(gameServerAuth.getMaxPlayers());
		gsi.setAuthed(true);
	}
	
	private void forceClose(int reason)
	{
		sendPacket(new LoginServerFail(reason));
		
		try
		{
			_connection.close();
		}
		catch (IOException e)
		{
			_log.finer("GameServerThread: Failed disconnecting banned server, server already disconnected.");
		}
	}
	
	public static boolean isBannedGameserverIP(String ipAddress)
	{
		return false;
	}
	
	public GameServerThread(Socket con)
	{
		_connection = con;
		_connectionIp = con.getInetAddress().getHostAddress();
		try
		{
			_in = _connection.getInputStream();
			_out = new BufferedOutputStream(_connection.getOutputStream());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		KeyPair pair = GameServerTable.getInstance().getKeyPair();
		_privateKey = (RSAPrivateKey) pair.getPrivate();
		_publicKey = (RSAPublicKey) pair.getPublic();
		_blowfish = new NewCrypt("_;v.]05-31!|+-%xT!^[$\00");
		start();
	}
	
	private void sendPacket(ServerBasePacket sl)
	{
		try
		{
			byte[] data = sl.getContent();
			NewCrypt.appendChecksum(data);
			data = _blowfish.crypt(data);
			
			int len = data.length + 2;
			synchronized (_out)
			{
				_out.write(len & 0xff);
				_out.write(len >> 8 & 0xff);
				_out.write(data);
				_out.flush();
			}
		}
		catch (IOException e)
		{
			_log.severe("IOException while sending packet " + sl.getClass().getSimpleName() + ".");
		}
	}
	
	public void kickPlayer(String account)
	{
		sendPacket(new KickPlayer(account));
	}
	
	public void setGameHosts(String gameExternalHost, String gameInternalHost)
	{
		String oldInternal = _gsi.getInternalHost();
		String oldExternal = _gsi.getExternalHost();
		
		_gsi.setExternalHost(gameExternalHost);
		_gsi.setInternalIp(gameInternalHost);
		
		if (!gameExternalHost.equals("*"))
		{
			try
			{
				_gsi.setExternalIp(InetAddress.getByName(gameExternalHost).getHostAddress());
			}
			catch (UnknownHostException e)
			{
				_log.warning(GameServerThread.class.getName() + ": Couldn't resolve hostname \"" + gameExternalHost + "\"");
			}
		}
		else
		{
			_gsi.setExternalIp(_connectionIp);
		}
		if (!gameInternalHost.equals("*"))
		{
			try
			{
				_gsi.setInternalIp(InetAddress.getByName(gameInternalHost).getHostAddress());
			}
			catch (UnknownHostException e)
			{
				_log.warning(GameServerThread.class.getName() + ": Couldn't resolve hostname \"" + gameInternalHost + "\"");
			}
		}
		else
		{
			_gsi.setInternalIp(_connectionIp);
		}
		
		_log.info("Updated Gameserver [" + getServerId() + "] " + GameServerTable.getInstance().getServerNameById(getServerId()) + " IP's:");
		if (oldInternal == null || !oldInternal.equalsIgnoreCase(gameInternalHost))
			_log.info("InternalIP: " + gameInternalHost);
		if (oldExternal == null || !oldExternal.equalsIgnoreCase(gameExternalHost))
			_log.info("ExternalIP: " + gameExternalHost);
	}
	
	public boolean isAuthed()
	{
		if (getGameServerInfo() == null)
			return false;
		return getGameServerInfo().isAuthed();
	}
	
	public void setGameServerInfo(GameServerInfo gsi)
	{
		_gsi = gsi;
	}
	
	public GameServerInfo getGameServerInfo()
	{
		return _gsi;
	}
	
	public String getConnectionIpAddress()
	{
		return _connectionIPAddress;
	}
	
	private int getServerId()
	{
		if (getGameServerInfo() != null)
		{
			return getGameServerInfo().getId();
		}
		return -1;
	}
}