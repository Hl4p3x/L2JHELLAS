package com.l2jhellas.gameserver.handlers.voicedcommandhandlers;

import com.l2jhellas.gameserver.Announcements;
import com.l2jhellas.gameserver.RestartVoteVariable;
import com.l2jhellas.gameserver.handler.IVoicedCommandHandler;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class ServerRestartVoteCmd implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"vote_restart"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		RestartVoteVariable e = new RestartVoteVariable();
		
		if (command.startsWith(VOICED_COMMANDS[0]))
		{
			if (activeChar._voteRestart == false)
			{
				e.increaseVoteCount("restart");
				activeChar._voteRestart = true;
				activeChar.sendMessage("You succesfully voted for the server restart. Votes For The Moment: " + e.getVoteCount("tvt") + ".");
				Announcements.getInstance().announceToAll("Player: " + activeChar.getName() + " has voted for server restart. If you whant to support him type .vote_restart !");
			}
			else
				activeChar.sendMessage("You have already voted for an server restart.");
		}
		return false;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}