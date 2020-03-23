package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.enums.sound.Sound;
import com.l2jhellas.gameserver.holder.SkillHolder;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;


public final class Q120_PavelsLastResearch extends Quest
{
	private static final String qn = "Q120_PavelsLastResearch";

	//npcs
	private static final int SUSPICIOUS_LOOKING_PILE_OF_STONES = 32046;
	private static final int WENDY = 32047;
	private static final int YUMI = 32041;
	private static final int WEATHERMASTER_1 = 32042;
	private static final int WEATHERMASTER_2 = 32043;
	private static final int WEATHERMASTER_3 = 32044;
	private static final int DOCTOR_CHAOS_SECRET_BOOKSHELF = 32045;
	//items
	private static final int FLOWER_OF_PAVEL = 8290;
	private static final int HEART_OF_ATLANTA = 8291;
	private static final int WENDYS_NECKLACE = 8292;
	private static final int LOCKUP_RESEARCH_REPORT = 8058;
	private static final int RESEARCH_REPORT = 8059;
	private static final int KEY_OF_ENIGMA = 8060;
	//skills
	private static final SkillHolder QUEST_TRAP_POWER_SHOT = new SkillHolder(5073, 5);
	private static final SkillHolder NPC_DEFAULT = new SkillHolder(7000);
	//rewards
	private static final int SEALED_PHOENIX_EARRING = 6324;
	
