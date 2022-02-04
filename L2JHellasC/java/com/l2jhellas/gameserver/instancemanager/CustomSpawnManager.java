package com.l2jhellas.gameserver.instancemanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.spawn.L2Spawn;

/**
 * @author AbsolutePower
 */
public class CustomSpawnManager
{	
	private final List<L2Spawn> _spawns = new ArrayList<>();
	
	public static CustomSpawnManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected CustomSpawnManager()
	{
		
	}

	public void spawnByEventName(String name)
	{
		getSpawnsByEventName(name).forEach(sp ->
		{
            sp.startRespawn();
            sp.doSpawn();
		});
	}
	
	public void despawnByEventName(String name)
	{
		getSpawnsByEventName(name).forEach(sp ->
		{
            sp.stopRespawn();
            
			final L2Npc lsp = sp.getLastSpawn();

			if (lsp != null)
				lsp.deleteMe();
		});
	}
	
	public void spawnByLocationName(String name)
	{
		getSpawnsByLocationName(name).forEach(sp ->
		{
            sp.startRespawn();
            sp.doSpawn();
		});
	}
	
	public void despawnByLocationtName(String name)
	{
		getSpawnsByLocationName(name).forEach(sp ->
		{
            sp.stopRespawn();
            
			final L2Npc lsp = sp.getLastSpawn();

			if (lsp != null)
				lsp.deleteMe();
		});
	}

	public void addSpawn(L2Spawn spawnDat)
	{
		getSpawns().add(spawnDat);
	}
	
	public  List<L2Spawn> getSpawnsByEventName(String Evtname)
	{
		return getSpawns().stream().filter(Objects::nonNull).filter(L2Spawn :: EvtNameIsNotBlank).filter(s -> String.valueOf(s.getEventName()).equalsIgnoreCase(Evtname)).collect(Collectors.toList());
	}
	
	public  List<L2Spawn> getSpawnsByLocationName(String Locname)
	{
		return getSpawns().stream().filter(Objects::nonNull).filter(L2Spawn :: LocNameIsNotBlank).filter(s -> String.valueOf(s.getLocationName()).equalsIgnoreCase(Locname)).collect(Collectors.toList());
	}
	
	public  List<L2Spawn> getSpawnsByTerritoryName(String Terrname)
	{
		return getSpawns().stream().filter(Objects::nonNull).filter(L2Spawn :: hasTerritory).filter(s -> String.valueOf(s.getTerritory().getName()).equalsIgnoreCase(Terrname)).collect(Collectors.toList());
	}
	
	public List<L2Spawn> getSpawns()
	{
		return _spawns;
	}
	
	public void cleanUp()
	{
		getSpawns().clear();
	}

	private static class SingletonHolder
	{
		protected static final CustomSpawnManager _instance = new CustomSpawnManager();
	}
}