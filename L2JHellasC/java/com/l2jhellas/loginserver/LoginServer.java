package com.l2jhellas.loginserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.L2JHellasInfo;
import com.PackRoot;
import com.l2jhellas.Config;
import com.l2jhellas.Server;
import com.l2jhellas.mmocore.network.SelectorConfig;
import com.l2jhellas.mmocore.network.SelectorThread;
import com.l2jhellas.util.Util;
import com.l2jhellas.util.database.L2DatabaseFactory;
import com.l2jhellas.util.ip.IPConfigData;
import com.l2jhellas.util.ip.LoginServerIP;

public class LoginServer
{
	private final Logger _log = Logger.getLogger(LoginServer.class.getName());
	
	public static final int PROTOCOL_REV = 0x0102;
	
	private static LoginServer _instance;
	
	private GameServerListener _gameServerListener;
	private SelectorThread<L2LoginClient> _selectorThread;
	private Thread _restartLoginServer;
	
	public static void main(String[] args)
	{
		_instance = new LoginServer();
	}
	
	public static LoginServer getInstance()
	{
		return _instance;
	}
	
	public LoginServer()
	{
		Server.serverMode = Server.MODE_LOGINSERVER;
		// Pack Root
		PackRoot.load();
		
		// Local Constants
		final String LOG_FOLDER = "log"; // Name of folder for log file
		final String LOG_NAME = "./config/Others/log.cfg"; // Name of log file
		
		// Create log folder
		File logFolder = new File(LOG_FOLDER);
		logFolder.mkdir();
		
		// Create input stream for log file -- or store file data into memory
		InputStream is = null;
		try
		{
			is = new FileInputStream(new File(LOG_NAME));
			LogManager.getLogManager().readConfiguration(is);
			is.close();
		}
		catch (IOException e)
		{
			_log.warning(LoginServer.class.getName() + " Failed reading log file. Reason: " + e);
			if (Config.DEVELOPER)
			{
				e.printStackTrace();
			}
		}
		finally
		{
			try
			{
				if (is != null)
					is.close();
			}
			catch (IOException e)
			{
			}
		}
		
		// IP Config
		Util.printSection("Network");
		IPConfigData.load();
		LoginServerIP.load();
		
		// Load Config
		Util.printSection("Config");
		Config.load();
		
		// Prepare Database
		Util.printSection("DataBase");
		L2DatabaseFactory.getInstance();
		
		Util.printSection("Team");
		L2JHellasInfo.showInfo();
		
		Util.printSection("Login Server Controller");
		try
		{
			LoginController.load();
		}
		catch (GeneralSecurityException e)
		{
			_log.warning(LoginServer.class.getName() + " Failed initializing LoginController. Reason: " + e);
			if (Config.DEVELOPER)
			{
				e.printStackTrace();
			}
			System.exit(1);
		}
		Util.printSection("Game Server Table");
		
		try
		{
			GameServerTable.loadGS();
		}
		catch (GeneralSecurityException e)
		{
			_log.warning(LoginServer.class.getName() + " Failed to load GameServerTable. Reason: " + e);
			if (Config.DEVELOPER)
			{
				e.printStackTrace();
			}
			System.exit(1);
		}
		
		Util.printSection("Banned IP File");
		loadBanFile();
		
		InetAddress bindAddress = null;
		if (!Config.LOGIN_BIND_ADDRESS.equals("*"))
		{
			try
			{
				bindAddress = InetAddress.getByName(Config.LOGIN_BIND_ADDRESS);
			}
			catch (UnknownHostException e1)
			{
				_log.warning(LoginServer.class.getName() + " The LoginServer bind address is invalid, using all avaliable IPs. Reason: " + e1);
				if (Config.DEVELOPER)
				{
					e1.printStackTrace();
				}
			}
		}
		
		Util.printSection("Login Server Status");
		final SelectorConfig sc = new SelectorConfig();
		sc.MAX_READ_PER_PASS = Config.MMO_MAX_READ_PER_PASS;
		sc.MAX_SEND_PER_PASS = Config.MMO_MAX_SEND_PER_PASS;
		sc.SLEEP_TIME = Config.MMO_SELECTOR_SLEEP_TIME;
		sc.HELPER_BUFFER_COUNT = Config.MMO_HELPER_BUFFER_COUNT;
		final L2LoginPacketHandler lph = new L2LoginPacketHandler();
		final SelectorHelper sh = new SelectorHelper();
		try
		{
			_selectorThread = new SelectorThread<>(sc, sh, lph, sh, sh);
		}
		catch (IOException e)
		{
			_log.warning(LoginServer.class.getName() + " Failed to open Selector. Reason: " + e);
			if (Config.DEVELOPER)
			{
				e.printStackTrace();
			}
			System.exit(1);
		}
		
		try
		{
			_gameServerListener = new GameServerListener();
			_gameServerListener.start();
			_log.info("Listening for GameServers on " + Config.GAME_SERVER_LOGIN_HOST + ":" + Config.GAME_SERVER_LOGIN_PORT);
		}
		catch (IOException e)
		{
			_log.warning(LoginServer.class.getName() + " Failed to start the Game Server Listener. Reason: " + e);
			if (Config.DEVELOPER)
			{
				e.printStackTrace();
			}
			System.exit(1);
		}
		
		try
		{
			_selectorThread.openServerSocket(bindAddress, Config.PORT_LOGIN);
		}
		catch (IOException e)
		{
			_log.warning(LoginServer.class.getName() + " Failed to open server socket. Reason: " + e);
			if (Config.DEVELOPER)
			{
				e.printStackTrace();
			}
			System.exit(1);
		}
		_selectorThread.start();
		_log.info(LoginServer.class.getSimpleName() + " Login Server ready on " + (bindAddress == null ? "*" : bindAddress.getHostAddress()) + ":" + Config.PORT_LOGIN);
	}
	
