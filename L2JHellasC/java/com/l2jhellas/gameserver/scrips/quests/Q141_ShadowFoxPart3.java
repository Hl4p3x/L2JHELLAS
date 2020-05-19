package com.l2jhellas.gameserver.scrips.quests;

import java.util.HashMap;
import java.util.Map;

import com.l2jhellas.gameserver.instancemanager.QuestManager;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.gameserver.scrips.quests.ai.custom.Q998_FallenAngelSelect;
import com.l2jhellas.util.Rnd;

public class Q141_ShadowFoxPart3 extends Quest
{
	private static final String qn = "Q141_ShadowFoxPart3";
	
	private static final int NATOOLS = 30894;

	private static final Map<Integer, Integer> MOBS = new HashMap<>();
	static
	{
		MOBS.put(20135, 53); 
		MOBS.put(20791, 100);
		MOBS.put(20792, 92); 
	}
	
	private static final int PREDECESSORS_REPORT = 10350;

	private static final int MIN_LEVEL = 37;
	private static final int REPORT_COUNT = 30;
	
	public Q141_ShadowFoxPart3()
	{
		super(141,qn,"Shadow Fox Part3");
		addStartNpc(NATOOLS);
		addTalkId(NATOOLS);
		addKillId(MOBS.keySet());
		setItemsIds(PREDECESSORS_REPORT);
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
			case "30894-05.html":
			case "30894-10.html":
			case "30894-11.html":
			case "30894-12.html":
			case "30894-13.html":
			case "30894-14.html":
			case "30894-16.html":
			case "30894-17.html":
			case "30894-19.html":
			case "30894-20.html":
			{
				break;
			}
			case "30894-03.htm":
			{
				qs.setState(STATE_STARTED);
				qs.set("cond", "1");
				qs.playSound(QuestState.SOUND_ACCEPT);
				break;
			}
			case "30894-06.html":
			{
				qs.set("cond", "2");
				break;
			}
			case "30894-15.html":
			{
				qs.set("talk", "2");
				break;
			}
			case "30894-18.html":
			{
				qs.set("cond", "4");
				qs.unset("talk");
				break;
			}
			case "30894-21.html":
			{
				qs.exitQuest(false);
				
				final Quest q = QuestManager.getInstance().getQuest(Q998_FallenAngelSelect.class.getSimpleName());
				if (q != null)
					q.newQuestState(player).setState(STATE_STARTED);
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
		final L2PcInstance member = getRandomPartyMember(player, npc);
		
		if (member == null)
			return super.onKill(npc, player, isSummon);
		
		final QuestState qs = member.getQuestState(qn);

	     if(qs == null)
	 		return super.onKill(npc, player, isSummon);
	     
		if (Rnd.get(100) < MOBS.get(npc.getNpcId()))
		{
			qs.giveItems(PREDECESSORS_REPORT, 1);
			if (qs.getQuestItemsCount(PREDECESSORS_REPORT) >= REPORT_COUNT)
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
		
		switch (qs.getState())
		{
			case STATE_CREATED:
			{
				final QuestState qst = player.getQuestState(Q140_ShadowFoxPart2.class.getSimpleName());
				htmltext = (player.getLevel() >= MIN_LEVEL) ? ((qst != null) && qst.isCompleted()) ? "30894-01.htm" : "30894-00.html" : "30894-02.htm";
				break;
			}
			case STATE_STARTED:
			{
				switch (qs.getCond())
				{
					case 1:
					{
						htmltext = "30894-04.html";
						break;
					}
					case 2:
					{
						htmltext = "30894-07.html";
						break;
					}
					case 3:
					{
						if (qs.getInt("talk") == 1)
							htmltext = "30894-09.html";
						else if (qs.getInt("talk") == 2)
							htmltext = "30894-16.html";
						else
						{
							htmltext = "30894-08.html";
							qs.takeItems(PREDECESSORS_REPORT, -1);
							qs.set("talk", "1");
						}
						break;
					}
					case 4:
					{
						htmltext = "30894-19.html";
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
		new Q141_ShadowFoxPart3();
	}
}