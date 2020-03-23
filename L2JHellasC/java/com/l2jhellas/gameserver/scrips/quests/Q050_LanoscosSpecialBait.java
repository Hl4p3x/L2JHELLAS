package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q050_LanoscosSpecialBait extends Quest
{
	private static final String qn = "Q050_LanoscosSpecialBait";
	
	// Item
	private static final int ESSENCE_OF_WIND = 7621;
	
	// Reward
	private static final int WIND_FISHING_LURE = 7610;
	
	public Q050_LanoscosSpecialBait()
	{
		super(50, qn, "Lanosco's Special Bait");
		
		setItemsIds(ESSENCE_OF_WIND);
		
		addStartNpc(31570); // Lanosco
		addTalkId(31570);
		
		addKillId(21026); // Singing wind
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31570-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31570-07.htm"))
		{
			htmltext = "31570-06.htm";
			st.takeItems(ESSENCE_OF_WIND, -1);
			st.rewardItems(WIND_FISHING_LURE, 4);
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
				htmltext = (player.getLevel() < 27) ? "31570-02.htm" : "31570-01.htm";
				break;
			
			case STATE_STARTED:
				htmltext = (st.getQuestItemsCount(ESSENCE_OF_WIND) == 100) ? "31570-04.htm" : "31570-05.htm";
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
		
		if (st.dropItems(ESSENCE_OF_WIND, 1, 100, 300000))
			st.set("cond", "2");
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q050_LanoscosSpecialBait();
	}
}