package com.l2jhellas.gameserver.datatables.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.PackRoot;
import com.l2jhellas.Config;
import com.l2jhellas.gameserver.engines.DocumentParser;
import com.l2jhellas.gameserver.enums.player.ClassId;
import com.l2jhellas.gameserver.model.L2EnchantSkillLearn;
import com.l2jhellas.gameserver.model.L2PledgeSkillLearn;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.L2SkillLearn;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.skills.SkillTable;

public class SkillTreeData implements DocumentParser
{
	protected static final Logger _log = Logger.getLogger(SkillTreeData.class.getName());
		
	private static Map<ClassId, Map<Integer, L2SkillLearn>> _skillTrees = new HashMap<>();
	private static List<L2SkillLearn> _fishingSkillTrees = new ArrayList<>(); // all common skills (teached by Fisherman)
	private static List<L2SkillLearn> _expandDwarfCraftSkillTrees = new ArrayList<>(); // list of special skill for dwarf (expand dwarf craft) learned by class teacher
	private static List<L2EnchantSkillLearn> _enchantSkillTrees = new ArrayList<>(); // enchant skill list
	private static List<L2PledgeSkillLearn> _pledgeSkillTrees = new ArrayList<>(); // pledge skill list

	
	protected SkillTreeData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseFile(new File(PackRoot.DATAPACK_ROOT, "data/xml/skill_tree.xml"));
		_log.info("SkillTreeTable: Loaded " + _skillTrees.size() + " skills.");
		
		parseFile(new File(PackRoot.DATAPACK_ROOT, "data/xml/fishing_skill_tree.xml"));
		_log.info("FishingSkillTreeTable: Loaded " + _fishingSkillTrees.size() + " general skills.");
		_log.info("DwarvenSkillTreeTable: Loaded " + _expandDwarfCraftSkillTrees.size() + " dwarven skills.");		
		
		parseFile(new File(PackRoot.DATAPACK_ROOT, "data/xml/enchant_skill_tree.xml"));
		_log.info("EnchantSkillTreeTable: Loaded " + _enchantSkillTrees.size() + " enchant skills & enchant stats.");
			
