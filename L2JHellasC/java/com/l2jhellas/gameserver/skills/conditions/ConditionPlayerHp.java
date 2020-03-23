package com.l2jhellas.gameserver.skills.conditions;

import com.l2jhellas.gameserver.skills.Env;

public class ConditionPlayerHp extends Condition
{
	private final int _hp;
	
	public ConditionPlayerHp(int hp)
	{
		_hp = hp;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		return env.player.getCurrentHp() * 100 / env.player.getMaxHp() <= _hp;
	}
}