package com.l2jhellas.gameserver.model.entity.events.engines;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.model.entity.events.CTF;

public class EventHandlerCtf
{
	private static final Logger _log = Logger.getLogger(EventHandlerCtf.class.getName());
	public List<Long> datesCtf = new ArrayList<>();
	
	public void startHandler()
	{
		loadConfisCtf(false);
		getNextTimeStampCTF();
		_log.info(EventHandlerCtf.class.getSimpleName() + ": Ctf handler initiated");
	}
	
	public void loadConfisCtf(boolean NextDay)
	{
		datesCtf.clear();
		for (String times : Config.CTF_EVENT_TIMES.split(","))
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
			datesCtf.add(time.getTimeInMillis());
		}
	}
	
	public void getNextTimeStampCTF()
	{
		boolean found = false;
		for (Long stamp : datesCtf)
		{
			if (stamp > System.currentTimeMillis())
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
				{
					@Override
					public void run()
					{
						CTF.loadData();
						CTF.autoEvent();
						getNextTimeStampCTF();
					}
				}, stamp - System.currentTimeMillis());
				found = true;
				break;
			}
		}
		if (!found)
		{
			loadConfisCtf(true);
			getNextTimeStampCTF();
		}
	}
}
