package com.l2jhellas.gameserver.scrips.quests.ai.vilagemaster;

import java.util.HashMap;
import java.util.Map;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;

public class Clan extends Quest
{
	private static final int[] NPC =
	{
		30026,
		30031,
		30037,
		30066,
		30070,
		30109,
		30115,
		30120,
		30154,
		30174,
		30175,
		30176,
		30187,
		30191,
		30195,
		30288,
		30289,
		30290,
		30297,
		30358,
		30373,
		30462,
		30474,
		30498,
		30499,
		30500,
		30503,
		30504,
		30505,
		30508,
		30511,
		30512,
		30513,
		30520,
		30525,
		30565,
		30594,
		30595,
		30676,
		30677,
		30681,
		30685,
		30687,
		30689,
		30694,
		30699,
		30704,
		30845,
		30847,
		30849,
		30854,
		30857,
		30862,
		30865,
		30894,
		30897,
		30900,
		30905,
		30910,
		30913,
		31269,
		31272,
		31276,
		31279,
		31285,
		31288,
		31314,
		31317,
		31321,
		31324,
		31326,
		31328,
		31331,
		31334,
		31755,
		31958,
		31961,
		31965,
		31968,
		31974,
		31977,
		31996,
		32092,
		32093,
		32094,
		32095,
		32096,
		32097,
		32098,
		32145,
		32146,
		32147,
		32150,
		32153,
		32154,
		32157,
		32158,
		32160,
		32171,
		32193,
		32199,
		32202,
		32213,
		32214,
		32221,
		32222,
		32233,
		32234
	};
	
	private static final Map<String, String> LEADER_REQUIRED = new HashMap<>();
	static
	{
		LEADER_REQUIRED.put("9000-03.htm", "9000-03-no.htm");
		LEADER_REQUIRED.put("9000-04.htm", "9000-04-no.htm");
		LEADER_REQUIRED.put("9000-05.htm", "9000-05-no.htm");
		LEADER_REQUIRED.put("9000-07.htm", "9000-07-no.htm");
		LEADER_REQUIRED.put("9000-12a.htm", "9000-07-no.htm");
		LEADER_REQUIRED.put("9000-12b.htm", "9000-07-no.htm");
		LEADER_REQUIRED.put("9000-13a.htm", "9000-07-no.htm");
		LEADER_REQUIRED.put("9000-13b.htm", "9000-07-no.htm");
		LEADER_REQUIRED.put("9000-14a.htm", "9000-07-no.htm");
		LEADER_REQUIRED.put("9000-14b.htm", "9000-07-no.htm");
		LEADER_REQUIRED.put("9000-15.htm", "9000-07-no.htm");
	}
	
	public Clan()
	{
		super(-1, Clan.class.getSimpleName(), "village_master");
		
		addStartNpc(NPC);
		addTalkId(NPC);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		
		if (LEADER_REQUIRED.containsKey(event) && !player.isClanLeader())
			return LEADER_REQUIRED.get(event);
		
		return event;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		return "9000-01.htm";
	}
	
	public static void main(String[] args)
	{
		new Clan();
	}
}