package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q381_LetsBecomeARoyalMember extends Quest
{
	private static final String qn = "Q381_LetsBecomeARoyalMember";
	
	// NPCs
	private static final int SORINT = 30232;
	private static final int SANDRA = 30090;
	
	// Items
	private static final int KAILS_COIN = 5899;
	private static final int COIN_ALBUM = 5900;
	private static final int GOLDEN_CLOVER_COIN = 7569;
	private static final int COIN_COLLECTOR_MEMBERSHIP_1 = 3813;
	
	// Reward
	private static final int ROYAL_MEMBERSHIP = 5898;
	
	public Q381_LetsBecomeARoyalMember()
	{
		super(381, qn, "Lets Become a Royal Member!");
		
		setItemsIds(KAILS_COIN, GOLDEN_CLOVER_COIN);
		
		addStartNpc(SORINT);
		addTalkId(SORINT, SANDRA);
		
		addKillId(21018, 27316);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30090-02.htm"))
			st.set("aCond", "1"); // Alternative cond used for Sandra.
		else if (event.equalsIgnoreCase("30232-03.htm"))
		{
			st.set("cond", "1");
			st.setState(STATE_STARTED);
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
				htmltext = (player.getLevel() < 55 || !st.hasQuestItems(COIN_COLLECTOR_MEMBERSHIP_1)) ? "30232-02.htm" : "30232-01.htm";
				break;
			
			case STATE_STARTED:
				switch (npc.getNpcId())
				{
					case SORINT:
						if (!st.hasQuestItems(KAILS_COIN))
							htmltext = "30232-04.htm";
						else if (!st.hasQuestItems(COIN_ALBUM))
							htmltext = "30232-05.htm";
						else
						{
							htmltext = "30232-06.htm";
							st.takeItems(KAILS_COIN, -1);
							st.takeItems(COIN_ALBUM, -1);
							st.giveItems(ROYAL_MEMBERSHIP, 1);
							st.playSound(QuestState.SOUND_FINISH);
							st.exitQuest(true);
						}
						break;
					
					case SANDRA:
						if (!st.hasQuestItems(COIN_ALBUM))
						{
							if (st.getInt("aCond") == 0)
								htmltext = "30090-01.htm";
							else
							{
								if (!st.hasQuestItems(GOLDEN_CLOVER_COIN))
									htmltext = "30090-03.htm";
								else
								{
									htmltext = "30090-04.htm";
									st.takeItems(GOLDEN_CLOVER_COIN, -1);
									st.giveItems(COIN_ALBUM, 1);
								}
							}
						}
						else
							htmltext = "30090-05.htm";
						break;
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
		
		switch (npc.getNpcId())
		{
			case 21018:
				st.dropItems(KAILS_COIN, 1, 1, 50000);
				break;
			
			case 27316:
				st.dropItemsAlways(GOLDEN_CLOVER_COIN, 1, 1);
				break;
		}
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q381_LetsBecomeARoyalMember();
	}
}