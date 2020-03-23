package com.l2jhellas.gameserver.skills.conditions;

import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.Stats;

public class ConditionSkillStats extends Condition
{
	private final Stats _stat;
	
	public ConditionSkillStats(Stats stat)
	{
		super();
		_stat = stat;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if (env.skill == null)
			return false;
		return env.skill.getStat() == _stat;
	}
}