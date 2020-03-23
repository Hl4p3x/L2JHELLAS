package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q319_ScentOfDeath extends Quest
{
	private static final String qn = "Q319_ScentOfDeath";
	
	// Item
	private static final int ZOMBIE_SKIN = 1045;
	
	public Q319_ScentOfDeath()
	{
		super(319, qn, "Scent of Death");
		
		setItemsIds(ZOMBIE_SKIN);
		
		addStartNpc(30138); // Minaless
		addTalkId(30138);
		
		addKillId(20015, 20020);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30138-04.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
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
				htmltext = (player.getLevel() < 11) ? "30138-02.htm" : "30138-03.htm";
				break;
			
			case STATE_STARTED:
				if (st.getInt("cond") == 1)
					htmltext = "30138-05.htm";
				else
				{
					htmltext = "30138-06.htm";
					st.takeItems(ZOMBIE_SKIN, -1);
					st.rewardItems(57, 3350);
					st.rewardItems(1060, 1);
					st.playSound(QuestState.SOUND_FINISH);
					st.exitQuest(true);
				}
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
		
		if (st.dropItems(ZOMBIE_SKIN, 1, 5, 300000))
			st.set("cond", "2");
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q319_ScentOfDeath();
	}
}