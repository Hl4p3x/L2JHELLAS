package com.l2jhellas.gameserver.scrips.quests;

import java.util.HashMap;
import java.util.Map;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q628_HuntOfTheGoldenRamMercenaryForce extends Quest
{
	private static final String qn = "Q628_HuntOfTheGoldenRamMercenaryForce";
	
	// NPCs
	private static final int KAHMAN = 31554;
	
	// Items
	private static final int CHITIN = 7248;
	private static final int CHITIN2 = 7249;
	private static final int RECRUIT = 7246;
	private static final int SOLDIER = 7247;
	
	// Chance to drop a qItem
	private static final Map<Integer, Integer> chances = new HashMap<>();
	{
		chances.put(21508, 250000);
		chances.put(21509, 210000);
		chances.put(21510, 260000);
		chances.put(21511, 260000);
		chances.put(21512, 370000);
		
		chances.put(21513, 250000);
		chances.put(21514, 210000);
		chances.put(21515, 250000);
		chances.put(21516, 260000);
		chances.put(21517, 370000);
	}
	
	public Q628_HuntOfTheGoldenRamMercenaryForce()
	{
		super(628, qn, "Hunt of the Golden Ram Mercenary Force");
		
		setItemsIds(CHITIN, CHITIN2, RECRUIT, SOLDIER);
		
		addStartNpc(KAHMAN);
		addTalkId(KAHMAN);
		
		addKillId(21508, 21509, 21510, 21511, 21512, 21513, 21514, 21515, 21516, 21517);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31554-02.htm"))
		{
			st.set("cond", "1");
			st.setState(STATE_STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31554-03a.htm"))
		{
			if (st.getQuestItemsCount(CHITIN) >= 100 && st.getInt("cond") == 1) // Giving Recruit Medals
			{
				st.set("cond", "2");
				st.takeItems(CHITIN, -1);
				st.giveItems(RECRUIT, 1);
				htmltext = "31554-04.htm";
				st.playSound(QuestState.SOUND_MIDDLE);
			}
		}
		else if (event.equalsIgnoreCase("31554-07.htm")) // Cancel Quest
		{
			st.playSound(QuestState.SOUND_GIVEUP);
			st.exitQuest(true);
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
				if (player.getLevel() >= 66)
					htmltext = "31554-01.htm";
				else
				{
					htmltext = "31554-01a.htm";
					st.exitQuest(true);
				}
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				if (cond == 1)
				{
					if (st.getQuestItemsCount(CHITIN) >= 100)
						htmltext = "31554-03.htm";
					else
						htmltext = "31554-03a.htm";
				}
				else if (cond == 2)
				{
					if (st.getQuestItemsCount(CHITIN) >= 100 && st.getQuestItemsCount(CHITIN2) >= 100)
					{
						htmltext = "31554-05.htm";
						st.takeItems(CHITIN, -1);
						st.takeItems(CHITIN2, -1);
						st.takeItems(RECRUIT, 1);
						st.giveItems(SOLDIER, 1);
						st.set("cond", "3");
						st.playSound(QuestState.SOUND_FINISH);
					}
					else if (!st.hasQuestItems(CHITIN) && !st.hasQuestItems(CHITIN2))
						htmltext = "31554-04b.htm";
					else
						htmltext = "31554-04a.htm";
				}
				else if (cond == 3)
					htmltext = "31554-05a.htm";
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
				
		int cond = st.getInt("cond");
		int npcId = npc.getNpcId();
		switch (npcId)
		{
			case 21508:
			case 21509:
			case 21510:
			case 21511:
			case 21512:
				if (cond == 1 || cond == 2)
					st.dropItems(CHITIN, 1, 100, chances.get(npcId));
				break;
			
			case 21513:
			case 21514:
			case 21515:
			case 21516:
			case 21517:
				if (cond == 2)
					st.dropItems(CHITIN2, 1, 100, chances.get(npcId));
				break;
		}
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q628_HuntOfTheGoldenRamMercenaryForce();
	}
}