package com.l2jhellas.gameserver.skills.conditions;

import com.l2jhellas.gameserver.skills.Env;

public class ConditionPlayerMp extends Condition
{
	private final int _mp;
	
	public ConditionPlayerMp(int mp)
	{
		_mp = mp;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		return env.player.getCurrentMp() * 100 / env.player.getMaxMp() <= _mp;
	}
}