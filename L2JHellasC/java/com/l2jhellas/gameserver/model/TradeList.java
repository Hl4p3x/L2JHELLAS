package com.l2jhellas.gameserver.model;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.sql.ItemTable;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2Item;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jhellas.gameserver.network.serverpackets.ItemList;
import com.l2jhellas.gameserver.network.serverpackets.StatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.util.Util;

public class TradeList
{
	public class TradeItem
	{
		private int _objectId;
		private final L2Item _item;
		private int _enchant;
		private int _count;
		private int _price;
		
		public TradeItem(L2ItemInstance item, int count, int price)
		{
			_objectId = item.getObjectId();
			_item = item.getItem();
			_enchant = item.getEnchantLevel();
			_count = count;
			_price = price;
		}
		
		public TradeItem(L2Item item, int count, int price)
		{
			_objectId = 0;
			_item = item;
			_enchant = 0;
			_count = count;
			_price = price;
		}
		
		public TradeItem(L2Item item, int count, int price , int enchant)
		{
			_objectId = 0;
			_item = item;
			_enchant = enchant;
			_count = count;
			_price = price;
		}
		
		public TradeItem(TradeItem item, int count, int price)
		{
			_objectId = item.getObjectId();
			_item = item.getItem();
			_enchant = item.getEnchant();
			_count = count;
			_price = price;
		}
		
		public void setObjectId(int objectId)
		{
			_objectId = objectId;
		}
		
		public int getObjectId()
		{
			return _objectId;
		}
		
		public L2Item getItem()
		{
			return _item;
		}
		
		public void setEnchant(int enchant)
		{
			_enchant = enchant;
		}
		
		public int getEnchant()
		{
			return _enchant;
		}
		
		public void setCount(int count)
		{
			_count = count;
		}
		
		public int getCount()
		{
			return _count;
		}
		
		public void setPrice(int price)
		{
			_price = price;
		}
		
		public int getPrice()
		{
			return _price;
		}
	}
	
	private static Logger _log = Logger.getLogger(TradeList.class.getName());
	
	private final L2PcInstance _owner;
	private L2PcInstance _partner;
	private final List<TradeItem> _items;
	private String _title;
	private boolean _packaged;
	
	private boolean _confirmed = false;
	private boolean _locked = false;
	
	public TradeList(L2PcInstance owner)
	{
		_items = new ArrayList<>();
		_owner = owner;
	}
	
	public L2PcInstance getOwner()
	{
		return _owner;
	}
	
	public void setPartner(L2PcInstance partner)
	{
		_partner = partner;
	}
	
	public L2PcInstance getPartner()
	{
		return _partner;
	}
	
	public void setTitle(String title)
	{
		_title = title;
	}
	
	public String getTitle()
	{
		return _title;
	}
	
	public boolean isLocked()
	{
		return _locked;
	}
	
	public boolean isConfirmed()
	{
		return _confirmed;
	}
	
	public boolean isPackaged()
	{
		return _packaged;
	}
	
	public void setPackaged(boolean value)
	{
		_packaged = value;
	}
	
	public TradeItem[] getItems()
	{
		return _items.toArray(new TradeItem[_items.size()]);
	}
	
	public TradeList.TradeItem[] getAvailableItems(PcInventory inventory)
	{
		List<TradeList.TradeItem> list = new ArrayList<>();
		for (TradeList.TradeItem item : _items)
		{
			item = new TradeItem(item, item.getCount(), item.getPrice());
			inventory.adjustAvailableItem(item);
			list.add(item);
		}
		
		return list.toArray(new TradeList.TradeItem[list.size()]);
	}
	
	public int getItemCount()
	{
		return _items.size();
	}
	
	public TradeItem adjustAvailableItem(L2ItemInstance item)
	{
		if (item.isStackable())
		{
			for (TradeItem exclItem : _items)
			{
				if (exclItem.getItem().getItemId() == item.getItemId())
				{
					if (item.getCount() <= exclItem.getCount())
					{
						return null;
					}
					
					return new TradeItem(item, item.getCount() - exclItem.getCount(), item.getReferencePrice());
				}
			}
		}
		else
		{
			for (TradeItem exclItem : _items)
			{
				if (item.getObjectId() == exclItem.getObjectId())
				{
					return null;
				}
			}
		}
		return new TradeItem(item, item.getCount(), item.getReferencePrice());
	}
	
	public void adjustItemRequest(ItemRequest item)
	{
		for (TradeItem filtItem : _items)
			if (filtItem.getObjectId() == item.getObjectId())
			{
				if (filtItem.getCount() < item.getCount())
					item.setCount(filtItem.getCount());
				return;
			}
		item.setCount(0);
	}
	
