package Extensions.RankSystem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import Extensions.RankSystem.Util.RPSUtil;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class TopTable
{
	public static final Logger log = Logger.getLogger(TopTable.class.getSimpleName());
	
	public static final int TOP_LIMIT = 100; // limit for top list players data.
	
	private static TopTable _instance = null;
	
	private boolean _isUpdating = false;
	
	public static final long DAY = 86400000;
	public static final long HOUR = 3600000;
	
	private Map<Integer, TopField> _topKillsTable = new LinkedHashMap<>();
	
	private Map<Integer, TopField> _topGatherersTable = new LinkedHashMap<>();
	
	private static long _lastUpdateTime = 0; // used in initialization table
	private static long _nextUpdateTime = 0; // used in CB
	
	private TopTable()
	{
		load();
	}
	
	public static TopTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new TopTable();
		}
		
		return _instance;
	}
	
	private void load()
	{
		long startTime = Calendar.getInstance().getTimeInMillis();
		
		String info = "Loaded";
		
		// initialize _lastUpdateTime:
		loadLastUpdate();
		
		// load times:
		long[] times = calculateNextUpdateTime();
		
		TopTable.setNextUpdateTime(times[2]);
		
		// initialize Top Tables:
		// if last update time < previous update time
		if (_lastUpdateTime < times[3])
		{
			updateTopTable();
			info = "Updated";
		}
		else
		{
			restoreTopTable();
		}
		
		// update schedule:
		ThreadPoolManager.getInstance().scheduleGeneral(new TopTableSchedule(), times[1]);
		
		long endTime = Calendar.getInstance().getTimeInMillis();
		
		log.log(Level.INFO, " - TopTable: Data " + info + ". " + _topKillsTable.size() + " killers and " + _topGatherersTable.size() + " gatherers in " + (endTime - startTime) + " ms.");
		log.log(Level.INFO, " - TopTable: Next update on " + RPSUtil.dateToString(times[2]) + " at " + RPSUtil.timeToString(times[2]) + ".");
	}
	
	public static long[] calculateNextUpdateTime()
	{
		// initialize current & update time (today update):
		Calendar uTime = Calendar.getInstance();
		long currentTime = uTime.getTimeInMillis(); // date & time
		
		uTime.set(Calendar.HOUR_OF_DAY, 0);
		uTime.set(Calendar.MINUTE, 0);
		uTime.set(Calendar.SECOND, 0);
		uTime.set(Calendar.MILLISECOND, 0);
		
		long currentTimeNoHour = uTime.getTimeInMillis(); // date
		long currentDayTime = currentTime - currentTimeNoHour - HOUR; // time
		long nextUpdateTime = 0; // date & time
		long prevUpdateTime = 0; // date & time
		
		// get next and previous update time:
		prevUpdateTime = (currentTimeNoHour - DAY) + Config.TOP_TABLE_UPDATE_TIMES.get(Config.TOP_TABLE_UPDATE_TIMES.size() - 1) + HOUR;
		
		for (Long time : Config.TOP_TABLE_UPDATE_TIMES)
		{
			// the TOP_TABLE_UPDATE_TIMES are ordered from 0 to 24h in ms.
			if (currentDayTime < time)
			{
				nextUpdateTime = currentTimeNoHour + time + HOUR;
				break;
			}
			
			prevUpdateTime = currentTimeNoHour + time + HOUR;
		}
		
		// get next update time for next day update exception:
		if (nextUpdateTime == 0)
		{
			// nextUpdateTime = current time + (24h - currentDayTime) + firstDayUpdateTime:
			nextUpdateTime = currentTime + (DAY - currentDayTime) + Config.TOP_TABLE_UPDATE_TIMES.get(0);
		}
		
		// calculate time to next update:
		long timeToNextUpdate = nextUpdateTime - currentTime; // time
		
		long[] a = new long[4];
		a[0] = currentTime;
		a[1] = timeToNextUpdate;
		a[2] = nextUpdateTime;
		a[3] = prevUpdateTime;
		
		return a;
	}
	
	protected boolean updateTopTable()
	{
		boolean ok = false;
		
		// lock table:
		setUpdating(true);
		
		// clear tables:
		_topKillsTable.clear();
		_topGatherersTable.clear();
		
		// get minimum allowed time:
		long sysTime = Calendar.getInstance().getTimeInMillis() - Config.TOP_LIST_IGNORE_TIME_LIMIT;

		// order and load top killers & gatherers from model:
		Map<Integer, PvpSummary> pvpList = PvpTable.getInstance().getPvpTable();
		
		if (pvpList ==null)	
			return false;
		
		Map<Integer, TopField> tmpTopKillsTable = new LinkedHashMap<>();
		Map<Integer, TopField> tmpTopGatherersTable = new LinkedHashMap<>();
		
		int KillPosition = 0;
		int PointPosition = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
		PreparedStatement statement = con.prepareStatement("SELECT char_name as name, base_class as base_class, level as level FROM characters WHERE " + RankLoader.CHAR_ID_COLUMN_NAME + " = ?"))
		{
			// for TopKillersTable:
			for (int i = 1; i <= TOP_LIMIT; i++)
			{
				int maxKills = 0;
				int bestKiller = 0;
				
				long maxPoints = 0;
				int bestGatherer = 0;
				
				for (Map.Entry<Integer, PvpSummary> e : pvpList.entrySet())
				{
					PvpSummary kps = e.getValue();
					
					if ((Config.TOP_LIST_IGNORE_TIME_LIMIT == 0) || (kps.getLastKillTime() >= sysTime)) // if last kill is in TOP_LIST_IGNORE_TIME_LIMIT.
					{
						if (!tmpTopKillsTable.containsKey(kps.getKillerId())) // don't check already added killer.
						{
							if ((kps.getTotalKillsLegal() > maxKills) && (kps.getTotalKillsLegal() > 0)) // finding the best.
							{
								maxKills = kps.getTotalKillsLegal();
								bestKiller = kps.getKillerId();
							}
						}
						
						if (!tmpTopGatherersTable.containsKey(kps.getKillerId())) // don't check already added gatherer.
						{
							if ((kps.getTotalRankPoints() > maxPoints) && (kps.getTotalRankPoints() > 0)) // finding the best.
							{
								maxPoints = kps.getTotalRankPoints();
								bestGatherer = kps.getKillerId();
							}
						}
					}
				}
				
				// if killer found:
				if (bestKiller > 0)
				{
					KillPosition++;
					
					// if founded, add him to list:
					TopField tf = new TopField();
					
					tf.setCharacterId(bestKiller);
					tf.setValue(maxKills);
					tf.setTopPosition(KillPosition);
					
					// get character data:
					statement.setInt(1, bestKiller);
					
					try(ResultSet rset = statement.executeQuery())
					{
						while (rset.next())
						{
							tf.setCharacterName(rset.getString("name"));
							tf.setCharacterLevel(rset.getInt("level"));
							tf.setCharacterBaseClassId(rset.getInt("base_class"));
						}
					}
					// add this killer on temporary top list:
					tmpTopKillsTable.put(bestKiller, tf);
				}
				
				// if any best killer not found, break action:
				if (bestGatherer > 0)
				{
					PointPosition++;
					
					// if founded, add him to list:
					TopField tf = new TopField();
					
					tf.setCharacterId(bestGatherer);
					tf.setValue(maxPoints);
					tf.setTopPosition(PointPosition);
					
					// get character data:
					try(PreparedStatement ps = con.prepareStatement("SELECT char_name as name, base_class as base_class, level as level FROM characters WHERE " + RankLoader.CHAR_ID_COLUMN_NAME + " = ?"))
					{
						ps.setInt(1, bestGatherer);

						try(ResultSet rset = ps.executeQuery())
						{
							while (rset.next())
							{
								tf.setCharacterName(rset.getString("name"));
								tf.setCharacterLevel(rset.getInt("level"));
								tf.setCharacterBaseClassId(rset.getInt("base_class"));
							}
						}
					}
					// add this gatherer on top list:
					tmpTopGatherersTable.put(bestGatherer, tf);
				}			
			}
			
			// TODO reorder the tmpTopKillsTable and tmpTopGatherersTable here, can be required in special situations.
			// add new top tables:
			setTopKillsTable(tmpTopKillsTable);
			setTopGatherersTable(tmpTopGatherersTable);

			try(PreparedStatement ps = con.prepareStatement("SELECT * FROM rank_pvp_system_top_table"))
			{
				// clear Top Table:
				ps.addBatch("DELETE FROM rank_pvp_system_top_table");

				// insert new Top Killers list:
				for (Map.Entry<Integer, TopField> e : _topKillsTable.entrySet())
				{
					ps.addBatch("INSERT INTO rank_pvp_system_top_table (position, player_id, value, table_id) VALUES (" + e.getValue().getTopPosition() + "," + e.getValue().getCharacterId() + "," + e.getValue().getValue() + ",1)");
				}

				// insert new Top Killers list:
				for (Map.Entry<Integer, TopField> e : _topGatherersTable.entrySet())
				{
					ps.addBatch("INSERT INTO rank_pvp_system_top_table (position, player_id, value, table_id) VALUES (" + e.getValue().getTopPosition() + "," + e.getValue().getCharacterId() + "," + e.getValue().getValue() + ",2)");
				}

				ps.executeBatch();
				ps.close();
			}

			// save time of update in rank_pvp_system_options table:
			long calendar = Calendar.getInstance().getTimeInMillis();
			try(PreparedStatement ps = con.prepareStatement("UPDATE rank_pvp_system_options SET option_value_long=? WHERE option_id=1"))
			{
				ps.setLong(1, calendar);
				ps.execute();
				_lastUpdateTime = calendar;
			}
			
			ok = true;	
		}
		catch (SQLException e)
		{
			log.log(Level.WARNING, e.getMessage()+"poutsa");
			
			// clear tables:
			_topKillsTable.clear();
			_topGatherersTable.clear();
			
			ok = false;
		}
		
		// unlock table:
		setUpdating(false);
		return ok;
		
	}
	
	private void restoreTopTable()
	{
		
		// clear tables:
		_topKillsTable.clear();
		_topGatherersTable.clear();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
		PreparedStatement statement = con.prepareStatement("SELECT position, player_id, value, table_id, char_name as name, base_class as base_class, level as level FROM rank_pvp_system_top_table JOIN characters ON rank_pvp_system_top_table.player_id = characters." + RankLoader.CHAR_ID_COLUMN_NAME + " ORDER BY position"))
		{
			// get top killers:
			try(ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					TopField tf = new TopField();

					tf.setCharacterId(rset.getInt("player_id"));
					tf.setValue(rset.getLong("value"));
					tf.setTopPosition(rset.getInt("position"));
					tf.setCharacterName(rset.getString("name"));
					tf.setCharacterLevel(rset.getInt("level"));
					tf.setCharacterBaseClassId(rset.getInt("base_class"));

					if (rset.getInt("table_id") == 1)
						_topKillsTable.put(rset.getInt("player_id"), tf);
					else if (rset.getInt("table_id") == 2)
						_topGatherersTable.put(rset.getInt("player_id"), tf);
				}
			}
		}
		catch (SQLException e)
		{
			log.log(Level.WARNING, e.getMessage());
		}
	}
	
	private static void loadLastUpdate()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
		PreparedStatement statement = con.prepareStatement("SELECT option_value_long FROM rank_pvp_system_options WHERE option_id=1"))
		{
			// get top killers:
			try(ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					_lastUpdateTime = (rset.getLong("option_value_long"));
				}
			}
		}
		catch (SQLException e)
		{
			log.log(Level.WARNING, e.getMessage());
		}
	}
	
	public Map<Integer, TopField> getTopKillsTable()
	{
		return _topKillsTable;
	}
	
	public void setTopKillsTable(Map<Integer, TopField> topKillsTable)
	{
		_topKillsTable = topKillsTable;
	}
	
	public Map<Integer, TopField> getTopGatherersTable()
	{
		return _topGatherersTable;
	}
	
	public void setTopGatherersTable(Map<Integer, TopField> topGatherersTable)
	{
		_topGatherersTable = topGatherersTable;
	}
	
	public boolean isUpdating()
	{
		return _isUpdating;
	}
	
	public void setUpdating(boolean isUpdating)
	{
		_isUpdating = isUpdating;
	}
	
	public static long getLastUpdateTime()
	{
		return _lastUpdateTime;
	}
	
	public static void setLastUpdateTime(long lastUpdateTime)
	{
		_lastUpdateTime = lastUpdateTime;
	}
	
	public static long getNextUpdateTime()
	{
		return _nextUpdateTime;
	}
	
	public static void setNextUpdateTime(long _nextUpdateTime)
	{
		TopTable._nextUpdateTime = _nextUpdateTime;
	}
	
	private static class TopTableSchedule implements Runnable
	{
		public TopTableSchedule()
		{
			
		}
		
		@Override
		public void run()
		{
			long[] times = TopTable.calculateNextUpdateTime();
			
			TopTable.setNextUpdateTime(times[2]);
			
			if (!TopTable.getInstance().isUpdating() && TopTable.getInstance().updateTopTable())
			{
				log.log(Level.INFO, "TopTable: Data updated in " + (Calendar.getInstance().getTimeInMillis() - times[0]) + " ms <<< Next update on " + RPSUtil.dateToString(times[2]) + " at " + RPSUtil.timeToString(times[2]));
				
				ThreadPoolManager.getInstance().scheduleGeneral(new TopTableSchedule(), times[1]);
			}
			else
			{
				log.log(Level.INFO, "TopTable: Data update failed! <<< Next try for 5 minutes.");
				ThreadPoolManager.getInstance().scheduleGeneral(new TopTableSchedule(), 300000);
			}
		}
	}
}