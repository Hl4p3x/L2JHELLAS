package com.l2jhellas.gameserver.model;

import com.l2jhellas.gameserver.enums.items.ItemLocation;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class PcWarehouse extends Warehouse
{
	private final L2PcInstance _owner;
	
	public PcWarehouse(L2PcInstance owner)
	{
		_owner = owner;
	}
	
	@Override
	public L2PcInstance getOwner()
	{
		return _owner;
	}
	
	@Override
	public ItemLocation getBaseLocation()
	{
		return ItemLocation.WAREHOUSE;
	}
	
	public String getLocationId()
	{
		return "0";
	}
	
	public int getLocationId(boolean dummy)
	{
		return 0;
	}
	
	public void setLocationId(L2PcInstance dummy)
	{
		return;
	}
	
	@Override
	public boolean validateCapacity(int slots)
	{
		return (_items.size() + slots <= _owner.getWareHouseLimit());
	}
}