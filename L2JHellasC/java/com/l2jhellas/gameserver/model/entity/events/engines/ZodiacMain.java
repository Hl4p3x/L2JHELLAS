package com.l2jhellas.gameserver.model.entity.events.engines;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.Announcements;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.entity.events.CTF;
import com.l2jhellas.gameserver.model.entity.events.CaptureThem;
import com.l2jhellas.gameserver.model.entity.events.CastleWars;
import com.l2jhellas.gameserver.model.entity.events.ChaosEvent;
import com.l2jhellas.gameserver.model.entity.events.DM;
import com.l2jhellas.gameserver.model.entity.events.DaChallenge;
import com.l2jhellas.gameserver.model.entity.events.PeloponnesianWar;
import com.l2jhellas.gameserver.model.entity.events.ProtectTheLdr;
import com.l2jhellas.gameserver.model.entity.events.TreasureChest;
import com.l2jhellas.gameserver.model.entity.events.TvT;
import com.l2jhellas.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jhellas.gameserver.network.serverpackets.ExShowScreenMessage.SMPOS;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;

public class ZodiacMain
{
	static final Logger _log = Logger.getLogger(ZodiacMain.class.getName());
	public static List<String> Ips = new ArrayList<>();
	public static boolean ZodiacRegisterActive;
	public static int i, max;
	public static List<String> VotedPlayers = new ArrayList<>();
	public static int[] count =
	{
		0,
		0,
		0,
		0,
		0,
		0,
		0
	};
	
	public static boolean voting;
	
