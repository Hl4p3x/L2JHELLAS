package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Rnd;

public class Q329_CuriosityOfADwarf extends Quest
{
	private static final String qn = "Q329_CuriosityOfADwarf";
	
	// Items
	private static final int GOLEM_HEARTSTONE = 1346;
	private static final int BROKEN_HEARTSTONE = 1365;
	
	public Q329_CuriosityOfADwarf()
	{
		super(329, qn, "Curiosity of a Dwarf");
		
		addStartNpc(30437); // Rolento
		addTalkId(30437);
		
		addKillId(20083, 20085); // Granite golem, Puncher
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30437-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30437-06.htm"))
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
				htmltext = (player.getLevel() < 33) ? "30437-01.htm" : "30437-02.htm";
				break;
			
			case STATE_STARTED:
				final int golem = st.getQuestItemsCount(GOLEM_HEARTSTONE);
				final int broken = st.getQuestItemsCount(BROKEN_HEARTSTONE);
				
				if (golem + broken == 0)
					htmltext = "30437-04.htm";
				else
				{
					htmltext = "30437-05.htm";
					st.takeItems(GOLEM_HEARTSTONE, -1);
					st.takeItems(BROKEN_HEARTSTONE, -1);
					st.rewardItems(57, broken * 50 + golem * 1000 + ((golem + broken > 10) ? 1183 : 0));
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
		
		final int chance = Rnd.get(100);
		if (chance < 15)
			st.dropItemsAlways(GOLEM_HEARTSTONE, 1, 0);
		else if (chance < 65)
			st.dropItemsAlways(BROKEN_HEARTSTONE, 1, 0);
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q329_CuriosityOfADwarf();
	}
}