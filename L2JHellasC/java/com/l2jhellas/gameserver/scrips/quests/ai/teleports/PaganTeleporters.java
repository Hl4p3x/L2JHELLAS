package com.l2jhellas.gameserver.scrips.quests.ai.teleports;

import com.l2jhellas.gameserver.datatables.xml.DoorData;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class PaganTeleporters extends Quest
{
	// Items
	private final static int VISITOR_MARK = 8064;
	private final static int PAGAN_MARK = 8067;
	
	public PaganTeleporters()
	{
		super(-1, "PaganTeleporters", "teleports");
		
		addStartNpc(32034, 32035, 32036, 32037, 32039, 32040);
		addTalkId(32034, 32035, 32036, 32037, 32039, 32040);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("Close_Door1"))
			DoorData.getInstance().getDoor(19160001).closeMe();
		else if (event.equalsIgnoreCase("Close_Door2"))
		{
			DoorData.getInstance().getDoor(19160010).closeMe();
			DoorData.getInstance().getDoor(19160011).closeMe();
		}
		return null;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		if (st == null)
			return htmltext;
		
		switch (npc.getNpcId())
		{
			case 32034:
				if (st.hasQuestItems(VISITOR_MARK) || st.hasQuestItems(PAGAN_MARK))
				{
					DoorData.getInstance().getDoor(19160001).openMe();
					startQuestTimer("Close_Door1", 10000, npc, player, false);
					htmltext = "FadedMark.htm";
				}
				else
				{
					htmltext = "32034-1.htm";
					st.exitQuest(true);
				}
				break;
			
			case 32035:
				DoorData.getInstance().getDoor(19160001).openMe();
				startQuestTimer("Close_Door1", 10000, npc, player, false);
				htmltext = "FadedMark.htm";
				break;
			
			case 32036:
				if (!st.hasQuestItems(PAGAN_MARK))
					htmltext = "32036-1.htm";
				else
				{
					DoorData.getInstance().getDoor(19160010).openMe();
					DoorData.getInstance().getDoor(19160011).openMe();
					startQuestTimer("Close_Door2", 10000, npc, player, false);
					htmltext = "32036-2.htm";
				}
				break;
			
			case 32037:
				DoorData.getInstance().getDoor(19160010).openMe();
				DoorData.getInstance().getDoor(19160011).openMe();
				startQuestTimer("Close_Door2", 10000, npc, player, false);
				htmltext = "FadedMark.htm";
				break;
			
			case 32039:
				player.teleToLocation(-12766, -35840, -10856, false);
				break;
			
			case 32040:
				player.teleToLocation(34962, -49758, -763, false);
				break;
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new PaganTeleporters();
	}
}