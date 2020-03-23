package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q110_ToThePrimevalIsle extends Quest
{
	private static final String qn = "Q110_ToThePrimevalIsle";
	
	// NPCs
	private static final int ANTON = 31338;
	private static final int MARQUEZ = 32113;
	
	// Item
	private static final int ANCIENT_BOOK = 8777;
	
	public Q110_ToThePrimevalIsle()
	{
		super(110, qn, "To the Primeval Isle");
		
		setItemsIds(ANCIENT_BOOK);
		
		addStartNpc(ANTON);
		addTalkId(ANTON, MARQUEZ);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31338-02.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(ANCIENT_BOOK, 1);
		}
		else if (event.equalsIgnoreCase("32113-03.htm") && st.hasQuestItems(ANCIENT_BOOK))
		{
			st.takeItems(ANCIENT_BOOK, 1);
			st.rewardItems(57, 169380);
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
				htmltext = (player.getLevel() < 75) ? "31338-00.htm" : "31338-01.htm";
				break;
			
			case STATE_STARTED:
				switch (npc.getNpcId())
				{
					case ANTON:
						htmltext = "31338-01c.htm";
						break;
					
					case MARQUEZ:
						htmltext = "32113-01.htm";
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
		new Q110_ToThePrimevalIsle();
	}
}