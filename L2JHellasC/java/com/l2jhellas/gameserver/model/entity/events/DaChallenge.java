package com.l2jhellas.gameserver.model.entity.events;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.PackRoot;
import com.l2jhellas.Config;
import com.l2jhellas.gameserver.Announcements;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.datatables.sql.NpcData;
import com.l2jhellas.gameserver.datatables.xml.DoorData;
import com.l2jhellas.gameserver.enums.player.ChatType;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.L2Spawn;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.entity.events.engines.ZodiacMain;
import com.l2jhellas.gameserver.network.serverpackets.CreatureSay;
import com.l2jhellas.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jhellas.gameserver.network.serverpackets.ExShowScreenMessage.SMPOS;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;
import com.l2jhellas.util.Rnd;

public class DaChallenge
{
	public static byte RoomCount;
	public static boolean ChallengeRunning;
	private static List<L2PcInstance> _Participants = new ArrayList<>();
	private static List<L2Npc> _RoomMobs = new ArrayList<>();
	
	public static void StartEvent()
	{
		ChallengeRunning = true;
		ZodiacMain.ZodiacRegisterActive = true;
		Announcements.getInstance().announceToAll("Boss Event has Started!");
		Announcements.getInstance().announceToAll("Type .join to enter or .leave to leave!");
		int minutes = Config.TIME_TO_REGISTER;
		Announcements.getInstance().announceToAll("You have " + minutes + " minutes to register!");
		waitSecs(minutes / 2 * 60);
		Announcements.getInstance().announceToAll("You have " + minutes / 2 + " minutes to register!");
		waitSecs(minutes / 2 * 60);
		ZodiacMain.ZodiacRegisterActive = false;
		if (_Participants.size() < -1)
		{
			Announcements.getInstance().announceToAll("Boss event has been canceled due to low participants");
			Announcements.getInstance().announceToAll("Min: 10 , Participating: " + _Participants.size());
			_Participants.clear();
			ChallengeRunning = false;
			return;
		}
		PrepareAndTele();
		waitSecs(10);
		SpawnRoom1();
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				ClearDaMess(false);
			}
		}, 60 * 20 * 1000);
	}
	
	private static void SpawnRoom1()
	{
		int i;
		for (i = 0; i < 40; i++)
		{
			_RoomMobs.add(addSpawn(36009, 174105 + Rnd.get(-60, +60), -76113 + Rnd.get(-60, +60), -5109));
		}
		_RoomMobs.add(addSpawn(36010, 174069, -79936, -5109));
		RoomCount = 0;
		
	}
	
	private static void PrepareAndTele()
	{
		for (L2PcInstance player : L2World.getInstance().getAllPlayers().values())
		{
			if (player.isinZodiac)
			{
				_Participants.add(player);
				player.OriginalColor = player.getAppearance().getNameColor();
				player.OriginalTitle = player.getTitle();
				player.setTitle("Challenger");
				player.getAppearance().setNameColor(0x0000FF);
				player.broadcastUserInfo();
			}
		}
		for (L2PcInstance part : _Participants)
		{
			String Capture_Path = "data/html/zodiac/DaChallenge.htm";
			new File(PackRoot.DATAPACK_ROOT, Capture_Path);
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			html.setFile(Capture_Path);
			part.sendPacket(html);
		}
		waitSecs(60);
		for (L2PcInstance part : _Participants)
		{
			L2Skill skill;
			skill = SkillTable.getInstance().getInfo(1323, 1);
			skill.getEffects(part, part);
			part.teleToLocation(174105, -76113, -5109, true);
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
	
	public static void PrepareRoom2()
	{
		int i;
		RoomCount = 0;
		for (i = 0; i < 40; i++)
		{
			_RoomMobs.add(addSpawn(36011, 174136 + Rnd.get(-60, +60), -81636 + Rnd.get(-60, +60), -5125));
		}
		for (L2PcInstance player : _Participants)
		{
			player.sendPacket(new ExShowScreenMessage("Guard1 is Dead. The door will open in 10 seconds!", 4500, SMPOS.TOP_CENTER, true));
		}
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				DoorData.getInstance().getDoor(25150043).openMe();
			}
		}, 10 * 1000);
		
	}
	
	public static void Daboss()
	{
		for (L2PcInstance player : _Participants)
		{
			player.sendPacket(new ExShowScreenMessage("Go and kill DaBoss!!!!!!!", 4500, SMPOS.TOP_CENTER, true));
		}
		final L2Npc Daboss = addSpawn(36013, 174238, -88013, -5116);
		_RoomMobs.add(Daboss);
		DoorData.getInstance().getDoor(25150046).openMe();
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				final CreatureSay cs = new CreatureSay(Daboss.getObjectId(), ChatType.SHOUT.getClientId(), "DaRealBoss", "Welcome to your doom Challengers!");
				Daboss.broadcastPacket(cs, 1000);
			}
		}, 8 * 1000);
		
	}
	
	public static void Room1Cleared()
	{
		for (L2PcInstance player : _Participants)
		{
			player.sendPacket(new ExShowScreenMessage("Room 1 Cleared Move to the next room", 4500, SMPOS.TOP_CENTER, true));
		}
		DoorData.getInstance().getDoor(25150053).openMe();
		DaChallenge.RoomCount = 0;
		waitSecs(5);
		DoorData.getInstance().getDoor(25150042).openMe();
		
	}
	
	public static void SpawnFinalGuard()
	{
		for (L2PcInstance player : _Participants)
		{
			player.sendPacket(new ExShowScreenMessage("Room 2 Cleared! Procced to kill the final guard to let you in the final boss!", 4500, SMPOS.TOP_CENTER, true));
		}
		DoorData.getInstance().getDoor(25150045).openMe();
		_RoomMobs.add(addSpawn(36012, 174249, -85754, -5109));
	}
	
	public static void ClearDaMess(boolean Reward)
	{
		if (!ChallengeRunning)
			return;
		ChallengeRunning = false;
		DoorData.getInstance().getDoor(25150043).closeMe();
		DoorData.getInstance().getDoor(25150053).closeMe();
		DoorData.getInstance().getDoor(25150045).closeMe();
		DoorData.getInstance().getDoor(25150046).closeMe();
		DoorData.getInstance().getDoor(25150068).closeMe();
		DoorData.getInstance().getDoor(25150063).closeMe();
		for (L2Npc mob : _RoomMobs)
		{
			mob.deleteMe();
		}
		for (L2PcInstance player : _Participants)
		{
			player.teleToLocation(82813, 148205, -3470, true);
			player.getAppearance().setNameColor(player.OriginalColor);
			player.setTitle(player.OriginalTitle);
			if (Reward)
				player.addItem("Reward", Config.ZODIAC_REWARD, Config.ZODIAC_REWARD_COUN, player, true);
			
		}
		if (!Reward)
			Announcements.getInstance().announceToAll("Players weren't able to finish the challenge within 20 minutes!");
		
	}
	
	public static void SpawnExtraMobs()
	{
		
		DoorData.getInstance().getDoor(25150068).openMe();
		DoorData.getInstance().getDoor(25150063).openMe();
		int i;
		for (i = 0; i < 10; i++)
		{
			_RoomMobs.add(addSpawn(36011, 174833, -81787, -5074));
			_RoomMobs.add(addSpawn(36011, 173287, -81701, -5074));
		}
		for (L2PcInstance player : _Participants)
		{
			player.sendPacket(new ExShowScreenMessage("Look out! More Enemies!!!", 4500, SMPOS.BOTTOM_RIGHT, true));
		}
	}
	
	public static void LastOne()
	{
		final L2Npc Last = addSpawn(36014, 174238, -88013, -5116);
		_RoomMobs.add(Last);
		CreatureSay cs = new CreatureSay(Last.getObjectId(), ChatType.SHOUT.getClientId(), "DaRealBoss", "You thought it was over?!? HAHAHAHA");
		Last.broadcastPacket(cs, 1000);
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				final CreatureSay cs1 = new CreatureSay(Last.getObjectId(), ChatType.SHOUT.getClientId(), "DaRealBoss", "You will never kill me Challengers!!!");
				Last.broadcastPacket(cs1, 1000);
			}
		}, 5 * 1000);
	}
	
	public static void onRevive(L2PcInstance player)
	{
		player.getStatus().setCurrentHp(player.getMaxHp());
		player.getStatus().setCurrentMp(player.getMaxMp());
		player.getStatus().setCurrentCp(player.getMaxCp());
		L2Skill skill;
		skill = SkillTable.getInstance().getInfo(1323, 1);
		skill.getEffects(player, player);
		
	}
	
	public static void onDeath(L2PcInstance player)
	{
		player.teleToLocation(174105, -76113, -5109, true);
		player.doRevive();
		
	}
}