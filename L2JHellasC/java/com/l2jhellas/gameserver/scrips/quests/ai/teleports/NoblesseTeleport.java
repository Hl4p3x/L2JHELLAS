package com.l2jhellas.gameserver.scrips.quests.ai.teleports;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;

public class NoblesseTeleport extends Quest
{
	public NoblesseTeleport()
	{
		super(-1, "NoblesseTeleport", "teleports");
		
		addStartNpc(30006, 30059, 30080, 30134, 30146, 30177, 30233, 30256, 30320, 30540, 30576, 30836, 30848, 30878, 30899, 31275, 31320, 31964);
		addTalkId(30006, 30059, 30080, 30134, 30146, 30177, 30233, 30256, 30320, 30540, 30576, 30836, 30848, 30878, 30899, 31275, 31320, 31964);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		return player.isNoble() ? "noble.htm" : "nobleteleporter-no.htm";
	}
	
	public static void main(String[] args)
	{
		new NoblesseTeleport();
	}
}