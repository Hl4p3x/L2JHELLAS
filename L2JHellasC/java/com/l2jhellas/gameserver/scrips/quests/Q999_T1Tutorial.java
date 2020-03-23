package com.l2jhellas.gameserver.scrips.quests;

import java.util.HashMap;
import java.util.Map;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q999_T1Tutorial extends Quest
{
	private static final String qn = "Q999_T1Tutorial";
	private static final int RECOMMENDATION_01 = 1067;
	private static final int RECOMMENDATION_02 = 1068;
	private static final int LEAF_OF_MOTHERTREE = 1069;
	private static final int BLOOD_OF_JUNDIN = 1070;
	private static final int LICENSE_OF_MINER = 1498;
	private static final int VOUCHER_OF_FLAME = 1496;
	private static final int SOULSHOT_NOVICE = 5789;
	private static final int SPIRITSHOT_NOVICE = 5790;
	private static final int BLUE_GEM = 6353;
	
	private static final Map<String, Event> events = new HashMap<>();
	{
		events.put("30008_02", new Event("30008-03.htm", 0, 0, 0, RECOMMENDATION_01, 0, SOULSHOT_NOVICE, 200, 0, 0, 0));
		events.put("30008_04", new Event("30008-04.htm", -84058, 243239, -3730, 0, 0, 0, 0, 0, 0, 0));
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
	
	private static final Map<Integer, Talk> talks = new HashMap<>();
	{
		talks.put(30017, new Talk(0, new String[]
		{
			"30017-01.htm",
			"30017-02.htm",
			"30017-04.htm"
		}, 0, 0));
		talks.put(30008, new Talk(0, new String[]
		{
			"30008-01.htm",
			"30008-02.htm",
			"30008-04.htm"
		}, 0, 0));
		talks.put(30370, new Talk(1, new String[]
		{
			"30370-01.htm",
			"30370-02.htm",
			"30370-04.htm"
		}, 0, 0));
		talks.put(30129, new Talk(2, new String[]
		{
			"30129-01.htm",
			"30129-02.htm",
			"30129-04.htm"
		}, 0, 0));
		talks.put(30573, new Talk(3, new String[]
		{
			"30573-01.htm",
			"30573-02.htm",
			"30573-04.htm"
		}, 0, 0));
		talks.put(30528, new Talk(4, new String[]
		{
			"30528-01.htm",
			"30528-02.htm",
			"30528-04.htm"
		}, 0, 0));
		talks.put(30018, new Talk(0, new String[]
		{
			"30131-01.htm",
			"",
			"30019-03a.htm",
			"30019-04.htm"
		}, 1, RECOMMENDATION_02));
		talks.put(30019, new Talk(0, new String[]
		{
			"30131-01.htm",
			"",
			"30019-03a.htm",
			"30019-04.htm"
		}, 1, RECOMMENDATION_02));
		talks.put(30020, new Talk(0, new String[]
		{
			"30131-01.htm",
			"",
			"30019-03a.htm",
			"30019-04.htm"
		}, 1, RECOMMENDATION_02));
		talks.put(30021, new Talk(0, new String[]
		{
			"30131-01.htm",
			"",
			"30019-03a.htm",
			"30019-04.htm"
		}, 1, RECOMMENDATION_02));
		talks.put(30009, new Talk(0, new String[]
		{
			"30530-01.htm",
			"30009-03.htm",
			"",
			"30009-04.htm"
		}, 1, RECOMMENDATION_01));
		talks.put(30011, new Talk(0, new String[]
		{
			"30530-01.htm",
			"30009-03.htm",
			"",
			"30009-04.htm"
		}, 1, RECOMMENDATION_01));
		talks.put(30012, new Talk(0, new String[]
		{
			"30530-01.htm",
			"30009-03.htm",
			"",
			"30009-04.htm"
		}, 1, RECOMMENDATION_01));
		talks.put(30056, new Talk(0, new String[]
		{
			"30530-01.htm",
			"30009-03.htm",
			"",
			"30009-04.htm"
		}, 1, RECOMMENDATION_01));
		talks.put(30400, new Talk(1, new String[]
		{
			"30131-01.htm",
			"30400-03.htm",
			"30400-03a.htm",
			"30400-04.htm"
		}, 1, LEAF_OF_MOTHERTREE));
		talks.put(30401, new Talk(1, new String[]
		{
			"30131-01.htm",
			"30400-03.htm",
			"30400-03a.htm",
			"30400-04.htm"
		}, 1, LEAF_OF_MOTHERTREE));
		talks.put(30402, new Talk(1, new String[]
		{
			"30131-01.htm",
			"30400-03.htm",
			"30400-03a.htm",
			"30400-04.htm"
		}, 1, LEAF_OF_MOTHERTREE));
		talks.put(30403, new Talk(1, new String[]
		{
			"30131-01.htm",
			"30400-03.htm",
			"30400-03a.htm",
			"30400-04.htm"
		}, 1, LEAF_OF_MOTHERTREE));
		talks.put(30131, new Talk(2, new String[]
		{
			"30131-01.htm",
			"30131-03.htm",
			"30131-03a.htm",
			"30131-04.htm"
		}, 1, BLOOD_OF_JUNDIN));
		talks.put(30404, new Talk(2, new String[]
		{
			"30131-01.htm",
			"30131-03.htm",
			"30131-03a.htm",
			"30131-04.htm"
		}, 1, BLOOD_OF_JUNDIN));
		talks.put(30574, new Talk(3, new String[]
		{
			"30575-01.htm",
			"30575-03.htm",
			"30575-03a.htm",
			"30575-04.htm"
		}, 1, VOUCHER_OF_FLAME));
		talks.put(30575, new Talk(3, new String[]
		{
			"30575-01.htm",
			"30575-03.htm",
			"30575-03a.htm",
			"30575-04.htm"
		}, 1, VOUCHER_OF_FLAME));
		talks.put(30530, new Talk(4, new String[]
		{
			"30530-01.htm",
			"30530-03.htm",
			"",
			"30530-04.htm"
		}, 1, LICENSE_OF_MINER));
		
	}
	
	public Q999_T1Tutorial()
	{
		super(999, qn, "Q999_T1Tutorial");
		addStartNpc(30008, 30009, 30017, 30019, 30129, 30131, 30573, 30575, 30370, 30528, 30530, 30400, 30401, 30402, 30403, 30404);
		addTalkId(30008, 30009, 30017, 30019, 30129, 30131, 30573, 30575, 30370, 30528, 30530, 30400, 30401, 30402, 30403, 30404);
		addFirstTalkId(30008, 30009, 30017, 30019, 30129, 30131, 30573, 30575, 30370, 30528, 30530, 30400, 30401, 30402, 30403, 30404);
		addKillId(18342, 20001);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (player == null)
			return null;
		
		final QuestState st = player.getQuestState(qn);
		final QuestState qs = player.getQuestState("Q255_Tutorial");
		
		if (qs == null || st == null)
			return null;
		
		String htmltext = event;
		Event e = events.get(event);
		htmltext = e.htm;
		int Ex = qs.getInt("Ex");
		final int classId = player.getClassId().getId();
		boolean isMage = player.isMageClass();
		
		if (event.equalsIgnoreCase("TimerEx_NewbieHelper"))
		{
			if (Ex == 0)
			{
				if (isMage)
					st.playTutorialVoice("tutorial_voice_009b");
				else
					st.playTutorialVoice("tutorial_voice_009a");
				
				qs.set("Ex", "1");
			}
			else if (Ex == 3)
			{
				st.playTutorialVoice("tutorial_voice_010a");
				qs.set("Ex", "4");
			}
		}
		
		if (event.equalsIgnoreCase("TimerEx_GrandMaster"))
		{
			if (Ex >= 4)
			{
				st.showQuestionMark(7);
				st.playSound("ItemSound.quest_tutorial");
				st.playTutorialVoice("tutorial_voice_025");
			}
		}
		
		if (st.getQuestItemsCount(e.item) > 0 && st.getInt("onlyone") == 0)
		{
			st.rewardExpAndSp(0, 50);
			st.getQuest().startQuestTimer("TimerEx_GrandMaster", 60000, null, null, true);
			st.takeItems(e.item, 1);
			
			if (Ex <= 3)
				qs.set("Ex", "4");
			
			if (classId == e.classId1)
			{
				st.giveItems(e.gift1, e.count1);
				
				if (e.gift1 == SPIRITSHOT_NOVICE)
					st.playTutorialVoice("tutorial_voice_027");
				else
					st.playTutorialVoice("tutorial_voice_026");
			}
			else if (classId == e.classId2 && e.gift2 != 0)
			{
				st.giveItems(e.gift2, e.count2);
				st.playTutorialVoice("tutorial_voice_026");
			}
			st.set("step", "3");
			st.set("onlyone", "1");
		}
		
		if (e.radarX != 0)
			st.addRadar(e.radarX, e.radarY, e.radarZ);
		
		return htmltext;
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
					{
						st.set("step", "1");
						st.setState(STATE_COMPLETED);
					}
					else
					{
						htmltext = "30530-01.htm";
						st.set("step", "1");
						st.setState(STATE_COMPLETED);
					}
				}
				else if (step == 1 && st.getQuestItemsCount(t.item) == 0 && Ex <= 2)
				{
					if (st.getQuestItemsCount(BLUE_GEM) > 0)
					{
						st.takeItems(BLUE_GEM, st.getQuestItemsCount(BLUE_GEM));
						st.giveItems(t.item, 1);
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
		new Q999_T1Tutorial();
	}
}