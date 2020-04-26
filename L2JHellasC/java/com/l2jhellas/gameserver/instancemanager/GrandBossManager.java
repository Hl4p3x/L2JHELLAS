package com.l2jhellas.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.zone.type.L2BossZone;
import com.l2jhellas.gameserver.templates.StatsSet;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class GrandBossManager
{
	protected static Logger _log = Logger.getLogger(GrandBossManager.class.getName());
	
	private static final String SELECT_GRAND_BOSS_DATA = "SELECT * from grandboss_data ORDER BY boss_id";
	private static final String DELETE_GRAND_BOSS_LIST = "DELETE FROM grandboss_list";	
	private static final String INSERT_GRAND_BOSS_LIST = "INSERT INTO grandboss_list (player_id,zone) VALUES (?,?)";
	private static final String UPDATE_GRAND_BOSS_DATA = "UPDATE grandboss_data SET loc_x = ?, loc_y = ?, loc_z = ?, heading = ?, respawn_time = ?, currentHP = ?, currentMP = ?, status = ? WHERE boss_id = ?";
	private static final String UPDATE_GRAND_BOSS_DATA2 = "UPDATE grandboss_data SET status = ? WHERE boss_id = ?";
		
	protected static Map<Integer, L2GrandBossInstance> _bosses = new HashMap<>();	
	protected static Map<Integer, StatsSet> _storedInfo = new HashMap<>();	
	private static Map<Integer, Integer> _bossStatus = new HashMap<>();

	private static List<L2BossZone> _zones = new ArrayList<>();
	
	public GrandBossManager()
	{
		init();
	}
	
	public void reload()
	{
		cleanUp();
		init();
	}
	
	private static void init()
	{	
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(SELECT_GRAND_BOSS_DATA);
				ResultSet rset = ps.executeQuery())
			{
				while (rset.next())
				{					
					final StatsSet info = new StatsSet();
					final int bossId = rset.getInt("boss_id");
					info.set("loc_x", rset.getInt("loc_x"));
					info.set("loc_y", rset.getInt("loc_y"));
					info.set("loc_z", rset.getInt("loc_z"));
					info.set("heading", rset.getInt("heading"));
					info.set("respawn_time", rset.getLong("respawn_time"));
					info.set("currentHP", rset.getDouble("currentHP"));
					info.set("currentMP", rset.getDouble("currentMP"));
					
					_bossStatus.put(bossId, rset.getInt("status"));
					_storedInfo.put(bossId, info);
				}
			}
		catch (SQLException e)
		{
			_log.warning(GrandBossManager.class.getName() + ": Could not load grandboss_data table");
			e.printStackTrace();
		}
		
		_log.info(GrandBossManager.class.getSimpleName() + ": Loaded " + _storedInfo.size() + " Instances.");
	}
	
	public void initZoness()
	{
		HashMap<Integer, ArrayList<Integer>> zones = new HashMap<>();
		
		if (_zones == null)
		{
			_log.warning(GrandBossManager.class.getName() + ": Could not read Grand Boss zone data");
			return;
		}
		
		for (L2BossZone zone : _zones)
		{
			if (zone == null)
				continue;
			zones.put(zone.getId(), new ArrayList<Integer>());
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM grandboss_list ORDER BY player_id");
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				int id = rset.getInt("player_id");
				int zone_id = rset.getInt("zone");
				zones.get(zone_id).add(id);
			}
			
			for (L2BossZone zone : _zones)
			{
				if (zone == null)
					continue;
				zone.allowPlayerEntry(rset.getInt("player_id"));
			}
			
			rset.close();
			statement.close();
			
			_log.info(GrandBossManager.class.getSimpleName() + ": Initialized " + _zones.size() + " Grand Boss Zones.");
		}
		catch (SQLException e)
		{
			_log.warning(GrandBossManager.class.getName() + ": Could not load grandboss_list table");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
		zones.clear();
	}
	
	public void addZone(L2BossZone zone)
	{
		if (zone != null && !_zones.contains(zone))
			_zones.add(zone);
	}
	
	public final static L2BossZone getZone(L2Character character)
	{
		if (_zones != null)
		{
			for (L2BossZone temp : _zones)
			{
				if (temp.isCharacterInZone(character))
					return temp;
			}
		}
		return null;
	}
	
	public final static L2BossZone getZoneById(int Id)
	{
		return _zones.stream().filter(zo -> zo != null && zo.getId() == Id).findFirst().orElse(null);
	}
	
	public final static L2BossZone getZone(int x, int y, int z)
	{
		if (_zones != null)
		{
			for (L2BossZone temp : _zones)
			{
				if (temp.isInsideZone(x, y, z))
					return temp;
			}
		}
		return null;
	}
	
	public boolean checkIfInZone(L2PcInstance player)
	{
		if (player == null)
			return false;
		L2BossZone temp = getZone(player.getX(), player.getY(), player.getZ());
		if (temp == null)
			return false;
		
		return true;
	}
	
	public int getBossStatus(int bossId)
	{
		return _bossStatus != null && _bossStatus.containsKey(bossId)  ? _bossStatus.get(bossId) : -1;
	}
	
	public static void setBossStatus(int bossId, int status)
	{
		_bossStatus.put(bossId, status);
		storeToDb(bossId,true);
	}
	
	public static void addBoss(L2GrandBossInstance boss)
	{
		if (boss != null)
			_bosses.put(boss.getNpcId(), boss);
	}
	
	public static void addBoss(int npcId , L2GrandBossInstance boss)
	{
		if (boss != null)
			_bosses.put(npcId, boss);
	}
	
	public L2GrandBossInstance getBoss(int bossId)
	{
		return _bosses.get(bossId);
	}
	
	public static StatsSet getStatsSet(int bossId)
	{
		return _storedInfo.get(bossId);
	}
	
	public static void setStatsSet(int bossId, StatsSet info)
	{
		_storedInfo.put(bossId, info);
		storeToDb(bossId,false);
	}
	
	private static void storeToDb(int bossId, boolean status)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			final StatsSet info = _storedInfo.get(bossId);
			final L2GrandBossInstance boss = _bosses.get(bossId);
			
			if (status || boss == null || info == null)
			{
				try (PreparedStatement ps = con.prepareStatement(UPDATE_GRAND_BOSS_DATA2))
				{
					ps.setInt(1, _bossStatus.get(bossId));
					ps.setInt(2, bossId);
					ps.executeUpdate();
				}
			}
			else
			{
				try (PreparedStatement ps = con.prepareStatement(UPDATE_GRAND_BOSS_DATA))
				{
					ps.setInt(1, boss.getX());
					ps.setInt(2, boss.getY());
					ps.setInt(3, boss.getZ());
					ps.setInt(4, boss.getHeading());
					ps.setLong(5, info.getLong("respawn_time"));
					ps.setDouble(6, (boss.isDead()) ? boss.getMaxHp() : boss.getCurrentHp());
					ps.setDouble(7, (boss.isDead()) ? boss.getMaxMp() : boss.getCurrentMp());
					ps.setInt(8, _bossStatus.get(bossId));
					ps.setInt(9, bossId);
					ps.executeUpdate();
				}
			}
				
			try (PreparedStatement ps = con.prepareStatement(DELETE_GRAND_BOSS_LIST))
			{
				ps.executeUpdate();
			}
			
			try (PreparedStatement ps = con.prepareStatement(INSERT_GRAND_BOSS_LIST))
			{
				for (L2BossZone zone : _zones)
				{
					if (zone == null)
						continue;
					Integer id = zone.getId();
					Set<Integer> list = zone.getAllowedPlayers();
					if (list == null || list.isEmpty())
						continue;
					for (Integer player : list)
					{
						ps.setInt(1, player);
						ps.setInt(2, id);
						ps.addBatch();
					}
				}
				ps.executeBatch();
			}		
		}
		catch (Exception e)
		{
			_log.warning(GrandBossManager.class.getName() + ": Couldn't store grandbosses to database: " +e);
		}
	}

	public void cleanUp()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps1 = con.prepareStatement(UPDATE_GRAND_BOSS_DATA2);
			PreparedStatement ps2 = con.prepareStatement(UPDATE_GRAND_BOSS_DATA))
		{					
			for (Map.Entry<Integer, StatsSet> infoEntry : _storedInfo.entrySet())
			{
				final int bossId = infoEntry.getKey();
				final StatsSet info = infoEntry.getValue();
				final L2GrandBossInstance boss = _bosses.get(bossId);
				
				if (boss == null || info == null)
				{
					ps1.setInt(1, _bossStatus.get(bossId));
					ps1.setInt(2, bossId);
					ps1.addBatch();
				}
				else
				{
					ps2.setInt(1, boss.getX());
					ps2.setInt(2, boss.getY());
					ps2.setInt(3, boss.getZ());
					ps2.setInt(4, boss.getHeading());
					ps2.setLong(5, info.getLong("respawn_time"));
					ps2.setDouble(6, (boss.isDead()) ? boss.getMaxHp() : boss.getCurrentHp());
					ps2.setDouble(7, (boss.isDead()) ? boss.getMaxMp() : boss.getCurrentMp());
					ps2.setInt(8, _bossStatus.get(bossId));
					ps2.setInt(9, bossId);
					ps2.addBatch();
				}
			}
			
			ps1.executeBatch();
			ps2.executeBatch();
			
			try (PreparedStatement ps = con.prepareStatement(DELETE_GRAND_BOSS_LIST))
			{
				ps.executeUpdate();
			}
			
			try (PreparedStatement ps = con.prepareStatement(INSERT_GRAND_BOSS_LIST))
			{
				for (L2BossZone zone : _zones)
				{
					if (zone == null)
						continue;
					Integer id = zone.getId();
					Set<Integer> list = zone.getAllowedPlayers();
					if (list == null || list.isEmpty())
						continue;
					for (Integer player : list)
					{
						ps.setInt(1, player);
						ps.setInt(2, id);
						ps.addBatch();
					}
				}
				ps.executeBatch();
			}
		}
		catch (Exception e)
		{
			_log.warning(GrandBossManager.class.getName() + ": cleanUp couldn't store grandbosses to database: " +e);
		}
		
		_bosses.clear();
		_storedInfo.clear();
		_bossStatus.clear();
		_zones.clear();
	}
	
	public static boolean isInBossZone(L2Character character)
	{
		for (L2BossZone temp : _zones)
		{
			if (temp.isCharacterInZone(character))
				return true;
		}
		return false;
	}

	public List<L2BossZone> getZones()
	{
		return _zones;
	}
	
	private static GrandBossManager _instance;

	public static GrandBossManager getInstance()
	{
		if (_instance == null)
			_instance = new GrandBossManager();
		return _instance;
	}
}