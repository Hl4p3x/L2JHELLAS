package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Util;

public class Q644_GraveRobberAnnihilation extends Quest
{
	private static final String qn = "Q644_GraveRobberAnnihilation";
	
	// Item
	private static final int GOODS = 8088;
	
	// Rewards
	private static final int[][] rewards =
	{
		{
			1865,
			30
		},
		{
			1867,
			40
		},
		{
			1872,
			40
		},
		{
			1871,
			30
		},
		{
			1870,
			30
		},
		{
			1869,
			30
		}
	};
	
	// NPC
	private static final int KARUDA = 32017;
	
	public Q644_GraveRobberAnnihilation()
	{
		super(644, qn, "Grave Robber Annihilation");
		
		setItemsIds(GOODS);
		
		addStartNpc(KARUDA);
		addTalkId(KARUDA);
		
		addKillId(22003, 22004, 22005, 22006, 22008);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("32017-02.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (Util.isDigit(event))
		{
			if (st.getQuestItemsCount(GOODS) == 120)
			{
				htmltext = "32017-04.htm";
				st.takeItems(GOODS, -1);
				
				int reward[] = rewards[Integer.parseInt(event)];
				st.rewardItems(reward[0], reward[1]);
				
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
			}
			else
				htmltext = "32017-07.htm";
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
				if (player.getLevel() >= 20)
					htmltext = "32017-01.htm";
				else
					htmltext = "32017-06.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				if (cond == 1)
					htmltext = "32017-05.htm";
				else if (cond == 2)
				{
					if (st.getQuestItemsCount(GOODS) == 120)
						htmltext = "32017-03.htm";
					else
						htmltext = "32017-07.htm";
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = getRandomPartyMember(player, npc, "1");
		if (st == null)
			return null;
				
		if (st.dropItems(GOODS, 1, 120, 500000))
			st.set("cond", "2");
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q644_GraveRobberAnnihilation();
	}
}