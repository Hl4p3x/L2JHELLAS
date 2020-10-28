package com.l2jhellas.gameserver;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;

public class ItemsAutoDestroy
{
	private final Set<L2ItemInstance> _items = ConcurrentHashMap.newKeySet();

	protected ItemsAutoDestroy()
	{
		
	}
	
	public void CheckItemsForDestroy()
	{
		if (Config.AUTODESTROY_ITEM_AFTER > 0)
			ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(this::removeItems,Config.AUTODESTROY_ITEM_AFTER,Config.AUTODESTROY_ITEM_AFTER);
	}
	
	public void addItem(L2ItemInstance item)
	{
		item.setDropTime(System.currentTimeMillis());
		_items.add(item);
	}
	
	public void removeItems()
	{
		if (_items.isEmpty())
			return;
		
		_items.removeIf(item -> item.canBeRemoved());
	}	
	
	public static ItemsAutoDestroy getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ItemsAutoDestroy _instance = new ItemsAutoDestroy();
	}
}