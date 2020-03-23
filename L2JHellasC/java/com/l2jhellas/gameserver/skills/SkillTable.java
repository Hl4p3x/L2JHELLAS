package com.l2jhellas.gameserver.skills;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.l2jhellas.gameserver.enums.items.L2WeaponType;
import com.l2jhellas.gameserver.model.L2Skill;

public class SkillTable
{
	private static Logger _log = Logger.getLogger(SkillTable.class.getName());
	
	private static Map<Integer, L2Skill> _skills = new HashMap<>();
		
	protected SkillTable()
	{
		SkillsEngine.getInstance().loadAllSkills(_skills);
		_log.info(SkillTable.class.getSimpleName() + ": Loaded " + _skills.size() + " skills.");
	}
	
	public static void reload()
	{
		_skills.clear();
		SkillsEngine.getInstance().loadAllSkills(_skills);
		_log.info(SkillTable.class.getSimpleName() + ": Loaded " + _skills.size() + " skills.");
	}

	public static int getSkillHashCode(L2Skill skill)
	{
		return SkillTable.getSkillHashCode(skill.getId(), skill.getLevel());
	}
	
	public static int getSkillHashCode(int skillId, int skillLevel)
	{
		return skillId * 256 + skillLevel;
	}
	
	public L2Skill getInfo(int skillId, int level)
	{
		return _skills.get(SkillTable.getSkillHashCode(skillId, level));
	}
	
	public int getMaxLevel(int magicId, int level)
	{
		L2Skill temp;
		
		while (level < 100)
		{
			level++;
			temp = _skills.get(SkillTable.getSkillHashCode(magicId, level));
			
			if (temp == null)
				return level - 1;
		}
		
		return level;
	}
	
	private static final L2WeaponType[] weaponDbMasks =
	{
		L2WeaponType.ETC,
		L2WeaponType.BOW,
		L2WeaponType.POLE,
		L2WeaponType.DUALFIST,
		L2WeaponType.DUAL,
		L2WeaponType.BLUNT,
		L2WeaponType.SWORD,
		L2WeaponType.DAGGER,
		L2WeaponType.BIGSWORD,
		L2WeaponType.ROD,
		L2WeaponType.BIGBLUNT
	};
	
	public int calcWeaponsAllowed(int mask)
	{
		if (mask == 0)
			return 0;
		
		int weaponsAllowed = 0;
		
		for (int i = 0; i < weaponDbMasks.length; i++)
			if ((mask & (1 << i)) != 0)
				weaponsAllowed |= weaponDbMasks[i].mask();
		
		return weaponsAllowed;
	}
	
	public static SkillTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SkillTable _instance = new SkillTable();
	}
}