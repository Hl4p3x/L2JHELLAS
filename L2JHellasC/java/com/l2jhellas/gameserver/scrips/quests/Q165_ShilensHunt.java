package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.enums.player.ClassRace;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q165_ShilensHunt extends Quest
{
	private static final String qn = "Q165_ShilensHunt";
	
	// Items
	private static final int DARK_BEZOAR = 1160;
	private static final int LESSER_HEALING_POTION = 1060;
	
	public Q165_ShilensHunt()
	{
		super(165, qn, "Shilen's Hunt");
		
		setItemsIds(DARK_BEZOAR);
		
		addStartNpc(30348); // Nelsya
		addTalkId(30348);
		
		addKillId(20456, 20529, 20532, 20536);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30348-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
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
				if (player.getRace() != ClassRace.DARK_ELF)
					htmltext = "30348-00.htm";
				else if (player.getLevel() < 3)
					htmltext = "30348-01.htm";
				else
					htmltext = "30348-02.htm";
				break;
			
			case STATE_STARTED:
				if (st.getQuestItemsCount(DARK_BEZOAR) >= 13)
				{
					htmltext = "30348-05.htm";
					st.takeItems(DARK_BEZOAR, -1);
					st.rewardItems(LESSER_HEALING_POTION, 5);
					st.rewardExpAndSp(1000, 0);
					st.playSound(QuestState.SOUND_FINISH);
					st.exitQuest(false);
				}
				else
					htmltext = "30348-04.htm";
				break;
			
			case STATE_COMPLETED:
				htmltext = getAlreadyCompletedMsg();
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = checkPlayerCondition(player, npc, "cond", "1");
		if (st == null)
			return null;
		
		if (st.dropItems(DARK_BEZOAR, 1, 13, 200000))
			st.set("cond", "2");
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q165_ShilensHunt();
	}
}