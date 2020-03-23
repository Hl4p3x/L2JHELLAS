package com.l2jhellas.gameserver.model;

import com.l2jhellas.gameserver.datatables.sql.ItemTable;
import com.l2jhellas.gameserver.enums.items.ItemLocation;
import com.l2jhellas.gameserver.enums.items.L2EtcItemType;
import com.l2jhellas.gameserver.model.actor.instance.L2PetInstance;
import com.l2jhellas.gameserver.model.actor.item.Inventory;
import com.l2jhellas.gameserver.model.actor.item.L2Item;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;

public class PetInventory extends Inventory
{
	private final L2PetInstance _owner;
	
	public PetInventory(L2PetInstance owner)
	{
		_owner = owner;
	}
	
	@Override
	public int getOwnerId()
	{
		// gets the L2PcInstance-owner's ID
		int id;
		try
		{
			id = _owner.getOwner().getObjectId();
		}
		catch (NullPointerException e)
		{
			return 0;
		}
		return id;
	}
	
	@Override
	public L2PetInstance getOwner()
	{
		return _owner;
	}
	
	@Override
	protected void refreshWeight()
	{
		super.refreshWeight();
		getOwner().updateAndBroadcastStatus(1);
	}
	
	@Override
	protected ItemLocation getBaseLocation()
	{
		return ItemLocation.PET;
	}
	
	public boolean validateCapacity(L2ItemInstance item)
	{
		int slots = 0;
		
		if (!(item.isStackable() && getItemByItemId(item.getItemId()) != null) && item.getItemType() != L2EtcItemType.HERB)
			slots++;
		
		return validateCapacity(slots);
	}
	
	@Override
	public boolean validateCapacity(int slots)
	{
		return (_items.size() + slots <= _owner.getInventoryLimit());
	}
	
	public boolean validateWeight(L2ItemInstance item, long count)
	{
		int weight = 0;
		
		L2Item template = ItemTable.getInstance().getTemplate(item.getItemId());
		if (template == null)
			return false;
		
		weight += count * template.getWeight();
		return validateWeight(weight);
	}
	
	@Override
	public boolean validateWeight(int weight)
	{
		return (_totalWeight + weight <= _owner.getMaxLoad());
	}
	
	@Override
	protected ItemLocation getEquipLocation()
	{
		return ItemLocation.PET_EQUIP;
	}
}