package com.l2jhellas.gameserver.controllers;

import java.util.Calendar;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.instancemanager.DayNightSpawnManager;
import com.l2jhellas.gameserver.model.actor.L2Character;

public class GameTimeController extends Thread
{
	static final Logger _log = Logger.getLogger(GameTimeController.class.getName());
	
	public static final int TICKS_PER_SECOND = 10; 
	public static final int MILLIS_IN_TICK = 1000 / TICKS_PER_SECOND;
	public static final int IG_DAYS_PER_DAY = 6;
	public static final int MILLIS_PER_IG_DAY = (3600000 * 24) / IG_DAYS_PER_DAY;
	public static final int SECONDS_PER_IG_DAY = MILLIS_PER_IG_DAY / 1000;
	public static final int TICKS_PER_IG_DAY = SECONDS_PER_IG_DAY * TICKS_PER_SECOND;
	
	private static GameTimeController _instance;
	
	private final Set<L2Character> _movingObjects = ConcurrentHashMap.newKeySet();
	private final long _referenceTime;
	
	private GameTimeController()
	{
		super("GameTimeController");
		super.setDaemon(true);
		super.setPriority(MAX_PRIORITY);
		
		final Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		_referenceTime = c.getTimeInMillis();
		
		super.start();
	}
	
	public static void init()
	{
		_instance = new GameTimeController();
	}
	
	public int getGameTime()
	{
		return (getGameTicks() % TICKS_PER_IG_DAY) / MILLIS_IN_TICK;
	}
	
	public int getGameHour()
	{
		return getGameTime() / 60;
	}
	
	public int getGameMinute()
	{
		return getGameTime() % 60;
	}
	
	public boolean isNight()
	{
		return getGameHour() < 6;
	}
	
	public int getGameTicks()
	{
		return (int) ((System.currentTimeMillis() - _referenceTime) / MILLIS_IN_TICK);
	}
	
	public void registerMovingObject(final L2Character cha)
	{
		if (cha == null)
			return;

		_movingObjects.add(cha);
	}
	
	private void moveObjects()
	{
		_movingObjects.removeIf(L2Character::updatePosition);
	}
	
	public void stopTimer()
	{
		super.interrupt();
	}
	
	@Override
	public void run()
	{
		
		long nextTickTime, sleepTime;
		boolean isNight = isNight();
		
		if (isNight)
		{
			ThreadPoolManager.getInstance().executeAi(() -> DayNightSpawnManager.getInstance().notifyChangeMode());
		}
		
		while (true)
		{
			nextTickTime = ((System.currentTimeMillis() / MILLIS_IN_TICK) * MILLIS_IN_TICK) + 100;
			
			try
			{
				moveObjects();
			}
			catch (final Throwable e)
			{

			}
			
			sleepTime = nextTickTime - System.currentTimeMillis();
			
			if (sleepTime > 0)
			{
				try
				{
					Thread.sleep(sleepTime);
				}
				catch (final InterruptedException e)
				{
					
				}
			}
			
			if (isNight() != isNight)
			{
				isNight = !isNight;
				
				ThreadPoolManager.getInstance().executeAi(() -> DayNightSpawnManager.getInstance().notifyChangeMode());
			}
		}
	}
	
	public static GameTimeController getInstance()
	{
		return _instance;
	}
}