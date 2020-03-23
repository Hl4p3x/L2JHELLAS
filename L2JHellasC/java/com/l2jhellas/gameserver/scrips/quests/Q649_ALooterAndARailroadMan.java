package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q649_ALooterAndARailroadMan extends Quest
{
	private static final String qn = "Q649_ALooterAndARailroadMan";
	
	// Item
	private static final int THIEF_GUILD_MARK = 8099;
	
	// NPC
	private static final int OBI = 32052;
	
	public Q649_ALooterAndARailroadMan()
	{
		super(649, qn, "A Looter and a Railroad Man");
		
		setItemsIds(THIEF_GUILD_MARK);
		
		addStartNpc(OBI);
		addTalkId(OBI);
		
		addKillId(22017, 22018, 22019, 22021, 22022, 22023, 22024, 22026);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("32052-1.htm"))
		{
			st.set("cond", "1");
			st.setState(STATE_STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("32052-3.htm"))
		{
			if (st.getQuestItemsCount(THIEF_GUILD_MARK) < 200)
				htmltext = "32052-3a.htm";
			else
			{
				st.takeItems(THIEF_GUILD_MARK, -1);
				st.rewardItems(57, 21698);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
			}
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
				if (player.getLevel() >= 30)
					htmltext = "32052-0.htm";
				else
				{
					htmltext = "32052-0a.htm";
					st.exitQuest(true);
				}
				break;
			
			case STATE_STARTED:
				if (st.getQuestItemsCount(THIEF_GUILD_MARK) == 200)
					htmltext = "32052-2.htm";
				else
					htmltext = "32052-2a.htm";
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
		
		if (st.dropItems(THIEF_GUILD_MARK, 1, 200, 800000))
			st.set("cond", "2");
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q649_ALooterAndARailroadMan();
	}
}