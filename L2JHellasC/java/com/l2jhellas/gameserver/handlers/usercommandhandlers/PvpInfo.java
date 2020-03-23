package com.l2jhellas.gameserver.handlers.usercommandhandlers;

import Extensions.RankSystem.RPSCookie;
import Extensions.RankSystem.RPSHtmlPvpStatus;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.handler.IUserCommandHandler;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class PvpInfo implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		Config.PVP_INFO_USER_COMMAND_ID
	};
	
	@Override
	public boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		if (id != COMMAND_IDS[0])
		{
			return false;
		}
		
		if (activeChar == null)
			return false;
		
		if (activeChar.getRPSCookie() == null)
			return false;
		
		RPSCookie pc = activeChar.getRPSCookie();
		
		// reset death status:
		if (!activeChar.isDead())
		{
			pc.setDeathStatus(null);
		}
		
		// save target of active player when command executed:
		if (activeChar.getTarget() != null && activeChar.getTarget() instanceof L2PcInstance)
		{
			pc.setTarget((L2PcInstance) activeChar.getTarget());
		}
		else
		{
			pc.setTarget(activeChar);
			activeChar.sendMessage("PvP Status executed on self!");
		}
		
		RPSHtmlPvpStatus.sendPage(activeChar, pc.getTarget());
		
		return true;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
	
}