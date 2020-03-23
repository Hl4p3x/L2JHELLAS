package com.l2jhellas.gameserver.model.entity.events;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.PackRoot;
import com.l2jhellas.Config;
import com.l2jhellas.gameserver.Announcements;
import com.l2jhellas.gameserver.datatables.sql.NpcData;
import com.l2jhellas.gameserver.datatables.xml.DoorData;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.L2Spawn;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.entity.events.engines.ZodiacMain;
import com.l2jhellas.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jhellas.gameserver.network.serverpackets.ExShowScreenMessage.SMPOS;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public class CaptureThem
{
	public static boolean CaptureThemRunning = false;
	private static int i;
	private static List<L2PcInstance> _players = new ArrayList<>();
	private static List<L2Npc> _flags = new ArrayList<>();
	private static int flagId = 36006;
	private static int MostPoints = 0;
	public static L2PcInstance MostPointsPlayer;
	private static int[] flagsx =
	{
		149506,
		149412,
		150025,
		148741,
		149999,
		150110,
		150121,
		149550,
		149235,
	};
	private static int[] flagsy =
	{
		47471,
		46144,
		46670,
		46750,
		46221,
		46464,
		46872,
		47105,
		47871,
	};
	private static int flagsz = -3413;
	
	public static void openRegistration()
	{
		CaptureThemRunning = true;
		ZodiacMain.ZodiacRegisterActive = true;
		Announcements.getInstance().announceToAll("CaptureThem Event has Started!");
		Announcements.getInstance().announceToAll("Type .join to enter or .leave to leave!");
		int minutes = Config.TIME_TO_REGISTER;
		Announcements.getInstance().announceToAll("You have " + minutes + " minutes to register!");
		waitSecs(minutes / 2 * 60);
		Announcements.getInstance().announceToAll("You have " + minutes / 2 + " minutes to register!");
		waitSecs(minutes / 2 * 60);
		stopRegistration();
	}
	
	public static void stopRegistration()
	{
		Announcements.getInstance().announceToAll("CaptureThem Registration is Over!");
		String Capture_Path = "data/html/zodiac/CaptureThem.htm";
		// TODO check this if work like that
		new File(PackRoot.DATAPACK_ROOT, Capture_Path);
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(Capture_Path);
		
		ZodiacMain.ZodiacRegisterActive = false;
		
		for (L2PcInstance players : L2World.getInstance().getAllPlayers().values())
		{
			if (players == null)
				continue;
			
			if (players.isinZodiac)
			{
				players.sendPacket(html);
				_players.add(players);
			}
		}
		
		waitSecs(20);
		if (_players != null)
			StartEvent();
		else
		{
			Announcements.getInstance().announceToAll("The event was canceled due to lack of players!");
			CaptureThemRunning = false;
		}
	}
	
	public static void StartEvent()
	{
		DoorData.getInstance().getDoor(24190001).closeMe();
		DoorData.getInstance().getDoor(24190002).closeMe();
		DoorData.getInstance().getDoor(24190003).closeMe();
		DoorData.getInstance().getDoor(24190004).closeMe();
		L2Npc flags = null;
		for (i = 0; i < 9; i++)
		{
			flags = addSpawn(flagId, flagsx[i], flagsy[i], flagsz);
			_flags.add(flags);
		}
		for (L2PcInstance players : _players)
		{
			players.ZodiacPoints = 0;
			players.OriginalColor = players.getAppearance().getNameColor();
			players.getAppearance().setNameColor(0xFF0000);
			players.OriginalTitle = players.getTitle();
			players.setTitle("CaptureThem");
			L2Skill skill;
			skill = SkillTable.getInstance().getInfo(1323, 1);
			skill.getEffects(players, players);
			players.broadcastUserInfo();
			players.teleToLocation(149527, 46684, -3413);
			players.sendMessage("The Event has Started! And will finish in 10 minutes");
			
		}
		Announcements.getInstance().announceToAll("You have 10 minutes until the event is over!");
		waitSecs(300);
		Announcements.getInstance().announceToAll("You have 5 minutes until the event is over!");
		waitSecs(180);
		Announcements.getInstance().announceToAll("You have 2 minutes until the event is over!");
		waitSecs(60);
		Announcements.getInstance().announceToAll("You have 1 minute until the event is over!");
		waitSecs(60);
		StopClean();
		
	}
	
	public static void StopClean()
	{
		for (L2PcInstance players : _players)
		{
			if (players.ZodiacPoints > MostPoints)
			{
				MostPointsPlayer = players;
				MostPoints = players.ZodiacPoints;
			}
		}
		if (MostPointsPlayer != null)
		{
			MostPointsPlayer.addItem("Reward", Config.ZODIAC_REWARD, Config.ZODIAC_REWARD_COUN, MostPointsPlayer, true);
			Announcements.getInstance().announceToAll("Winner of the event is " + MostPointsPlayer + " With " + MostPoints + " Points!");
		}
		for (L2PcInstance players : _players)
		{
			players.getAppearance().setNameColor(players.OriginalColor);
			players.setTitle(players.OriginalTitle);
			players.broadcastUserInfo();
			players.teleToLocation(82698, 148638, -3473);
			players.broadcastUserInfo();
			players.isinZodiac = false;
			players.sendMessage("The Event is officially finished!");
		}
		for (L2Npc flags : _flags)
		{
			flags.deleteMe();
			
		}
		DoorData.getInstance().getDoor(24190001).openMe();
		DoorData.getInstance().getDoor(24190002).openMe();
		DoorData.getInstance().getDoor(24190003).openMe();
		DoorData.getInstance().getDoor(24190004).openMe();
		_players.clear();
		_flags.clear();
		CaptureThemRunning = false;
		
	}
	
	public static void OnRevive(L2PcInstance player)
	{
		player.setCurrentHp(player.getMaxHp());
		player.setCurrentMp(player.getMaxMp());
		player.setCurrentCp(player.getMaxCp());
		L2Skill skill = SkillTable.getInstance().getInfo(1323, 1);
		skill.getEffects(player, player);
	}
	
	public static void onDeath(L2PcInstance player, L2PcInstance killer)
	{
		if (killer == null)
			return;
		if (killer.isinZodiac)
		{
			killer.ZodiacPoints++;
			killer.sendPacket(new ExShowScreenMessage("You have " + killer.ZodiacPoints + " Points.", 3000, SMPOS.BOTTOM_RIGHT, true));
		}
		if (player == null)
			return;
		player.teleToLocation(149722, 46700, -3413);
		player.doRevive();
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
	
	private static L2Npc addSpawn(int npcId, int x, int y, int z)
	{
		L2Npc result = null;
		try
		{
			L2NpcTemplate template = NpcData.getInstance().getTemplate(npcId);
			if (template != null)
			{
				L2Spawn spawn = new L2Spawn(template);
				spawn.setInstanceId(0);
				spawn.setHeading(1);
				spawn.setLocx(x);
				spawn.setLocy(y);
				spawn.setLocz(z);
				spawn.stopRespawn();
				result = spawn.spawnOne();
				return result;
			}
		}
		catch (Exception e1)
		{
		}
		return null;
	}
	
}