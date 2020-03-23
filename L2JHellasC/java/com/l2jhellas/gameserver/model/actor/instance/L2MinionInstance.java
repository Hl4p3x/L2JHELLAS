package com.l2jhellas.gameserver.model.actor.instance;

import com.l2jhellas.gameserver.ai.L2AttackableAI;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.L2WorldRegion;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public final class L2MinionInstance extends L2MonsterInstance
{
	
	public L2MinionInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		// Notify Leader that Minion has Spawned
		getLeader().getMinionList().onMinionSpawn(this);
		
		if (getLeader().isRaid())
		{
			setIsRaidMinion(true);
		}
		
		// check the region where this mob is, do not activate the AI if region is inactive.
		final L2WorldRegion region = L2World.getInstance().getRegion(getX(), getY(), getZ());
		if ((region != null) && (!region.isActive()))
			((L2AttackableAI) getAI()).stopAITask();
	}
	
	@Override
	public L2MonsterInstance getLeader()
	{
		return super.getLeader();
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;
		
		if (getLeader() != null)
			getLeader().getMinionList().onMinionDie(this, getLeader().getSpawn().getRespawnDelay() / 2);
		
		return true;
	}
}