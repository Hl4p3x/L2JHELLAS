package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Rnd;

public class Q373_SupplierOfReagents extends Quest
{
	private static final String qn = "Q373_SupplierOfReagents";
	
	// Variables
	private static final String _ingredient = "ingredient";
	private static final String _catalyst = "catalyst";
	
	// NPCs
	private static final int WESLEY = 30166;
	private static final int URN = 31149;
	
	// Monsters
	private static final int CRENDION = 20813;
	private static final int HALLATES_MAID = 20822;
	private static final int HALLATES_GUARDIAN = 21061;
	private static final int PLATINUM_TRIBE_SHAMAN = 20828;
	private static final int PLATINUM_GUARDIAN_SHAMAN = 21066;
	private static final int LAVA_WYRM = 21111;
	private static final int HAMES_ORC_SHAMAN = 21115;
	
	// Quest items
	private static final int MIXING_STONE = 5904;
	private static final int MIXING_MANUAL = 6317;
	
	// Items - pouches
	private static final int REAGENT_POUCH_1 = 6007;
	private static final int REAGENT_POUCH_2 = 6008;
	private static final int REAGENT_POUCH_3 = 6009;
	private static final int REAGENT_BOX = 6010;
	// Items - ingredients
	private static final int WYRMS_BLOOD = 6011;
	private static final int LAVA_STONE = 6012;
	private static final int MOONSTONE_SHARD = 6013;
	private static final int ROTTEN_BONE = 6014;
	private static final int DEMONS_BLOOD = 6015;
	private static final int INFERNIUM_ORE = 6016;
	// Items - catalysts
	private static final int BLOOD_ROOT = 6017;
	private static final int VOLCANIC_ASH = 6018;
	private static final int QUICKSILVER = 6019;
	private static final int SULFUR = 6020;
	private static final int DEMONIC_ESSENCE = 6031;
	private static final int MIDNIGHT_OIL = 6030;
	// Items - products
	private static final int DRACOPLASM = 6021;
	private static final int MAGMA_DUST = 6022;
	private static final int MOON_DUST = 6023;
	private static final int NECROPLASM = 6024;
	private static final int DEMONPLASM = 6025;
	private static final int INFERNO_DUST = 6026;
	private static final int FIRE_ESSENCE = 6028;
	private static final int LUNARGENT = 6029;
	// Items - products final
	private static final int DRACONIC_ESSENCE = 6027;
	private static final int ABYSS_OIL = 6032;
	private static final int HELLFIRE_OIL = 6033;
	private static final int NIGHTMARE_OIL = 6034;
	private static final int PURE_SILVER = 6320;
	
	private static final int[][] DROPLISTS =
	{
		{
			HAMES_ORC_SHAMAN,
			REAGENT_POUCH_3,
			470
		},
		{
			HALLATES_MAID,
			REAGENT_POUCH_1,
			664
		},
		{
			HALLATES_MAID,
			VOLCANIC_ASH,
			180
		},
		{
			HALLATES_GUARDIAN,
			DEMONS_BLOOD,
			729
		},
		{
			HALLATES_GUARDIAN,
			MOONSTONE_SHARD,
			104
		},
		{
			PLATINUM_GUARDIAN_SHAMAN,
			REAGENT_BOX,
			442
		},
		{
			PLATINUM_TRIBE_SHAMAN,
			REAGENT_POUCH_2,
			680
		},
		{
			PLATINUM_TRIBE_SHAMAN,
			QUICKSILVER,
			320
		},
		{
			CRENDION,
			QUICKSILVER,
			618
		},
		{
			CRENDION,
			ROTTEN_BONE,
			382
		},
		{
			LAVA_WYRM,
			WYRMS_BLOOD,
			505
		},
		{
			LAVA_WYRM,
			LAVA_STONE,
			245
		},
	};
	
	private static final int[][] FORMULAS =
	{
		{
			10,
			WYRMS_BLOOD,
			BLOOD_ROOT,
			DRACOPLASM
		},
		{
			10,
			LAVA_STONE,
			VOLCANIC_ASH,
			MAGMA_DUST
		},
		{
			10,
			MOONSTONE_SHARD,
			VOLCANIC_ASH,
			MOON_DUST
		},
		{
			10,
			ROTTEN_BONE,
			BLOOD_ROOT,
			NECROPLASM
		},
		{
			10,
			DEMONS_BLOOD,
			BLOOD_ROOT,
			DEMONPLASM
		},
		{
			10,
			INFERNIUM_ORE,
			VOLCANIC_ASH,
			INFERNO_DUST
		},
		{
			10,
			DRACOPLASM,
			QUICKSILVER,
			DRACONIC_ESSENCE
		},
		{
			10,
			MAGMA_DUST,
			SULFUR,
			FIRE_ESSENCE
		},
		{
			10,
			MOON_DUST,
			QUICKSILVER,
			LUNARGENT
		},
		{
			10,
			NECROPLASM,
			QUICKSILVER,
			MIDNIGHT_OIL
		},
		{
			10,
			DEMONPLASM,
			SULFUR,
			DEMONIC_ESSENCE
		},
		{
			10,
			INFERNO_DUST,
			SULFUR,
			ABYSS_OIL
		},
		{
			1,
			FIRE_ESSENCE,
			DEMONIC_ESSENCE,
			HELLFIRE_OIL
		},
		{
			1,
			LUNARGENT,
			MIDNIGHT_OIL,
			NIGHTMARE_OIL
		},
		{
			1,
			LUNARGENT,
			QUICKSILVER,
			PURE_SILVER
		}
	};
	
