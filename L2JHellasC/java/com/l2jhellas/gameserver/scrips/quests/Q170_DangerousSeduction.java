package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.enums.player.ClassRace;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q170_DangerousSeduction extends Quest
{
	private static final String qn = "Q170_DangerousSeduction";
	
	// Item
	private static final int NIGHTMARE_CRYSTAL = 1046;
	
	public Q170_DangerousSeduction()
	{
		super(170, qn, "Dangerous Seduction");
		
		setItemsIds(NIGHTMARE_CRYSTAL);
		
		addStartNpc(30305); // Vellior
		addTalkId(30305);
		
		addKillId(27022); // Merkenis
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30305-04.htm"))
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
					htmltext = "30305-00.htm";
				else if (player.getLevel() < 21)
					htmltext = "30305-02.htm";
				else
					htmltext = "30305-03.htm";
				break;
			
			case STATE_STARTED:
				if (st.hasQuestItems(NIGHTMARE_CRYSTAL))
				{
					htmltext = "30305-06.htm";
					st.takeItems(NIGHTMARE_CRYSTAL, -1);
					st.rewardItems(57, 102680);
					st.playSound(QuestState.SOUND_FINISH);
					st.exitQuest(false);
				}
				else
					htmltext = "30305-05.htm";
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
		
		st.set("cond", "2");
		st.playSound(QuestState.SOUND_MIDDLE);
		st.giveItems(NIGHTMARE_CRYSTAL, 1);
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q170_DangerousSeduction();
	}
}