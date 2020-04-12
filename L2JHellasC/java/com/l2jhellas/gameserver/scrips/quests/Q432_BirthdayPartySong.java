package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q432_BirthdayPartySong extends Quest
{
	private static final String qn = "Q432_BirthdayPartySong";
	
	// NPC
	private static final int OCTAVIA = 31043;
	
	// Item
	private static final int RED_CRYSTAL = 7541;
	
	public Q432_BirthdayPartySong()
	{
		super(432, qn, "Birthday Party Song");
		
		setItemsIds(RED_CRYSTAL);
		
		addStartNpc(OCTAVIA);
		addTalkId(OCTAVIA);
		
		addKillId(21103);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31043-02.htm"))
		{
			st.set("cond", "1");
			st.setState(STATE_STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31043-06.htm"))
		{
			if (st.getQuestItemsCount(RED_CRYSTAL) == 50)
			{
				htmltext = "31043-05.htm";
				st.takeItems(RED_CRYSTAL, -1);
				st.rewardItems(7061, 25);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
			}
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
				if (player.getLevel() >= 31)
					htmltext = "31043-01.htm";
				else
				{
					htmltext = "31043-00.htm";
					st.exitQuest(true);
				}
				break;
			
			case STATE_STARTED:
				if (st.getQuestItemsCount(RED_CRYSTAL) < 50)
					htmltext = "31043-03.htm";
				else
					htmltext = "31043-04.htm";
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = getRandomPartyMember(player, npc, "1");
		if (st == null)
			return null;
				
		if (st.dropItems(RED_CRYSTAL, 1, 50, 330000))
			st.set("cond", "2");
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q432_BirthdayPartySong();
	}
}