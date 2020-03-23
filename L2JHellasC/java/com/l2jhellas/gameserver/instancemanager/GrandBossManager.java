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
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM grandboss_data ORDER BY boss_id");
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				// Read all info from DB, and store it for AI to read and decide what to do
				// faster than accessing DB in real time
				StatsSet info = new StatsSet();
				int bossId = rset.getInt("boss_id");
				info.set("loc_x", rset.getInt("loc_x"));
				info.set("loc_y", rset.getInt("loc_y"));
				info.set("loc_z", rset.getInt("loc_z"));
				info.set("heading", rset.getInt("heading"));
				info.set("respawn_time", rset.getLong("respawn_time"));
				double HP = rset.getDouble("currentHP"); // jython doesn't recognize doubles
				int true_HP = (int) HP; // so use java's ability to type cast
				info.set("currentHP", true_HP); // to convert double to int
				double MP = rset.getDouble("currentMP");
				int true_MP = (int) MP;
				info.set("currentMP", true_MP);
				_bossStatus.put(bossId, rset.getInt("status"));
				
				_storedInfo.put(bossId, info);
				info = null;
			}
			
			_log.info(GrandBossManager.class.getSimpleName() + ": Loaded " + _storedInfo.size() + " Instances.");
			
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning(GrandBossManager.class.getName() + ": Could not load grandboss_data table");
			e.printStackTrace();
		}
	}
	
	public void initZones()
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
		return _bossStatus != null &&_bossStatus.containsKey(bossId) ? _bossStatus.get(bossId) : -1;
	}
	
	public static void setBossStatus(int bossId, int status)
	{
		_bossStatus.put(bossId, status);
	}
	
	public static void addBoss(L2GrandBossInstance boss)
	{
		if (boss != null)
			_bosses.put(boss.getNpcId(), boss);
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
		storeToDb();
	}
	
	private static void storeToDb()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(DELETE_GRAND_BOSS_LIST);
			statement.executeUpdate();
			statement.close();
			
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
					statement = con.prepareStatement(INSERT_GRAND_BOSS_LIST);
					statement.setInt(1, player);
					statement.setInt(2, id);
					statement.executeUpdate();
					statement.close();
				}
			}
			
			for (Integer bossId : _storedInfo.keySet())
			{
				final L2GrandBossInstance boss = _bosses.get(bossId);
				StatsSet info = _storedInfo.get(bossId);
				if (boss == null || info == null)
				{
					statement = con.prepareStatement(UPDATE_GRAND_BOSS_DATA2);
					statement.setInt(1, _bossStatus.get(bossId));
					statement.setInt(2, bossId);
				}
				else
				{
					statement = con.prepareStatement(UPDATE_GRAND_BOSS_DATA);
					statement.setInt(1, boss.getX());
					statement.setInt(2, boss.getY());
					statement.setInt(3, boss.getZ());
					statement.setInt(4, boss.getHeading());
					statement.setLong(5, info.getLong("respawn_time"));
					double hp = boss.getCurrentHp();
					double mp = boss.getCurrentMp();
					if (boss.isDead())
					{
						hp = boss.getMaxHp();
						mp = boss.getMaxMp();
					}
					statement.setDouble(6, hp);
					statement.setDouble(7, mp);
					statement.setInt(8, _bossStatus.get(bossId));
					statement.setInt(9, bossId);
				}
				statement.executeUpdate();
				statement.clearParameters();
				statement.close();
			}
		}
		catch (SQLException e)
		{
			_log.warning(GrandBossManager.class.getName() + ": Couldn't store grandbosses to database:");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
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
	
	public void cleanUp()
	{
		storeToDb();
		
		_bosses.clear();
		_storedInfo.clear();
		_bossStatus.clear();
		_zones.clear();
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