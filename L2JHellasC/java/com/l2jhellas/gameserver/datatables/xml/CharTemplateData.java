package com.l2jhellas.gameserver.datatables.xml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.PackRoot;
import com.l2jhellas.Config;
import com.l2jhellas.gameserver.engines.DocumentParser;
import com.l2jhellas.gameserver.enums.player.ClassId;
import com.l2jhellas.gameserver.templates.L2PcTemplate;
import com.l2jhellas.gameserver.templates.StatsSet;

public class CharTemplateData implements DocumentParser
{
	protected static final Logger _log = Logger.getLogger(CharTemplateData.class.getName());
	
	private static final String[] CHAR_CLASSES =
	{
		"Human Fighter",
		"Warrior",
		"Gladiator",
		"Warlord",
		"Human Knight",
		"Paladin",
		"Dark Avenger",
		"Rogue",
		"Treasure Hunter",
		"Hawkeye",
		"Human Mystic",
		"Human Wizard",
		"Sorceror",
		"Necromancer",
		"Warlock",
		"Cleric",
		"Bishop",
		"Prophet",
		"Elven Fighter",
		"Elven Knight",
		"Temple Knight",
		"Swordsinger",
		"Elven Scout",
		"Plainswalker",
		"Silver Ranger",
		"Elven Mystic",
		"Elven Wizard",
		"Spellsinger",
		"Elemental Summoner",
		"Elven Oracle",
		"Elven Elder",
		"Dark Fighter",
		"Palus Knight",
		"Shillien Knight",
		"Bladedancer",
		"Assassin",
		"Abyss Walker",
		"Phantom Ranger",
		"Dark Elven Mystic",
		"Dark Elven Wizard",
		"Spellhowler",
		"Phantom Summoner",
		"Shillien Oracle",
		"Shillien Elder",
		"Orc Fighter",
		"Orc Raider",
		"Destroyer",
		"Orc Monk",
		"Tyrant",
		"Orc Mystic",
		"Orc Shaman",
		"Overlord",
		"Warcryer",
		"Dwarven Fighter",
		"Dwarven Scavenger",
		"Bounty Hunter",
		"Dwarven Artisan",
		"Warsmith",
		"dummyEntry1",
		"dummyEntry2",
		"dummyEntry3",
		"dummyEntry4",
		"dummyEntry5",
		"dummyEntry6",
		"dummyEntry7",
		"dummyEntry8",
		"dummyEntry9",
		"dummyEntry10",
		"dummyEntry11",
		"dummyEntry12",
		"dummyEntry13",
		"dummyEntry14",
		"dummyEntry15",
		"dummyEntry16",
		"dummyEntry17",
		"dummyEntry18",
		"dummyEntry19",
		"dummyEntry20",
		"dummyEntry21",
		"dummyEntry22",
		"dummyEntry23",
		"dummyEntry24",
		"dummyEntry25",
		"dummyEntry26",
		"dummyEntry27",
		"dummyEntry28",
		"dummyEntry29",
		"dummyEntry30",
		"Duelist",
		"DreadNought",
		"Phoenix Knight",
		"Hell Knight",
		"Sagittarius",
		"Adventurer",
		"Archmage",
		"Soultaker",
		"Arcana Lord",
		"Cardinal",
		"Hierophant",
		"Eva Templar",
		"Sword Muse",
		"Wind Rider",
		"Moonlight Sentinel",
		"Mystic Muse",
		"Elemental Master",
		"Eva's Saint",
		"Shillien Templar",
		"Spectral Dancer",
		"Ghost Hunter",
		"Ghost Sentinel",
		"Storm Screamer",
		"Spectral Master",
		"Shillien Saint",
		"Titan",
		"Grand Khauatari",
		"Dominator",
		"Doomcryer",
		"Fortune Seeker",
		"Maestro"
	};
	
	private final Map<Integer, L2PcTemplate> _templates = new HashMap<>();
	