	private static final int[][] TEMPERATURES =
	{
		{
			1,
			100,
			1
		},
		{
			2,
			45,
			3
		},
		{
			3,
			15,
			5
		}
	};
	
	public Q373_SupplierOfReagents()
	{
		super(373, qn, "Supplier of Reagents");
		
		setItemsIds(MIXING_STONE, MIXING_MANUAL);
		
		addStartNpc(WESLEY);
		addTalkId(WESLEY, URN);
		
		addKillId(CRENDION, HALLATES_MAID, HALLATES_GUARDIAN, PLATINUM_TRIBE_SHAMAN, PLATINUM_GUARDIAN_SHAMAN, LAVA_WYRM, HAMES_ORC_SHAMAN);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		// Wesley
		if (event.equalsIgnoreCase("30166-04.htm"))
		{
			st.set("cond", "1");
			st.setState(STATE_STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
			
			st.giveItems(MIXING_STONE, 1);
			st.giveItems(MIXING_MANUAL, 1);
		}
		else if (event.equalsIgnoreCase("30166-09.htm"))
		{
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(true);
		}
		// Urn
		else if (event.equalsIgnoreCase("31149-02.htm"))
		{
			if (!st.hasQuestItems(MIXING_STONE))
				htmltext = "31149-04.htm";
		}
		else if (event.startsWith("31149-03-"))
		{
			int regentId = Integer.parseInt(event.substring(9, 13));
			for (int[] formula : FORMULAS)
			{
				if (formula[1] != regentId)
					continue;
				
				// Not enough items, cancel the operation.
				if (st.getQuestItemsCount(regentId) < formula[0])
					break;
				
				st.set(_ingredient, Integer.toString(regentId));
				return htmltext;
			}
			htmltext = "31149-04.htm";
		}
		else if (event.startsWith("31149-06-"))
		{
			int catalyst = Integer.parseInt(event.substring(9, 13));
			
			// Not enough items, cancel the operation.
			if (!st.hasQuestItems(catalyst))
				return "31149-04.htm";
			
			st.set(_catalyst, Integer.toString(catalyst));
		}
		else if (event.startsWith("31149-12-"))
		{
			int regent = st.getInt(_ingredient);
			int catalyst = st.getInt(_catalyst);
			
			for (int[] formula : FORMULAS)
			{
				if (formula[1] != regent || formula[2] != catalyst)
					continue;
				
				// Not enough regents.
				if (st.getQuestItemsCount(regent) < formula[0])
					break;
				
				// Not enough catalysts.
				if (!st.hasQuestItems(catalyst))
					break;
				
				st.takeItems(regent, formula[0]);
				st.takeItems(catalyst, 1);
				
				int tempIndex = Integer.parseInt(event.substring(9, 10));
				for (int temperature[] : TEMPERATURES)
				{
					if (temperature[0] != tempIndex)
						continue;
					
					if (Rnd.get(100) < temperature[1])
					{
						st.giveItems(formula[3], temperature[2]);
						return "31149-12-" + formula[3] + ".htm";
					}
					return "31149-11.htm";
				}
			}
			htmltext = "31149-13.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = Quest.getNoQuestMsg();
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case STATE_CREATED:
				if (player.getLevel() < 57)
				{
					htmltext = "30166-01.htm";
					st.exitQuest(true);
				}
				else
					htmltext = "30166-02.htm";
				break;
			
			case STATE_STARTED:
				if (npc.getNpcId() == WESLEY)
					htmltext = "30166-05.htm";
				else
					htmltext = "31149-01.htm";
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		L2PcInstance partyMember = getRandomPartyMemberState(player, npc, STATE_STARTED);
		if (partyMember == null)
			return null;
		
		QuestState st = partyMember.getQuestState(qn);
		
		int npcId = npc.getNpcId();
		int chance = Rnd.get(1000);
		for (int droplist[] : DROPLISTS)
		{
			// check mob, go to next mob
			if (npcId != droplist[0])
				continue;
			
			// check chance, go to next item if exists
			chance -= droplist[2];
			if (chance >= 0)
				continue;
			
			st.giveItems(droplist[1], 1);
			st.playSound(QuestState.SOUND_ITEMGET);
			break;
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q373_SupplierOfReagents();
	}
}