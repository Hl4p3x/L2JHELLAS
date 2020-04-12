package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Rnd;

public class Q647_InfluxOfMachines extends Quest
{
	private static final String qn = "Q647_InfluxOfMachines";
	
	// Item
	private static final int DESTROYED_GOLEM_SHARD = 8100;
	
	// NPC
	private static final int Gutenhagen = 32069;
	
	// Low B-grade weapons recipes
	private static final int recipes[] =
	{
		4963,
		4964,
		4965,
		4966,
		4967,
		4968,
		4969,
		4970,
		4971,
		4972
	};
	
	public Q647_InfluxOfMachines()
	{
		super(647, qn, "Influx of Machines");
		
		setItemsIds(DESTROYED_GOLEM_SHARD);
		
		addStartNpc(Gutenhagen);
		addTalkId(Gutenhagen);
		
		for (int i = 22052; i < 22079; i++)
			addKillId(i);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("32069-02.htm"))
		{
			st.set("cond", "1");
			st.setState(STATE_STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("32069-06.htm"))
		{
			if (st.getQuestItemsCount(DESTROYED_GOLEM_SHARD) >= 500)
			{
				st.takeItems(DESTROYED_GOLEM_SHARD, -1);
				st.giveItems(recipes[Rnd.get(recipes.length)], 1);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(true);
			}
			else
				htmltext = "32069-04.htm";
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
				if (player.getLevel() >= 46)
					htmltext = "32069-01.htm";
				else
				{
					htmltext = "32069-03.htm";
					st.exitQuest(true);
				}
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				if (cond == 1)
					htmltext = "32069-04.htm";
				else if (cond == 2)
				{
					if (st.getQuestItemsCount(DESTROYED_GOLEM_SHARD) >= 500)
						htmltext = "32069-05.htm";
					else
						st.set("cond", "1");
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = getRandomPartyMember(player, npc, "1");
		if (st == null)
			return null;
				
		if (st.dropItems(DESTROYED_GOLEM_SHARD, 1, 500, 300000))
			st.set("cond", "2");
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q647_InfluxOfMachines();
	}
}