	public Q120_PavelsLastResearch()
	{
		super(120, qn, "Pavel's Last Research");
		addStartNpc(SUSPICIOUS_LOOKING_PILE_OF_STONES);
		addTalkId(SUSPICIOUS_LOOKING_PILE_OF_STONES, WENDY, YUMI, WEATHERMASTER_1, WEATHERMASTER_2, WEATHERMASTER_3, DOCTOR_CHAOS_SECRET_BOOKSHELF);
		addSkillSeeId(WEATHERMASTER_1, WEATHERMASTER_2, WEATHERMASTER_3);
		setItemsIds(FLOWER_OF_PAVEL, HEART_OF_ATLANTA, WENDYS_NECKLACE, LOCKUP_RESEARCH_REPORT, RESEARCH_REPORT, KEY_OF_ENIGMA);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = player.getQuestState(qn);
		String html = getNoQuestMsg();
		
		switch (npc.getNpcId())
		{
			case SUSPICIOUS_LOOKING_PILE_OF_STONES:
			{
				if (qs.isCreated())
				{
					if (player.hasQuestCompleted(Q114_ResurrectionOfAnOldManager.class.getSimpleName()))
						html = "32046-01.htm";
					else
						html = "32046-02.htm";
				}
				else if (qs.isStarted())
				{
					switch (qs.getMemoState())
					{
						case 1:
						{
							html = "32046-09.html";
							break;
						}
						case 2:
						{
							html = "32046-12.html";
							break;
						}
						case 3:
						{
							html = "32046-13.html";
							break;
						}
						case 4:
						{
							if (qs.hasQuestItems(FLOWER_OF_PAVEL))
								html = "32046-16.html";
							break;
						}
						case 7:
						{
							html = "32046-17.html";
							break;
						}
						case 8:
						{
							html = "32046-28.html";
							break;
						}
						case 11:
						{
							html = "32046-29.html";
							break;
						}
						case 12:
						{
							html = "32046-36.html";
							break;
						}
						case 19:
						{
							html = "32046-37.html";
							break;
						}
						case 20:
						{
							html = "32046-42.html";
							break;
						}
						case 22:
						{
							html = "32046-43.html";
							break;
						}
						case 23:
						{
							if (qs.hasQuestItems(HEART_OF_ATLANTA))
								html = "32046-45.html";
							break;
						}
					}
				}
				else
				{
					if (player.hasQuestCompleted(Q114_ResurrectionOfAnOldManager.class.getSimpleName()))
						html = getAlreadyCompletedMsg();
				}
				break;
			}
			case WENDY:
			{
				switch (qs.getMemoState())
				{
					case 2:
					{
						html = "32047-01.html";
						break;
					}
					case 3:
					{
						html = "32047-07.html";
						break;
					}
					case 4:
					{
						if (qs.hasQuestItems(FLOWER_OF_PAVEL))
							html = "32047-08.html";
						break;
					}
					case 5:
					{
						html = "32047-11.html";
						break;
					}
					case 6:
					{
						html = "32047-12.html";
						break;
					}
					case 7:
					{
						html = "32047-16.html";
						break;
					}
					case 12:
					{
						html = "32047-17.html";
						break;
					}
					case 13:
					{
						html = "32047-20.html";
						break;
					}
					case 14:
					{
						if (qs.hasQuestItems(LOCKUP_RESEARCH_REPORT))
							html = "32047-21.html";
						break;
					}
					case 23:
					{
						if (qs.hasQuestItems(HEART_OF_ATLANTA))
							html = "32047-22.html";
						break;
					}
					case 24:
					{
						html = "32047-27.html";
						break;
					}
					case 25:
					{
						html = "32047-30.html";
						break;
					}
					case 26:
					{
						if (qs.hasQuestItems(WENDYS_NECKLACE))
							html = "32047-34.html";
						break;
					}
				}
				break;
			}
			case YUMI:
			{
				switch (qs.getMemoState())
				{
					case 2:
					{
						
						switch (qs.getMemoStateEx(0))
						{
							case 0:
							{
								html = "32041-01.html";
								break;
							}
							case 1:
							{
								html = "32041-04.html";
								break;
							}
							case 2:
							{
								html = "32041-06.html";
								break;
							}
						}
						break;
					}
					case 5:
					{
						html = qs.getMemoStateEx(0) > 0 ? "32041-07.html" : "32041-08.html"; 
						break;
					}
					case 6:
					{
						html = "32041-14.html";
						break;
					}
					case 14:
					{
						if (qs.hasQuestItems(LOCKUP_RESEARCH_REPORT))
							html = "32041-15.html";
						break;
					}
					case 15:
					{
						if (qs.hasQuestItems(KEY_OF_ENIGMA))
							html = qs.hasQuestItems(RESEARCH_REPORT) ? "32041-19.html" : qs.hasQuestItems(LOCKUP_RESEARCH_REPORT) ? "32041-18.html": html;
						break;
					}
					case 16:
					{
						if (qs.hasQuestItems(RESEARCH_REPORT))
							html = "32041-27.html";
						break;
					}
					case 26:
					{
						if (qs.hasQuestItems(WENDYS_NECKLACE))
							html = "32041-28.html";
						break;
					}
				}
				break;
			}
			case WEATHERMASTER_1:
			{
				switch (qs.getMemoState())
				{
					case 8:
					{
						html = "32042-01.html";
						qs.playSound(Sound.AMBSOUND_CRYSTAL_LOOP.getSoundName());
						break;
					}
					case 9:
					{
						html = "32042-06.html";
						break;
					}
					case 10:
					{
						html = qs.isMemoStateEx(0, 10101) ? "32042-13.html" : "32042-09.html";
						break;
					}
					case 11:
					{
						html = "32042-14.html";
						break;
					}
				}
				break;
			}
			case WEATHERMASTER_2:
			{
				switch (qs.getMemoState())
				{
					case 16:
					{
						html = "32043-01.html";
						break;
					}
					case 17:
					{
						html = "32043-06.html";
						break;
					}
					case 18:
					{
						html = "32043-09.html";
						break;
					}
					case 19:
					{
						html = "32043-23.html";
						break;
					}
				}
				break;
			}
			case WEATHERMASTER_3:
			{
				switch (qs.getMemoState())
				{
					case 20:
					{
						html = "32044-01.html";
						break;
					}
					case 21:
					{
						html = "32044-08.html";
						break;
					}
					case 22:
					{
						html = "32044-22.html";
						break;
					}
				}
				break;
			}
			case DOCTOR_CHAOS_SECRET_BOOKSHELF:
			{
				switch (qs.getMemoState())
				{
					case 13:
					{
						html = "32045-01.html";
						break;
					}
					case 14:
					{
						html = "32045-03.html";
						break;
					}
				}
				break;
			}
		}
		
		return html;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = player.getQuestState(qn);
		if (qs == null)
			return super.onAdvEvent(event, npc, player);
		
		String html = null;
		switch (event)
		{
			case "32046-03.html":
			case "32046-04.htm":
			case "32046-05.html":
			case "32046-06.html":
			{
				if (qs.isCreated())
					html = event;
				break;
			}
			case "quest_accept":
			{
				if (qs.isCreated() && player.hasQuestCompleted(Q114_ResurrectionOfAnOldManager.class.getSimpleName()))
				{
					if (player.getLevel() >= 70)
					{
						qs.setState(STATE_STARTED);
						qs.set("cond", "1");
						qs.playSound(QuestState.SOUND_ACCEPT);
						qs.setMemoState(1);
						html = "32046-08.htm";
					}
					else
						html = "32046-07.htm";
				}
				break;
			}
			case "32046-10.html":
			{
				if (qs.isMemoState(1))
					html = event;
				break;
			}
			case "32046-11.html":
			{
				if (qs.isMemoState(1))
				{
					qs.setMemoState(2);
					qs.set("cond", "2");
					html = event;
				}
				break;
			}
			case "32046-14.html":
			{
				if (qs.isMemoState(3))
					html = event;
				break;
			}
			case "32046-15.html":
			{
				if (qs.isMemoState(3))
				{
					qs.giveItems(FLOWER_OF_PAVEL, 1);
					qs.setMemoState(4);
					qs.set("cond", "6");
					html = event;
				}
				break;
			}
			case "32046-18.html":
			case "32046-19.html":
			case "32046-20.html":
			case "32046-21.html":
			case "32046-22.html":
			case "32046-23.html":
			case "32046-24.html":
			{
				if (qs.isMemoState(7))
					html = event;
				break;
			}
			case "32046-25.html":
			{
				if (qs.isMemoState(7))
				{
					qs.setMemoState(8);
					qs.set("cond", "10");
					html = event;
				}
				break;
			}
			case "32046-26.html":
			case "32046-27.html":
			case "32046-28.html":
			{
				if (qs.isMemoState(8))
					html = event;
				break;
			}
			case "32046-30.html":
			case "32046-31.html":
			case "32046-32.html":
			case "32046-33.html":
			case "32046-34.html":
			{
				if (qs.isMemoState(11))
					html = event;
				break;
			}
			case "32046-35.html":
			{
				if (qs.isMemoState(11))
				{
					qs.setMemoState(12);
					qs.set("cond", "13");
					html = event;
				}
				break;
			}
			case "32046-38.html":
			case "32046-39.html":
			case "32046-40.html":
			{
				if (qs.isMemoState(19))
					html = event;
				break;
			}
			case "32046-41.html":
			{
				if (qs.isMemoState(19))
				{
					qs.setMemoState(20);
					qs.set("cond", "20");
					html = event;
				}
				break;
			}
			case "32046-44.html":
			{
				if (qs.isMemoState(22))
				{
					qs.giveItems(HEART_OF_ATLANTA, 1);
					qs.setMemoState(23);
					qs.set("cond", "23");
					html = event;
				}
				break;
			}
			case "32047-02.html":
			case "32047-03.html":
			case "32047-04.html":
			case "32047-05.html":
			{
				if (qs.isMemoState(2))
					html = event;
				break;
			}
			case "32047-06.html":
			{
				if (qs.isMemoState(2))
				{
					qs.setMemoState(3);
					qs.set("cond", "5");
					html = event;
				}
				break;
			}
			case "32047-09.html":
			{
				if (qs.isMemoState(4) && qs.hasQuestItems(FLOWER_OF_PAVEL))
					html = event;

				break;
			}
			case "32047-10.html":
			{
				if (qs.isMemoState(4) && qs.hasQuestItems(FLOWER_OF_PAVEL))
				{
					qs.takeItems(FLOWER_OF_PAVEL, -1);
					qs.setMemoState(5);
					qs.set("cond", "7");
					html = event;
				}
				break;
			}
			case "32047-13.html":
			case "32047-14.html":
			{
				if (qs.isMemoState(6))
					html = event;
				break;
			}
			case "32047-15.html":
			{
				if (qs.isMemoState(6))
				{
					qs.setMemoState(7);
					qs.set("cond", "9");
					html = event;
				}
				break;
			}
			case "32047-18.html":
			{
				if (qs.isMemoState(12))
					html = event;
				break;
			}
			case "32047-19.html":
			{
				if (qs.isMemoState(12))
				{
					qs.setMemoState(13);
					qs.set("cond", "14");
					html = event;
				}
				break;
			}
			case "32047-23.html":
			case "32047-24.html":
			case "32047-25.html":
			case "32047-26.html":
			{
				if (qs.isMemoState(23) && qs.hasQuestItems(HEART_OF_ATLANTA))
					html = event;
				break;
			}
			case "32047-27.html":
			{
				if (qs.isMemoState(23) && qs.hasQuestItems(HEART_OF_ATLANTA))
				{
					qs.takeItems(HEART_OF_ATLANTA, -1);
					qs.setMemoState(24);
					qs.set("cond", "24");
					html = event;
				}
				break;
			}
			case "32047-28.html":
			case "32047-29.html":
			{
				if (qs.isMemoState(24))
					html = event;
				break;
			}
			case "32047-30.html":
			{
				if (qs.isMemoState(24))
				{
					qs.setMemoState(25);
					html = event;
				}
				break;
			}
			case "32047-31.html":
			case "32047-32.html":
			{
				if (qs.isMemoState(25))
				{
					html = event;
				}
				break;
			}
			case "32047-33.html":
			{
				if (qs.isMemoState(25))
				{
					qs.giveItems(WENDYS_NECKLACE, 1);
					qs.setMemoState(26);
					qs.set("cond", "25");
					html = event;
				}
				break;
			}
			case "32041-02.html":
			{
				if (qs.isMemoState(2))
					html = event;
				break;
			}
			case "32041-03.html":
			{
				if (qs.isMemoState(2) && qs.isMemoStateEx(0, 0))
				{
					qs.setMemoStateEx(0, 1);
					qs.set("cond", "3");
					html = event;
				}
				break;
			}
			case "32041-05.html":
			{
				if (qs.isMemoState(2) && qs.isMemoStateEx(0, 0))
				{
					qs.setMemoStateEx(0, 2);
					qs.set("cond", "4");
					html = event;
				}
				break;
			}
			case "32041-09.html":
			case "32041-10.html":
			case "32041-11.html":
			case "32041-12.html":
			{
				if (qs.isMemoState(5))
					html = event;
				break;
			}
			case "32041-13.html":
			{
				if (qs.isMemoState(5))
				{
					qs.setMemoState(6);
					qs.set("cond", "8");
					html = event;
				}
				break;
			}
			case "32041-16.html":
			{
				if (qs.isMemoState(14) && qs.hasQuestItems(LOCKUP_RESEARCH_REPORT))
					html = event;
				break;
			}
			case "32041-17.html":
			{
				if (qs.isMemoState(14) && qs.hasQuestItems(LOCKUP_RESEARCH_REPORT))
				{
					qs.giveItems(KEY_OF_ENIGMA, 1);
					qs.setMemoState(15);
					qs.set("cond", "16");
					html = event;
				}
				break;
			}
			case "32041-20.html":
			{
				if (qs.isMemoState(15) && qs.hasQuestItems(RESEARCH_REPORT, KEY_OF_ENIGMA))
					html = event;
				break;
			}
			case "pavel":
			case "e=mc2":
			{
				if (qs.isMemoState(15) && qs.hasQuestItems(RESEARCH_REPORT, KEY_OF_ENIGMA))
					html = "32041-21.html";
				break;
			}
			case "wdl":
			{
				if (qs.isMemoState(15) && qs.hasQuestItems(RESEARCH_REPORT, KEY_OF_ENIGMA))
					html = "32041-22.html";
				break;
			}
			case "32041-23.html":
			{
				if (qs.isMemoState(15) && qs.hasQuestItems(RESEARCH_REPORT, KEY_OF_ENIGMA))
				{
					qs.takeItems(KEY_OF_ENIGMA, -1);
					qs.setMemoState(16);
					qs.set("cond", "17");
					html = event;
				}
				break;
			}
			case "32041-24.html":
			case "32041-26.html":
			{
				if (qs.isMemoState(16) && qs.hasQuestItems(RESEARCH_REPORT))
					html = event;
				break;
			}
			case "32041-29.html":
			case "32041-30.html":
			case "32041-31.html":
			case "32041-32.html":
			case "32041-33.html":
			{
				if (qs.isMemoState(26) && qs.hasQuestItems(WENDYS_NECKLACE))
					html = event;
				break;
			}
			case "32041-34.html":
			{
				if (qs.isMemoState(26) && qs.hasQuestItems(WENDYS_NECKLACE))
				{
					qs.takeItems(WENDYS_NECKLACE, -1);
					qs.rewardItems(SEALED_PHOENIX_EARRING, 1);
					qs.giveItems(57, 783720);
					qs.rewardExpAndSp(3447315, 272615);
					qs.exitQuest(false);
					html = event;
				}
				break;
			}
			case "32042-02.html":
			{
				if (qs.isMemoState(8))
				{
					qs.setMemoStateEx(0, 0);
					html = event;
				}
				break;
			}
			case "wm1_1_b":
			case "wm1_1_c":
			case "wm1_1_d":
			case "wm1_1_l":
			case "wm1_1_m":
			case "wm1_1_n":
			case "wm1_1_s":
			case "wm1_1_t":
			case "wm1_1_u":
			{
				if (qs.isMemoState(8))
					html = "32042-03.html";
				break;
			}
			case "wm1_1_a":
			{
				if (qs.isMemoState(8))
				{
					qs.setMemoStateEx(0, 1);
					html = "32042-03.html";
				}
				break;
			}
			case "wm1_2_a":
			case "wm1_2_b":
			case "wm1_2_c":
			case "wm1_2_d":
			case "wm1_2_l":
			case "wm1_2_m":
			case "wm1_2_n":
			case "wm1_2_s":
			case "wm1_2_u":
			{
				if (qs.isMemoState(8))
					html = "32042-04.html";
				break;
			}
			case "wm1_2_t":
			{
				if (qs.isMemoState(8))
				{
					qs.setMemoStateEx(0, 10 + (qs.getMemoStateEx(0) % 10));
					html = "32042-04.html";
				}
				break;
			}
			case "wm1_3_a":
			case "wm1_3_b":
			case "wm1_3_c":
			case "wm1_3_d":
			case "wm1_3_m":
			case "wm1_3_n":
			case "wm1_3_s":
			case "wm1_3_t":
			case "wm1_3_u":
			{
				if (qs.isMemoState(8))
					html = "32042-05.html";
				break;
			}
			case "wm1_3_l":
			{
				if (qs.isMemoState(8))
				{
					if (qs.isMemoStateEx(0, 11))
					{
						qs.setMemoState(9);
						qs.set("cond", "11");
						qs.setMemoStateEx(0, 0);
						html = "32042-06.html";
					}
					else
						html = "32042-05.html";
				}
				break;
			}
			case "32042-15.html":
			case "32042-06.html":
			case "32042-07.html":
			{
				if (qs.isMemoState(9))
					html = event;
				break;
			}
			case "32042-08.html":
			{
				if (qs.isMemoState(9))
				{
					qs.setMemoState(10);
					qs.playSound(Sound.AMBSOUND_PERCUSSION_01.getSoundName());
					html = event;
				}
				break;
			}
			case "wm1_return":
			{
				if (qs.isMemoState(10))
					html = qs.isMemoStateEx(0, 10101) ? "32042-13.html" : "32042-09.html";
				break;
			}
			case "32042-10.html":
			{
				if (qs.isMemoState(10))
				{
					qs.setMemoStateEx(0, ((qs.getMemoStateEx(0) / 10) * 10) + 1);
					html = event;
				}
				break;
			}
			case "32042-11.html":
			{
				if (qs.isMemoState(10))
				{
					final int memoStateEx = qs.getMemoStateEx(0);
					final int i1 = (memoStateEx / 1000) * 1000;
					final int i2 = (memoStateEx % 100) + 100;
					qs.setMemoStateEx(0, i1 + i2);
					html = event;
				}
				break;
			}
			case "32042-12.html":
			{
				if (qs.isMemoState(10))
				{
					qs.setMemoStateEx(0, 10000 + (qs.getMemoStateEx(0) % 10000));
					html = event;
				}
				break;
			}
			case "32042-14.html":
			{
				if (qs.isMemoState(10) && qs.isMemoStateEx(0, 10101))
				{
					qs.setMemoState(11);
					qs.set("cond", "12");
					qs.setMemoStateEx(0, 0);
					html = event;
				}
				break;
			}
			case "32043-02.html":
			{
				if (qs.isMemoState(16))
				{
					qs.setMemoStateEx(0, 0);
					html = event;
				}
				break;
			}
			case "wm2_1_a":
			case "wm2_1_b":
			case "wm2_1_c":
			case "wm2_1_d":
			case "wm2_1_l":
			case "wm2_1_m":
			case "wm2_1_n":
			case "wm2_1_v":
			case "wm2_1_x":
			{
				if (qs.isMemoState(16))
					html = "32043-03.html";
				break;
			}
			case "wm2_1_w":
			{
				if (qs.isMemoState(16))
				{
					qs.setMemoStateEx(0, 1);
					html = "32043-03.html";
				}
				break;
			}
			case "wm2_2_a":
			case "wm2_2_b":
			case "wm2_2_c":
			case "wm2_2_l":
			case "wm2_2_m":
			case "wm2_2_n":
			case "wm2_2_v":
			case "wm2_2_w":
			case "wm2_2_x":
			{
				if (qs.isMemoState(16))
					html = "32043-04.html";
				break;
			}
			case "wm2_2_d":
			{
				if (qs.isMemoState(16))
				{
					qs.setMemoStateEx(0, 10 + (qs.getMemoStateEx(0) % 10));
					html = "32043-04.html";
				}
				break;
			}
			case "wm2_3_a":
			case "wm2_3_b":
			case "wm2_3_c":
			case "wm2_3_d":
			case "wm2_3_m":
			case "wm2_3_n":
			case "wm2_3_v":
			case "wm2_3_w":
			case "wm2_3_x":
			{
				if (qs.isMemoState(8))
					html = "32043-05.html";
				break;
			}
			case "wm2_3_l":
			{
				if (qs.isMemoState(16))
				{
					if (qs.isMemoStateEx(0, 11))
					{
						qs.setMemoState(17);
						qs.set("cond", "18");
						qs.setMemoStateEx(0, 0);
						html = "32043-06.html";
					}
					else
						html = "32043-05.html";
				}
				break;
			}
			case "32043-31.html":
			case "32043-30.html":
			case "32043-29.html":
			case "32043-28.html":
			case "32043-06.html":
			case "32043-07.html":
			case "32043-08.html":
			{
				if (qs.isMemoState(17))
					html = event;
				break;
			}
			case "32043-09.html":
			{
				if (qs.isMemoState(17))
				{
					qs.setMemoState(18);
					html = event;
				}
				break;
			}
			case "32043-10.html":
			case "wm2_return":
			{
				if (qs.isMemoState(18))
					html = qs.isMemoStateEx(0, 1111) ? "32043-12.html" : "32043-11.html";
				break;
			}
			case "32043-13.html":
			{
				if (qs.isMemoState(18))
				{
					qs.setMemoStateEx(0, ((qs.getMemoStateEx(0) / 10) * 10) + 1);
					html = event;
				}
				break;
			}
			case "32043-14.html":
			{
				if (qs.isMemoState(18))
					html = event;
				break;
			}
			case "wm2_output":
			{
				if (qs.isMemoState(18))
					html = qs.getMemoStateEx(0) < 1000 ? "32043-15.html" : "32043-18.html";
				break;
			}
			case "32043-16.html":
			{
				if (qs.isMemoState(18))
					html = event;
				break;
			}
			case "32043-17.html":
			{
				if (qs.isMemoState(18))
				{
					final int memoStateEx = qs.getMemoStateEx(0);
					final int i1 = (memoStateEx / 10000) * 10000;
					final int i2 = (memoStateEx % 1000) + 1000;
					qs.setMemoStateEx(0, i1 + i2);
					qs.playSound(Sound.AMBSOUND_DRONE.getSoundName());
					html = event;
				}
				break;
			}
			case "32043-19.html":
			case "32043-20.html":
			{
				if (qs.isMemoState(18))
					html = event;
				break;
			}
			case "32043-21.html":
			{
				if (qs.isMemoState(18))
				{
					final int memoStateEx = qs.getMemoStateEx(0);
					final int i1 = (memoStateEx / 100) * 100;
					final int i2 = (memoStateEx % 10) + 10;
					qs.setMemoStateEx(0, i1 + i2);
					html = event;
				}
				break;
			}
			case "32043-22.html":
			{
				if (qs.isMemoState(18) && qs.isMemoStateEx(0, 1111))
				{
					qs.setMemoState(19);
					qs.set("cond", "19");
					qs.setMemoStateEx(0, 0);
					html = event;
				}
				break;
			}
			case "32043-24.html":
			case "32043-25.html":
			{
				if (qs.isMemoState(18))
					html = event;
				break;
			}
			case "32043-26.html":
			{
				if (qs.isMemoState(18))
				{
					final int memoStateEx = qs.getMemoStateEx(0);
					final int i1 = (memoStateEx / 1000) * 1000;
					final int i2 = (memoStateEx % 100) + 100;
					qs.setMemoStateEx(0, i1 + i2);
					html = event;
				}
				break;
			}
			case "32043-27.html":
			{
				if (qs.isMemoState(18))
					html = event;
				break;
			}
			case "32044-02.html":
			{
				if (qs.isMemoState(20))
				{
					qs.setMemoStateEx(0, 0);
					html = event;
				}
				break;
			}
			case "wm3_1_a":
			case "wm3_1_b":
			case "wm3_1_c":
			case "wm3_1_d":
			case "wm3_1_l":
			case "wm3_1_m":
			case "wm3_1_v":
			case "wm3_1_w":
			case "wm3_1_x":
			{
				if (qs.isMemoState(20))
					html = "32044-03.html";
				break;
			}
			case "wm3_1_n":
			{
				if (qs.isMemoState(20))
				{
					qs.setMemoStateEx(0, 1);
					html = "32044-03.html";
				}
				break;
			}
			case "wm3_2_1":
			case "wm3_2_2":
			case "wm3_2_3":
			case "wm3_2_5":
			case "wm3_2_6":
			case "wm3_2_7":
			case "wm3_2_8":
			case "wm3_2_9":
			case "wm3_2_10":
			{
				if (qs.isMemoState(20))
					html = "32044-04.html";
				break;
			}
			case "wm3_2_4":
			{
				if (qs.isMemoState(20))
				{
					qs.setMemoStateEx(0, 10 + (qs.getMemoStateEx(0) % 10));
					html = "32044-04.html";
				}
				break;
			}
			case "wm3_3_1":
			case "wm3_3_2":
			case "wm3_3_3":
			case "wm3_3_4":
			case "wm3_3_6":
			case "wm3_3_7":
			case "wm3_3_8":
			case "wm3_3_9":
			case "wm3_3_10":
			{
				if (qs.isMemoState(20))
					html = "32044-05.html";
				break;
			}
			case "wm3_3_5":
			{
				if (qs.isMemoState(20))
				{
					if (qs.isMemoStateEx(0, 11))
					{
						qs.setMemoState(21);
						qs.set("cond", "21");
						qs.setMemoStateEx(0, 0);
						qs.playSound(Sound.AMBSOUND_PERCUSSION_02.getSoundName());
						html = "32044-06.html";
					}
					else
						html = "32044-05.html";
				}
				break;
			}
			case "32044-07.html":
			{
				if (qs.isMemoState(21))
				{
					html = event;
				}
				break;
			}
			case "wm3_observe":
			{
				if (qs.isMemoState(21))
					html = qs.getMemoStateEx(0) % 100 == 11 ? "32044-10.html" : "32044-09.html";
				break;
			}
			case "32044-11.html":
			{
				if (qs.isMemoState(21))
				{
					final int memoStateEx = qs.getMemoStateEx(0);
					final int i1 = (memoStateEx / 100) * 100;
					final int i2 = (memoStateEx % 10) + 10;
					qs.setMemoStateEx(0, i1 + i2);
					html = event;
				}
				break;
			}
			case "wm3_fire_of_paagrio":
			{
				if (qs.isMemoState(21))
				{
					if ((qs.getMemoStateEx(0) / 100) == 1)
						html = "32044-13.html";
					else
					{
						qs.setMemoStateEx(0, ((qs.getMemoStateEx(0) / 10) * 10) + 1);
						html = "32044-12.html";
					}
				}
				break;
			}
			case "wm3_control":
			{
				if (qs.isMemoState(21))
					html = qs.getMemoStateEx(0) / 100 == 1 ? "32044-15.html" : "32044-14.html";
				break;
			}
			case "32044-16.html":
			{
				if (qs.isMemoState(21) && ((qs.getMemoStateEx(0) / 100) != 1))
				{
					qs.setMemoStateEx(0, (qs.getMemoStateEx(0) % 100) + 100);
					html = event;
				}
				break;
			}
			case "32044-17.html":
			case "32044-18.html":
			case "32044-19.html":
			{
				if (qs.isMemoState(21))
					html = event;
				break;
			}
			case "32044-20.html":
			{
				if (qs.isMemoState(21) && ((qs.getMemoStateEx(0) / 100) == 1))
				{
					qs.setMemoState(22);
					qs.set("cond", "22");
					qs.setMemoStateEx(0, 0);
					qs.playSound(Sound.AMBSOUND_DRONE.getSoundName());
					npc.setTarget(player);
					npc.doCast(QUEST_TRAP_POWER_SHOT.getSkill());
					html = event;
				}
				break;
			}
			case "32044-21.html":
			{
				if (qs.isMemoState(22))
					html = event;
				break;
			}
			case "32045-02.html":
			{
				if (qs.isMemoState(13))
				{
					qs.giveItems(LOCKUP_RESEARCH_REPORT, 1);
					qs.setMemoState(14);
					qs.set("cond", "15");
					npc.setTarget(player);
					npc.doCast(QUEST_TRAP_POWER_SHOT.getSkill());
					html = event;
				}
				break;
			}
		}
		
		return html;
	}
	
	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance player, L2Skill skill, L2Object[] targets, boolean isSummon)
	{
		final QuestState qs = player.getQuestState(qn);

		if ((qs != null) && qs.isStarted())
		{
			final L2Skill npcDefault = NPC_DEFAULT.getSkill();
			castSkill(npc, player, npcDefault);
			castSkill(npc, player, npcDefault);
		}
		return null;
	}	
	
	public static void main(String[] args)
	{
		new Q120_PavelsLastResearch();
	}
}
