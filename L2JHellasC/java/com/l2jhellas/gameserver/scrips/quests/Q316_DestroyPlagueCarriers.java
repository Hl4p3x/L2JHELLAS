package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.enums.player.ClassRace;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q316_DestroyPlagueCarriers extends Quest
{
	private static final String qn = "Q316_DestroyPlagueCarriers";
	
	// Items
	private static final int WERERAT_FANG = 1042;
	private static final int VAROOL_FOULCLAW_FANG = 1043;
	
	// Monsters
	private static final int SUKAR_WERERAT = 20040;
	private static final int SUKAR_WERERAT_LEADER = 20047;
	private static final int VAROOL_FOULCLAW = 27020;
	
	public Q316_DestroyPlagueCarriers()
	{
		super(316, qn, "Destroy Plague Carriers");
		
		setItemsIds(WERERAT_FANG, VAROOL_FOULCLAW_FANG);
		
		addStartNpc(30155); // Ellenia
		addTalkId(30155);
		
		addKillId(SUKAR_WERERAT, SUKAR_WERERAT_LEADER, VAROOL_FOULCLAW);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30155-04.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30155-08.htm"))
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
				if (player.getRace() != ClassRace.ELF)
					htmltext = "30155-00.htm";
				else if (player.getLevel() < 18)
					htmltext = "30155-02.htm";
				else
					htmltext = "30155-03.htm";
				break;
			
			case STATE_STARTED:
				final int ratFangs = st.getQuestItemsCount(WERERAT_FANG);
				final int varoolFangs = st.getQuestItemsCount(VAROOL_FOULCLAW_FANG);
				
				if (ratFangs + varoolFangs == 0)
					htmltext = "30155-05.htm";
				else
				{
					htmltext = "30155-07.htm";
					st.takeItems(WERERAT_FANG, -1);
					st.takeItems(VAROOL_FOULCLAW_FANG, -1);
					st.rewardItems(57, ratFangs * 30 + varoolFangs * 10000 + ((ratFangs > 10) ? 5000 : 0));
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
			case SUKAR_WERERAT:
			case SUKAR_WERERAT_LEADER:
				st.dropItems(WERERAT_FANG, 1, 0, 600000);
				break;
			
			case VAROOL_FOULCLAW:
				st.dropItems(VAROOL_FOULCLAW_FANG, 1, 1, 100000);
				break;
		}
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q316_DestroyPlagueCarriers();
	}
}