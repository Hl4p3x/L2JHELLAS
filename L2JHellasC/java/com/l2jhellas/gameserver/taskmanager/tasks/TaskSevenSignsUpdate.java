package com.l2jhellas.gameserver.taskmanager.tasks;

import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.SevenSigns;
import com.l2jhellas.gameserver.SevenSignsFestival;
import com.l2jhellas.gameserver.taskmanager.Task;
import com.l2jhellas.gameserver.taskmanager.TaskManager;
import com.l2jhellas.gameserver.taskmanager.TaskManager.ExecutedTask;

import com.l2jhellas.gameserver.taskmanager.TaskTypes;

public class TaskSevenSignsUpdate extends Task
{
	private static final Logger _log = Logger.getLogger(TaskOlympiadSave.class.getName());
	public static final String NAME = "SevenSignsUpdate";
	
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
			if (!SevenSigns.getInstance().isSealValidationPeriod())
				SevenSignsFestival.getInstance().saveFestivalData(false);
			
			SevenSigns.getInstance().saveSevenSignsData();
			SevenSigns.getInstance().saveSevenSignsStatus();
			
			_log.info("SevenSigns: Data updated successfully.");
		}
		catch (Exception e)
		{
			_log.warning(TaskSevenSignsUpdate.class.getName() + ": SevenSigns: Failed to save Seven Signs configuration: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	@Override
	public void initializate()
	{
		super.initializate();
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_FIXED_SHEDULED, "1800000", "1800000", "");
	}
}