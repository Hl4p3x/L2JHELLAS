package com.l2jhellas.gameserver.communitybbs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.instancemanager.RaidBossSpawnManager;
import com.l2jhellas.gameserver.model.actor.instance.L2RaidBossInstance;
import com.l2jhellas.gameserver.templates.StatsSet;

public class RaidList
{
	protected static final Logger _log = Logger.getLogger(RaidList.class.getName());
	private static final SimpleDateFormat Time = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	private final StringBuilder _raidList = new StringBuilder();

	public RaidList(String rfid)
	{
		loadFromDB(rfid);
	}
	
	private void loadFromDB(String rfid)
	{
		int type = Integer.parseInt(rfid);

		List<L2RaidBossInstance> rbs = RaidBossSpawnManager.getInstance().getBosses().values().stream().filter(Objects::nonNull).sorted((x1, x2) -> Integer.compare(x2.getLevel(),x1.getLevel())).collect(Collectors.toList());
		rbs = rbs.subList((type - 1) * 20, Math.min(type * 20, rbs.size()));

	    AtomicInteger counter = new AtomicInteger(0);

        rbs.forEach(rb ->
		{		
			final StatsSet info = RaidBossSpawnManager.getStatsSet(rb.getNpcId());
			long respawn = 	info.getLong("respawnTime");
			final long currentTime = System.currentTimeMillis();
			boolean alive = respawn <= currentTime;							

			addRaidToList(counter.incrementAndGet(),rb.getName(), rb.getLevel(), respawn, alive);
		});		
	}
	
	private void addRaidToList(int pos, String npcname, int rlevel, long delay, boolean rstatus)
	{
		_raidList.append("<table border=0 cellspacing=0 cellpadding=2 width=610 height=" + Config.RAID_LIST_ROW_HEIGHT + ">");
		_raidList.append("<tr>");
		_raidList.append("<td FIXWIDTH=5></td>");
		_raidList.append("<td FIXWIDTH=25>" + pos + "</td>");
		_raidList.append("<td FIXWIDTH=270>" + npcname + "</td>");
		_raidList.append("<td FIXWIDTH=50>" + rlevel + "</td>");		
		_raidList.append("<td FIXWIDTH=120 align=center>" + (rstatus ? "-" : Time.format(new Date(delay))) + "</td>");
		_raidList.append("<td FIXWIDTH=50 align=center>" + ((rstatus) ? "<font color=99FF00>Alive</font>" : "<font color=CC0000>Dead</font>") + "</td>");
		_raidList.append("<td FIXWIDTH=5></td>");
		_raidList.append("</tr>");
		_raidList.append("</table>");
		_raidList.append("<img src=\"L2UI.Squaregray\" width=\"610\" height=\"1\">");
	}
	
	public String loadRaidList()
	{
		return _raidList.toString();
	}
}