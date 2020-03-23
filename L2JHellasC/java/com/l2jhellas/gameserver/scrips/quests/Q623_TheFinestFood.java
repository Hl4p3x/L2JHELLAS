package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Rnd;

public class Q623_TheFinestFood extends Quest
{
	private static final String qn = "Q623_TheFinestFood";
	
	// Items
	private static final int LEAF = 7199;
	private static final int MEAT = 7200;
	private static final int HORN = 7201;
	
	// NPC
	private static final int JEREMY = 31521;
	
	// Monsters
	private static final int FLAVA = 21316;
	private static final int BUFFALO = 21315;
	private static final int ANTELOPE = 21318;
	
	public Q623_TheFinestFood()
	{
		super(623, qn, "The Finest Food");
		
		setItemsIds(LEAF, MEAT, HORN);
		
		addStartNpc(JEREMY);
		addTalkId(JEREMY);
		
		addKillId(FLAVA, BUFFALO, ANTELOPE);
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
			if (player.getLevel() >= 71)
			{
				st.set("cond", "1");
				st.setState(STATE_STARTED);
				st.playSound(QuestState.SOUND_ACCEPT);
			}
			else
				htmltext = "31521-03.htm";
		}
		else if (event.equalsIgnoreCase("31521-05.htm"))
		{
			st.takeItems(LEAF, -1);
			st.takeItems(MEAT, -1);
			st.takeItems(HORN, -1);
			
			int luck = Rnd.get(100);
			if (luck < 11)
			{
				st.rewardItems(57, 25000);
				st.giveItems(6849, 1);
			}
			else if (luck < 23)
			{
				st.rewardItems(57, 65000);
				st.giveItems(6847, 1);
			}
			else if (luck < 33)
			{
				st.rewardItems(57, 25000);
				st.giveItems(6851, 1);
			}
			else
			{
				st.rewardItems(57, 73000);
				st.rewardExpAndSp(230000, 18250);
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
				htmltext = "31521-01.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				if (cond == 1)
					htmltext = "31521-06.htm";
				else if (cond == 2)
				{
					if (st.getQuestItemsCount(LEAF) >= 100 && st.getQuestItemsCount(MEAT) >= 100 && st.getQuestItemsCount(HORN) >= 100)
						htmltext = "31521-04.htm";
					else
						htmltext = "31521-07.htm";
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		L2PcInstance partyMember = getRandomPartyMember(player, npc, "1");
		if (partyMember == null)
			return null;
		
		QuestState st = partyMember.getQuestState(qn);
		
		if (Rnd.get(100) < 66)
		{
			switch (npc.getNpcId())
			{
				case FLAVA:
					if (st.getQuestItemsCount(LEAF) < 100)
					{
						st.giveItems(LEAF, 1);
						if (st.getQuestItemsCount(LEAF) >= 100 && st.getQuestItemsCount(MEAT) >= 100 && st.getQuestItemsCount(HORN) >= 100)
						{
							st.set("cond", "2");
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						else
							st.playSound(QuestState.SOUND_ITEMGET);
					}
					break;
				
				case BUFFALO:
					if (st.getQuestItemsCount(MEAT) < 100)
					{
						st.giveItems(MEAT, 1);
						if (st.getQuestItemsCount(LEAF) >= 100 && st.getQuestItemsCount(MEAT) >= 100 && st.getQuestItemsCount(HORN) >= 100)
						{
							st.set("cond", "2");
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						else
							st.playSound(QuestState.SOUND_ITEMGET);
					}
					break;
				
				case ANTELOPE:
					if (st.getQuestItemsCount(HORN) < 100)
					{
						st.giveItems(HORN, 1);
						if (st.getQuestItemsCount(LEAF) >= 100 && st.getQuestItemsCount(MEAT) >= 100 && st.getQuestItemsCount(HORN) >= 100)
						{
							st.set("cond", "2");
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						else
							st.playSound(QuestState.SOUND_ITEMGET);
					}
					break;
			}
		}
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q623_TheFinestFood();
	}
}