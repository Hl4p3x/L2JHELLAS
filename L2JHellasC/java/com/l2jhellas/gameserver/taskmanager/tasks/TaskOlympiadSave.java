package com.l2jhellas.gameserver.taskmanager.tasks;

import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.model.entity.olympiad.Olympiad;
import com.l2jhellas.gameserver.taskmanager.Task;
import com.l2jhellas.gameserver.taskmanager.TaskManager;
import com.l2jhellas.gameserver.taskmanager.TaskManager.ExecutedTask;
import com.l2jhellas.gameserver.taskmanager.TaskTypes;

public class TaskOlympiadSave extends Task
{
	private static final Logger _log = Logger.getLogger(TaskOlympiadSave.class.getName());
	public static final String NAME = "OlympiadSave";
	
	@Override
	public String getName()
	{
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		try
		{
			Olympiad.getInstance().saveOlympiadStatus();
			_log.info("Olympiad System: Data updated successfully.");
		}
		catch (Exception e)
		{
			_log.warning(TaskOlympiadSave.class.getName() + ": Olympiad System: Failed to save Olympiad configuration: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	@Override
	public void initializate()
	{
		super.initializate();
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_FIXED_SHEDULED, "900000", "1800000", "");
	}
}
