package com.l2jhellas.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.l2jhellas.gameserver.enums.items.ItemLocation;
import com.l2jhellas.gameserver.enums.items.L2EtcItemType;
import com.l2jhellas.gameserver.model.TradeList.TradeItem;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.Inventory;
import com.l2jhellas.gameserver.model.actor.item.ItemContainer;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jhellas.gameserver.network.serverpackets.StatusUpdate;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class PcInventory extends Inventory
{
	public static final int ADENA_ID = 57;
	public static final int ANCIENT_ADENA_ID = 5575;
	
	private final L2PcInstance _owner;
	private L2ItemInstance _adena;
	private L2ItemInstance _ancientAdena;
	
	public PcInventory(L2PcInstance owner)
	{
		_owner = owner;
	}
	
	@Override
	public L2PcInstance getOwner()
	{
		return _owner;
	}
	
	@Override
	protected ItemLocation getBaseLocation()
	{
		return ItemLocation.INVENTORY;
	}
	
	@Override
	protected ItemLocation getEquipLocation()
	{
		return ItemLocation.PAPERDOLL;
	}
	
	public L2ItemInstance getAdenaInstance()
	{
		return _adena;
	}
	
	@Override
	public int getAdena()
	{
		return _adena != null ? _adena.getCount() : 0;
	}
	
	public L2ItemInstance getAncientAdenaInstance()
	{
		return _ancientAdena;
	}
	
	public int getAncientAdena()
	{
		return (_ancientAdena != null) ? _ancientAdena.getCount() : 0;
	}
	
	public L2ItemInstance[] getUniqueItems(boolean allowAdena, boolean allowAncientAdena)
	{
		return getUniqueItems(allowAdena, allowAncientAdena, true);
	}
	
	public L2ItemInstance[] getUniqueItems(boolean allowAdena, boolean allowAncientAdena, boolean onlyAvailable)
	{
		List<L2ItemInstance> list = new ArrayList<>();
		for (L2ItemInstance item : _items.values())
		{
			if ((!allowAdena && item.getItemId() == 57))
				continue;
			if ((!allowAncientAdena && item.getItemId() == 5575))
				continue;
			
			boolean isDuplicate = false;
			for (L2ItemInstance litem : list)
			{
				if (litem.getItemId() == item.getItemId())
				{
					isDuplicate = true;
					break;
				}
			}
			if (!isDuplicate && (!onlyAvailable || (item.getItem().isSellable() && item.isAvailable(getOwner(), false))))
				list.add(item);
		}
		
		return list.toArray(new L2ItemInstance[list.size()]);
	}
	
	public L2ItemInstance[] getUniqueItemsByEnchantLevel(boolean allowAdena, boolean allowAncientAdena)
	{
		return getUniqueItemsByEnchantLevel(allowAdena, allowAncientAdena, true);
	}
	
	public L2ItemInstance[] getUniqueItemsByEnchantLevel(boolean allowAdena, boolean allowAncientAdena, boolean onlyAvailable)
	{
		List<L2ItemInstance> list = new ArrayList<>();
		for (L2ItemInstance item : _items.values())
		{
			if ((!allowAdena && item.getItemId() == 57))
				continue;
			if ((!allowAncientAdena && item.getItemId() == 5575))
				continue;
			
			boolean isDuplicate = false;
			for (L2ItemInstance litem : list)
				if ((litem.getItemId() == item.getItemId()) && (litem.getEnchantLevel() == item.getEnchantLevel()))
				{
					isDuplicate = true;
					break;
				}
			if (!isDuplicate && (!onlyAvailable || (item.getItem().isSellable() && item.isAvailable(getOwner(), false))))
				list.add(item);
		}
		
		return list.toArray(new L2ItemInstance[list.size()]);
	}
	
	public L2ItemInstance[] getAllItemsByItemId(int itemId)
	{
		List<L2ItemInstance> list = new ArrayList<>();
		for (L2ItemInstance item : _items.values())
		{
			if (item.getItemId() == itemId)
				list.add(item);
		}
		
		return list.toArray(new L2ItemInstance[list.size()]);
	}
	
	public L2ItemInstance[] getAllItemsByItemId(int itemId, int enchantment)
	{
		List<L2ItemInstance> list = new ArrayList<>();
		for (L2ItemInstance item : _items.values())
		{
			if ((item.getItemId() == itemId) && (item.getEnchantLevel() == enchantment))
				list.add(item);
		}
		
		return list.toArray(new L2ItemInstance[list.size()]);
	}
	
	public L2ItemInstance[] getAvailableItems(boolean allowAdena)
	{
		List<L2ItemInstance> list = new ArrayList<>();
		for (L2ItemInstance item : _items.values())
			if (item != null && item.isAvailable(getOwner(), allowAdena))
				list.add(item);
		
		return list.toArray(new L2ItemInstance[list.size()]);
	}
	
	public L2ItemInstance[] getAugmentedItems()
	{
		List<L2ItemInstance> list = new ArrayList<>();
		for (L2ItemInstance item : _items.values())
			if (item != null && item.isAugmented())
				list.add(item);
		
		return list.toArray(new L2ItemInstance[list.size()]);
	}
	
	public TradeList.TradeItem[] getAvailableItems(TradeList tradeList)
	{
		List<TradeList.TradeItem> list = new ArrayList<>();
		for (L2ItemInstance item : _items.values())
			if (item.isAvailable(getOwner(), false))
			{
				TradeList.TradeItem adjItem = tradeList.adjustAvailableItem(item);
				if (adjItem != null)
					list.add(adjItem);
			}
		
		return list.toArray(new TradeList.TradeItem[list.size()]);
	}
	
	public void adjustAvailableItem(TradeItem item)
	{
		for (L2ItemInstance adjItem : _items.values())
		{
			if (adjItem.getItemId() == item.getItem().getItemId())
			{
				item.setObjectId(adjItem.getObjectId());
				item.setEnchant(adjItem.getEnchantLevel());
				
				if (adjItem.getCount() < item.getCount())
					item.setCount(adjItem.getCount());
				
				return;
			}
		}
		
		item.setCount(0);
	}
	
	public void addAdena(String process, int count, L2PcInstance actor, L2Object reference)
	{
		if (count > 0)
			addItem(process, ADENA_ID, count, actor, reference);
	}
	
	public void reduceAdena(String process, int count, L2PcInstance actor, L2Object reference)
	{
		if (count > 0)
			destroyItemByItemId(process, ADENA_ID, count, actor, reference);
	}
	
	public void addAncientAdena(String process, int count, L2PcInstance actor, L2Object reference)
	{
		if (count > 0)
			addItem(process, ANCIENT_ADENA_ID, count, actor, reference);
	}
	
	public void reduceAncientAdena(String process, int count, L2PcInstance actor, L2Object reference)
	{
		if (count > 0)
			destroyItemByItemId(process, ANCIENT_ADENA_ID, count, actor, reference);
	}
	
	@Override
	public L2ItemInstance addItem(String process, L2ItemInstance item, L2PcInstance actor, L2Object reference)
	{
		item = super.addItem(process, item, actor, reference);
		
		if (item != null && item.getItemId() == ADENA_ID && !item.equals(_adena))
			_adena = item;
		
		if (item != null && item.getItemId() == ANCIENT_ADENA_ID && !item.equals(_ancientAdena))
			_ancientAdena = item;
		
		return item;
	}
	
	@Override
	public L2ItemInstance addItem(String process, int itemId, int count, L2PcInstance actor, L2Object reference)
	{
		L2ItemInstance item = super.addItem(process, itemId, count, actor, reference);
		
		if (item != null)
		{
		   if (item.getItemId() == ADENA_ID && !item.equals(_adena))
			  _adena = item;
		
		   if (item.getItemId() == ANCIENT_ADENA_ID && !item.equals(_ancientAdena))
			 _ancientAdena = item;
		
		   if (actor != null)
		   {
			   InventoryUpdate playerIU = new InventoryUpdate();
			   playerIU.addItem(item);
			   actor.sendPacket(playerIU);
			
			   StatusUpdate su = new StatusUpdate(actor.getObjectId());
			   su.addAttribute(StatusUpdate.CUR_LOAD, actor.getCurrentLoad());
			   actor.sendPacket(su);
		   }
		}
		
		return item;
	}
	
	@Override
	public L2ItemInstance transferItem(String process, int objectId, int count, ItemContainer target, L2PcInstance actor, L2Object reference)
	{
		L2ItemInstance item = super.transferItem(process, objectId, count, target, actor, reference);
		
		if (_adena != null && (_adena.getCount() <= 0 || _adena.getOwnerId() != getOwnerId()))
			_adena = null;
		
		if (_ancientAdena != null && (_ancientAdena.getCount() <= 0 || _ancientAdena.getOwnerId() != getOwnerId()))
			_ancientAdena = null;
		
		return item;
	}
	
	@Override
	public L2ItemInstance destroyItem(String process, L2ItemInstance item, L2PcInstance actor, L2Object reference)
	{
		item = super.destroyItem(process, item, actor, reference);
		
		if (_adena != null && _adena.getCount() <= 0)
			_adena = null;
		
		if (_ancientAdena != null && _ancientAdena.getCount() <= 0)
			_ancientAdena = null;
		
		return item;
	}
	
	@Override
	public L2ItemInstance destroyItem(String process, int objectId, int count, L2PcInstance actor, L2Object reference)
	{
		L2ItemInstance item = super.destroyItem(process, objectId, count, actor, reference);
		
		if (_adena != null && _adena.getCount() <= 0)
			_adena = null;
		
		if (_ancientAdena != null && _ancientAdena.getCount() <= 0)
			_ancientAdena = null;
		
		return item;
	}
	
	@Override
	public L2ItemInstance destroyItemByItemId(String process, int itemId, int count, L2PcInstance actor, L2Object reference)
	{
		L2ItemInstance item = super.destroyItemByItemId(process, itemId, count, actor, reference);
		
		if (_adena != null && _adena.getCount() <= 0)
			_adena = null;
		
		if (_ancientAdena != null && _ancientAdena.getCount() <= 0)
			_ancientAdena = null;
		
		return item;
	}
	
	@Override
	public L2ItemInstance dropItem(String process, L2ItemInstance item, L2PcInstance actor, L2Object reference)
	{
		item = super.dropItem(process, item, actor, reference);
		
		if (_adena != null && (_adena.getCount() <= 0 || _adena.getOwnerId() != getOwnerId()))
			_adena = null;
		
		if (_ancientAdena != null && (_ancientAdena.getCount() <= 0 || _ancientAdena.getOwnerId() != getOwnerId()))
			_ancientAdena = null;
		
		return item;
	}
	
	@Override
	public L2ItemInstance dropItem(String process, int objectId, int count, L2PcInstance actor, L2Object reference)
	{
		L2ItemInstance item = super.dropItem(process, objectId, count, actor, reference);
		
		if (_adena != null && (_adena.getCount() <= 0 || _adena.getOwnerId() != getOwnerId()))
			_adena = null;
		
		if (_ancientAdena != null && (_ancientAdena.getCount() <= 0 || _ancientAdena.getOwnerId() != getOwnerId()))
			_ancientAdena = null;
		
		return item;
	}
	
	@Override
	protected boolean removeItem(L2ItemInstance item)
	{
		// Removes any reference to the item from Shortcut bar
		getOwner().removeItemFromShortCut(item.getObjectId());
		
		// Removes active Enchant Scroll
		if (item.equals(getOwner().getActiveEnchantItem()))
			getOwner().setActiveEnchantItem(null);
		
		if (item.getItemId() == ADENA_ID)
			_adena = null;
		else if (item.getItemId() == ANCIENT_ADENA_ID)
			_ancientAdena = null;
		
		return super.removeItem(item);
	}
	
	@Override
	public void refreshWeight()
	{
		super.refreshWeight();
		getOwner().refreshOverloaded();
	}
	
	@Override
	public void restore()
	{
		super.restore();
		_adena = getItemByItemId(ADENA_ID);
		_ancientAdena = getItemByItemId(ANCIENT_ADENA_ID);
	}
	
	public static int[][] restoreVisibleInventory(int objectId)
	{
		int[][] paperdoll = new int[0x12][3];
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement2 = con.prepareStatement("SELECT object_id,item_id,loc_data,enchant_level FROM items WHERE owner_id=? AND loc='PAPERDOLL'");
			statement2.setInt(1, objectId);
			ResultSet invdata = statement2.executeQuery();
			
			while (invdata.next())
			{
				int slot = invdata.getInt("loc_data");
				paperdoll[slot][0] = invdata.getInt("object_id");
				paperdoll[slot][1] = invdata.getInt("item_id");
				paperdoll[slot][2] = invdata.getInt("enchant_level");
			}
			
			invdata.close();
			statement2.close();
		}
		catch (Exception e)
		{
			_log.warning(PcInventory.class.getSimpleName() + ": could not restore inventory:");
		}
		return paperdoll;
	}
	
	public boolean validateCapacity(L2ItemInstance item)
	{
		int slots = 0;
		
		if (!(item.isStackable() && getItemByItemId(item.getItemId()) != null) && item.getItemType() != L2EtcItemType.HERB)
			slots++;
		
		return validateCapacity(slots);
	}
	
	public boolean validateCapacity(List<L2ItemInstance> items)
	{
		int slots = 0;
		
		for (L2ItemInstance item : items)
			if (!(item.isStackable() && getItemByItemId(item.getItemId()) != null))
				slots++;
		
		return validateCapacity(slots);
	}
	
	public boolean validateCapacityByItemId(int ItemId)
	{
		int slots = 0;
		
		L2ItemInstance invItem = getItemByItemId(ItemId);
		if (!(invItem != null && invItem.isStackable()))
			slots++;
		
		return validateCapacity(slots);
	}
	
	@Override
	public boolean validateCapacity(int slots)
	{
		return (_items.size() + slots <= _owner.getInventoryLimit());
	}
	
	@Override
	public boolean validateWeight(int weight)
	{
		return (_totalWeight + weight <= _owner.getMaxLoad());
	}
	
	public boolean hasAtLeastOneItem(int... itemIds)
	{
		for (int itemId : itemIds)
		{
			if (getItemByItemId(itemId) != null)
				return true;
		}
		return false;
	}
	
	public List<L2ItemInstance> getItemsByItemId(int itemId)
	{
		List<L2ItemInstance> returnList = new ArrayList<>();
		for (L2ItemInstance item : _items.values())
		{
			if (item != null && item.getItemId() == itemId)
				returnList.add(item);
		}
		return returnList;
	}
	
}