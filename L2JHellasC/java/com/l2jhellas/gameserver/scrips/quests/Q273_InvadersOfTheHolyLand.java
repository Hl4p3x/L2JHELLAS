package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.enums.player.ClassRace;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Rnd;

public class Q273_InvadersOfTheHolyLand extends Quest
{
	private static final String qn = "Q273_InvadersOfTheHolyLand";
	
	// Items
	private static final int BLACK_SOULSTONE = 1475;
	private static final int RED_SOULSTONE = 1476;
	
	// Reward
	private static final int SOULSHOT_FOR_BEGINNERS = 5789;
	
	public Q273_InvadersOfTheHolyLand()
	{
		super(273, qn, "Invaders of the Holy Land");
		
		setItemsIds(BLACK_SOULSTONE, RED_SOULSTONE);
		
		addStartNpc(30566); // Varkees
		addTalkId(30566);
		
		addKillId(20311, 20312, 20313);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30566-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30566-07.htm"))
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
				if (player.getRace() != ClassRace.ORC)
					htmltext = "30566-00.htm";
				else if (player.getLevel() < 6)
					htmltext = "30566-01.htm";
				else
					htmltext = "30566-02.htm";
				break;
			
			case STATE_STARTED:
				int red = st.getQuestItemsCount(RED_SOULSTONE);
				int black = st.getQuestItemsCount(BLACK_SOULSTONE);
				
				if (red + black == 0)
					htmltext = "30566-04.htm";
				else
				{
					if (red == 0)
						htmltext = "30566-05.htm";
					else
						htmltext = "30566-06.htm";
					
					int reward = (black * 3) + (red * 10) + ((black >= 10) ? ((red >= 1) ? 1800 : 1500) : 0);
					
					st.takeItems(BLACK_SOULSTONE, -1);
					st.takeItems(RED_SOULSTONE, -1);
					st.rewardItems(57, reward);
					
					if (player.isNewbie() && st.getInt("Reward") == 0)
					{
						st.giveItems(SOULSHOT_FOR_BEGINNERS, 6000);
						st.playTutorialVoice("tutorial_voice_026");
						st.set("Reward", "1");
					}
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
		
		int probability = 77;
		if (npc.getNpcId() == 20311)
			probability = 90;
		else if (npc.getNpcId() == 20312)
			probability = 87;
		
		if (Rnd.get(100) <= probability)
			st.giveItems(BLACK_SOULSTONE, 1);
		else
			st.giveItems(RED_SOULSTONE, 1);
		
		st.playSound(QuestState.SOUND_ITEMGET);
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q273_InvadersOfTheHolyLand();
	}
}