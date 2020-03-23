package com.l2jhellas.gameserver.handlers.usercommandhandlers;

import com.l2jhellas.gameserver.handler.IUserCommandHandler;
import com.l2jhellas.gameserver.instancemanager.SiegeManager;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.entity.Siege;
import com.l2jhellas.gameserver.model.zone.type.L2CastleZone;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.util.StringUtil;

public class SiegeStatus implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		99
	};
	
	private static final String IN_PROGRESS = "Castle Siege in Progress";
	private static final String OUTSIDE_ZONE = "Outside Castle Siege Zone";
	
	@Override
	public boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		if (COMMAND_IDS[0] != id)
			return false;
		
		if (!activeChar.isClanLeader())
		{
			activeChar.sendPacket(SystemMessageId.ONLY_CLAN_LEADER_CAN_ISSUE_COMMANDS);
			return false;
		}
		
		if (!activeChar.isNoble())
		{
			activeChar.sendPacket(SystemMessageId.ONLY_NOBLESSE_LEADER_CAN_VIEW_SIEGE_STATUS_WINDOW);
			return false;
		}
		
		final L2Clan clan = activeChar.getClan();
		
		// Used to build dynamic content (in that case, online clan members).
		StringBuilder content = new StringBuilder();
		
		for (Siege siege : SiegeManager.getInstance().getSieges())
		{
			// Search on lists : as a clan can only be registered in a single siege, break after one case is found.
			if (siege.getAttackerClan(clan.getClanId()) != null || siege.getDefenderClan(clan.getClanId()) != null)
			{
				if (!siege.getIsInProgress())
					break;
				
				final L2CastleZone zone = siege.getCastle().getZone();
				for (L2PcInstance member : clan.getOnlineMembers())
					StringUtil.append(content, "<tr><td width=170>", member.getName(), "</td><td width=100>", (zone.isInsideZone(member.getX(), member.getY(), member.getZ())) ? IN_PROGRESS : OUTSIDE_ZONE, "</td></tr>");
				
				NpcHtmlMessage html = new NpcHtmlMessage(0);
				html.setFile("data/html/siege/siege_status.htm");
				html.replace("%kills%", String.valueOf(clan.getSiegeKills()));
				html.replace("%deaths%", String.valueOf(clan.getSiegeDeaths()));
				html.replace("%content%", content.toString());
				activeChar.sendPacket(html);
				return true;
			}
		}
		
		activeChar.sendPacket(SystemMessageId.ONLY_DURING_SIEGE);
		return false;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}