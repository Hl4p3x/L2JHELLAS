package com.l2jhellas.gameserver.model.actor.item;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.controllers.GameTimeController;
import com.l2jhellas.gameserver.datatables.sql.ItemTable;
import com.l2jhellas.gameserver.enums.items.ItemLocation;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.util.database.L2DatabaseFactory;

public abstract class ItemContainer
{
	protected static final Logger _log = Logger.getLogger(ItemContainer.class.getName());

	protected final Map<Integer, L2ItemInstance> _items = new ConcurrentHashMap<>();
	
	protected ItemContainer()
	{
		
	}
	
	public abstract L2Character getOwner();
	
	protected abstract ItemLocation getBaseLocation();
	
	public int getOwnerId()
	{
		return getOwner() == null ? 0 : getOwner().getObjectId();
	}
	
	public int getSize()
	{
		return _items.size();
	}

	public Collection<L2ItemInstance> getItems()
	{
		return getItems(Objects::nonNull);
	}

	@SafeVarargs
	public final Collection<L2ItemInstance> getItems(Predicate<L2ItemInstance> filter, Predicate<L2ItemInstance>... filters)
	{
		for (Predicate<L2ItemInstance> additionalFilter : filters)
		{
			filter = filter.and(additionalFilter);
		}
		return _items.values().stream().filter(filter).collect(Collectors.toCollection(LinkedList::new));
	}
	
	public L2ItemInstance getItemByItemId(int itemId)
	{
		return _items.values().stream().filter(item -> item.getItemId() == itemId).findFirst().orElse(null);
	}

	public L2ItemInstance getItemByObjectId(int objectId)
	{
		return _items.get(objectId);
	}
	
	public int getInventoryItemCount(int itemId, int enchantLevel)
	{
		int count = 0;
		
		for (L2ItemInstance item : _items.values())
		{
			if (item.getItemId() == itemId && ((item.getEnchantLevel() == enchantLevel) || (enchantLevel < 0)))
			{
				if (item.isStackable())
					return item.getCount();
		
					count++;
			}
		}
		
		return count;
	}
			
	public L2ItemInstance addItem(String process, L2ItemInstance item, L2PcInstance actor, L2Object reference)
	{
		L2ItemInstance olditem = getItemByItemId(item.getItemId());
		
		// If stackable item is found in inventory just add to current quantity
		if (olditem != null && olditem.isStackable())
		{
			int count = item.getCount();
			olditem.changeCount(process, count, actor, reference);
			olditem.setLastChange(L2ItemInstance.MODIFIED);
			
			// And destroys the item
			ItemTable.getInstance().destroyItem(process, item, actor, reference);
			item.updateDatabase();
			item = olditem;
			
			// Updates database
			if (item.getItemId() == 57 && count < 10000 * Config.RATE_DROP_ADENA)
			{
				// Small adena changes won't be saved to database all the time
				if (GameTimeController.getInstance().getGameTicks() % 5 == 0)
					item.updateDatabase();
			}
			else
				item.updateDatabase();
		}
		// If item hasn't be found in inventory, create new one
		else
		{
			item.setOwnerId(process, getOwnerId(), actor, reference);
			item.setLocation(getBaseLocation());
			item.setLastChange((L2ItemInstance.ADDED));
			
			// Add item in inventory
			addItem(item);
			
			// Updates database
			item.updateDatabase();
		}
		
		refreshWeight();
		return item;
	}
	
