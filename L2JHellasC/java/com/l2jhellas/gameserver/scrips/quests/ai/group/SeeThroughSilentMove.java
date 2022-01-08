package com.l2jhellas.gameserver.scrips.quests.ai.group;

import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.spawn.SpawnData;
import com.l2jhellas.gameserver.scrips.quests.ai.AbstractNpcAI;
import com.l2jhellas.util.Util;

public class SeeThroughSilentMove extends AbstractNpcAI
{
	private static final int[] MOBIDS =
	{
		18001,
		18002,
		22199,
		22215,
		22216,
		22217,
		29009,
		29010,
		29011,
		29012,
		29013
	};
	
	public SeeThroughSilentMove()
	{
		super("SeeThroughSilentMove", "ai");
		
		SpawnData.getInstance().forEachSpawn(sp ->
		{
			if (sp != null && Util.contains(MOBIDS, sp.getNpcid()) && sp.getLastSpawn() != null && sp.getLastSpawn() instanceof L2Attackable)
				((L2Attackable) sp.getLastSpawn()).seeThroughSilentMove(true);
			return true;
		});

		for (int npcId : MOBIDS)
			addSpawnId(npcId);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		if (npc instanceof L2Attackable)
			((L2Attackable) npc).seeThroughSilentMove(true);
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new SeeThroughSilentMove();
	}
}