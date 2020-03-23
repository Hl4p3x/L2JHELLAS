package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.enums.player.ClassRace;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Rnd;

public class Q267_WrathOfVerdure extends Quest
{
	private static final String qn = "Q267_WrathOfVerdure";
	
	// Items
	private static final int GOBLIN_CLUB = 1335;
	
	// Reward
	private static final int SILVERY_LEAF = 1340;
	
	public Q267_WrathOfVerdure()
	{
		super(267, qn, "Wrath of Verdure");
		
		setItemsIds(GOBLIN_CLUB);
		
		addStartNpc(31853); // Bremec
		addTalkId(31853);
		
		addKillId(20325); // Goblin
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31853-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31853-06.htm"))
		{
			st.playSound(QuestState.SOUND_FINISH);
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
				if (player.getRace() != ClassRace.ELF)
					htmltext = "31853-00.htm";
				else if (player.getLevel() < 4)
					htmltext = "31853-01.htm";
				else
					htmltext = "31853-02.htm";
				break;
			
			case STATE_STARTED:
				final int count = st.getQuestItemsCount(GOBLIN_CLUB);
				if (count > 0)
				{
					htmltext = "31853-05.htm";
					st.takeItems(GOBLIN_CLUB, -1);
					st.rewardItems(SILVERY_LEAF, count);
				}
				else
					htmltext = "31853-04.htm";
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
		
		if (Rnd.get(100) < 50)
		{
			st.giveItems(GOBLIN_CLUB, 1);
			st.playSound(QuestState.SOUND_ITEMGET);
		}
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q267_WrathOfVerdure();
	}
}