package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q606_WarWithVarkaSilenos extends Quest
{
	private static final String qn = "Q606_WarWithVarkaSilenos";
	
	// Items
	private static final int Horn = 7186;
	private static final int Mane = 7233;
	
	public Q606_WarWithVarkaSilenos()
	{
		super(606, qn, "War with Varka Silenos");
		
		setItemsIds(Mane);
		
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
			if (player.getLevel() >= 74 && player.getAllianceWithVarkaKetra() >= 1)
			{
				st.set("cond", "1");
				st.setState(STATE_STARTED);
				st.playSound(QuestState.SOUND_ACCEPT);
			}
			else
			{
				htmltext = "31370-02.htm";
				st.exitQuest(true);
			}
		}
		else if (event.equalsIgnoreCase("31370-07.htm"))
		{
			if (st.getQuestItemsCount(Mane) >= 100)
			{
				st.takeItems(Mane, 100);
				st.giveItems(Horn, 20);
				st.playSound(QuestState.SOUND_ITEMGET);
			}
			else
				htmltext = "31370-08.htm";
		}
		else if (event.equalsIgnoreCase("31370-09.htm"))
		{
			st.takeItems(Mane, -1);
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
				htmltext = "31370-01.htm";
				break;
			
			case STATE_STARTED:
				if (st.getQuestItemsCount(Mane) > 0)
					htmltext = "31370-04.htm";
				else
					htmltext = "31370-05.htm";
				break;
		}
		
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Q606_WarWithVarkaSilenos();
	}
}