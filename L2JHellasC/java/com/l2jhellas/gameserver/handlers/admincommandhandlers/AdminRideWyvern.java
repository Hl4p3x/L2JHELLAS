package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class AdminRideWyvern implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_ride_wyvern",
		"admin_ride_strider",
		"admin_unride_wyvern",
		"admin_unride_strider",
		"admin_unride"
	};
	private int _petRideId;
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_ride"))
		{
			if (activeChar.isCursedWeaponEquiped())
			{
				activeChar.sendMessage("You can't use //ride owning a Cursed Weapon.");
				return false;
			}
			
			if (command.startsWith("admin_ride_wyvern"))
				_petRideId = 12621;
			else if (command.startsWith("admin_ride_strider"))
				_petRideId = 12526;
			else
			{
				activeChar.sendMessage("Command '" + command + "' not recognized");
				return false;
			}
			
			if (activeChar.isMounted())
				activeChar.dismount();
			
			activeChar.mount(_petRideId);
		}
		else if (command.equals("admin_unride"))
			activeChar.dismount();
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}