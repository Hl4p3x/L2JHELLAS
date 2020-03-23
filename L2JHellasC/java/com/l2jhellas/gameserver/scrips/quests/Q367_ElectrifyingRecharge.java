package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Rnd;

public class Q367_ElectrifyingRecharge extends Quest
{
	private static final String qn = "Q367_ElectrifyingRecharge";
	
	// NPCs
	private static final int LORAIN = 30673;
	
	// Item
	private static final int LORAINS_LAMP = 5875;
	private static final int T_L1 = 5876;
	private static final int T_L2 = 5877;
	private static final int T_L3 = 5878;
	private static final int T_L4 = 5879;
	private static final int T_L5 = 5880;
	
	// Reward
	private static final int REWARD[] =
	{
		4553,
		4554,
		4555,
		4556,
		4557,
		4558,
		4559,
		4560,
		4561,
		4562,
		4563,
		4564
	};
	
	// Mobs
	private static final int CATHEROK = 21035;
	
	public Q367_ElectrifyingRecharge()
	{
		super(367, qn, "Electrifying Recharge!");
		
		setItemsIds(LORAINS_LAMP, T_L1, T_L2, T_L3, T_L4, T_L5);
		
		addStartNpc(LORAIN);
		addTalkId(LORAIN);
		
		addSpellFinishedId(CATHEROK);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30673-03.htm"))
		{
			st.set("cond", "1");
			st.setState(STATE_STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(LORAINS_LAMP, 1);
		}
		else if (event.equalsIgnoreCase("30673-09.htm"))
		{
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(LORAINS_LAMP, 1);
		}
		else if (event.equalsIgnoreCase("30673-08.htm"))
		{
			st.playSound(QuestState.SOUND_GIVEUP);
			st.exitQuest(true);
		}
		else if (event.equalsIgnoreCase("30673-07.htm"))
		{
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(LORAINS_LAMP, 1);
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
				if (player.getLevel() >= 37)
					htmltext = "30673-01.htm";
				else
				{
					htmltext = "30673-02.htm";
					st.exitQuest(true);
				}
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				if (cond == 1)
				{
					if (st.hasQuestItems(5880))
					{
						htmltext = "30673-05.htm";
						st.takeItems(5880, 1);
						st.giveItems(LORAINS_LAMP, 1);
						st.playSound(QuestState.SOUND_ACCEPT);
					}
					else if (st.hasQuestItems(5876))
					{
						htmltext = "30673-04.htm";
						st.takeItems(5876, 1);
					}
					else if (st.hasQuestItems(5877))
					{
						htmltext = "30673-04.htm";
						st.takeItems(5877, 1);
					}
					else if (st.hasQuestItems(5878))
					{
						htmltext = "30673-04.htm";
						st.takeItems(5878, 1);
					}
					else
						htmltext = "30673-03.htm";
				}
				else if (cond == 2 && st.hasQuestItems(5879))
				{
					htmltext = "30673-06.htm";
					st.takeItems(5879, 1);
					st.rewardItems(REWARD[Rnd.get(REWARD.length)], 1);
					st.playSound(QuestState.SOUND_FINISH);
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		QuestState st = checkPlayerCondition(player, npc, "cond", "1");
		if (st == null)
			return null;
		
		if (skill.getId() == 4072)
		{
			if (st.hasQuestItems(LORAINS_LAMP))
			{
				int randomItem = Rnd.get(5876, 5880);
				
				st.takeItems(LORAINS_LAMP, 1);
				st.giveItems(randomItem, 1);
				
				if (randomItem == 5879)
				{
					st.set("cond", "2");
					st.playSound(QuestState.SOUND_MIDDLE);
				}
				else
					st.playSound(QuestState.SOUND_ITEMGET);
			}
		}
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q367_ElectrifyingRecharge();
	}
}