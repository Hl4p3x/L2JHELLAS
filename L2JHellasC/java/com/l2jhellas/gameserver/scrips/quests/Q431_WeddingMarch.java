package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q431_WeddingMarch extends Quest
{
	private static final String qn = "Q431_WeddingMarch";
	
	// NPC
	private static final int KANTABILON = 31042;
	
	// Item
	private static final int SILVER_CRYSTAL = 7540;
	
	// Reward
	private static final int WEDDING_ECHO_CRYSTAL = 7062;
	
	public Q431_WeddingMarch()
	{
		super(431, qn, "Wedding March");
		
		setItemsIds(SILVER_CRYSTAL);
		
		addStartNpc(KANTABILON);
		addTalkId(KANTABILON);
		
		addKillId(20786, 20787);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31042-02.htm"))
		{
			st.set("cond", "1");
			st.setState(STATE_STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31042-05.htm"))
		{
			if (st.getQuestItemsCount(SILVER_CRYSTAL) < 50)
				htmltext = "31042-03.htm";
			else
			{
				st.takeItems(SILVER_CRYSTAL, -1);
				st.giveItems(WEDDING_ECHO_CRYSTAL, 25);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
			}
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
				if (player.getLevel() >= 38)
					htmltext = "31042-01.htm";
				else
				{
					htmltext = "31042-00.htm";
					st.exitQuest(true);
				}
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				if (cond == 1)
					htmltext = "31042-02.htm";
				else if (cond == 2)
				{
					if (st.getQuestItemsCount(SILVER_CRYSTAL) < 50)
						htmltext = "31042-03.htm";
					else
						htmltext = "31042-04.htm";
				}
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
				
		if (st.dropItemsAlways(SILVER_CRYSTAL, 1, 50))
			st.set("cond", "2");
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q431_WeddingMarch();
	}
}