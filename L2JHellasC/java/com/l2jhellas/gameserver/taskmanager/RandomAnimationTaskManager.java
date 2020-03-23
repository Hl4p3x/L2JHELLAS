package com.l2jhellas.gameserver.taskmanager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.util.Rnd;

public final class RandomAnimationTaskManager implements Runnable
{
	private final Map<L2Npc, Long> _npcs = new ConcurrentHashMap<>();
	
	public static final RandomAnimationTaskManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected RandomAnimationTaskManager()
	{
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 1000, 1000);
	}
	
	public final void add(L2Npc character, int interval)
	{
		_npcs.put(character, System.currentTimeMillis() + interval * 1000);
	}
	
	@Override
	public final void run()
	{
		if (_npcs.isEmpty())
			return;
		
		final long time = System.currentTimeMillis();
		
		for (Map.Entry<L2Npc, Long> entry : _npcs.entrySet())
		{		
			final L2Npc character = entry.getKey();
			
			if (!character.isInActiveRegion() || character.isDead() || (character instanceof L2Attackable && character.getAI().getIntention() != CtrlIntention.AI_INTENTION_ACTIVE))
			{
				_npcs.remove(character);
				continue;
			}
			
			if (time < entry.getValue())
				continue;
			
			if (!(character.isDead() || !character.isMoving() ||  character.isStunned() || character.isSleeping() || character.isParalyzed()))
				character.onRandomAnimation();
			
			final int timer = (character.isMob()) ? Rnd.get(Config.MIN_MONSTER_ANIMATION, Config.MAX_MONSTER_ANIMATION) : Rnd.get(Config.MIN_NPC_ANIMATION, Config.MAX_NPC_ANIMATION);
			add(character, timer);
		}
	}
	
	private static final class SingletonHolder
	{
		protected static final RandomAnimationTaskManager _instance = new RandomAnimationTaskManager();
	}
}