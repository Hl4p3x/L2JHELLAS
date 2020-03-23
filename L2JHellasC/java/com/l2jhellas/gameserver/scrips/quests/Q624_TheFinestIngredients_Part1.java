package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q624_TheFinestIngredients_Part1 extends Quest
{
	private static final String qn = "Q624_TheFinestIngredients_Part1";
	
	// Mobs
	private static final int NEPENTHES = 21319;
	private static final int ATROX = 21321;
	private static final int ATROXSPAWN = 21317;
	private static final int BANDERSNATCH = 21314;
	
	// Items
	private static final int TRUNK = 7202;
	private static final int FOOT = 7203;
	private static final int SPICE = 7204;
	
	// Rewards
	private static final int CRYSTAL = 7080;
	private static final int SAUCE = 7205;
	
	public Q624_TheFinestIngredients_Part1()
	{
		super(624, qn, "The Finest Ingredients - Part 1");
		
		setItemsIds(TRUNK, FOOT, SPICE);
		
		addStartNpc(31521); // Jeremy
		addTalkId(31521);
		
		addKillId(NEPENTHES, ATROX, ATROXSPAWN, BANDERSNATCH);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31521-02.htm"))
		{
			st.set("cond", "1");
			st.setState(STATE_STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31521-05.htm"))
		{
			if (st.getQuestItemsCount(TRUNK) >= 50 && st.getQuestItemsCount(FOOT) >= 50 && st.getQuestItemsCount(SPICE) >= 50)
			{
				st.takeItems(TRUNK, -1);
				st.takeItems(FOOT, -1);
				st.takeItems(SPICE, -1);
				st.giveItems(CRYSTAL, 1);
				st.giveItems(SAUCE, 1);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
			}
			else
			{
				st.set("cond", "1");
				htmltext = "31521-07.htm";
			}
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
				if (player.getLevel() >= 73)
					htmltext = "31521-01.htm";
				else
				{
					htmltext = "31521-03.htm";
					st.exitQuest(true);
				}
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				if (cond == 1)
					htmltext = "31521-06.htm";
				else if (cond == 2)
				{
					if (st.getQuestItemsCount(TRUNK) >= 50 && st.getQuestItemsCount(FOOT) >= 50 && st.getQuestItemsCount(SPICE) >= 50)
						htmltext = "31521-04.htm";
					else
						htmltext = "31521-07.htm";
				}
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
		
		switch (npc.getNpcId())
		{
			case NEPENTHES:
				if (st.dropItemsAlways(TRUNK, 1, 50))
					if (st.getQuestItemsCount(FOOT) >= 50 && st.getQuestItemsCount(SPICE) >= 50)
						st.set("cond", "2");
				break;
			
			case ATROX:
			case ATROXSPAWN:
				if (st.dropItemsAlways(SPICE, 1, 50))
					if (st.getQuestItemsCount(TRUNK) >= 50 && st.getQuestItemsCount(FOOT) >= 50)
						st.set("cond", "2");
				break;
			
			case BANDERSNATCH:
				if (st.dropItemsAlways(FOOT, 1, 50))
					if (st.getQuestItemsCount(TRUNK) >= 50 && st.getQuestItemsCount(SPICE) >= 50)
						st.set("cond", "2");
				break;
		}
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q624_TheFinestIngredients_Part1();
	}
}