	public void adjustItemRequestByItemId(ItemRequest item)
	{
		for (TradeItem filtItem : _items)
			if (filtItem.getItem().getItemId() == item.getItemId())
			{
				if (filtItem.getCount() < item.getCount())
					item.setCount(filtItem.getCount());
				return;
			}
		item.setCount(0);
	}
	
	public synchronized TradeItem addItem(int objectId, int count)
	{
		return addItem(objectId, count, 0);
	}
	
	public synchronized TradeItem addItem(int objectId, int count, int price)
	{
		if (isLocked())
		{
			_log.warning(TradeList.class.getName() + ": " + _owner.getName() + ": Attempt to modify locked TradeList!");
			return null;
		}
		
		L2ItemInstance item = _owner.getInventory().getItemByObjectId(objectId);
		
		if (item == null)
		{
			_log.warning(TradeList.class.getName() + ": " + _owner.getName() + ": Attempt to add invalid item to TradeList!");
			return null;
		}
		
		int _count = _owner.getInventory().getInventoryItemCount(item.getItemId(),item.getEnchantLevel());

		
		if (!item.isTradeable() || item.isQuestItem())
			return null;
		
		if (count <= 0 || count > item.getCount() || count > _count)
			return null;
		
		if (!item.isStackable() && count > 1)
			return null;
		
		if ((Integer.MAX_VALUE / count) < price)
			return null;
		
		for (TradeItem checkitem : _items)
		{
			if (checkitem.getObjectId() == objectId)
				return null;
		}
		
		TradeItem titem = new TradeItem(item, count, price);
		_items.add(titem);
		
		// If Player has already confirmed this trade, invalidate the confirmation
		invalidateConfirmation();
		return titem;
	}
	
	public synchronized TradeItem addItemByItemId(int itemId, int count, int price, int enchant)
	{
		if (isLocked())
		{
			_log.warning(TradeList.class.getName() + ": " + _owner.getName() + ": Attempt to modify locked TradeList!");
			return null;
		}
		
		L2Item item = ItemTable.getInstance().getTemplate(itemId);
		if (item == null)
		{
			_log.warning(TradeList.class.getName() + ": " + _owner.getName() + ": Attempt to add invalid item to TradeList!");
			return null;
		}
		
		if (!item.isTradeable() || item.isQuestItem())
			return null;
		
		if (!item.isStackable() && count > 1)
		{
			_log.warning(TradeList.class.getName() + ": " + _owner.getName() + ": Attempt to add non-stackable item to TradeList with count > 1!");
			return null;
		}
		
		if ((Integer.MAX_VALUE / count) < price)
			return null;
		
		TradeItem titem = new TradeItem(item, count, price,enchant);
		
		_items.add(titem);
		
		// If Player has already confirmed this trade, invalidate the confirmation
		invalidateConfirmation();
		return titem;
	}
	
	public synchronized TradeItem removeItem(int objectId, int itemId, int count)
	{
		if (isLocked())
		{
			_log.warning(TradeList.class.getName() + ": " + _owner.getName() + ": Attempt to modify locked TradeList!");
			return null;
		}
		
		for (TradeItem titem : _items)
			if (titem.getObjectId() == objectId || titem.getItem().getItemId() == itemId)
			{
				// If Partner has already confirmed this trade, invalidate the confirmation
				if (_partner != null)
				{
					TradeList partnerList = _partner.getActiveTradeList();
					if (partnerList == null)
					{
						_log.warning(TradeList.class.getName() + ": " + _partner.getName() + ": Trading partner (" + _partner.getName() + ") is invalid in this trade!");
						return null;
					}
					partnerList.invalidateConfirmation();
				}
				
				// Reduce item count or complete item
				if (count != -1 && titem.getCount() > count)
					titem.setCount(titem.getCount() - count);
				else
					_items.remove(titem);
				
				return titem;
			}
		return null;
	}
	
	public synchronized void updateItems()
	{
		for (TradeItem titem : _items)
		{
			L2ItemInstance item = _owner.getInventory().getItemByObjectId(titem.getObjectId());
			if (item == null || titem.getCount() < 1)
				removeItem(titem.getObjectId(), -1, -1);
			else if (item.getCount() < titem.getCount())
				titem.setCount(item.getCount());
		}
	}
	
	public void lock()
	{
		_locked = true;
	}
	
	public void clear()
	{
		_items.clear();
		_locked = false;
	}
	
