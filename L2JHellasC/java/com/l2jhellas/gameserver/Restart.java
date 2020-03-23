package com.l2jhellas.gameserver;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Logger;

import com.l2jhellas.Config;

public class Restart
{
	protected static final Logger _log = Logger.getLogger(Restart.class.getName());
	private static Restart _bitching = null;
	private Calendar NextRestart;
	private final SimpleDateFormat format = new SimpleDateFormat("HH:mm");
	
	public static Restart getInstance()
	{
		if (_bitching == null)
			_bitching = new Restart();
		return _bitching;
	}
	
	public String getRestartNextTime()
	{
		if (NextRestart.getTime() != null)
			return format.format(NextRestart.getTime());
		return "Something went wrong";
	}
	
	public void StartCalculationOfNextRestartTime()
	{
		_log.info(Restart.class.getName() + "[Restart System]: System actived");
		try
		{
			Calendar currentTime = Calendar.getInstance();
			Calendar StartTime = null;
			long flush2 = 0, datime = 0;
			int count = 0;
			for (String timeOfDay : Config.RESTART_INTERVAL_BY_TIME_OF_DAY)
			{
				StartTime = Calendar.getInstance();
				StartTime.setLenient(true);
				String[] splitTimeOfDay = timeOfDay.split(":");
				StartTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitTimeOfDay[0]));
				StartTime.set(Calendar.MINUTE, Integer.parseInt(splitTimeOfDay[1]));
				StartTime.set(Calendar.SECOND, 00);
				if (StartTime.getTimeInMillis() < currentTime.getTimeInMillis())
				{
					StartTime.add(Calendar.DAY_OF_MONTH, 1);
				}
				datime = StartTime.getTimeInMillis() - currentTime.getTimeInMillis();
				if (count == 0)
				{
					flush2 = datime;
					NextRestart = StartTime;
				}
				if (datime < flush2)
				{
					flush2 = datime;
					NextRestart = StartTime;
				}
				count++;
			}
			_log.info(Restart.class.getName() + "[Restart System]: Next Restart Time: " + NextRestart.getTime().toString());
			ThreadPoolManager.getInstance().scheduleGeneral(new StartRestartTask(), flush2);
		}
		catch (Exception e)
		{
			_log.warning(Restart.class.getName() + ": [Restart System]: The automatic restart system presented error while loading the configs.");
		}
	}
	
	class StartRestartTask implements Runnable
	{
		@Override
		public void run()
		{
			_log.info(Restart.class.getSimpleName() + ": Start automated restart GameServer.");
			Shutdown.getInstance().autoRestart(Config.RESTART_SECONDS);
		}
	}
}