	public GameServerListener getGameServerListener()
	{
		return _gameServerListener;
	}
	
	private void loadBanFile()
	{
		File bannedFile = new File("./config/Others/BannedIPs.cfg");
		if (bannedFile.exists() && bannedFile.isFile())
		{
			FileInputStream fis = null;
			try
			{
				fis = new FileInputStream(bannedFile);
			}
			catch (FileNotFoundException e)
			{
				_log.warning(LoginServer.class.getName() + " Failed to load bannedIPs file (" + bannedFile.getName() + ") for reading. Reason: " + e);
				if (Config.DEVELOPER)
				{
					e.printStackTrace();
				}
				return;
			}
			
			LineNumberReader reader = new LineNumberReader(new InputStreamReader(fis));
			
			String line;
			String[] parts;
			try
			{
				while ((line = reader.readLine()) != null)
				{
					line = line.trim();
					// check if this line isnt a comment line
					if (line.length() > 0 && line.charAt(0) != '#')
					{
						// split comments if any
						parts = line.split("#");
						
						// discard comments in the line, if any
						line = parts[0];
						parts = line.split(" ");
						String address = parts[0];
						long duration = 0;
						
						if (parts.length > 1)
						{
							try
							{
								duration = Long.parseLong(parts[1]);
							}
							catch (NumberFormatException e)
							{
								_log.warning(LoginServer.class.getName() + " Incorrect ban duration (" + parts[1] + ") on (" + bannedFile.getName() + "). Line: " + reader.getLineNumber());
								continue;
							}
						}
						
						try
						{
							LoginController.getInstance().addBanForAddress(address, duration);
						}
						catch (UnknownHostException e)
						{
							_log.warning(LoginServer.class.getName() + " Invalid address (" + parts[0] + ") on (" + bannedFile.getName() + "). Line: " + reader.getLineNumber());
						}
					}
				}
				reader.close();
			}
			catch (IOException e)
			{
				_log.warning(LoginServer.class.getName() + " Error while reading the bannedIPs file (" + bannedFile.getName() + "). Details: " + e);
				if (Config.DEVELOPER)
				{
					e.printStackTrace();
				}
			}
			_log.info(LoginServer.class.getSimpleName() + " Loaded " + LoginController.getInstance().getBannedIps().size() + " IP Bans.");
		}
		else
		{
			_log.warning(LoginServer.class.getSimpleName() + " IP Bans file (" + bannedFile.getName() + ") is missing or is a directory, skipped.");
		}
		if (Config.LOGIN_SERVER_SCHEDULE_RESTART)
		{
			_log.info(LoginServer.class.getSimpleName() + " Scheduled restart after " + Config.LOGIN_SERVER_SCHEDULE_RESTART_TIME + " hours.");
			_restartLoginServer = new LoginServerRestart();
			_restartLoginServer.setDaemon(true);
			_restartLoginServer.start();
		}
	}
	
	class LoginServerRestart extends Thread
	{
		public LoginServerRestart()
		{
			setName("LoginServerRestart");
		}
		
		@Override
		public void run()
		{
			while (!isInterrupted())
			{
				try
				{
					wait(Config.LOGIN_SERVER_SCHEDULE_RESTART_TIME * 60 * 60 * 1000);
				}
				catch (InterruptedException e)
				{
					return;
				}
				shutdown(true);
			}
		}
	}
	
	public void shutdown(boolean restart)
	{
		Runtime.getRuntime().exit(restart ? 2 : 0);
	}
}