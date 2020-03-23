package com.l2jhellas.gameserver.scrips.quests.ai.group;

import java.util.HashMap;
import java.util.Map;

import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.QuestEventType;
import com.l2jhellas.gameserver.scrips.quests.ai.AbstractNpcAI;
import com.l2jhellas.util.Rnd;

public class SummonMinions extends AbstractNpcAI
{
	private static final String[] ORCS_WORDS =
	{
		"Come out, you children of darkness!",
		"Destroy the enemy, my brothers!",
		"Show yourselves!",
		"Forces of darkness! Follow me!"
	};
	
	private static final Map<Integer, int[]> MINIONS = new HashMap<>();
	{
		MINIONS.put(20767, new int[]
		{
			20768,
			20769,
			20770
		}); // Timak Orc Troop
		
		MINIONS.put(21524, new int[]
		{
			21525
		}); // Blade of Splendor
		
		MINIONS.put(21531, new int[]
		{
			21658
		}); // Punishment of Splendor
		
		MINIONS.put(21539, new int[]
		{
			21540
		}); // Wailing of Splendor
	}
	
	public SummonMinions()
	{
		super(SummonMinions.class.getSimpleName(), "ai/group");
		
		for (int[] mini : MINIONS.values())
			registerMobs(mini, QuestEventType.ON_ATTACK, QuestEventType.ON_KILL);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (npc.isScriptValue(0))
		{
			int npcId = npc.getNpcId();
			if (npcId != 20767)
			{
				for (int val : MINIONS.get(npcId))
				{
					L2Attackable newNpc = (L2Attackable) addSpawn(val, npc.getX() + Rnd.get(-150, 150), npc.getY() + Rnd.get(-150, 150), npc.getZ(), 0, false, 0, false);
					attack(newNpc, attacker);
				}
			}
			else
			{
				for (int val : MINIONS.get(npcId))
					addSpawn(val, (npc.getX() + Rnd.get(-100, 100)), (npc.getY() + Rnd.get(-100, 100)), npc.getZ(), 0, false, 0, false);
				
				npc.broadcastNpcSay(ORCS_WORDS[Rnd.get(ORCS_WORDS.length)]);
			}
			npc.setScriptValue(1);
		}
		
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	public static void main(String[] args)
	{
		new SummonMinions();
	}
}