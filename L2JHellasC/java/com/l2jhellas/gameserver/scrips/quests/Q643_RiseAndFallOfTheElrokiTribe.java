package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Rnd;

public class Q643_RiseAndFallOfTheElrokiTribe extends Quest
{
	private static final String qn = "Q643_RiseAndFallOfTheElrokiTribe";
	
	// NPCs
	private static final int SINGSING = 32106;
	private static final int KARAKAWEI = 32117;
	
	// Items
	private static final int BONES = 8776;
	
	public Q643_RiseAndFallOfTheElrokiTribe()
	{
		super(643, qn, "Rise and Fall of the Elroki Tribe");
		
		setItemsIds(BONES);
		
		addStartNpc(SINGSING);
		addTalkId(SINGSING, KARAKAWEI);
		
		addKillId(22208, 22209, 22210, 22211, 22212, 22213, 22221, 22222, 22226, 22227);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("32106-03.htm"))
		{
			st.set("cond", "1");
			st.setState(STATE_STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("32106-07.htm"))
		{
			final int count = st.getQuestItemsCount(BONES);
			
			st.takeItems(BONES, count);
			st.rewardItems(57, count * 1374);
		}
		else if (event.equalsIgnoreCase("32106-09.htm"))
		{
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
		}
		else if (event.equalsIgnoreCase("32117-03.htm"))
		{
			final int count = st.getQuestItemsCount(BONES);
			if (count >= 300)
			{
				st.takeItems(BONES, 300);
				st.rewardItems(Rnd.get(8712, 8722), 5);
			}
			else
				htmltext = "32117-04.htm";
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
				if (player.getLevel() < 75)
				{
					htmltext = "31147-00.htm";
					st.exitQuest(true);
				}
				else
					htmltext = "32106-01.htm";
				break;
			
			case STATE_STARTED:
				switch (npc.getNpcId())
				{
					case SINGSING:
						htmltext = (st.hasQuestItems(BONES)) ? "32106-06.htm" : "32106-05.htm";
						break;
					
					case KARAKAWEI:
						htmltext = "32117-01.htm";
						break;
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
		
		partyMember.getQuestState(qn).dropItems(BONES, 1, -1, 750000);
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q643_RiseAndFallOfTheElrokiTribe();
	}
}