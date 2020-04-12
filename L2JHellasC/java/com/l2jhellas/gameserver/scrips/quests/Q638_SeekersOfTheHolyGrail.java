package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Rnd;

public class Q638_SeekersOfTheHolyGrail extends Quest
{
	private static final String qn = "Q638_SeekersOfTheHolyGrail";
	
	// NPC
	private static final int INNOCENTIN = 31328;
	
	// Item
	private static final int TOTEM = 8068;
	
	public Q638_SeekersOfTheHolyGrail()
	{
		super(638, qn, "Seekers of the Holy Grail");
		
		setItemsIds(TOTEM);
		
		addStartNpc(INNOCENTIN);
		addTalkId(INNOCENTIN);
		
		for (int i = 22138; i < 22175; i++)
			addKillId(i);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31328-02.htm"))
		{
			st.set("cond", "1");
			st.setState(STATE_STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31328-06.htm"))
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
				if (player.getLevel() >= 73)
					htmltext = "31328-01.htm";
				else
				{
					htmltext = "31328-00.htm";
					st.exitQuest(true);
				}
				break;
			
			case STATE_STARTED:
				if (st.getQuestItemsCount(TOTEM) >= 2000)
				{
					htmltext = "31328-03.htm";
					st.takeItems(TOTEM, 2000);
					
					int chance = Rnd.get(3);
					if (chance == 0)
						st.rewardItems(959, 1);
					else if (chance == 1)
						st.rewardItems(960, 1);
					else
						st.rewardItems(57, 3576000);
					
					st.playSound(QuestState.SOUND_MIDDLE);
				}
				else
					htmltext = "31328-04.htm";
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
				
		st.dropItemsAlways(TOTEM, 1, -1);
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q638_SeekersOfTheHolyGrail();
	}
}