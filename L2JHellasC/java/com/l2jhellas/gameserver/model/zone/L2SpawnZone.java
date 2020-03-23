package com.l2jhellas.gameserver.model.zone;

import java.util.ArrayList;
import java.util.List;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.util.Rnd;

public abstract class L2SpawnZone extends L2ZoneType
{
	private static Location[] _coords = new Location[22];
	private List<Location> _chaoticSpawnLocs = null;
	private List<Location> _spawnLocs = null;
	
	public static final void STADIUMSADD()
	{
		_coords[0] = new Location(-20814, -21189, -3030);
		_coords[1] = new Location(-120324, -225077, -3331);
		_coords[2] = new Location(-102495, -209023, -3331);
		_coords[3] = new Location(-120156, -207378, -3331);
		_coords[4] = new Location(-87628, -225021, -3331);
		_coords[5] = new Location(-81705, -213209, -3331);
		_coords[6] = new Location(-87593, -207339, -3331);
		_coords[7] = new Location(-93709, -218304, -3331);
		_coords[8] = new Location(-77157, -218608, -3331);
		_coords[9] = new Location(-69682, -209027, -3331);
		_coords[10] = new Location(-76887, -201256, -3331);
		_coords[11] = new Location(-109985, -218701, -3331);
		_coords[12] = new Location(-126367, -218228, -3331);
		_coords[13] = new Location(-109629, -201292, -3331);
		_coords[14] = new Location(-87523, -240169, -3331);
		_coords[15] = new Location(-81748, -245950, -3331);
		_coords[16] = new Location(-77123, -251473, -3331);
		_coords[17] = new Location(-69778, -241801, -3331);
		_coords[18] = new Location(-76754, -234014, -3331);
		_coords[19] = new Location(-93742, -251032, -3331);
		_coords[20] = new Location(-87466, -257752, -3331);
		_coords[21] = new Location(-114413, -213241, -3331);
	}
	
	public Location getCoordinates(int id)
	{
		return _coords[id];
	}
	
	public L2SpawnZone(int id)
	{
		super(id);
	}
	
	public void addSpectator(int id, L2PcInstance spec)
	{
		if (spec != null)
			spec.enterOlympiadObserverMode(getCoordinates(id).getX(), getCoordinates(id).getY(), getCoordinates(id).getZ(), id);
	}
	
	public final void addSpawn(int x, int y, int z)
	{
		if (_spawnLocs == null)
			_spawnLocs = new ArrayList<>();
		
		_spawnLocs.add(new Location(x, y, z));
	}
	
	public final void addChaoticSpawn(int x, int y, int z)
	{
		if (_chaoticSpawnLocs == null)
			_chaoticSpawnLocs = new ArrayList<>();
		
		_chaoticSpawnLocs.add(new Location(x, y, z));
	}
	
	public final List<Location> getSpawns()
	{
		return _spawnLocs;
	}
	
	public final Location getSpawnLoc()
	{
		return _spawnLocs.get(Rnd.get(_spawnLocs.size()));
	}
	
	public void clearSpawnZone()
	{
		if (_spawnLocs != null && !_spawnLocs.isEmpty())
			_spawnLocs.clear();
		
		if (_chaoticSpawnLocs != null && !_chaoticSpawnLocs.isEmpty())
			_chaoticSpawnLocs.clear();
	}
	
	public final Location getChaoticSpawnLoc()
	{
		if (_chaoticSpawnLocs != null)
			return _chaoticSpawnLocs.get(Rnd.get(_chaoticSpawnLocs.size()));
		
		return getSpawnLoc();
	}
}