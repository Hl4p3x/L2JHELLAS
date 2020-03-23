package com.l2jhellas.gameserver.skills.conditions;

import com.l2jhellas.gameserver.skills.Env;

public final class ConditionUsingSkill extends Condition
{
	private final int _skillId;
	
	public ConditionUsingSkill(int skillId)
	{
		_skillId = skillId;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if (env.skill == null)
			return false;
		return env.skill.getId() == _skillId;
	}
}