package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Rnd;

public class Q357_WarehouseKeepersAmbition extends Quest
{
	private static final String qn = "Q357_WarehouseKeepersAmbition";
	
	// Item
	private static final int JADE_CRYSTAL = 5867;
	
	// Monsters
	private static final int FOREST_RUNNER = 20594;
	private static final int FLINE_ELDER = 20595;
	private static final int LIELE_ELDER = 20596;
	private static final int VALLEY_TREANT_ELDER = 20597;
	
	public Q357_WarehouseKeepersAmbition()
	{
		super(357, qn, "Warehouse Keeper's Ambition");
		
		setItemsIds(JADE_CRYSTAL);
		
		addStartNpc(30686); // Silva
		addTalkId(30686);
		
		addKillId(FOREST_RUNNER, FLINE_ELDER, LIELE_ELDER, VALLEY_TREANT_ELDER);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30686-2.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30686-7.htm"))
		{
			final int count = st.getQuestItemsCount(JADE_CRYSTAL);
			if (count == 0)
				htmltext = "30686-4.htm";
			else
			{
				int reward = (count * 425) + 3500;
				if (count >= 100)
					reward += 7400;
				
				st.takeItems(JADE_CRYSTAL, -1);
				st.rewardItems(57, reward);
			}
		}
		else if (event.equalsIgnoreCase("30686-8.htm"))
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
				htmltext = (player.getLevel() < 47) ? "30686-0a.htm" : "30686-0.htm";
				break;
			
			case STATE_STARTED:
				htmltext = (!st.hasQuestItems(JADE_CRYSTAL)) ? "30686-4.htm" : "30686-6.htm";
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		L2PcInstance partyMember = getRandomPartyMemberState(player, npc, STATE_STARTED);
		if (partyMember == null)
			return null;
		
		switch (npc.getNpcId())
		{
			case FOREST_RUNNER:
				partyMember.getQuestState(qn).dropItems(JADE_CRYSTAL, 1, -1, 577000);
				break;
			
			case FLINE_ELDER:
				partyMember.getQuestState(qn).dropItems(JADE_CRYSTAL, 1, -1, 600000);
				break;
			
			case LIELE_ELDER:
				partyMember.getQuestState(qn).dropItems(JADE_CRYSTAL, 1, -1, 638000);
				break;
			
			case VALLEY_TREANT_ELDER:
				partyMember.getQuestState(qn).dropItemsAlways(JADE_CRYSTAL, (Rnd.get(1000) < 62) ? 2 : 1, -1);
				break;
		}
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q357_WarehouseKeepersAmbition();
	}
}