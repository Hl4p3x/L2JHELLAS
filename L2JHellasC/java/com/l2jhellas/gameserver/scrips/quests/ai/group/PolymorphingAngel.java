package com.l2jhellas.gameserver.scrips.quests.ai.group;

import java.util.HashMap;
import java.util.Map;

import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.scrips.quests.ai.AbstractNpcAI;

public class PolymorphingAngel extends AbstractNpcAI
{
	private static final Map<Integer, Integer> ANGELSPAWNS = new HashMap<>();
	{
		ANGELSPAWNS.put(20830, 20859);
		ANGELSPAWNS.put(21067, 21068);
		ANGELSPAWNS.put(21062, 21063);
		ANGELSPAWNS.put(20831, 20860);
		ANGELSPAWNS.put(21070, 21071);
	}
	
	public PolymorphingAngel()
	{
		super(PolymorphingAngel.class.getSimpleName(), "ai/group");
		
		for (int mob : ANGELSPAWNS.keySet())
			addKillId(mob);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		final L2Attackable newNpc = (L2Attackable) addSpawn(ANGELSPAWNS.get(npc.getNpcId()), npc, false, 0, false);
		attack(newNpc, killer);
		
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(String[] args)
	{
		new PolymorphingAngel();
	}
}