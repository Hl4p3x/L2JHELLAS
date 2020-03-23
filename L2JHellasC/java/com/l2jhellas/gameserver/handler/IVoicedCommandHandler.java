package com.l2jhellas.gameserver.handler;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public interface IVoicedCommandHandler
{
	
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target);
	
	public String[] getVoicedCommandList();
}