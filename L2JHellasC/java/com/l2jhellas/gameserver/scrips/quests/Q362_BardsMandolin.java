package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q362_BardsMandolin extends Quest
{
	private static final String qn = "Q362_BardsMandolin";
	
	// Items
	private static final int SWAN_FLUTE = 4316;
	private static final int SWAN_LETTER = 4317;
	
	// NPCs
	private static final int SWAN = 30957;
	private static final int NANARIN = 30956;
	private static final int GALION = 30958;
	private static final int WOODROW = 30837;
	
	public Q362_BardsMandolin()
	{
		super(362, qn, "Bard's Mandolin");
		
		setItemsIds(SWAN_FLUTE, SWAN_LETTER);
		
		addStartNpc(SWAN);
		addTalkId(SWAN, NANARIN, GALION, WOODROW);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30957-3.htm"))
		{
			st.set("cond", "1");
			st.setState(STATE_STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30957-7.htm") || event.equalsIgnoreCase("30957-8.htm"))
		{
			st.rewardItems(57, 10000);
			st.giveItems(4410, 1);
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
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
				if (player.getLevel() >= 15)
					htmltext = "30957-1.htm";
				else
				{
					htmltext = "30957-2.htm";
					st.exitQuest(true);
				}
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case SWAN:
						if (cond == 1 || cond == 2)
							htmltext = "30957-4.htm";
						else if (cond == 3)
						{
							htmltext = "30957-5.htm";
							st.set("cond", "4");
							st.giveItems(SWAN_LETTER, 1);
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						else if (cond == 4)
							htmltext = "30957-5a.htm";
						else if (cond == 5)
							htmltext = "30957-6.htm";
						break;
					
					case WOODROW:
						if (cond == 1)
						{
							htmltext = "30837-1.htm";
							st.set("cond", "2");
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						else if (cond == 2)
							htmltext = "30837-2.htm";
						else if (cond > 2)
							htmltext = "30837-3.htm";
						break;
					
					case GALION:
						if (cond == 2)
						{
							htmltext = "30958-1.htm";
							st.set("cond", "3");
							st.giveItems(SWAN_FLUTE, 1);
							st.playSound(QuestState.SOUND_ITEMGET);
						}
						else if (cond >= 3)
							htmltext = "30958-2.htm";
						break;
					
					case NANARIN:
						if (cond == 4)
						{
							htmltext = "30956-1.htm";
							st.set("cond", "5");
							st.takeItems(SWAN_FLUTE, 1);
							st.takeItems(SWAN_LETTER, 1);
							st.playSound(QuestState.SOUND_MIDDLE);
						}
						else if (cond == 5)
							htmltext = "30956-2.htm";
						break;
				}
				break;
		}
		
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Q362_BardsMandolin();
	}
}