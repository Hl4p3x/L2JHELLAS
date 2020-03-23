package com.l2jhellas.gameserver.skills;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.PackRoot;
import com.l2jhellas.gameserver.engines.Item;
import com.l2jhellas.gameserver.enums.items.L2EtcItemType;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.item.L2EtcItem;
import com.l2jhellas.gameserver.model.actor.item.L2Item;
import com.l2jhellas.gameserver.templates.L2Armor;
import com.l2jhellas.gameserver.templates.L2Weapon;

public class SkillsEngine
{
	
	protected static final Logger _log = Logger.getLogger(SkillsEngine.class.getName());
	
	private static final SkillsEngine _instance = new SkillsEngine();
	
	private final List<File> _armorFiles = new ArrayList<>();
	private final List<File> _weaponFiles = new ArrayList<>();
	private final List<File> _etcitemFiles = new ArrayList<>();
	private final List<File> _skillFiles = new ArrayList<>();
	
	public static SkillsEngine getInstance()
	{
		return _instance;
	}
	
	private SkillsEngine()
	{
		// hashFiles("data/stats/etcitem", _etcitemFiles);
		hashFiles("data/stats/armor", _armorFiles);
		hashFiles("data/stats/weapon", _weaponFiles);
		hashFiles("data/stats/skills", _skillFiles);
	}
	
	private static void hashFiles(String dirname, List<File> hash)
	{
		File dir = new File(PackRoot.DATAPACK_ROOT, dirname);
		if (!dir.exists())
		{
			_log.info("Dir " + dir.getAbsolutePath() + " not exists");
			return;
		}
		File[] files = dir.listFiles();
		for (File f : files)
		{
			if (f.getName().endsWith(".xml"))
				if (!f.getName().startsWith("custom"))
					hash.add(f);
		}
		File customfile = new File(PackRoot.DATAPACK_ROOT, dirname + "/custom.xml");
		if (customfile.exists())
			hash.add(customfile);
	}
	
	public List<L2Skill> loadSkills(File file)
	{
		if (file == null)
		{
			_log.config("Skill file not found.");
			return null;
		}
		DocumentSkill doc = new DocumentSkill(file);
		doc.parse();
		return doc.getSkills();
	}
	
	public void loadAllSkills(Map<Integer, L2Skill> allSkills)
	{
		int count = 0;
		for (File file : _skillFiles)
		{
			List<L2Skill> s = loadSkills(file);
			if (s == null)
				continue;
			for (L2Skill skill : s)
			{
				allSkills.put(SkillTable.getSkillHashCode(skill), skill);
				count++;
			}
		}
		_log.info("SkillsEngine: Loaded " + count + " Skill templates from XML files.");
	}
	
	public List<L2Armor> loadArmors(Map<Integer, Item> armorData)
	{
		List<L2Armor> list = new ArrayList<>();
		for (L2Item item : loadData(armorData, _armorFiles))
		{
			list.add((L2Armor) item);
		}
		return list;
	}
	
	public List<L2Weapon> loadWeapons(Map<Integer, Item> weaponData)
	{
		List<L2Weapon> list = new ArrayList<>();
		for (L2Item item : loadData(weaponData, _weaponFiles))
		{
			list.add((L2Weapon) item);
		}
		return list;
	}
	
	public List<L2EtcItem> loadItems(Map<Integer, Item> itemData)
	{
		List<L2EtcItem> list = new ArrayList<>();
		for (L2Item item : loadData(itemData, _etcitemFiles))
		{
			list.add((L2EtcItem) item);
		}
		if (list.size() == 0)
		{
			for (Item item : itemData.values())
			{
				list.add(new L2EtcItem((L2EtcItemType) item.type, item.set));
			}
		}
		return list;
	}
	
	public List<L2Item> loadData(Map<Integer, Item> itemData, List<File> files)
	{
		List<L2Item> list = new ArrayList<>();
		for (File f : files)
		{
			DocumentItem document = new DocumentItem(itemData, f);
			document.parse();
			list.addAll(document.getItemList());
		}
		return list;
	}
}