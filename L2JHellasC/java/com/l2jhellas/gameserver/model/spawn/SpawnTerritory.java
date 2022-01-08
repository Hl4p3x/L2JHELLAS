package com.l2jhellas.gameserver.model.spawn;

import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.model.zone.L2ZoneForm;
import com.l2jhellas.gameserver.network.serverpackets.ExServerPrimitive;

public class SpawnTerritory
{
	private final String _name;
	private final L2ZoneForm _territory;
	
	public SpawnTerritory(String name, L2ZoneForm territory)
	{
		_name = name;
		_territory = territory;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public Location getRandomPoint()
	{
		return _territory.getRandomPoint();
	}
	
	public boolean isInsideZone(double x, double y, double z)
	{
		return _territory.isInsideZone(x, y, z);
	}
	
	public void visualizeZone(ExServerPrimitive debug, int z)
	{
		_territory.visualizeZone(getName(),debug, z);
	}
}