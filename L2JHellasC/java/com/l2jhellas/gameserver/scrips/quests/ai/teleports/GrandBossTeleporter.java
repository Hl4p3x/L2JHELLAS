package com.l2jhellas.gameserver.scrips.quests.ai.teleports;

import java.util.List;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.xml.DoorData;
import com.l2jhellas.gameserver.instancemanager.GrandBossManager;
import com.l2jhellas.gameserver.instancemanager.QuestManager;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.gameserver.model.zone.type.L2BossZone;
import com.l2jhellas.gameserver.scrips.quests.ai.invidual.Antharas;
import com.l2jhellas.gameserver.scrips.quests.ai.invidual.Baium;
import com.l2jhellas.gameserver.scrips.quests.ai.invidual.Sailren;
import com.l2jhellas.gameserver.scrips.quests.ai.invidual.Valakas;
import com.l2jhellas.util.Rnd;
import com.l2jhellas.util.Util;

public class GrandBossTeleporter extends Quest
{
	private static final String qn = "GrandBossTeleporter";
	
	private static final Location baiumTeleIn = new Location(113100, 14500, 10077);
	private static final Location[] baiumTeleOut =
	{
		new Location(108784, 16000, -4928),
		new Location(113824, 10448, -5164),
		new Location(115488, 22096, -5168)
	};
	
	private static final Location sailrenTeleIn = new Location(27333, -6835, -1970);
	private static final Location[] sailrenTeleOut =
	{
		new Location(10610, -24035, -3676),
		new Location(10703, -24041, -3673),
		new Location(10769, -24107, -3672)
	};
	
	public static int _valakasPlayersCount = 0;
	
