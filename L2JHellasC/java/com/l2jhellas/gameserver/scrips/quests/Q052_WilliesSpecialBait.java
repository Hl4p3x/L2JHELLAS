package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q052_WilliesSpecialBait extends Quest
{
	private static final String qn = "Q052_WilliesSpecialBait";
	
	// Item
	private static final int TARLK_EYE = 7623;
	
	// Reward
	private static final int EARTH_FISHING_LURE = 7612;
	
	public Q052_WilliesSpecialBait()
	{
		super(52, qn, "Willie's Special Bait");
		
		setItemsIds(TARLK_EYE);
		
		addStartNpc(31574); // Willie
		addTalkId(31574);
		
		addKillId(20573); // Tarlk Basilik
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31574-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31574-07.htm"))
		{
			htmltext = "31574-06.htm";
			st.takeItems(TARLK_EYE, -1);
			st.rewardItems(EARTH_FISHING_LURE, 4);
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(false);
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
				htmltext = (player.getLevel() < 48) ? "31574-02.htm" : "31574-01.htm";
				break;
			
			case STATE_STARTED:
				htmltext = (st.getQuestItemsCount(TARLK_EYE) == 100) ? "31574-04.htm" : "31574-05.htm";
				break;
			
			case STATE_COMPLETED:
				htmltext = getAlreadyCompletedMsg();
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = checkPlayerCondition(player, npc, "cond", "1");
		if (st == null)
			return null;
		
		if (st.dropItems(TARLK_EYE, 1, 100, 300000))
			st.set("cond", "2");
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q052_WilliesSpecialBait();
	}
}