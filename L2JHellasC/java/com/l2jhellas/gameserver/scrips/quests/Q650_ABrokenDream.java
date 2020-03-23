package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Rnd;

public class Q650_ABrokenDream extends Quest
{
	private static final String qn = "Q650_ABrokenDream";
	
	// NPC
	private static final int GHOST = 32054;
	
	// Item
	private static final int DREAM_FRAGMENT = 8514;
	
	// Monsters
	private static final int CREWMAN = 22027;
	private static final int VAGABOND = 22028;
	
	public Q650_ABrokenDream()
	{
		super(650, qn, "A Broken Dream");
		
		setItemsIds(DREAM_FRAGMENT);
		
		addStartNpc(GHOST);
		addTalkId(GHOST);
		addKillId(CREWMAN, VAGABOND);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("32054-01a.htm"))
		{
			st.set("cond", "1");
			st.setState(STATE_STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("32054-03.htm"))
		{
			if (!st.hasQuestItems(DREAM_FRAGMENT))
				htmltext = "32054-04.htm";
		}
		else if (event.equalsIgnoreCase("32054-05.htm"))
		{
			st.exitQuest(true);
			st.playSound(QuestState.SOUND_GIVEUP);
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
				QuestState st2 = player.getQuestState("Q117_TheOceanOfDistantStars");
				if (st2 != null && st2.isCompleted() && player.getLevel() >= 39)
					htmltext = "32054-01.htm";
				else
				{
					htmltext = "32054-00.htm";
					st.exitQuest(true);
				}
				break;
			
			case STATE_STARTED:
				htmltext = "32054-02.htm";
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
		
		if (Rnd.get(100) < 25)
		{
			st.giveItems(DREAM_FRAGMENT, 1);
			st.playSound(QuestState.SOUND_ITEMGET);
		}
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q650_ABrokenDream();
	}
}