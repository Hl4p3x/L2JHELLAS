package com.l2jhellas.gameserver;

import java.util.ArrayList;
import java.util.List;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;

public class ItemsAutoDestroy
{
	protected List<L2ItemInstance> _items = new ArrayList<>();
	
	protected ItemsAutoDestroy()
	{
		
	}
	
	public void CheckItemsForDestroy()
	{
		if (Config.AUTODESTROY_ITEM_AFTER > 0)
			ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(this::removeItems,Config.AUTODESTROY_ITEM_AFTER,Config.AUTODESTROY_ITEM_AFTER);
	}
	
	public synchronized void addItem(L2ItemInstance item)
	{
		item.setDropTime(System.currentTimeMillis());
		_items.add(item);
	}
	
	public synchronized void removeItems()
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