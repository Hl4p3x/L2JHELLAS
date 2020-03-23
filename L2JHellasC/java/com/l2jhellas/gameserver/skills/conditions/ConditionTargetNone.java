package com.l2jhellas.gameserver.skills.conditions;

import com.l2jhellas.gameserver.skills.Env;

public class ConditionTargetNone extends Condition
{
	public ConditionTargetNone()
	{
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		return (env.target == null);
	}
}