package com.l2jhellas.gameserver.skills.conditions;

import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.skills.Env;

public class ConditionTargetAggro extends Condition
{
	private final boolean _isAggro;
	
	public ConditionTargetAggro(boolean isAggro)
	{
		_isAggro = isAggro;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		L2Character target = env.target;
		if (target instanceof L2MonsterInstance)
		{
			return ((L2MonsterInstance) target).isAggressive() == _isAggro;
		}
		if (target instanceof L2PcInstance)
		{
			return ((L2PcInstance) target).getKarma() > 0;
		}
		return false;
	}
}