package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q014_WhereaboutsOfTheArchaeologist extends Quest
{
	private static final String qn = "Q014_WhereaboutsOfTheArchaeologist";
	
	// NPCs
	private static final int LIESEL = 31263;
	private static final int GHOST_OF_ADVENTURER = 31538;
	
	// Items
	private static final int LETTER = 7253;
	
	public Q014_WhereaboutsOfTheArchaeologist()
	{
		super(14, qn, "Whereabouts of the Archaeologist");
		
		setItemsIds(LETTER);
		
		addStartNpc(LIESEL);
		addTalkId(LIESEL, GHOST_OF_ADVENTURER);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31263-2.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(LETTER, 1);
		}
		else if (event.equalsIgnoreCase("31538-1.htm"))
		{
			st.takeItems(LETTER, 1);
			st.rewardItems(57, 113228);
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(false);
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
				htmltext = (player.getLevel() < 74) ? "31263-1.htm" : "31263-0.htm";
				break;
			
			case STATE_STARTED:
				switch (npc.getNpcId())
				{
					case LIESEL:
						htmltext = "31263-2.htm";
						break;
					
					case GHOST_OF_ADVENTURER:
						htmltext = "31538-0.htm";
						break;
				}
				break;
			
			case STATE_COMPLETED:
				htmltext = getAlreadyCompletedMsg();
				break;
		}
		
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Q014_WhereaboutsOfTheArchaeologist();
	}
}