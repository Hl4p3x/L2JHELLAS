package com.l2jhellas.gameserver.skills.conditions;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.skills.Env;

public class ConditionPlayerInvSize extends Condition
{
	private final int _size;
	
	public ConditionPlayerInvSize(int size)
	{
		_size = size;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if (!(env.player instanceof L2PcInstance))
			return false;
		
		if (env.player != null)
			return ((L2PcInstance) env.player).getInventory().getSize() <= (((L2PcInstance) env.player).getInventoryLimit() - _size);
		
		return true;
	}
}