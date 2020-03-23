package com.l2jhellas.gameserver.scrips.quests.ai.teleports;

import java.util.HashMap;
import java.util.Map;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;

public class NewbieTravelToken extends Quest
{
	private final static Map<String, int[]> data = new HashMap<>();
	{
		data.put("30600", new int[]
		{
			12160,
			16554,
			-4583
		}); // DE
		data.put("30601", new int[]
		{
			115594,
			-177993,
			-912
		}); // DW
		data.put("30599", new int[]
		{
			45470,
			48328,
			-3059
		}); // EV
		data.put("30602", new int[]
		{
			-45067,
			-113563,
			-199
		}); // OV
		data.put("30598", new int[]
		{
			-84053,
			243343,
			-3729
		}); // TI
	}
	
	private final static int TOKEN = 8542;
	
	public NewbieTravelToken()
	{
		super(-1, "NewbieTravelToken", "teleports");
		
		addStartNpc(30598, 30599, 30600, 30601, 30602);
		addTalkId(30598, 30599, 30600, 30601, 30602);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
			st = newQuestState(player);
		
		if (data.containsKey(event))
		{
			int x = data.get(event)[0];
			int y = data.get(event)[1];
			int z = data.get(event)[2];
			
			if (st.getQuestItemsCount(TOKEN) != 0)
			{
				st.takeItems(TOKEN, 1);
				st.getPlayer().teleToLocation(x, y, z, false);
			}
			else
				return "notoken.htm";
		}
		st.exitQuest(true);
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		int npcId = npc.getNpcId();
		
		if (player.getLevel() >= 20)
		{
			htmltext = "wronglevel.htm";
			st.exitQuest(true);
		}
		else
			htmltext = npcId + ".htm";
		
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new NewbieTravelToken();
	}
}