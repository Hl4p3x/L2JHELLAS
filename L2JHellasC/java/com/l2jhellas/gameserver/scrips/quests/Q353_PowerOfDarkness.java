package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q353_PowerOfDarkness extends Quest
{
	private static final String qn = "Q353_PowerOfDarkness";
	
	// Item
	private static final int STONE = 5862;
	
	public Q353_PowerOfDarkness()
	{
		super(353, qn, "Power of Darkness");
		
		setItemsIds(STONE);
		
		addStartNpc(31044); // Galman
		addTalkId(31044);
		
		addKillId(20244, 20245, 20283, 20284);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31044-04.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31044-08.htm"))
		{
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
				htmltext = (player.getLevel() < 55) ? "31044-01.htm" : "31044-02.htm";
				break;
			
			case STATE_STARTED:
				final int stones = st.getQuestItemsCount(STONE);
				if (stones == 0)
					htmltext = "31044-05.htm";
				else
				{
					htmltext = "31044-06.htm";
					st.takeItems(STONE, -1);
					st.rewardItems(57, 2500 + 230 * stones);
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
		
		st.dropItems(STONE, 1, 0, (npc.getNpcId() == 20244 || npc.getNpcId() == 20283) ? 700000 : 720000);
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q353_PowerOfDarkness();
	}
}