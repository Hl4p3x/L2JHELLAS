package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.util.Rnd;

public class Q139_ShadowFoxPart1 extends Quest
{
	private static final String qn = "Q139_ShadowFoxPart1";

	private static final int MIA = 30896;

	private static final int MOBS[] =
	{
		20784,
		20785, 
		21639,
		21640,
	};
	
	private static final int FRAGMENT = 10345;
	private static final int CHEST = 10346;
	
	private static final int MIN_LEVEL = 37;
	private static final int DROP_CHANCE = 68;
	
	public Q139_ShadowFoxPart1()
	{
		super(139,qn,"Shadow Fox Part1");
		addStartNpc(MIA);
		addTalkId(MIA);
		addKillId(MOBS);
		setItemsIds(FRAGMENT, CHEST);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = player.getQuestState(qn);
		
		if (qs == null)
			return null;
		
		String htmltext = event;
		switch (event)
		{
			case "30896-02.htm":
			{
				if (player.getLevel() < MIN_LEVEL)
				{
					htmltext = "30896-03.htm";
				}
				break;
			}
			case "30896-04.htm":
			{
				qs.setState(STATE_STARTED);
				qs.set("cond", "1");
				qs.playSound(QuestState.SOUND_ACCEPT);
				break;
			}
			case "30896-11.html":
			{
				qs.set("talk", "1");
				break;
			}
			case "30896-13.html":
			{
				qs.set("cond", "2");
				qs.unset("talk");
				break;
			}
			case "30896-17.html":
			{
				if (Rnd.get(20) < 3)
				{
					qs.takeItems(FRAGMENT, 10);
					qs.takeItems(CHEST, 1);
					return "30896-16.html";
				}
				qs.takeItems(FRAGMENT, -1);
				qs.takeItems(CHEST, -1);
				qs.set("talk", "1");
				break;
			}
			case "30896-19.html":
			{
				qs.exitQuest(false);
				break;
			}
			case "30896-06.html":
			case "30896-07.html":
			case "30896-08.html":
			case "30896-09.html":
			case "30896-10.html":
			case "30896-12.html":
			case "30896-18.html":
			{
				break;
			}
			default:
			{
				htmltext = null;
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		final L2PcInstance member = getRandomPartyMember(player,npc);
		
		if (member == null)
			return super.onKill(npc, player, isSummon);
		
		final QuestState st = member.getQuestState(qn);

		final QuestState qs = player.getQuestState(qn);
		
		if (qs == null)
			return super.onKill(npc, player, isSummon);
		
		if (!st.isSet("talk") && (Rnd.get(100) < DROP_CHANCE))
		{
			final int itemId = (Rnd.get(11) == 0) ? CHEST : FRAGMENT;
			qs.giveItems(itemId, 1);
			qs.playSound(QuestState.SOUND_ITEMGET);
		}
		return super.onKill(npc, player, isSummon);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = player.getQuestState(qn);

		String htmltext = getNoQuestMsg();
		
		switch (qs.getState())
		{
			case STATE_CREATED:
			{
				final QuestState qst = player.getQuestState(Q138_TempleChampionPart2.class.getSimpleName());
				htmltext = ((qst != null) && qst.isCompleted()) ? "30896-01.htm" : "30896-00.html";
				break;
			}
			case STATE_STARTED:
			{
				switch (qs.getCond())
				{
					case 1:
					{
						htmltext = qs.isSet("talk") ? "30896-11.html" : "30896-05.html";
						break;
					}
					case 2:
					{
						htmltext = qs.isSet("talk") ? "30896-18.html" : ((qs.getQuestItemsCount(FRAGMENT) >= 10) && (qs.getQuestItemsCount(CHEST) >= 1)) ? "30896-15.html" : "30896-14.html";
						break;
					}
				}
				break;
			}
			case STATE_COMPLETED:
			{
				htmltext = getAlreadyCompletedMsg();
				break;
			}
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Q139_ShadowFoxPart1();
	}
}