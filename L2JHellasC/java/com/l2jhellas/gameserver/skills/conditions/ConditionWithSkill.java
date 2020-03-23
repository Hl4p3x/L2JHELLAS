package com.l2jhellas.gameserver.skills.conditions;

import com.l2jhellas.gameserver.skills.Env;

public class ConditionWithSkill extends Condition
{
	private final boolean _skill;
	
	public ConditionWithSkill(boolean skill)
	{
		_skill = skill;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if (!_skill && env.skill != null)
			return false;
		return true;
	}
}