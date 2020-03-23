package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q341_HuntingForWildBeasts extends Quest
{
	private static final String qn = "Q341_HuntingForWildBeasts";
	
	// Item
	private static final int BEAR_SKIN = 4259;
	
	public Q341_HuntingForWildBeasts()
	{
		super(341, qn, "Hunting for Wild Beasts");
		
		setItemsIds(BEAR_SKIN);
		
		addStartNpc(30078); // Pano
		addTalkId(30078);
		
		// Red bear, brown bear, grizzly, Dion grizzly.
		addKillId(20203, 20021, 20310, 20143);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30078-02.htm"))
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
		QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg();
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case STATE_CREATED:
				htmltext = (player.getLevel() < 20) ? "30078-00.htm" : "30078-01.htm";
				break;
			
			case STATE_STARTED:
				if (st.getQuestItemsCount(BEAR_SKIN) < 20)
					htmltext = "30078-03.htm";
				else
				{
					htmltext = "30078-04.htm";
					st.takeItems(BEAR_SKIN, -1);
					st.rewardItems(57, 3710);
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
		QuestState st = checkPlayerState(player, npc, STATE_STARTED);
		if (st == null)
			return null;
		
		st.dropItems(BEAR_SKIN, 1, 20, 400000);
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q341_HuntingForWildBeasts();
	}
}