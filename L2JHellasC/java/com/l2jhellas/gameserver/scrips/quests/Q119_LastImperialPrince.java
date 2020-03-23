package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q119_LastImperialPrince extends Quest
{
	private static final String qn = "Q119_LastImperialPrince";
	
	// NPCs
	private static final int NAMELESS_SPIRIT = 31453;
	private static final int DEVORIN = 32009;
	
	// Item
	private static final int ANTIQUE_BROOCH = 7262;
	
	public Q119_LastImperialPrince()
	{
		super(119, qn, "Last Imperial Prince");
		
		setItemsIds(ANTIQUE_BROOCH);
		
		addStartNpc(NAMELESS_SPIRIT);
		addTalkId(NAMELESS_SPIRIT, DEVORIN);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31453-04.htm"))
		{
			if (st.hasQuestItems(ANTIQUE_BROOCH))
			{
				st.setState(STATE_STARTED);
				st.set("cond", "1");
				st.playSound(QuestState.SOUND_ACCEPT);
			}
			else
			{
				htmltext = "31453-04b.htm";
				st.exitQuest(true);
			}
		}
		else if (event.equalsIgnoreCase("32009-02.htm"))
		{
			if (!st.hasQuestItems(ANTIQUE_BROOCH))
			{
				htmltext = "31453-02a.htm";
				st.exitQuest(true);
			}
		}
		else if (event.equalsIgnoreCase("32009-03.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("31453-07.htm"))
		{
			st.rewardItems(57, 68787);
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
				htmltext = (!st.hasQuestItems(ANTIQUE_BROOCH) || player.getLevel() < 74) ? "31453-00a.htm" : "31453-01.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case NAMELESS_SPIRIT:
						if (cond == 1)
							htmltext = "31453-04a.htm";
						else if (cond == 2)
							htmltext = "31453-05.htm";
						break;
					
					case DEVORIN:
						if (cond == 1)
							htmltext = "32009-01.htm";
						else if (cond == 2)
							htmltext = "32009-04.htm";
						break;
				}
				break;
			
			case STATE_COMPLETED:
				htmltext = "31453-00b.htm";
				break;
		}
		
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Q119_LastImperialPrince();
	}
}