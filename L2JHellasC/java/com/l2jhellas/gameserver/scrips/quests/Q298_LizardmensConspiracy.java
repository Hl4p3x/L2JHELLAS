package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q298_LizardmensConspiracy extends Quest
{
	private static final String qn = "Q298_LizardmensConspiracy";
	
	// NPCs
	private static final int PRAGA = 30333;
	private static final int ROHMER = 30344;
	
	// Items
	private static final int PATROL_REPORT = 7182;
	private static final int WHITE_GEM = 7183;
	private static final int RED_GEM = 7184;
	
	public Q298_LizardmensConspiracy()
	{
		super(298, qn, "Lizardmen's Conspiracy");
		
		setItemsIds(PATROL_REPORT, WHITE_GEM, RED_GEM);
		
		addStartNpc(PRAGA);
		addTalkId(PRAGA, ROHMER);
		
		addKillId(20926, 20927, 20922, 20923, 20924);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30333-1.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(PATROL_REPORT, 1);
		}
		else if (event.equalsIgnoreCase("30344-1.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(PATROL_REPORT, 1);
		}
		else if (event.equalsIgnoreCase("30344-4.htm"))
		{
			if (st.getInt("cond") == 3)
			{
				htmltext = "30344-3.htm";
				st.takeItems(WHITE_GEM, -1);
				st.takeItems(RED_GEM, -1);
				st.rewardExpAndSp(0, 42000);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
			}
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
				htmltext = (player.getLevel() < 25) ? "30333-0b.htm" : "30333-0a.htm";
				break;
			
			case STATE_STARTED:
				switch (npc.getNpcId())
				{
					case PRAGA:
						htmltext = "30333-2.htm";
						break;
					
					case ROHMER:
						if (st.getInt("cond") == 1)
							htmltext = (st.hasQuestItems(PATROL_REPORT)) ? "30344-0.htm" : "30344-0a.htm";
						else
							htmltext = "30344-2.htm";
						break;
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = getRandomPartyMember(player, npc, "2");
		if (st == null)
			return null;
				
		switch (npc.getNpcId())
		{
			case 20922:
			case 20923:
			case 20924:
				if (st.dropItems(WHITE_GEM, 1, 50, 620000) && st.getQuestItemsCount(RED_GEM) >= 50)
					st.set("cond", "3");
				break;
			
			case 20926:
			case 20927:
				if (st.dropItems(RED_GEM, 1, 50, 620000) && st.getQuestItemsCount(WHITE_GEM) >= 50)
					st.set("cond", "3");
				break;
		}
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q298_LizardmensConspiracy();
	}
}