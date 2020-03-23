package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.enums.player.ClassRace;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Rnd;

public class Q271_ProofOfValor extends Quest
{
	private static final String qn = "Q271_ProofOfValor";
	
	// Item
	private static final int KASHA_WOLF_FANG = 1473;
	
	// Rewards
	private static final int NECKLACE_OF_VALOR = 1507;
	private static final int NECKLACE_OF_COURAGE = 1506;
	
	public Q271_ProofOfValor()
	{
		super(271, qn, "Proof of Valor");
		
		setItemsIds(KASHA_WOLF_FANG);
		
		addStartNpc(30577); // Rukain
		addTalkId(30577);
		
		addKillId(20475); // Kasha Wolf
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30577-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			
			if (st.hasAtLeastOneQuestItem(NECKLACE_OF_COURAGE, NECKLACE_OF_VALOR))
				htmltext = "30577-07.htm";
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
					htmltext = "30577-00.htm";
				else if (player.getLevel() < 4)
					htmltext = "30577-01.htm";
				else
					htmltext = (st.hasAtLeastOneQuestItem(NECKLACE_OF_COURAGE, NECKLACE_OF_VALOR)) ? "30577-06.htm" : "30577-02.htm";
				break;
			
			case STATE_STARTED:
				if (st.getInt("cond") == 1)
					htmltext = (st.hasAtLeastOneQuestItem(NECKLACE_OF_COURAGE, NECKLACE_OF_VALOR)) ? "30577-07.htm" : "30577-04.htm";
				else
				{
					htmltext = "30577-05.htm";
					st.takeItems(KASHA_WOLF_FANG, -1);
					st.giveItems((Rnd.get(100) < 10) ? NECKLACE_OF_VALOR : NECKLACE_OF_COURAGE, 1);
					st.playSound(QuestState.SOUND_FINISH);
					st.exitQuest(true);
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = checkPlayerCondition(player, npc, "cond", "1");
		if (st == null)
			return null;
		
		if (st.dropItemsAlways(KASHA_WOLF_FANG, (Rnd.get(100) < 25) ? 2 : 1, 50))
			st.set("cond", "2");
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q271_ProofOfValor();
	}
}