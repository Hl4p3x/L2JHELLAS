package com.l2jhellas.gameserver.skills.conditions;

import com.l2jhellas.gameserver.skills.Env;

public class ConditionPlayerHpPercentage extends Condition
{
	private final double _p;
	
	public ConditionPlayerHpPercentage(double p)
	{
		_p = p;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		return env.player.getCurrentHp() <= env.player.getMaxHp() * _p;
	}
}