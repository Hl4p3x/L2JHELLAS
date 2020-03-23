package com.l2jhellas.gameserver.scrips.quests.ai.invidual;

import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.instancemanager.GrandBossManager;
import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.QuestEventType;
import com.l2jhellas.gameserver.model.zone.type.L2BossZone;
import com.l2jhellas.gameserver.scrips.quests.ai.AbstractNpcAI;

public class Anays extends AbstractNpcAI
{
	private final int ANAYS = 25517;
	private final L2BossZone _Zone;
	
	public Anays()
	{
		super("Anays", "ai");
		_Zone = GrandBossManager.getZone(113000, -76000, 200);
		addEventId(ANAYS, QuestEventType.ON_ATTACK);
		addEventId(ANAYS, QuestEventType.ON_SPAWN);
		addEventId(ANAYS, QuestEventType.ON_AGGRO_RANGE_ENTER);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if ((npc.getNpcId() == ANAYS) && !_Zone.isInsideZone(npc.getX(), npc.getY()))
		{
			((L2Attackable) npc).clearAggroList();
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			npc.teleToLocation(113000, -76000, 200);
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		if (npc instanceof L2Attackable)
			((L2Attackable) npc).seeThroughSilentMove(true);
		return super.onSpawn(npc);
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if ((npc.getNpcId() == ANAYS) && !npc.isInCombat() && (npc.getTarget() == null))
		{
			npc.setTarget(player);
			npc.setIsRunning(true);
			((L2Attackable) npc).addDamageHate(player, 0, 999);
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
		}
		else if (((L2Attackable) npc).getMostHated() == null)
			return null;
		return super.onAggroRangeEnter(npc, player, isPet);
	}
	
	public static void main(String[] args)
	{
		new Anays();
	}
}