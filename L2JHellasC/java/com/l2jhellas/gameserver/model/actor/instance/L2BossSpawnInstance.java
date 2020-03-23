package com.l2jhellas.gameserver.model.actor.instance;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.datatables.sql.NpcData;
import com.l2jhellas.gameserver.instancemanager.GrandBossManager;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;
import com.l2jhellas.gameserver.templates.StatsSet;

public class L2BossSpawnInstance extends L2Npc
{
	private static final SimpleDateFormat Time = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	
	public L2BossSpawnInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player))
		{
			return;
		}
		
		if (this != player.getTarget())
		{
			player.setTarget(this);
			
			player.sendPacket(new MyTargetSelected(getObjectId(), 0));
		}
		else if (!canInteract(player))
		{
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
		}
		else
		{
			showHtmlWindow(player);
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private void showHtmlWindow(L2PcInstance activeChar)
	{
		showRbInfo(activeChar);
		
		activeChar.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	private final void showRbInfo(L2PcInstance player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		StringBuilder tb = new StringBuilder();
		tb.append("<html><title>Boss Info</title><body><br><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32>");
		
		for (final int boss : Config.BOSS_RESPAWN_INFO)
		{
			final String name = NpcData.getInstance().getTemplate(boss).getName();
			final StatsSet stats = GrandBossManager.getStatsSet(boss);
			if (stats == null)
			{
				player.sendMessage("Stats for GrandBoss " + boss + " not found!");
				continue;
			}
			final long delay = stats.getLong("respawn_time");
			final long currentTime = System.currentTimeMillis();
			if (delay <= currentTime)
			{
				tb.append("<center><table width=\"280\">");
				tb.append("<tr><td width=\"140\"><font color=\"00C3FF\">" + name + "</font>:</td><td width=\"80\" align=\"right\"> " + "<font color=\"9CC300\">Is Alive</font>" + "</td></tr></table><br1>");
			}
			else
			{
				tb.append("<center><table width=\"280\">");
				tb.append("<tr><td width=\"95\"><font color=\"00C3FF\">" + name + "</font>:</td><td width=\"165\" align=\"right\"> " + "<font color=\"FF0000\">Is Dead");
				if (Config.RAID_INFO_SHOW_TIME)
					tb.append("" + " " + "" + "</font>" + " " + " <font color=\"32C332\">" + Time.format(new Date(delay)) + "</font>" + "</td></tr></table><br1>");
				else
					tb.append("</font></td></tr></table>");
			}
		}
		tb.append("<br><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32></center></body></html>");
		
		html.setHtml(tb.toString());
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
}