	public static void ZodiacIn()
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				startVoting();
			}
		}, 60 * 1000 * Config.INITIAL_START);
	}
	
	public static void startVoting()
	{
		
		if (EventsZodiacAreRunning())
		{
			Announcements.getInstance().announceToAll("Another event of zodiac engine is running. The zodiac engine will start when the event is completed.");
			voting = false;
			return;
		}
		
		_log.info(ZodiacMain.class.getSimpleName() + ": Zodiac Event Engine Started.");
		
		voting = true;
		for (L2PcInstance players : L2World.getInstance().getAllPlayers().values())
		{
			if (players != null)
				showHtmlWindow(players);
		}
		final int minutes = Config.ZODIAC_VOTE_MINUTES;
		Announcements.getInstance().announceToAll("You have " + minutes + " minutes to vote for the event you like!");
		waitSecs(minutes / 2 * 60);
		Announcements.getInstance().announceToAll("You have " + minutes / 2 + " minutes to vote for the event you like!");
		waitSecs(minutes / 2 * 60);
		voting = false;
		endit();
		
	}
	
	private static void ExecuteEvent(int Id)
	{
		switch (Id)
		{
			case 0:
				PeloponnesianWar.startevent();
				break;
			case 1:
				CaptureThem.openRegistration();
				break;
			case 2:
				CastleWars.openRegi();
				break;
			case 3:
				ProtectTheLdr.startevent();
				break;
			case 4:
				TreasureChest.registration();
				break;
			case 5:
				ChaosEvent.registration();
				break;
			case 6:
				DaChallenge.StartEvent();
				break;
			default:
				Announcements.getInstance().announceToAll("Not enough votes. The event is canceled. Next event starts in " + Config.BETWEEN_EVENTS + " Minutes!");
				break;
		
		}
	}
	
	public static void showHtmlWindow(L2PcInstance activeChar)
	{
		NpcHtmlMessage nhm = new NpcHtmlMessage(5);
		StringBuilder tb = new StringBuilder("");
		tb.append("<html><head><title>Zodiac Event Voting</title></head><body>");
		tb.append("<center>");
		tb.append("<td valign=\"top\"><font color=\"FF6600\">Vote for the event you want to play!</font>");
		tb.append("<br>");
		tb.append("<img src=\"SSQ_dungeon_T.SSQ_fire1_e013\" width=256 height=64>");
		tb.append("<br1>");
		tb.append("<img src=\"L2UI.SquareWhite\" width=194 height=1>");
		tb.append("<table bgcolor=333333 width=204>");
		tb.append("<tr>");
		tb.append("<td><center><button value=\"CaptureThem\" action=\"bypass -h CaptureThem\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></center></td>");
		tb.append("</tr>");
		tb.append("	<tr>");
		tb.append("<td><center><button value=\"Peloponnesian\" action=\"bypass -h PeloponnesianWar\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></center></td>");
		tb.append("</tr>");
		tb.append("<tr>");
		tb.append("<td><center><button value=\"CastleWars\" action=\"bypass -h CastleWars\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></center></td>");
		tb.append("</tr>");
		tb.append("<tr>");
		tb.append("<td><center><button value=\"ProtectTheLdr\" action=\"bypass -h ProtectTheLdr\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></center></td>");
		tb.append("</tr>");
		tb.append("<tr>");
		tb.append("<td><center><button value=\"TreasureChest\" action=\"bypass -h TreasureChest\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></center></td>");
		tb.append("</tr>");
		tb.append("<tr>");
		tb.append("<td><center><button value=\"Chaos Event\" action=\"bypass -h ChaosEvent\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></center></td>");
		tb.append("</tr>");
		tb.append("<tr>");
		tb.append("<td><center><button value=\"BossChallenge\" action=\"bypass -h Challenge\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></center></td>");
		tb.append("</tr>");
		tb.append("</table>");
		tb.append("<font color=\"FF6600\">By Boorinio!</font>");
		tb.append("</center>");
		tb.append("</body></html>");
		nhm.setHtml(tb.toString());
		activeChar.sendPacket(nhm);
	}
	
	public static void showFinalWindow(L2PcInstance player)
	{
		
		NpcHtmlMessage nhm = new NpcHtmlMessage(5);
		StringBuilder tb = new StringBuilder("");
		tb.append("<html><title>Zodiac Event Engine</title><body>Current Votes:<br>");
		tb.append("Current Votes:<br><br>");
		tb.append("Peloponnesian War: " + count[0] + "<br>");
		tb.append("Capture Them: " + count[1] + "<br>");
		tb.append("Castle Wars: " + count[2] + "<br>");
		tb.append("Treasure Chests: " + count[4] + "<br>");
		tb.append("Protect The Leader: " + count[3] + "<br>");
		tb.append("Chaos Event: " + count[5] + "<br>");
		tb.append("Boss Challenge: " + count[6] + "<br><br>");
		tb.append("</body></html>");
		nhm.setHtml(tb.toString());
		player.sendPacket(nhm);
	}
	
	public static void endit()
	{
		max = -1;
		for (i = 0; i < 7; i++)
		{
			if (count[i] > max && !(count[i] == 0))
				max = i;
		}
		ExecuteEvent(max);
		for (i = 0; i < 7; i++)
		{
			count[i] = 0;
		}
		ClearVotedPlayers();
		waitSecs((Config.BETWEEN_EVENTS * 60) + Config.TIME_TO_REGISTER + 10);
		startVoting();
	}
	
	public static void AddVotedPlayer(L2PcInstance player)
	{
		if (player != null)
			VotedPlayers.add(player.getName());
	}
	
	public static boolean HasVoted(L2PcInstance player)
	{
		if (player != null && VotedPlayers.contains(player.getName()))
			return true;
		
		return false;
	}
	
	public static void ClearVotedPlayers()
	{
		if (VotedPlayers.size() > 0)
			VotedPlayers.clear();
	}
	
	public static void OnDeath(L2PcInstance player, L2PcInstance killer)
	{
		if (CaptureThem.CaptureThemRunning)
			CaptureThem.onDeath(player, killer);
		if (PeloponnesianWar.PeloRunning)
			PeloponnesianWar.onDeath(player);
		if (CastleWars.CastleWarsRunning)
			CastleWars.OnDeath(player);
		if (ChaosEvent._isChaosActive)
			ChaosEvent.onDeath(player, killer);
		if (TreasureChest.TreasureRunning)
			TreasureChest.onDeath(player);
		if (ProtectTheLdr.ProtectisRunning)
			ProtectTheLdr.onDeath(player);
		if (DaChallenge.ChallengeRunning)
			DaChallenge.onDeath(player);
		
	}
	
	public static void OnKillNpc(L2Npc npc, L2Character killer)
	{
		if (npc.getNpcId() == 18286 && TreasureChest.TreasureRunning)
		{
			TreasureChest.LuckyOne((L2PcInstance) killer);
		}
		if (npc.getNpcId() == 36006)
		{
			if (!CastleWars.isFinished && CastleWars.CastleWarsRunning)
			{
				CastleWars.IncreaseKilledFlags();
			}
			if (CaptureThem.CaptureThemRunning)
			{
				((L2PcInstance) killer).ZodiacPoints = ((L2PcInstance) killer).ZodiacPoints + 10;
				killer.sendPacket(new ExShowScreenMessage("You have " + killer.getActingPlayer().ZodiacPoints + " Points.", 3000, SMPOS.BOTTOM_RIGHT, true));
			}
		}
		if (ProtectTheLdr.ProtectisRunning)
		{
			if (npc.getNpcId() == 36007)
			{
				ProtectTheLdr.team2wins();
			}
			if (npc.getNpcId() == 36008)
			{
				ProtectTheLdr.team1wins();
			}
		}
		if (DaChallenge.ChallengeRunning)
		{
			if (npc.getNpcId() == 36009)
			{
				DaChallenge.RoomCount++;
				if (DaChallenge.RoomCount == 40)
				{
					DaChallenge.Room1Cleared();
				}
			}
			if (npc.getNpcId() == 36010)
			{
				DaChallenge.PrepareRoom2();
			}
			if (npc.getNpcId() == 36011)
			{
				DaChallenge.RoomCount++;
				if (DaChallenge.RoomCount == 10)
				{
					DaChallenge.SpawnExtraMobs();
				}
				if (DaChallenge.RoomCount == 60)
				{
					DaChallenge.SpawnFinalGuard();
					
					DaChallenge.RoomCount = 0;
				}
				
			}
			if (npc.getNpcId() == 36012)
			{
				DaChallenge.Daboss();
			}
			if (npc.getNpcId() == 36013)
			{
				DaChallenge.LastOne();
			}
			if (npc.getNpcId() == 36014)
			{
				DaChallenge.ClearDaMess(true);
			}
		}
	}
	
	public static void OnRevive(L2PcInstance player)
	{
		if (player != null)
		{
			if (CaptureThem.CaptureThemRunning)
				CaptureThem.OnRevive(player);
			if (CastleWars.CastleWarsRunning)
				CastleWars.OnRevive(player);
			if (ProtectTheLdr.ProtectisRunning)
				ProtectTheLdr.OnRevive(player);
			if (ChaosEvent._isChaosActive)
				ChaosEvent.onRevive(player);
			if (TreasureChest.TreasureRunning)
				TreasureChest.onRevive(player);
			if (DaChallenge.ChallengeRunning)
				DaChallenge.onRevive(player);
		}
	}
	
	public static boolean isEligible(L2PcInstance player, String ip)
	{
		
		if (player.isinZodiac)
		{
			player.sendMessage("You are Already participating in a Zodiac Event.");
			return false;
		}
		if (!ZodiacRegisterActive)
		{
			player.sendMessage("Zodiac's Registration is offline!");
			return false;
		}
		if (player.getLevel() < 76)
		{
			player.sendMessage("You are lower than 76 levels.");
			return false;
		}
		if (player.isInJail())
		{
			player.sendMessage("You are in jail. You cannot participate in events.");
			return false;
		}
		if (player.isInOlympiadMode())
		{
			player.sendMessage("You are in olympiad mode.");
			return false;
		}
		if (hasbots(ip) && Config.SAME_IP_ZODIAC)
		{
			player.sendMessage("You have already joinned with that ip.");
			return false;
		}
		return true;
	}
	
	public static void waitSecs(int i)
	{
		try
		{
			Thread.sleep(i * 1000);
		}
		catch (InterruptedException ie)
		{
			ie.printStackTrace();
		}
	}
	
	public static boolean hasbots(String ip)
	{
		return Ips.contains(ip) ? true : false;
	}
	
	// full events
	public boolean AllEventsAreRunning()
	{
		return DaChallenge.ChallengeRunning || PeloponnesianWar.PeloRunning || CaptureThem.CaptureThemRunning || CastleWars.CastleWarsRunning || ChaosEvent._isChaosActive || CTF._started || CTF._joining || DM._joining || DM._started || ProtectTheLdr.ProtectisRunning || TreasureChest.TreasureRunning || TvT._joining || TvT._started;
	}
	
	// Only zodiac events
	public static boolean EventsZodiacAreRunning()
	{
		return DaChallenge.ChallengeRunning || PeloponnesianWar.PeloRunning || CaptureThem.CaptureThemRunning || CastleWars.CastleWarsRunning || ChaosEvent._isChaosActive || ProtectTheLdr.ProtectisRunning || TreasureChest.TreasureRunning;
	}
}