	public boolean confirm()
	{
		if (_confirmed)
			return true; // Already confirmed
			
		// If Partner has already confirmed this trade, proceed exchange
		if (_partner != null)
		{
			TradeList partnerList = _partner.getActiveTradeList();
			if (partnerList == null)
			{
				_log.warning(TradeList.class.getName() + ": " + _partner.getName() + ": Trading partner (" + _partner.getName() + ") is invalid in this trade!");
				return false;
			}
			
			// Synchronization order to avoid deadlock
			TradeList sync1, sync2;
			if (getOwner().getObjectId() > partnerList.getOwner().getObjectId())
			{
				sync1 = partnerList;
				sync2 = this;
			}
			else
			{
				sync1 = this;
				sync2 = partnerList;
			}
			
			synchronized (sync1)
			{
				synchronized (sync2)
				{
					_confirmed = true;
					if (partnerList.isConfirmed())
					{
						partnerList.lock();
						lock();
						if (!partnerList.validate())
							return false;
						if (!validate())
							return false;
						
						doExchange(partnerList);
					}
					else
						_partner.onTradeConfirm(_owner);
				}
			}
		}
		else
			_confirmed = true;
		
		return _confirmed;
	}
	
	public void invalidateConfirmation()
	{
		_confirmed = false;
	}
	
	private boolean validate()
	{
		// Check for Owner validity
		if (_owner == null || L2World.getInstance().findObject(_owner.getObjectId()) == null)
		{
			_log.warning(TradeList.class.getName() + ": Invalid owner of TradeList");
			return false;
		}
		
		// Check for Item validity
		for (TradeItem titem : _items)
		{
			L2ItemInstance item = _owner.checkItemManipulation(titem.getObjectId(), titem.getCount(), "transfer");
			if (item == null || titem.getCount() < 1)
			{
				_log.warning(TradeList.class.getName() + ": " + _owner.getName() + ": Invalid Item in TradeList");
				return false;
			}
		}
		
		return true;
	}
	
	private boolean TransferItems(L2PcInstance partner, InventoryUpdate ownerIU, InventoryUpdate partnerIU)
	{
		for (TradeItem titem : _items)
		{
			L2ItemInstance oldItem = _owner.getInventory().getItemByObjectId(titem.getObjectId());
			if (oldItem == null)
				return false;
			L2ItemInstance newItem = _owner.getInventory().transferItem("Trade", titem.getObjectId(), titem.getCount(), partner.getInventory(), _owner, _partner);
			if (newItem == null)
				return false;
			
			// Add changes to inventory update packets
			if (ownerIU != null)
			{
				if (oldItem.getCount() > 0 && oldItem != newItem)
					ownerIU.addModifiedItem(oldItem);
				else
					ownerIU.addRemovedItem(oldItem);
			}
			
			if (partnerIU != null)
			{
				if (newItem.getCount() > titem.getCount())
					partnerIU.addModifiedItem(newItem);
				else
					partnerIU.addNewItem(newItem);
			}
		}
		return true;
	}
	
	public int countItemsSlots(L2PcInstance partner)
	{
		int slots = 0;
		
		for (TradeItem item : _items)
		{
			if (item == null)
				continue;
			L2Item template = ItemTable.getInstance().getTemplate(item.getItem().getItemId());
			if (template == null)
				continue;
			if (!template.isStackable())
				slots += item.getCount();
			else if (partner.getInventory().getItemByItemId(item.getItem().getItemId()) == null)
				slots++;
		}
		
		return slots;
	}
	
	public int calcItemsWeight()
	{
		int weight = 0;
		
		for (TradeItem item : _items)
		{
			if (item == null)
				continue;
			L2Item template = ItemTable.getInstance().getTemplate(item.getItem().getItemId());
			if (template == null)
				continue;
			weight += item.getCount() * template.getWeight();
		}
		
		return weight;
	}
	
