package com.l2jhellas.gameserver.scrips.quests;

import java.util.HashMap;
import java.util.Map;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q378_MagnificentFeast extends Quest
{
	private static final String qn = "Q378_MagnificentFeast";
	
	// NPC
	private static final int RANSPO = 30594;
	
	// Items
	private static final int WINE_15 = 5956;
	private static final int WINE_30 = 5957;
	private static final int WINE_60 = 5958;
	private static final int MUSICALS_SCORE = 4421;
	private static final int JSALAD_RECIPE = 1455;
	private static final int JSAUCE_RECIPE = 1456;
	private static final int JSTEAK_RECIPE = 1457;
	private static final int RITRON_DESSERT = 5959;
	
	// Rewards
	private static final Map<String, int[]> Reward_list = new HashMap<>();
	{
		Reward_list.put("9", new int[]
		{
			847,
			1,
			5700
		});
		Reward_list.put("10", new int[]
		{
			846,
			2,
			0
		});
		Reward_list.put("12", new int[]
		{
			909,
			1,
			25400
		});
		Reward_list.put("17", new int[]
		{
			846,
			2,
			1200
		});
		Reward_list.put("18", new int[]
		{
			879,
			1,
			6900
		});
		Reward_list.put("20", new int[]
		{
			890,
			2,
			8500
		});
		Reward_list.put("33", new int[]
		{
			879,
			1,
			8100
		});
		Reward_list.put("34", new int[]
		{
			910,
			1,
			0
		});
		Reward_list.put("36", new int[]
		{
			848,
			1,
			2200
		});
	}
	
	public Q378_MagnificentFeast()
	{
		super(378, qn, "Magnificent Feast");
		
		addStartNpc(RANSPO);
		addTalkId(RANSPO);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30594-2.htm"))
		{
			st.set("cond", "1");
			st.setState(STATE_STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30594-4a.htm"))
		{
			if (st.getQuestItemsCount(WINE_15) >= 1)
			{
				st.set("cond", "2");
				st.set("score", "1");
				st.takeItems(WINE_15, 1);
				st.playSound(QuestState.SOUND_MIDDLE);
				
			}
			else
				htmltext = "30594-4.htm";
		}
		else if (event.equalsIgnoreCase("30594-4b.htm"))
		{
			if (st.getQuestItemsCount(WINE_30) >= 1)
			{
				st.set("cond", "2");
				st.set("score", "2");
				st.takeItems(WINE_30, 1);
				st.playSound(QuestState.SOUND_MIDDLE);
			}
			else
				htmltext = "30594-4.htm";
		}
		else if (event.equalsIgnoreCase("30594-4c.htm"))
		{
			if (st.getQuestItemsCount(WINE_60) >= 1)
			{
				st.set("cond", "2");
				st.set("score", "4");
				st.takeItems(WINE_60, 1);
				st.playSound(QuestState.SOUND_MIDDLE);
			}
			else
				htmltext = "30594-4.htm";
		}
		else if (event.equalsIgnoreCase("30594-6.htm"))
		{
			if (st.getQuestItemsCount(MUSICALS_SCORE) >= 1)
			{
				st.set("cond", "3");
				st.takeItems(MUSICALS_SCORE, 1);
				st.playSound(QuestState.SOUND_MIDDLE);
			}
			else
				htmltext = "30594-5.htm";
		}
		else
		{
			int score = st.getInt("score");
			if (event.equalsIgnoreCase("30594-8a.htm"))
			{
				if (st.getQuestItemsCount(JSALAD_RECIPE) >= 1)
				{
					st.set("cond", "4");
					st.takeItems(JSALAD_RECIPE, 1);
					st.playSound(QuestState.SOUND_MIDDLE);
					st.set("score", String.valueOf(score + 8));
				}
				else
					htmltext = "30594-8.htm";
			}
			else if (event.equalsIgnoreCase("30594-8b.htm"))
			{
				if (st.getQuestItemsCount(JSAUCE_RECIPE) >= 1)
				{
					st.set("cond", "4");
					st.takeItems(JSAUCE_RECIPE, 1);
					st.playSound(QuestState.SOUND_MIDDLE);
					st.set("score", String.valueOf(score + 16));
				}
				else
					htmltext = "30594-8.htm";
			}
			else if (event.equalsIgnoreCase("30594-8c.htm"))
			{
				if (st.getQuestItemsCount(JSTEAK_RECIPE) >= 1)
				{
					st.set("cond", "4");
					st.takeItems(JSTEAK_RECIPE, 1);
					st.playSound(QuestState.SOUND_MIDDLE);
					st.set("score", String.valueOf(score + 32));
				}
				else
					htmltext = "30594-8.htm";
			}
		}
		
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
					htmltext = "30594-1.htm";
				else
				{
					st.exitQuest(true);
					htmltext = "30594-0.htm";
				}
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				if (cond == 1)
					htmltext = "30594-3.htm";
				else if (cond == 2)
				{
					if (st.getQuestItemsCount(MUSICALS_SCORE) >= 1)
						htmltext = "30594-5a.htm";
					else
						htmltext = "30594-5.htm";
				}
				else if (cond == 3)
					htmltext = "30594-7.htm";
				else if (cond == 4)
				{
					String score = st.get("score");
					if (Reward_list.containsKey(score) && st.getQuestItemsCount(RITRON_DESSERT) >= 1)
					{
						htmltext = "30594-10.htm";
						
						st.takeItems(RITRON_DESSERT, 1);
						st.giveItems(Reward_list.get(score)[0], Reward_list.get(score)[1]);
						
						int adena = Reward_list.get(score)[2];
						if (adena > 0)
							st.rewardItems(57, adena);
						
						st.playSound(QuestState.SOUND_FINISH);
						st.exitQuest(true);
					}
					else
						htmltext = "30594-9.htm";
				}
		}
		
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Q378_MagnificentFeast();
	}
}