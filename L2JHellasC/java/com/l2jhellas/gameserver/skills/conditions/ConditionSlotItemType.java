package com.l2jhellas.gameserver.skills.conditions;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.Inventory;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.skills.Env;

public final class ConditionSlotItemType extends ConditionInventory
{
	private final int _mask;
	
	public ConditionSlotItemType(int slot, int mask)
	{
		super(slot);
		_mask = mask;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if (!(env.player instanceof L2PcInstance))
			return false;
		Inventory inv = ((L2PcInstance) env.player).getInventory();
		L2ItemInstance item = inv.getPaperdollItem(_slot);
		if (item == null)
			return false;
		return (item.getItem().getItemMask() & _mask) != 0;
	}
}