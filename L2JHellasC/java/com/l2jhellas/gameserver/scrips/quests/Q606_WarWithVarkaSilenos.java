package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q606_WarWithVarkaSilenos extends Quest
{
	private static final String qn = "Q606_WarWithVarkaSilenos";
	
	// Items
	private static final int HORN_OF_BUFFALO = 7186;
	private static final int VARKA_MANE = 7233;
	
	public Q606_WarWithVarkaSilenos()
	{
		super(606, qn, "War with Varka Silenos");
		
		setItemsIds(VARKA_MANE);
		
		addStartNpc(31370); // Kadun Zu Ketra
		addTalkId(31370);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31370-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31370-07.htm"))
		{
			if (st.getQuestItemsCount(VARKA_MANE) >= 100)
			{
				st.playSound(QuestState.SOUND_ITEMGET);
				st.takeItems(VARKA_MANE, 100);
				st.giveItems(HORN_OF_BUFFALO, 20);
			}
			else
				htmltext = "31370-08.htm";
		}
		else if (event.equalsIgnoreCase("31370-09.htm"))
		{
			st.takeItems(VARKA_MANE, -1);
			st.exitQuest(true);
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg();
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case STATE_CREATED:
				htmltext = (player.getLevel() >= 74 && player.isAlliedWithKetra()) ? "31370-01.htm" : "31370-02.htm";
				break;
			
			case STATE_STARTED:
				htmltext = (st.hasQuestItems(VARKA_MANE)) ? "31370-04.htm" : "31370-05.htm";
				break;
		}
		
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Q606_WarWithVarkaSilenos();
	}
}