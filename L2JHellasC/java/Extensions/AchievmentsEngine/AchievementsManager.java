package Extensions.AchievmentsEngine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import Extensions.AchievmentsEngine.base.Achievement;
import Extensions.AchievmentsEngine.base.Condition;
import Extensions.AchievmentsEngine.conditions.Adena;
import Extensions.AchievmentsEngine.conditions.Castle;
import Extensions.AchievmentsEngine.conditions.ClanLeader;
import Extensions.AchievmentsEngine.conditions.ClanLevel;
import Extensions.AchievmentsEngine.conditions.CompleteAchievements;
import Extensions.AchievmentsEngine.conditions.Crp;
import Extensions.AchievmentsEngine.conditions.Hero;
import Extensions.AchievmentsEngine.conditions.HeroCount;
import Extensions.AchievmentsEngine.conditions.ItemsCount;
import Extensions.AchievmentsEngine.conditions.Karma;
import Extensions.AchievmentsEngine.conditions.Levelup;
import Extensions.AchievmentsEngine.conditions.Mage;
import Extensions.AchievmentsEngine.conditions.Marry;
import Extensions.AchievmentsEngine.conditions.MinCMcount;
import Extensions.AchievmentsEngine.conditions.Noble;
import Extensions.AchievmentsEngine.conditions.OnlineTime;
import Extensions.AchievmentsEngine.conditions.Pk;
import Extensions.AchievmentsEngine.conditions.Pvp;
import Extensions.AchievmentsEngine.conditions.RaidKill;
import Extensions.AchievmentsEngine.conditions.RaidPoints;
import Extensions.AchievmentsEngine.conditions.SkillEnchant;
import Extensions.AchievmentsEngine.conditions.Sub;
import Extensions.AchievmentsEngine.conditions.WeaponEnchant;
import Extensions.AchievmentsEngine.conditions.eventKills;
import Extensions.AchievmentsEngine.conditions.eventWins;
import Extensions.AchievmentsEngine.conditions.events;

