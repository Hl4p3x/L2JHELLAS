package com.l2jhellas.gameserver.taskmanager.tasks;

import com.l2jhellas.gameserver.Shutdown;
import com.l2jhellas.gameserver.taskmanager.Task;
import com.l2jhellas.gameserver.taskmanager.TaskManager.ExecutedTask;

public class TaskShutdown extends Task
{
	public static final String NAME = "shutdown";
	
	@Override
	public String getName()
	{
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		Shutdown handler = new Shutdown(Integer.valueOf(task.getParams()[2]), false);
		handler.start();
	}
}