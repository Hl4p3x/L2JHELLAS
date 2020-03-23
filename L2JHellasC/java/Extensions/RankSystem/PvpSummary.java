package Extensions.RankSystem;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jhellas.Config;

public class PvpSummary
{
	private int _killerId = 0; // killer id
	private int _rankId = 0; // id of current rank
	
	// calculated fields:
	private int _totalKills = 0; // sum of kill's
	private int _totalKillsLegal = 0; // sum of legal kill's
	private long _totalRankPoints = 0; // sum of rank point's
	private long _totalRankPointsToday = 0; // sum of rank point's in this day
	
	// stored fields (in DB):
	private long _pvpExp = 0; // killer PvP experience. [1 RP == 1 EXP]
	private int _totalWarKills = 0; // sum of kill's on victims from war clan
	private int _totalWarKillsLegal = 0; // sum of legal kill's on victims from war clan
	private int _maxRankId = 0; // maximum rank obtained by player. It is never decrease (reward security).
	
	private static final byte FULL = 1;
	private static final byte BASIC = 2;
	private static final byte CONSTANT = 3;
	private static final byte FRACTION = 4;
	
	private Map<Integer, Pvp> _victimPvpTable = new ConcurrentHashMap<>();
	
	private long _lastKillTime = 0; // store last <legal> kill time
	private byte _dbStatus = DBStatus.INSERTED;
	
	public void increasePvpExp(long value)
	{
		if (value > 0)
		{
			_pvpExp += value;
			updateRankId();
			onUpdate();
		}
	}
	
	public int decreasePvpExpBy(int[] values)
	{
		if (values == null || values.length != 4)
			return 0;
		
		int value = 0;
		
		if (Config.PVP_EXP_DECREASE_METHOD == FULL)
			value = values[0];
		else if (Config.PVP_EXP_DECREASE_METHOD == BASIC)
			value = values[0] - values[1] - values[2] - values[3];
		else if (Config.PVP_EXP_DECREASE_METHOD == CONSTANT)
			value = Config.PVP_EXP_DECREASE_CONSTANT;
		else if (Config.PVP_EXP_DECREASE_METHOD == FRACTION)
			value = (int) ((Config.PVP_EXP_DECREASE_FRACTION) * values[0]);
		
		if (value > 0)
		{
			_pvpExp -= value;
			
			if (_pvpExp < 0)
				_pvpExp = 0;
			
			updateRankId();
			onUpdate();
		}
		
		return value;
	}
	
	public void addTotalKills(int kills)
	{
		_totalKills += kills;
	}
	
	public void addTotalKillsLegal(int killsLegal)
	{
		_totalKillsLegal += killsLegal;
		onUpdate();
	}
	
	public void addTotalRankPoints(long rankPoints)
	{
		_totalRankPoints += rankPoints;
	}
	
	public void addTotalRankPointsToday(long rankPointsToday)
	{
		_totalRankPointsToday += rankPointsToday;
	}
	
	public void addTotalWarKills(int warKills)
	{
		_totalWarKills += warKills;
		onUpdate();
	}
	
	public void addTotalWarKillsLegal(int warKillsLegal)
	{
		_totalWarKillsLegal += warKillsLegal;
	}
	
	public int getKillerId()
	{
		return _killerId;
	}
	
	public void setKillerId(int killerId)
	{
		_killerId = killerId;
	}
	
	public int getTotalKills()
	{
		return _totalKills;
	}
	
	public void setTotalKills(int totalKills)
	{
		_totalKills = totalKills;
	}
	
	public int getTotalKillsLegal()
	{
		return _totalKillsLegal;
	}
	
	public void setTotalKillsLegal(int totalKillsLegal)
	{
		_totalKillsLegal = totalKillsLegal;
		onUpdate();
	}
	
	public long getTotalRankPoints()
	{
		return _totalRankPoints;
	}
	
	public void setTotalRankPoints(long totalRankPoints)
	{
		_totalRankPoints = totalRankPoints;
	}
	
	public void setTotalRankPointsOnly(long totalRankPoints)
	{
		_totalRankPoints = totalRankPoints;
	}
	
	public long getTotalRankPointsToday()
	{
		return _totalRankPointsToday;
	}
	
