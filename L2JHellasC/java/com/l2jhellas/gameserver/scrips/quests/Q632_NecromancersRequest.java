package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q632_NecromancersRequest extends Quest
{
	private static final String qn = "Q632_NecromancersRequest";
	
	// Monsters
	private static final int[] VAMPIRES =
	{
		21568,
		21573,
		21582,
		21585,
		21586,
		21587,
		21588,
		21589,
		21590,
		21591,
		21592,
		21593,
		21594,
		21595
	};
	
	private static final int[] UNDEADS =
	{
		21547,
		21548,
		21549,
		21551,
		21552,
		21555,
		21556,
		21562,
		21571,
		21576,
		21577,
		21579
	};
	
	// Items
	private static final int VAMPIRE_HEART = 7542;
	private static final int ZOMBIE_BRAIN = 7543;
	
	public Q632_NecromancersRequest()
	{
		super(632, qn, "Necromancer's Request");
		
		setItemsIds(VAMPIRE_HEART, ZOMBIE_BRAIN);
		
		addStartNpc(31522); // Mysterious Wizard
		addTalkId(31522);
		
		addKillId(VAMPIRES);
		addKillId(UNDEADS);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31522-03.htm"))
		{
			st.set("cond", "1");
			st.setState(STATE_STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31522-06.htm"))
		{
			if (st.getQuestItemsCount(VAMPIRE_HEART) > 199)
			{
				st.takeItems(VAMPIRE_HEART, -1);
				st.rewardItems(57, 120000);
				
				st.set("cond", "1");
				st.playSound(QuestState.SOUND_MIDDLE);
			}
			else
				htmltext = "31522-09.htm";
		}
		else if (event.equalsIgnoreCase("31522-08.htm"))
		{
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
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
				if (player.getLevel() < 63)
				{
					st.exitQuest(true);
					htmltext = "31522-01.htm";
				}
				else
					htmltext = "31522-02.htm";
				break;
			
			case STATE_STARTED:
				htmltext = (st.getQuestItemsCount(VAMPIRE_HEART) >= 200) ? "31522-05.htm" : "31522-04.htm";
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
		
		int npcId = npc.getNpcId();
		for (int undead : UNDEADS)
		{
			if (undead != npcId)
				continue;
			
			st.dropItems(ZOMBIE_BRAIN, 1, -1, 330000);
			return null;
		}
		
		if (st.getInt("cond") == 1)
			if (st.dropItems(VAMPIRE_HEART, 1, 200, 500000))
				st.set("cond", "2");
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q632_NecromancersRequest();
	}
}