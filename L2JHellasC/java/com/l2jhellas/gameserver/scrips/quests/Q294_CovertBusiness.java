package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.enums.player.ClassRace;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Rnd;

public class Q294_CovertBusiness extends Quest
{
	private static final String qn = "Q294_CovertBusiness";
	
	// Item
	private static final int BAT_FANG = 1491;
	
	// Reward
	private static final int RING_OF_RACCOON = 1508;
	
	public Q294_CovertBusiness()
	{
		super(294, qn, "Covert Business");
		
		setItemsIds(BAT_FANG);
		
		addStartNpc(30534); // Keef
		addTalkId(30534);
		
		addKillId(20370, 20480); // Barded Bat, Blade Bat
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30534-03.htm"))
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
				if (player.getRace() != ClassRace.DWARF)
					htmltext = "30534-00.htm";
				else if (player.getLevel() < 10)
					htmltext = "30534-01.htm";
				else
					htmltext = "30534-02.htm";
				break;
			
			case STATE_STARTED:
				if (st.getInt("cond") == 1)
					htmltext = "30534-04.htm";
				else
				{
					htmltext = "30534-05.htm";
					st.takeItems(BAT_FANG, -1);
					st.giveItems(RING_OF_RACCOON, 1);
					st.rewardExpAndSp(0, 600);
					st.playSound(QuestState.SOUND_FINISH);
					st.exitQuest(true);
				}
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
		
		if (st.dropItemsAlways(BAT_FANG, Rnd.get(1, 4), 100))
			st.set("cond", "2");
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q294_CovertBusiness();
	}
}