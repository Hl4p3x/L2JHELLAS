package com.l2jhellas.gameserver.handler;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public interface IUserCommandHandler
{
	
	public boolean useUserCommand(int id, L2PcInstance activeChar);
	
	public int[] getUserCommandList();
}