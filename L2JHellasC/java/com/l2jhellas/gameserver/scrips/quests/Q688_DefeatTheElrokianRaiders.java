package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q688_DefeatTheElrokianRaiders extends Quest
{
	private static final String qn = "Q688_DefeatTheElrokianRaiders";
	
	// Item
	private static final int DINOSAUR_FANG_NECKLACE = 8785;
	
	// NPC
	private static final int DINN = 32105;
	
	// Monster
	private static final int ELROKI = 22214;
	
	public Q688_DefeatTheElrokianRaiders()
	{
		super(688, qn, "Defeat the Elrokian Raiders!");
		
		setItemsIds(DINOSAUR_FANG_NECKLACE);
		
		addStartNpc(DINN);
		addTalkId(DINN);
		
		addKillId(ELROKI);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		int count = st.getQuestItemsCount(DINOSAUR_FANG_NECKLACE);
		if (event.equalsIgnoreCase("None"))
			return null;
		
		if (event.equalsIgnoreCase("32105-03.htm"))
		{
			st.set("cond", "1");
			st.setState(STATE_STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("32105-08.htm"))
		{
			if (count > 0)
			{
				st.takeItems(DINOSAUR_FANG_NECKLACE, -1);
				st.rewardItems(57, count * 3000);
			}
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
		}
		else if (event.equalsIgnoreCase("32105-06.htm"))
		{
			st.takeItems(DINOSAUR_FANG_NECKLACE, -1);
			st.rewardItems(57, count * 3000);
		}
		else if (event.equalsIgnoreCase("32105-07.htm"))
		{
			if (count >= 100)
			{
				st.takeItems(DINOSAUR_FANG_NECKLACE, 100);
				st.rewardItems(57, 450000);
			}
			else
				htmltext = "32105-04.htm";
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
				if (player.getLevel() >= 75)
					htmltext = "32105-01.htm";
				else
				{
					htmltext = "32105-00.htm";
					st.exitQuest(true);
				}
				break;
			
			case STATE_STARTED:
				if (!st.hasQuestItems(DINOSAUR_FANG_NECKLACE))
					htmltext = "32105-04.htm";
				else
					htmltext = "32105-05.htm";
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = getRandomPartyMemberState(player, npc, STATE_STARTED);
		if (st == null)
			return null;
		st.dropItems(DINOSAUR_FANG_NECKLACE, 1, -1, 500000);
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q688_DefeatTheElrokianRaiders();
	}
}