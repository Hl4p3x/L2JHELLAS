package com.l2jhellas.gameserver.scrips.quests;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.l2jhellas.gameserver.cache.HtmCache;
import com.l2jhellas.gameserver.instancemanager.ClanHallSiegeManager;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.gameserver.scrips.siegable.ClanHallSiegeEngine;
import com.l2jhellas.gameserver.scrips.siegable.SiegableHall;

public class Q655_AGrandPlanForTamingWildBeasts extends Quest
{
	public static final String QUEST_NAME = "Q655_AGrandPlanForTamingWildBeasts";
	
	private static final int MESSENGER = 35627;
	
	private static final int CRYSTAL_OF_PURITY = 8084;
	private static final int TRAINER_LICENSE = 8293;
	
	private static final int REQUIRED_CRYSTAL_COUNT = 10;
	private static final int REQUIRED_CLAN_LEVEL = 4;
	private static final int MINUTES_TO_SIEGE = 3600;
	
	private static final String PATH_TO_HTML = "data/html/script/siegablehall/WildBeastReserve/messenger_initial.htm";
	
	public Q655_AGrandPlanForTamingWildBeasts()
	{
		super(655, QUEST_NAME , "A Grand Plan for Taming Wild Beasts");
		
		setItemsIds(CRYSTAL_OF_PURITY, TRAINER_LICENSE);
		
		addStartNpc(MESSENGER);
		addTalkId(MESSENGER);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(QUEST_NAME);
		if (st == null)
			return htmltext;
		
		final long minutesToSiege = getMinutesToSiege();
		
		if (event.equalsIgnoreCase("35627-06.htm"))
		{
			final L2Clan clan = player.getClan();
			if (clan != null && clan.getLevel() >= REQUIRED_CLAN_LEVEL && clan.hasHideout() == 0 && player.isClanLeader() && minutesToSiege > 0 && minutesToSiege < MINUTES_TO_SIEGE)
			{
				st.setState(Quest.STATE_STARTED);
				st.set("cond", "1");
				st.playSound(QuestState.SOUND_ACCEPT);
			}
		}
		else if (event.equalsIgnoreCase("35627-11.htm"))
		{
			if (minutesToSiege > 0 && minutesToSiege < MINUTES_TO_SIEGE)
				htmltext = HtmCache.getInstance().getHtm(PATH_TO_HTML);
			else
				htmltext = htmltext.replace("%next_siege%", getSiegeDate());
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(QUEST_NAME);
		String htmltext = getNoQuestMsg();
		if (st == null)
			return htmltext;
		
		final long minutesToSiege = getMinutesToSiege();
		
		switch (st.getState())
		{
			case STATE_CREATED:
				final L2Clan clan = player.getClan();
				if (clan == null)
					return htmltext;
				
				if (minutesToSiege > 0 && minutesToSiege < MINUTES_TO_SIEGE)
				{
					if (player.isClanLeader())
					{
						if (clan.hasHideout() == 0)
							htmltext = (clan.getLevel() >= REQUIRED_CLAN_LEVEL) ? "35627-01.htm" : "35627-03.htm";
						else
							htmltext = "35627-04.htm";
					}
					else
					{
						if (clan.hasHideout() == ClanHallSiegeEngine.BEAST_FARM && minutesToSiege > 0 && minutesToSiege < MINUTES_TO_SIEGE)
							htmltext = HtmCache.getInstance().getHtm(PATH_TO_HTML);
						else
							htmltext = "35627-05.htm";
					}
				}
				else
					htmltext = getHtmlText("35627-02.htm").replace("%next_siege%", getSiegeDate());
				break;
			
			case STATE_STARTED:
				if (minutesToSiege < 0 || minutesToSiege > MINUTES_TO_SIEGE)
				{
					htmltext = "35627-07.htm";
					st.exitQuest(true);
				}
				else
				{
					int cond = st.getCond();
					if (cond == 1)
						htmltext = "35627-08.htm";
					else if (cond == 2)
					{
						htmltext = "35627-10.htm";
						st.set("cond", "3");
						st.playSound(QuestState.SOUND_MIDDLE);
						takeItems(player, CRYSTAL_OF_PURITY, -1);
						st.giveItems( TRAINER_LICENSE, 1);
					}
					else if (cond == 3)
						htmltext = "35627-09.htm";
				}
				break;
		}
		return htmltext;
	}

	private static String getSiegeDate()
	{
		final SiegableHall hall = ClanHallSiegeManager.getInstance().getSiegableHall(ClanHallSiegeEngine.BEAST_FARM);
		if (hall != null)
		{
			final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return sdf.format(hall.getSiegeDate());
		}
		return "Error in date.";
	}

	private static long getMinutesToSiege()
	{
		final SiegableHall hall = ClanHallSiegeManager.getInstance().getSiegableHall(ClanHallSiegeEngine.BEAST_FARM);
		if (hall != null)
			return (hall.getNextSiegeTime() - Calendar.getInstance().getTimeInMillis()) / 3600;
		
		return -1;
	}

	public void reward(L2PcInstance player, L2Npc npc)
	{
		final QuestState leaderQs = getClanLeaderQuestState(player, npc);

		if (leaderQs == null || leaderQs.getCond() != 1)
			return;
		
		if (leaderQs.dropItems(CRYSTAL_OF_PURITY, 1, REQUIRED_CRYSTAL_COUNT , 1000000))
			leaderQs.set("cond", "2");
	}
	
	public static void main(String[] args)
	{
		new Q655_AGrandPlanForTamingWildBeasts();
	}
}