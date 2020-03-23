package com.l2jhellas.gameserver.scrips.quests;

import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.gameserver.model.zone.L2ZoneType;

public class Q636_TruthBeyondTheGate extends Quest
{
	private static final String qn = "Q636_TruthBeyondTheGate";
	
	// NPCs
	private static final int ELIYAH = 31329;
	private static final int FLAURON = 32010;
	
	// Reward
	private static final int VISITOR_MARK = 8064;
	private static final int FADED_VISITOR_MARK = 8065;
	
	public Q636_TruthBeyondTheGate()
	{
		super(636, qn, "The Truth Beyond the Gate");
		
		addStartNpc(ELIYAH);
		addTalkId(ELIYAH, FLAURON);
		addEnterZoneId(100000);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31329-04.htm"))
		{
			st.set("cond", "1");
			st.setState(STATE_STARTED);
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("32010-02.htm"))
		{
			st.giveItems(VISITOR_MARK, 1);
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(false);
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
				if (player.getLevel() > 72)
					htmltext = "31329-02.htm";
				else
				{
					htmltext = "31329-01.htm";
					st.exitQuest(true);
				}
				break;
			
			case STATE_STARTED:
				switch (npc.getNpcId())
				{
					case ELIYAH:
						htmltext = "31329-05.htm";
						break;
					
					case FLAURON:
						if (st.hasQuestItems(VISITOR_MARK))
							htmltext = "32010-03.htm";
						else
							htmltext = "32010-01.htm";
						break;
				}
				break;
			
			case STATE_COMPLETED:
				htmltext = getAlreadyCompletedMsg();
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public final String onEnterZone(L2Character character, L2ZoneType zone)
	{
		// QuestState already null on enter because quest is finished
		if (character instanceof L2PcInstance)
		{
			if (character.getActingPlayer().destroyItemByItemId("Mark", VISITOR_MARK, 1, character, false))
				character.getActingPlayer().addItem("Mark", FADED_VISITOR_MARK, 1, character, true);
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new Q636_TruthBeyondTheGate();
	}
}