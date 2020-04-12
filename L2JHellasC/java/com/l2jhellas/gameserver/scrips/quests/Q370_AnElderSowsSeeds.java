package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q370_AnElderSowsSeeds extends Quest
{
	private static final String qn = "Q370_AnElderSowsSeeds";
	
	// NPC
	private static final int CASIAN = 30612;
	
	// Items
	private static final int SPELLBOOK_PAGE = 5916;
	private static final int CHAPTER_OF_FIRE = 5917;
	private static final int CHAPTER_OF_WATER = 5918;
	private static final int CHAPTER_OF_WIND = 5919;
	private static final int CHAPTER_OF_EARTH = 5920;
	
	public Q370_AnElderSowsSeeds()
	{
		super(370, qn, "An Elder Sows Seeds");
		
		setItemsIds(SPELLBOOK_PAGE, CHAPTER_OF_FIRE, CHAPTER_OF_WATER, CHAPTER_OF_WIND, CHAPTER_OF_EARTH);
		
		addStartNpc(CASIAN);
		addTalkId(CASIAN);
		
		addKillId(20082, 20084, 20086, 20089, 20090);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30612-3.htm"))
		{
			st.set("cond", "1");
			st.setState(STATE_STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30612-6.htm"))
		{
			if (st.getQuestItemsCount(CHAPTER_OF_FIRE) > 0 && st.getQuestItemsCount(CHAPTER_OF_WATER) > 0 && st.getQuestItemsCount(CHAPTER_OF_WIND) > 0 && st.getQuestItemsCount(CHAPTER_OF_EARTH) > 0)
			{
				htmltext = "30612-8.htm";
				st.takeItems(CHAPTER_OF_FIRE, 1);
				st.takeItems(CHAPTER_OF_WATER, 1);
				st.takeItems(CHAPTER_OF_WIND, 1);
				st.takeItems(CHAPTER_OF_EARTH, 1);
				st.rewardItems(57, 3600);
			}
		}
		else if (event.equalsIgnoreCase("30612-9.htm"))
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
				if (player.getLevel() >= 28)
					htmltext = "30612-0.htm";
				else
				{
					htmltext = "30612-0a.htm";
					st.exitQuest(true);
				}
				break;
			
			case STATE_STARTED:
				htmltext = "30612-4.htm";
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState qs = getRandomPartyMemberState(player, npc, STATE_STARTED);
		if (qs == null)
			return null;
				
		qs.dropItemsAlways(SPELLBOOK_PAGE, 1, -1);
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q370_AnElderSowsSeeds();
	}
}