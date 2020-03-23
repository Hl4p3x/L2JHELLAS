package com.l2jhellas.gameserver.model.actor.instance;

import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public class L2FriendlyMobInstance extends L2Attackable
{
	public L2FriendlyMobInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if (attacker instanceof L2PcInstance)
			return ((L2PcInstance) attacker).getKarma() > 0;
		return false;
	}
	
	@Override
	public boolean isAggressive()
	{
		return true;
	}
}