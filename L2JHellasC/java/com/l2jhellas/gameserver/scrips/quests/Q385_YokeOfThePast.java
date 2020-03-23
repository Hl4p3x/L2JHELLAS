package com.l2jhellas.gameserver.scrips.quests;

import java.util.HashMap;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q385_YokeOfThePast extends Quest
{
	private static final String qn = "Q385_YokeOfThePast";
	
	// NPCs
	private static final int GATEKEEPER_ZIGGURAT[] =
	{
		31095,
		31096,
		31097,
		31098,
		31099,
		31100,
		31101,
		31102,
		31103,
		31104,
		31105,
		31106,
		31107,
		31108,
		31109,
		31110,
		31114,
		31115,
		31116,
		31117,
		31118,
		31119,
		31120,
		31121,
		31122,
		31123,
		31124,
		31125,
		31126
	};
	
	// Item
	private static final int ANCIENT_SCROLL = 5902;
	
	// Reward
	private static final int BLANK_SCROLL = 5965;
	
	private static final HashMap<Integer, Integer> Chance = new HashMap<>();
	{
		Chance.put(21208, 70000);
		Chance.put(21209, 80000);
		Chance.put(21210, 110000);
		Chance.put(21211, 110000);
		Chance.put(21213, 140000);
		Chance.put(21214, 190000);
		Chance.put(21215, 190000);
		Chance.put(21217, 240000);
		Chance.put(21218, 300000);
		Chance.put(21219, 300000);
		Chance.put(21221, 370000);
		Chance.put(21222, 460000);
		Chance.put(21223, 450000);
		Chance.put(21224, 500000);
		Chance.put(21225, 540000);
		Chance.put(21226, 660000);
		Chance.put(21227, 640000);
		Chance.put(21228, 700000);
		Chance.put(21229, 750000);
		Chance.put(21230, 910000);
		Chance.put(21231, 860000);
		Chance.put(21236, 120000);
		Chance.put(21237, 140000);
		Chance.put(21238, 190000);
		Chance.put(21239, 190000);
		Chance.put(21240, 220000);
		Chance.put(21241, 240000);
		Chance.put(21242, 300000);
		Chance.put(21243, 300000);
		Chance.put(21244, 340000);
		Chance.put(21245, 370000);
		Chance.put(21246, 460000);
		Chance.put(21247, 450000);
		Chance.put(21248, 500000);
		Chance.put(21249, 540000);
		Chance.put(21250, 990000);
		Chance.put(21251, 640000);
		Chance.put(21252, 700000);
		Chance.put(21253, 750000);
		Chance.put(21254, 910000);
		Chance.put(21255, 860000);
	}
	
	public Q385_YokeOfThePast()
	{
		super(385, qn, "Yoke of the Past");
		
		setItemsIds(ANCIENT_SCROLL);
		
		for (int ziggurat : GATEKEEPER_ZIGGURAT)
		{
			addStartNpc(ziggurat);
			addTalkId(ziggurat);
		}
		
		addKillId(21208, 21209, 21210, 21211, 21213, 21214, 21215, 21217, 21218, 21219, 21221, 21223, 21224, 21225, 21226, 21227, 21228, 21229, 21230, 21231, 21236, 21237, 21238, 21239, 21240, 21241, 21242, 21243, 21244, 21245, 21246, 21247, 21248, 21249, 21250, 21251, 21252, 21253, 21254, 21255);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("05.htm"))
		{
			st.set("cond", "1");
			st.setState(STATE_STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("10.htm"))
		{
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg();
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case STATE_CREATED:
				if (player.getLevel() >= 20)
					htmltext = "01.htm";
				else
				{
					htmltext = "02.htm";
					st.exitQuest(true);
				}
				break;
			
			case STATE_STARTED:
				if (st.getQuestItemsCount(ANCIENT_SCROLL) == 0)
					htmltext = "08.htm";
				else
				{
					htmltext = "09.htm";
					int count = st.getQuestItemsCount(ANCIENT_SCROLL);
					st.takeItems(ANCIENT_SCROLL, -1);
					st.rewardItems(BLANK_SCROLL, count);
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		L2PcInstance partyMember = getRandomPartyMemberState(player, npc, STATE_STARTED);
		if (partyMember == null)
			return null;
		
		QuestState st = partyMember.getQuestState(qn);
		final int npcId = Chance.get(npc.getNpcId());
		st.dropItems(ANCIENT_SCROLL, 1, -1, npcId);
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q385_YokeOfThePast();
	}
}