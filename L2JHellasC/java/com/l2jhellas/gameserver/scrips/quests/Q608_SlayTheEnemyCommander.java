package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q608_SlayTheEnemyCommander extends Quest
{
	private static final String qn = "Q608_SlayTheEnemyCommander";
	
	// Quest Items
	private static final int Mos_Head = 7236;
	private static final int Wisdom_Totem = 7220;
	private static final int Ketra_Alliance_Four = 7214;
	
	public Q608_SlayTheEnemyCommander()
	{
		super(608, qn, "Slay the enemy commander!");
		
		setItemsIds(Mos_Head);
		
		addStartNpc(31370); // Kadun Zu Ketra
		addTalkId(31370);
		
		addKillId(25312); // Mos
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31370-04.htm"))
		{
			if (player.getAllianceWithVarkaKetra() >= 4 && st.getQuestItemsCount(Ketra_Alliance_Four) > 0 && st.getQuestItemsCount(Wisdom_Totem) == 0)
			{
				if (player.getLevel() >= 75)
				{
					st.set("cond", "1");
					st.setState(STATE_STARTED);
					st.playSound(QuestState.SOUND_ACCEPT);
				}
				else
				{
					htmltext = "31370-03.htm";
					st.exitQuest(true);
				}
			}
			else
			{
				htmltext = "31370-02.htm";
				st.exitQuest(true);
			}
		}
		else if (event.equalsIgnoreCase("31370-07.htm"))
		{
			if (st.getQuestItemsCount(Mos_Head) == 1)
			{
				st.takeItems(Mos_Head, -1);
				st.giveItems(Wisdom_Totem, 1);
				st.rewardExpAndSp(10000, 0);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
			}
			else
			{
				htmltext = "31370-06.htm";
				st.set("cond", "1");
				st.playSound(QuestState.SOUND_ACCEPT);
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
				htmltext = "31370-01.htm";
				break;
			
			case STATE_STARTED:
				if (st.getQuestItemsCount(Mos_Head) > 0)
					htmltext = "31370-05.htm";
				else
					htmltext = "31370-06.htm";
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		for (QuestState st : getPartyMembers(player, npc, "cond", "1"))
		{
			if(st == null)
				continue;
			
			if (st.getPlayer().getAllianceWithVarkaKetra() >= 4)
			{
				if (st.hasQuestItems(Ketra_Alliance_Four))
				{
					st.set("cond", "2");
					st.giveItems(Mos_Head, 1);
					st.playSound(QuestState.SOUND_MIDDLE);
				}
			}
		}
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q608_SlayTheEnemyCommander();
	}
}