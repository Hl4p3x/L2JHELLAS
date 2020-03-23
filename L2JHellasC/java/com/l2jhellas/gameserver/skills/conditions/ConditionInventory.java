package com.l2jhellas.gameserver.skills.conditions;

import com.l2jhellas.gameserver.skills.Env;

public abstract class ConditionInventory extends Condition
{
	protected final int _slot;
	
	public ConditionInventory(int slot)
	{
		_slot = slot;
	}
	
	@Override
	public abstract boolean testImpl(Env env);
}