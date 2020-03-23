package com.l2jhellas.gameserver.communitybbs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.instancemanager.games.Lottery;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class GrandBossList
{
	protected static final Logger _log = Logger.getLogger(Lottery.class.getName());
	
	private static final String SELECT_BOSS = "SELECT boss_id, status FROM grandboss_data";
	private static final String SELECT_NAME = "SELECT name FROM npc WHERE id=";
	private final StringBuilder _GrandBossList = new StringBuilder();
	
	public GrandBossList()
	{
		loadFromDB();
	}
	
	private void loadFromDB()
	{
		int pos = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(SELECT_BOSS);
			ResultSet result = statement.executeQuery();
			
			nextnpc:
			while (result.next())
			{
				int npcid = result.getInt("boss_id");
				int status = result.getInt("status");
				if (npcid == 29066 || npcid == 29067 || npcid == 29068 || npcid == 29118)
					continue nextnpc;
				
				PreparedStatement statement2 = con.prepareStatement(SELECT_NAME + npcid);
				ResultSet result2 = statement2.executeQuery();
				
				while (result2.next())
				{
					pos++;
					boolean rstatus = false;
					if (status == 0)
						rstatus = true;
					String npcname = result2.getString("name");
					addGrandBossToList(pos, npcname, rstatus);
				}
				result2.close();
				statement2.close();
			}
			
			result.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning(GrandBossList.class.getName() + ": Error Loading DB ");
			if (Config.DEVELOPER)
				e.printStackTrace();
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