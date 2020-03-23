package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import java.util.StringTokenizer;

import com.l2jhellas.gameserver.communitybbs.Manager.RegionBBSManager;
import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.LeaveWorld;

public class AdminKick implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_kick",
		"admin_kick_non_gm"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_kick"))
		{
			StringTokenizer st = new StringTokenizer(command);
			if (st.countTokens() > 1)
			{
				st.nextToken();
				String player = st.nextToken();
				L2PcInstance plyr = L2World.getInstance().getPlayer(player);
				if (plyr != null)
				{
					plyr.sendPacket(new LeaveWorld());
					plyr.closeNetConnection(false);
					activeChar.sendMessage("You kicked " + plyr.getName() + " from the game.");
					RegionBBSManager.getInstance().changeCommunityBoard();
				}
			}
		}
		if (command.startsWith("admin_kick_non_gm"))
		{
			int counter = 0;
			for (L2PcInstance player : L2World.getInstance().getAllPlayers().values())
			{
				if (!player.isGM())
				{
					counter++;
					player.sendPacket(new LeaveWorld());
					player.closeNetConnection(false);
					RegionBBSManager.getInstance().changeCommunityBoard();
				}
			}
			activeChar.sendMessage("Kicked " + counter + " players");
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}