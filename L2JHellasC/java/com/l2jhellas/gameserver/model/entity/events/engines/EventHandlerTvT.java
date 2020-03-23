package com.l2jhellas.gameserver.model.entity.events.engines;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.model.entity.events.TvT;

public class EventHandlerTvT
{
	private static final Logger _log = Logger.getLogger(EventHandlerTvT.class.getName());
	public List<Long> datesTvT = new ArrayList<>();
	
	public void startHandler()
	{
		loadConfisTvT(false);
		getNextTimeStampTvT();
		_log.info(EventHandlerTvT.class.getSimpleName() + ": TvT handler initiated.");
	}
	
	public void loadConfisTvT(boolean NextDay)
	{
		datesTvT.clear();
		for (String times : Config.TVT_EVENT_TIMES.split(","))
		{
			String[] timesSplited = times.split(":");
			int hour = Integer.parseInt(timesSplited[0]);
			int minute = Integer.parseInt(timesSplited[1]);
			Calendar time = Calendar.getInstance();
			if (!NextDay)
			{
				time.set(Calendar.HOUR_OF_DAY, hour);
				time.set(Calendar.MINUTE, minute);
			}
			else
			{
				time.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + 1);
				time.set(Calendar.HOUR_OF_DAY, hour);
				time.set(Calendar.MINUTE, minute);
			}
			datesTvT.add(time.getTimeInMillis());
		}
	}
	
	public void getNextTimeStampTvT()
	{
		boolean found = false;
		for (Long stamp : datesTvT)
		{
			if (stamp > System.currentTimeMillis())
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
				{
					@Override
					public void run()
					{
						TvT.loadData();
						TvT.autoEvent();
						getNextTimeStampTvT();
					}
				}, stamp - System.currentTimeMillis());
				found = true;
				break;
			}
		}
		if (!found)
		{
			loadConfisTvT(true);
			getNextTimeStampTvT();
		}
	}
}
