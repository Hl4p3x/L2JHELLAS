package com.l2jhellas.gameserver.scrips.quests.ai.custom;

import com.l2jhellas.gameserver.instancemanager.QuestManager;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.gameserver.scrips.quests.Q141_ShadowFoxPart3;
import com.l2jhellas.gameserver.scrips.quests.Q142_FallenAngelRequestOfDawn;
import com.l2jhellas.gameserver.scrips.quests.Q143_FallenAngelRequestOfDusk;

public class Q998_FallenAngelSelect extends Quest
{
	private static final String qn = "Q998_FallenAngelSelect";

	private static final int NATOOLS = 30894;
	private static final int MIN_LEVEL = 38;
	
	public Q998_FallenAngelSelect()
	{
		super(-1, qn, "custom");
		addStartNpc(NATOOLS);
		addTalkId(NATOOLS);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(qn);

		if (st == null)
			return null;
		
		switch (event)
		{
			case "30894-01.html":
			case "30894-02.html":
			case "30894-03.html":
			{
				return event;
			}
			case "dawn":
			{
				startQuest(Q142_FallenAngelRequestOfDawn.class.getSimpleName(), player);
				break;
			}
			case "dusk":
			{
				startQuest(Q143_FallenAngelRequestOfDusk.class.getSimpleName(), player);
				break;
			}
		}
		return null;
	}
	
	private void startQuest(String name, L2PcInstance player)
	{
		final Quest q = QuestManager.getInstance().getQuest(name);
		if (q != null)
		{
			q.newQuestState(player);
			q.notifyEvent("30894-01.html", null, player);
			player.getQuestState(getName()).setState(STATE_COMPLETED);
		}
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(qn);

		final QuestState qs = player.getQuestState(Q141_ShadowFoxPart3.class.getSimpleName());
		
		if (!st.isStarted())
			return getNoQuestMsg();

		return ((player.getLevel() >= MIN_LEVEL) && qs.isCompleted()) ? "30894-01.html" : "30894-00.html";
	}
	
	public static void main(String[] args)
	{
		new Q998_FallenAngelSelect();
	}
}