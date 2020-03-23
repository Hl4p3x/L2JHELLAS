package com.l2jhellas.gameserver.taskmanager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SetupGauge;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.Stats;

public final class WaterTaskManager implements Runnable
{
	private final Map<L2PcInstance, Long> _players = new ConcurrentHashMap<>();
	
	public static final WaterTaskManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected WaterTaskManager()
	{
		// Run task each second.
		ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(this, 1000, 1000);
	}
	
	public final void add(L2PcInstance player)
	{
		if (!player.isDead() && !_players.containsKey(player))
		{
			final int time = (int) player.calcStat(Stats.BREATH, 60000 * player.getRace().getBreathMultiplier(), player, null);
			
			_players.put(player, System.currentTimeMillis() + time);
			
			player.sendPacket(new SetupGauge(SetupGauge.CYAN, time));
		}
	}
	
	public final void remove(L2PcInstance player)
	{
		if (_players.remove(player) != null)
			player.sendPacket(new SetupGauge(SetupGauge.CYAN, 0));
	}
	
	@Override
	public final void run()
	{
		// List is empty, skip.
		if (_players.isEmpty())
			return;
		
		// Get current time.
		final long time = System.currentTimeMillis();
		
		// Loop all players.
		for (Map.Entry<L2PcInstance, Long> entry : _players.entrySet())
		{
			// Time has not passed yet, skip.
			if (time < entry.getValue())
				continue;
			
			// Get player.
			final L2PcInstance player = entry.getKey();
			
			// Reduce 1% of HP per second.
			final double hp = player.getMaxHp() / 100.0;
			player.reduceCurrentHp(hp, player, false);
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DROWN_DAMAGE_S1).addNumber((int) hp));
		}
	}
	
	private static class SingletonHolder
	{
		protected static final WaterTaskManager _instance = new WaterTaskManager();
	}
}