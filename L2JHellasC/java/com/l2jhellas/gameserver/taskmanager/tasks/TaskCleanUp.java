package com.l2jhellas.gameserver.taskmanager.tasks;

import com.l2jhellas.gameserver.taskmanager.Task;
import com.l2jhellas.gameserver.taskmanager.TaskManager.ExecutedTask;

public final class TaskCleanUp extends Task
{
	public static final String NAME = "CleanUp";
	
	@Override
	public String getName()
	{
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		System.runFinalization();
		System.gc();
	}
}