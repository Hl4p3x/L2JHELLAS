package com.l2jhellas.gameserver.scrips.quests;

import java.util.HashMap;
import java.util.Map;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.instancemanager.QuestManager;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q255_Tutorial extends Quest
{
	private static final String qn = "Q255_Tutorial";
		
	private static final int RECOMMENDATION_01 = 1067;
	private static final int RECOMMENDATION_02 = 1068;
	private static final int LEAF_OF_MOTHERTREE = 1069;
	private static final int BLOOD_OF_JUNDIN = 1070;
	private static final int LICENSE_OF_MINER = 1498;
	private static final int VOUCHER_OF_FLAME = 1496;
	private static final int SOULSHOT_NOVICE = 5789;
	private static final int SPIRITSHOT_NOVICE = 5790;
	private static final int BLUE_GEM = 6353;
		
	private static final String[][] QTEXMTWO =
	{
		{"0","tutorial_voice_001a","tutorial_human_fighter001.htm"},
		{"10","tutorial_voice_001b","tutorial_human_mage001.htm"},
		{"18","tutorial_voice_001c","tutorial_elven_fighter001.htm"},
		{"25","tutorial_voice_001d","tutorial_elven_mage001.htm"},
		{"31","tutorial_voice_001e","tutorial_delf_fighter001.htm"},
		{"38","tutorial_voice_001f","tutorial_delf_mage001.htm"},
		{"44","tutorial_voice_001g","tutorial_orc_fighter001.htm"},
		{"49","tutorial_voice_001h","tutorial_orc_mage001.htm"},
		{"53","tutorial_voice_001i","tutorial_dwarven_fighter001.htm"}
	};
	
	private static final String[][] CEEa =
	{
		{ "0", "tutorial_human_fighter007.htm", "-71424", "258336", "-3109" },
		{ "10", "tutorial_human_mage007.htm", "-91036", "248044", "-3568" },
		{ "18", "tutorial_elf007.htm", "46112", "41200", "-3504" },
		{ "25", "tutorial_elf007.htm", "46112", "41200", "-3504" },
		{ "31", "tutorial_delf007.htm", "28384", "11056", "-4233" },
		{ "38", "tutorial_delf007.htm", "28384", "11056", "-4233" },
		{ "44", "tutorial_orc007.htm", "-56736", "-113680", "-672" },
		{ "49", "tutorial_orc007.htm", "-56736", "-113680", "-672" },
		{ "53", "tutorial_dwarven_fighter007.htm", "108567", "-173994","-406" }
	};
	
	private static final String[][] QMCa =
	{
		{"0", "tutorial_fighter017.htm", "-83165", "242711", "-3720" },
		{ "10", "tutorial_mage017.htm", "-85247", "244718", "-3720" },
		{ "18", "tutorial_fighter017.htm", "45610", "52206", "-2792" },
		{ "25", "tutorial_mage017.htm", "45610", "52206", "-2792" },
		{ "31", "tutorial_fighter017.htm", "10344", "14445", "-4242" },
		{ "38", "tutorial_mage017.htm", "10344", "14445", "-4242" },
		{ "44", "tutorial_fighter017.htm", "-46324", "-114384", "-200" },
		{ "49", "tutorial_fighter017.htm", "-46305", "-112763", "-200" },
		{ "53", "tutorial_fighter017.htm", "115447", "-182672", "-1440" }
	};
	
	private static final Map<Integer, Talk> talks = new HashMap<>();
	{
		talks.put(30017, new Talk(0, new String[]{"30017-01.htm","30017-02.htm","30017-04.htm"}, 0, 0));
		talks.put(30008, new Talk(0, new String[]{"30008-01.htm","30008-02.htm","30008-04.htm"}, 0, 0));
		talks.put(30370, new Talk(1, new String[]{"30370-01.htm","30370-02.htm","30370-04.htm"}, 0, 0));
		talks.put(30129, new Talk(2, new String[]{"30129-01.htm","30129-02.htm","30129-04.htm"}, 0, 0));
		talks.put(30573, new Talk(3, new String[]{"30573-01.htm","30573-02.htm","30573-04.htm"}, 0, 0));
		talks.put(30528, new Talk(4, new String[]{"30528-01.htm","30528-02.htm","30528-04.htm"}, 0, 0));
		talks.put(30018, new Talk(0, new String[]{"30131-01.htm","","30019-03a.htm","30019-04.htm"}, 1, RECOMMENDATION_02));
		talks.put(30019, new Talk(0, new String[]{"30131-01.htm","","30019-03a.htm","30019-04.htm"}, 1, RECOMMENDATION_02));
		talks.put(30020, new Talk(0, new String[]{"30131-01.htm","","30019-03a.htm","30019-04.htm"}, 1, RECOMMENDATION_02));
		talks.put(30021, new Talk(0, new String[]{"30131-01.htm","","30019-03a.htm","30019-04.htm"}, 1, RECOMMENDATION_02));
		talks.put(30009, new Talk(0, new String[]{"30530-01.htm","30009-03.htm","","30009-04.htm"}, 1, RECOMMENDATION_01));
		talks.put(30011, new Talk(0, new String[]{"30530-01.htm","30009-03.htm","","30009-04.htm"}, 1, RECOMMENDATION_01));
		talks.put(30012, new Talk(0, new String[]{"30530-01.htm","30009-03.htm","","30009-04.htm"}, 1, RECOMMENDATION_01));
		talks.put(30056, new Talk(0, new String[]{"30530-01.htm","30009-03.htm","","30009-04.htm"}, 1, RECOMMENDATION_01));
		talks.put(30400, new Talk(1, new String[]{"30131-01.htm","30400-03.htm","30400-03a.htm","30400-04.htm"}, 1, LEAF_OF_MOTHERTREE));
		talks.put(30401, new Talk(1, new String[]{"30131-01.htm","30400-03.htm","30400-03a.htm","30400-04.htm"}, 1, LEAF_OF_MOTHERTREE));
		talks.put(30402, new Talk(1, new String[]{"30131-01.htm","30400-03.htm","30400-03a.htm","30400-04.htm"}, 1, LEAF_OF_MOTHERTREE));
		talks.put(30403, new Talk(1, new String[]{"30131-01.htm","30400-03.htm","30400-03a.htm","30400-04.htm"}, 1, LEAF_OF_MOTHERTREE));
		talks.put(30131, new Talk(2, new String[]{"30131-01.htm","30131-03.htm","30131-03a.htm","30131-04.htm"}, 1, BLOOD_OF_JUNDIN));
		talks.put(30404, new Talk(2, new String[]{"30131-01.htm","30131-03.htm","30131-03a.htm","30131-04.htm"}, 1, BLOOD_OF_JUNDIN));
		talks.put(30574, new Talk(3, new String[]{"30575-01.htm","30575-03.htm","30575-03a.htm","30575-04.htm"}, 1, VOUCHER_OF_FLAME));
		talks.put(30575, new Talk(3, new String[]{"30575-01.htm","30575-03.htm","30575-03a.htm","30575-04.htm"}, 1, VOUCHER_OF_FLAME));
		talks.put(30530, new Talk(4, new String[]{"30530-01.htm","30530-03.htm","","30530-04.htm"}, 1, LICENSE_OF_MINER));		
	}
	
	private static final Map<Integer, String> QMCb = new HashMap<>();
	{
		QMCb.put(0, "tutorial_human009.htm");
		QMCb.put(10, "tutorial_human009.htm");
		QMCb.put(18, "tutorial_elf009.htm");
		QMCb.put(25, "tutorial_elf009.htm");
		QMCb.put(31, "tutorial_delf009.htm");
		QMCb.put(38, "tutorial_delf009.htm");
		QMCb.put(44, "tutorial_orc009.htm");
		QMCb.put(49, "tutorial_orc009.htm");
		QMCb.put(53, "tutorial_dwarven009.htm");
	}
	
	private static final Map<Integer, String> QMCc = new HashMap<>();
	{
		QMCc.put(0, "tutorial_21.htm");
		QMCc.put(10, "tutorial_21a.htm");
		QMCc.put(18, "tutorial_21b.htm");
		QMCc.put(25, "tutorial_21c.htm");
		QMCc.put(31, "tutorial_21g.htm");
		QMCc.put(38, "tutorial_21h.htm");
		QMCc.put(44, "tutorial_21d.htm");
		QMCc.put(49, "tutorial_21e.htm");
		QMCc.put(53, "tutorial_21f.htm");
		QMCc.put(53, "tutorial_21f.htm");
	}
	
	private static final Map<Integer, String> TCLa = new HashMap<>();
	{
		TCLa.put(1, "tutorial_22w.htm");
		TCLa.put(4, "tutorial_22.htm");
		TCLa.put(7, "tutorial_22b.htm");
		TCLa.put(11, "tutorial_22c.htm");
		TCLa.put(15, "tutorial_22d.htm");
		TCLa.put(19, "tutorial_22e.htm");
		TCLa.put(22, "tutorial_22f.htm");
		TCLa.put(26, "tutorial_22g.htm");
		TCLa.put(29, "tutorial_22h.htm");
		TCLa.put(32, "tutorial_22n.htm");
		TCLa.put(35, "tutorial_22o.htm");
		TCLa.put(39, "tutorial_22p.htm");
		TCLa.put(42, "tutorial_22q.htm");
		TCLa.put(45, "tutorial_22i.htm");
		TCLa.put(47, "tutorial_22j.htm");
		TCLa.put(50, "tutorial_22k.htm");
		TCLa.put(54, "tutorial_22l.htm");
		TCLa.put(56, "tutorial_22m.htm");
	}
	
	private static final Map<Integer, String> TCLb = new HashMap<>();
	{
		TCLb.put(4, "tutorial_22aa.htm");
		TCLb.put(7, "tutorial_22ba.htm");
		TCLb.put(11, "tutorial_22ca.htm");
		TCLb.put(15, "tutorial_22da.htm");
		TCLb.put(19, "tutorial_22ea.htm");
		TCLb.put(22, "tutorial_22fa.htm");
		TCLb.put(26, "tutorial_22ga.htm");
		TCLb.put(32, "tutorial_22na.htm");
		TCLb.put(35, "tutorial_22oa.htm");
		TCLb.put(39, "tutorial_22pa.htm");
		TCLb.put(50, "tutorial_22ka.htm");
	}
	
	private static final Map<Integer, String> TCLc = new HashMap<>();
	{
		TCLc.put(4, "tutorial_22ab.htm");
		TCLc.put(7, "tutorial_22bb.htm");
		TCLc.put(11, "tutorial_22cb.htm");
		TCLc.put(15, "tutorial_22db.htm");
		TCLc.put(19, "tutorial_22eb.htm");
		TCLc.put(22, "tutorial_22fb.htm");
		TCLc.put(26, "tutorial_22gb.htm");
		TCLc.put(32, "tutorial_22nb.htm");
		TCLc.put(35, "tutorial_22ob.htm");
		TCLc.put(39, "tutorial_22pb.htm");
		TCLc.put(50, "tutorial_22kb.htm");
	}
	
	private static final Map<String, Event> events = new HashMap<>();
	{
		events.put("30017_02", new Event("30017-03.htm", 0, 0, 0, RECOMMENDATION_02, 10, SPIRITSHOT_NOVICE, 100, 0, 0, 0));
		events.put("30017_04", new Event("30017-04.htm", -84058, 243239, -3730, 0, 10, 0, 0, 0, 0, 0));
		events.put("30370_02", new Event("30370-03.htm", 0, 0, 0, LEAF_OF_MOTHERTREE, 25, SPIRITSHOT_NOVICE, 100, 18, SOULSHOT_NOVICE, 200));
		events.put("30370_04", new Event("30370-04.htm", 45491, 48359, -3086, 0, 25, 0, 0, 18, 0, 0));
		events.put("30129_02", new Event("30129-03.htm", 0, 0, 0, BLOOD_OF_JUNDIN, 38, SPIRITSHOT_NOVICE, 100, 31, SOULSHOT_NOVICE, 200));
		events.put("30129_04", new Event("30129-04.htm", 12116, 16666, -4610, 0, 38, 0, 0, 31, 0, 0));
		events.put("30528_02", new Event("30528-03.htm", 0, 0, 0, LICENSE_OF_MINER, 53, SOULSHOT_NOVICE, 200, 0, 0, 0));
		events.put("30528_04", new Event("30528-04.htm", 115642, -178046, -941, 0, 53, 0, 0, 0, 0, 0));
		events.put("30573_02", new Event("30573-03.htm", 0, 0, 0, VOUCHER_OF_FLAME, 49, SPIRITSHOT_NOVICE, 100, 44, SOULSHOT_NOVICE, 200));
		events.put("30573_04", new Event("30573-04.htm", -45067, -113549, -235, 0, 49, 0, 0, 44, 0, 0));
	}
	
	public Q255_Tutorial()
	{
		super(255, qn, "Q255_Tutorial");
		
		addStartNpc(30009, 30017, 30019, 30129, 30131, 30573, 30575, 30370, 30528, 30530, 30400, 30401, 30402, 30403, 30404);
		addTalkId(30009, 30017, 30019, 30129, 30131, 30573, 30575, 30370, 30528, 30530, 30400, 30401, 30402, 30403, 30404);
		addFirstTalkId(30009, 30017, 30019, 30129, 30131, 30573, 30575, 30370, 30528, 30530, 30400, 30401, 30402, 30403, 30404);
		addKillId(18342, 20001);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (player == null)
			return null;
		
		final QuestState qs = player.getQuestState(qn);
		
		if (qs == null)
			return event;

		String html = "";
		int Ex = qs.getInt("Ex");
		final int classId = player.getClassId().getId();

		if (event.startsWith("UC"))
		{
			if ((player.getLevel() < 6) && (qs.getInt("onlyone") == 0))
			{
				switch (qs.getInt("ucMemo"))
				{
					case 0:
					{
						qs.set("ucMemo", "0");
						startQuestTimer("QT", 10000, null, player, false);
						qs.set("Ex", "-2");
						break;
					}
					case 1:
					{
						qs.showQuestionMark(1);
						qs.playTutorialVoice("tutorial_voice_006");
						qs.playSound("ItemSound.quest_tutorial");
						break;
					}
					case 2:
					{
						if (Ex == 2)
							qs.showQuestionMark(3);				
						else if (qs.getQuestItemsCount(6353) > 0)
							qs.showQuestionMark(5);
						
						qs.playSound("ItemSound.quest_tutorial");
						break;
					}
					case 3:
					{
						qs.showQuestionMark(12);
						qs.playSound("ItemSound.quest_tutorial");
						qs.onTutorialClientEvent(0);
						break;
					}
				}
			}
		}
		else if (event.startsWith("QT"))
		{
			if (Ex == -2)
			{
				String voice = "";
				for (String[] element : QTEXMTWO)
				{
					if (classId == Integer.valueOf(element[0]))
					{
						voice = element[1];
						html = element[2];
					}
				}
				qs.playTutorialVoice(voice);
				qs.set("Ex", "-3");
				cancelQuestTimers("QT");
				startQuestTimer("QT", 30000, null, player, false);
			}
			else if (Ex == -3)
			{
				qs.playTutorialVoice("tutorial_voice_002");
				qs.set("Ex", "0");
			}
			else if (Ex == -4)
			{
				qs.playTutorialVoice("tutorial_voice_008");
				qs.set("Ex", "-5");
			}
		}
		else if (event.startsWith("TE"))
		{
			cancelQuestTimers("TE");
			if (!event.equalsIgnoreCase("TE"))
			{
				switch (Integer.valueOf(event.substring(2)))
				{
					case 0:
					{
						qs.closeTutorialHtml();
						break;
					}
					case 1:
					{
						qs.closeTutorialHtml();
						qs.playTutorialVoice("tutorial_voice_006");
						qs.showQuestionMark(1);
						qs.playSound("ItemSound.quest_tutorial");
						startQuestTimer("QT", 30000, null, player, false);
						qs.set("Ex", "-4");
						break;
					}
					case 2:
					{
						qs.playTutorialVoice("tutorial_voice_003");
						html = "tutorial_02.htm";
						qs.onTutorialClientEvent(1);
						qs.set("Ex", "-5");
						break;
					}
					case 3:
					{
						html = "tutorial_03.htm";
						qs.onTutorialClientEvent(2);
						break;
					}
					case 5:
					{
						html = "tutorial_05.htm";
						qs.onTutorialClientEvent(8);
						break;
					}
					case 7:
					{
						html = "tutorial_100.htm";
						qs.onTutorialClientEvent(0);
						break;
					}
					case 8:
					{
						html = "tutorial_101.htm";
						qs.onTutorialClientEvent(0);
						break;
					}
					case 10:
					{
						html = "tutorial_103.htm";
						qs.onTutorialClientEvent(0);
						break;
					}
					case 12:
					{
						qs.closeTutorialHtml();
						break;
					}
					case 23:
					{
						if (TCLb.containsKey(classId))
							html = TCLb.get(classId);
						break;
					}
					case 24:
					{
						if (TCLc.containsKey(classId))
							html = TCLc.get(classId);
						break;
					}
					case 25:
					{
						html = "tutorial_22cc.htm";
						break;
					}
					case 26:
					{
						if (TCLa.containsKey(classId))
							html = TCLa.get(classId);
						break;
					}
					case 27:
					{
						html = "tutorial_29.htm";
						break;
					}
					case 28:
					{
						html = "tutorial_28.htm";
						break;
					}
				}
			}
		}
		else if (event.startsWith("CE"))
		{
			final int event_id = Integer.valueOf(event.substring(2));
			if ((event_id == 1) && (player.getLevel() < 6))
			{
				qs.playTutorialVoice("tutorial_voice_004");
				html = "tutorial_03.htm";
				qs.playSound("ItemSound.quest_tutorial");
				qs.onTutorialClientEvent(2);
			}
			else if ((event_id == 2) && (player.getLevel() < 6))
			{
				qs.playTutorialVoice("tutorial_voice_005");
				html = "tutorial_05.htm";
				qs.playSound("ItemSound.quest_tutorial");
				qs.onTutorialClientEvent(8);
			}
			else if ((event_id == 8) && (player.getLevel() < 6))
			{
				int x = 0;
				int y = 0;
				int z = 0;
				for (String[] element : CEEa)
				{
					if (classId == Integer.valueOf(element[0]))
					{
						html = element[1];
						x = Integer.valueOf(element[2]);
						y = Integer.valueOf(element[3]);
						z = Integer.valueOf(element[4]);
					}
				}
				if (x != 0)
				{
					qs.playSound("ItemSound.quest_tutorial");
					qs.addRadar(x, y, z);
					qs.playTutorialVoice("tutorial_voice_007");
					qs.set("ucMemo", "1");
					qs.set("Ex", "-5");
				}
			}
			else if ((event_id == 30) && (player.getLevel() < 10) && (qs.getInt("Die") == 0))
			{
				qs.playTutorialVoice("tutorial_voice_016");
				qs.playSound("ItemSound.quest_tutorial");
				qs.set("Die", "1");
				qs.showQuestionMark(8);
				qs.onTutorialClientEvent(0);
			}
			else if ((event_id == 800000) && (player.getLevel() < 6) && (qs.getInt("sit") == 0))
			{
				qs.playTutorialVoice("tutorial_voice_018");
				qs.playSound("ItemSound.quest_tutorial");
				qs.set("sit", "1");
				qs.onTutorialClientEvent(0);
				html = "tutorial_21z.htm";
			}
			else if (event_id == 40)
			{
				switch (player.getLevel())
				{
					case 5:
					{
						if (((qs.getInt("lvl") < 5) && !player.isMageClass()) || (classId == 49))
						{
							qs.playTutorialVoice("tutorial_voice_014");
							qs.showQuestionMark(9);
							qs.playSound("ItemSound.quest_tutorial");
							qs.set("lvl", "5");
						}
						break;
					}
					case 6:
					{
						if ((qs.getInt("lvl") < 6) && (player.getClassId().level() == 0))
						{
							qs.playTutorialVoice("tutorial_voice_020");
							qs.playSound("ItemSound.quest_tutorial");
							qs.showQuestionMark(24);
							qs.set("lvl", "6");
						}
						break;
					}
					case 7:
					{
						if ((qs.getInt("lvl") < 7) && player.isMageClass() && (classId != 49) && (player.getClassId().level() == 0))
						{
							qs.playTutorialVoice("tutorial_voice_019");
							qs.playSound("ItemSound.quest_tutorial");
							qs.set("lvl", "7");
							qs.showQuestionMark(11);
						}
						break;
					}
					case 15:
					{
						if (qs.getInt("lvl") < 15)
						{
							qs.playSound("ItemSound.quest_tutorial");
							qs.set("lvl", "15");
							qs.showQuestionMark(33);
						}
						break;
					}
					case 19:
					{
						if ((qs.getInt("lvl") < 19) && (player.getClassId().level() == 0))
						{
							switch (classId)
							{
								case 0:
								case 10:
								case 18:
								case 25:
								case 31:
								case 38:
								case 44:
								case 49:
								case 52:
								{
									qs.playSound("ItemSound.quest_tutorial");
									qs.set("lvl", "19");
									qs.showQuestionMark(35);
									break;
								}
							}
						}
						break;
					}
					case 35:
					{
						if ((qs.getInt("lvl") < 35) && (player.getClassId().level() == 1))
						{
							switch (classId)
							{
								case 1:
								case 4:
								case 7:
								case 11:
								case 15:
								case 19:
								case 22:
								case 26:
								case 29:
								case 32:
								case 35:
								case 39:
								case 42:
								case 45:
								case 47:
								case 50:
								case 54:
								case 56:
								{
									qs.playSound("ItemSound.quest_tutorial");
									qs.set("lvl", "35");
									qs.showQuestionMark(34);
									break;
								}
							}
						}
						break;
					}
				}
			}
			else if ((event_id == 45) && (player.getLevel() < 10) && (qs.getInt("HP") == 0))
			{
				qs.playTutorialVoice("tutorial_voice_017");
				qs.playSound("ItemSound.quest_tutorial");
				qs.set("HP", "1");
				qs.showQuestionMark(10);
				qs.onTutorialClientEvent(800000);
			}
			else if ((event_id == 57) && (player.getLevel() < 6) && (qs.getInt("Adena") == 0))
			{
				qs.playTutorialVoice("tutorial_voice_012");
				qs.playSound("ItemSound.quest_tutorial");
				qs.set("Adena", "1");
				qs.showQuestionMark(23);
			}
			else if ((event_id == 6353) && (player.getLevel() < 6) && (qs.getInt("Gemstone") == 0))
			{
				qs.playTutorialVoice("tutorial_voice_013");
				qs.playSound("ItemSound.quest_tutorial");
				qs.set("Gemstone", "1");
				qs.showQuestionMark(5);
			}
			else if ((event_id == 1048576) && (player.getLevel() < 6))
			{
				qs.showQuestionMark(5);
				qs.playTutorialVoice("tutorial_voice_013");
				qs.playSound("ItemSound.quest_tutorial");
			}
		}
		else if (event.startsWith("QM"))
		{
			int x = 0;
			int y = 0;
			int z = 0;
			switch (Integer.valueOf(event.substring(2)))
			{
				case 1:
				{
					qs.playTutorialVoice("tutorial_voice_007");
					qs.set("Ex", "-5");
					for (String[] element : CEEa)
					{
						if (classId == Integer.valueOf(element[0]))
						{
							html = element[1];
							x = Integer.valueOf(element[2]);
							y = Integer.valueOf(element[3]);
							z = Integer.valueOf(element[4]);
						}
					}
					qs.addRadar(x, y, z);
					break;
				}
				case 3:
				{
					html = "tutorial_09.htm";
					qs.onTutorialClientEvent(1048576);
					break;
				}
				case 5:
				{
					for (String[] element : CEEa)
					{
						if (classId == Integer.valueOf(element[0]))
						{
							html = element[1];
							x = Integer.valueOf(element[2]);
							y = Integer.valueOf(element[3]);
							z = Integer.valueOf(element[4]);
						}
					}
					qs.addRadar(x, y, z);
					html = "tutorial_11.htm";
					break;
				}
				case 7:
				{
					html = "tutorial_15.htm";
					qs.set("ucMemo", "3");
					break;
				}
				case 8:
				{
					html = "tutorial_18.htm";
					break;
				}
				case 9:
				{
					for (String[] element : QMCa)
					{
						if (classId == Integer.valueOf(element[0]))
						{
							html = element[1];
							x = Integer.valueOf(element[2]);
							y = Integer.valueOf(element[3]);
							z = Integer.valueOf(element[4]);
						}
					}
					if (x != 0)
						qs.addRadar(x, y, z);
					break;
				}
				case 10:
				{
					html = "tutorial_19.htm";
					break;
				}
				case 11:
				{
					for (String[] element : QMCa)
					{
						if (classId == Integer.valueOf(element[0]))
						{
							html = element[1];
							x = Integer.valueOf(element[2]);
							y = Integer.valueOf(element[3]);
							z = Integer.valueOf(element[4]);
						}
					}
					if (x != 0)
						qs.addRadar(x, y, z);
					break;
				}
				case 12:
				{
					html = "tutorial_15.htm";
					qs.set("ucMemo", "4");
					break;
				}
				case 17:
				{
					html = "tutorial_30.htm";
					break;
				}
				case 23:
				{
					html = "tutorial_24.htm";
					break;
				}
				case 24:
				{
					if (QMCb.containsKey(classId))
						html = QMCb.get(classId);
					break;
				}
				case 26:
				{
					html = player.isMageClass() && (classId != 49) ? "tutorial_newbie004b.htm" : "tutorial_newbie004a.htm";
					break;
				}
				case 33:
				{
					html = "tutorial_27.htm";
					break;
				}
				case 34:
				{
					html = "tutorial_28.htm";
					break;
				}
				case 35:
				{
					if (QMCc.containsKey(classId))
						html = QMCc.get(classId);
					break;
				}
			}
		}

		boolean isMage = player.isMageClass();
		
		if (event.equalsIgnoreCase("TimerEx_NewbieHelper"))
		{
			if (Ex == 0)
			{
				if (isMage)
					qs.playTutorialVoice("tutorial_voice_009b");
				else
					qs.playTutorialVoice("tutorial_voice_009a");
				
				qs.set("Ex", "1");
			}
			else if (Ex == 3)
			{
				qs.playTutorialVoice("tutorial_voice_010a");
				qs.set("Ex", "4");
			}
			return null;
		}
		
		if (event.equalsIgnoreCase("TimerEx_GrandMaster"))
		{
			if (Ex >= 4)
			{
				qs.showQuestionMark(7);
				qs.playSound("ItemSound.quest_tutorial");
				qs.playTutorialVoice("tutorial_voice_025");
			}
			return null;
		}
		
		Event e = events.get(event);
		
		if(e != null)
		{
			html = e.htm;

			if (qs.getQuestItemsCount(e.item) > 0 && qs.getInt("onlyone") == 0)
			{
				qs.rewardExpAndSp(0, 50);
				qs.getQuest().startQuestTimer("TimerEx_GrandMaster", 60000, null, null, true);
				qs.takeItems(e.item, 1);

				if (Ex <= 3)
					qs.set("Ex", "4");

				if (classId == e.classId1)
				{
					qs.giveItems(e.gift1, e.count1);

					if (e.gift1 == SPIRITSHOT_NOVICE)
						qs.playTutorialVoice("tutorial_voice_027");
					else
						qs.playTutorialVoice("tutorial_voice_026");
				}
				else if (classId == e.classId2 && e.gift2 != 0)
				{
					qs.giveItems(e.gift2, e.count2);
					qs.playTutorialVoice("tutorial_voice_026");
				}
				qs.set("step", "3");
				qs.set("onlyone", "1");
			}

			if (e.radarX != 0)
				qs.addRadar(e.radarX, e.radarY, e.radarZ);			
		}
				
		if (html.isEmpty())
			return null;

		qs.showTutorialHTML(html);
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg();
		
		QuestState st = player.getQuestState(qn);
		
		if (st == null)
			st = newQuestState(player);
		
		final QuestState qs = player.getQuestState("Q255_Tutorial");
		
		if ((qs == null) || qs.isCompleted())
		{
			npc.showChatWindow(player);
			return null;
		}
		
		int Ex = qs.getInt("Ex");
		int npcId = npc.getNpcId();
		int step = st.getInt("step");
		int onlyone = st.getInt("onlyone");
		int level = player.getLevel();
		boolean isMage = player.isMageClass();
		Talk t = talks.get(npcId);
		
		if (t == null)
			return htmltext;
		
		if ((level >= 10 || onlyone == 1) && t.npcTyp == 1)
			htmltext = "30575-05.htm";
		else if (onlyone == 0 && level < 10)
		{
			if (player.getRace().ordinal() == t.raceId)
				htmltext = t.htmlfiles[0];
			
			if (t.npcTyp == 1)
			{
				if (step == 0 && Ex < 0)
				{
					qs.set("Ex", "0");
					st.getQuest().startQuestTimer("TimerEx_NewbieHelper", 30000, null, null, true);
					
					if (isMage)
						st.set("step", "1");
					else
					{
						htmltext = "30530-01.htm";
						st.set("step", "1");
					}
				}
				else if (step == 1 && st.getQuestItemsCount(t.item) == 0 && Ex <= 2)
				{
					if (st.getQuestItemsCount(BLUE_GEM) > 0)
					{
						st.takeItems(BLUE_GEM, st.getQuestItemsCount(BLUE_GEM));
						st.set("step", "2");
						qs.set("Ex", "3");
						st.getQuest().startQuestTimer("TimerEx_NewbieHelper", 30000, null, null, true);
						qs.set("ucMemo", "3");
						
						if (isMage)
						{
							st.playTutorialVoice("tutorial_voice_027");
							st.giveItems(SPIRITSHOT_NOVICE, 100);
							htmltext = t.htmlfiles[2];
							
							if (htmltext.isEmpty())
								htmltext = "<html><body>" + (npc.getTitle().isEmpty() ? "" : new StringBuilder().append(npc.getTitle()).append(" ").toString()) + npc.getName() + "<br>I am sorry. I only help warriors. Please go to another Newbie Helper who may assist you.</body></html>";
						}
						else
						{
							st.playTutorialVoice("tutorial_voice_026");
							st.giveItems(SOULSHOT_NOVICE, 200);
							htmltext = t.htmlfiles[1];
							
							if (htmltext.isEmpty())
								htmltext = "<html><body>" + (npc.getTitle().isEmpty() ? "" : new StringBuilder().append(npc.getTitle()).append(" ").toString()) + npc.getName() + "<br>I am sorry. I only help mystics. Please go to another Newbie Helper who may assist you.</body></html>";
						}	
						qs.setState(STATE_COMPLETED);
					}
					else if (isMage)
					{
						htmltext = "30131-02.htm";
						
						if (player.getRace().ordinal() == 3)
							htmltext = "30575-02.htm";
					}
					else
						htmltext = "30530-02.htm";
				}
				else if (step == 2)
					htmltext = t.htmlfiles[3];
			}
			else if (t.npcTyp == 0)
			{
				if (step == 1)
					htmltext = t.htmlfiles[0];
				else if (step == 2)
					htmltext = t.htmlfiles[1];
				else if (step == 3)
					htmltext = t.htmlfiles[2];
			}
		}
		return htmltext;
	}
			
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		
		final QuestState st = player.getQuestState(qn);
		
		if (st == null)
			return null;
		
		final QuestState qs = player.getQuestState("Q255_Tutorial");
		
		if (qs == null)
			return null;
		
		final int Ex = qs.getInt("Ex");
		
		if (Ex <= 1)
		{
			st.playTutorialVoice("tutorial_voice_011");
			st.showQuestionMark(3);
			qs.set("Ex", "2");
		}
		
		if (Ex <= 2 && st.getQuestItemsCount(BLUE_GEM) < 1)
			ThreadPoolManager.getInstance().scheduleGeneral(new BlueGemDrop(npc, st), 2500);
		
		return null;
	}
	
	@Override
	public String onEnterWorld(L2PcInstance player)
	{
		if (player.getLevel() < 6)
		{
			Quest q = QuestManager.getInstance().getQuest(qn);
			
			if (q != null)
				player.processQuestEvent(q.getName(), "CE45");
		}
		return "";
	}
	
	
	private static class Talk
	{
		public int raceId;
		public String[] htmlfiles;
		public int npcTyp;
		public int item;
		
		public Talk(int raceId, String[] htmlfiles, int npcTyp, int item)
		{
			this.raceId = raceId;
			this.htmlfiles = htmlfiles;
			this.npcTyp = npcTyp;
			this.item = item;
		}
	}
	
	public static class BlueGemDrop implements Runnable
	{
		private final L2Npc _npc;
		private final QuestState _st;
		
		public BlueGemDrop(L2Npc npc, QuestState st)
		{
			_npc = npc;
			_st = st;
		}
		
		@Override
		public void run()
		{
			if (_st != null && _npc != null)
			{
				((L2MonsterInstance) _npc).dropItem(_st.getPlayer(), BLUE_GEM, 1);
				_st.playSound("ItemSound.quest_tutorial");
			}
		}
	}
	
	private static class Event
	{
		public String htm;
		public int radarX;
		public int radarY;
		public int radarZ;
		public int item;
		public int classId1;
		public int gift1;
		public int count1;
		public int classId2;
		public int gift2;
		public int count2;
		
		public Event(String htm, int radarX, int radarY, int radarZ, int item, int classId1, int gift1, int count1, int classId2, int gift2, int count2)
		{
			this.htm = htm;
			this.radarX = radarX;
			this.radarY = radarY;
			this.radarZ = radarZ;
			this.item = item;
			this.classId1 = classId1;
			this.gift1 = gift1;
			this.count1 = count1;
			this.classId2 = classId2;
			this.gift2 = gift2;
			this.count2 = count2;
		}
	}
	
	public static void main(String[] args)
	{
		new Q255_Tutorial();
	}
}