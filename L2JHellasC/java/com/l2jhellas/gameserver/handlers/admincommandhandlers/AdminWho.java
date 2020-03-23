package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class AdminWho implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_who"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.equalsIgnoreCase("admin_who"))
			activeChar.sendMessage("SYS: Online Players:" + L2World.getInstance().getAllPlayers().size() + "/" + Config.MAXIMUM_ONLINE_USERS + ".");

		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}