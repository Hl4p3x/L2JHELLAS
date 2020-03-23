package com.l2jhellas.gameserver.holder;

import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.skills.SkillTable;

public final class SkillHolder
{
	private final int _skillId;
	private final int _skillLvl;
	
	public SkillHolder(int skillId, int skillLvl)
	{
		_skillId = skillId;
		_skillLvl = skillLvl;
	}
	
	public SkillHolder(int skillId) 
	{
		_skillId = skillId;
		_skillLvl = 1;
	}
	
	public SkillHolder(L2Skill skill)
	{
		_skillId = skill.getId();
		_skillLvl = skill.getLevel();
	}
	
	public final int getSkillId()
	{
		return _skillId;
	}
	
	public final int getSkillLvl()
	{
		return _skillLvl;
	}
	
	public final L2Skill getSkill()
	{
		return SkillTable.getInstance().getInfo(_skillId, _skillLvl);
	}
}