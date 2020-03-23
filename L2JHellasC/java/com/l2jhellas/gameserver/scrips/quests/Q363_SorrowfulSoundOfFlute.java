package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q363_SorrowfulSoundOfFlute extends Quest
{
	private static final String qn = "Q363_SorrowfulSoundOfFlute";
	
	// NPCs
	private static final int NANARIN = 30956;
	private static final int OPIX = 30595;
	private static final int ALDO = 30057;
	private static final int RANSPO = 30594;
	private static final int HOLVAS = 30058;
	private static final int BARBADO = 30959;
	private static final int POITAN = 30458;
	
	// Item
	private static final int NANARINS_FLUTE = 4319;
	private static final int BLACK_BEER = 4320;
	private static final int CLOTHES = 4318;
	
	// Reward
	private static final int THEME_OF_SOLITUDE = 4420;
	
	public Q363_SorrowfulSoundOfFlute()
	{
		super(363, qn, "Sorrowful Sound of Flute");
		
		setItemsIds(NANARINS_FLUTE, BLACK_BEER, CLOTHES);
		
		addStartNpc(NANARIN);
		addTalkId(NANARIN, OPIX, ALDO, RANSPO, HOLVAS, BARBADO, POITAN);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30956-02.htm"))
		{
			st.set("cond", "1");
			st.setState(STATE_STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30956-05.htm"))
		{
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(CLOTHES, 1);
		}
		else if (event.equalsIgnoreCase("30956-06.htm"))
		{
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(NANARINS_FLUTE, 1);
		}
		else if (event.equalsIgnoreCase("30956-07.htm"))
		{
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(BLACK_BEER, 1);
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
				if (player.getLevel() < 15)
				{
					htmltext = "30956-03.htm";
					st.exitQuest(true);
				}
				else
					htmltext = "30956-01.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case NANARIN:
						if (cond == 1)
							htmltext = "30956-02.htm";
						else if (cond == 2)
							htmltext = "30956-04.htm";
						else if (cond == 3)
							htmltext = "30956-08.htm";
						else if (cond == 4)
						{
							if (st.getInt("success") == 1)
							{
								htmltext = "30956-09.htm";
								st.giveItems(THEME_OF_SOLITUDE, 1);
								st.playSound(QuestState.SOUND_FINISH);
							}
							else
							{
								htmltext = "30956-10.htm";
								st.playSound(QuestState.SOUND_GIVEUP);
							}
							st.exitQuest(true);
						}
						break;
					
					case OPIX:
					case POITAN:
					case ALDO:
					case RANSPO:
					case HOLVAS:
						htmltext = npc.getNpcId() + "-01.htm";
						if (cond == 1)
						{
							st.set("cond", "2");
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						break;
					
					case BARBADO:
						if (cond == 3)
						{
							st.set("cond", "4");
							st.playSound(QuestState.SOUND_MIDDLE);
							
							if (st.hasQuestItems(NANARINS_FLUTE))
							{
								htmltext = "30959-02.htm";
								st.set("success", "1");
							}
							else
								htmltext = "30959-01.htm";
							
							st.takeItems(BLACK_BEER, -1);
							st.takeItems(CLOTHES, -1);
							st.takeItems(NANARINS_FLUTE, -1);
						}
						else if (cond == 4)
							htmltext = "30959-03.htm";
						break;
				}
		}
		
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Q363_SorrowfulSoundOfFlute();
	}
}