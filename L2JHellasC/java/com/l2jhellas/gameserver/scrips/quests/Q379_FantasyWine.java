package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Rnd;

public class Q379_FantasyWine extends Quest
{
	private static final String qn = "Q379_FantasyWine";
	
	// NPCs
	private static final int HARLAN = 30074;
	
	// Monsters
	private static final int ENKU_CHAMPION = 20291;
	private static final int ENKU_SHAMAN = 20292;
	
	// Items
	private static final int LEAF = 5893;
	private static final int STONE = 5894;
	
	public Q379_FantasyWine()
	{
		super(379, qn, "Fantasy Wine");
		
		setItemsIds(LEAF, STONE);
		
		addStartNpc(HARLAN);
		addTalkId(HARLAN);
		
		addKillId(ENKU_CHAMPION, ENKU_SHAMAN);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30074-3.htm"))
		{
			st.set("cond", "1");
			st.setState(STATE_STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30074-6.htm"))
		{
			final int leaf = st.getQuestItemsCount(LEAF);
			final int stone = st.getQuestItemsCount(STONE);
			
			if (leaf == 80 && stone == 100)
			{
				st.takeItems(LEAF, 80);
				st.takeItems(STONE, 100);
				int rand = Rnd.get(100);
				
				if (rand < 25)
				{
					st.giveItems(5956, 1);
					htmltext = "30074-6.htm";
				}
				else if (rand < 50)
				{
					st.giveItems(5957, 1);
					htmltext = "30074-7.htm";
				}
				else
				{
					st.giveItems(5958, 1);
					htmltext = "30074-8.htm";
				}
				
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
			}
			else
				htmltext = "30074-4.htm";
		}
		else if (event.equalsIgnoreCase("30074-2a.htm"))
			st.exitQuest(true);
		
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
				if (player.getLevel() >= 20)
					htmltext = "30074-0.htm";
				else
				{
					htmltext = "30074-0a.htm";
					st.exitQuest(true);
				}
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				int leaf = st.getQuestItemsCount(LEAF);
				int stone = st.getQuestItemsCount(STONE);
				
				if (cond == 1)
				{
					if (leaf < 80 && stone < 100)
						htmltext = "30074-4.htm";
					else if (leaf == 80 && stone < 100)
						htmltext = "30074-4a.htm";
					else if (leaf < 80 & stone == 100)
						htmltext = "30074-4b.htm";
				}
				else if (cond == 2 && leaf == 80 && stone == 100)
					htmltext = "30074-5.htm";
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = checkPlayerState(player, npc, STATE_STARTED);
		if (st == null)
			return null;
		
		int npcId = npc.getNpcId();
		if (npcId == ENKU_CHAMPION && st.getQuestItemsCount(LEAF) < 80)
		{
			st.giveItems(LEAF, 1);
			if (st.getQuestItemsCount(LEAF) >= 80 && st.getQuestItemsCount(STONE) >= 100)
			{
				st.set("cond", "2");
				st.playSound(QuestState.SOUND_MIDDLE);
			}
			else
				st.playSound(QuestState.SOUND_ITEMGET);
		}
		else if (npcId == ENKU_SHAMAN && st.getQuestItemsCount(STONE) < 100)
		{
			st.giveItems(STONE, 1);
			if (st.getQuestItemsCount(LEAF) >= 80 && st.getQuestItemsCount(STONE) >= 100)
			{
				st.set("cond", "2");
				st.playSound(QuestState.SOUND_MIDDLE);
			}
			else
				st.playSound(QuestState.SOUND_ITEMGET);
		}
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q379_FantasyWine();
	}
}