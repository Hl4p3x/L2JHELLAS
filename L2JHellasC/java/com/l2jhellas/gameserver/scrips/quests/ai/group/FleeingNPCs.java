package com.l2jhellas.gameserver.scrips.quests.ai.group;

import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.QuestEventType;
import com.l2jhellas.gameserver.scrips.quests.ai.AbstractNpcAI;
import com.l2jhellas.util.Rnd;

public class FleeingNPCs extends AbstractNpcAI
{
	private final int[] _npcId =
	{
		20002, // Rabbit
		20432, // Elpy
		18150,
		18151,
		18152,
		18153,
		18154,
		18155,
		18156,
		18157
	};

	public FleeingNPCs()
	{
		super(FleeingNPCs.class.getSimpleName(), "ai/group");
		registerMobs(_npcId, QuestEventType.ON_ATTACK);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{	
		npc.setRunning();	
		npc.disableCoreAI(true);	
		if (Rnd.get(2) == 1 && !npc.isMoving() && !npc.isDead() && !npc.isMovementDisabled() && npc.getMoveSpeed() > 0)
		{
			final L2Summon summon = isPet ? attacker.getPet() : null;
			final L2Character attackerLoc = summon == null ? attacker : summon;
			npc.tryToFlee(attackerLoc);
			
			if (npc.getNpcId() >= 18150 && npc.getNpcId() <= 18157)
			{
				L2PcInstance plr = null;
				if (Rnd.nextBoolean())
					plr = Rnd.get(L2World.getInstance().getVisibleObjects(npc, L2PcInstance.class,1200));
					
				npc.broadcastNpcSay((plr == null) ? "Help me!!" : "%n! Help me!!".replaceAll("%n", plr.getName()));
			}
		}			
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	public static void main(String[] args)
	{
		new FleeingNPCs();
	}
}