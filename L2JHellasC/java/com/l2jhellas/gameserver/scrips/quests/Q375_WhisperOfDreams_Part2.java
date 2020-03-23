package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Rnd;

public class Q375_WhisperOfDreams_Part2 extends Quest
{
	private static final String qn = "Q375_WhisperOfDreams_Part2";
	
	// NPCs
	private static final int MANAKIA = 30515;
	
	// Monsters
	private static final int KARIK = 20629;
	private static final int CAVE_HOWLER = 20624;
	
	// Items
	private static final int MYSTERIOUS_STONE = 5887;
	private static final int KARIK_HORN = 5888;
	private static final int CAVE_HOWLER_SKULL = 5889;
	
	// Rewards : A grade robe recipes
	private static final int[] REWARDS =
	{
		5348,
		5350,
		5352
	};
	
	public Q375_WhisperOfDreams_Part2()
	{
		super(375, qn, "Whisper of Dreams, Part 2");
		
		setItemsIds(KARIK_HORN, CAVE_HOWLER_SKULL);
		
		addStartNpc(MANAKIA);
		addTalkId(MANAKIA);
		
		addKillId(KARIK, CAVE_HOWLER);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		// Manakia
		if (event.equalsIgnoreCase("30515-03.htm"))
		{
			st.set("cond", "1");
			st.setState(STATE_STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
			st.takeItems(MYSTERIOUS_STONE, 1);
		}
		else if (event.equalsIgnoreCase("30515-07.htm"))
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
				if (st.hasQuestItems(MYSTERIOUS_STONE))
				{
					if (player.getLevel() < 60)
					{
						htmltext = "30515-01.htm";
						st.exitQuest(true);
					}
					else
						htmltext = "30515-02.htm";
				}
				else
				{
					htmltext = "30515-01.htm";
					st.exitQuest(true);
				}
				break;
			
			case STATE_STARTED:
				if (st.getQuestItemsCount(KARIK_HORN) >= 100 && st.getQuestItemsCount(CAVE_HOWLER_SKULL) >= 100)
				{
					htmltext = "30515-05.htm";
					st.takeItems(KARIK_HORN, 100);
					st.takeItems(CAVE_HOWLER_SKULL, 100);
					st.giveItems(REWARDS[Rnd.get(REWARDS.length)], 1);
					st.playSound(QuestState.SOUND_MIDDLE);
				}
				else
					htmltext = "30515-04.htm";
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		// Drop horn or skull to anyone.
		L2PcInstance partyMember = getRandomPartyMemberState(player, npc, STATE_STARTED);
		if (partyMember == null)
			return null;
		
		QuestState st = partyMember.getQuestState(qn);
		
		switch (npc.getNpcId())
		{
			case KARIK:
				st.dropItemsAlways(KARIK_HORN, 1, 100);
				break;
			
			case CAVE_HOWLER:
				st.dropItems(CAVE_HOWLER_SKULL, 1, 100, 900000);
				break;
		}
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q375_WhisperOfDreams_Part2();
	}
}