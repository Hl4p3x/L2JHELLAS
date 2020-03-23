package com.l2jhellas.gameserver.enums.skills;

import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.skills.SkillTable;

/**
 * @author AbsolutePower
 */
public enum HeroSkills
{
	HEROIC_MIRACLE(395,1),
	HEROIC_BERSERKER(396,1),
	HEROIC_VALOR(1374,1),
	HEROIC_GRANDEUR(1375,1),
	HEROIC_DREAD(1376,1);
	
	private final int _skillId;
	private final int _level;

	private HeroSkills(int skillId,int level)
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
	
	public static HeroSkills[] getAllSkills()
	{
		return values();	
	}
		
	public static boolean isHeroSkill(int skillId)
	{	
		for (HeroSkills current : values())
		{
			if (current.getSkillId() == skillId)
				return true;
		}		
		return false;	
	}

	public static L2Skill getSkillBy(int skillId)
	{
		for (HeroSkills current : values())
		{
			if (current.getSkillId() == skillId)
			  return SkillTable.getInstance().getInfo(current.getSkillId(),current.getLevel());
		}		
		return null;		
	}
}