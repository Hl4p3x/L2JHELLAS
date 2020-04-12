package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Rnd;

public class Q371_ShriekOfGhosts extends Quest
{
	private static final String qn = "Q371_ShriekOfGhosts";
	
	// NPCs
	private static final int REVA = 30867;
	private static final int PATRIN = 30929;
	
	// Item
	private static final int URN = 5903;
	private static final int PORCELAIN = 6002;
	
	// Mobs
	private static final int HALLATE_WARRIOR = 20818;
	private static final int HALLATE_KNIGHT = 20820;
	private static final int HALLATE_COMMANDER = 20824;
	
	public Q371_ShriekOfGhosts()
	{
		super(371, qn, "Shriek of Ghosts");
		
		setItemsIds(URN, PORCELAIN);
		
		addStartNpc(REVA);
		addTalkId(REVA, PATRIN);
		
		addKillId(HALLATE_WARRIOR, HALLATE_KNIGHT, HALLATE_COMMANDER);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30867-03.htm"))
		{
			st.set("cond", "1");
			st.setState(STATE_STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30867-07.htm"))
		{
			int urns = st.getQuestItemsCount(URN);
			if (urns > 0)
			{
				st.takeItems(URN, urns);
				if (urns >= 100)
				{
					urns += 13;
					htmltext = "30867-08.htm";
				}
				else
					urns += 7;
				st.rewardItems(57, urns * 1000);
			}
		}
		else if (event.equalsIgnoreCase("30867-10.htm"))
		{
			st.playSound(QuestState.SOUND_GIVEUP);
			st.exitQuest(true);
		}
		else if (event.equalsIgnoreCase("APPR"))
		{
			if (st.hasQuestItems(PORCELAIN))
			{
				int chance = Rnd.get(100);
				
				st.takeItems(PORCELAIN, 1);
				
				if (chance < 2)
				{
					st.giveItems(6003, 1);
					htmltext = "30929-03.htm";
				}
				else if (chance < 32)
				{
					st.giveItems(6004, 1);
					htmltext = "30929-04.htm";
				}
				else if (chance < 62)
				{
					st.giveItems(6005, 1);
					htmltext = "30929-05.htm";
				}
				else if (chance < 77)
				{
					st.giveItems(6006, 1);
					htmltext = "30929-06.htm";
				}
				else
					htmltext = "30929-07.htm";
			}
			else
				htmltext = "30929-02.htm";
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
				if (player.getLevel() >= 59)
					htmltext = "30867-02.htm";
				else
				{
					htmltext = "30867-01.htm";
					st.exitQuest(true);
				}
				break;
			
			case STATE_STARTED:
				switch (npc.getNpcId())
				{
					case REVA:
						if (st.hasQuestItems(URN))
							htmltext = (st.hasQuestItems(PORCELAIN)) ? "30867-05.htm" : "30867-04.htm";
						else
							htmltext = "30867-06.htm";
						break;
					
					case PATRIN:
						htmltext = "30929-01.htm";
						break;
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = getRandomPartyMemberState(player, npc, STATE_STARTED);
		if (st == null)
			return null;
				
		int chance = Rnd.get(100);
		switch (npc.getNpcId())
		{
			case HALLATE_WARRIOR:
				if (chance < 43)
				{
					st.giveItems((chance < 38) ? URN : PORCELAIN, 1);
					st.playSound(QuestState.SOUND_ITEMGET);
				}
				break;
			
			case HALLATE_KNIGHT:
				if (chance < 56)
				{
					st.giveItems((chance < 48) ? URN : PORCELAIN, 1);
					st.playSound(QuestState.SOUND_ITEMGET);
				}
				break;
			
			case HALLATE_COMMANDER:
				if (chance < 58)
				{
					st.giveItems((chance < 50) ? URN : PORCELAIN, 1);
					st.playSound(QuestState.SOUND_ITEMGET);
				}
				break;
		}
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q371_ShriekOfGhosts();
	}
}