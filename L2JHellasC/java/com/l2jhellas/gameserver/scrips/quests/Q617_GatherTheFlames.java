package com.l2jhellas.gameserver.scrips.quests;

import java.util.HashMap;
import java.util.Map;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Rnd;
import com.l2jhellas.util.Util;

public class Q617_GatherTheFlames extends Quest
{
	private static final String qn = "Q617_GatherTheFlames";
	
	// NPCs
	private static final int HILDA = 31271;
	private static final int VULCAN = 31539;
	private static final int ROONEY = 32049;
	
	// Items
	private static final int TORCH = 7264;
	
	// Droplist
	private static final Map<Integer, Integer> droplist = new HashMap<>();
	{
		droplist.put(21381, 510000);
		droplist.put(21653, 510000);
		droplist.put(21387, 530000);
		droplist.put(21655, 530000);
		droplist.put(21390, 560000);
		droplist.put(21656, 690000);
		droplist.put(21389, 550000);
		droplist.put(21388, 530000);
		droplist.put(21383, 510000);
		droplist.put(21392, 560000);
		droplist.put(21382, 600000);
		droplist.put(21654, 520000);
		droplist.put(21384, 640000);
		droplist.put(21394, 510000);
		droplist.put(21395, 560000);
		droplist.put(21385, 520000);
		droplist.put(21391, 550000);
		droplist.put(21393, 580000);
		droplist.put(21657, 570000);
		droplist.put(21386, 520000);
		droplist.put(21652, 490000);
		droplist.put(21378, 490000);
		droplist.put(21376, 480000);
		droplist.put(21377, 480000);
		droplist.put(21379, 590000);
		droplist.put(21380, 490000);
	}
	
	// Rewards
	private static final int reward[] =
	{
		6881,
		6883,
		6885,
		6887,
		6891,
		6893,
		6895,
		6897,
		6899,
		7580
	};
	
	public Q617_GatherTheFlames()
	{
		super(617, qn, "Gather the Flames");
		
		setItemsIds(TORCH);
		
		addStartNpc(VULCAN, HILDA);
		addTalkId(VULCAN, HILDA, ROONEY);
		
		for (int mobs : droplist.keySet())
			addKillId(mobs);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31539-03.htm"))
		{
			st.set("cond", "1");
			st.setState(STATE_STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31271-03.htm"))
		{
			st.set("cond", "1");
			st.setState(STATE_STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31539-05.htm"))
		{
			if (st.getQuestItemsCount(TORCH) >= 1000)
			{
				htmltext = "31539-07.htm";
				st.takeItems(TORCH, 1000);
				st.giveItems(reward[Rnd.get(reward.length)], 1);
			}
		}
		else if (event.equalsIgnoreCase("31539-08.htm"))
		{
			st.takeItems(TORCH, -1);
			st.exitQuest(true);
		}
		else if (Util.isDigit(event))
		{
			if (st.getQuestItemsCount(TORCH) >= 1200)
			{
				htmltext = "32049-03.htm";
				st.takeItems(TORCH, 1200);
				st.giveItems(Integer.valueOf(event), 1);
			}
			else
				htmltext = "32049-02.htm";
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
				switch (npc.getNpcId())
				{
					case VULCAN:
						if (player.getLevel() >= 74)
							htmltext = "31539-01.htm";
						else
						{
							htmltext = "31539-02.htm";
							st.exitQuest(true);
						}
						break;
					
					case HILDA:
						if (player.getLevel() >= 74)
							htmltext = "31271-02.htm";
						else
						{
							htmltext = "31271-01.htm";
							st.exitQuest(true);
						}
						break;
				}
				break;
			
			case STATE_STARTED:
				switch (npc.getNpcId())
				{
					case VULCAN:
						if (st.getQuestItemsCount(TORCH) >= 1000)
							htmltext = "31539-04.htm";
						else
							htmltext = "31539-05.htm";
						break;
					
					case HILDA:
						htmltext = "31271-04.htm";
						break;
					
					case ROONEY:
						if (st.getQuestItemsCount(TORCH) >= 1200)
							htmltext = "32049-01.htm";
						else
							htmltext = "32049-02.htm";
						break;
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = getRandomPartyMemberState(player, npc, STATE_STARTED);
		if (st == null)
			return null;
				
		st.dropItems(TORCH, 1, -1, droplist.get(npc.getNpcId()));
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q617_GatherTheFlames();
	}
}