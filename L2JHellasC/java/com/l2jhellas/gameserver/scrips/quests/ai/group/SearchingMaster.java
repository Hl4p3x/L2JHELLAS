package com.l2jhellas.gameserver.scrips.quests.ai.group;

import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.QuestEventType;
import com.l2jhellas.gameserver.scrips.quests.ai.AbstractNpcAI;

public class SearchingMaster extends AbstractNpcAI
{
	private static final int[] mobs =
	{
		20965,
		20966,
		20967,
		20968,
		20969,
		20970,
		20971,
		20972,
		20973
	};
	
	public SearchingMaster()
	{
		super(SearchingMaster.class.getSimpleName(), "ai/group");
		registerMobs(mobs, QuestEventType.ON_ATTACK);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet)
	{
		if (player == null)
			return null;
		
		attack(((L2Attackable) npc), player);
		return super.onAttack(npc, player, damage, isPet);
	}
	
	public static void main(String[] args)
	{
		new SearchingMaster();
	}
}