	public L2ItemInstance addItem(String process, int itemId, int count, L2PcInstance actor, L2Object reference)
	{
		L2ItemInstance item = getItemByItemId(itemId);
		
		// If stackable item is found in inventory just add to current quantity
		if (item != null && item.isStackable())
		{
			item.changeCount(process, count, actor, reference);
			item.setLastChange(L2ItemInstance.MODIFIED);
			// Updates database
			if (itemId == 57 && count < 10000 * Config.RATE_DROP_ADENA)
			{
				// Small adena changes won't be saved to database all the time
				if (GameTimeController.getInstance().getGameTicks() % 5 == 0)
					item.updateDatabase();
			}
			else
				item.updateDatabase();
		}
		// If item hasn't be found in inventory, create new one
		else
		{
			for (int i = 0; i < count; i++)
			{
				L2Item template = ItemTable.getInstance().getTemplate(itemId);
				if (template == null)
				{
					_log.warning(ItemContainer.class.getName() + ": " + (actor != null ? "[" + actor.getName() + "] " : "") + "Invalid ItemId requested: " + itemId);
					return null;
				}
				
				item = ItemTable.getInstance().createItem(process, itemId, template.isStackable() ? count : 1, actor, reference);
				item.setOwnerId(getOwnerId());
				item.setLocation(getBaseLocation());
				item.setLastChange(L2ItemInstance.ADDED);
				
				// Add item in inventory
				addItem(item);
				// Updates database
				item.updateDatabase();
				
				// If stackable, end loop as entire count is included in 1 instance of item
				if (template.isStackable() || !Config.MULTIPLE_ITEM_DROP)
					break;
			}
		}
		
		refreshWeight();
		return item;
	}
	
	public L2ItemInstance addWearItem(String process, int itemId, L2PcInstance actor, L2Object reference)
	{
		// Surch the item in the inventory of the player
		L2ItemInstance item = getItemByItemId(itemId);
		
		// There is such item already in inventory
		if (item != null)
			return item;
		
		// Create and Init the L2ItemInstance corresponding to the Item Identifier and quantity
		// Add the L2ItemInstance object to _allObjects of L2world
		item = ItemTable.getInstance().createItem(process, itemId, 1, actor, reference);
		
		// Set Item Properties
		item.setWear(true); // "Try On" Item -> Don't save it in database
		item.setOwnerId(getOwnerId());
		item.setLocation(getBaseLocation());
		item.setLastChange((L2ItemInstance.ADDED));
		
		// Add item in inventory and equip it if necessary (item location defined)
		addItem(item);
		
		// Calculate the weight loaded by player
		refreshWeight();
		
		return item;
	}
	
	public L2ItemInstance transferItem(String process, int objectId, int count, ItemContainer target, L2PcInstance actor, L2Object reference)
	{
		if (target == null)
		{
			return null;
		}
		
		L2ItemInstance sourceitem = getItemByObjectId(objectId);
		if (sourceitem == null)
		{
			return null;
		}
		L2ItemInstance targetitem = sourceitem.isStackable() ? target.getItemByItemId(sourceitem.getItemId()) : null;
		
		synchronized (sourceitem)
		{
			// check if this item still present in this container
			if (getItemByObjectId(objectId) != sourceitem)
			{
				return null;
			}
			
			// Check if requested quantity is available
			if (count > sourceitem.getCount())
				count = sourceitem.getCount();
			
			// If possible, move entire item object
			if (sourceitem.getCount() == count && targetitem == null)
			{
				removeItem(sourceitem);
				target.addItem(process, sourceitem, actor, reference);
				targetitem = sourceitem;
			}
			else
			{
				if (sourceitem.getCount() > count) // If possible, only update counts
				{
					sourceitem.changeCount(process, -count, actor, reference);
				}
				else
				// Otherwise destroy old item
				{
					removeItem(sourceitem);
					ItemTable.getInstance().destroyItem(process, sourceitem, actor, reference);
				}
				
				if (targetitem != null) // If possible, only update counts
				{
					targetitem.changeCount(process, count, actor, reference);
				}
				else
				// Otherwise add new item
				{
					targetitem = target.addItem(process, sourceitem.getItemId(), count, actor, reference);
				}
			}
			
			// Updates database
			sourceitem.updateDatabase();
			if (targetitem != sourceitem && targetitem != null)
				targetitem.updateDatabase();
			if (sourceitem.isAugmented())
				sourceitem.getAugmentation().removeBoni(actor);
			refreshWeight();
		}
		return targetitem;
	}
	
