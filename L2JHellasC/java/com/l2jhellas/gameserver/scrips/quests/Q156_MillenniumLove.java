package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q156_MillenniumLove extends Quest
{
	private static final String qn = "Q156_MillenniumLove";
	
	// Items
	private static final int LILITH_LETTER = 1022;
	private static final int THEON_DIARY = 1023;
	
	// NPCs
	private static final int LILITH = 30368;
	private static final int BAENEDES = 30369;
	
	public Q156_MillenniumLove()
	{
		super(156, qn, "Millennium Love");
		
		setItemsIds(LILITH_LETTER, THEON_DIARY);
		
		addStartNpc(LILITH);
		addTalkId(LILITH, BAENEDES);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30368-04.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(LILITH_LETTER, 1);
		}
		else if (event.equalsIgnoreCase("30369-02.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.takeItems(LILITH_LETTER, 1);
			st.giveItems(THEON_DIARY, 1);
		}
		else if (event.equalsIgnoreCase("30369-03.htm"))
		{
			st.takeItems(LILITH_LETTER, 1);
			st.rewardExpAndSp(3000, 0);
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(false);
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
				htmltext = (player.getLevel() < 15) ? "30368-00.htm" : "30368-01.htm";
				break;
			
			case STATE_STARTED:
				switch (npc.getNpcId())
				{
					case LILITH:
						if (st.hasQuestItems(LILITH_LETTER))
							htmltext = "30368-05.htm";
						else if (st.hasQuestItems(THEON_DIARY))
						{
							htmltext = "30368-06.htm";
							st.takeItems(THEON_DIARY, 1);
							st.giveItems(5250, 1);
							st.rewardExpAndSp(3000, 0);
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(false);
						}
						break;
					
					case BAENEDES:
						if (st.hasQuestItems(LILITH_LETTER))
							htmltext = "30369-01.htm";
						else if (st.hasQuestItems(THEON_DIARY))
							htmltext = "30369-04.htm";
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
		new Q156_MillenniumLove();
	}
}