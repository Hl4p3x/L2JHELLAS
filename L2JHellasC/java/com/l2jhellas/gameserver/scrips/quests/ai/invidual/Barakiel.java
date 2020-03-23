package com.l2jhellas.gameserver.scrips.quests.ai.invidual;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.QuestEventType;
import com.l2jhellas.gameserver.scrips.quests.ai.AbstractNpcAI;

public class Barakiel extends AbstractNpcAI
{
	private final int BARAKIEL = 25325;
	private final int BARAKIELx1 = 89800;
	private final int BARAKIELx2 = 93200;
	private final int BARAKIELy1 = -87038;
	final int[] BARAKIELLOC =
	{
		91008,
		-85904,
		-2736
	};
	
	public Barakiel()
	{
		super("Barakiel", "ai");
		addEventId(25325, QuestEventType.ON_ATTACK);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (npc.getNpcId() == BARAKIEL)
		{
			int x = npc.getX();
			int y = npc.getY();
			
			if ((x < BARAKIELx1) || (x > BARAKIELx2) || (y < BARAKIELy1))
			{
				npc.teleToLocation(BARAKIELLOC[0], BARAKIELLOC[1], BARAKIELLOC[2]);
				npc.getStatus().setCurrentHp(npc.getMaxHp());
				npc.broadcastStatusUpdate();
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	public static void main(String[] args)
	{
		new Barakiel();
	}
}