	public void setTotalRankPointsToday(long totalRankPointsToday)
	{
		_totalRankPointsToday = totalRankPointsToday;
	}
	
	public int getTotalWarKills()
	{
		return _totalWarKills;
	}
	
	public void setTotalWarKills(int totalWarKills)
	{
		_totalWarKills = totalWarKills;
		onUpdate();
	}
	
	public int getTotalWarKillsLegal()
	{
		return _totalWarKillsLegal;
	}
	
	public void setTotalWarKillsLegal(int totalWarKillsLegal)
	{
		_totalWarKillsLegal = totalWarKillsLegal;
	}
	
	public int getRankId()
	{
		return _rankId;
	}
	
	public Rank getRank()
	{
		return RankTable.getInstance().getRankById(getRankId());
	}
	
	public void setRankId(int rankId)
	{
		_rankId = rankId;
	}
	
	public void updateRankId()
	{
		Map<Integer, Rank> list = RankTable.getInstance().getRankList();
		
		if (list == null)
			return;
		
		// if Pvp Exp equals 0 return minimum rank:
		if (_pvpExp <= 0)
		{
			Rank rank = list.get(1);
			
			if (rank != null)
			{
				_rankId = rank.getId();
				return;
			}
		}
		
		int rankId = 1; // ranks starts from id = 1.
		for (Map.Entry<Integer, Rank> e : list.entrySet())
		{
			Rank rank = e.getValue();
			
			if (rank != null)
			{
				// ranks are checked from rankId == 1, so if the pvpExp is lower than minExp then we found the rank.
				// last iteration of this loop returns the highest rankId.
				if (_pvpExp < rank.getMinExp())
					break;
				
				rankId = rank.getId();
			}
		}
		
		_rankId = rankId;
		
		// update the max rank id
		if (rankId > _maxRankId)
		{
			_maxRankId = rankId;
			onUpdate();
		}
	}
	
	public void updateDailyStats(long systemDay)
	{
		long totalRankPointsToday = 0;
		
		for (Map.Entry<Integer, Pvp> e : _victimPvpTable.entrySet())
		{
			Pvp pvp = e.getValue();
			
			if (pvp != null)
			{
				if (pvp.getKillDay() == systemDay)
					totalRankPointsToday += pvp.getRankPointsToday();
				else
					pvp.resetDailyFields();
			}
		}
		
		_totalRankPointsToday = totalRankPointsToday;
	}
	
	public Map<Integer, Pvp> getVictimPvpTable()
	{
		return _victimPvpTable;
	}
	
	public void setVictimPvpTable(Map<Integer, Pvp> victimPvpTable)
	{
		_victimPvpTable = victimPvpTable;
	}
	
	public boolean addVictimPvpOnLoadFromDB(Pvp pvp)
	{
		// add PvP:
		_victimPvpTable.put(pvp.getVictimId(), pvp);
		
		// update killer pvp stats (only calcualted fields):
		addTotalKills(pvp.getKills());
		addTotalKillsLegal(pvp.getKillsLegal());
		
		addTotalRankPoints(pvp.getRankPoints());
		addTotalRankPointsToday(pvp.getRankPointsToday());
		
		// set last kill time:
		if (pvp.getKillTime() > getLastKillTime())
			_lastKillTime = pvp.getKillTime();
		
		return true;
	}
	
	public long getLastKillTime()
	{
		return _lastKillTime;
	}
	
	public void setLastKillTime(long lastKillTime)
	{
		_lastKillTime = lastKillTime;
	}
	
	public long getPvpExp()
	{
		return _pvpExp;
	}
	
	public void setPvpExp(long pvpExp)
	{
		_pvpExp = pvpExp;
	}
	
	public int getMaxRankId()
	{
		return _maxRankId;
	}
	
	public void setMaxRankId(int maxRankId)
	{
		_maxRankId = maxRankId;
	}
	
	public byte getDbStatus()
	{
		return _dbStatus;
	}
	
	public void setDbStatus(byte dbStatus)
	{
		_dbStatus = dbStatus;
	}
	
	private void onUpdate()
	{
		if (_dbStatus == DBStatus.NONE)
			_dbStatus = DBStatus.UPDATED;
	}
}