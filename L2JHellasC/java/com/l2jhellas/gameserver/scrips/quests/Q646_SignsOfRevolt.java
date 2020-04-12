package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Util;

public class Q646_SignsOfRevolt extends Quest
{
	private static final String qn = "Q646_SignsOfRevolt";
	
	// NPC
	private static final int TORRANT = 32016;
	
	// Item
	private static final int CURSED_DOLL = 8087;
	
	// Rewards
	private static final int[][] rewards =
	{
		{
			1880,
			9
		},
		{
			1881,
			12
		},
		{
			1882,
			20
		},
		{
			57,
			21600
		}
	};
	
	public Q646_SignsOfRevolt()
	{
		super(646, qn, "Signs Of Revolt");
		
		setItemsIds(CURSED_DOLL);
		
		addStartNpc(TORRANT);
		addTalkId(TORRANT);
		
		addKillId(22029, 22030, 22031, 22032, 22033, 22034, 22035, 22036, 22037, 22038, 22039, 22040, 22041, 22042, 22043, 22044, 22045, 22047, 22049);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("32016-03.htm"))
		{
			st.set("cond", "1");
			st.setState(STATE_STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (Util.isDigit(event))
		{
			htmltext = "32016-07.htm";
			st.takeItems(CURSED_DOLL, -1);
			
			int reward[] = rewards[Integer.parseInt(event)];
			st.giveItems(reward[0], reward[1]);
			
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
				if (player.getLevel() >= 40)
					htmltext = "32016-01.htm";
				else
				{
					htmltext = "32016-02.htm";
					st.exitQuest(true);
				}
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				if (cond == 1)
					htmltext = "32016-04.htm";
				else if (cond == 2)
				{
					if (st.getQuestItemsCount(CURSED_DOLL) == 180)
						htmltext = "32016-05.htm";
					else
						htmltext = "32016-04.htm";
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
				
		if (st.dropItems(CURSED_DOLL, 1, 180, 750000))
			st.set("cond", "2");
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q646_SignsOfRevolt();
	}
}