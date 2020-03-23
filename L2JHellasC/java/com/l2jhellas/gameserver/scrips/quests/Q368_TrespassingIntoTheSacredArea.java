package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q368_TrespassingIntoTheSacredArea extends Quest
{
	private static final String qn = "Q368_TrespassingIntoTheSacredArea";
	
	// NPC
	private static final int RESTINA = 30926;
	
	// Item
	private static final int FANG = 5881;
	
	public Q368_TrespassingIntoTheSacredArea()
	{
		super(368, qn, "Trespassing into the Sacred Area");
		
		setItemsIds(FANG);
		
		addStartNpc(RESTINA);
		addTalkId(RESTINA);
		addKillId(20794, 20795, 20796, 20797);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30926-02.htm"))
		{
			st.set("cond", "1");
			st.setState(STATE_STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30926-05.htm"))
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
				if (player.getLevel() >= 36)
					htmltext = "30926-01.htm";
				else
				{
					htmltext = "30926-01a.htm";
					st.exitQuest(true);
				}
				break;
			
			case STATE_STARTED:
				int fangs = st.getQuestItemsCount(FANG);
				if (fangs == 0)
					htmltext = "30926-03.htm";
				else
				{
					int reward = 250 * fangs + (fangs > 10 ? 5730 : 2000);
					htmltext = "30926-04.htm";
					st.takeItems(5881, -1);
					st.rewardItems(57, reward);
				}
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
		
		st.dropItems(FANG, 1, -1, 330000);
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q368_TrespassingIntoTheSacredArea();
	}
}