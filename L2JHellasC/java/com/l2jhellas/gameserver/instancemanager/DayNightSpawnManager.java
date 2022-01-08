package com.l2jhellas.gameserver.instancemanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.l2jhellas.gameserver.controllers.GameTimeController;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2RaidBossInstance;
import com.l2jhellas.gameserver.model.spawn.L2Spawn;

public class DayNightSpawnManager
{
	private static Logger _log = Logger.getLogger(DayNightSpawnManager.class.getName());
	
	private final List<L2Spawn> _dayCreatures = new ArrayList<>();
	private final List<L2Spawn> _nightCreatures = new ArrayList<>();
	private final Map<L2Spawn, L2RaidBossInstance> _bosses = new ConcurrentHashMap<>();
	
	public static DayNightSpawnManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected DayNightSpawnManager()
	{
		
	}
	
	public void addDayCreature(L2Spawn spawnDat)
	{
		_dayCreatures.add(spawnDat);
	}
	
	public void addNightCreature(L2Spawn spawnDat)
	{
		_nightCreatures.add(spawnDat);
	}
	
	public void spawnCreatures(boolean isNight)
	{
		final List<L2Spawn> creaturesToUnspawn = (isNight) ? _dayCreatures : _nightCreatures;
		final List<L2Spawn> creaturesToSpawn = (isNight) ? _nightCreatures : _dayCreatures;
		
		if(creaturesToUnspawn.isEmpty() && creaturesToSpawn.isEmpty())
			return;
		
		for (L2Spawn Unspawn : creaturesToUnspawn)
		{
			Unspawn.stopRespawn();

			final L2Npc last = Unspawn.getLastSpawn();
			
			if (last != null)
				last.deleteMe();
		}
		
		for (L2Spawn spawn : creaturesToSpawn)
		{
			spawn.startRespawn();
			spawn.doSpawn();
		}

		_log.info(DayNightSpawnManager.class.getSimpleName() + ": Spawned " +  ((isNight) ? "night" : "day") + " creatures");
	}

	public void notifyChangeMode()
	{	
		spawnCreatures(GameTimeController.getInstance().isNight());	
		specialNightBoss(GameTimeController.getInstance().isNight()?1:0);
	}
	
	public void cleanUp()
	{
		_nightCreatures.clear();
		_dayCreatures.clear();
		_bosses.clear();
	}
	
	private void specialNightBoss(int mode)
	{
		try
		{
			for (Map.Entry<L2Spawn, L2RaidBossInstance> infoEntry : _bosses.entrySet())
			{
				L2RaidBossInstance boss = infoEntry.getValue();
				if (boss == null)
				{
					if (mode == 1)
					{
						final L2Spawn spawn = infoEntry.getKey();
						
						boss = (L2RaidBossInstance) spawn.doSpawn();
						RaidBossSpawnManager.getInstance().notifySpawnNightBoss(boss);
						
						_bosses.put(spawn, boss);
					}
					continue;
				}
				
				if (boss.getNpcId() == 25328 && boss.getRaidStatus().equals(RaidBossSpawnManager.StatusEnum.ALIVE))
					handleHellmans(boss, mode);
				
				return;
			}
		}
		catch (Exception e)
		{
			_log.info(DayNightSpawnManager.class.getSimpleName() + " Error while specialNightBoss(): " + e.getMessage());
		}
	}
	
	private static void handleHellmans(L2RaidBossInstance boss, int mode)
	{
		switch (mode)
		{
			case 0:
				boss.deleteMe();
				_log.info(DayNightSpawnManager.class.getSimpleName() + ": Deleting Hellman Raidboss!");
				break;
			case 1:
				boss.spawnMe();
				_log.info(DayNightSpawnManager.class.getSimpleName() + ": Spawning Hellman Raidboss!");
				break;
		}
	}
	
	public L2RaidBossInstance handleBoss(L2Spawn spawnDat)
	{
		if (_bosses.containsKey(spawnDat))
			return _bosses.get(spawnDat);
		
		if (GameTimeController.getInstance().isNight())
		{
			L2RaidBossInstance raidboss = (L2RaidBossInstance) spawnDat.doSpawn();
			_bosses.put(spawnDat, raidboss);
			
			return raidboss;
		}
		
		_bosses.put(spawnDat, null);
		
		return null;
	}
	
	private static class SingletonHolder
	{
		protected static final DayNightSpawnManager _instance = new DayNightSpawnManager();
	}
}