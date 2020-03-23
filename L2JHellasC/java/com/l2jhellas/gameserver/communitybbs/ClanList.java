package com.l2jhellas.gameserver.communitybbs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class ClanList
{
	protected static final Logger _log = Logger.getLogger(ClanList.class.getName());
	
	private static final String SELECT_CLAN_DATA = "SELECT * FROM clan_data ORDER BY clan_level DESC LIMIT ";
	private static final String SELECT_CASTLE = "SELECT name FROM castle WHERE id=";
	private static final String SELECT_CHARNAME = "SELECT char_name FROM characters WHERE obj_Id=";
	
	private final StringBuilder _clanList = new StringBuilder();
	
	public ClanList(int type)
	{
		loadFromDB(type);
	}
	
	private void loadFromDB(int type)
	{
		int stpoint = 0;
		int results = 20;
		String castlename = "";
		String allystatus = "";
		String leadername = "";
		for (int count = 1; count != type; count++)
		{
			stpoint += 20;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(SELECT_CLAN_DATA + stpoint + ", " + results);
			ResultSet result = statement.executeQuery();
			int pos = 0;
			
			while (result.next())
			{
				int clanid = result.getInt("leader_id");
				String clan = result.getString("clan_name");
				String ally = result.getString("ally_name");
				int clanleader = result.getInt("leader_id");
				int clanlevel = result.getInt("clan_level");
				int reputation = result.getInt("reputation_score");
				int hascastle = result.getInt("hasCastle");
				int allyid = result.getInt("ally_id");
				if (allyid != 0)
				{
					if (allyid == clanid)
						allystatus = "Alliance Leader";
					allystatus = "Affiliated Clan";
				}
				else
				{
					allystatus = "-";
					ally = "[no-ally]";
				}
				if (hascastle != 0)
				{
					PreparedStatement statement2 = con.prepareStatement(SELECT_CASTLE + hascastle);
					ResultSet result2 = statement2.executeQuery();
					if (result2.next())
						castlename = result2.getString("name");
					result2.close();
					statement2.close();
				}
				else
					castlename = "[none]";
				PreparedStatement statement3 = con.prepareStatement(SELECT_CHARNAME + clanleader);
				ResultSet result3 = statement3.executeQuery();
				
				if (result3.next())
					leadername = result3.getString("char_name");
				result3.close();
				statement3.close();
				pos++;
				addClanToList(pos, clan, ally, leadername, clanlevel, reputation, castlename, allystatus);
			}
			result.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning(ClanList.class.getName() + ": Error loading DB ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	private void addClanToList(int pos, String clan, String ally, String leadername, int clanlevel, int reputation, String castlename, String allystatus)
	{
		_clanList.append("<table border=0 cellspacing=0 cellpadding=2 width=610>");
		_clanList.append("<tr>");
		_clanList.append("<td FIXWIDTH=5></td>");
		_clanList.append("<td FIXWIDTH=20>" + pos + "</td>");
		_clanList.append("<td FIXWIDTH=90>" + clan + "</td>");
		_clanList.append("<td FIXWIDTH=90>" + ally + "</td>");
		_clanList.append("<td FIXWIDTH=85>" + leadername + "</td>");
		_clanList.append("<td FIXWIDTH=45 align=center>" + clanlevel + "</td>");
		_clanList.append("<td FIXWIDTH=70 align=center>" + reputation + "</td>");
		_clanList.append("<td FIXWIDTH=50 align=center>" + castlename + "</td>");
		_clanList.append("<td FIXWIDTH=70 align=center>" + allystatus + "</td>");
		_clanList.append("<td FIXWIDTH=5></td>");
		_clanList.append("</tr>");
		_clanList.append("</table>");
		_clanList.append("<img src=\"L2UI.Squaregray\" width=\"610\" height=\"1\">");
	}
	
	public String loadClanList()
	{
		return _clanList.toString();
	}
}