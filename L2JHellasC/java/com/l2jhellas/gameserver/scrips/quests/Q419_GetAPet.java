package com.l2jhellas.gameserver.scrips.quests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Rnd;

public class Q419_GetAPet extends Quest
{
	private static final String qn = "Q419_GetAPet";
	
	// Items
	private static final int AnimalLoversList = 3417;
	private static final int AnimalSlayers1stList = 3418;
	private static final int AnimalSlayers2ndList = 3419;
	private static final int AnimalSlayers3rdList = 3420;
	private static final int AnimalSlayers4thList = 3421;
	private static final int AnimalSlayers5thList = 3422;
	private static final int BloodyFang = 3423;
	private static final int BloodyClaw = 3424;
	private static final int BloodyNail = 3425;
	private static final int BloodyKashaFang = 3426;
	private static final int BloodyTarantulaNail = 3427;
	
	// Reward
	private static final int WolfCollar = 2375;
	
	// NPCs
	private static final int Martin = 30731;
	private static final int Bella = 30256;
	private static final int Metty = 30072;
	private static final int Ellie = 30091;
	
	public Q419_GetAPet()
	{
		super(419, qn, "Get a Pet");
		
		setItemsIds(AnimalLoversList, AnimalSlayers1stList, AnimalSlayers2ndList, AnimalSlayers3rdList, AnimalSlayers4thList, AnimalSlayers5thList, BloodyFang, BloodyClaw, BloodyNail, BloodyKashaFang, BloodyTarantulaNail);
		
		addStartNpc(Martin);
		addTalkId(Martin, Bella, Ellie, Metty);
		
		addKillId(20103, 20106, 20108, 20460, 20308, 20466, 20025, 20105, 20034, 20474, 20476, 20478, 20403, 20508);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("task"))
		{
			st.setState(STATE_STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
			st.set("cond", "1");
			
			switch (player.getRace().ordinal())
			{
				case 0:
					st.giveItems(AnimalSlayers1stList, 1);
					htmltext = "30731-04.htm";
					break;
				
				case 1:
					st.giveItems(AnimalSlayers2ndList, 1);
					htmltext = "30731-05.htm";
					break;
				
				case 2:
					st.giveItems(AnimalSlayers3rdList, 1);
					htmltext = "30731-06.htm";
					break;
				
				case 3:
					st.giveItems(AnimalSlayers4thList, 1);
					htmltext = "30731-07.htm";
					break;
				
				case 4:
					st.giveItems(AnimalSlayers5thList, 1);
					htmltext = "30731-08.htm";
					break;
			}
		}
		else if (event.equalsIgnoreCase("30731-12.htm"))
		{
			st.takeItems(AnimalSlayers1stList, 1);
			st.takeItems(AnimalSlayers2ndList, 1);
			st.takeItems(AnimalSlayers3rdList, 1);
			st.takeItems(AnimalSlayers4thList, 1);
			st.takeItems(AnimalSlayers5thList, 1);
			st.takeItems(BloodyFang, -1);
			st.takeItems(BloodyClaw, -1);
			st.takeItems(BloodyNail, -1);
			st.takeItems(BloodyKashaFang, -1);
			st.takeItems(BloodyTarantulaNail, -1);
			st.giveItems(AnimalLoversList, 1);
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("30256-03.htm"))
		{
			st.set("progress", String.valueOf(st.getInt("progress") | 1));
			if (st.getInt("progress") == 7)
				st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("30072-02.htm"))
		{
			st.set("progress", String.valueOf(st.getInt("progress") | 2));
			if (st.getInt("progress") == 7)
				st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("30091-02.htm"))
		{
			st.set("progress", String.valueOf(st.getInt("progress") | 4));
			if (st.getInt("progress") == 7)
				st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("test"))
		{
			st.set("answers", "0");
			st.set("quiz", "20 21 22 23 24 25 26 27 28 29 30 31 32 33");
			return checkQuestions(st);
		}
		else if (event.equalsIgnoreCase("wrong"))
		{
			st.set("wrong", String.valueOf(st.getInt("wrong") + 1));
			return checkQuestions(st);
		}
		else if (event.equalsIgnoreCase("right"))
		{
			st.set("correct", String.valueOf(st.getInt("correct") + 1));
			return checkQuestions(st);
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
				if (player.getLevel() >= 15)
					htmltext = "30731-02.htm";
				else
				{
					htmltext = "30731-01.htm";
					st.exitQuest(true);
				}
				break;
			
			case STATE_STARTED:
				switch (npc.getNpcId())
				{
					case Martin:
						int lists = st.getQuestItemsCount(AnimalSlayers1stList) + st.getQuestItemsCount(AnimalSlayers2ndList) + st.getQuestItemsCount(AnimalSlayers3rdList) + st.getQuestItemsCount(AnimalSlayers4thList) + st.getQuestItemsCount(AnimalSlayers5thList);
						int proofs = st.getQuestItemsCount(BloodyFang) + st.getQuestItemsCount(BloodyClaw) + st.getQuestItemsCount(BloodyNail) + st.getQuestItemsCount(BloodyKashaFang) + st.getQuestItemsCount(BloodyTarantulaNail);
						if (lists == 1)
						{
							if (proofs == 0)
								htmltext = "30731-09.htm";
							else if (proofs < 50)
								htmltext = "30731-10.htm";
							else
								htmltext = "30731-11.htm";
						}
						else
						{
							if (st.getInt("progress") == 7)
								htmltext = "30731-13.htm";
							else
								htmltext = "30731-16.htm";
						}
						break;
					
					case Bella:
						htmltext = "30256-01.htm";
						break;
					
					case Metty:
						htmltext = "30072-01.htm";
						break;
					
					case Ellie:
						htmltext = "30091-01.htm";
						break;
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
			case 20103:
			case 20106:
			case 20108:
				if (st.hasQuestItems(AnimalSlayers1stList))
					st.dropItemsAlways(BloodyFang, 1, 50);
				break;
			
			case 20460:
			case 20308:
			case 20466:
				if (st.hasQuestItems(AnimalSlayers2ndList))
					st.dropItemsAlways(BloodyClaw, 1, 50);
				break;
			
			case 20025:
			case 20105:
			case 20034:
				if (st.hasQuestItems(AnimalSlayers3rdList))
					st.dropItemsAlways(BloodyNail, 1, 50);
				break;
			
			case 20474:
			case 20476:
			case 20478:
				if (st.hasQuestItems(AnimalSlayers4thList))
					st.dropItemsAlways(BloodyKashaFang, 1, 50);
				break;
			
			case 20403:
			case 20508:
				if (st.hasQuestItems(AnimalSlayers5thList))
					st.dropItemsAlways(BloodyTarantulaNail, 1, 50);
				break;
		}
		return null;
	}
	
	private static String join(List<String> list)
	{
		StringBuilder sb = new StringBuilder();
		String loopDelim = "";
		for (String s : list)
		{
			sb.append(loopDelim);
			sb.append(s);
			loopDelim = " ";
		}
		return sb.toString();
	}
	
	private static String checkQuestions(QuestState st)
	{
		final int answers = st.getInt("correct") + (st.getInt("wrong"));
		if (answers < 10)
		{
			String[] questions = st.get("quiz").split(" ");
			int index = Rnd.get(questions.length - 1);
			String question = questions[index];
			
			if (questions.length > 10 - answers)
			{
				questions[index] = questions[questions.length - 1];
				List<String> list = new ArrayList<>(Arrays.asList(questions));
				list.remove(questions.length - 1);
				questions = list.toArray(questions);
				st.set("quiz", join(list));
			}
			return "30731-" + question + ".htm";
		}
		
		if (st.getInt("wrong") > 0)
		{
			st.unset("progress");
			st.unset("answers");
			st.unset("quiz");
			st.unset("wrong");
			st.unset("correct");
			return "30731-14.htm";
		}
		
		st.takeItems(AnimalLoversList, 1);
		st.giveItems(WolfCollar, 1);
		st.playSound(QuestState.SOUND_FINISH);
		st.exitQuest(true);
		
		return "30731-15.htm";
	}
	
	public static void main(String[] args)
	{
		new Q419_GetAPet();
	}
}