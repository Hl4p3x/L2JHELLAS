package com.l2jhellas.gameserver.taskmanager.tasks;

import java.util.logging.Logger;

import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.UserInfo;
import com.l2jhellas.gameserver.taskmanager.Task;
import com.l2jhellas.gameserver.taskmanager.TaskManager;
import com.l2jhellas.gameserver.taskmanager.TaskManager.ExecutedTask;
import com.l2jhellas.gameserver.taskmanager.TaskTypes;

public class TaskRecom extends Task
{
	private static final Logger _log = Logger.getLogger(TaskRecom.class.getName());
	private static final String NAME = "sp_recommendations";
	
	@Override
	public String getName()
	{
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		for (L2PcInstance player : L2World.getInstance().getAllPlayers().values())
		{
			player.restartRecom();
			player.sendPacket(new UserInfo(player));
		}
		_log.config("Recommendation Global Task: launched.");
	}
	
	@Override
	public void initializate()
	{
		super.initializate();
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_GLOBAL_TASK, "1", "13:00:00", "");
	}
}