package com.l2jhellas.gameserver.communitybbs;

import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.sql.NpcData;
import com.l2jhellas.gameserver.instancemanager.GrandBossManager;
import com.l2jhellas.gameserver.templates.StatsSet;

public class GrandBossList
{
	protected static final Logger _log = Logger.getLogger(GrandBossList.class.getName());
	
	private final StringBuilder _GrandBossList = new StringBuilder();
	
	public GrandBossList()
	{
		load();
	}
	
	private void load()
	{
		int pos = 0;
		
		for (final int npcId : Config.BOSS_RESPAWN_INFO)
		{
			final StatsSet stats = GrandBossManager.getStatsSet(npcId);
			
			if (stats == null)
				continue;
			
			final String name = NpcData.getInstance().getTemplate(npcId).getName();
			final long delay = stats.getLong("respawn_time");
			final long currentTime = System.currentTimeMillis();
			boolean alive = delay <= currentTime;							
			addGrandBossToList(pos++,name,alive);
		}
	}
	
	private void addGrandBossToList(int pos, String npcname, boolean rstatus)
	{
		_GrandBossList.append("<table border=0 cellspacing=0 cellpadding=2>");
		_GrandBossList.append("<tr>");
		_GrandBossList.append("<td FIXWIDTH=5></td>");
		_GrandBossList.append("<td FIXWIDTH=50>" + pos + "</td>");
		_GrandBossList.append("<td FIXWIDTH=130>" + npcname + "</td>");
		_GrandBossList.append("<td FIXWIDTH=60 align=center>" + ((rstatus) ? "<font color=99FF00>Alive</font>" : "<font color=CC0000>Dead</font>") + "</td>");
		_GrandBossList.append("<td FIXWIDTH=5></td>");
		_GrandBossList.append("</tr>");
		_GrandBossList.append("</table>");
		_GrandBossList.append("<img src=\"L2UI.Squaregray\" width=\"250\" height=\"1\">");
	}
	
	public String loadGrandBossList()
	{
		return _GrandBossList.toString();
	}
}