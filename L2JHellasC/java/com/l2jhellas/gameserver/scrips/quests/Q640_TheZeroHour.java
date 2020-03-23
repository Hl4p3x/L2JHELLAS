package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Util;

public class Q640_TheZeroHour extends Quest
{
	private static final String qn = "Q640_TheZeroHour";
	
	// NPC
	private static final int KAHMAN = 31554;
	
	// Item
	private static final int FANG = 8085;
	
	private static final int[][] rewards =
	{
		{
			12,
			4042,
			1
		},
		{
			6,
			4043,
			1
		},
		{
			6,
			4044,
			1
		},
		{
			81,
			1887,
			10
		},
		{
			33,
			1888,
			5
		},
		{
			30,
			1889,
			10
		},
		{
			150,
			5550,
			10
		},
		{
			131,
			1890,
			10
		},
		{
			123,
			1893,
			5
		}
	};
	
	public Q640_TheZeroHour()
	{
		super(640, qn, "The Zero Hour");
		
		setItemsIds(FANG);
		
		addStartNpc(KAHMAN);
		addTalkId(KAHMAN);
		
		// All "spiked" stakatos types, except babies and cannibalistic followers.
		addKillId(22105, 22106, 22107, 22108, 22109, 22110, 22111, 22113, 22114, 22115, 22116, 22117, 22118, 22119, 22121);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31554-02.htm"))
		{
			st.set("cond", "1");
			st.setState(STATE_STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31554-05.htm"))
		{
			if (!st.hasQuestItems(FANG))
				htmltext = "31554-06.htm";
		}
		else if (event.equalsIgnoreCase("31554-08.htm"))
		{
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
		}
		else if (Util.isDigit(event))
		{
			int reward[] = rewards[Integer.parseInt(event)];
			
			if (st.getQuestItemsCount(FANG) >= reward[0])
			{
				st.takeItems(FANG, reward[0]);
				st.rewardItems(reward[1], reward[2]);
				htmltext = "31554-09.htm";
			}
			else
				htmltext = "31554-06.htm";
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
				if (player.getLevel() >= 66)
				{
					QuestState st2 = player.getQuestState("Q109_InSearchOfTheNest");
					if (st2 != null && st2.isCompleted())
						htmltext = "31554-01.htm";
					else
						htmltext = "31554-10.htm";
				}
				else
				{
					htmltext = "31554-00.htm";
					st.exitQuest(true);
				}
				break;
			
			case STATE_STARTED:
				if (st.hasQuestItems(FANG))
					htmltext = "31554-04.htm";
				else
					htmltext = "31554-03.htm";
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		L2PcInstance partyMember = getRandomPartyMemberState(player, npc, STATE_STARTED);
		if (partyMember == null)
			return null;
		
		QuestState st = partyMember.getQuestState(qn);
		
		st.dropItemsAlways(FANG, 1, -1);
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q640_TheZeroHour();
	}
}