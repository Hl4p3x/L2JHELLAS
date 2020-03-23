package com.l2jhellas.gameserver.model;

import java.util.Collection;
import java.util.stream.Collectors;

import com.l2jhellas.gameserver.enums.items.ItemLocation;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.ItemContainer;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;

public class PcFreight extends ItemContainer
{
	private final L2PcInstance _owner; // This is the L2PcInstance that owns this Freight;
	private int _activeLocationId;
	
	public PcFreight(L2PcInstance owner)
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
		return ItemLocation.FREIGHT;
	}
	
	public void setActiveLocation(int locationId)
	{
		_activeLocationId = locationId;
	}
	
	public int getactiveLocation()
	{
		return _activeLocationId;
	}
	
	@Override
	public int getSize()
	{
		int size = 0;
		for (L2ItemInstance item : _items.values())
		{
			if (item.getEquipSlot() == 0 || _activeLocationId == 0 || item.getEquipSlot() == _activeLocationId)
				size++;
		}
		return size;
	}
	
	@Override
	public Collection<L2ItemInstance> getItems()
	{
		return _items.values().stream().filter(i -> i.getLocationSlot() == 0 || i.getLocationSlot() == _activeLocationId).collect(Collectors.toList());
	}
	
	@Override
	public L2ItemInstance getItemByItemId(int itemId)
	{
		return _items.values().stream().filter(it -> it.getItemId() == itemId && (it.getEquipSlot() == 0 || _activeLocationId == 0 || it.getEquipSlot() == _activeLocationId)).findFirst().orElse(null);
	}
	
	@Override
	protected void addItem(L2ItemInstance item)
	{
		super.addItem(item);
		if (_activeLocationId > 0)
			item.setLocation(item.getLocation(), _activeLocationId);
	}
	
	@Override
	public void restore()
	{
		int locationId = _activeLocationId;
		_activeLocationId = 0;
		super.restore();
		_activeLocationId = locationId;
	}
	
	@Override
	public boolean validateCapacity(int slots)
	{
		return (getSize() + slots <= _owner.getFreightLimit());
	}
}