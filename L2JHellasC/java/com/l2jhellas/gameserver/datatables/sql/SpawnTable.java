package com.l2jhellas.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.instancemanager.DayNightSpawnManager;
import com.l2jhellas.gameserver.model.L2Spawn;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class SpawnTable
{
	private static Logger _log = Logger.getLogger(SpawnTable.class.getName());
	
	private static final SpawnTable _instance = new SpawnTable();
	
	private final Map<Integer, Set<L2Spawn>> _spawntable = new ConcurrentHashMap<>();
	
	private static final String SELECT_ALL_SPAWNS = "SELECT * FROM spawnlist";
	private static final String ADD_NEW_SPAWN = "INSERT INTO spawnlist (id,count,npc_templateid,locx,locy,locz,heading,respawn_delay,loc_id) VALUES (?,?,?,?,?,?,?,?,?)";
	private static final String DELETE_SPAWN = "DELETE FROM spawnlist WHERE locx=? AND locy=? AND locz=? AND npc_templateid=? AND heading=?";

	public static SpawnTable getInstance()
	{
		return _instance;
	}
	
	private SpawnTable()
	{
		if (!Config.ALT_DEV_NO_SPAWNS)
			reloadAll();
	}

	private void fillSpawnTable()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
		PreparedStatement statement = con.prepareStatement(SELECT_ALL_SPAWNS))
		{	
			try(ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					final L2NpcTemplate template1 = NpcData.getInstance().getTemplate(rset.getInt("npc_templateid"));
					if (template1 != null)
					{
						// Don't spawn
						if (template1.type.equalsIgnoreCase("L2SiegeGuard"))
							continue;
						if (template1.type.equalsIgnoreCase("L2RaidBoss"))
							continue;
						if (!Config.ALLOW_CLASS_MASTER && template1.type.equals("L2ClassMaster"))
							continue;

						final L2Spawn spawnDat = new L2Spawn(template1);
						spawnDat.setId(rset.getInt("id"));
						spawnDat.setAmount(rset.getInt("count"));
						spawnDat.setLocx(rset.getInt("locx"));
						spawnDat.setLocy(rset.getInt("locy"));
						spawnDat.setLocz(rset.getInt("locz"));
						spawnDat.setHeading(rset.getInt("heading"));
						spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
						int loc_id = rset.getInt("loc_id");
						spawnDat.setLocation(loc_id);			
						NotifySpawnManager(spawnDat,rset.getInt("periodOfDay"));				
						addSpawn(spawnDat);			
					}
					else
						_log.warning("SpawnTable: data missing in npc table for id: " + rset.getInt("npc_templateid") + ".");
				}
			}
		}
		catch (Exception e)
		{
			_log.warning("SpawnTable: Spawn could not be initialized: " + e);
		}
		
		_log.info(SpawnTable.class.getSimpleName() + ": Loaded " + _spawntable.size() + " Npc Spawn Locations.");
	}

	protected void NotifySpawnManager(L2Spawn spawn , int period)
	{
		switch (period)
		{
			case 0: // default
				spawn.init();
				break;
			case 1: // Day
				DayNightSpawnManager.getInstance().addDayCreature(spawn);
				break;
			case 2: // Night
				DayNightSpawnManager.getInstance().addNightCreature(spawn);
				break;
		}	
	}
	
	public void addNewSpawn(L2Spawn spawn, boolean storeInDb)
	{
		addSpawn(spawn);
		
		if (storeInDb)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(ADD_NEW_SPAWN))
			{
				statement.setInt(1, spawn.getId());
				statement.setInt(2, spawn.getAmount());
				statement.setInt(3, spawn.getNpcid());
				statement.setInt(4, spawn.getLocx());
				statement.setInt(5, spawn.getLocy());
				statement.setInt(6, spawn.getLocz());
				statement.setInt(7, spawn.getHeading());
				statement.setInt(8, spawn.getRespawnDelay() / 1000);
				statement.setInt(9, spawn.getLocation());
				statement.execute();
			}
			catch (Exception e)
			{
				_log.warning(SpawnTable.class.getName() + ": Could not store spawn in the DB:");
				e.printStackTrace();
			}
		}
	}
	
	public void deleteSpawn(L2Spawn spawn, boolean updateDb)
	{
		final Set<L2Spawn> set = _spawntable.get(spawn.getId());		
		if (set == null)
             return;
		
		set.remove(spawn);
		
		if (set.isEmpty())
			_spawntable.remove(spawn.getId());
		
		if (updateDb)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(DELETE_SPAWN))
			{
				ps.setInt(1, spawn.getLocx());
				ps.setInt(2, spawn.getLocy());
				ps.setInt(3, spawn.getLocz());
				ps.setInt(4, spawn.getNpcid());
				ps.setInt(5, spawn.getHeading());
				ps.execute();
			}
			catch (Exception e)
			{
				_log.warning(SpawnTable.class.getName() + ": Spawn Id " + spawn.getNpcid() + " could not be removed from DB: " + e);
			}
		}
	}
	
	public Map<Integer, Set<L2Spawn>> getSpawnTable()
	{
		return _spawntable;
	}
	
	public Set<L2Spawn> getTemplate(int id)
	{
		return _spawntable.getOrDefault(id, Collections.emptySet());
	}
	
	public L2Spawn getSpawn(int npcId)
	{
		return getTemplate(npcId).stream().findFirst().orElse(null);
	}
	
	private void addSpawn(L2Spawn spawn)
	{
		_spawntable.computeIfAbsent(spawn.getId(), k -> ConcurrentHashMap.newKeySet(1)).add(spawn);
	}
	
	// just wrapper
	public void reloadAll()
	{
		_spawntable.clear();
		fillSpawnTable();
	}
	
	public void findNPCInstances(L2PcInstance activeChar, int npcId, int teleportIndex)
	{
		int index = 0;
		for (Set<L2Spawn> set :  _spawntable.values())
		{		
			for (L2Spawn spawn : set)
			{
			if (npcId == spawn.getNpcid())
			{
				index++;
				
				if (teleportIndex > -1)
				{
					if (teleportIndex == index)
						activeChar.teleToLocation(spawn.getLocx(), spawn.getLocy(), spawn.getLocz(), true);
				}
				else
					activeChar.sendMessage(index + " - " + spawn.getTemplate().name + " (" + spawn.getId() + "): " + spawn.getLocx() + " " + spawn.getLocy() + " " + spawn.getLocz());
			}
			}
		}
		
		if (index == 0)
			activeChar.sendMessage("No current spawns found.");
	}
	
	public boolean forEachSpawn(Function<L2Spawn, Boolean> function)
	{
		for (Set<L2Spawn> set : _spawntable.values())
		{
			for (L2Spawn spawn : set)
				if (!function.apply(spawn))
					return false;
		}
		return true;
	}
}