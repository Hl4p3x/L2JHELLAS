package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Rnd;

public class Q300_HuntingLetoLizardman extends Quest
{
	private static final String qn = "Q300_HuntingLetoLizardman";
	
	// Item
	private static final int BRACELET = 7139;
	
	public Q300_HuntingLetoLizardman()
	{
		super(300, qn, "Hunting Leto Lizardman");
		
		setItemsIds(BRACELET);
		
		addStartNpc(30126); // Rath
		addTalkId(30126);
		
		addKillId(20577, 20578, 20579, 20580, 20582);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30126-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30126-05.htm"))
		{
			if (st.getQuestItemsCount(BRACELET) >= 60)
			{
				htmltext = "30126-06.htm";
				st.takeItems(BRACELET, -1);
				
				final int luck = Rnd.get(3);
				if (luck == 0)
					st.rewardItems(57, 30000);
				else if (luck == 1)
					st.rewardItems(1867, 50);
				else if (luck == 2)
					st.rewardItems(1872, 50);
				
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
			}
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
				htmltext = (player.getLevel() < 34) ? "30126-01.htm" : "30126-02.htm";
				break;
			
			case STATE_STARTED:
				htmltext = (st.getInt("cond") == 1) ? "30126-04a.htm" : "30126-04.htm";
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		L2PcInstance partyMember = getRandomPartyMember(player, npc, "1");
		if (partyMember == null)
			return null;
		
		QuestState st = partyMember.getQuestState(qn);
		
		if (st.dropItems(BRACELET, 1, 60, 330000))
			st.set("cond", "2");
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q300_HuntingLetoLizardman();
	}
}