	public GrandBossTeleporter()
	{
		super(-1, qn, "teleports");
		
		addFirstTalkId(29055, 31862,29061);
		addStartNpc(13001,29055, 31859, 31384, 31385, 31540, 31686, 31687, 31759, 31862, 32107, 32109);
		addTalkId(13001,29055, 31859, 31384, 31385, 31540, 31686, 31687, 31759, 31862, 32107, 32109);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(qn);
		if (st == null)
			st = newQuestState(player);
		
		if (event.equalsIgnoreCase("baium"))
		{
			// Player is mounted on a wyvern, cancel it.
			if (player.isFlying())
				htmltext = "31862-05.htm";
			// Player hasn't blooded fabric, cancel it.
			else if (!st.hasQuestItems(4295))
				htmltext = "31862-03.htm";
			// All is ok, take the item and teleport the player inside.
			else
			{
				st.takeItems(4295, 1);
				// allow entry for the player for the next 30 secs.
				GrandBossManager.getZoneById(110002).allowPlayerEntry(player, 30);
				player.teleToLocation(baiumTeleIn, false);
			}
		}
		else if (event.equalsIgnoreCase("baium_story"))
			htmltext = "31862-02.htm";
		else if (event.equalsIgnoreCase("baium_exit"))
			player.teleToLocation(baiumTeleOut[Rnd.get(baiumTeleOut.length)], true);
		else if (event.equalsIgnoreCase("31540"))
		{
			if (st.hasQuestItems(7267))
			{
				st.takeItems(7267, 1);
				player.teleToLocation(183813, -115157, -3303, false);
				st.set("allowEnter", "1");
			}
			else
				htmltext = "31540-06.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(qn);
		if (st == null)
			st = newQuestState(player);
		
		switch (npc.getNpcId())
		{
			case 29055:
				htmltext = "29055-01.htm";
				break;
			case 29061:
				player.teleToLocation(150037 + Rnd.get(400), -57720 + Rnd.get(400), -2976, false);
				break;
			case 31862:
				final int status = GrandBossManager.getInstance().getBossStatus(29020);
				if (status == Baium.AWAKE)
					htmltext = "31862-01.htm";
				else if (status == Baium.DEAD)
					htmltext = "31862-04.htm";
				else
					htmltext = "31862-00.htm";
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		if (st == null)
			return null;
		
		int status;
		switch (npc.getNpcId())
		{
			case 13001:
				status = GrandBossManager.getInstance().getBossStatus(Antharas.ANTHARAS);
				if (status == Antharas.FIGHTING)
					htmltext = "13001-02.htm";
				else if (status == Antharas.DEAD)
					htmltext = "13001-01.htm";
				else if (status == Antharas.DORMANT || status == Antharas.WAITING)
				{
					if (st.hasQuestItems(3865))
					{
						st.takeItems(3865, 1);
						GrandBossManager.getZoneById(110001).allowPlayerEntry(player, 30);
						
						player.teleToLocation(175300 + Rnd.get(-350, 350), 115180 + Rnd.get(-1000, 1000), -7709, false);
						
						if (status == Antharas.DORMANT)
						{
							GrandBossManager.setBossStatus(Antharas.ANTHARAS, Antharas.WAITING);
							QuestManager.getInstance().getQuest("Antharas").startQuestTimer("beginning", Config.Antharas_Wait_Time, null, null, false);
						}
					}
					else
						htmltext = "13001-03.htm";
				}
				break;
			case 31859:
				player.teleToLocation(79800 + Rnd.get(600), 151200 + Rnd.get(1100), -3534, false);
				break;
			
			case 31385:
				status = GrandBossManager.getInstance().getBossStatus(Valakas.VALAKAS);
				if (status == 0 || status == 1)
				{
					if (_valakasPlayersCount >= 200)
						htmltext = "31385-03.htm";
					else if (st.getInt("allowEnter") == 1)
					{
						st.unset("allowEnter");
						GrandBossManager.getZoneById(110010).allowPlayerEntry(player, 30);
						
						player.teleToLocation(204328, -111874, 70, true);
						
						_valakasPlayersCount++;
						
						if (status == Valakas.DORMANT)
						{
							L2GrandBossInstance valakas = GrandBossManager.getInstance().getBoss(Valakas.VALAKAS);
							GrandBossManager.setBossStatus(Valakas.VALAKAS, 1);
							QuestManager.getInstance().getQuest("Valakas").startQuestTimer("beginning", Config.Valakas_Wait_Time, valakas, null, false);
						}
					}
					else
						htmltext = "31385-04.htm";
				}
				else if (status == 2)
					htmltext = "31385-02.htm";
				else
					htmltext = "31385-01.htm";
				break;
			case 31384:
				DoorData.getInstance().getDoor(24210004).openMe();
				break;
			
			case 31686:
				DoorData.getInstance().getDoor(24210006).openMe();
				break;
			
			case 31687:
				DoorData.getInstance().getDoor(24210005).openMe();
				break;
			
			case 31540:
				if (_valakasPlayersCount < 50)
					htmltext = "31540-01.htm";
				else if (_valakasPlayersCount < 100)
					htmltext = "31540-02.htm";
				else if (_valakasPlayersCount < 150)
					htmltext = "31540-03.htm";
				else if (_valakasPlayersCount < 200)
					htmltext = "31540-04.htm";
				else
					htmltext = "31540-05.htm";
				break;
			
			case 31759:
				player.teleToLocation(150037, -57720, -2976, true);
				break;				
			case 32107:
				player.teleToLocation(sailrenTeleOut[Rnd.get(sailrenTeleOut.length)], true);
				break;
			
			case 32109:
				if (!player.isInParty())
					htmltext = "32109-03.htm";
				else if (!player.getParty().isLeader(player))
					htmltext = "32109-01.htm";
				else
				{
					if (st.hasQuestItems(8784))
					{
						status = GrandBossManager.getInstance().getBossStatus(Sailren.SAILREN);
						if (status == Sailren.DORMANT)
						{
							final List<L2PcInstance> party = player.getParty().getPartyMembers();
							
							// Check players conditions.
							for (L2PcInstance member : party)
							{
								if (member.getLevel() < 70)
									return "32109-06.htm";
								
								if (!Util.checkIfInRange(1000, player, member, true))
									return "32109-07.htm";
							}
							
							// Take item from party leader.
							st.takeItems(8784, 1);
							
							final L2BossZone nest = GrandBossManager.getZoneById(110015);
							
							// Teleport players.
							for (L2PcInstance member : party)
							{
								if (nest != null)
								{
									nest.allowPlayerEntry(member, 30);
									member.teleToLocation(sailrenTeleIn, true);
								}
							}
							GrandBossManager.setBossStatus(Sailren.SAILREN, Sailren.FIGHTING);
							QuestManager.getInstance().getQuest("Sailren").startQuestTimer("beginning", 60000, null, null, false);
						}
						else if (status == Sailren.DEAD)
							htmltext = "32109-04.htm";
						else
							htmltext = "32109-05.htm";
					}
					else
						htmltext = "32109-02.htm";
				}
				break;
		}
		
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new GrandBossTeleporter();
	}
}