package com.l2jhellas.gameserver.scrips.quests.ai.custom;

import java.util.HashMap;
import java.util.Map;

import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.spawn.SpawnData;
import com.l2jhellas.util.Util;

public class RaidbossInfo extends Quest
{
	private static final String qn = "RaidbossInfo";
	
	private static final Map<Integer, Location> RADARS = new HashMap<>();
	
	private static final int[] NPCs =
	{
		31729,
		31730,
		31731,
		31732,
		31733,
		31734,
		31735,
		31736,
		31737,
		31738,
		31739,
		31740,
		31741,
		31742,
		31743,
		31744,
		31745,
		31746,
		31747,
		31748,
		31749,
		31750,
		31751,
		31752,
		31753,
		31754,
		31755,
		31756,
		31757,
		31758,
		31759,
		31760,
		31761,
		31762,
		31763,
		31764,
		31765,
		31766,
		31767,
		31768,
		31769,
		31770,
		31771,
		31772,
		31773,
		31774,
		31775,
		31776,
		31777,
		31778,
		31779,
		31780,
		31781,
		31782,
		31783,
		31784,
		31785,
		31786,
		31787,
		31788,
		31789,
		31790,
		31791,
		31792,
		31793,
		31794,
		31795,
		31796,
		31797,
		31798,
		31799,
		31800,
		31801,
		31802,
		31803,
		31804,
		31805,
		31806,
		31807,
		31808,
		31809,
		31810,
		31811,
		31812,
		31813,
		31814,
		31815,
		31816,
		31817,
		31818,
		31819,
		31820,
		31821,
		31822,
		31823,
		31824,
		31825,
		31826,
		31827,
		31828,
		31829,
		31830,
		31831,
		31832,
		31833,
		31834,
		31835,
		31836,
		31837,
		31838,
		31839,
		31840,
		31841
	};
	
	public RaidbossInfo()
	{
		super(-1, qn, "custom");
		
		for (int npcId : NPCs)
		{
			addStartNpc(npcId);
			addTalkId(npcId);
		}
		
		SpawnData.getInstance().forEachSpawn(sp ->
		{
			if (sp != null && sp.getLastSpawn() != null && sp.getLastSpawn().getTemplate().isType("L2RaidBoss"))
				 RADARS.put(sp.getNpcid(), new Location(sp.getLocx(), sp.getLocy(), sp.getLocz()));
			return true;
		});
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (!Util.isDigit(event))
			return event;
		
		final Location loc = RADARS.get(Integer.parseInt(event));
		if (loc != null)
			player.getRadar().addMarker(loc.getX(), loc.getY(), loc.getZ());
		
		return null;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		return "info.htm";
	}
	
	public static void main(String args[])
	{
		new RaidbossInfo();
	}
}