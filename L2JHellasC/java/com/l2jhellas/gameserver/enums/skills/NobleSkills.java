package com.l2jhellas.gameserver.enums.skills;

import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.skills.SkillTable;

/**
 * @author AbsolutePower
 */
public enum NobleSkills
{
	STRDDER_SIEGE_ASSAULT(325,1),
	BUILD_ADVANCED_HEADQUARTERS(326,1),
	WYVERN_AEGIS(327,1),
	NONLESSE_BLESSING(1323,1),
	SUMMON_CP_POTION(1324,1),
	FORTUNE_OF_NOBLESSE(1325,1),
	HARMONY_OF_NOBLESSE(1326,1),
	SYMPHONY_OF_NOBLESSE(1327,1);
	
	private final int _skillId;
	private final int _level;

	private NobleSkills(int skillId,int level)
	{
		_skillId = skillId;
		_level = level;
	}

	public int getSkillId()
	{
		return _skillId;
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public static NobleSkills[] getAllSkills()
	{
		return values();	
	}
		
	public static boolean isNobleSkill(int skillId)
	{	
		for (NobleSkills current : values())
		{
			if (current.getSkillId() == skillId)
				return true;
		}		
		return false;	
	}

	public static L2Skill getSkillBy(int skillId)
	{
		for (NobleSkills current : values())
		{
			if (current.getSkillId() == skillId)
			  return SkillTable.getInstance().getInfo(current.getSkillId(),current.getLevel());
		}		
		return null;		
	}
}