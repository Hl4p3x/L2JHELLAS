package com.l2jhellas.gameserver.taskmanager;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2RaidBossInstance;

public class DecayTaskManager
{
	protected static final Logger _log = Logger.getLogger(DecayTaskManager.class.getName());
	protected final Map<L2Character, Long> _decayTasks = new ConcurrentHashMap<>();
	
	private static DecayTaskManager _instance;
	
	public DecayTaskManager()
	{
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new DecayScheduler(), 10000, 5000);
	}
	
	public static DecayTaskManager getInstance()
	{
		if (_instance == null)
			_instance = new DecayTaskManager();
		
		return _instance;
	}
	
	public void addDecayTask(L2Character actor)
	{
		_decayTasks.put(actor, System.currentTimeMillis());
	}
	
	public void addDecayTask(L2Character actor, int interval)
	{
		_decayTasks.put(actor, System.currentTimeMillis() + interval);
	}
	
	public void cancelDecayTask(L2Character actor)
	{
		try
		{
			_decayTasks.remove(actor);
		}
		catch (NoSuchElementException e)
		{
		}
	}
	
	private class DecayScheduler implements Runnable
	{
		protected DecayScheduler()
		{
			// Do nothing
		}
		
		@Override
		public void run()
		{
			Long current = System.currentTimeMillis();
			int delay;
			try
			{
				if (_decayTasks != null)
					for (L2Character actor : _decayTasks.keySet())
					{
						if (actor instanceof L2RaidBossInstance)
							delay = 30000;
						else
							delay = 8500;
						if ((current - _decayTasks.get(actor)) > delay)
						{
							actor.onDecay();
							_decayTasks.remove(actor);
						}
					}
			}
			catch (Throwable e)
			{
				_log.warning(DecayTaskManager.class.getName() + ": Decay task failed");
				if (Config.DEVELOPER)
					e.printStackTrace();
			}
		}
	}
	
	@Override
	public String toString()
	{
		String ret = "============= DecayTask Manager Report ============\r\n";
		ret += "Tasks count: " + _decayTasks.size() + "\r\n";
		ret += "Tasks dump:\r\n";
		
		Long current = System.currentTimeMillis();
		for (L2Character actor : _decayTasks.keySet())
		{
			ret += "Class/Name: " + actor.getClass().getSimpleName() + "/" + actor.getName() + " decay timer: " + (current - _decayTasks.get(actor)) + "\r\n";
		}
		
		return ret;
	}
}