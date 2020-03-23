package com.l2jhellas.gameserver.communitybbs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class CastleStatus
{
	protected static final Logger _log = Logger.getLogger(CastleStatus.class.getName());
	
	private static final String SELECT_CLAN_DATA = "SELECT clan_name,clan_level FROM clan_data WHERE hasCastle=";
	private static final String SELECT_CASTLE_DATA = "SELECT name,siegeDate,taxPercent FROM castle WHERE id=";
	
	private final StringBuilder _playerList = new StringBuilder();
	
	public CastleStatus()
	{
		loadFromDB();
	}
	
	private void loadFromDB()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			for (int i = 1; i < 9; i++)
			{
				PreparedStatement statement = con.prepareStatement(SELECT_CLAN_DATA + i);
				ResultSet result = statement.executeQuery();
				
				PreparedStatement statement2 = con.prepareStatement(SELECT_CASTLE_DATA + i);
				ResultSet result2 = statement2.executeQuery();
				
				while (result.next())
				{
					String owner = result.getString("clan_name");
					int level = result.getInt("clan_level");
					
					while (result2.next())
					{
						String name = result2.getString("name");
						long someLong = result2.getLong("siegeDate");
						int tax = result2.getInt("taxPercent");
						Date anotherDate = new Date(someLong);
						String DATE_FORMAT = "dd-MM-yyyy HH:mm";
						SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
						
						addCastleToList(name, owner, level, tax, sdf.format(anotherDate));
					}

				}
							
				result2.close();
				statement2.close();
				
				result.close();
				statement.close();
			}
		}
		
		catch (Exception e)
		{
			_log.warning(CastleStatus.class.getName() + ": Error loading db ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	private void addCastleToList(String name, String owner, int level, int tax, String siegeDate)
	{
		_playerList.append("<table border=0 cellspacing=0 cellpadding=2 width=610>");
		_playerList.append("<tr>");
		_playerList.append("<td FIXWIDTH=10></td>");
		_playerList.append("<td FIXWIDTH=100>" + name + "</td>");
		_playerList.append("<td FIXWIDTH=100>" + owner + "</td>");
		_playerList.append("<td FIXWIDTH=80>" + level + "</td>");
		_playerList.append("<td FIXWIDTH=40>" + tax + "</td>");
		_playerList.append("<td FIXWIDTH=180>" + siegeDate + "</td>");
		_playerList.append("<td FIXWIDTH=5></td>");
		_playerList.append("</tr>");
		_playerList.append("</table>");
		_playerList.append("<img src=\"L2UI.Squaregray\" width=\"610\" height=\"1\">");
	}
	
	public String loadCastleList()
	{
		return _playerList.toString();
	}
}