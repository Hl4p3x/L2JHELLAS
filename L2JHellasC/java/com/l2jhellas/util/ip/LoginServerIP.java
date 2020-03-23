package com.l2jhellas.util.ip;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import com.PackRoot;

public class LoginServerIP
{
	private static final String dirLogin = PackRoot.DATAPACK_ROOT + "/config/Network/IPConfig/IPLoginServer.ini";
	
	public static void load()
	{
		// login server
		if (IPConfigData.AUTO_IP)
		{
			try (BufferedWriter out = new BufferedWriter(new FileWriter(new File(dirLogin))))
			{
				/** @formatter:off */
				out.write(
					"##########################################################################################\r\n" +
					"# Server configuration file. Here you can set up the connection for your server.         #\r\n" +
					"# = you can use the NO-IP system for dynamic DNS > http://www.no-ip.com/                 #\r\n" +
					"# * if you want to restore default settings delete this file. and run the server         #\r\n" +
					"##########################################################################################\r\n" +
					"# Configure your External IP(WAN)\r\n" +
					"# Default: 127.0.0.1 (LOCALHOST)\r\n" +
					"ExternalHostname = " + IPConfigData.externalIp + "\r\n" +
					"\r\n"+
					"# Configure your Internal IP(LAN)\r\n" +
					"# Default: 127.0.0.1 (LOCALHOST)\r\n" +
					"InternalHostname = " + IPConfigData.sub.getIPAddress() + "\r\n" +
					"\r\n" +
					"# Bind ip of the LoginServer, use * to bind on all available IPs\r\n" +
					"LoginserverHostname = *\r\n" +
					"LoginserverPort = 2106\r\n" +
					"\r\n" +
					"# The address on which login will listen for GameServers, use * to bind on all available IPs\r\n" +
					"LoginHostname = 127.0.0.1\r\n" +
					"\r\n" +
					"# The port on which login will listen for GameServers\r\n" +
					"LoginPort = 9014");
					/** @formatter:on */
			}
			catch (Exception e)
			{
				System.err.println("Network Config: could not create " + dirLogin);
			}
		}
	}
}