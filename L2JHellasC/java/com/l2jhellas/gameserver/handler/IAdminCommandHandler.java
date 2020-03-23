package com.l2jhellas.gameserver.handler;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public interface IAdminCommandHandler
{
	
	public boolean useAdminCommand(String command, L2PcInstance activeChar);
	
	public String[] getAdminCommandList();
}