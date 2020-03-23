package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q317_CatchTheWind extends Quest
{
	private static final String qn = "Q317_CatchTheWind";
	
	// Item
	private static final int WIND_SHARD = 1078;
	
	public Q317_CatchTheWind()
	{
		super(317, qn, "Catch the Wind");
		
		setItemsIds(WIND_SHARD);
		
		addStartNpc(30361); // Rizraell
		addTalkId(30361);
		
		addKillId(20036, 20044);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30361-04.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30361-08.htm"))
		{
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
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
				htmltext = (player.getLevel() < 18) ? "30361-02.htm" : "30361-03.htm";
				break;
			
			case STATE_STARTED:
				final int shards = st.getQuestItemsCount(WIND_SHARD);
				if (shards == 0)
					htmltext = "30361-05.htm";
				else
				{
					htmltext = "30361-07.htm";
					st.takeItems(WIND_SHARD, -1);
					st.rewardItems(57, 40 * shards + (shards >= 10 ? 2988 : 0));
				}
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
		
		st.dropItems(WIND_SHARD, 1, 0, 500000);
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q317_CatchTheWind();
	}
}