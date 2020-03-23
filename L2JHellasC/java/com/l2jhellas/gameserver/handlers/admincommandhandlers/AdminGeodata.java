package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.geodata.GeoEngine;
import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class AdminGeodata implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_geo_z",
		"admin_geo_type",
		"admin_geo_nswe",
		"admin_geo_los",
		"admin_geo_position",
		"admin_geo_bug",
		"admin_geo_load",
		"admin_geo_unload"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!Config.GEODATA)
		{
			activeChar.sendMessage("Geo Engine is Turned Off!");
			AdminHelpPage.showHelpPage(activeChar, "server_menu.htm");
			return true;
		}
		
		if (command.equals("admin_geo_z"))
		{
			activeChar.sendMessage("GeoEngine: Geo_Z = " + GeoEngine.getHeight(activeChar.getX(), activeChar.getY(), activeChar.getZ()) + " Loc_Z = " + activeChar.getZ());
			AdminHelpPage.showHelpPage(activeChar, "server_menu.htm");
		}
		else if (command.equals("admin_geo_type"))
		{
			short type = GeoEngine.getType(activeChar.getX(), activeChar.getY());
			activeChar.sendMessage("GeoEngine: Geo_Type = " + type);
			int height = GeoEngine.getHeight(activeChar.getX(), activeChar.getY(), activeChar.getZ());
			activeChar.sendMessage("GeoEngine: height = " + height);
			AdminHelpPage.showHelpPage(activeChar, "server_menu.htm");
		}
		else if (command.equals("admin_geo_nswe"))
		{
			String result = "";
			short nswe = GeoEngine.getNSWE(activeChar.getX(), activeChar.getY(), activeChar.getZ());
			if ((nswe & 8) == 0)
				result += " N";
			if ((nswe & 4) == 0)
				result += " S";
			if ((nswe & 2) == 0)
				result += " W";
			if ((nswe & 1) == 0)
				result += " E";
			activeChar.sendMessage("GeoEngine: Geo_NSWE -> " + nswe + "->" + result);
			AdminHelpPage.showHelpPage(activeChar, "server_menu.htm");
		}
		else if (command.equals("admin_geo_los"))
		{
			if (activeChar.getTarget() != null)
			{
				if (GeoEngine.canSeeTarget(activeChar, activeChar.getTarget(), false))
					activeChar.sendMessage("GeoEngine: Can See Target");
				else
					activeChar.sendMessage("GeoEngine: Can't See Target");
				
			}
			else
				activeChar.sendMessage("None Target!");
			AdminHelpPage.showHelpPage(activeChar, "server_menu.htm");
		}
		else if (command.equals("admin_geo_position"))
		{
			activeChar.sendMessage("GeoEngine: Your current position: ");
			activeChar.sendMessage(".... world coords: x: " + activeChar.getX() + " y: " + activeChar.getY() + " z: " + activeChar.getZ());
			AdminHelpPage.showHelpPage(activeChar, "server_menu.htm");
		}
		else if (command.startsWith("admin_geo_load"))
		{
			String[] v = command.substring(15).split(" ");
			if (v.length != 2)
				activeChar.sendMessage("Usage: //admin_geo_load <regionX> <regionY>");
			else
			{
				try
				{
					byte rx = Byte.parseByte(v[0]);
					byte ry = Byte.parseByte(v[1]);
					
					boolean result = GeoEngine.LoadGeodataFile(rx, ry);
					
					if (result)
						activeChar.sendMessage("GeoEngine: File for region [" + rx + "," + ry + "] loaded succesfuly");
					else
						activeChar.sendMessage("GeoEngine: File for region [" + rx + "," + ry + "] couldn't be loaded");
				}
				catch (Exception e)
				{
					activeChar.sendMessage("You have to write numbers of regions <regionX> <regionY>");
				}
			}
			AdminHelpPage.showHelpPage(activeChar, "server_menu.htm");
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}