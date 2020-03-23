package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q618_IntoTheFlame extends Quest
{
	private static final String qn = "Q618_IntoTheFlame";
	
	// NPCs
	private static final int KLEIN = 31540;
	private static final int HILDA = 31271;
	
	// Items
	private static final int VACUALITE_ORE = 7265;
	private static final int VACUALITE = 7266;
	
	// Reward
	private static final int FLOATING_STONE = 7267;
	
	public Q618_IntoTheFlame()
	{
		super(618, qn, "Into The Flame");
		
		setItemsIds(VACUALITE_ORE, VACUALITE);
		
		addStartNpc(KLEIN);
		addTalkId(KLEIN, HILDA);
		
		// Kookaburras, Bandersnatches, Grendels
		addKillId(21274, 21275, 21276, 21277, 21282, 21283, 21284, 21285, 21290, 21291, 21292, 21293);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		int cond = st.getInt("cond");
		if (cond == 0 && event.equalsIgnoreCase("31540-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31540-05.htm"))
		{
			if (cond == 4 && st.getQuestItemsCount(VACUALITE) > 0)
			{
				st.takeItems(VACUALITE, 1);
				st.giveItems(FLOATING_STONE, 1);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
			}
			else
				htmltext = "31540-03.htm";
		}
		else if (cond == 1 && event.equalsIgnoreCase("31271-02.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("31271-05.htm"))
		{
			if (cond == 3 && st.getQuestItemsCount(VACUALITE_ORE) == 50)
			{
				st.takeItems(VACUALITE_ORE, -1);
				st.giveItems(VACUALITE, 1);
				st.set("cond", "4");
				st.playSound(QuestState.SOUND_MIDDLE);
			}
			else
				htmltext = "31271-03.htm";
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
				if (player.getLevel() < 60)
				{
					htmltext = "31540-01.htm";
					st.exitQuest(true);
				}
				else
					htmltext = "31540-02.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case KLEIN:
						if (cond == 4 && st.getQuestItemsCount(VACUALITE) > 0)
							htmltext = "31540-04.htm";
						else
							htmltext = "31540-03.htm";
						break;
					
					case HILDA:
						if (cond == 1)
							htmltext = "31271-01.htm";
						else if (cond == 3 && st.getQuestItemsCount(VACUALITE_ORE) == 50)
							htmltext = "31271-04.htm";
						else if (cond == 4)
							htmltext = "31271-06.htm";
						else
							htmltext = "31271-03.htm";
						break;
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		L2PcInstance partyMember = getRandomPartyMember(player, npc, "2");
		if (partyMember == null)
			return null;
		
		QuestState st = partyMember.getQuestState(qn);
		
		if (st.dropItems(VACUALITE_ORE, 1, 50, 500000))
			st.set("cond", "3");
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q618_IntoTheFlame();
	}
}