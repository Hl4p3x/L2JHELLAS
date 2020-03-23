package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q366_SilverHairedShaman extends Quest
{
	private static final String qn = "Q366_SilverHairedShaman";
	
	// NPC
	private static final int DIETER = 30111;
	
	// Item
	private static final int HAIR = 5874;
	
	public Q366_SilverHairedShaman()
	{
		super(366, qn, "Silver Haired Shaman");
		
		setItemsIds(HAIR);
		
		addStartNpc(DIETER);
		addTalkId(DIETER);
		
		addKillId(20986, 20987, 20988);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30111-2.htm"))
		{
			st.set("cond", "1");
			st.setState(STATE_STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30111-6.htm"))
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
				if (player.getLevel() >= 48)
					htmltext = "30111-1.htm";
				else
				{
					htmltext = "30111-0.htm";
					st.exitQuest(true);
				}
				break;
			
			case STATE_STARTED:
				int count = st.getQuestItemsCount(HAIR);
				if (count == 0)
					htmltext = "30111-3.htm";
				else
				{
					htmltext = "30111-4.htm";
					st.takeItems(HAIR, -1);
					st.rewardItems(57, 12070 + 500 * count);
				}
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
		
		QuestState st = partyMember.getQuestState(qn);
		
		st.dropItems(HAIR, 1, -1, 550000);
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q366_SilverHairedShaman();
	}
}