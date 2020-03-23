package com.l2jhellas.gameserver.cache;

import java.util.HashMap;
import java.util.List;

import com.l2jhellas.gameserver.model.L2DropData;

public class InfoCache
{
	private static final HashMap<Integer, List<L2DropData>> _droplistCache = new HashMap<>();
	
	public static void addToDroplistCache(final int id, final List<L2DropData> list)
	{
		_droplistCache.put(id, list);
	}
	
	public static List<L2DropData> getFromDroplistCache(final int id)
	{
		return _droplistCache.get(id);
	}
	
	public static void unload()
	{
		_droplistCache.clear();
	}
}