	private void doExchange(TradeList partnerList)
	{
		boolean success = false;
		// check weight and slots
		if ((!getOwner().getInventory().validateWeight(partnerList.calcItemsWeight())) || !(partnerList.getOwner().getInventory().validateWeight(calcItemsWeight())))
		{
			partnerList.getOwner().sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
			getOwner().sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
		}
		else if ((!getOwner().getInventory().validateCapacity(partnerList.countItemsSlots(getOwner()))) || (!partnerList.getOwner().getInventory().validateCapacity(countItemsSlots(partnerList.getOwner()))))
		{
			partnerList.getOwner().sendPacket(SystemMessageId.SLOTS_FULL);
			getOwner().sendPacket(SystemMessageId.SLOTS_FULL);
		}
		else
		{
			// Prepare inventory update packet
			InventoryUpdate ownerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
			InventoryUpdate partnerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
			
			// Transfer items
			partnerList.TransferItems(getOwner(), partnerIU, ownerIU);
			TransferItems(partnerList.getOwner(), ownerIU, partnerIU);
			
			// Send inventory update packet
			if (ownerIU != null)
				_owner.sendPacket(ownerIU);
			else
				_owner.sendPacket(new ItemList(_owner, false));
			
			if (partnerIU != null)
				_partner.sendPacket(partnerIU);
			else
				_partner.sendPacket(new ItemList(_partner, false));
			
			// Update current load as well
			StatusUpdate playerSU = new StatusUpdate(_owner.getObjectId());
			playerSU.addAttribute(StatusUpdate.CUR_LOAD, _owner.getCurrentLoad());
			_owner.sendPacket(playerSU);
			playerSU = new StatusUpdate(_partner.getObjectId());
			playerSU.addAttribute(StatusUpdate.CUR_LOAD, _partner.getCurrentLoad());
			_partner.sendPacket(playerSU);
			
			success = true;
		}
		// Finish the trade
		partnerList.getOwner().onTradeFinish(success);
		getOwner().onTradeFinish(success);
	}
	
