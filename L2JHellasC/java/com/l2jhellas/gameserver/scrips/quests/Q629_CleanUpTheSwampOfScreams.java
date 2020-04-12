package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q629_CleanUpTheSwampOfScreams extends Quest
{
	private static final String qn = "Q629_CleanUpTheSwampOfScreams";
	
	// NPC
	private static final int CAPTAIN = 31553;
	
	// ITEMS
	private static final int CLAWS = 7250;
	private static final int COIN = 7251;
	
	// MOBS / CHANCES
	private static final int[][] CHANCE =
	{
		{
			21508,
			500000
		},
		{
			21509,
			430000
		},
		{
			21510,
			520000
		},
		{
			21511,
			570000
		},
		{
			21512,
			740000
		},
		{
			21513,
			530000
		},
		{
			21514,
			530000
		},
		{
			21515,
			540000
		},
		{
			21516,
			550000
		},
		{
			21517,
			560000
		}
	};
	
	public Q629_CleanUpTheSwampOfScreams()
	{
		super(629, qn, "Clean up the Swamp of Screams");
		
		setItemsIds(CLAWS, COIN);
		
		addStartNpc(CAPTAIN);
		addTalkId(CAPTAIN);
		
		for (int[] i : CHANCE)
			addKillId(i[0]);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31553-1.htm"))
		{
			if (player.getLevel() >= 66)
			{
				st.set("cond", "1");
				st.setState(STATE_STARTED);
				st.playSound(QuestState.SOUND_ACCEPT);
			}
			else
			{
				htmltext = "31553-0a.htm";
				st.exitQuest(true);
			}
		}
		else if (event.equalsIgnoreCase("31553-3.htm"))
		{
			if (st.getQuestItemsCount(CLAWS) >= 100)
			{
				st.takeItems(CLAWS, 100);
				st.giveItems(COIN, 20);
			}
			else
				htmltext = "31553-3a.htm";
		}
		else if (event.equalsIgnoreCase("31553-5.htm"))
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
		
		if (st.hasAtLeastOneQuestItem(7246, 7247))
		{
			switch (st.getState())
			{
				case STATE_CREATED:
					if (player.getLevel() >= 66)
						htmltext = "31553-0.htm";
					else
					{
						htmltext = "31553-0a.htm";
						st.exitQuest(true);
					}
					break;
				
				case STATE_STARTED:
					if (st.getQuestItemsCount(CLAWS) >= 100)
						htmltext = "31553-2.htm";
					else
						htmltext = "31553-1a.htm";
					break;
			}
		}
		else
		{
			htmltext = "31553-6.htm";
			st.exitQuest(true);
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = getRandomPartyMemberState(player, npc, STATE_STARTED);
		if (st == null)
			return null;
				
		st.dropItems(CLAWS, 1, 100, CHANCE[npc.getNpcId() - 21508][1]);
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q629_CleanUpTheSwampOfScreams();
	}
}