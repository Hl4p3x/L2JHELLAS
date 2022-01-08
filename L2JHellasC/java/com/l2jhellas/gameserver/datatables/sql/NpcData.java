package com.l2jhellas.gameserver.datatables.sql;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.PackRoot;
import com.l2jhellas.Config;
import com.l2jhellas.gameserver.cache.InfoCache;
import com.l2jhellas.gameserver.enums.player.ClassId;
import com.l2jhellas.gameserver.model.L2DropData;
import com.l2jhellas.gameserver.model.L2MinionData;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.gameserver.skills.Stats;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;
import com.l2jhellas.gameserver.templates.StatsSet;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class NpcData
{
	protected static Logger _log = Logger.getLogger(NpcData.class.getName());
	
	// SQL Queries
	private static final String RESTORE_SELECT_NPC = "SELECT * FROM npc";
	private static final String RESTORE_SELECT_CUSTOM_NPC = "SELECT * FROM custom_npc";
	private static final String RESTORE_NPC_SKILLS = "SELECT * FROM npcskills";
	private static final String RESTORE_DROPLIST = "SELECT * FROM droplist ORDER BY mobId, chance DESC";
	private static final String RESTORE_CUSTOM_DROPLIST = "SELECT * FROM custom_droplist ORDER BY mobId, chance DESC";
	private static final String RELOAD_NPC = "SELECT * FROM npc WHERE id=?";
	private static final String RELOAD_CUSTOM_NPC = "SELECT * FROM custom_npc WHERE id=?";
	
	private final Map<Integer, L2NpcTemplate> _npcs = new HashMap<>();
	
	protected NpcData()
	{
		_npcs.clear();
		restoreNpcData();
	}
	
	private void restoreNpcData()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			try (PreparedStatement ps = con.prepareStatement(RESTORE_SELECT_NPC))
			{			
				try (ResultSet rset = ps.executeQuery())
				{				
					try
					{
						fillNpcTable(rset, false);
					}
					catch (Exception e)
					{
						_log.warning(NpcData.class.getName() + ": Error creating NPC table");
					}
				}
			}
			
			try (PreparedStatement ps = con.prepareStatement(RESTORE_SELECT_CUSTOM_NPC))
			{			
				try (ResultSet rset = ps.executeQuery())
				{				
					try
					{
						fillNpcTable(rset, true);
					}
					catch (Exception e)
					{
						_log.warning(NpcData.class.getName() + ": Error creating custom NPC table");
					}
				}
			}
			
			try (PreparedStatement ps = con.prepareStatement(RESTORE_NPC_SKILLS))
			{				
				try (ResultSet rset = ps.executeQuery())
				{				
					while (rset.next())
					{
						int mobId = rset.getInt("npcid");
						L2NpcTemplate npcDat = _npcs.get(mobId);

						if (npcDat == null)
							continue;

						int skillId = rset.getInt("skillid");
						int level = rset.getInt("level");

						if (npcDat.race == null && skillId == 4416)
						{
							npcDat.setRace(level);
							continue;
						}

						L2Skill npcSkill = SkillTable.getInstance().getInfo(skillId, level);

						if (npcSkill == null)
							continue;

						npcDat.addSkill(npcSkill);
					}
				}
			}
			
			try (PreparedStatement ps = con.prepareStatement(RESTORE_DROPLIST))
			{
				try (ResultSet rset = ps.executeQuery())
				{
					while (rset.next())
					{
						int mobId = rset.getInt("mobId");
						
						L2NpcTemplate npcDat = _npcs.get(mobId);
						
						if (npcDat == null)
						{
							_log.warning(NpcData.class.getName() + ": No npc correlating with id: " + mobId);
							continue;
						}
						
						L2DropData dropDat = new L2DropData();
						
						dropDat.setItemId(rset.getInt("itemId"));
						dropDat.setMinDrop(rset.getInt("min"));
						dropDat.setMaxDrop(rset.getInt("max"));
						dropDat.setChance(rset.getInt("chance"));
						
						int category = rset.getInt("category");
						
						npcDat.addDropData(dropDat, category);
					}
				}		
			}
		
			try (PreparedStatement ps = con.prepareStatement(RESTORE_CUSTOM_DROPLIST))
			{
				int cCount = 0;

				try (ResultSet rset = ps.executeQuery())
				{	
					while (rset.next())
					{
						int mobId = rset.getInt("mobId");
						
						L2NpcTemplate npcDat = _npcs.get(mobId);
						
						if (npcDat == null)
						{
							_log.warning(NpcData.class.getName() + ":  CUSTOM DROPLIST No npc correlating with id : " + mobId);
							continue;
						}
						
						L2DropData dropDat = new L2DropData();
						dropDat.setItemId(rset.getInt("itemId"));
						dropDat.setMinDrop(rset.getInt("min"));
						dropDat.setMaxDrop(rset.getInt("max"));
						dropDat.setChance(rset.getInt("chance"));
						
						int category = rset.getInt("category");
						
						npcDat.addDropData(dropDat, category);
						cCount++;
						dropDat = null;
					}
				}
				
				_log.info(NpcData.class.getSimpleName() + ": Loaded " + cCount + " custom droplist.");
			}
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			int th = 0;
			File f = new File(PackRoot.DATAPACK_ROOT, "data/xml/skill_learn.xml");
			if (!f.exists())
			{
				_log.warning(NpcData.class.getName() + ": skill_learn.xml could not be loaded: file not found");
				return;
			}
			try
			{
				InputSource in = new InputSource(new InputStreamReader(new FileInputStream(f), "UTF-8"));
				in.setEncoding("UTF-8");
				Document doc = factory.newDocumentBuilder().parse(in);
				for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
				{
					if (n.getNodeName().equalsIgnoreCase("list"))
					{
						for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						{
							if (d.getNodeName().equalsIgnoreCase("learn"))
							{
								int npcId = Integer.valueOf(d.getAttributes().getNamedItem("npc_id").getNodeValue());
								int classId = Integer.valueOf(d.getAttributes().getNamedItem("class_id").getNodeValue());
								L2NpcTemplate npc = _npcs.get(npcId);
								
								if (npc == null)
								{
									_log.warning(NpcData.class.getName() + ":  Error getting NPC template ID " + npcId + " while trying to load skill trainer data.");
									continue;
								}
								
								npc.addTeachInfo(ClassId.values()[classId]);
								th++;
							}
						}
					}
				}
			}
			
			catch (SAXException e)
			{
				_log.warning(NpcData.class.getName() + ":  Error reading NPC trainer data");
			}
			catch (IOException e)
			{
				_log.warning(NpcData.class.getName() + ":  Error reading NPC trainer data");
			}
			catch (ParserConfigurationException e)
			{
				_log.warning(NpcData.class.getName() + ":  Error reading NPC trainer data");
			}
			
			_log.info("NpcData: Loaded " + th + " teachers.");
			
			DocumentBuilderFactory factory1 = DocumentBuilderFactory.newInstance();
			factory1.setValidating(false);
			factory1.setIgnoringComments(true);
			int cnt = 0;
			File f1 = new File(PackRoot.DATAPACK_ROOT, "data/xml/minion.xml");
			if (!f1.exists())
			{
				_log.warning(NpcData.class.getName() + ": minion.xml could not be loaded: file not found");
				return;
			}
			try
			{
				InputSource in1 = new InputSource(new InputStreamReader(new FileInputStream(f1), "UTF-8"));
				in1.setEncoding("UTF-8");
				Document doc1 = factory1.newDocumentBuilder().parse(in1);
				L2MinionData minionDat = null;
				L2NpcTemplate npcDat = null;
				for (Node n1 = doc1.getFirstChild(); n1 != null; n1 = n1.getNextSibling())
				{
					if (n1.getNodeName().equalsIgnoreCase("list"))
					{
						for (Node d1 = n1.getFirstChild(); d1 != null; d1 = d1.getNextSibling())
						{
							if (d1.getNodeName().equalsIgnoreCase("minion"))
							{
								int raidId = Integer.valueOf(d1.getAttributes().getNamedItem("boss_id").getNodeValue());
								int mid = Integer.valueOf(d1.getAttributes().getNamedItem("minion_id").getNodeValue());
								int mmin = Integer.valueOf(d1.getAttributes().getNamedItem("amount_min").getNodeValue());
								int mmax = Integer.valueOf(d1.getAttributes().getNamedItem("amount_max").getNodeValue());
								
								npcDat = _npcs.get(raidId);
								minionDat = new L2MinionData();
								
								minionDat.setMinionId(mid);
								minionDat.setAmountMin(mmin);
								minionDat.setAmountMax(mmax);
								npcDat.addRaidData(minionDat);
								cnt++;
								minionDat = null;
							}
						}
					}
				}
			}
			catch (SAXException e)
			{
				_log.warning(NpcData.class.getName() + ": Error loading minion data");
			}
			catch (IOException e)
			{
				_log.warning(NpcData.class.getName() + ": Error loading minion data");
			}
			catch (ParserConfigurationException e)
			{
				_log.warning(NpcData.class.getName() + ": Error loading minion data");
			}
			_log.info(NpcData.class.getSimpleName() + ": Loaded " + cnt + " minions.");
		}
		catch (SQLException e)
		{
			_log.warning(NpcData.class.getName() + ": Error General");
		}
	}
	
	private void fillNpcTable(ResultSet NpcData, boolean customData) throws Exception
	{
		while (NpcData.next())
		{
			StatsSet npcDat = new StatsSet();
			
			int id = NpcData.getInt("id");
			npcDat.set("npcId", id);
			
			npcDat.set("idTemplate", NpcData.getInt("idTemplate"));
			
			int level = NpcData.getInt("level");
			npcDat.set("level", level);
			npcDat.set("jClass", NpcData.getString("class"));
			npcDat.set("baseShldDef", 0);
			npcDat.set("baseShldRate", 0);
			npcDat.set("crit", 38);
			
			npcDat.set("name", NpcData.getString("name"));
			npcDat.set("serverSideName", NpcData.getBoolean("serverSideName"));
			// npcDat.set("name", "");
			npcDat.set("title", NpcData.getString("title"));
			npcDat.set("serverSideTitle", NpcData.getBoolean("serverSideTitle"));
			npcDat.set("radius", NpcData.getDouble("collision_radius"));
			npcDat.set("height", NpcData.getDouble("collision_height"));
			npcDat.set("sex", NpcData.getString("sex"));
			npcDat.set("type", NpcData.getString("type"));
			npcDat.set("baseAtkRange", NpcData.getInt("attackrange"));
			npcDat.set("rewardExp", NpcData.getInt("exp"));
			npcDat.set("rewardSp", NpcData.getInt("sp"));
			npcDat.set("atkSpd", NpcData.getInt("atkSpd"));
			npcDat.set("baseMAtkSpd", NpcData.getInt("matkspd"));
			npcDat.set("aggroRange", NpcData.getInt("aggro"));
			npcDat.set("rhand", NpcData.getInt("rhand"));
			npcDat.set("lhand", NpcData.getInt("lhand"));
			npcDat.set("armor", NpcData.getInt("armor"));
			npcDat.set("walkSpd", NpcData.getInt("walkspd"));
			npcDat.set("runSpd", NpcData.getInt("runspd"));
			
			// constants, until we have stats in DB
			npcDat.set("str", NpcData.getInt("str"));
			npcDat.set("con", NpcData.getInt("con"));
			npcDat.set("dex", NpcData.getInt("dex"));
			npcDat.set("int", NpcData.getInt("int"));
			npcDat.set("wit", NpcData.getInt("wit"));
			npcDat.set("men", NpcData.getInt("men"));
			
			npcDat.set("hp", NpcData.getInt("hp"));
			npcDat.set("baseCpMax", 0);
			npcDat.set("mp", NpcData.getInt("mp"));
			npcDat.set("baseHpReg", NpcData.getFloat("hpreg") > 0 ? NpcData.getFloat("hpreg") : 1.5 + ((level - 1) / 10.0));
			npcDat.set("baseMpReg", NpcData.getFloat("mpreg") > 0 ? NpcData.getFloat("mpreg") : 0.9 + 0.3 * ((level - 1) / 10.0));
			
			npcDat.set("pAtk", NpcData.getInt("patk"));
			npcDat.set("pDef", NpcData.getInt("pdef"));
			npcDat.set("mAtk", NpcData.getInt("matk"));
			npcDat.set("mDef", NpcData.getInt("mdef"));
			
			npcDat.set("factionId", NpcData.getString("faction_id"));
			npcDat.set("factionRange", NpcData.getInt("faction_range"));
			
			npcDat.set("isUndead", NpcData.getString("isUndead"));
			
			npcDat.set("absorb_level", NpcData.getString("absorb_level"));
			npcDat.set("absorb_type", NpcData.getString("absorb_type"));
			L2NpcTemplate template = new L2NpcTemplate(npcDat);
			template.addVulnerability(Stats.BOW_WPN_VULN, 1);
			template.addVulnerability(Stats.BLUNT_WPN_VULN, 1);
			template.addVulnerability(Stats.DAGGER_WPN_VULN, 1);
			_npcs.put(id, template);
		}
		
		_log.info(NpcData.class.getSimpleName() + ": Loaded " + _npcs.size() + " npc templates.");
	}
	
	public void reloadNpc(int id)
	{
		try
		{
			// save a copy of the old data
			L2NpcTemplate old = getTemplate(id);
			Map<Integer, L2Skill> skills = new HashMap<>();
			
			if (old.getSkills() != null)
				skills.putAll(old.getSkills());
			
			ClassId[] classIds = null;
			
			if (old.getTeachInfo() != null)
				classIds = old.getTeachInfo().clone();
			
			List<L2MinionData> minions = new ArrayList<>();
			
			if (old.getMinionData() != null)
				minions.addAll(old.getMinionData());
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				// reload the NPC base data
				if (old.isCustom())
				{
					final PreparedStatement st = con.prepareStatement(RELOAD_CUSTOM_NPC);
					st.setInt(1, id);
					final ResultSet rs = st.executeQuery();
					fillNpcTable(rs, true);
					rs.close();
					st.close();
				}
				else
				{
					final PreparedStatement st = con.prepareStatement(RELOAD_NPC);
					st.setInt(1, id);
					final ResultSet rs = st.executeQuery();
					fillNpcTable(rs, false);
					rs.close();
					st.close();
				}
			}
			catch (SQLException e)
			{
				_log.warning(NpcData.class.getName() + ": Error reloading NPC");
				if (Config.DEVELOPER)
					e.printStackTrace();
			}
			
			// restore additional data from saved copy
			L2NpcTemplate created = getTemplate(id);
			
			for (L2Skill skill : skills.values())
			{
				created.addSkill(skill);
			}
			
			skills = null;
			
			if (classIds != null)
			{
				for (ClassId classId : classIds)
				{
					created.addTeachInfo(classId);
				}
			}
			
			for (L2MinionData minion : minions)
			{
				created.addRaidData(minion);
			}
			
			created = null;
			minions = null;
			classIds = null;
		}
		catch (Exception e)
		{
			_log.warning(NpcData.class.getName() + ":  Could not reload data for NPC " + id);
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public void reloadAllNpc()
	{
		_npcs.clear();
		restoreNpcData();
	}
	
	public void saveNpc(StatsSet npc)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			Map<String, Object> set = npc;
			
			String name = "";
			String values = "";
			
			final L2NpcTemplate old = getTemplate(npc.getInteger("npcId"));
			
			for (Object obj : set.keySet())
			{
				name = (String) obj;
				
				if (!name.equalsIgnoreCase("npcId"))
				{
					if (values != "")
					{
						values += ", ";
					}
					
					values += name + " = '" + set.get(name) + "'";
				}
			}
			
			PreparedStatement statement;
			if (old.isCustom())
			{
				statement = con.prepareStatement("UPDATE custom_npc SET " + values + " WHERE id=?");
			}
			else
			{
				statement = con.prepareStatement("UPDATE npc SET " + values + " WHERE id=?");
			}
			statement.setInt(1, npc.getInteger("npcId"));
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning(NpcData.class.getName() + ":  Could not store new NPC data in database");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public void replaceTemplate(L2NpcTemplate npc)
	{
		_npcs.replace(npc.npcId, npc);
	}
	
	public L2NpcTemplate getTemplate(int id)
	{
		return _npcs.get(id);
	}
	
	public L2NpcTemplate getTemplateByName(String name)
	{
		for (L2NpcTemplate npcTemplate : _npcs.values())
			if (npcTemplate.name.equalsIgnoreCase(name))
				return npcTemplate;
		
		return null;
	}
	
	public L2NpcTemplate[] getAllOfLevel(int lvl)
	{
		List<L2NpcTemplate> list = new ArrayList<>();
		
		for (L2NpcTemplate t : _npcs.values())
			if (t.level == lvl)
				list.add(t);
		
		return list.toArray(new L2NpcTemplate[list.size()]);
	}
	
	public L2NpcTemplate[] getAllMonstersOfLevel(int lvl)
	{
		List<L2NpcTemplate> list = new ArrayList<>();
		
		for (L2NpcTemplate t : _npcs.values())
			if (t.level == lvl && "L2Monster".equals(t.type))
				list.add(t);
		
		return list.toArray(new L2NpcTemplate[list.size()]);
	}
	
	public L2NpcTemplate[] getAllNpcStartingWith(String letter)
	{
		List<L2NpcTemplate> list = new ArrayList<>();
		
		for (L2NpcTemplate t : _npcs.values())
			if (t.name.startsWith(letter) && "L2Npc".equals(t.type))
				list.add(t);
		
		return list.toArray(new L2NpcTemplate[list.size()]);
	}
	
	public Map<Integer, L2NpcTemplate> getAllTemplates()
	{
		return _npcs;
	}
	
	public void FillDropList()
	{
		for (L2NpcTemplate npc : _npcs.values())
		{
			InfoCache.addToDroplistCache(npc.npcId, npc.getAllDropData());
		}
		
		_log.info(NpcData.class.getSimpleName() + ": Players droplist was cached.");
	}
	
	public Collection<L2NpcTemplate> getAllNpcs()
	{
		return _npcs.values();
	}
	
	public static NpcData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final NpcData _instance = new NpcData();
	}
}