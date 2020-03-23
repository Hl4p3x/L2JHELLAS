package com.l2jhellas.gameserver.skills.conditions;

import com.l2jhellas.gameserver.skills.Env;

public final class ConditionItemId extends Condition
{
	private final int _itemId;
	
	public ConditionItemId(int itemId)
	{
		_itemId = itemId;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if (env.item == null)
			return false;
		return env.item.getItemId() == _itemId;
	}
}