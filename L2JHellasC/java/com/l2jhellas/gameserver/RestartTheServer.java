package com.l2jhellas.gameserver;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class RestartTheServer
{
	public static void playerRestart(L2PcInstance activeChar, boolean restart)
	{
		RestartVoteVariable e = new RestartVoteVariable();
		
		if (e.getVoteCount("restart") > Config.VOTES_NEEDED_FOR_RESTART)
			Shutdown.getInstance().startShutdown(activeChar, null, 60, restart);
	}
}