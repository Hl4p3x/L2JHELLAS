package com.l2jhellas.util.ip;

import info.tak11.subnet.Subnet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet6Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import com.PackRoot;
import com.l2jhellas.Config;

public class IPConfigData
{
	protected static final Logger _log = Logger.getLogger(IPConfigData.class.getName());
	
	private static final List<String> _subnets = new ArrayList<>(5);
	private static final List<String> _hosts = new ArrayList<>(5);
	
	public static String externalIp = "127.0.0.1";
	public static Subnet sub = new Subnet();
	public static boolean AUTO_IP;
	
	private static void config()
	{
		Properties IPSettings = new Properties();
		try (InputStream is = new FileInputStream(new File(PackRoot.DATAPACK_ROOT, "config/Network/AutomaticIP.ini")))
		{
			IPSettings.load(is);
		}
		catch (Exception e)
		{
			_log.warning(IPConfigData.class.getSimpleName() + ": Error while config/Network/AutomaticIP.ini settings!");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
		AUTO_IP = Boolean.parseBoolean(IPSettings.getProperty("AutomaticIP", "True"));
	}
	
	public static void load()
	{
		config();
		if (AUTO_IP == false)
		{
			_log.info("Network Config: Manual Configuration.");
		}
		else
		// Auto configuration...
		{
			autoIpConfig();
		}
	}
	
	public static void autoIpConfig()
	{
		try
		{
			URL autoIp = new URL("http://ip1.dynupdate.no-ip.com:8245/");
			try (BufferedReader in = new BufferedReader(new InputStreamReader(autoIp.openStream())))
			{
				externalIp = in.readLine();
			}
		}
		catch (IOException e)
		{
			_log.warning("Network Config: Failed to connect to api.externalip.net please check your internet connection using 127.0.0.1!");
			externalIp = "127.0.0.1";
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
		
		try
		{
			Enumeration<NetworkInterface> niList = NetworkInterface.getNetworkInterfaces();
			
			while (niList.hasMoreElements())
			{
				NetworkInterface ni = niList.nextElement();
				
				if (!ni.isUp() || ni.isVirtual())
				{
					continue;
				}
				
				if (!ni.isLoopback() && ((ni.getHardwareAddress() == null) || (ni.getHardwareAddress().length != 6)))
				{
					continue;
				}
				
				for (InterfaceAddress ia : ni.getInterfaceAddresses())
				{
					if (ia.getAddress() instanceof Inet6Address)
					{
						continue;
					}
					
					sub.setIPAddress(ia.getAddress().getHostAddress());
					sub.setMaskedBits(ia.getNetworkPrefixLength());
					String subnet = sub.getSubnetAddress() + '/' + sub.getMaskedBits();
					if (!_subnets.contains(subnet) && !subnet.equals("0.0.0.0/0"))
					{
						_subnets.add(subnet);
						_hosts.add(sub.getIPAddress());
						_log.info("Network Config: Adding new subnet: " + subnet + " address: " + sub.getIPAddress());
					}
				}
			}
			
			// External host and subnet
			_hosts.add(externalIp);
			_subnets.add("0.0.0.0/0");
			_log.info("Network Config: Adding new subnet: 0.0.0.0/0 address: " + externalIp);
		}
		catch (SocketException e)
		{
			_log.warning("Network Config: Configuration failed please configure manually using IPGameServer.ini and IPLoginServer.ini");
			if (Config.DEVELOPER)
				e.printStackTrace();
			System.exit(0);
		}
	}
	
	protected List<String> getSubnets()
	{
		if (_subnets.isEmpty())
		{
			return Arrays.asList("0.0.0.0/0");
		}
		return _subnets;
	}
	
	protected List<String> getHosts()
	{
		if (_hosts.isEmpty())
		{
			return Arrays.asList("127.0.0.1");
		}
		return _hosts;
	}
}