package com.l2jhellas.gameserver.taskmanager;

import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import com.l2jhellas.gameserver.taskmanager.TaskManager.ExecutedTask;

public abstract class Task
{
	private static Logger _log = Logger.getLogger(Task.class.getName());
	
	public void initializate()
	{
		_log.info("Task " + getName() + " initialized.");
	}
	
	public ScheduledFuture<?> launchSpecial(ExecutedTask instance)
	{
		return null;
	}
	
	public abstract String getName();
	
	public abstract void onTimeElapsed(ExecutedTask task);
	
	public void onDestroy()
	{
	}
}