	protected CharTemplateData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseFile(new File(PackRoot.DATAPACK_ROOT, "data/xml/char_templates.xml"));
		_log.info(CharTemplateData.class.getSimpleName() + ": Loaded " + _templates.size() + " character templates.");
	}
	
	@Override
	public void parseDocument(Document doc)
	{
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if (n.getNodeName().equalsIgnoreCase("list"))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if (d.getNodeName().equalsIgnoreCase("class"))
					{
						L2PcTemplate ct;
						StatsSet set = new StatsSet();
						int ID = Integer.valueOf(d.getAttributes().getNamedItem("Id").getNodeValue());
						String NAME = String.valueOf(d.getAttributes().getNamedItem("name").getNodeValue());
						int race_id = Integer.valueOf(d.getAttributes().getNamedItem("RaceId").getNodeValue());
						for (Node t = d.getFirstChild(); t != null; t = t.getNextSibling())
						{
							if (t.getNodeName().equalsIgnoreCase("stats"))
							{
								
								int STR = Integer.valueOf(t.getAttributes().getNamedItem("str").getNodeValue());
								int CON = Integer.valueOf(t.getAttributes().getNamedItem("con").getNodeValue());
								int DEX = Integer.valueOf(t.getAttributes().getNamedItem("dex").getNodeValue());
								int INT = Integer.valueOf(t.getAttributes().getNamedItem("_int").getNodeValue());
								int WIT = Integer.valueOf(t.getAttributes().getNamedItem("wit").getNodeValue());
								int MEN = Integer.valueOf(t.getAttributes().getNamedItem("men").getNodeValue());
								int PA = Integer.valueOf(t.getAttributes().getNamedItem("p_atk").getNodeValue());
								int PD = Integer.valueOf(t.getAttributes().getNamedItem("p_def").getNodeValue());
								int MA = Integer.valueOf(t.getAttributes().getNamedItem("m_atk").getNodeValue());
								int MD = Integer.valueOf(t.getAttributes().getNamedItem("m_def").getNodeValue());
								int PS = Integer.valueOf(t.getAttributes().getNamedItem("p_spd").getNodeValue());
								int MS = Integer.valueOf(t.getAttributes().getNamedItem("m_spd").getNodeValue());
								int CR = Integer.valueOf(t.getAttributes().getNamedItem("critical").getNodeValue());
								int MSP = Integer.valueOf(t.getAttributes().getNamedItem("move_spd").getNodeValue());
								int X = Integer.valueOf(t.getAttributes().getNamedItem("x").getNodeValue());
								int Y = Integer.valueOf(t.getAttributes().getNamedItem("y").getNodeValue());
								int Z = Integer.valueOf(t.getAttributes().getNamedItem("z").getNodeValue());
								
								double COL_R = Double.valueOf(t.getAttributes().getNamedItem("m_col_r").getNodeValue());
								double COL_H = Double.valueOf(t.getAttributes().getNamedItem("m_col_h").getNodeValue());
								
								double COL_RF = Double.valueOf(t.getAttributes().getNamedItem("f_col_r").getNodeValue());
								double COL_HF = Double.valueOf(t.getAttributes().getNamedItem("f_col_h").getNodeValue());
								for (Node h = t.getFirstChild(); h != null; h = h.getNextSibling())
								{
									if (h.getNodeName().equalsIgnoreCase("lvlup"))
									{
										float HPBASE = Float.valueOf(h.getAttributes().getNamedItem("hpbase").getNodeValue());
										float HPADD = Float.valueOf(h.getAttributes().getNamedItem("hpadd").getNodeValue());
										float HPMOD = Float.valueOf(h.getAttributes().getNamedItem("hpmod").getNodeValue());
										float MPBASE = Float.valueOf(h.getAttributes().getNamedItem("mpbase").getNodeValue());
										float CPBASE = Float.valueOf(h.getAttributes().getNamedItem("cpbase").getNodeValue());
										float CPADD = Float.valueOf(h.getAttributes().getNamedItem("cpadd").getNodeValue());
										float CPMOD = Float.valueOf(h.getAttributes().getNamedItem("cpmod").getNodeValue());
										float MPADD = Float.valueOf(h.getAttributes().getNamedItem("mpadd").getNodeValue());
										float MPMOD = Float.valueOf(h.getAttributes().getNamedItem("mpmod").getNodeValue());
										int lvl = Integer.valueOf(h.getAttributes().getNamedItem("class_lvl").getNodeValue());
										for (Node q = h.getFirstChild(); q != null; q = q.getNextSibling())
										{
											if (q.getNodeName().equalsIgnoreCase("item"))
											{
												set.set("classId", ID);
												set.set("className", NAME);
												set.set("raceId", race_id);
												set.set("baseSTR", STR);
												set.set("baseCON", CON);
												set.set("baseDEX", DEX);
												set.set("baseINT", INT);
												set.set("baseWIT", WIT);
												set.set("baseMEN", MEN);
												set.set("baseHpReg", 1.5);
												set.set("baseMpReg", 0.9);
												set.set("basePAtk", PA);
												set.set("basePDef", PD);
												set.set("baseMAtk", MA);
												set.set("baseMDef", MD);
												set.set("basePAtkSpd", PS);
												set.set("baseMAtkSpd", MS);
												set.set("baseCritRate", CR / 10);
												set.set("baseRunSpd", MSP);
												set.set("baseWalkSpd", 0);
												set.set("baseShldDef", 0);
												set.set("baseShldRate", 0);
												set.set("baseAtkRange", 40);
												if (Config.SPAWN_CHAR)
												{
													set.set("spawnX", Config.SPAWN_X);
													set.set("spawnY", Config.SPAWN_Y);
													set.set("spawnZ", Config.SPAWN_Z);
												}
												else
												{
													set.set("spawnX", X);
													set.set("spawnY", Y);
													set.set("spawnZ", Z);
												}
												set.set("collision_radius", COL_R);
												set.set("collision_height", COL_H);
												
												set.set("collision_radiusf", COL_RF);
												set.set("collision_heightf", COL_HF);
												
												set.set("baseHpMax", HPBASE);
												set.set("lvlHpAdd", HPADD);
												set.set("lvlHpMod", HPMOD);
												set.set("baseMpMax", MPBASE);
												set.set("baseCpMax", CPBASE);
												set.set("lvlCpAdd", CPADD);
												set.set("lvlCpMod", CPMOD);
												set.set("lvlMpAdd", MPADD);
												set.set("lvlMpMod", MPMOD);
												set.set("classBaseLevel", lvl);
												ct = new L2PcTemplate(set);
												
												// 5items must go here
												for (int x = 1; x < 6; x++)
												{
													if (Integer.valueOf(q.getAttributes().getNamedItem("item" + x).getNodeValue()) != 0)
													{
														ct.addItem(Integer.valueOf(q.getAttributes().getNamedItem("item" + x).getNodeValue()));
													}
												}
												
												_templates.put(ct.classId.getId(), ct);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	public L2PcTemplate getTemplate(ClassId classId)
	{
		return getTemplate(classId.getId());
	}
	
	public L2PcTemplate getTemplate(int classId)
	{
		int key = classId;
		
		return _templates.get(key);
	}
	
	public final static String getClassNameById(int classId)
	{
		return CHAR_CLASSES[classId];
	}
	
	public static final int getClassIdByName(String className)
	{
		int currId = 1;
		
		for (String name : CHAR_CLASSES)
		{
			if (name.equalsIgnoreCase(className))
			{
				break;
			}
			
			currId++;
		}
		
		return currId;
	}
	
	public static CharTemplateData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final CharTemplateData _instance = new CharTemplateData();
	}
}