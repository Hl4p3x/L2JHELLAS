package com.l2jhellas.gameserver.skills.conditions;

import com.l2jhellas.gameserver.skills.Env;

public class ConditionTargetLevel extends Condition
{
	private final int _level;
	
	public ConditionTargetLevel(int level)
	{
		_level = level;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if (env.target == null)
			return false;
		return env.target.getLevel() >= _level;
	}
}