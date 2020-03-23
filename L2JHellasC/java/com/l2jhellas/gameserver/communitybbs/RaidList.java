package com.l2jhellas.gameserver.communitybbs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class RaidList
{
	protected static final Logger _log = Logger.getLogger(RaidList.class.getName());
	
	private static final String SELECT_RAID_DATA = "SELECT id, name, level FROM npc WHERE type='L2RaidBoss' AND EXISTS (SELECT * FROM raidboss_spawnlist WHERE raidboss_spawnlist.boss_id = npc.id) ORDER BY `level` ";
	private static final String SELECT_SPAWN = "SELECT respawn_time, respawn_min_delay, respawn_max_delay FROM raidboss_spawnlist WHERE boss_id=";
	
	private final StringBuilder _raidList = new StringBuilder();
	
	public RaidList(String rfid)
	{
		loadFromDB(rfid);
	}
	
	private void loadFromDB(String rfid)
	{
		int type = Integer.parseInt(rfid);
		int stpoint = 0;
		int pos = 0;
		String sort = "";
		if (Config.RAID_LIST_SORT_ASC)
			sort = "ASC";
		else
			sort = "DESC";
		for (int count = 1; count != type; count++)
		{
			stpoint += Config.RAID_LIST_RESULTS;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(SELECT_RAID_DATA + sort + " Limit " + stpoint + ", " + Config.RAID_LIST_RESULTS);
			ResultSet result = statement.executeQuery();
			pos = stpoint;
			
			while (result.next())
			{
				int npcid = result.getInt("id");
				String npcname = result.getString("name");
				int rlevel = result.getInt("level");
				PreparedStatement statement2 = con.prepareStatement(SELECT_SPAWN + npcid);
				ResultSet result2 = statement2.executeQuery();
				
				while (result2.next())
				{
					pos++;
					boolean rstatus = false;
					long respawn = result2.getLong("respawn_time");
					if (respawn == 0)
						rstatus = true;
					int mindelay = result2.getInt("respawn_min_delay");
					int maxdelay = result2.getInt("respawn_max_delay");
					mindelay = mindelay / 60 / 60;
					maxdelay = maxdelay / 60 / 60;
					addRaidToList(pos, npcname, rlevel, mindelay, maxdelay, rstatus);
				}
				result2.close();
				statement2.close();
			}
			
			result.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning(RaidList.class.getName() + ": Error Loading DB ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	private void addRaidToList(int pos, String npcname, int rlevel, int mindelay, int maxdelay, boolean rstatus)
	{
		_raidList.append("<table border=0 cellspacing=0 cellpadding=2 width=610 height=" + Config.RAID_LIST_ROW_HEIGHT + ">");
		_raidList.append("<tr>");
		_raidList.append("<td FIXWIDTH=5></td>");
		_raidList.append("<td FIXWIDTH=25>" + pos + "</td>");
		_raidList.append("<td FIXWIDTH=270>" + npcname + "</td>");
		_raidList.append("<td FIXWIDTH=50>" + rlevel + "</td>");
		_raidList.append("<td FIXWIDTH=120 align=center>" + mindelay + " - " + maxdelay + "</td>");
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