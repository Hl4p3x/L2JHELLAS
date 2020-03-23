package com.l2jhellas.gameserver.cache;

import java.util.HashMap;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class WarehouseCache
{
	private static WarehouseCache _instance;
	protected final HashMap<L2PcInstance, Long> _cachedWh;
	protected final long _cacheTime;
	
	public static WarehouseCache getInstance()
	{
		if (_instance == null)
			_instance = new WarehouseCache();
		return _instance;
	}
	
	private WarehouseCache()
	{
		_cacheTime = Config.WAREHOUSE_CACHE_TIME * 60000L; // 60*1000 = 60000
		_cachedWh = new HashMap<>();
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new CacheScheduler(), 120000, 60000);
	}
	
	public void addCacheTask(L2PcInstance pc)
	{
		_cachedWh.put(pc, System.currentTimeMillis());
	}
	
	public void remCacheTask(L2PcInstance pc)
	{
		_cachedWh.remove(pc);
	}
	
	public class CacheScheduler implements Runnable
	{
		@Override
		public void run()
		{
			long cTime = System.currentTimeMillis();
			for (L2PcInstance pc : _cachedWh.keySet())
			{
				if (cTime - _cachedWh.get(pc) > _cacheTime)
				{
					pc.clearWarehouse();
					_cachedWh.remove(pc);
				}
			}
		}
	}
}