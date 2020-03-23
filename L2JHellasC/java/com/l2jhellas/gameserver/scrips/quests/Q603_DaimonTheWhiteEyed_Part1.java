package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q603_DaimonTheWhiteEyed_Part1 extends Quest
{
	private static final String qn = "Q603_DaimonTheWhiteEyed_Part1";
	
	// NPCs
	private static final int EYE_OF_ARGOS = 31683;
	private static final int MYSTERIOUS_TABLET_1 = 31548;
	private static final int MYSTERIOUS_TABLET_2 = 31549;
	private static final int MYSTERIOUS_TABLET_3 = 31550;
	private static final int MYSTERIOUS_TABLET_4 = 31551;
	private static final int MYSTERIOUS_TABLET_5 = 31552;
	
	// Monsters
	private static final int CANYON_BANDERSNATCH_SLAVE = 21297;
	private static final int BUFFALO_SLAVE = 21299;
	private static final int GRENDEL_SLAVE = 21304;
	
	// Items
	private static final int EVIL_SPIRIT_BEADS = 7190;
	private static final int BROKEN_CRYSTAL = 7191;
	private static final int UNFINISHED_SUMMON_CRYSTAL = 7192;
	
	public Q603_DaimonTheWhiteEyed_Part1()
	{
		super(603, qn, "Daimon the White-Eyed - Part 1");
		
		setItemsIds(EVIL_SPIRIT_BEADS, BROKEN_CRYSTAL);
		
		addStartNpc(EYE_OF_ARGOS);
		addTalkId(EYE_OF_ARGOS, MYSTERIOUS_TABLET_1, MYSTERIOUS_TABLET_2, MYSTERIOUS_TABLET_3, MYSTERIOUS_TABLET_4, MYSTERIOUS_TABLET_5);
		
		addKillId(BUFFALO_SLAVE, GRENDEL_SLAVE, CANYON_BANDERSNATCH_SLAVE);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		// Eye of Argos
		if (event.equalsIgnoreCase("31683-03.htm"))
		{
			st.set("cond", "1");
			st.setState(STATE_STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31683-06.htm"))
		{
			if (st.getQuestItemsCount(BROKEN_CRYSTAL) > 4)
			{
				st.set("cond", "7");
				st.takeItems(BROKEN_CRYSTAL, -1);
				st.playSound(QuestState.SOUND_MIDDLE);
			}
			else
				htmltext = "31683-07.htm";
		}
		else if (event.equalsIgnoreCase("31683-10.htm"))
		{
			if (st.getQuestItemsCount(EVIL_SPIRIT_BEADS) > 199)
			{
				st.takeItems(EVIL_SPIRIT_BEADS, -1);
				st.giveItems(UNFINISHED_SUMMON_CRYSTAL, 1);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
			}
			else
			{
				st.set("cond", "7");
				htmltext = "31683-11.htm";
			}
		}
		// Mysterious tablets
		else if (event.equalsIgnoreCase("31548-02.htm"))
		{
			st.set("cond", "2");
			st.giveItems(BROKEN_CRYSTAL, 1);
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("31549-02.htm"))
		{
			st.set("cond", "3");
			st.giveItems(BROKEN_CRYSTAL, 1);
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("31550-02.htm"))
		{
			st.set("cond", "4");
			st.giveItems(BROKEN_CRYSTAL, 1);
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("31551-02.htm"))
		{
			st.set("cond", "5");
			st.giveItems(BROKEN_CRYSTAL, 1);
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("31552-02.htm"))
		{
			st.set("cond", "6");
			st.giveItems(BROKEN_CRYSTAL, 1);
			st.playSound(QuestState.SOUND_MIDDLE);
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
				if (player.getLevel() < 73)
				{
					htmltext = "31683-02.htm";
					st.exitQuest(true);
				}
				else
					htmltext = "31683-01.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case EYE_OF_ARGOS:
						if (cond >= 1 && cond <= 5)
							htmltext = "31683-04.htm";
						else if (cond == 6)
							htmltext = "31683-05.htm";
						else if (cond == 7)
							htmltext = "31683-08.htm";
						else if (cond == 8)
							htmltext = "31683-09.htm";
						break;
					
					case MYSTERIOUS_TABLET_1:
						if (cond == 1)
							htmltext = "31548-01.htm";
						else if (cond >= 2)
							htmltext = "31548-03.htm";
						break;
					
					case MYSTERIOUS_TABLET_2:
						if (cond == 2)
							htmltext = "31549-01.htm";
						else if (cond >= 3)
							htmltext = "31549-03.htm";
						break;
					
					case MYSTERIOUS_TABLET_3:
						if (cond == 3)
							htmltext = "31550-01.htm";
						else if (cond >= 4)
							htmltext = "31550-03.htm";
						break;
					
					case MYSTERIOUS_TABLET_4:
						if (cond == 4)
							htmltext = "31551-01.htm";
						else if (cond >= 5)
							htmltext = "31551-03.htm";
						break;
					
					case MYSTERIOUS_TABLET_5:
						if (cond == 5)
							htmltext = "31552-01.htm";
						else if (cond >= 6)
							htmltext = "31552-03.htm";
						break;
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		L2PcInstance partyMember = getRandomPartyMember(player, npc, "7");
		if (partyMember == null)
			return null;
		
		QuestState st = partyMember.getQuestState(qn);
		
		if (st.dropItemsAlways(EVIL_SPIRIT_BEADS, 1, 200))
			st.set("cond", "8");
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q603_DaimonTheWhiteEyed_Part1();
	}
}