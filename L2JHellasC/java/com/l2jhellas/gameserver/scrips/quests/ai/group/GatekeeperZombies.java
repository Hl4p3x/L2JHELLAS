package com.l2jhellas.gameserver.scrips.quests.ai.group;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.scrips.quests.ai.AbstractNpcAI;

public class GatekeeperZombies extends AbstractNpcAI
{
	public GatekeeperZombies()
	{
		super(GatekeeperZombies.class.getSimpleName(), "ai/group");
		addAggroRangeEnterId(22136);
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (player.getInventory().hasAtLeastOneItem(8064, 8065, 8067))
			return null;
		
		return super.onAggroRangeEnter(npc, player, isPet);
	}
	
	public static void main(String[] args)
	{
		new GatekeeperZombies();
	}
}