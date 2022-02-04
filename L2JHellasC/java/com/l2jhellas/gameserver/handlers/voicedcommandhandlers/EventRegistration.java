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
		"leave",
		"vote"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{		
		if (OlympiadManager.getInstance().isRegisteredInComp(activeChar) || activeChar.isInOlympiadMode() || activeChar.getOlympiadGameId() > 0)
		{
			activeChar.sendMessage("You can't register while you are in olympiad!");
			return false;
		}
		
		switch(command)
		{
			case "join":
				EventManager.getInstance().registerPlayer(activeChar);	
				break;
			case "leave":
				EventManager.getInstance().unregisterPlayer(activeChar);
				break;
			case "vote":
				EventManager.getInstance().showVoteHtml(activeChar);
				break;
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}