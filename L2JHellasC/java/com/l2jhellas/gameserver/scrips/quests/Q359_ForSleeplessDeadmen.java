package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Rnd;

public class Q359_ForSleeplessDeadmen extends Quest
{
	private static final String qn = "Q359_ForSleeplessDeadmen";
	
	// Item
	private static final int REMAINS = 5869;
	
	// NPCs
	private static final int DOOM_SERVANT = 21006;
	private static final int DOOM_GUARD = 21007;
	private static final int DOOM_ARCHER = 21008;
	
	// Reward
	private static final int REWARD[] =
	{
		6341,
		6342,
		6343,
		6344,
		6345,
		6346,
		5494,
		5495
	};
	
	public Q359_ForSleeplessDeadmen()
	{
		super(359, qn, "For Sleepless Deadmen");
		
		setItemsIds(REMAINS);
		
		addStartNpc(30857); // Orven
		addTalkId(30857);
		
		addKillId(DOOM_SERVANT, DOOM_GUARD, DOOM_ARCHER);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30857-06.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30857-10.htm"))
		{
			st.giveItems(REWARD[Rnd.get(REWARD.length)], 4);
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
				htmltext = (player.getLevel() < 60) ? "30857-01.htm" : "30857-02.htm";
				break;
			
			case STATE_STARTED:
				final int cond = st.getInt("cond");
				if (cond == 1)
					htmltext = "30857-07.htm";
				else if (cond == 2)
				{
					htmltext = "30857-08.htm";
					st.set("cond", "3");
					st.playSound(QuestState.SOUND_MIDDLE);
					st.takeItems(REMAINS, -1);
				}
				else if (cond == 3)
					htmltext = "30857-09.htm";
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
		
		switch (npc.getNpcId())
		{
			case DOOM_SERVANT:
				if (st.dropItems(REMAINS, 1, 60, 365000))
					st.set("cond", "2");
				break;
			
			case DOOM_GUARD:
				if (st.dropItems(REMAINS, 1, 60, 392000))
					st.set("cond", "2");
				break;
			
			case DOOM_ARCHER:
				if (st.dropItems(REMAINS, 1, 60, 503000))
					st.set("cond", "2");
				break;
		}
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q359_ForSleeplessDeadmen();
	}
}