package Extensions.RankSystem;

import java.util.Calendar;

public class Pvp
{
	private int _victimId = 0;
	private int _kills = 0;
	private int _killsToday = 0;
	private int _killsLegal = 0;
	private int _killsLegalToday = 0;
	private long _rankPoints = 0;
	private long _rankPointsToday = 0;
	
	private long _killTime = 0; // store date and time, used in anti-farm options.
	private long _killDay = 0; // store only date without time.
	
	private byte _dbStatus = DBStatus.INSERTED;
	
	public void increaseKills()
	{
		_kills++;
		onUpdate();
	}
	
	public void increaseKillsToday()
	{
		_killsToday++;
		onUpdate();
	}
	
	public void increaseKillsLegal()
	{
		_killsLegal++;
		onUpdate();
	}
	
	public void increaseKillsLegalToday()
	{
		_killsLegalToday++;
		onUpdate();
	}
	
	public void increaseRankPointsBy(long rankPoints)
	{
		_rankPoints += rankPoints;
		onUpdate();
	}
	
	public void increaseRankPointsTodayBy(long rankPoints)
	{
		_rankPointsToday += rankPoints;
		onUpdate();
	}
	
	public int getVictimId()
	{
		return _victimId;
	}
	
	public void setVictimId(int victimId)
	{
		_victimId = victimId;
		onUpdate();
	}
	
	public int getKills()
	{
		return _kills;
	}
	
	public void setKills(int kills)
	{
		_kills = kills;
		onUpdate();
	}
	
	public int getKillsToday()
	{
		if (!checkToday())
			return 0;
		
		return _killsToday;
	}
	
	public void setKillsToday(int killsToday)
	{
		_killsToday = killsToday;
		onUpdate();
	}
	
	public int getKillsLegal()
	{
		return _killsLegal;
	}
	
	public void setKillsLegal(int killsLegal)
	{
		_killsLegal = killsLegal;
		onUpdate();
	}
	
	public int getKillsLegalToday()
	{
		if (!checkToday())
			return 0;
		
		return _killsLegalToday;
	}
	
	public void setKillsLegalToday(int killsLegalToday)
	{
		_killsLegalToday = killsLegalToday;
		onUpdate();
	}
	
	public long getRankPoints()
	{
		return _rankPoints;
	}
	
	public void setRankPoints(long rankPoints)
	{
		_rankPoints = rankPoints;
		onUpdate();
	}
	
	public long getRankPointsToday()
	{
		if (!checkToday())
			return 0;
		
		return _rankPointsToday;
	}
	
	public void setRankPointsToday(long rankPointsToday)
	{
		_rankPointsToday = rankPointsToday;
		onUpdate();
	}
	
	public long getKillTime()
	{
		return _killTime;
	}
	
	public void setKillTime(long killTime)
	{
		_killTime = killTime;
		onUpdate();
	}
	
	public long getKillDay()
	{
		return _killDay;
	}
	
	public void setKillDay(long killDay)
	{
		_killDay = killDay;
		onUpdate();
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
	
	private boolean checkToday()
	{
		Calendar c = Calendar.getInstance();
		c.set(Calendar.MILLISECOND, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.HOUR, 0);
		long systemDay = c.getTimeInMillis(); // date
		if (_killDay != systemDay)
			return false;
		return true;
	}
	
	public void resetDailyFields()
	{
		_killsToday = 0;
		_killsLegalToday = 0;
		_rankPointsToday = 0;
	}
}