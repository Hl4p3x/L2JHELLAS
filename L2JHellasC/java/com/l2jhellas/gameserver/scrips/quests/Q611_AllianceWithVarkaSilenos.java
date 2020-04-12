package com.l2jhellas.gameserver.scrips.quests;

import java.util.HashMap;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Rnd;

public class Q611_AllianceWithVarkaSilenos extends Quest
{
	private static final String qn = "Q611_AllianceWithVarkaSilenos";
	private static final String qn2 = "Q612_WarWithKetraOrcs";
	
	private static final HashMap<Integer, Integer> Chance = new HashMap<>();
	{
		Chance.put(21324, 508000);
		Chance.put(21325, 500000);
		Chance.put(21327, 500000);
		Chance.put(21328, 519000);
		Chance.put(21329, 527000);
		Chance.put(21331, 518000);
		Chance.put(21332, 626000);
		Chance.put(21334, 500000);
		Chance.put(21335, 500000);
		Chance.put(21336, 521000);
		Chance.put(21338, 500000);
		Chance.put(21339, 628000);
		Chance.put(21340, 500000);
		Chance.put(21342, 509000);
		Chance.put(21343, 509000);
		Chance.put(21344, 518000);
		Chance.put(21345, 626000);
		Chance.put(21346, 604000);
		Chance.put(21347, 649000);
		Chance.put(21348, 604000);
		Chance.put(21349, 627000);
	}
	
	private static final HashMap<Integer, Integer> ChanceMolar = new HashMap<>();
	{
		ChanceMolar.put(21324, 500000);
		ChanceMolar.put(21327, 510000);
		ChanceMolar.put(21328, 522000);
		ChanceMolar.put(21329, 519000);
		ChanceMolar.put(21331, 529000);
		ChanceMolar.put(21332, 529000);
		ChanceMolar.put(21334, 539000);
		ChanceMolar.put(21336, 568000);
		ChanceMolar.put(21338, 558000);
		ChanceMolar.put(21339, 568000);
		ChanceMolar.put(21340, 664000);
		ChanceMolar.put(21342, 568000);
		ChanceMolar.put(21343, 548000);
		ChanceMolar.put(21345, 713000);
		ChanceMolar.put(21347, 773000);
	}
	
	// Quest Items
	private static final int Ketra_Badge_Soldier = 7226;
	private static final int Ketra_Badge_Officer = 7227;
	private static final int Ketra_Badge_Captain = 7228;
	
	private static final int Varka_Alliance_One = 7221;
	private static final int Varka_Alliance_Two = 7222;
	private static final int Varka_Alliance_Three = 7223;
	private static final int Varka_Alliance_Four = 7224;
	private static final int Varka_Alliance_Five = 7225;
	
	private static final int Valor_Feather = 7229;
	private static final int Wisdom_Feather = 7230;
	
	private static final int Molar = 7234;
	