	public L2ItemInstance destroyItem(String process, L2ItemInstance item, L2PcInstance actor, L2Object reference)
	{
		synchronized (item)
		{
			if (!_items.containsKey(item.getObjectId()))
				return null;
			
			removeItem(item);
			ItemTable.getInstance().destroyItem(process, item, actor, reference);
			
			item.updateDatabase();
			refreshWeight();
		}
		return item;
	}
	
	public L2ItemInstance destroyItem(String process, int objectId, int count, L2PcInstance actor, L2Object reference)
	{
		L2ItemInstance item = getItemByObjectId(objectId);
		if (item == null)
			return null;
		
		// Adjust item quantity
		if (item.getCount() > count)
		{
			synchronized (item)
			{
				item.changeCount(process, -count, actor, reference);
				item.setLastChange(L2ItemInstance.MODIFIED);
				
				item.updateDatabase();
				refreshWeight();
			}
			return item;
		}
		// Directly drop entire item
		return destroyItem(process, item, actor, reference);
	}
	
	public L2ItemInstance destroyItemByItemId(String process, int itemId, int count, L2PcInstance actor, L2Object reference)
	{
		L2ItemInstance item = getItemByItemId(itemId);
		if (item == null)
			return null;
		
		synchronized (item)
		{
			// Adjust item quantity
			if (item.getCount() > count)
			{
				item.changeCount(process, -count, actor, reference);
				item.setLastChange(L2ItemInstance.MODIFIED);
			}
			// Directly drop entire item
			else
				return destroyItem(process, item, actor, reference);
			
			item.updateDatabase();
			refreshWeight();
		}
		return item;
	}
	
	public synchronized void destroyAllItems(String process, L2PcInstance actor, L2Object reference)
	{
		for (L2ItemInstance item : _items.values())
			destroyItem(process, item, actor, reference);
	}
	
	public int getAdena()
	{
		int count = 0;
		
		for (L2ItemInstance item : _items.values())
			if (item.getItemId() == 57)
			{
				count = item.getCount();
				return count;
			}
		
		return count;
	}
	
	protected void addItem(L2ItemInstance item)
	{
		_items.put(item.getObjectId(), item);
	}
	
	protected boolean removeItem(L2ItemInstance item)
	{
		return _items.remove(item.getObjectId()) != null;
	}
	
	protected void refreshWeight()
	{
	}
	
	public void deleteMe()
	{		
		if (getOwner() != null)
		{
			for (L2ItemInstance item : _items.values())
			{
				if (item != null)
				{
					item.updateDatabase();
					L2World.getInstance().removeObject(item);
				}
			}
		}
		_items.clear();
	}
	
	public void updateDatabase()
	{
		if (getOwner() != null)
		{
			for (L2ItemInstance item : _items.values())
			{
				if (item != null)
					item.updateDatabase();
			}
		}
	}
	
	public void restore()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT object_id FROM items WHERE owner_id=? AND (loc=?) " + "ORDER BY object_id DESC");
			statement.setInt(1, getOwnerId());
			statement.setString(2, getBaseLocation().name());
			ResultSet inv = statement.executeQuery();
			
			L2ItemInstance item;
			while (inv.next())
			{
				int objectId = inv.getInt(1);
				item = L2ItemInstance.restoreFromDb(objectId);
				if (item == null)
					continue;
				
				L2World.getInstance().storeObject(item);
				
				// If stackable item is found in inventory just add to current quantity
				if (item.isStackable() && getItemByItemId(item.getItemId()) != null)
					addItem("Restore", item, null, getOwner());
				else
					addItem(item);
			}
			
			inv.close();
			statement.close();
			refreshWeight();
		}
		catch (SQLException e)
		{
			_log.warning(ItemContainer.class.getName() + ": could not restore container");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public boolean validateCapacity(int slots)
	{
		return true;
	}
	
	public boolean validateWeight(int weight)
	{
		return true;
	}
}