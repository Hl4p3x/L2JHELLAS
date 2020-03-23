package com.l2jhellas.gameserver.scrips.quests.ai.teleports;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;

public class ElrokiTeleporters extends Quest
{
	public ElrokiTeleporters()
	{
		super(-1, "ElrokiTeleporters", "teleports");
		
		addStartNpc(32111, 32112);
		addTalkId(32111, 32112);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		if (npc.getNpcId() == 32111)
		{
			if (player.isInCombat())
				return "32111-no.htm";
			
			player.teleToLocation(4990, -1879, -3178, false);
		}
		else
			player.teleToLocation(7557, -5513, -3221, false);
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new ElrokiTeleporters();
	}
}