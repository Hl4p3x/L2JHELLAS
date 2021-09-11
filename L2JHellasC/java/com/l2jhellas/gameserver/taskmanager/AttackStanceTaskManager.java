package com.l2jhellas.gameserver.taskmanager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.AutoAttackStop;

public class AttackStanceTaskManager implements Runnable
{
	private static final long ATTACK_STANCE_PERIOD = 15000;
	
	private final Map<L2Character, Long> _characters = new ConcurrentHashMap<>();

	protected AttackStanceTaskManager()
	{
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 1000, 1000);
	}
	
	public final void add(L2Character character)
	{
		if (character !=null)	   		 
			_characters.put(character, System.currentTimeMillis() + ATTACK_STANCE_PERIOD);	
	}

	public final boolean remove(L2Character character)
	{
		if (character != null)
			return _characters.remove(character) != null;
		
		return false;
	}
	
	public final boolean isInAttackStance(L2Character character)
	{
		if (character != null)
			return _characters.containsKey(character);
		
		return false;
	}
	
	@Override
	public final void run()
	{
		// List is empty, skip.
		if (_characters.isEmpty())
			return;
		
		// Get current time.
		final long time = System.currentTimeMillis();
		
		// Loop all characters.
		for (Map.Entry<L2Character, Long> entry : _characters.entrySet())
		{
			// Time hasn't passed yet, skip.
			if (time < entry.getValue())
				continue;
			
			// Get character.
			final L2Character character = entry.getKey();
			
			// Stop character attack stance animation.
			character.broadcastPacket(new AutoAttackStop(character.getObjectId()));

			// Stop pet attack stance animation.
			if (character instanceof L2PcInstance)
			{
				final L2Summon summon = ((L2PcInstance) character).getPet();
				if (summon != null)
					summon.broadcastPacket(new AutoAttackStop(summon.getObjectId()));
			}

			_characters.remove(character);
		}
	}
	
	public static final AttackStanceTaskManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final AttackStanceTaskManager _instance = new AttackStanceTaskManager();
	}
}