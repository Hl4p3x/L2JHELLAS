package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jhellas.util.Rnd;

public class Q651_RunawayYouth extends Quest
{
	private static final String qn = "Q651_RunawayYouth";
	
	// NPCs
	private static final int IVAN = 32014;
	private static final int BATIDAE = 31989;
	
	// Item
	private static final int SOE = 736;
	
	// Table of possible spawns
	private static final int[][] spawns =
	{
		{
			118600,
			-161235,
			-1119
		},
		{
			108380,
			-150268,
			-2376
		},
		{
			123254,
			-148126,
			-3425
		}
	};
	
	// Current position
	private int _currentPosition = 0;
	
	public Q651_RunawayYouth()
	{
		super(651, qn, "Runaway Youth");
		
		addStartNpc(IVAN);
		addTalkId(IVAN, BATIDAE);
		
		addSpawn(IVAN, 118600, -161235, -1119, 0, false, 0, false);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("32014-04.htm"))
		{
			if (st.hasQuestItems(SOE))
			{
				st.set("cond", "1");
				st.setState(STATE_STARTED);
				st.takeItems(SOE, 1);
				st.playSound(QuestState.SOUND_ACCEPT);
				
				htmltext = "32014-03.htm";
				npc.broadcastPacket(new MagicSkillUse(npc, npc, 2013, 1, 3500, 0));
				startQuestTimer("apparition_npc", 4000, npc, player, false);
			}
			else
				st.exitQuest(true);
		}
		else if (event.equalsIgnoreCase("apparition_npc"))
		{
			int chance = Rnd.get(3);
			
			// Loop to avoid to spawn to the same place.
			while (chance == _currentPosition)
				chance = Rnd.get(3);
			
			// Register new position.
			_currentPosition = chance;
			
			npc.deleteMe();
			addSpawn(IVAN, spawns[chance][0], spawns[chance][1], spawns[chance][2], 0, false, 0, false);
			return null;
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
				if (player.getLevel() >= 26)
					htmltext = "32014-02.htm";
				else
				{
					htmltext = "32014-01.htm";
					st.exitQuest(true);
				}
				break;
			
			case STATE_STARTED:
				switch (npc.getNpcId())
				{
					case BATIDAE:
						htmltext = "31989-01.htm";
						st.rewardItems(57, 2883);
						st.playSound(QuestState.SOUND_FINISH);
						st.exitQuest(true);
						break;
					
					case IVAN:
						htmltext = "32014-04a.htm";
						break;
				}
				break;
		}
		
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Q651_RunawayYouth();
	}
}