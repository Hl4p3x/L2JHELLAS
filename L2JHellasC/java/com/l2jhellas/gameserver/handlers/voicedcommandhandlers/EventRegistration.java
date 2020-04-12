package com.l2jhellas.gameserver.handlers.voicedcommandhandlers;

import com.l2jhellas.gameserver.handler.IVoicedCommandHandler;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.entity.events.engines.EventManager;
import com.l2jhellas.gameserver.model.entity.olympiad.OlympiadManager;

public class EventRegistration implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"join",
		"leave"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{		
		if (OlympiadManager.getInstance().isRegisteredInComp(activeChar) || activeChar.isInOlympiadMode() || activeChar.getOlympiadGameId() > 0)
		{
			activeChar.sendMessage("You can't register while you are in olympiad!");
			return false;
		}
		
		if (command.startsWith(VOICED_COMMANDS[0]))
			EventManager.getInstance().registerPlayer(activeChar);		
		if (command.startsWith(VOICED_COMMANDS[1]))
			EventManager.getInstance().unregisterPlayer(activeChar);
		
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}