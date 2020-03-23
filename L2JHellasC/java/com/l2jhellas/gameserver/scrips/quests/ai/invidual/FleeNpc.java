package com.l2jhellas.gameserver.scrips.quests.ai.invidual;

import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.model.quest.QuestEventType;
import com.l2jhellas.gameserver.scrips.quests.ai.AbstractNpcAI;
import com.l2jhellas.util.Rnd;

public class FleeNpc extends AbstractNpcAI
{
	private final int[] _npcId =
	{
		20432,
		22228,
		18150,
		18151,
		18152,
		18153,
		18154,
		18155,
		18156,
		18157
	};
	
	public FleeNpc()
	{
		super("FleeNpc", "Ai for Flee Npcs");
		
		for (int element : _npcId)
		{
			addEventId(element, QuestEventType.ON_ATTACK);
		}
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (npc.getNpcId() >= 18150 && npc.getNpcId() <= 18157)
		{
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location((npc.getX() + Rnd.get(-40, 40)), (npc.getY() + Rnd.get(-40, 40)), npc.getZ(), npc.getHeading()));
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
			return null;
		}
		else if (npc.getNpcId() == 20432 || npc.getNpcId() == 22228)
		{
			if (Rnd.get(3) == 2)
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location((npc.getX() + Rnd.get(-200, 200)), (npc.getY() + Rnd.get(-200, 200)), npc.getZ(), npc.getHeading()));
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
			return null;
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	// Register the new Script at the Script System
	public static void main(String[] args)
	{
		new FleeNpc();
	}
}
