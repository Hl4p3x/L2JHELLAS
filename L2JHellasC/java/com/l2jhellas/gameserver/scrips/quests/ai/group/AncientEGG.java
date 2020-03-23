package com.l2jhellas.gameserver.scrips.quests.ai.group;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.scrips.quests.ai.AbstractNpcAI;
import com.l2jhellas.gameserver.skills.SkillTable;

public class AncientEGG extends AbstractNpcAI
{
	private final int EGG = 18344;
	
	public AncientEGG()
	{
		super("AncientEGG", "ai");
		addAttackId(EGG);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet)
	{
		player.setTarget(player);
		player.doCast(SkillTable.getInstance().getInfo(5088, 1));
		return super.onAttack(npc, player, damage, isPet);
	}
	
	public static void main(String[] args)
	{
		new AncientEGG();
	}
}