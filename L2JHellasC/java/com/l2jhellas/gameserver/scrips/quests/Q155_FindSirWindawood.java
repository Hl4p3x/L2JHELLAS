package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q155_FindSirWindawood extends Quest
{
	private static final String qn = "Q155_FindSirWindawood";
	
	// Items
	private static final int OFFICIAL_LETTER = 1019;
	private static final int HASTE_POTION = 734;
	
	// NPCs
	private static final int ABELLOS = 30042;
	private static final int WINDAWOOD = 30311;
	
	public Q155_FindSirWindawood()
	{
		super(155, qn, "Find Sir Windawood");
		
		setItemsIds(OFFICIAL_LETTER);
		
		addStartNpc(ABELLOS);
		addTalkId(WINDAWOOD, ABELLOS);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30042-02.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(OFFICIAL_LETTER, 1);
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
				htmltext = (player.getLevel() < 3) ? "30042-01a.htm" : "30042-01.htm";
				break;
			
			case STATE_STARTED:
				switch (npc.getNpcId())
				{
					case ABELLOS:
						htmltext = "30042-03.htm";
						break;
					
					case WINDAWOOD:
						if (st.hasQuestItems(OFFICIAL_LETTER))
						{
							htmltext = "30311-01.htm";
							st.takeItems(OFFICIAL_LETTER, 1);
							st.rewardItems(HASTE_POTION, 1);
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
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
		new Q155_FindSirWindawood();
	}
}