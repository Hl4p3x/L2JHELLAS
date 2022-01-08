package com.l2jhellas.gameserver.scrips.quests.ai.custom;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class MissQueen extends Quest
{
	private static final String qn = "MissQueen";
	
	// Rewards
	private static final int COUPON_ONE = 7832;
	private static final int COUPON_TWO = 7833;

	public MissQueen()
	{
		super(-1, qn, "custom");
		
		addStartNpc(31760);
		addTalkId(31760);
		addFirstTalkId(31760);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		
		if (event.equalsIgnoreCase("newbie_coupon"))
		{
			if (player.getClassId().level() == 0 && player.getLevel() >= 6 && player.getLevel() <= 25 && player.getPkKills() <= 0)
			{
				if (st.getInt("reward_1") == 1)
					htmltext = "31760-01.htm";
				else
				{
					st.setState(STATE_STARTED);
					htmltext = "31760-02.htm";
					st.set("reward_1", "1");
					st.giveItems(COUPON_ONE, 1);
				}
			}
			else
				htmltext = "31760-03.htm";
		}
		else if (event.equalsIgnoreCase("traveller_coupon"))
		{
			if (player.getClassId().level() == 1 && player.getLevel() >= 6 && player.getLevel() <= 25 && player.getPkKills() <= 0)
			{
				if (st.getInt("reward_2") == 1)
					htmltext = "31760-04.htm";
				else
				{
					st.setState(STATE_STARTED);
					htmltext = "31760-05.htm";
					st.set("reward_2", "1");
					st.giveItems(COUPON_TWO, 1);
				}
			}
			else
				htmltext = "31760-06.htm";
		}
		
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			st = newQuestState(player);
		
		return "31760.htm";
	}
	
	public static void main(String[] args)
	{
		new MissQueen();
	}
}