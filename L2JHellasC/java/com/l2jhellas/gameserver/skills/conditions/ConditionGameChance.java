package com.l2jhellas.gameserver.skills.conditions;

import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.util.Rnd;

public class ConditionGameChance extends Condition
{
	private final int _chance;
	
	public ConditionGameChance(int chance)
	{
		_chance = chance;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		return Rnd.get(100) < _chance;
	}
}