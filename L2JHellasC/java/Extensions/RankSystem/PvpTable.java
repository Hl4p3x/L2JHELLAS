package Extensions.RankSystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import Extensions.RankSystem.Util.RPSUtil;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class PvpTable
{
	public static final Logger log = Logger.getLogger(PvpTable.class.getSimpleName());
	
	private static PvpTable _instance = null;
	
	private Map<Integer, PvpSummary> _pvpTable = new ConcurrentHashMap<>();
	
	private PvpTable()
	{
		long startTime = Calendar.getInstance().getTimeInMillis();
		
		if (Config.DATABASE_CLEANER_ENABLED)
			cleanPvpTable();
		
		load();
		
		long endTime = Calendar.getInstance().getTimeInMillis();
		
		log.log(Level.INFO, " - PvpTable: Data loaded. " + (_pvpTable.size()) + " objects in " + (endTime - startTime) + " ms.");
		
		ThreadPoolManager.getInstance().scheduleGeneral(new PvpTableSchedule(), Config.PVP_TABLE_UPDATE_INTERVAL);
	}
	
	public static PvpTable getInstance()
	{
		if (_instance == null)
			_instance = new PvpTable();
		
		return _instance;
	}
	
	public Pvp getPvp(int killerId, int victimId, boolean readOnly, boolean updateDailyStats)
	{
		// find and get PvP:
		PvpSummary kps = getKillerPvpSummary(killerId, readOnly, updateDailyStats);
		
		if (kps != null)
		{
			Pvp pvp = kps.getVictimPvpTable().get(victimId);
			
			if (pvp != null)
				return pvp;
		}
		
		// otherwise create and get new PvP:
		if (kps == null)
		{
			kps = new PvpSummary();
			kps.setKillerId(killerId);
			kps.updateRankId();
		}
		
		Pvp pvp = kps.getVictimPvpTable().get(victimId);
		
		if (pvp == null)
		{
			pvp = new Pvp();
			
			pvp.setVictimId(victimId);
			
			if (readOnly)
				pvp.setDbStatus(DBStatus.NONE);
			else
				kps.getVictimPvpTable().put(victimId, pvp);
		}
		
		return pvp;
	}
	
	public Pvp getPvp(int killerId, int victimId, long systemDay, boolean updateDailyStats)
	{
		// find and get PvP:
		PvpSummary kps = getKillerPvpSummary(killerId, systemDay, false, updateDailyStats);
		
		if (kps != null)
		{
			Pvp pvp = kps.getVictimPvpTable().get(victimId);
			
			if (pvp != null)
			{
				// check daily fields, reset if kill day is other than system day:
				if (pvp.getKillDay() != systemDay)
					pvp.resetDailyFields();
				
				return pvp;
			}
		}
		
		// otherwise create and get new PvP:
		if (kps == null)
		{
			kps = new PvpSummary();
			kps.setKillerId(killerId);
			kps.updateRankId();
		}
		
		Pvp pvp = kps.getVictimPvpTable().get(victimId);
		
		if (pvp == null)
		{
			pvp = new Pvp();
			
			pvp.setVictimId(victimId);
			
			kps.getVictimPvpTable().put(victimId, pvp);
		}
		
		return pvp;
	}
	
	public PvpSummary getKillerPvpSummary(int killerId, boolean readOnly, boolean updateDailyStats)
	{
		// get system day for update daily fields:
		Calendar c = Calendar.getInstance();
		c.set(Calendar.MILLISECOND, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.HOUR, 0);
		
		long systemDay = c.getTimeInMillis(); // current system day
		
		// find and get PvP:
		PvpSummary kps = _pvpTable.get(killerId);
		
		if (kps == null)
		{
			kps = new PvpSummary();
			kps.setKillerId(killerId);
			kps.updateRankId();
			kps.updateDailyStats(systemDay);
			
			if (!readOnly)
				_pvpTable.put(killerId, kps);
		}
		else if (updateDailyStats)
		{
			kps.updateDailyStats(systemDay);
		}
		
		return kps;
	}
	
	public PvpSummary getKillerPvpSummary(int killerId, long systemDay, boolean readOnly, boolean updateDailyStats)
	{
		// find and get PvP:
		PvpSummary kps = _pvpTable.get(killerId);
		
		if (kps == null)
		{
			kps = new PvpSummary();
			
			kps.setKillerId(killerId);
			kps.updateRankId();
			kps.updateDailyStats(systemDay);
			
			if (!readOnly)
				_pvpTable.put(killerId, kps);
		}
		else if (updateDailyStats)
		{
			kps.updateDailyStats(systemDay);
		}
		
		return kps;
	}
	
	public int getRankId(int killerId)
	{
		// find and get PvP:
		PvpSummary kps = _pvpTable.get(killerId);
		
		if (kps != null)
			return kps.getRankId();
		
		return -1;
	}
	
	public Map<Integer, PvpSummary> getPvpTable()
	{
		return _pvpTable;
	}
	
	public void setPvpTable(Map<Integer, PvpSummary> pvpTable)
	{
		_pvpTable = pvpTable;
	}
	
	private void load()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
		PreparedStatement statement = con.prepareStatement("SELECT * FROM rank_pvp_system_pvp"))
		{
			try(ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					Pvp pvp = new Pvp();

					pvp.setVictimId(rset.getInt("victim_id"));
					pvp.setKills(rset.getInt("kills"));
					pvp.setKillsToday(rset.getInt("kills_today"));
					pvp.setKillsLegal(rset.getInt("kills_legal"));
					pvp.setKillsLegalToday(rset.getInt("kills_today_legal"));
					pvp.setRankPoints(rset.getLong("rank_points"));
					pvp.setRankPointsToday(rset.getLong("rank_points_today"));
					pvp.setKillTime(rset.getLong("kill_time"));
					pvp.setKillDay(rset.getLong("kill_day"));

					PvpSummary kps = getKillerPvpSummary(rset.getInt("killer_id"), false, false);

					pvp.setDbStatus(DBStatus.NONE);

					kps.addVictimPvpOnLoadFromDB(pvp);
				}
			}
			
			try (PreparedStatement ps = con.prepareStatement("SELECT * FROM rank_pvp_system_pvp_summary"))
			{
				try (ResultSet rset = ps.executeQuery())
				{
					while (rset.next())
					{
						// get only existed summaries:
						PvpSummary kps = getKillerPvpSummary(rset.getInt("killer_id"), true, false);

						kps.setPvpExp(rset.getLong("pvp_exp"));
						kps.setTotalWarKills(rset.getInt("total_war_kills"));
						kps.setTotalWarKillsLegal(rset.getInt("total_war_kills_legal"));
						kps.setMaxRankId(rset.getInt("max_rank_id"));

						kps.setDbStatus(DBStatus.NONE);

						kps.updateRankId();
					}
				}
			}
		}
		catch (SQLException e)
		{
			log.log(Level.WARNING, e.getMessage());
		}
	}
	
	public int[] updateDB()
	{
		int[] result =
		{
			0,
			0,
			0
		};
		
		int insertCount = 0; // count of insert queries
		int updateCount = 0; // count of update queries
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
		Statement statement = con.createStatement())
		{
			// search new or updated fields in VictimPvpTable:
			for (Map.Entry<Integer, PvpSummary> e : _pvpTable.entrySet())
			{
				PvpSummary kps = e.getValue();
				
				if (kps == null)
					break;
				
				Map<Integer, Pvp> victimPvpTable = kps.getVictimPvpTable();
				
				for (Map.Entry<Integer, Pvp> f : victimPvpTable.entrySet())
				{
					Pvp pvp = f.getValue();
					
					if (pvp == null)
						break;
					
					if (pvp.getDbStatus() == DBStatus.UPDATED)
					{
						// rank_pvp_system_pvp:
						statement.addBatch("UPDATE rank_pvp_system_pvp SET kills=" + pvp.getKills() + ", kills_today=" + pvp.getKillsToday() + ", kills_legal=" + pvp.getKillsLegal() + ", kills_today_legal=" + pvp.getKillsLegalToday() + ", rank_points=" + pvp.getRankPoints() + ", rank_points_today=" + pvp.getRankPointsToday() + ", kill_time=" + pvp.getKillTime() + ", kill_day=" + pvp.getKillDay() + " WHERE killer_id=" + kps.getKillerId() + " AND victim_id=" + pvp.getVictimId());
						pvp.setDbStatus(DBStatus.NONE); // it is after query because PvP is updating in real time.
						updateCount++;
					}
					else if (pvp.getDbStatus() == DBStatus.INSERTED)
					{
						// rank_pvp_system_pvp:
						statement.addBatch("INSERT INTO rank_pvp_system_pvp (killer_id, victim_id, kills, kills_today, kills_legal, kills_today_legal, rank_points, rank_points_today, kill_time, kill_day) VALUES (" + kps.getKillerId() + ", " + pvp.getVictimId() + ", " + pvp.getKills() + ", " + pvp.getKillsToday() + ", " + pvp.getKillsLegal() + ", " + pvp.getKillsLegalToday() + ", " + pvp.getRankPoints() + ", " + pvp.getRankPointsToday() + ", " + pvp.getKillTime() + ", " + pvp.getKillDay() + ")");
						pvp.setDbStatus(DBStatus.NONE);
						insertCount++;
					}
				}
				
				if (kps.getDbStatus() == DBStatus.UPDATED)
				{
					// rank_pvp_system_pvp_summary:
					statement.addBatch("UPDATE rank_pvp_system_pvp_summary SET pvp_exp=" + kps.getPvpExp() + ", total_war_kills=" + kps.getTotalWarKills() + ", total_war_kills_legal=" + kps.getTotalWarKillsLegal() + ", max_rank_id=" + kps.getMaxRankId() + " WHERE killer_id=" + kps.getKillerId());
					kps.setDbStatus(DBStatus.NONE);
					updateCount++;
				}
				else if (kps.getDbStatus() == DBStatus.INSERTED)
				{
					// rank_pvp_system_pvp_summary:
					statement.addBatch("INSERT INTO rank_pvp_system_pvp_summary (killer_id, pvp_exp, total_war_kills, total_war_kills_legal, max_rank_id) VALUES (" + kps.getKillerId() + ", " + kps.getPvpExp() + ", " + kps.getTotalWarKills() + ", " + kps.getTotalWarKillsLegal() + ", " + kps.getMaxRankId() + ")");
					kps.setDbStatus(DBStatus.NONE);
					insertCount++;
				}
			}
			
			statement.executeBatch();
			statement.close();
		}
		catch (SQLException e)
		{
			log.log(Level.WARNING, e.getMessage());
			result[0] = -1;
		}
		
		result[1] = insertCount;
		result[2] = updateCount;
		
		return result;
	}
	
	private static void cleanPvpTable()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			// calculate ignore time:
			Calendar c = Calendar.getInstance();
			long ignoreTime = c.getTimeInMillis() - Config.DATABASE_CLEANER_REPEAT_TIME;
			
			Statement statement = con.createStatement();
			
			statement.addBatch("DELETE FROM rank_pvp_system_pvp WHERE (SELECT (lastAccess) FROM characters WHERE " + RankLoader.CHAR_ID_COLUMN_NAME + " = killer_id) < " + ignoreTime);
			statement.addBatch("DELETE FROM rank_pvp_system_pvp_summary WHERE (SELECT (lastAccess) FROM characters WHERE " + RankLoader.CHAR_ID_COLUMN_NAME + " = killer_id) < " + ignoreTime);
			
			statement.executeBatch();
			statement.close();
		}
		catch (SQLException e)
		{
			log.log(Level.WARNING, e.getMessage());
		}
		
		log.log(Level.INFO, " - PvpTable: Data older than " + Math.round((double) Config.DATABASE_CLEANER_REPEAT_TIME / (double) 86400000) + " day(s) removed.");
	}
	
	private static class PvpTableSchedule implements Runnable
	{
		public PvpTableSchedule()
		{
			
		}
		
		@Override
		public void run()
		{
			if (!TopTable.getInstance().isUpdating())
			{
				int[] up = PvpTable.getInstance().updateDB();
				
				if (up[0] == 0)
				{
					log.log(Level.INFO, "PvpTable: Data updated [" + up[1] + " inserts and " + up[2] + " updates] <<< Next update at " + RPSUtil.timeToString(Calendar.getInstance().getTimeInMillis() + Config.PVP_TABLE_UPDATE_INTERVAL));
				}
				
				// update RPC here:
				if (Config.RPC_REWARD_ENABLED || Config.RANK_RPC_ENABLED || Config.RPC_TABLE_FORCE_UPDATE_ENABLED)
					RPCTable.getInstance().updateDB();
				
				ThreadPoolManager.getInstance().scheduleGeneral(new PvpTableSchedule(), Config.PVP_TABLE_UPDATE_INTERVAL);
			}
			else
			{
				log.log(Level.INFO, "PvpTable: Waiting for update. <<< Next try for 30 seconds.");
				ThreadPoolManager.getInstance().scheduleGeneral(new PvpTableSchedule(), 30000);
			}
		}
	}
}