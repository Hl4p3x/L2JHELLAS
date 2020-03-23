package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q262_TradeWithTheIvoryTower extends Quest
{
	private static final String qn = "Q262_TradeWithTheIvoryTower";
	
	// Item
	private static final int FUNGUS_SAC = 707;
	
	public Q262_TradeWithTheIvoryTower()
	{
		super(262, qn, "Trade with the Ivory Tower");
		
		setItemsIds(FUNGUS_SAC);
		
		addStartNpc(30137); // Vollodos
		addTalkId(30137);
		
		addKillId(20400, 20007);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30137-03.htm"))
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
				htmltext = (player.getLevel() < 8) ? "30137-01.htm" : "30137-02.htm";
				break;
			
			case STATE_STARTED:
				if (st.getQuestItemsCount(FUNGUS_SAC) < 10)
					htmltext = "30137-04.htm";
				else
				{
					htmltext = "30137-05.htm";
					st.takeItems(FUNGUS_SAC, -1);
					st.rewardItems(57, 3000);
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
		
		if (st.dropItems(FUNGUS_SAC, 1, 10, (npc.getNpcId() == 20400) ? 400000 : 300000))
			st.set("cond", "2");
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q262_TradeWithTheIvoryTower();
	}
}