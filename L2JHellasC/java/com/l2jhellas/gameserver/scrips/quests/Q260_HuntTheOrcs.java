package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.enums.player.ClassRace;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Rnd;

public class Q260_HuntTheOrcs extends Quest
{
	private static final String qn = "Q260_HuntTheOrcs";
	
	// NPC
	private static final int RAYEN = 30221;
	
	// Items
	private static final int ORC_AMULET = 1114;
	private static final int ORC_NECKLACE = 1115;
	
	// Monsters
	private static final int KABOO_ORC = 20468;
	private static final int KABOO_ORC_ARCHER = 20469;
	private static final int KABOO_ORC_GRUNT = 20470;
	private static final int KABOO_ORC_FIGHTER = 20471;
	private static final int KABOO_ORC_FIGHTER_LEADER = 20472;
	private static final int KABOO_ORC_FIGHTER_LIEUTENANT = 20473;
	
	public Q260_HuntTheOrcs()
	{
		super(260, qn, "Hunt the Orcs");
		
		setItemsIds(ORC_AMULET, ORC_NECKLACE);
		
		addStartNpc(RAYEN);
		addTalkId(RAYEN);
		
		addKillId(KABOO_ORC, KABOO_ORC_ARCHER, KABOO_ORC_GRUNT, KABOO_ORC_FIGHTER, KABOO_ORC_FIGHTER_LEADER, KABOO_ORC_FIGHTER_LIEUTENANT);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30221-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30221-06.htm"))
		{
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
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
				if (player.getRace() != ClassRace.ELF)
					htmltext = "30221-00.htm";
				else if (player.getLevel() < 6)
					htmltext = "30221-01.htm";
				else
					htmltext = "30221-02.htm";
				break;
			
			case STATE_STARTED:
				int amulet = st.getQuestItemsCount(ORC_AMULET);
				int necklace = st.getQuestItemsCount(ORC_NECKLACE);
				
				if (amulet == 0 && necklace == 0)
					htmltext = "30221-04.htm";
				else
				{
					htmltext = "30221-05.htm";
					st.takeItems(ORC_AMULET, -1);
					st.takeItems(ORC_NECKLACE, -1);
					st.rewardItems(57, amulet * 5 + necklace * 15);
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
		
		if (Rnd.get(100) < 40)
		{
			switch (npc.getNpcId())
			{
				case KABOO_ORC:
				case KABOO_ORC_GRUNT:
				case KABOO_ORC_ARCHER:
					st.giveItems(ORC_AMULET, 1);
					break;
				
				case KABOO_ORC_FIGHTER:
				case KABOO_ORC_FIGHTER_LEADER:
				case KABOO_ORC_FIGHTER_LIEUTENANT:
					st.giveItems(ORC_NECKLACE, 1);
					break;
			}
			st.playSound(QuestState.SOUND_ITEMGET);
		}
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q260_HuntTheOrcs();
	}
}