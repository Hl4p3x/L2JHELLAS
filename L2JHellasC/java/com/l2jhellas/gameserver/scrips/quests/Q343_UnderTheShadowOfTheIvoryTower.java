package com.l2jhellas.gameserver.scrips.quests;

import java.util.HashMap;
import java.util.Map;

import com.l2jhellas.gameserver.enums.player.ClassRace;
import com.l2jhellas.gameserver.enums.player.ClassType;
import com.l2jhellas.gameserver.holder.IntIntHolder;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Rnd;

public class Q343_UnderTheShadowOfTheIvoryTower extends Quest
{	
	private static final String qn = "Q343_UnderTheShadowOfTheIvoryTower";

	// NPCs
	private static final int CEMA = 30834;
	private static final int ICARUS = 30835;
	private static final int MARSHA = 30934;
	private static final int TRUMPIN = 30935; 

	// Item
	private static final int NEBULITE_ORB = 4364;
	
	// Rewards
	private static final int TOWER_SHIELD = 103;
	private static final int NECKLACE_OF_MAGIC = 118;
	private static final int SAGE_BLOOD = 316;
	private static final int SQUARE_SHIELD = 630;
	private static final int SCROLL_OF_ESCAPE = 736;
	private static final int RING_OF_AGES = 885;
	private static final int NECKLACE_OF_MERMAID = 917;
	private static final int SCROLL_ENCHANT_WEAPON_C_GRADE = 951;
	private static final int SCROLL_ENCHANT_WEAPON_D_GRADE = 955;
	private static final int SPIRITSHOT_D_GRADE = 2510;
	private static final int SPIRITSHOT_C_GRADE = 2511;
	private static final int ECTOPLASM_LIQUEUR = 4365;
	
	private static final Map<Integer, IntIntHolder> DROPDATA = new HashMap<>();
	{
		DROPDATA.put(20563, new IntIntHolder(630000, 12));
		DROPDATA.put(20564, new IntIntHolder(630000, 12));
		DROPDATA.put(20565, new IntIntHolder(650000, 12));
		DROPDATA.put(20566, new IntIntHolder(680000, 13));
	}
	
