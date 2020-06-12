package com.l2jhellas.gameserver.communitybbs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class TopPlayers
{
	protected static final Logger _log = Logger.getLogger(TopPlayers.class.getName());
	
	private static final String SELECT_CHARS = "SELECT SUM(chr.points), SUM(it.count), ch.char_name, ch.pkkills, ch.pvpkills, ch.onlinetime, ch.base_class, ch.online FROM characters ch LEFT JOIN character_raid_points chr ON ch.obj_Id=ch.obj_Id LEFT OUTER JOIN items it ON ch.obj_Id=it.owner_id WHERE item_id=57 GROUP BY ch.obj_Id ORDER BY ";
	
	private int pos;
	private final StringBuilder _topList = new StringBuilder();
	String sort = "";
	
	public TopPlayers(String file)
	{
		loadDB(file);
	}
	
	private void loadDB(String file)
	{
		switch (file)
		{
			case "toppvp":
				sort = "pvpkills";
				break;
			case "toppk":
				sort = "pkkills";
				break;
			case "topadena":
				sort = "SUM(it.count)";
				break;
			case "toprbrank":
				sort = "SUM(chr.points)";
				break;
			case "toponline":
				sort = "onlinetime";
				break;
			default:
				break;
		
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
		PreparedStatement statement = con.prepareStatement(SELECT_CHARS + sort + " DESC LIMIT " + Config.TOP_PLAYER_RESULTS))
		{
			pos = 0;
						
			try (ResultSet result = statement.executeQuery())
			{
				while (result.next())
				{
					boolean status = false;
					pos++;

					if (result.getInt("online") == 1)
						status = true;
					
					String timeon = getPlayerRunTime(result.getInt("ch.onlinetime"));
					String adenas = getAdenas(result.getLong("SUM(it.count)"));
					addChar(pos, result.getString("ch.char_name"), result.getInt("base_class"), result.getInt("ch.pvpkills"), result.getInt("ch.pkkills"), result.getInt("SUM(chr.points)"), adenas, timeon, status);
				}
			}
		}
		catch (Exception e)
		{
			_log.warning(TopPlayers.class.getName() + ": Could not Select Top Players ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public String loadTopList()
	{
		return _topList.toString();
	}
	
	private void addChar(int position, String name, int classid, int pvp, int pk, int raid, String adenas, String online, boolean isOnline)
	{
		_topList.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=050505 height=" + Config.TOP_PLAYER_ROW_HEIGHT + "><tr><td FIXWIDTH=5></td>");
		_topList.append("<td FIXWIDTH=27>" + position + ".</td>");
		_topList.append("<td FIXWIDTH=160>" + name + "</td>");
		_topList.append("<td FIXWIDTH=145>" + className(classid) + "</td>");
		_topList.append("<td FIXWIDTH=60>" + pvp + "</td>");
		_topList.append("<td FIXWIDTH=60>" + pk + "</td>");
		_topList.append("<td FIXWIDTH=60>" + raid + "</td>");
		_topList.append("<td FIXWIDTH=150>" + adenas + "</td>");
		_topList.append("<td FIXWIDTH=150>" + online + "</td>");
		_topList.append("<td FIXWIDTH=65>" + ((isOnline) ? "<font color=99FF00>Online</font>" : "<font color=CC0000>Offline</font>") + "</td>");
		_topList.append("</tr></table><img src=\"L2UI.Squaregray\" width=\"610\" height=\"1\">");
	}
	
	public final static String className(int classid)
	{
		Map<Integer, String> classList;
		classList = new HashMap<>();
		classList.put(0, "Fighter");
		classList.put(1, "Warrior");
		classList.put(2, "Gladiator");
		classList.put(3, "Warlord");
		classList.put(4, "Knight");
		classList.put(5, "Paladin");
		classList.put(6, "Dark Avenger");
		classList.put(7, "Rogue");
		classList.put(8, "Treasure Hunter");
		classList.put(9, "Hawkeye");
		classList.put(10, "Mage");
		classList.put(11, "Wizard");
		classList.put(12, "Sorcerer");
		classList.put(13, "Necromancer");
		classList.put(14, "Warlock");
		classList.put(15, "Cleric");
		classList.put(16, "Bishop");
		classList.put(17, "Prophet");
		classList.put(18, "Elven Fighter");
		classList.put(19, "Elven Knight");
		classList.put(20, "Temple Knight");
		classList.put(21, "Swordsinger");
		classList.put(22, "Elven Scout");
		classList.put(23, "Plains Walker");
		classList.put(24, "Silver Ranger");
		classList.put(25, "Elven Mage");
		classList.put(26, "Elven Wizard");
		classList.put(27, "Spellsinger");
		classList.put(28, "Elemental Summoner");
		classList.put(29, "Oracle");
		classList.put(30, "Elder");
		classList.put(31, "Dark Fighter");
		classList.put(32, "Palus Knightr");
		classList.put(33, "Shillien Knight");
		classList.put(34, "Bladedancer");
		classList.put(35, "Assasin");
		classList.put(36, "Abyss Walker");
		classList.put(37, "Phantom Ranger");
		classList.put(38, "Dark Mage");
		classList.put(39, "Dark Wizard");
		classList.put(40, "Spellhowler");
		classList.put(41, "Phantom Summoner");
		classList.put(42, "Shillien Oracle");
		classList.put(43, "Shilien Elder");
		classList.put(44, "Orc Fighter");
		classList.put(45, "Orc Raider");
		classList.put(46, "Destroyer");
		classList.put(47, "Orc Monk");
		classList.put(48, "Tyrant");
		classList.put(49, "Orc Mage");
		classList.put(50, "Orc Shaman");
		classList.put(51, "Overlord");
		classList.put(52, "Warcryer");
		classList.put(53, "Dwarven Fighter");
		classList.put(54, "Scavenger");
		classList.put(55, "Bounty Hunter");
		classList.put(56, "Artisan");
		classList.put(57, "Warsmith");
		classList.put(88, "Duelist");
		classList.put(89, "Dreadnought");
		classList.put(90, "Phoenix Knight");
		classList.put(91, "Hell Knight");
		classList.put(92, "Sagittarius");
		classList.put(93, "Adventurer");
		classList.put(94, "Archmage");
		classList.put(95, "Soultaker");
		classList.put(96, "Arcana Lord");
		classList.put(97, "Cardinal");
		classList.put(98, "Hierophant");
		classList.put(99, "Evas Templar");
		classList.put(100, "Sword Muse");
		classList.put(101, "Wind Rider");
		classList.put(102, "Moonlight Sentinel");
		classList.put(103, "Mystic Muse");
		classList.put(104, "Elemental Master");
		classList.put(105, "Evas Saint");
		classList.put(106, "Shillien Templar");
		classList.put(107, "Spectral Dancer");
		classList.put(108, "Ghost Hunter");
		classList.put(109, "Ghost Sentinel");
		classList.put(110, "Storm Screamer");
		classList.put(111, "Spectral Master");
		classList.put(112, "Shillien Saint");
		classList.put(113, "Titan");
		classList.put(114, "Grand Khavatari");
		classList.put(115, "Dominator");
		classList.put(116, "Doomcryer");
		classList.put(117, "Fortune Seeker");
		classList.put(118, "Maestro");
		
		return classList.get(classid);
	}
	
	public String getPlayerRunTime(int secs)
	{
		String timeResult = "";
		if (secs >= 86400)
		{
			timeResult = Integer.toString(secs / 86400) + " Days " + Integer.toString((secs % 86400) / 3600) + " hours";
		}
		else
		{
			timeResult = Integer.toString(secs / 3600) + " Hours " + Integer.toString((secs % 3600) / 60) + " mins";
		}
		return timeResult;
	}
	
	public String getAdenas(Long adena)
	{
		String adenas = "";
		if (adena >= 1000000000)
		{
			adenas = Long.toString(adena / 1000000000) + " Billion " + Long.toString((adena % 1000000000) / 1000000) + " million";
		}
		else
		{
			adenas = Long.toString(adena / 1000000) + " Million " + Long.toString((adena % 1000000) / 1000) + " k";
		}
		return adenas;
	}
}