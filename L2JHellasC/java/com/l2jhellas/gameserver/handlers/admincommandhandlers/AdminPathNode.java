package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class AdminPathNode implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_pn_info",
		"admin_show_path",
		"admin_path_debug",
		"admin_show_pn",
		"admin_find_path"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.equals("admin_pn_info"))
		{
			
		}
		else if (command.equals("admin_show_path"))
		{
			
		}
		else if (command.equals("admin_path_debug"))
		{
			
		}
		else if (command.equals("admin_show_pn"))
		{
			
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}