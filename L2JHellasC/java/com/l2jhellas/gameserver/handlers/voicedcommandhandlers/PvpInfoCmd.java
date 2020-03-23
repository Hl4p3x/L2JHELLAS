package com.l2jhellas.gameserver.handlers.voicedcommandhandlers;

import Extensions.RankSystem.RPSCookie;
import Extensions.RankSystem.RPSHtmlPvpStatus;

import com.l2jhellas.gameserver.handler.IVoicedCommandHandler;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class PvpInfoCmd implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"pvpinfo"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.startsWith(VOICED_COMMANDS[0]))
		{
			if (activeChar == null)
				return false;
			
			if (activeChar.getRPSCookie() == null)
				return false;
			
			final RPSCookie pc = activeChar.getRPSCookie();
			
			// reset death status:
			if (!activeChar.isDead())
				pc.setDeathStatus(null);
			
			// save target of active player when command executed:
			if (activeChar.getTarget() != null && activeChar.getTarget() instanceof L2PcInstance)
				pc.setTarget((L2PcInstance) activeChar.getTarget());
			else
				pc.setTarget(activeChar);
						
			RPSHtmlPvpStatus.sendPage(activeChar, pc.getTarget());
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
	
}