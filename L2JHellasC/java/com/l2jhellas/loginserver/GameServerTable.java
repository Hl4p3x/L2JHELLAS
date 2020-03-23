package com.l2jhellas.loginserver;

import java.io.File;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.RSAKeyGenParameterSpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jhellas.Config;
import com.l2jhellas.loginserver.gameserverpackets.ServerStatus;
import com.l2jhellas.util.Rnd;
import com.l2jhellas.util.XMLDocumentFactory;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class GameServerTable
{
	private static Logger _log = Logger.getLogger(GameServerTable.class.getName());
	
	private static final String SELECT_GS = "SELECT * FROM gameservers";
	private static final String INSERT_GS = "INSERT INTO gameservers (server_id,hexid,host) VALUES (?,?,?)";
	
	private static GameServerTable _instance;
	
	// Server Names Config
	private static Map<Integer, String> _serverNames = new HashMap<>();
	
	// Game Server Table
	private final Map<Integer, GameServerInfo> _gameServerTable = new ConcurrentHashMap<>();
	
	// RSA Config
	private static final int KEYS_SIZE = 10;
	private KeyPair[] _keyPairs;
	
	public static void load() throws GeneralSecurityException
	{
		if (_instance == null)
		{
			_instance = new GameServerTable();
		}
		else
		{
			throw new IllegalStateException("Load can only be invoked a single time.");
		}
	}
	
	public static GameServerTable getInstance()
	{
		return _instance;
	}
	
	public GameServerTable() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException
	{
		loadServerNames();
		_log.info(GameServerTable.class.getSimpleName() + " Loaded " + _serverNames.size() + " Server Names.");
		
		loadRegisteredGameServers();
		_log.info(GameServerTable.class.getSimpleName() + " Loaded " + _gameServerTable.size() + " registered Game Servers.");
		
		loadRSAKeys();
		_log.info(GameServerTable.class.getSimpleName() + " Cached " + _keyPairs.length + " RSA keys for Game Server communication.");
	}
	
	private void loadRSAKeys() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException
	{
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(512, RSAKeyGenParameterSpec.F4);
		keyGen.initialize(spec);
		
		_keyPairs = new KeyPair[KEYS_SIZE];
		for (int i = 0; i < KEYS_SIZE; i++)
		{
			_keyPairs[i] = keyGen.genKeyPair();
		}
		
		keyGen = null;
		spec = null;
	}
	
	private static void loadServerNames()
	{
		try
		{
			final File f = new File("./config/Network/ServerName.xml");
			final Document doc = XMLDocumentFactory.getInstance().loadDocument(f);
			
			Node n = doc.getFirstChild();
			for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
			{
				if (d.getNodeName().equalsIgnoreCase("server"))
				{
					NamedNodeMap attrs = d.getAttributes();
					
					int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
					String name = attrs.getNamedItem("name").getNodeValue();
					
					_serverNames.put(id, name);
				}
			}
		}
		catch (Exception e)
		{
			_log.warning(GameServerTable.class.getName() + " ServerName.xml could not be loaded.");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	private void loadRegisteredGameServers()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			int id;
			
			PreparedStatement statement = con.prepareStatement(SELECT_GS);
			ResultSet rset = statement.executeQuery();
			GameServerInfo gsi;
			while (rset.next())
			{
				id = rset.getInt("server_id");
				gsi = new GameServerInfo(id, stringToHex(rset.getString("hexid")));
				_gameServerTable.put(id, gsi);
			}
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning(GameServerTable.class.getName() + " cant select any game server(s) from database. ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public Map<Integer, GameServerInfo> getRegisteredGameServers()
	{
		return _gameServerTable;
	}
	
	public GameServerInfo getRegisteredGameServerById(int id)
	{
		return _gameServerTable.get(id);
	}
	
	public boolean hasRegisteredGameServerOnId(int id)
	{
		return _gameServerTable.containsKey(id);
	}
	
	public boolean registerWithFirstAvaliableId(GameServerInfo gsi)
	{
		// avoid two servers registering with the same "free" id
		synchronized (_gameServerTable)
		{
			for (Entry<Integer, String> entry : _serverNames.entrySet())
			{
				if (!_gameServerTable.containsKey(entry.getKey()))
				{
					_gameServerTable.put(entry.getKey(), gsi);
					gsi.setId(entry.getKey());
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean register(int id, GameServerInfo gsi)
	{
		// avoid two servers registering with the same id
		synchronized (_gameServerTable)
		{
			if (!_gameServerTable.containsKey(id))
			{
				_gameServerTable.put(id, gsi);
				gsi.setId(id);
				return true;
			}
		}
		return false;
	}
	
	public void registerServerOnDB(GameServerInfo gsi)
	{
		registerServerOnDB(gsi.getId(), gsi.getHexId(), gsi.getExternalHost());
	}
	
	public void registerServerOnDB(int id, byte[] hexId, String externalHost)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(INSERT_GS);
			statement.setInt(1, id);
			statement.setString(2, hexToString(hexId));
			statement.setString(3, externalHost);
			statement.executeUpdate();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning(GameServerTable.class.getName() + " SQL error while saving gameserver: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public String getServerNameById(int id)
	{
		return getServerNames().get(id);
	}
	
	public Map<Integer, String> getServerNames()
	{
		return _serverNames;
	}
	
	public KeyPair getKeyPair()
	{
		return _keyPairs[Rnd.nextInt(10)];
	}
	
	private static byte[] stringToHex(String string)
	{
		return new BigInteger(string, 16).toByteArray();
	}
	
	private static String hexToString(byte[] hex)
	{
		if (hex == null)
			return "null";
		return new BigInteger(hex).toString(16);
	}
	
	public static class GameServerInfo
	{
		// auth
		private int _id;
		private final byte[] _hexId;
		private boolean _isAuthed;
		
		// status
		private GameServerThread _gst;
		private int _status;
		
		// network
		private String _internalIp;
		private String _externalIp;
		private String _externalHost;
		private int _port;
		
		// config
		private boolean _isPvp = true;
		private int _ageLimit;
		private boolean _isTestServer;
		private boolean _isShowingClock;
		private boolean _isShowingBrackets;
		private int _maxPlayers;
		
		public GameServerInfo(int id, byte[] hexId, GameServerThread gst)
		{
			_id = id;
			_hexId = hexId;
			_gst = gst;
			_status = ServerStatus.STATUS_DOWN;
		}
		
		public GameServerInfo(int id, byte[] hexId)
		{
			this(id, hexId, null);
		}
		
		public void setId(int id)
		{
			_id = id;
		}
		
		public int getId()
		{
			return _id;
		}
		
		public byte[] getHexId()
		{
			return _hexId;
		}
		
		public void setAuthed(boolean isAuthed)
		{
			_isAuthed = isAuthed;
		}
		
		public boolean isAuthed()
		{
			return _isAuthed;
		}
		
		public void setGameServerThread(GameServerThread gst)
		{
			_gst = gst;
		}
		
		public GameServerThread getGameServerThread()
		{
			return _gst;
		}
		
		public void setStatus(int status)
		{
			_status = status;
		}
		
		public int getStatus()
		{
			return _status;
		}
		
		public int getCurrentPlayerCount()
		{
			if (_gst == null)
				return 0;
			return _gst.getPlayerCount();
		}
		
		public void setInternalIp(String internalIp)
		{
			_internalIp = internalIp;
		}
		
		public String getInternalHost()
		{
			return _internalIp;
		}
		
		public void setExternalIp(String externalIp)
		{
			_externalIp = externalIp;
		}
		
		public String getExternalIp()
		{
			return _externalIp;
		}
		
		public void setExternalHost(String externalHost)
		{
			_externalHost = externalHost;
		}
		
		public String getExternalHost()
		{
			return _externalHost;
		}
		
		public int getPort()
		{
			return _port;
		}
		
		public void setPort(int port)
		{
			_port = port;
		}
		
		public void setMaxPlayers(int maxPlayers)
		{
			_maxPlayers = maxPlayers;
		}
		
		public int getMaxPlayers()
		{
			return _maxPlayers;
		}
		
		public void setTestServer(boolean val)
		{
			_isTestServer = val;
		}
		
		public boolean isTestServer()
		{
			return _isTestServer;
		}
		
		public void setShowingClock(boolean clock)
		{
			_isShowingClock = clock;
		}
		
		public boolean isShowingClock()
		{
			return _isShowingClock;
		}
		
		public void setShowingBrackets(boolean val)
		{
			_isShowingBrackets = val;
		}
		
		public boolean isShowingBrackets()
		{
			return _isShowingBrackets;
		}
		
		public int getAgeLimit()
		{
			return _ageLimit;
		}
		
		public void setAgeLimit(int ageLimit)
		{
			_ageLimit = ageLimit;
		}
		
		public boolean isPvp()
		{
			return _isPvp;
		}
		
		public void setPvp(boolean isPvp)
		{
			_isPvp = isPvp;
		}
		
		public void setDown()
		{
			setAuthed(false);
			setPort(0);
			setGameServerThread(null);
			setStatus(ServerStatus.STATUS_DOWN);
		}
	}
}