import com.PackRoot;
import com.l2jhellas.Config;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.ItemList;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class AchievementsManager
{
	private static final Logger log = Logger.getLogger(AchievementsManager.class.getSimpleName());
	private final Map<Integer, Achievement> _achievementList = new HashMap<>();
	private final ArrayList<String> _binded = new ArrayList<>();
	
	public AchievementsManager()
	{
		loadAchievements();
	}
	
	private void loadAchievements()
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		
		File file = new File(PackRoot.DATAPACK_ROOT, "data/xml/achievements.xml");
		
		if (!file.exists())
		{
			log.log(Level.WARNING, getClass().getSimpleName(), ": Error achievements xml file does not exist, check directory!");
		}
		try
		{
			InputSource in = new InputSource(reader(file));
			
			Document doc = factory.newDocumentBuilder().parse(in);
			
			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if (n.getNodeName().equalsIgnoreCase("list"))
				{
					for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if (d.getNodeName().equalsIgnoreCase("achievement"))
						{
							int id = checkInt(d, "id");
							
							String name = String.valueOf(d.getAttributes().getNamedItem("name").getNodeValue());
							String description = String.valueOf(d.getAttributes().getNamedItem("description").getNodeValue());
							String reward = String.valueOf(d.getAttributes().getNamedItem("reward").getNodeValue());
							boolean repeat = checkBoolean(d, "repeatable");
							
							ArrayList<Condition> conditions = conditionList(d.getAttributes());
							
							_achievementList.put(id, new Achievement(id, name, description, reward, repeat, conditions));
							alterTable(id);
						}
					}
				}
			}
			
			log.log(Level.INFO, getClass().getSimpleName(), ": loaded " + getAchievementList().size() + " achievements from xml!");
		}
		catch (Exception e)
		{
			log.log(Level.WARNING, getClass().getSimpleName(), ": Error ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}

	private static InputStreamReader reader(File file) throws UnsupportedEncodingException, FileNotFoundException
	{
		return new InputStreamReader(new FileInputStream(file), "UTF-8");
	}
	
	public void rewardForAchievement(int achievementID, L2PcInstance player)
	{
		Achievement achievement = _achievementList.get(achievementID);
		
		for (int id : achievement.getRewardList().keySet())
		{
			int count = achievement.getRewardList().get(id).intValue();
			player.addItem(achievement.getName(), id, count, player, true);
			player.sendPacket(new ItemList(player, true));		
		}
	}
	
	private static boolean checkBoolean(Node d, String nodename)
	{
		boolean b = false;
		
		try
		{
			b = Boolean.valueOf(d.getAttributes().getNamedItem(nodename).getNodeValue());
		}
		catch (Exception e)
		{
			
		}
		return b;
	}
	
	private static int checkInt(Node d, String nodename)
	{
		int i = 0;
		
		try
		{
			i = Integer.valueOf(d.getAttributes().getNamedItem(nodename).getNodeValue());
		}
		catch (Exception e)
		{
			
		}
		return i;
	}
	
	private static void alterTable(int fieldID)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			Statement statement = con.createStatement();
			statement.executeUpdate("ALTER TABLE achievements ADD a" + fieldID + " INT DEFAULT 0");
			statement.close();
		}
		catch (SQLException e)
		{
			
		}
	}
	
	public ArrayList<Condition> conditionList(NamedNodeMap attributesList)
	{
		ArrayList<Condition> conditions = new ArrayList<>();
		
		for (int j = 0; j < attributesList.getLength(); j++)
		{
			addToConditionList(attributesList.item(j).getNodeName(), attributesList.item(j).getNodeValue(), conditions);
		}
		
		return conditions;
	}
	
	public Map<Integer, Achievement> getAchievementList()
	{
		return _achievementList;
	}
	
	public ArrayList<String> getBinded()
	{
		return _binded;
	}
	
	public boolean isBinded(int obj, int ach)
	{
		for (String binds : _binded)
		{
			String[] spl = binds.split("@");
			if (spl[0].equals(String.valueOf(obj)) && spl[1].equals(String.valueOf(ach)))
				return true;
		}
		return false;
	}
	
	public static AchievementsManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final AchievementsManager _instance = new AchievementsManager();
	}
	
	private static void addToConditionList(String nodeName, Object value, ArrayList<Condition> conditions)
	{
			switch (nodeName)
			{
				case "minLevel":
					conditions.add(new Levelup(value));	
					break;
				case "minPvPCount":
					conditions.add(new Pvp(value));
					break;
				case "minPkCount":
					conditions.add(new Pk(value));
					break;										
				case "minClanLevel":
					conditions.add(new ClanLevel(value));
					break;
				case "mustBeHero":
					conditions.add(new Hero(value));
					break;
				case "mustBeNoble":
					conditions.add(new Noble(value));
					break;	
				case "minWeaponEnchant":
					conditions.add(new WeaponEnchant(value));	
					break;	
				case "minKarmaCount":
					conditions.add(new Karma(value));
					break;	
				case "minAdenaCount":
					conditions.add(new Adena(value));
					break;	
				case "minClanMembersCount":
					conditions.add(new MinCMcount(value));	
					break;	
				case "mustBeClanLeader":
					conditions.add(new ClanLeader(value));	
					break;	
				case "mustBeMarried":
					conditions.add(new Marry(value));
					break;	
				case "itemAmmount":
					conditions.add(new ItemsCount(value));
					break;	
				case "crpAmmount":
					conditions.add(new Crp(value));	
					break;	
				case "lordOfCastle":
					conditions.add(new Castle(value));		
					break;	
				case "mustBeMageClass":
					conditions.add(new Mage(value));		
					break;
				case "minSubclassCount":
					conditions.add(new Sub(value));
					break;			
				case "CompleteAchievements":
					conditions.add(new CompleteAchievements(value));
					break;						
				case "minSkillEnchant":
					conditions.add(new SkillEnchant(value));
					break;							
				case "minOnlineTime":
					conditions.add(new OnlineTime(value));		
					break;	
				case "minHeroCount":
					conditions.add(new HeroCount(value));				
					break;	
				case "raidToKill":
					conditions.add(new RaidKill(value));					
					break;	
				case "raidToKill1":
					conditions.add(new RaidKill(value));							
					break;	
				case "raidToKill2":
					conditions.add(new RaidKill(value));						
					break;	
				case "minRaidPoints":
					conditions.add(new RaidPoints(value));								
					break;	
				case "eventKills":
					conditions.add(new eventKills(value));								
					break;	
				case "events":
					conditions.add(new events(value));									
					break;
				case "eventWins":
					conditions.add(new eventWins(value));								
					break;																															
			}
                				
	}
	
	public void loadUsed()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement;
			ResultSet rs;
			String sql = "SELECT ";
			for (int i = 1; i <= getAchievementList().size(); i++)
			{
				if (i != getAchievementList().size())
					sql = sql + "a" + i + ",";
				else
					sql = sql + "a" + i;
			}
			
			sql = sql + " FROM achievements";
			statement = con.prepareStatement(sql);
			
			rs = statement.executeQuery();
			while (rs.next())
			{
				for (int i = 1; i <= getAchievementList().size(); i++)
				{
					String ct = rs.getString(i);
					if (ct.length() > 1 && ct.startsWith("1"))
					{
						_binded.add(ct.substring(ct.indexOf("1") + 1) + "@" + i);
					}
				}
			}
			rs.close();
			statement.close();
		}
		catch (SQLException e)
		{
			log.log(Level.WARNING, getClass().getSimpleName(), ":[ACHIEVEMENTS SAVE GETDATA]");
			if (Config.DEVELOPER)
			{
				e.printStackTrace();
			}
		}
	}
}