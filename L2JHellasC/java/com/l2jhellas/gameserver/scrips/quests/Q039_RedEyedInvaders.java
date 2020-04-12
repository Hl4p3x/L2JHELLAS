package com.l2jhellas.gameserver.scrips.quests;

import java.util.HashMap;
import java.util.Map;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q039_RedEyedInvaders extends Quest
{
	private static final String qn = "Q039_RedEyedInvaders";
	
	// NPCs
	private static final int BABENCO = 30334;
	private static final int BATHIS = 30332;
	
	// Mobs
	private static final int M_LIZARDMAN = 20919;
	private static final int M_LIZARDMAN_SCOUT = 20920;
	private static final int M_LIZARDMAN_GUARD = 20921;
	private static final int ARANEID = 20925;
	
	// Items
	private static final int BLACK_BONE_NECKLACE = 7178;
	private static final int RED_BONE_NECKLACE = 7179;
	private static final int INCENSE_POUCH = 7180;
	private static final int GEM_OF_MAILLE = 7181;
	
	// First droplist
	private static final Map<Integer, int[]> FIRST_DP = new HashMap<>();
	{
		FIRST_DP.put(M_LIZARDMAN_GUARD, new int[]
		{
			RED_BONE_NECKLACE,
			100,
			BLACK_BONE_NECKLACE,
			3,
			330000
		});
		FIRST_DP.put(M_LIZARDMAN, new int[]
		{
			BLACK_BONE_NECKLACE,
			100,
			RED_BONE_NECKLACE,
			3,
			500000
		});
		FIRST_DP.put(M_LIZARDMAN_SCOUT, new int[]
		{
			BLACK_BONE_NECKLACE,
			100,
			RED_BONE_NECKLACE,
			3,
			500000
		});
	}
	
	// Second droplist
	private static final Map<Integer, int[]> SECOND_DP = new HashMap<>();
	{
		SECOND_DP.put(ARANEID, new int[]
		{
			GEM_OF_MAILLE,
			30,
			INCENSE_POUCH,
			5,
			250000
		});
		SECOND_DP.put(M_LIZARDMAN_GUARD, new int[]
		{
			INCENSE_POUCH,
			30,
			GEM_OF_MAILLE,
			5,
			250000
		});
		SECOND_DP.put(M_LIZARDMAN_SCOUT, new int[]
		{
			INCENSE_POUCH,
			30,
			GEM_OF_MAILLE,
			5,
			250000
		});
	}
	
	// Rewards
	private static final int GREEN_COLORED_LURE_HG = 6521;
	private static final int BABY_DUCK_RODE = 6529;
	private static final int FISHING_SHOT_NG = 6535;
	
	public Q039_RedEyedInvaders()
	{
		super(39, qn, "Red-Eyed Invaders");
		
		setItemsIds(BLACK_BONE_NECKLACE, RED_BONE_NECKLACE, INCENSE_POUCH, GEM_OF_MAILLE);
		
		addStartNpc(BABENCO);
		addTalkId(BABENCO, BATHIS);
		
		addKillId(M_LIZARDMAN, M_LIZARDMAN_SCOUT, M_LIZARDMAN_GUARD, ARANEID);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30334-1.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30332-1.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("30332-3.htm"))
		{
			st.set("cond", "4");
			st.takeItems(BLACK_BONE_NECKLACE, -1);
			st.takeItems(RED_BONE_NECKLACE, -1);
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("30332-5.htm"))
		{
			st.takeItems(INCENSE_POUCH, -1);
			st.takeItems(GEM_OF_MAILLE, -1);
			st.giveItems(GREEN_COLORED_LURE_HG, 60);
			st.giveItems(BABY_DUCK_RODE, 1);
			st.giveItems(FISHING_SHOT_NG, 500);
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(false);
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
				htmltext = (player.getLevel() < 20) ? "30334-2.htm" : "30334-0.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case BABENCO:
						htmltext = "30334-3.htm";
						break;
					
					case BATHIS:
						if (cond == 1)
							htmltext = "30332-0.htm";
						else if (cond == 2)
							htmltext = "30332-2a.htm";
						else if (cond == 3)
							htmltext = "30332-2.htm";
						else if (cond == 4)
							htmltext = "30332-3a.htm";
						else if (cond == 5)
							htmltext = "30332-4.htm";
						break;
				}
				break;
			
			case STATE_COMPLETED:
				htmltext = getAlreadyCompletedMsg();
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		final int npcId = npc.getNpcId();
		
		QuestState st = getRandomPartyMember(player, npc, "2");
		if (st != null && npcId != ARANEID)
		{
			final int[] list = FIRST_DP.get(npcId);
			
			if (st.dropItems(list[0], 1, list[1], list[4]) && st.getQuestItemsCount(list[2]) == list[1])
				st.set("cond", String.valueOf(list[3]));
		}
		else
		{
			st = getRandomPartyMember(player, npc, "4");
			if (st != null && npcId != M_LIZARDMAN)
			{
				final int[] list = SECOND_DP.get(npcId);
				
				if (st.dropItems(list[0], 1, list[1], list[4]) && st.getQuestItemsCount(list[2]) == list[1])
					st.set("cond", String.valueOf(list[3]));
			}
		}
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q039_RedEyedInvaders();
	}
}