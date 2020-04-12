package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q634_InSearchOfFragmentsOfDimension extends Quest
{
	private static final String qn = "Q634_InSearchOfFragmentsOfDimension";
	
	// Items
	private static final int DIMENSION_FRAGMENT = 7079;
	
	public Q634_InSearchOfFragmentsOfDimension()
	{
		super(634, qn, "In Search of Fragments of Dimension");
		
		// Dimensional Gate Keepers.
		for (int i = 31494; i < 31508; i++)
		{
			addStartNpc(i);
			addTalkId(i);
		}
		
		// All mobs.
		for (int i = 21208; i < 21256; i++)
			addKillId(i);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("02.htm"))
		{
			st.set("cond", "1");
			st.setState(STATE_STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("05.htm"))
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
				if (player.getLevel() >= 20)
					htmltext = "01.htm";
				else
				{
					htmltext = "01a.htm";
					st.exitQuest(true);
				}
				break;
			
			case STATE_STARTED:
				htmltext = "03.htm";
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = getRandomPartyMemberState(player, npc, STATE_STARTED);
		if (st == null)
			return null;
				
		st.dropItems(DIMENSION_FRAGMENT, (int) (npc.getLevel() * 0.15 + 2.6), -1, 80000);
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q634_InSearchOfFragmentsOfDimension();
	}
}