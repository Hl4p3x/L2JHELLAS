package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class Q137_TempleChampionPart1 extends Quest
{
	private static final String qn = "Q137_TempleChampionPart1";

	private static final int SYLVAIN = 30070;
	private static final int MOBS[] =
	{
		20083,
		20144,
		20199,
		20200,
		20201,
		20202,
	};

	private static final int FRAGMENT = 10340;
	private static final int EXECUTOR = 10334;
	private static final int MISSIONARY = 10339;
	
	public Q137_TempleChampionPart1()
	{
		super(137,qn,"Temple Champion Part1");
		addStartNpc(SYLVAIN);
		addTalkId(SYLVAIN);
		addKillId(MOBS);
		setItemsIds(FRAGMENT);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = player.getQuestState(qn);

		if (qs == null)
			return getNoQuestMsg();

		switch (event)
		{
			case "30070-02.htm":
			{
				qs.setState(STATE_STARTED);
				qs.set("cond", "1");
				qs.playSound(QuestState.SOUND_ACCEPT);
				break;
			}
			case "30070-05.html":
			{
				qs.set("talk", "1");
				break;
			}
			case "30070-06.html":
			{
				qs.set("talk", "2");
				break;
			}
			case "30070-08.html":
			{
				qs.unset("talk");
				qs.set("cond", "2");
				break;
			}
			case "30070-16.html":
			{
				if (qs.isCond(3) && qs.hasQuestItems(EXECUTOR) && qs.hasQuestItems(MISSIONARY))
				{
					qs.takeItems(EXECUTOR, -1);
					qs.takeItems(MISSIONARY, -1);
					qs.exitQuest(false);
				}
				break;
			}
		}
		return event;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		final QuestState qs = player.getQuestState(qn);

		if ((qs != null) && qs.isStarted() && qs.isCond(2) && (qs.getQuestItemsCount(FRAGMENT) < 30))
		{
			qs.giveItems(FRAGMENT, 1);
			if (qs.getQuestItemsCount(FRAGMENT) >= 30)
				qs.set("cond", "3");
			else
				qs.playSound(QuestState.SOUND_ITEMGET);
		}
		return super.onKill(npc, player, isSummon);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = player.getQuestState(qn);
		String htmltext = getNoQuestMsg();
		
		if (qs.isCompleted())
			return getAlreadyCompletedMsg();

		switch (qs.getCond())
		{
			case 1:
			{
				switch (qs.getInt("talk"))
				{
					case 1:
					{
						htmltext = "30070-05.html";
						break;
					}
					case 2:
					{
						htmltext = "30070-06.html";
						break;
					}
					default:
					{
						htmltext = "30070-03.html";
						break;
					}
				}
				break;
			}
			case 2:
			{
				htmltext = "30070-08.html";
				break;
			}
			case 3:
			{
				if (qs.getInt("talk") == 1)
					htmltext = "30070-10.html";
				else if (qs.getQuestItemsCount(FRAGMENT) >= 30)
				{
					qs.set("talk", "1");
					htmltext = "30070-09.html";
					qs.takeItems(FRAGMENT, -1);
				}
				break;
			}
			default:
			{
				htmltext = ((player.getLevel() >= 35) && qs.hasQuestItems(EXECUTOR, MISSIONARY)) ? "30070-01.htm" : "30070-00.html";
				break;
			}
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Q137_TempleChampionPart1();
	}
}
