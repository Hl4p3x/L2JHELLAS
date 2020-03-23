package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Rnd;

public class Q621_EggDelivery extends Quest
{
	private static final String qn = "Q621_EggDelivery";
	
	// Items
	private static final int EGG = 7195;
	private static final int FEE = 7196;
	
	// NPCs
	private static final int JEREMY = 31521;
	private static final int PULIN = 31543;
	private static final int NAFF = 31544;
	private static final int CROCUS = 31545;
	private static final int KUBER = 31546;
	private static final int BEOLIN = 31547;
	private static final int VALENTINE = 31584;
	
	// Rewards
	private static final int HASTE_POT = 1062;
	private static final int[] RECIPES =
	{
		6847,
		6849,
		6851
	};
	
	public Q621_EggDelivery()
	{
		super(621, qn, "Egg Delivery");
		
		setItemsIds(EGG, FEE);
		
		addStartNpc(JEREMY);
		addTalkId(JEREMY, PULIN, NAFF, CROCUS, KUBER, BEOLIN, VALENTINE);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31521-02.htm"))
		{
			st.set("cond", "1");
			st.setState(STATE_STARTED);
			st.giveItems(EGG, 5);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31543-02.htm"))
		{
			st.set("cond", "2");
			st.takeItems(EGG, 1);
			st.giveItems(FEE, 1);
			st.playSound(QuestState.SOUND_ITEMGET);
		}
		else if (event.equalsIgnoreCase("31544-02.htm"))
		{
			st.set("cond", "3");
			st.takeItems(EGG, 1);
			st.giveItems(FEE, 1);
			st.playSound(QuestState.SOUND_ITEMGET);
		}
		else if (event.equalsIgnoreCase("31545-02.htm"))
		{
			st.set("cond", "4");
			st.takeItems(EGG, 1);
			st.giveItems(FEE, 1);
			st.playSound(QuestState.SOUND_ITEMGET);
		}
		else if (event.equalsIgnoreCase("31546-02.htm"))
		{
			st.set("cond", "5");
			st.takeItems(EGG, 1);
			st.giveItems(FEE, 1);
			st.playSound(QuestState.SOUND_ITEMGET);
		}
		else if (event.equalsIgnoreCase("31547-02.htm"))
		{
			st.set("cond", "6");
			st.takeItems(EGG, 1);
			st.giveItems(FEE, 1);
			st.playSound(QuestState.SOUND_ITEMGET);
		}
		else if (event.equalsIgnoreCase("31521-06.htm"))
		{
			if (st.getQuestItemsCount(FEE) < 5)
			{
				htmltext = "31521-08.htm";
				st.playSound(QuestState.SOUND_GIVEUP);
				st.exitQuest(true);
			}
			else
			{
				st.set("cond", "7");
				st.takeItems(FEE, 5);
				st.playSound(QuestState.SOUND_MIDDLE);
			}
		}
		else if (event.equalsIgnoreCase("31584-02.htm"))
		{
			if (Rnd.get(5) < 1)
			{
				st.rewardItems(RECIPES[Rnd.get(3)], 1);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
			}
			else
			{
				st.rewardItems(57, 18800);
				st.rewardItems(HASTE_POT, 1);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
			}
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
				if (player.getLevel() >= 68)
					htmltext = "31521-01.htm";
				else
				{
					htmltext = "31521-03.htm";
					st.exitQuest(true);
				}
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				
				switch (npc.getNpcId())
				{
					case JEREMY:
						if (cond == 1)
							htmltext = "31521-04.htm";
						else if (cond == 6)
							htmltext = "31521-05.htm";
						else if (cond == 7)
							htmltext = "31521-07.htm";
						break;
					
					case PULIN:
						if (cond == 1 && st.getQuestItemsCount(EGG) == 5)
							htmltext = "31543-01.htm";
						else if (cond >= 2)
							htmltext = "31543-03.htm";
						break;
					
					case NAFF:
						if (cond == 2 && st.getQuestItemsCount(EGG) == 4)
							htmltext = "31544-01.htm";
						else if (cond >= 3)
							htmltext = "31544-03.htm";
						break;
					
					case CROCUS:
						if (cond == 3 && st.getQuestItemsCount(EGG) == 3)
							htmltext = "31545-01.htm";
						else if (cond >= 4)
							htmltext = "31545-03.htm";
						break;
					
					case KUBER:
						if (cond == 4 && st.getQuestItemsCount(EGG) == 2)
							htmltext = "31546-01.htm";
						else if (cond >= 5)
							htmltext = "31546-03.htm";
						break;
					
					case BEOLIN:
						if (cond == 5 && st.getQuestItemsCount(EGG) == 1)
							htmltext = "31547-01.htm";
						else if (cond >= 6)
							htmltext = "31547-03.htm";
						break;
					
					case VALENTINE:
						if (cond == 7)
							htmltext = "31584-01.htm";
						break;
				}
				break;
		}
		
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Q621_EggDelivery();
	}
}