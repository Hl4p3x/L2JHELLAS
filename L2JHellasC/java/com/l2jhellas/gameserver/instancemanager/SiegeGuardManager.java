package com.l2jhellas.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.sql.NpcData;
import com.l2jhellas.gameserver.model.L2Spawn;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.entity.Castle;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class SiegeGuardManager
{
	private static Logger _log = Logger.getLogger(SiegeGuardManager.class.getName());
	
	private static final Map<Integer, Set<L2Spawn>> _siegeGuardSpawn = new ConcurrentHashMap<>();

	public SiegeGuardManager()
	{
		
	}
	
	public void addSiegeGuard(Castle castle,L2PcInstance activeChar, int npcId)
	{
		if (activeChar == null)
			return;
		addSiegeGuard(castle,activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getHeading(), npcId);
	}
	
	public void addSiegeGuard(Castle castle,int x, int y, int z, int heading, int npcId)
	{
		saveSiegeGuard(castle,x, y, z, heading, npcId, 0);
	}
	
	public void hireMerc(Castle castle,L2PcInstance activeChar, int npcId)
	{
		if (activeChar == null)
			return;
		hireMerc(castle,activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getHeading(), npcId);
	}
	
	public void hireMerc(Castle castle,int x, int y, int z, int heading, int npcId)
	{
		saveSiegeGuard(castle,x, y, z, heading, npcId, 1);
	}
	
	public void removeMerc(int npcId, int x, int y, int z)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("DELETE FROM castle_siege_guards WHERE npcId = ? AND x = ? AND y = ? AND z = ? AND isHired = 1");
			statement.setInt(1, npcId);
			statement.setInt(2, x);
			statement.setInt(3, y);
			statement.setInt(4, z);
			statement.execute();
			statement.close();
		}
		catch (Exception e1)
		{
			_log.warning(SiegeGuardManager.class.getName() + ": Error deleting hired siege guard at " + x + ',' + y + ',' + z + ":" + e1);
			if (Config.DEVELOPER)
				e1.printStackTrace();
		}
	}
	
	public void removeMercs(Castle castle)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement("DELETE FROM castle_siege_guards WHERE castleId = ? AND isHired = 1"))
		{
			st.setInt(1, castle.getCastleId());
			st.execute();
		}
		catch (Exception e1)
		{
			_log.warning(SiegeGuardManager.class.getName() + ": Error deleting hired siege guard for castle " + castle.getName() + ":" + e1);
			if (Config.DEVELOPER)
				e1.printStackTrace();
		}
	}
	
	public void spawnSiegeGuard(Castle castle)
	{
		try
		{
			final boolean isHired = (castle.getOwnerId() > 0);
			loadSiegeGuard(castle);
			
			for (L2Spawn spawn : getSpawnedGuards(castle.getCastleId()))
			{
				if (spawn != null)
				{
					spawn.init();
					if (isHired)
						spawn.stopRespawn();
					
					spawn.getLastSpawn().broadcastInfo();
				}
			}
		}
		catch (Exception e)
		{
			_log.warning(SiegeGuardManager.class.getName() + ": Error spawning siege guards for castle " + castle.getName() + e);
		}
	}
	
	public void unspawnSiegeGuard(Castle castle)
	{
		for (L2Spawn spawn : getSpawnedGuards(castle.getCastleId()))
		{
			if (spawn == null)
				continue;
			
			spawn.stopRespawn();
			spawn.getLastSpawn().doDie(spawn.getLastSpawn());
		}
		
		getSpawnedGuards(castle.getCastleId()).clear();
	}
	
	private void loadSiegeGuard(Castle castle)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM castle_siege_guards WHERE castleId = ? AND isHired = ?");
			statement.setInt(1, castle.getCastleId());
			statement.setInt(2, castle.getOwnerId() > 0 ? 1 : 0);

			ResultSet rs = statement.executeQuery();
			
			L2Spawn spawn1;
			L2NpcTemplate template1;
			
			while (rs.next())
			{
				template1 = NpcData.getInstance().getTemplate(rs.getInt("npcId"));
				if (template1 != null)
				{
					spawn1 = new L2Spawn(template1);
					spawn1.setId(rs.getInt("id"));
					spawn1.setAmount(1);
					spawn1.setLocx(rs.getInt("x"));
					spawn1.setLocy(rs.getInt("y"));
					spawn1.setLocz(rs.getInt("z"));
					spawn1.setHeading(rs.getInt("heading"));
					spawn1.setRespawnDelay(rs.getInt("respawnDelay"));
					spawn1.setLocation(0);
					getSpawnedGuards(castle.getCastleId()).add(spawn1);
				}
				else
				{
					_log.warning(SiegeGuardManager.class.getName() + ": Missing npc data in npc table for id: " + rs.getInt("npcId"));
				}
			}
			rs.close();
			statement.close();
		}
		catch (Exception e1)
		{
			_log.warning(SiegeGuardManager.class.getName() + ": Error loading siege guard for castle " + castle.getName() + ":" + e1);
			if (Config.DEVELOPER)
				e1.printStackTrace();
		}
	}
	
	public void saveSiegeGuard(Castle castle,int x, int y, int z, int heading, int npcId, int isHire)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("INSERT INTO castle_siege_guards (castleId,npcId,x,y,z,heading,respawnDelay,isHired) VALUES (?,?,?,?,?,?,?,?)");
			statement.setInt(1, castle.getCastleId());
			statement.setInt(2, npcId);
			statement.setInt(3, x);
			statement.setInt(4, y);
			statement.setInt(5, z);
			statement.setInt(6, heading);
			if (isHire == 1)
				statement.setInt(7, 0);
			else
				statement.setInt(7, 600);
			statement.setInt(8, isHire);
			statement.execute();
			statement.close();
		}
		catch (Exception e1)
		{
			_log.warning(SiegeGuardManager.class.getName() + ": Error adding siege guard for castle " + castle.getName());
			if (Config.DEVELOPER)
			{
				e1.printStackTrace();
			}
		}
	}

	public Set<L2Spawn> getSpawnedGuards(int castleId)
	{
		return _siegeGuardSpawn.computeIfAbsent(castleId, key -> ConcurrentHashMap.newKeySet());
	}
	
	public static SiegeGuardManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SiegeGuardManager _instance = new SiegeGuardManager();
	}
}