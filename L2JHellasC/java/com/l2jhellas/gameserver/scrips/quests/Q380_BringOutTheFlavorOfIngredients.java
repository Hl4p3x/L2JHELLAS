package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Rnd;

public class Q380_BringOutTheFlavorOfIngredients extends Quest
{
	private static final String qn = "Q380_BringOutTheFlavorOfIngredients";
	
	// Monsters
	private static final int DIRE_WOLF = 20205;
	private static final int KADIF_WEREWOLF = 20206;
	private static final int GIANT_MIST_LEECH = 20225;
	
	// Items
	private static final int RITRONS_FRUIT = 5895;
	private static final int MOON_FACE_FLOWER = 5896;
	private static final int LEECH_FLUIDS = 5897;
	private static final int ANTIDOTE = 1831;
	
	// Rewards
	private static final int RITRON_JELLY = 5960;
	private static final int JELLY_RECIPE = 5959;
	
	// Chance
	private static final int REC_CHANCE = 55;
	
	public Q380_BringOutTheFlavorOfIngredients()
	{
		super(380, qn, "Bring Out the Flavor of Ingredients!");
		
		setItemsIds(RITRONS_FRUIT, MOON_FACE_FLOWER, LEECH_FLUIDS);
		
		addStartNpc(30069); // Rollant
		addTalkId(30069);
		
		addKillId(DIRE_WOLF, KADIF_WEREWOLF, GIANT_MIST_LEECH);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30069-04.htm"))
		{
			st.set("cond", "1");
			st.setState(STATE_STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30069-12.htm"))
		{
			st.giveItems(JELLY_RECIPE, 1);
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
				if (player.getLevel() >= 24)
					htmltext = "30069-01.htm";
				else
				{
					htmltext = "30069-00.htm";
					st.exitQuest(true);
				}
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				if (cond == 1)
					htmltext = "30069-06.htm";
				else if (cond == 2)
				{
					if (st.getQuestItemsCount(ANTIDOTE) >= 2)
					{
						st.takeItems(RITRONS_FRUIT, -1);
						st.takeItems(MOON_FACE_FLOWER, -1);
						st.takeItems(LEECH_FLUIDS, -1);
						st.takeItems(ANTIDOTE, 2);
						st.set("cond", "3");
						st.playSound(QuestState.SOUND_MIDDLE);
						htmltext = "30069-07.htm";
					}
					else
						htmltext = "30069-06.htm";
				}
				else if (cond == 3)
				{
					st.set("cond", "4");
					st.playSound(QuestState.SOUND_MIDDLE);
					htmltext = "30069-08.htm";
				}
				else if (cond == 4)
				{
					st.set("cond", "5");
					st.playSound(QuestState.SOUND_MIDDLE);
					htmltext = "30069-09.htm";
				}
				else if (cond == 5)
				{
					st.set("cond", "6");
					st.playSound(QuestState.SOUND_MIDDLE);
					htmltext = "30069-10.htm";
				}
				else if (cond == 6)
				{
					st.giveItems(RITRON_JELLY, 1);
					if (Rnd.get(100) < REC_CHANCE)
						htmltext = "30069-11.htm";
					else
					{
						htmltext = "30069-13.htm";
						st.playSound(QuestState.SOUND_FINISH);
						st.exitQuest(true);
					}
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = checkPlayerCondition(player, npc, "cond", "1");
		if (st == null)
			return null;
		
		switch (npc.getNpcId())
		{
			case DIRE_WOLF:
				if (st.dropItems(RITRONS_FRUIT, 1, 4, 100000))
					if (st.getQuestItemsCount(MOON_FACE_FLOWER) == 20 && st.getQuestItemsCount(LEECH_FLUIDS) == 10)
						st.set("cond", "2");
				break;
			
			case KADIF_WEREWOLF:
				if (st.dropItems(MOON_FACE_FLOWER, 1, 20, 250000))
					if (st.getQuestItemsCount(RITRONS_FRUIT) == 4 && st.getQuestItemsCount(LEECH_FLUIDS) == 10)
						st.set("cond", "2");
				break;
			
			case GIANT_MIST_LEECH:
				if (st.dropItems(LEECH_FLUIDS, 1, 10, 250000))
					if (st.getQuestItemsCount(RITRONS_FRUIT) == 4 && st.getQuestItemsCount(MOON_FACE_FLOWER) == 20)
						st.set("cond", "2");
				break;
		}
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q380_BringOutTheFlavorOfIngredients();
	}
}