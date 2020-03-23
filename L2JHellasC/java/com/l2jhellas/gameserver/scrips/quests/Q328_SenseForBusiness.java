package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Rnd;

public class Q328_SenseForBusiness extends Quest
{
	private static final String qn = "Q328_SenseForBusiness";
	
	// Items
	private static final int MONSTER_EYE_LENS = 1366;
	private static final int MONSTER_EYE_CARCASS = 1347;
	private static final int BASILISK_GIZZARD = 1348;
	
	private static final int[][] DROPLIST =
	{
		{
			20055,
			61,
			62
		},
		{
			20059,
			61,
			62
		},
		{
			20067,
			72,
			74
		},
		{
			20068,
			78,
			79
		},
		{
			20070,
			60,
			0
		},
		{
			20072,
			63,
			0
		},
	};
	
	public Q328_SenseForBusiness()
	{
		super(328, qn, "Sense for Business");
		
		setItemsIds(MONSTER_EYE_LENS, MONSTER_EYE_CARCASS, BASILISK_GIZZARD);
		
		addStartNpc(30436); // Sarien
		addTalkId(30436);
		
		addKillId(20055, 20059, 20067, 20068, 20070, 20072);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30436-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30436-06.htm"))
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
				htmltext = (player.getLevel() < 21) ? "30436-01.htm" : "30436-02.htm";
				break;
			
			case STATE_STARTED:
				final int carcasses = st.getQuestItemsCount(MONSTER_EYE_CARCASS);
				final int lenses = st.getQuestItemsCount(MONSTER_EYE_LENS);
				final int gizzards = st.getQuestItemsCount(BASILISK_GIZZARD);
				
				final int all = carcasses + lenses + gizzards;
				
				if (all == 0)
					htmltext = "30436-04.htm";
				else
				{
					htmltext = "30436-05.htm";
					st.takeItems(MONSTER_EYE_CARCASS, -1);
					st.takeItems(MONSTER_EYE_LENS, -1);
					st.takeItems(BASILISK_GIZZARD, -1);
					st.rewardItems(57, (25 * carcasses) + (1000 * lenses) + (60 * gizzards) + ((all >= 10) ? 618 : 0));
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
		
		final int chance = Rnd.get(100);
		
		for (int[] dropInfos : DROPLIST)
		{
			if (dropInfos[0] == npc.getNpcId())
			{
				final int chanceToReach = dropInfos[1];
				final int secondChanceToReach = dropInfos[2];
				
				if (secondChanceToReach == 0)
				{
					if (chance < chanceToReach)
						st.dropItemsAlways(BASILISK_GIZZARD, 1, 0);
				}
				else
				{
					if (chance < chanceToReach)
						st.dropItemsAlways(MONSTER_EYE_LENS, 1, 0);
					else if (chance < secondChanceToReach)
						st.dropItemsAlways(MONSTER_EYE_CARCASS, 1, 0);
				}
				break;
			}
		}
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q328_SenseForBusiness();
	}
}