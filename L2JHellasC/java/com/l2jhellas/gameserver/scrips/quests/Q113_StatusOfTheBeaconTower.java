package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q113_StatusOfTheBeaconTower extends Quest
{
	private static final String qn = "Q113_StatusOfTheBeaconTower";
	
	// NPCs
	private static final int MOIRA = 31979;
	private static final int TORRANT = 32016;
	
	// Item
	private static final int BOX = 8086;
	
	public Q113_StatusOfTheBeaconTower()
	{
		super(113, qn, "Status of the Beacon Tower");
		
		setItemsIds(BOX);
		
		addStartNpc(MOIRA);
		addTalkId(MOIRA, TORRANT);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31979-02.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(BOX, 1);
		}
		else if (event.equalsIgnoreCase("32016-02.htm"))
		{
			st.takeItems(BOX, 1);
			st.rewardItems(57, 21578);
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(false);
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
				htmltext = (player.getLevel() < 40) ? "31979-00.htm" : "31979-01.htm";
				break;
			
			case STATE_STARTED:
				switch (npc.getNpcId())
				{
					case MOIRA:
						htmltext = "31979-03.htm";
						break;
					
					case TORRANT:
						htmltext = "32016-01.htm";
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
		new Q113_StatusOfTheBeaconTower();
	}
}