	public Q611_AllianceWithVarkaSilenos()
	{
		super(611, qn, "Alliance with Varka Silenos");
		
		setItemsIds(Ketra_Badge_Soldier, Ketra_Badge_Officer, Ketra_Badge_Captain);
		
		addStartNpc(31378); // Naran Ashanuk
		addTalkId(31378);
		
		for (int mobs : Chance.keySet())
			addKillId(mobs);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31378-03a.htm"))
		{
			if (player.getLevel() >= 74)
			{
				st.set("cond", "1");
				st.setState(STATE_STARTED);
				st.playSound(QuestState.SOUND_ACCEPT);
			}
			else
			{
				htmltext = "31378-02b.htm";
				st.exitQuest(true);
				player.setAllianceWithVarkaKetra(0);
			}
		}
		// Stage 1
		else if (event.equalsIgnoreCase("31378-10-1.htm"))
		{
			if (st.getQuestItemsCount(Ketra_Badge_Soldier) >= 100)
			{
				st.takeItems(Ketra_Badge_Soldier, -1);
				st.giveItems(Varka_Alliance_One, 1);
				player.setAllianceWithVarkaKetra(-1);
				st.playSound(QuestState.SOUND_MIDDLE);
			}
			else
				htmltext = "31378-03b.htm";
		}
		// Stage 2
		else if (event.equalsIgnoreCase("31378-10-2.htm"))
		{
			if (st.getQuestItemsCount(Ketra_Badge_Soldier) >= 200 && st.getQuestItemsCount(Ketra_Badge_Officer) >= 100)
			{
				st.takeItems(Ketra_Badge_Soldier, -1);
				st.takeItems(Ketra_Badge_Officer, -1);
				st.takeItems(Varka_Alliance_One, -1);
				st.giveItems(Varka_Alliance_Two, 1);
				player.setAllianceWithVarkaKetra(-2);
				st.playSound(QuestState.SOUND_MIDDLE);
			}
			else
				htmltext = "31378-12.htm";
		}
		// Stage 3
		else if (event.equalsIgnoreCase("31378-10-3.htm"))
		{
			if (st.getQuestItemsCount(Ketra_Badge_Soldier) >= 300 && st.getQuestItemsCount(Ketra_Badge_Officer) >= 200 && st.getQuestItemsCount(Ketra_Badge_Captain) >= 100)
			{
				st.takeItems(Ketra_Badge_Soldier, -1);
				st.takeItems(Ketra_Badge_Officer, -1);
				st.takeItems(Ketra_Badge_Captain, -1);
				st.takeItems(Varka_Alliance_Two, -1);
				st.giveItems(Varka_Alliance_Three, 1);
				player.setAllianceWithVarkaKetra(-3);
				st.playSound(QuestState.SOUND_MIDDLE);
			}
			else
				htmltext = "31378-15.htm";
		}
		// Stage 4
		else if (event.equalsIgnoreCase("31378-10-4.htm"))
		{
			if (st.getQuestItemsCount(Ketra_Badge_Soldier) >= 300 && st.getQuestItemsCount(Ketra_Badge_Officer) >= 300 && st.getQuestItemsCount(Ketra_Badge_Captain) >= 200 && st.getQuestItemsCount(Valor_Feather) >= 1)
			{
				st.takeItems(Ketra_Badge_Soldier, -1);
				st.takeItems(Ketra_Badge_Officer, -1);
				st.takeItems(Ketra_Badge_Captain, -1);
				st.takeItems(Varka_Alliance_Three, -1);
				st.takeItems(Valor_Feather, -1);
				st.giveItems(Varka_Alliance_Four, 1);
				player.setAllianceWithVarkaKetra(-4);
				st.playSound(QuestState.SOUND_MIDDLE);
			}
			else
				htmltext = "31378-21.htm";
		}
		// Leave quest
		else if (event.equalsIgnoreCase("31378-20.htm"))
		{
			st.takeItems(Varka_Alliance_One, -1);
			st.takeItems(Varka_Alliance_Two, -1);
			st.takeItems(Varka_Alliance_Three, -1);
			st.takeItems(Varka_Alliance_Four, -1);
			st.takeItems(Varka_Alliance_Five, -1);
			st.takeItems(Valor_Feather, -1);
			st.takeItems(Wisdom_Feather, -1);
			player.setAllianceWithVarkaKetra(0);
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
				if (player.isAlliedWithKetra())
				{
					htmltext = "31378-02a.htm";
					st.exitQuest(true);
				}
				else
					htmltext = "31378-01.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				if (st.getQuestItemsCount(Varka_Alliance_One) + st.getQuestItemsCount(Varka_Alliance_Two) + st.getQuestItemsCount(Varka_Alliance_Three) + st.getQuestItemsCount(Varka_Alliance_Four) + st.getQuestItemsCount(Varka_Alliance_Five) == 0)
				{
					if (st.getQuestItemsCount(Ketra_Badge_Soldier) < 100)
						htmltext = "31378-03b.htm";
					else
						htmltext = "31378-09.htm";
				}
				else if (st.getQuestItemsCount(Varka_Alliance_One) == 1)
				{
					if (cond != 2)
					{
						htmltext = "31378-04.htm";
						st.set("cond", "2");
						player.setAllianceWithVarkaKetra(-1);
						st.playSound(QuestState.SOUND_MIDDLE);
					}
					else
					{
						if (st.getQuestItemsCount(Ketra_Badge_Soldier) < 200 || st.getQuestItemsCount(Ketra_Badge_Officer) < 100)
							htmltext = "31378-12.htm";
						else
							htmltext = "31378-13.htm";
					}
				}
				else if (st.getQuestItemsCount(Varka_Alliance_Two) == 1)
				{
					if (cond != 3)
					{
						htmltext = "31378-05.htm";
						st.set("cond", "3");
						player.setAllianceWithVarkaKetra(-2);
						st.playSound(QuestState.SOUND_MIDDLE);
					}
					else
					{
						if (st.getQuestItemsCount(Ketra_Badge_Captain) < 100 || st.getQuestItemsCount(Ketra_Badge_Soldier) < 300 || st.getQuestItemsCount(Ketra_Badge_Officer) < 200)
							htmltext = "31378-15.htm";
						else
							htmltext = "31378-16.htm";
					}
				}
				else if (st.getQuestItemsCount(Varka_Alliance_Three) == 1)
				{
					if (cond != 4)
					{
						htmltext = "31378-06.htm";
						st.set("cond", "4");
						player.setAllianceWithVarkaKetra(-3);
						st.playSound(QuestState.SOUND_MIDDLE);
					}
					else
					{
						if (st.getQuestItemsCount(Ketra_Badge_Captain) < 200 || st.getQuestItemsCount(Ketra_Badge_Soldier) < 300 || st.getQuestItemsCount(Ketra_Badge_Officer) < 300 || st.getQuestItemsCount(Valor_Feather) == 0)
							htmltext = "31378-21.htm";
						else
							htmltext = "31378-22.htm";
					}
				}
				else if (st.getQuestItemsCount(Varka_Alliance_Four) == 1)
				{
					if (cond != 5)
					{
						htmltext = "31378-07.htm";
						st.set("cond", "5");
						player.setAllianceWithVarkaKetra(-4);
						st.playSound(QuestState.SOUND_MIDDLE);
					}
					else
					{
						if (st.getQuestItemsCount(Ketra_Badge_Captain) < 200 || st.getQuestItemsCount(Ketra_Badge_Soldier) < 400 || st.getQuestItemsCount(Ketra_Badge_Officer) < 400 || st.getQuestItemsCount(Wisdom_Feather) == 0)
							htmltext = "31378-17.htm";
						else
						{
							htmltext = "31378-10-5.htm";
							st.takeItems(Ketra_Badge_Soldier, 400);
							st.takeItems(Ketra_Badge_Officer, 400);
							st.takeItems(Ketra_Badge_Captain, 200);
							st.takeItems(Varka_Alliance_Four, -1);
							st.takeItems(Wisdom_Feather, -1);
							st.giveItems(Varka_Alliance_Five, 1);
							player.setAllianceWithVarkaKetra(5);
							st.playSound(QuestState.SOUND_MIDDLE);
						}
					}
				}
				else if (st.getQuestItemsCount(Varka_Alliance_Five) == 1)
				{
					if (cond != 6)
					{
						htmltext = "31378-18.htm";
						st.set("cond", "6");
						player.setAllianceWithVarkaKetra(-5);
						st.playSound(QuestState.SOUND_MIDDLE);
					}
					else
						htmltext = "31378-08.htm";
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = getRandomPartyMemberState(player, npc, STATE_STARTED);
		if (st == null)
			return null;
		
		final int npcId = npc.getNpcId();
		
		// Support for Q612.
		final QuestState st2 = st.getPlayer().getQuestState(qn2);
		if (st2 != null && Rnd.get(1) == 0)
		{
			int chance = ChanceMolar.get(npcId);
			if (chance != 0)
			{
				st2.dropItems(Molar, 1, -1, chance);
				return null;
			}
		}
				
		int cond = st.getInt("cond");
		if (cond == 6)
			return null;
		
		switch (npcId)
		{
			case 21324:
			case 21325:
			case 21327:
			case 21328:
			case 21329:
				if (cond == 1)
					st.dropItems(Ketra_Badge_Soldier, 1, 100, Chance.get(npcId));
				else if (cond == 2)
					st.dropItems(Ketra_Badge_Soldier, 1, 200, Chance.get(npcId));
				else if (cond == 3 || cond == 4)
					st.dropItems(Ketra_Badge_Soldier, 1, 300, Chance.get(npcId));
				else if (cond == 5)
					st.dropItems(Ketra_Badge_Soldier, 1, 400, Chance.get(npcId));
				break;
			
			case 21331:
			case 21332:
			case 21334:
			case 21335:
			case 21336:
			case 21338:
			case 21343:
			case 21344:
				if (cond == 2)
					st.dropItems(Ketra_Badge_Officer, 1, 100, Chance.get(npcId));
				else if (cond == 3)
					st.dropItems(Ketra_Badge_Officer, 1, 200, Chance.get(npcId));
				else if (cond == 4)
					st.dropItems(Ketra_Badge_Officer, 1, 300, Chance.get(npcId));
				else if (cond == 5)
					st.dropItems(Ketra_Badge_Officer, 1, 400, Chance.get(npcId));
				break;
			
			case 21339:
			case 21340:
			case 21342:
			case 21345:
			case 21346:
			case 21347:
			case 21348:
			case 21349:
				if (cond == 3)
					st.dropItems(Ketra_Badge_Captain, 1, 100, Chance.get(npcId));
				else if (cond == 4 || cond == 5)
					st.dropItems(Ketra_Badge_Captain, 1, 200, Chance.get(npcId));
				break;
		}
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q611_AllianceWithVarkaSilenos();
	}
}