	public synchronized boolean PrivateStoreBuy(L2PcInstance player, ItemRequest[] items, int price)
	{
		if (_locked)
			return false;
		if (!validate())
		{
			lock();
			return false;
		}
		
		int slots = 0;
		int weight = 0;
		
		for (ItemRequest item : items)
		{
			if (item == null)
				continue;
			L2Item template = ItemTable.getInstance().getTemplate(item.getItemId());
			if (template == null)
				continue;
			weight += item.getCount() * template.getWeight();
			if (!template.isStackable())
				slots += item.getCount();
			else if (player.getInventory().getItemByItemId(item.getItemId()) == null)
				slots++;
		}
		
		if (!player.getInventory().validateWeight(weight))
		{
			player.sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
			return false;
		}
		
		if (!player.getInventory().validateCapacity(slots))
		{
			player.sendPacket(SystemMessageId.SLOTS_FULL);
			return false;
		}
		
		PcInventory ownerInventory = _owner.getInventory();
		PcInventory playerInventory = player.getInventory();
		
		// Prepare inventory update packets
		InventoryUpdate ownerIU = new InventoryUpdate();
		InventoryUpdate playerIU = new InventoryUpdate();
		
		// Transfer adena
		if (price > playerInventory.getAdena())
		{
			lock();
			return false;
		}
		L2ItemInstance adenaItem = playerInventory.getAdenaInstance();
		playerInventory.reduceAdena("PrivateStore", price, player, _owner);
		playerIU.addItem(adenaItem);
		ownerInventory.addAdena("PrivateStore", price, _owner, player);
		ownerIU.addItem(ownerInventory.getAdenaInstance());
		
		// Transfer items
		for (ItemRequest item : items)
		{
			// Check if requested item is sill on the list and adjust its count
			adjustItemRequest(item);
			if (item.getCount() == 0)
				continue;
			
			// Check if requested item is available for manipulation
			L2ItemInstance oldItem = _owner.checkItemManipulation(item.getObjectId(), item.getCount(), "sell");
			if (oldItem == null)
			{
				lock();
				return false;
			}
			
			// Proceed with item transfer
			L2ItemInstance newItem = ownerInventory.transferItem("PrivateStore", item.getObjectId(), item.getCount(), playerInventory, _owner, player);
			if (newItem == null)
				return false;
			removeItem(item.getObjectId(), -1, item.getCount());
			
			// Add changes to inventory update packets
			if (oldItem.getCount() > 0 && oldItem != newItem)
				ownerIU.addModifiedItem(oldItem);
			else
				ownerIU.addRemovedItem(oldItem);
			if (newItem.getCount() > item.getCount())
				playerIU.addModifiedItem(newItem);
			else
				playerIU.addNewItem(newItem);
			
			// Send messages about the transaction to both players
			if (newItem.isStackable())
			{
				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.S1_PURCHASED_S3_S2_S);
				msg.addString(player.getName());
				msg.addItemName(newItem.getItemId());
				msg.addNumber(item.getCount());
				_owner.sendPacket(msg);
				
				msg = SystemMessage.getSystemMessage(SystemMessageId.PURCHASED_S3_S2_S_FROM_S1);
				msg.addString(_owner.getName());
				msg.addItemName(newItem.getItemId());
				msg.addNumber(item.getCount());
				player.sendPacket(msg);
			}
			else
			{
				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.S1_PURCHASED_S2);
				msg.addString(player.getName());
				msg.addItemName(newItem.getItemId());
				_owner.sendPacket(msg);
				
				msg = SystemMessage.getSystemMessage(SystemMessageId.PURCHASED_S2_FROM_S1);
				msg.addString(_owner.getName());
				msg.addItemName(newItem.getItemId());
				player.sendPacket(msg);
			}
		}
		
		// Send inventory update packet
		_owner.sendPacket(ownerIU);
		player.sendPacket(playerIU);
		return true;
	}
	
	public synchronized boolean PrivateStoreSell(L2PcInstance player, ItemRequest[] items, int price)
	{
		if (_locked)
			return false;
		
		PcInventory ownerInventory = _owner.getInventory();
		PcInventory playerInventory = player.getInventory();
		
		// we must check item are available before begining transaction,
		// also check if augmented items are traded. If so, cancel it...
		for (ItemRequest item : items)
		{
			// Check if requested item is available for manipulation
			L2ItemInstance oldItem = player.checkItemManipulation(item.getObjectId(), item.getCount(), "sell");
			if (oldItem == null)
				return false;
			if (oldItem.getAugmentation() != null)
			{
				String msg = "Transaction failed. Augmented items may not be exchanged.";
				_owner.sendMessage(msg);
				player.sendMessage(msg);
				return false;
			}
		}
		
		// Prepare inventory update packet
		InventoryUpdate ownerIU = new InventoryUpdate();
		InventoryUpdate playerIU = new InventoryUpdate();
		
		// Transfer items
		for (ItemRequest item : items)
		{
			// Check if requested item is sill on the list and adjust its count
			adjustItemRequestByItemId(item);
			if (item.getCount() == 0)
				continue;
			
			// Check if requested item is available for manipulation
			L2ItemInstance oldItem = player.checkItemManipulation(item.getObjectId(), item.getCount(), "sell");
			if (oldItem == null)
				return false;
			
			if (oldItem.getItemId() != item.getItemId())
			{
				String msg = "cheating is not allowed.";
				_owner.sendMessage(msg);
				player.sendMessage(msg);
				Util.handleIllegalPlayerAction(player, player + " cheating with sell items", Config.DEFAULT_PUNISH);
				return false;
			}
			
			// Proceed with item transfer
			L2ItemInstance newItem = playerInventory.transferItem("PrivateStore", item.getObjectId(), item.getCount(), ownerInventory, player, _owner);
			if (newItem == null)
				return false;
			
			removeItem(-1, item.getItemId(), item.getCount());
			
			// Add changes to inventory update packets
			if (oldItem.getCount() > 0 && oldItem != newItem)
				playerIU.addModifiedItem(oldItem);
			else
				playerIU.addRemovedItem(oldItem);
			if (newItem.getCount() > item.getCount())
				ownerIU.addModifiedItem(newItem);
			else
				ownerIU.addNewItem(newItem);
			
			// Send messages about the transaction to both players
			if (newItem.isStackable())
			{
				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.PURCHASED_S3_S2_S_FROM_S1);
				msg.addString(player.getName());
				msg.addItemName(newItem.getItemId());
				msg.addNumber(item.getCount());
				_owner.sendPacket(msg);
				
				msg = SystemMessage.getSystemMessage(SystemMessageId.S1_PURCHASED_S3_S2_S);
				msg.addString(_owner.getName());
				msg.addItemName(newItem.getItemId());
				msg.addNumber(item.getCount());
				player.sendPacket(msg);
			}
			else
			{
				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.PURCHASED_S2_FROM_S1);
				msg.addString(player.getName());
				msg.addItemName(newItem.getItemId());
				_owner.sendPacket(msg);
				
				msg = SystemMessage.getSystemMessage(SystemMessageId.S1_PURCHASED_S2);
				msg.addString(_owner.getName());
				msg.addItemName(newItem.getItemId());
				player.sendPacket(msg);
			}
		}
		
		// Transfer adena
		if (price > ownerInventory.getAdena())
			return false;
		L2ItemInstance adenaItem = ownerInventory.getAdenaInstance();
		ownerInventory.reduceAdena("PrivateStore", price, _owner, player);
		ownerIU.addItem(adenaItem);
		playerInventory.addAdena("PrivateStore", price, player, _owner);
		playerIU.addItem(playerInventory.getAdenaInstance());
		
		// Send inventory update packet
		_owner.sendPacket(ownerIU);
		player.sendPacket(playerIU);
		return true;
	}
	
	public TradeItem getItem(int objectId)
	{
		for (TradeItem item : _items)
		{
			if (item.getObjectId() == objectId)
				return item;
		}
		return null;
	}
}