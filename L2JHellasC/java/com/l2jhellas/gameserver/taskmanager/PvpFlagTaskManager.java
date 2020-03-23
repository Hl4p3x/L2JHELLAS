package com.l2jhellas.gameserver.taskmanager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public final class PvpFlagTaskManager implements Runnable
{
	private final Map<L2PcInstance, Long> _players = new ConcurrentHashMap<>();
	
	public static final PvpFlagTaskManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected PvpFlagTaskManager()
	{
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 1000, 1000);
	}
	
	public final void add(L2PcInstance player, long time)
	{
		_players.put(player, System.currentTimeMillis() + time);
	}
	
	public final void remove(L2PcInstance player)
	{
		_players.remove(player);
	}
	
	@Override
	public final void run()
	{
		if (_players.isEmpty())
			return;
		
		final long currentTime = System.currentTimeMillis();
		
		for (Map.Entry<L2PcInstance, Long> entry : _players.entrySet())
		{
			final L2PcInstance player = entry.getKey();
			final long timeLeft = entry.getValue();
			
			if (currentTime > timeLeft)
			{
				player.updatePvPFlag(0);
				_players.remove(player);
			}
			else if (currentTime > (timeLeft - 5000))
				player.updatePvPFlag(2);
			else
				player.updatePvPFlag(1);
		}
	}
	
	private static class SingletonHolder
	{
		protected static final PvpFlagTaskManager _instance = new PvpFlagTaskManager();
	}
}