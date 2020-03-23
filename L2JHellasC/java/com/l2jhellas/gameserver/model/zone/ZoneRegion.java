package com.l2jhellas.gameserver.model.zone;

import java.util.ArrayList;
import java.util.List;

import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.zone.type.L2DerbyTrackZone;
import com.l2jhellas.gameserver.model.zone.type.L2PeaceZone;
import com.l2jhellas.gameserver.model.zone.type.L2TownZone;

public class ZoneRegion
{
	private final int _regionX;
	private final int _regionY;
	
	private final List<L2ZoneType> _zones = new ArrayList<>();
	
	public ZoneRegion(int regionX, int regionY)
	{
		_regionX = regionX;
		_regionY = regionY;
	}
	
	public List<L2ZoneType> getZones()
	{
		return _zones;
	}
	
	public void addZone(L2ZoneType zone)
	{
		_zones.add(zone);
	}
	
	public void removeZone(L2ZoneType zone)
	{
		_zones.remove(zone);
	}
	
	public int getRegionX()
	{
		return _regionX;
	}
	
	public int getRegionY()
	{
		return _regionY;
	}
	
	public void revalidateZones(L2Character character)
	{
		// Do NOT update the world region while the character is still in the process of teleporting
		if (character.isTeleporting())
			return;
		
		_zones.forEach(z -> z.revalidateInZone(character));
	}
	
	public void removeFromZones(L2Character character)
	{
		_zones.forEach(z -> z.removeCharacter(character));
	}
	
	public boolean checkEffectRangeInsidePeaceZone(L2Skill skill, final int x, final int y, final int z)
	{
		final int range = skill.getEffectRange();
		final int up = y + range;
		final int down = y - range;
		final int left = x + range;
		final int right = x - range;
		
		for (L2ZoneType e : getZones())
		{
			if ((e instanceof L2TownZone && ((L2TownZone) e).isPeaceZone()) || e instanceof L2DerbyTrackZone || e instanceof L2PeaceZone)
			{
				if (e.isInsideZone(x, up, z))
					return false;
				
				if (e.isInsideZone(x, down, z))
					return false;
				
				if (e.isInsideZone(left, y, z))
					return false;
				
				if (e.isInsideZone(right, y, z))
					return false;
				
				if (e.isInsideZone(x, y, z))
					return false;
			}
		}
		return true;
	}
	
	public void onDeath(L2Character character)
	{
		_zones.stream().filter(z -> z.isCharacterInZone(character)).forEach(z -> z.onDieInside(character));
		
	}
	
	public void onRevive(L2Character character)
	{
		_zones.stream().filter(z -> z.isCharacterInZone(character)).forEach(z -> z.onReviveInside(character));
		
	}
}