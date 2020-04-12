package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q613_ProveYourCourage extends Quest
{
	private static final String qn = "Q613_ProveYourCourage";
	
	// Items
	private static final int Hekaton_Head = 7240;
	private static final int Valor_Feather = 7229;
	private static final int Varka_Alliance_Three = 7223;
	
	public Q613_ProveYourCourage()
	{
		super(613, qn, "Prove your courage!");
		
		setItemsIds(Hekaton_Head);
		
		addStartNpc(31377); // Ashas Varka Durai
		addTalkId(31377);
		
		addKillId(25299); // Hekaton
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31377-04.htm"))
		{
			if (player.getAllianceWithVarkaKetra() <= -3 && st.getQuestItemsCount(Varka_Alliance_Three) > 0 && st.getQuestItemsCount(Valor_Feather) == 0)
			{
				if (player.getLevel() >= 75)
				{
					st.set("cond", "1");
					st.setState(STATE_STARTED);
					st.playSound(QuestState.SOUND_ACCEPT);
				}
				else
				{
					htmltext = "31377-03.htm";
					st.exitQuest(true);
				}
			}
			else
			{
				htmltext = "31377-02.htm";
				st.exitQuest(true);
			}
		}
		else if (event.equalsIgnoreCase("31377-07.htm"))
		{
			if (st.getQuestItemsCount(Hekaton_Head) == 1)
			{
				st.takeItems(Hekaton_Head, -1);
				st.giveItems(Valor_Feather, 1);
				st.rewardExpAndSp(10000, 0);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
			}
			else
			{
				htmltext = "31377-06.htm";
				st.set("cond", "1");
				st.playSound(QuestState.SOUND_ACCEPT);
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
				htmltext = "31377-01.htm";
				break;
			
			case STATE_STARTED:
				if (st.getQuestItemsCount(Hekaton_Head) == 1)
					htmltext = "31377-05.htm";
				else
					htmltext = "31377-06.htm";
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		for (QuestState st : getPartyMembers(player, npc, "cond", "1"))
		{
			if(st == null)
				continue;
			
			if (st.getPlayer().getAllianceWithVarkaKetra() <= -3 && st.hasQuestItems(Varka_Alliance_Three))
			{
				st.set("cond", "2");
				st.giveItems(Hekaton_Head, 1);
				st.playSound(QuestState.SOUND_MIDDLE);
			}
		}
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q613_ProveYourCourage();
	}
}