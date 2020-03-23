package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Rnd;

public class Q652_AnAgedExAdventurer extends Quest
{
	private static final String qn = "Q652_AnAgedExAdventurer";
	
	// NPCs
	private static final int TANTAN = 32012;
	private static final int SARA = 30180;
	
	// Item
	private static final int CSS = 1464;
	
	// Reward
	private static final int EAD = 956;
	
	// Table of possible spawns
	private static final int[][] spawns =
	{
		{
			78355,
			-1325,
			-3659
		},
		{
			79890,
			-6132,
			-2922
		},
		{
			90012,
			-7217,
			-3085
		},
		{
			94500,
			-10129,
			-3290
		},
		{
			96534,
			-1237,
			-3677
		}
	};
	
	// Current position
	private int _currentPosition = 0;
	
	public Q652_AnAgedExAdventurer()
	{
		super(652, qn, "An Aged Ex-Adventurer");
		
		addStartNpc(TANTAN);
		addTalkId(TANTAN, SARA);
		
		addSpawn(TANTAN, 78355, -1325, -3659, 0, false, 0, false);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("32012-02.htm"))
		{
			if (st.getQuestItemsCount(CSS) >= 100)
			{
				st.set("cond", "1");
				st.setState(STATE_STARTED);
				st.takeItems(CSS, 100);
				st.playSound(QuestState.SOUND_ACCEPT);
				
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(85326, 7869, -3620, 0));
				startQuestTimer("apparition_npc", 6000, npc, player, false);
			}
			else
			{
				htmltext = "32012-02a.htm";
				st.exitQuest(true);
			}
		}
		else if (event.equalsIgnoreCase("apparition_npc"))
		{
			int chance = Rnd.get(5);
			
			// Loop to avoid to spawn to the same place.
			while (chance == _currentPosition)
				chance = Rnd.get(5);
			
			// Register new position.
			_currentPosition = chance;
			
			npc.deleteMe();
			addSpawn(TANTAN, spawns[chance][0], spawns[chance][1], spawns[chance][2], 0, false, 0, false);
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
				if (player.getLevel() >= 46)
					htmltext = "32012-01.htm";
				else
				{
					htmltext = "32012-00.htm";
					st.exitQuest(true);
				}
				break;
			
			case STATE_STARTED:
				switch (npc.getNpcId())
				{
					case SARA:
						if (Rnd.get(100) < 50)
						{
							htmltext = "30180-01.htm";
							st.rewardItems(57, 5026);
							st.giveItems(EAD, 1);
						}
						else
						{
							htmltext = "30180-02.htm";
							st.rewardItems(57, 10000);
						}
						st.playSound(QuestState.SOUND_FINISH);
						st.exitQuest(true);
						break;
					
					case TANTAN:
						htmltext = "32012-04a.htm";
						break;
				}
				break;
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Q652_AnAgedExAdventurer();
	}
}