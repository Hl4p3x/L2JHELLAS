package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.enums.player.ClassRace;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q320_BonesTellTheFuture extends Quest
{
	private static final String qn = "Q320_BonesTellTheFuture";
	
	// Quest item
	private final int BONE_FRAGMENT = 809;
	
	public Q320_BonesTellTheFuture()
	{
		super(320, qn, "Bones Tell the Future");
		
		setItemsIds(BONE_FRAGMENT);
		
		addStartNpc(30359); // Kaitar
		addTalkId(30359);
		
		addKillId(20517, 20518, 20022, 20455);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30359-04.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		
		return event;
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
				if (player.getRace() != ClassRace.DARK_ELF)
					htmltext = "30359-00.htm";
				else if (player.getLevel() < 10)
					htmltext = "30359-02.htm";
				else
					htmltext = "30359-03.htm";
				break;
			
			case STATE_STARTED:
				if (st.getInt("cond") == 1)
					htmltext = "30359-05.htm";
				else
				{
					htmltext = "30359-06.htm";
					st.takeItems(BONE_FRAGMENT, -1);
					st.rewardItems(57, 8470);
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
		
		if (st.dropItems(BONE_FRAGMENT, 1, 10, 200000))
			st.set("cond", "2");
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q320_BonesTellTheFuture();
	}
}