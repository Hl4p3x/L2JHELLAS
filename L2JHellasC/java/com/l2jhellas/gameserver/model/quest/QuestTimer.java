package com.l2jhellas.gameserver.model.quest;

import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class QuestTimer
{
	protected static final Logger _log = Logger.getLogger(QuestTimer.class.getName());
	
	private final Quest _quest;
	private final String _name;
	private final L2Npc _npc;
	private final L2PcInstance _player;
	
	private ScheduledFuture<?> _schedular;
	private final boolean _isRepeating;
	private boolean _isActive = true;
	
	QuestTimer(Quest quest, String name, L2Npc npc, L2PcInstance player, long time, boolean repeating)
	{
		_quest = quest;
		_name = name;
		_npc = npc;
		_player = player;
		_isRepeating = repeating;
		
		if (repeating)
			_schedular = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ScheduleTimerTask(), time, time);
		else
			_schedular = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTimerTask(), time);
	}
	
	final Quest getQuest()
	{
		return _quest;
	}
	
	final String getName()
	{
		return _name;
	}
	
	final L2Npc getNpc()
	{
		return _npc;
	}
	
	final L2PcInstance getPlayer()
	{
		return _player;
	}
	
	final boolean getIsRepeating()
	{
		return _isRepeating;
	}
	
	final boolean getIsActive()
	{
		return _isActive;
	}
	
	@Override
	public final String toString()
	{
		return _name;
	}
	
	final class ScheduleTimerTask implements Runnable
	{
		@Override
		public void run()
		{
			if (!getIsActive())
				return;
			
			try
			{
				if (!getIsRepeating())
					cancel();
				getQuest().notifyEvent(getName(), getNpc(), getPlayer());
			}
			catch (Exception e)
			{
				_log.severe(ScheduleTimerTask.class.getName() + ": Could not run schedule timer");
				if (Config.DEVELOPER)
					e.printStackTrace();
			}
		}
	}
	
	final void cancel()
	{
		_isActive = false;
		
		if (_schedular != null)
			_schedular.cancel(false);
		
		_quest.removeQuestTimer(this);
	}
	
	final boolean equals(Quest quest, String name, L2Npc npc, L2PcInstance player)
	{
		if (quest == null || quest != _quest)
			return false;
		
		if (name == null || !name.equals(_name))
			return false;
		
		return ((npc == _npc) && (player == _player));
	}
}