	public Q343_UnderTheShadowOfTheIvoryTower()
	{
		super(343, qn, "Under the Shadow of the Ivory Tower");		
		setItemsIds(NEBULITE_ORB);
		
		addStartNpc(CEMA);
		addTalkId(CEMA, ICARUS, MARSHA, TRUMPIN);

		for (int npcId : DROPDATA.keySet())
			addKillId(npcId);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30834-05.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.set("memoState", "1");
			st.set("memoStateEx", "0");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("30834-08.htm"))
		{
			if (st.hasQuestItems(NEBULITE_ORB))
			{
				final int count = st.getQuestItemsCount(NEBULITE_ORB) * 120;
				
				st.takeItems(NEBULITE_ORB, -1);
				st.giveItems(57, count);
			}
			else
				htmltext = "30834-08a.htm";
		}
		else if (event.equalsIgnoreCase("30834-11.htm"))
		{
			st.playSound(QuestState.SOUND_GIVEUP);
			st.exitQuest(true);
		}
		else if (event.equalsIgnoreCase("30835-02.htm"))
		{
			if (st.hasQuestItems(ECTOPLASM_LIQUEUR))
			{
				htmltext = "30835-03.htm";
				st.takeItems(ECTOPLASM_LIQUEUR, 1);
				
				final int chance = Rnd.get(1000);
				if (chance < 120)
					st.giveItems(SCROLL_ENCHANT_WEAPON_D_GRADE, 1);
				else if (chance < 170)
					st.giveItems(SCROLL_ENCHANT_WEAPON_C_GRADE, 1);
				else if (chance < 330)
					st.giveItems(SPIRITSHOT_C_GRADE, Rnd.get(200) + 401);
				else if (chance < 560)
					st.giveItems(SPIRITSHOT_D_GRADE, Rnd.get(200) + 401);
				else if (chance < 562)
					st.giveItems(SAGE_BLOOD, 1);
				else if (chance < 579)
					st.giveItems(SQUARE_SHIELD, 1);
				else if (chance < 580)
					st.giveItems(NECKLACE_OF_MAGIC, 1);
				else if (chance < 582)
					st.giveItems(RING_OF_AGES, 1);
				else if (chance < 583)
					st.giveItems(TOWER_SHIELD, 1);
				else if (chance < 585)
					st.giveItems(NECKLACE_OF_MERMAID, 1);
				else
					st.giveItems(SCROLL_OF_ESCAPE, 1);
			}
		}
		else if (event.equalsIgnoreCase("30934-05.htm"))
		{
			if (st.getInt("memoState") == 1)
			{
				final int memoStateEx = st.getInt("memoStateEx");
				if (memoStateEx >= 1 && memoStateEx < 25)
				{
					if (st.getQuestItemsCount(NEBULITE_ORB) < 10)
						htmltext = "30934-06.htm";
					else
					{
						st.set("memoState", "2");
						st.takeItems(NEBULITE_ORB, 10);
						htmltext = "30934-07.htm";
					}
				}
			}
		}
		else if (event.equalsIgnoreCase("30934-08a.htm"))
		{
			if (st.getInt("memoState") == 2)
			{
				final int i0 = Rnd.get(100);
				final int i1 = Rnd.get(3);
				
				if (i0 < 20)
				{
					if (i1 == 0)
						st.set("param1", "0");
					else if (i1 == 1)
					{
						st.set("param1", "1");
						htmltext = "30934-08b.htm";
					}
					else if (i1 == 2)
					{
						st.set("param1", "2");
						htmltext = "30934-08c.htm";
					}
					st.set("memoStateEx", String.valueOf(st.getInt("memoStateEx") + 4));
				}
				else if (i0 < 50)
				{
					if (i1 == 0)
					{
						htmltext = "30934-09a.htm";
						st.set("param1", (Rnd.nextBoolean()) ? "0" : "1");
					}
					else if (i1 == 1)
					{
						htmltext = "30934-09b.htm";
						st.set("param1", (Rnd.nextBoolean()) ? "1" : "2");
					}
					else if (i1 == 2)
					{
						htmltext = "30934-09c.htm";
						st.set("param1", (Rnd.nextBoolean()) ? "2" : "0");
					}
					st.set("memoStateEx", String.valueOf(st.getInt("memoStateEx") + 4));
				}
				else
				{
					htmltext = "30934-10.htm";
					st.set("param1", String.valueOf(i1));
				}
			}
		}
		else if (event.equalsIgnoreCase("30934-11a.htm"))
		{
			if (st.getInt("memoState") == 2)
			{
				final int param1 = st.getInt("param1");
				if (param1 == 0)
				{
					st.giveItems(NEBULITE_ORB, 10);
					st.set("param1", "4");
				}
				else if (param1 == 1)
					htmltext = "30934-11b.htm";
				else if (param1 == 2)
				{
					st.giveItems(NEBULITE_ORB, 20);
					st.set("param1", "4");
					htmltext = "30934-11c.htm";
				}
				st.set("memoState", "1");
			}
		}
		else if (event.equalsIgnoreCase("30934-12a.htm"))
		{
			if (st.getInt("memoState") == 2)
			{
				final int param1 = st.getInt("param1");
				if (param1 == 0)
				{
					st.giveItems(NEBULITE_ORB, 20);
					st.set("param1", "4");
				}
				else if (param1 == 1)
				{
					st.giveItems(NEBULITE_ORB, 10);
					st.set("param1", "4");
					htmltext = "30934-12b.htm";
				}
				else if (param1 == 2)
					htmltext = "30934-12c.htm";
				
				st.set("memoState", "1");
			}
		}
		else if (event.equalsIgnoreCase("30934-13a.htm"))
		{
			if (st.getInt("memoState") == 2)
			{
				final int param1 = st.getInt("param1");
				if (param1 == 0)
					htmltext = event;
				else if (param1 == 1)
				{
					st.giveItems(NEBULITE_ORB, 20);
					st.set("param1", "4");
					htmltext = "30934-13b.htm";
				}
				else if (param1 == 2)
				{
					st.giveItems(NEBULITE_ORB, 10);
					st.set("param1", "4");
					htmltext = "30934-13c.htm";
				}
				st.set("memoState", "1");
			}
		}
		else if (event.equalsIgnoreCase("30935-03.htm"))
		{
			if (st.getQuestItemsCount(NEBULITE_ORB) >= 10)
			{
				st.set("param2", String.valueOf(Rnd.get(2)));
				htmltext = "30935-04.htm";
			}
		}
		else if (event.equalsIgnoreCase("30935-05.htm"))
		{
			if (st.getQuestItemsCount(NEBULITE_ORB) >= 10)
			{
				final int param2 = st.getInt("param2");
				if (param2 == 0)
				{
					final int param1 = st.getInt("param1");
					if (param1 == 0)
					{
						st.set("param1", "1");
						st.set("param2", "2");
					}
					else if (param1 == 1)
					{
						st.set("param1", "2");
						st.set("param2", "2");
						htmltext = "30935-05a.htm";
					}
					else if (param1 == 2)
					{
						st.set("param1", "3");
						st.set("param2", "2");
						htmltext = "30935-05b.htm";
					}
					else if (param1 == 3)
					{
						st.set("param1", "4");
						st.set("param2", "2");
						htmltext = "30935-05c.htm";
					}
					else if (param1 == 4)
					{
						st.set("param1", "0");
						st.set("param2", "2");
						st.giveItems(NEBULITE_ORB, 310);
						htmltext = "30935-05d.htm";
					}
				}
				else if (param2 == 1)
				{
					st.takeItems(NEBULITE_ORB, 10);
					st.set("param1", "0");
					st.set("param2", "2");
					htmltext = "30935-06.htm";
				}
			}
			else
				htmltext = "30935-03.htm";
		}
		else if (event.equalsIgnoreCase("30935-07.htm"))
		{
			if (st.getQuestItemsCount(NEBULITE_ORB) >= 10)
			{
				final int param2 = st.getInt("param2");
				if (param2 == 0)
				{
					st.takeItems(NEBULITE_ORB, 10);
					st.set("param1", "0");
					st.set("param2", "2");
				}
				else if (param2 == 1)
				{
					final int param1 = st.getInt("param1");
					if (param1 == 0)
					{
						st.set("param1", "1");
						st.set("param2", "2");
						htmltext = "30935-08.htm";
					}
					else if (param1 == 1)
					{
						st.set("param1", "2");
						st.set("param2", "2");
						htmltext = "30935-08a.htm";
					}
					else if (param1 == 2)
					{
						st.set("param1", "3");
						st.set("param2", "2");
						htmltext = "30935-08b.htm";
					}
					else if (param1 == 3)
					{
						st.set("param1", "4");
						st.set("param2", "2");
						htmltext = "30935-08c.htm";
					}
					else if (param1 == 4)
					{
						st.set("param1", "0");
						st.set("param2", "2");
						st.giveItems(NEBULITE_ORB, 310);
						htmltext = "30935-08d.htm";
					}
				}
			}
			else
				htmltext = "30935-03.htm";
		}
		else if (event.equalsIgnoreCase("30935-09.htm"))
		{
			final int param1 = st.getInt("param1");
			if (param1 == 1)
			{
				st.set("param1", "0");
				st.set("param2", "2");
				st.giveItems(NEBULITE_ORB, 10);
				htmltext = event;
			}
			else if (param1 == 2)
			{
				st.set("param1", "0");
				st.set("param2", "2");
				st.giveItems(NEBULITE_ORB, 30);
				htmltext = "30935-09a.htm";
			}
			else if (param1 == 3)
			{
				st.set("param1", "0");
				st.set("param2", "2");
				st.giveItems(NEBULITE_ORB, 70);
				htmltext = "30935-09b.htm";
			}
			else if (param1 == 4)
			{
				st.set("param1", "0");
				st.set("param2", "2");
				st.giveItems(NEBULITE_ORB, 150);
				htmltext = "30935-09c.htm";
			}
		}
		else if (event.equalsIgnoreCase("30935-10.htm"))
		{
			st.set("param1", "0");
			st.set("param2", "2");
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
				htmltext = (player.getClassId().getType() == ClassType.MYSTIC && player.getRace() != ClassRace.ORC) ? ((player.getLevel() < 40) ? "30834-02.htm" : "30834-03.htm") : "30834-01.htm";
				break;
			
			case STATE_STARTED:
				switch (npc.getNpcId())
				{
					case CEMA:
						htmltext = (!st.hasQuestItems(NEBULITE_ORB)) ? "30834-06.htm" : "30834-07.htm";
						break;
					
					case ICARUS:
						htmltext = "30835-01.htm";
						break;
					
					case MARSHA:
						if (st.getInt("memoStateEx") == 0)
						{
							st.set("memoStateEx", "1");
							htmltext = "30934-03.htm";
						}
						else
						{
							st.set("memoState", "1");
							htmltext = "30934-04.htm";
						}
						break;
					
					case TRUMPIN:
						st.set("param1", "0");
						htmltext = "30935-01.htm";
						break;
				}
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		final QuestState st = checkPlayerState(player, npc, STATE_STARTED);
		if (st == null)
			return null;
		
		final IntIntHolder dropdata = DROPDATA.get(npc.getNpcId());
		if (dropdata == null)
			return null;
		
		st.dropItems(NEBULITE_ORB, 1, -1, dropdata.getId());		
		st.playSound(QuestState.SOUND_ITEMGET);
		
		final int memoStateEx = st.getInt("memoStateEx");
		if (memoStateEx > 1 && Rnd.get(100) <= dropdata.getValue())
			st.set("memoStateEx", String.valueOf(memoStateEx - 1));
		
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q343_UnderTheShadowOfTheIvoryTower();
	}
}
