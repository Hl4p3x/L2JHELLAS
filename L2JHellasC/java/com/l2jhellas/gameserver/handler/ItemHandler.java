package com.l2jhellas.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

public class ItemHandler implements IHandler<IItemHandler, Integer>
{
	private final Map<Integer, IItemHandler> _datatable;
	
	protected ItemHandler()
	{
		_datatable = new HashMap<>();
	}
	
	@Override
	public void registerHandler(IItemHandler handler)
	{
		int[] ids = handler.getItemIds();
		
		for (int id : ids)
		{
			_datatable.put(new Integer(id), handler);
		}
	}
	
	@Override
	public void removeHandler(IItemHandler handler)
	{
		int[] ids = handler.getItemIds();
		
		for (int id : ids)
		{
			_datatable.remove(new Integer(id));
		}
	}
	
	@Override
	public IItemHandler getHandler(Integer val)
	{
		return _datatable.get(new Integer(val));
	}
	
	@Override
	public int size()
	{
		return _datatable.size();
	}
	
	public static ItemHandler getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ItemHandler _instance = new ItemHandler();
	}
}