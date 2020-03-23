package com.l2jhellas.util.ip;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import com.PackRoot;

public class GameServerIP
{
	private static final String dirGame = PackRoot.DATAPACK_ROOT + "/config/Network/IPConfig/IPGameServer.ini";
	
	public static void load()
	{
		// game server
		if(IPConfigData.AUTO_IP)
		{
			try (BufferedWriter out = new BufferedWriter(new FileWriter(new File(dirGame))))
			{
				/** @formatter:off */
				out.write(
					"##########################################################################################\r\n" +
					"# This is the server configuration file. Here you can set up your server.                #\r\n" +
					"# * you can use the NO-IP system for dynamic DNS > http://www.no-ip.com/                 #\r\n" +
					"# * if you want to restore default settings delete this file. and run the server         #\r\n" +
					"##########################################################################################\r\n" +
					"# Configure your External IP(WAN)\r\n" +
					"# Default: 127.0.0.1 (LOCALHOST)\r\n" +
					"ExternalHostname = " + IPConfigData.externalIp + "\r\n" +
					"\r\n" +
					"# Configure your Internal IP(LAN)\r\n" +
					"# Default: 127.0.0.1 (LOCALHOST)\r\n" +
					"InternalHostname = " + IPConfigData.sub.getIPAddress() + "\r\n" +
					"\r\n"+
					"# Bind IP of the gameserver, use * to bind on all available IPs\r\n" +
					"GameserverHostname =  *\r\n" +
					"GameserverPort = 7777\r\n" +
					"\r\n" +
					"# The Loginserver host and port\r\n" +
					"LoginPort = 9014\r\n" +
					"LoginHost = 127.0.0.1\r\n");
				/** @formatter:on */
			}
			catch (Exception e)
			{
				System.err.println("Network Config: could not create " + dirGame);
			}
		}
	}
}