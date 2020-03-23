package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Rnd;

public class Q659_IdRatherBeCollectingFairyBreath extends Quest
{
	private static final String qn = "Q659_IdRatherBeCollectingFairyBreath";
	
	// NPCs
	private static final int GALATEA = 30634;
	
	// Item
	private static final int FAIRY_BREATH = 8286;
	
	// Monsters
	private static final int SOBBING_WIND = 21023;
	private static final int BABBLING_WIND = 21024;
	private static final int GIGGLING_WIND = 21025;
	
	public Q659_IdRatherBeCollectingFairyBreath()
	{
		super(659, qn, "I'd Rather Be Collecting Fairy Breath");
		
		setItemsIds(FAIRY_BREATH);
		
		addStartNpc(GALATEA);
		addTalkId(GALATEA);
		addKillId(GIGGLING_WIND, BABBLING_WIND, SOBBING_WIND);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30634-03.htm"))
		{
			st.set("cond", "1");
			st.setState(STATE_STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30634-06.htm"))
		{
			int count = st.getQuestItemsCount(FAIRY_BREATH);
			if (count > 0)
			{
				st.takeItems(FAIRY_BREATH, count);
				if (count < 10)
					st.rewardItems(57, count * 50);
				else
					st.rewardItems(57, count * 50 + 5365);
			}
		}
		else if (event.equalsIgnoreCase("30634-08.htm"))
			st.exitQuest(true);
		
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
				if (player.getLevel() >= 26)
					htmltext = "30634-02.htm";
				else
				{
					htmltext = "30634-01.htm";
					st.exitQuest(true);
				}
				break;
			
			case STATE_STARTED:
				if (!st.hasQuestItems(FAIRY_BREATH))
					htmltext = "30634-04.htm";
				else
					htmltext = "30634-05.htm";
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = checkPlayerState(player, npc, STATE_STARTED);
		if (st == null)
			return null;
		
		if (Rnd.get(100) < 90)
		{
			st.giveItems(FAIRY_BREATH, 1);
			st.playSound(QuestState.SOUND_ITEMGET);
		}
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q659_IdRatherBeCollectingFairyBreath();
	}
}