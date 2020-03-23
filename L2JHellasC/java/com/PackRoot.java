package com;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.Server;

public final class PackRoot
{
	private static final Logger _log = Logger.getLogger(PackRoot.class.getName());
	private static final String CONFIGURATION_FILE = "./config/Others/Pack Root.ini";
	public static File DATAPACK_ROOT;
	
	public static void load()
	{
		if (Server.serverMode == Server.MODE_GAMESERVER)
		{
			
			Properties serverSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(CONFIGURATION_FILE)))
			{
				serverSettings.load(is);
			}
			catch (Exception e)
			{
				_log.severe(PackRoot.class.getName() + ": Error while " + CONFIGURATION_FILE + " settings!");
				if (Config.DEVELOPER)
					e.printStackTrace();
			}
			try
			{
				DATAPACK_ROOT = new File(serverSettings.getProperty("PackRootGame", ".").replaceAll("\\\\", "/")).getCanonicalFile();
			}
			catch (IOException e)
			{
				_log.severe(PackRoot.class.getName() + ": Error setting datapack root!");
				DATAPACK_ROOT = new File(".");
				if (Config.DEVELOPER)
					e.printStackTrace();
			}
		}
		else if (Server.serverMode == Server.MODE_LOGINSERVER)
		{
			
			Properties serverSettings = new Properties();
			try (InputStream is = new FileInputStream(new File(CONFIGURATION_FILE)))
			{
				serverSettings.load(is);
			}
			catch (Exception e)
			{
				_log.severe(PackRoot.class.getName() + ": Error while " + CONFIGURATION_FILE + " settings!");
				if (Config.DEVELOPER)
					e.printStackTrace();
			}
			try
			{
				DATAPACK_ROOT = new File(serverSettings.getProperty("PackRootLogin", ".").replaceAll("\\\\", "/")).getCanonicalFile();
			}
			catch (IOException e)
			{
				_log.severe(PackRoot.class.getName() + ": Error setting datapack root!");
				DATAPACK_ROOT = new File(".");
				if (Config.DEVELOPER)
					e.printStackTrace();
			}
		}
		else
		{
			_log.severe("Could not Load Config: server mode was not set for network!");
		}
	}
}