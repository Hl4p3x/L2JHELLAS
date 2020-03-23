package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Rnd;

public class Q622_SpecialtyLiquorDelivery extends Quest
{
	private static final String qn = "Q622_SpecialtyLiquorDelivery";
	
	// Items
	private static final int DRINK = 7197;
	private static final int FEE = 7198;
	
	// NPCs
	private static final int JEREMY = 31521;
	private static final int PULIN = 31543;
	private static final int NAFF = 31544;
	private static final int CROCUS = 31545;
	private static final int KUBER = 31546;
	private static final int BEOLIN = 31547;
	private static final int LIETTA = 31267;
	
	// Rewards
	private static final int ADENA = 57;
	private static final int HASTE_POT = 1062;
	private static final int[] RECIPES =
	{
		6847,
		6849,
		6851
	};
	
	public Q622_SpecialtyLiquorDelivery()
	{
		super(622, qn, "Specialty Liquor Delivery");
		
		setItemsIds(DRINK, FEE);
		
		addStartNpc(JEREMY);
		addTalkId(JEREMY, PULIN, NAFF, CROCUS, KUBER, BEOLIN, LIETTA);
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
			st.giveItems(DRINK, 5);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31547-02.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(DRINK, 1);
			st.giveItems(FEE, 1);
		}
		else if (event.equalsIgnoreCase("31546-02.htm"))
		{
			st.set("cond", "3");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(DRINK, 1);
			st.giveItems(FEE, 1);
		}
		else if (event.equalsIgnoreCase("31545-02.htm"))
		{
			st.set("cond", "4");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(DRINK, 1);
			st.giveItems(FEE, 1);
		}
		else if (event.equalsIgnoreCase("31544-02.htm"))
		{
			st.set("cond", "5");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(DRINK, 1);
			st.giveItems(FEE, 1);
		}
		else if (event.equalsIgnoreCase("31543-02.htm"))
		{
			st.set("cond", "6");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(DRINK, 1);
			st.giveItems(FEE, 1);
		}
		else if (event.equalsIgnoreCase("31521-06.htm"))
		{
			st.set("cond", "7");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(FEE, 5);
		}
		else if (event.equalsIgnoreCase("31267-02.htm"))
		{
			if (Rnd.get(5) < 1)
				st.giveItems(RECIPES[Rnd.get(RECIPES.length)], 1);
			else
			{
				st.rewardItems(ADENA, 18800);
				st.rewardItems(HASTE_POT, 1);
			}
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
						if (cond >= 1 && cond <= 5)
							htmltext = "31521-04.htm";
						else if (cond == 6)
							htmltext = "31521-05.htm";
						else if (cond == 7)
							htmltext = "31521-06.htm";
						break;
					
					case BEOLIN:
						if (cond == 1 && st.getQuestItemsCount(DRINK) == 5)
							htmltext = "31547-01.htm";
						else if (cond >= 2)
							htmltext = "31547-03.htm";
						break;
					
					case KUBER:
						if (cond == 2 && st.getQuestItemsCount(DRINK) == 4)
							htmltext = "31546-01.htm";
						else if (cond >= 3)
							htmltext = "31546-03.htm";
						break;
					
					case CROCUS:
						if (cond == 3 && st.getQuestItemsCount(DRINK) == 3)
							htmltext = "31545-01.htm";
						else if (cond >= 4)
							htmltext = "31545-03.htm";
						break;
					
					case NAFF:
						if (cond == 4 && st.getQuestItemsCount(DRINK) == 2)
							htmltext = "31544-01.htm";
						else if (cond >= 5)
							htmltext = "31544-03.htm";
						break;
					
					case PULIN:
						if (cond == 5 && st.getQuestItemsCount(DRINK) == 1)
							htmltext = "31543-01.htm";
						else if (cond >= 6)
							htmltext = "31543-03.htm";
						break;
					
					case LIETTA:
						if (cond == 7)
							htmltext = "31267-01.htm";
						break;
				}
		}
		
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Q622_SpecialtyLiquorDelivery();
	}
}