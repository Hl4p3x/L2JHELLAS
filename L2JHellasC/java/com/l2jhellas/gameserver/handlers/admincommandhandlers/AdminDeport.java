package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class AdminDeport implements IAdminCommandHandler
{
	private static String[] ADMIN_COMMANDS =
	{
		"admin_deport"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{	
		final L2PcInstance player = activeChar.getTarget().getActingPlayer();
		
		if (command.startsWith(ADMIN_COMMANDS[0]) && player != null)
			player.teleToLocation(82698, 148638, -3473);
		
		return false;	
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}