		parseFile(new File(PackRoot.DATAPACK_ROOT, "data/xml/pledge_skill_tree.xml"));
		_log.info("PledgeSkillTreeTable: Loaded " + _pledgeSkillTrees.size() + " pledge skills.");
	}
	
	public void reload()
	{
		_skillTrees.clear();
		_fishingSkillTrees.clear();
		_expandDwarfCraftSkillTrees.clear();
		_enchantSkillTrees.clear();
		_pledgeSkillTrees.clear();
		load();
	}
	
	@Override
	public void parseDocument(Document doc)
	{		
		Map<Integer, L2SkillLearn> map;
		
		int classId = 0;
		int parentClassId;
		
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if (n.getNodeName().equalsIgnoreCase("list"))
			{			
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if (d.getNodeName().equalsIgnoreCase("skill"))
					{				
						map = new Hashtable<>(5, 0.9f);
						classId = Integer.valueOf(d.getAttributes().getNamedItem("class_id").getNodeValue());
						parentClassId = Integer.valueOf(d.getAttributes().getNamedItem("parent_id").getNodeValue());
						if (parentClassId != -1)
						{
							Map<Integer, L2SkillLearn> parentMap = getSkillTrees().get(ClassId.values()[parentClassId]);
							map.putAll(parentMap);
						}
						
						for (Node t = d.getFirstChild(); t != null; t = t.getNextSibling())
						{
							if (t.getNodeName().equalsIgnoreCase("data"))
							{
								int id = Integer.valueOf(t.getAttributes().getNamedItem("skill_id").getNodeValue());
								int lvl = Integer.valueOf(t.getAttributes().getNamedItem("level").getNodeValue());
								String name = String.valueOf(t.getAttributes().getNamedItem("name").getNodeValue());
								int cost = Integer.valueOf(t.getAttributes().getNamedItem("sp").getNodeValue());
								int minLvl = Integer.valueOf(t.getAttributes().getNamedItem("min_level").getNodeValue());

								L2SkillLearn skillLearn = new L2SkillLearn(id, lvl, minLvl, name, cost, 0, 0);
								map.put(SkillTable.getSkillHashCode(id, lvl), skillLearn);
							}
						}
						getSkillTrees().put(ClassId.values()[classId], map);						
					}
					
					if (d.getNodeName().equalsIgnoreCase("SkillTree"))
					{
						int id = Integer.valueOf(d.getAttributes().getNamedItem("skill_id").getNodeValue());
						int lvl = Integer.valueOf(d.getAttributes().getNamedItem("level").getNodeValue());
						String name = String.valueOf(d.getAttributes().getNamedItem("name").getNodeValue());
						int cost = Integer.valueOf(d.getAttributes().getNamedItem("sp").getNodeValue());
						int minLvl = Integer.valueOf(d.getAttributes().getNamedItem("min_level").getNodeValue());
						int costId = Integer.valueOf(d.getAttributes().getNamedItem("costid").getNodeValue());
						int costCount = Integer.valueOf(d.getAttributes().getNamedItem("cost").getNodeValue());
						int isDwarven = Integer.valueOf(d.getAttributes().getNamedItem("isfordwarf").getNodeValue());
			
						L2SkillLearn skill = new L2SkillLearn(id, lvl, minLvl, name, cost, costId, costCount);
						
						if (isDwarven == 0)
							_fishingSkillTrees.add(skill);
						else
							_expandDwarfCraftSkillTrees.add(skill);
					}
					
					if (d.getNodeName().equalsIgnoreCase("enchant"))
					{
						int minSkillLvl = 0;
						int id = Integer.valueOf(d.getAttributes().getNamedItem("id").getNodeValue());
						String name = String.valueOf(d.getAttributes().getNamedItem("name").getNodeValue());
						int baseLvl = Integer.valueOf(d.getAttributes().getNamedItem("base_lvl").getNodeValue());

						for (Node t2 = d.getFirstChild(); t2 != null; t2 = t2.getNextSibling())
						{
							if (t2.getNodeName().equalsIgnoreCase("data"))
							{
								int id1 = id;
								String name1 = name;
								int baseLvl1 = baseLvl;
								int lvl = Integer.valueOf(t2.getAttributes().getNamedItem("level").getNodeValue());
								// String type = String.valueOf(t2.getAttributes().getNamedItem("type").getNodeValue());
								int sp = Integer.valueOf(t2.getAttributes().getNamedItem("sp").getNodeValue());
								int exp = Integer.valueOf(t2.getAttributes().getNamedItem("exp").getNodeValue());
								byte rate76 = Byte.valueOf(t2.getAttributes().getNamedItem("rate76").getNodeValue());
								byte rate77 = Byte.valueOf(t2.getAttributes().getNamedItem("rate77").getNodeValue());
								byte rate78 = Byte.valueOf(t2.getAttributes().getNamedItem("rate78").getNodeValue());
								
								minSkillLvl = lvl == 101 || lvl == 141 ? baseLvl1 : lvl - 1;

								L2EnchantSkillLearn skill = new L2EnchantSkillLearn(id1, lvl, minSkillLvl, baseLvl1, name1, sp, exp, rate76, rate77, rate78);
								_enchantSkillTrees.add(skill);
							}
						}
					}
					
					if (d.getNodeName().equalsIgnoreCase("PledgeSkill"))
					{
						int id = Integer.valueOf(d.getAttributes().getNamedItem("skill_id").getNodeValue());
						int lvl = Integer.valueOf(d.getAttributes().getNamedItem("level").getNodeValue());
						String name = String.valueOf(d.getAttributes().getNamedItem("name").getNodeValue());
						int baseLvl = Integer.valueOf(d.getAttributes().getNamedItem("clan_lvl").getNodeValue());
						int sp = Integer.valueOf(d.getAttributes().getNamedItem("repCost").getNodeValue());
						int itemId = Integer.valueOf(d.getAttributes().getNamedItem("itemId").getNodeValue());

						L2PledgeSkillLearn skill = new L2PledgeSkillLearn(id, lvl, baseLvl, name, sp, itemId);
						_pledgeSkillTrees.add(skill);
					}
				}
			}
		}			
	}
	
	private static Map<ClassId, Map<Integer, L2SkillLearn>> getSkillTrees()
	{
		if (_skillTrees == null)
			_skillTrees = new HashMap<>();
		
		return _skillTrees;
	}
	
	public L2SkillLearn[] getAvailableSkills(L2PcInstance cha, ClassId classId)
	{
		List<L2SkillLearn> result = new ArrayList<>();
		L2Skill[] oldSkills = cha.getAllSkills();
		
		for (L2SkillLearn temp : getSkillTrees().get(classId).values())
		{
			if (temp.getMinLevel() <= cha.getLevel())
			{
				boolean knownSkill = false;
				
				for (int j = 0; j < oldSkills.length && !knownSkill; j++)
				{
					if (oldSkills[j].getId() == temp.getId())
					{
						knownSkill = true;
						
						if (oldSkills[j].getLevel() == temp.getLevel() - 1)
						{
							// this is the next level of a skill that we know
							result.add(temp);
						}
					}
				}
				
				if (!knownSkill && temp.getLevel() == 1)
				{
					// this is a new skill
					result.add(temp);
				}
			}
		}
		
		return result.toArray(new L2SkillLearn[result.size()]);
	}
	
	public Collection<L2SkillLearn> getAllAvailableSkills(L2PcInstance cha, ClassId classId)
	{
		Map<Integer, L2SkillLearn> result = new LinkedHashMap<>();
		
		int skillId, level = cha.getLevel();
		L2SkillLearn skill;
		
		for (L2SkillLearn sl : _skillTrees.get(classId).values())
		{
			skillId = sl.getId();
			// Exception for Lucky skill, it can't be learned back once lost.
			if (skillId == L2Skill.SKILL_LUCKY)
				continue;
			
			if (sl.getMinLevel() <= level)
			{
				skill = result.get(skillId);
				if (skill == null)
					result.put(skillId, sl);
				else if (sl.getLevel() > skill.getLevel())
					result.put(skillId, sl);
			}
		}
		for (L2Skill s : cha.getAllSkills())
		{
			skillId = s.getId();
			skill = result.get(skillId);
			if (skill != null)
				if (s.getLevel() >= skill.getLevel())
					result.remove(skillId);
		}
		return result.values();
	}
	
	public L2SkillLearn[] getAvailableSkills(L2PcInstance cha)
	{
		if (_fishingSkillTrees.isEmpty())
		{
			// the skilltree for this class is undefined, so we give an empty list
			_log.warning(SkillTreeData.class.getName() + ": Skilltree for fishing is not defined !");
			return new L2SkillLearn[0];
		}
		
		List<L2SkillLearn> result = new ArrayList<>();
		List<L2SkillLearn> skills = new ArrayList<>();
		
		skills.addAll(_fishingSkillTrees);
		
		if (cha.hasDwarvenCraft() && !_expandDwarfCraftSkillTrees.isEmpty())
			skills.addAll(_expandDwarfCraftSkillTrees);
		
		L2Skill[] oldSkills = cha.getAllSkills();
		
		for (L2SkillLearn temp : skills)
		{
			if (temp.getMinLevel() <= cha.getLevel())
			{
				boolean knownSkill = false;
				
				for (int j = 0; j < oldSkills.length && !knownSkill; j++)
				{
					if (oldSkills[j].getId() == temp.getId())
					{
						knownSkill = true;
						
						if (oldSkills[j].getLevel() == temp.getLevel() - 1)
						{
							// this is the next level of a skill that we know
							result.add(temp);
						}
					}
				}
				
				if (!knownSkill && temp.getLevel() == 1)
				{
					// this is a new skill
					result.add(temp);
				}
			}
		}
		
		return result.toArray(new L2SkillLearn[result.size()]);
	}
	
	public L2EnchantSkillLearn[] getAvailableEnchantSkills(L2PcInstance cha)
	{
		List<L2EnchantSkillLearn> result = new ArrayList<>();
		
		L2Skill[] oldSkills = cha.getAllSkills();
		
		for (L2EnchantSkillLearn temp : _enchantSkillTrees)
		{
			if (76 <= cha.getLevel())
			{
				boolean knownSkill = false;
				
				for (int j = 0; j < oldSkills.length && !knownSkill; j++)
				{
					if (oldSkills[j].getId() == temp.getId())
					{
						knownSkill = true;
						
						if (oldSkills[j].getLevel() == temp.getMinSkillLevel())
						{
							// this is the next level of a skill that we know
							result.add(temp);
						}
					}
				}
				
			}
		}
		
		oldSkills = null;
		
		if (Config.DEBUG)
			cha.sendMessage("loaded " + result.size() + " enchant skills for this char(You)");
		return result.toArray(new L2EnchantSkillLearn[result.size()]);
	}
	
	public L2PledgeSkillLearn[] getAvailablePledgeSkills(L2PcInstance cha)
	{
		List<L2PledgeSkillLearn> result = new ArrayList<>();
		
		L2Skill[] oldSkills = cha.getClan().getAllSkills();
		for (L2PledgeSkillLearn temp : _pledgeSkillTrees)
		{
			if (temp.getBaseLevel() <= cha.getClan().getLevel())
			{
				boolean knownSkill = false;
				
				for (int j = 0; j < oldSkills.length && !knownSkill; j++)
				{
					if (oldSkills[j].getId() == temp.getId())
					{
						knownSkill = true;
						
						if (oldSkills[j].getLevel() == temp.getLevel() - 1)
						{
							// this is the next level of a skill that we know
							result.add(temp);
						}
					}
				}
				
				if (!knownSkill && temp.getLevel() == 1)
				{
					// this is a new skill
					result.add(temp);
				}
			}
		}
		
		oldSkills = null;
		
		return result.toArray(new L2PledgeSkillLearn[result.size()]);
	}

	public Collection<L2SkillLearn> getAllowedSkills(ClassId classId)
	{
		return getSkillTrees().get(classId).values();
	}
	
	public int getMinLevelForNewSkill(L2PcInstance cha, ClassId classId)
	{
		int minLevel = 0;
		Collection<L2SkillLearn> skills = getSkillTrees().get(classId).values();
		
		if (skills == null)
		{
			// the skilltree for this class is undefined, so we give an empty list
			_log.warning(SkillTreeData.class.getName() + ": Skilltree for class " + classId + " is not defined !");
			return minLevel;
		}
		
		for (L2SkillLearn temp : skills)
		{
			if (temp.getMinLevel() > cha.getLevel() && temp.getSpCost() != 0)
				if (minLevel == 0 || temp.getMinLevel() < minLevel)
					minLevel = temp.getMinLevel();
		}
		
		skills = null;
		
		return minLevel;
	}
	
	public int getMinLevelForNewSkill(L2PcInstance cha)
	{
		int minLevel = 0;
		List<L2SkillLearn> skills = new ArrayList<>();
		
		skills.addAll(_fishingSkillTrees);
		
		if (skills.isEmpty())
		{
			// the skilltree for this class is undefined, so we give an empty list
			_log.warning(SkillTreeData.class.getName() + ": SkillTree for fishing is not defined !");
			return minLevel;
		}
		
		if (cha.hasDwarvenCraft())
			skills.addAll(_expandDwarfCraftSkillTrees);
		
		for (L2SkillLearn s : skills)
		{
			if (s.getMinLevel() > cha.getLevel())
				if (minLevel == 0 || s.getMinLevel() < minLevel)
					minLevel = s.getMinLevel();
		}
		
		skills = null;
		
		return minLevel;
	}
	
	public int getSkillCost(L2PcInstance player, L2Skill skill)
	{
		int skillCost = 100000000;
		ClassId classId = player.getSkillLearningClassId();
		int skillHashCode = SkillTable.getSkillHashCode(skill);
		
		if (getSkillTrees().get(classId).containsKey(skillHashCode))
		{
			L2SkillLearn skillLearn = getSkillTrees().get(classId).get(skillHashCode);
			if (skillLearn.getMinLevel() <= player.getLevel())
			{
				skillCost = skillLearn.getSpCost();
				if (!player.getClassId().equalsOrChildOf(classId))
				{
					if (skill.getCrossLearnAdd() < 0)
						return skillCost;
					
					skillCost += skill.getCrossLearnAdd();
					skillCost *= skill.getCrossLearnMul();
				}
				
				if (classId.getRace() != player.getRace() && !player.isSubClassActive())
					skillCost *= skill.getCrossLearnRace();
				
				if (classId.getType() != player.getClassId().getType())
					skillCost *= skill.getCrossLearnProf();
			}
			
			skillLearn = null;
		}
		
		classId = null;
		
		return skillCost;
	}
	
	public int getSkillSpCost(L2PcInstance player, L2Skill skill)
	{
		int skillCost = 100000000;
		L2EnchantSkillLearn[] enchantSkillLearnList = getAvailableEnchantSkills(player);
		
		for (L2EnchantSkillLearn enchantSkillLearn : enchantSkillLearnList)
		{
			if (enchantSkillLearn.getId() != skill.getId())
				continue;
			
			if (enchantSkillLearn.getLevel() != skill.getLevel())
				continue;
			
			if (76 > player.getLevel())
				continue;
			
			skillCost = enchantSkillLearn.getSpCost();
		}
		return skillCost;
	}
	
	public int getSkillExpCost(L2PcInstance player, L2Skill skill)
	{
		int skillCost = 100000000;
		L2EnchantSkillLearn[] enchantSkillLearnList = getAvailableEnchantSkills(player);
		
		for (L2EnchantSkillLearn enchantSkillLearn : enchantSkillLearnList)
		{
			if (enchantSkillLearn.getId() != skill.getId())
			{
				continue;
			}
			
			if (enchantSkillLearn.getLevel() != skill.getLevel())
			{
				continue;
			}
			
			if (76 > player.getLevel())
			{
				continue;
			}
			
			skillCost = enchantSkillLearn.getExp();
		}
		
		enchantSkillLearnList = null;
		
		return skillCost;
	}
	
	public L2SkillLearn getSkillLearnBySkillIdLevel(ClassId classId, int skillId, int skillLvl)
	{
		for (L2SkillLearn sl : getAllowedSkills(classId))
		{
			if (sl.getId() == skillId && sl.getLevel() == skillLvl)
				return sl;
		}
		return null;
	}
	
	public byte getSkillRate(L2PcInstance player, L2Skill skill)
	{
		L2EnchantSkillLearn[] enchantSkillLearnList = getAvailableEnchantSkills(player);
		
		for (L2EnchantSkillLearn enchantSkillLearn : enchantSkillLearnList)
		{
			if (enchantSkillLearn.getId() != skill.getId())
			{
				continue;
			}
			
			if (enchantSkillLearn.getLevel() != skill.getLevel())
			{
				continue;
			}
			
			return enchantSkillLearn.getRate(player);
		}
		
		enchantSkillLearnList = null;
		
		return 0;
	}
	
	
	public int getExpertiseLevel(int grade)
	{
		if (grade <= 0)
			return 0;
		
		// since expertise comes at same level for all classes we use paladin for now
		Map<Integer, L2SkillLearn> learnMap = getSkillTrees().get(ClassId.PALADIN);
		
		int skillHashCode = SkillTable.getSkillHashCode(239, grade);
		
		if (learnMap.containsKey(skillHashCode))
			return learnMap.get(skillHashCode).getMinLevel();
		
		learnMap = null;
		
		_log.warning(SkillTreeData.class.getName() + ": Expertise not found for grade " + grade);
		return 0;
	}
	
	public int getMinSkillLevel(int skillId, ClassId classId, int skillLvl)
	{
		Map<Integer, L2SkillLearn> map = getSkillTrees().get(classId);
		
		int skillHashCode = SkillTable.getSkillHashCode(skillId, skillLvl);
		
		if (map.containsKey(skillHashCode))
			return map.get(skillHashCode).getMinLevel();
		
		map = null;
		
		return 0;
	}
	
	public int getMinSkillLevel(int skillId, int skillLvl)
	{
		int skillHashCode = SkillTable.getSkillHashCode(skillId, skillLvl);
		return getSkillTrees().values().stream().filter(sk -> sk.containsKey(skillHashCode)).mapToInt(sk1 -> sk1.get(skillHashCode).getMinLevel()).findFirst().orElse(0);	
	}

	public boolean isClanSkill(int skillId, int skillLevel)
	{
		final long hashCode = SkillTable.getSkillHashCode(skillId, skillLevel);
		return _pledgeSkillTrees.contains(hashCode);
	}
	
	public static SkillTreeData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SkillTreeData _instance = new SkillTreeData();
	}
}