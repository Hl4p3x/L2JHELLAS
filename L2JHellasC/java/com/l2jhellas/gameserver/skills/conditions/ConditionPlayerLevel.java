package com.l2jhellas.gameserver.skills.conditions;

import com.l2jhellas.gameserver.skills.Env;

public class ConditionPlayerLevel extends Condition
{
	private final int _level;
	
	public ConditionPlayerLevel(int level)
	{
		_level = level;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		return env.player.getLevel() >= _level;
	}
}