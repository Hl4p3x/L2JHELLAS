package com.l2jhellas.gameserver.model.entity.events;

import java.util.ArrayList;
import java.util.List;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.Announcements;
import com.l2jhellas.gameserver.datatables.sql.NpcData;
import com.l2jhellas.gameserver.datatables.xml.DoorData;
import com.l2jhellas.gameserver.model.L2Spawn;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.entity.events.engines.ZodiacMain;
import com.l2jhellas.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jhellas.gameserver.network.serverpackets.ExShowScreenMessage.SMPOS;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public class CastleWars
{
	private static List<L2PcInstance> _defenders = new ArrayList<>();
	private static List<L2PcInstance> _attackers = new ArrayList<>();
	private static List<L2Npc> _flags = new ArrayList<>();
	public static boolean isFinished;
	public static boolean CastleWarsRunning = false;
	private static boolean alaksokolies = false;
	private static int i;
	public static int flagskilled = 0;
	public static int defendersx = 77566;
	public static int defendersy = -152128;
	public static int defendersz = -545;
	public static int attackersx = 75471;
	public static int attackersy = -147011;
	public static int attackersz = -933;
	public static int[] flagslocx =
	{
		77545,
		76663,
		78446,
	};
	private static int[] flagslocy =
	{
		-149937,
		-154522,
		-154524,
	};
	private static int[] flagslocz =
	{
		345,
		128,
		235,
	};
	
	public static void openRegi()
	{
		ZodiacMain.ZodiacRegisterActive = true;
		Announcements.getInstance().announceToAll("CastleWars Event has Started!");
		Announcements.getInstance().announceToAll("Type .join to enter or .leave to leave!");
		int minutes = Config.TIME_TO_REGISTER;
		Announcements.getInstance().announceToAll("You have " + minutes + " minutes to register!");
		waitSecs(minutes / 2 * 60);
		Announcements.getInstance().announceToAll("You have " + minutes / 2 + " minutes to register!");
		waitSecs(minutes / 2 * 60);
		CastleWarsRunning = true;
		ZodiacMain.ZodiacRegisterActive = false;
		isFinished = false;
		stopRegi();
		preparecastle();
		shufflePlayers();
		teleportThem();
		if (!isFinished)
			Announcements.getInstance().announceToAll("You have 10 minutes until the event is over!");
		waitSecs(300);
		if (!isFinished)
			Announcements.getInstance().announceToAll("You have 5 minutes until the event is over!");
		waitSecs(180);
		if (!isFinished)
			Announcements.getInstance().announceToAll("You have 2 minutes until the event is over!");
		waitSecs(60);
		if (!isFinished)
			Announcements.getInstance().announceToAll("You have 1 minute until the event is over!");
		waitSecs(60);
		if (!isFinished)
		{
			defendersWin();
		}
	}
	
	public static void preparecastle()
	{
		DoorData.getInstance().getDoor(22130001).openMe();
		DoorData.getInstance().getDoor(22130002).openMe();
		DoorData.getInstance().getDoor(22130003).openMe();
		DoorData.getInstance().getDoor(22130004).openMe();
		DoorData.getInstance().getDoor(22130005).openMe();
		DoorData.getInstance().getDoor(22130010).openMe();
		DoorData.getInstance().getDoor(22130007).openMe();
		DoorData.getInstance().getDoor(22130011).openMe();
		DoorData.getInstance().getDoor(22130009).openMe();
		DoorData.getInstance().getDoor(22130008).openMe();
		DoorData.getInstance().getDoor(22130010).openMe();
		DoorData.getInstance().getDoor(22130006).openMe();
		L2Npc flags = null;
		for (i = 0; i < 3; i++)
		{
			flags = addSpawn(36006, flagslocx[i], flagslocy[i], flagslocz[i]);
			_flags.add(flags);
		}
	}
	
	public static void cleanevent()
	{
		for (L2PcInstance defender : _defenders)
		{
			defender.getAppearance().setNameColor(defender.OriginalColor);
			defender.setTitle(defender.OriginalTitle);
			defender.broadcastUserInfo();
			defender.teleToLocation(82724, 148307, -3469);
			defender.isinZodiac = false;
		}
		for (L2PcInstance attacker : _attackers)
		{
			attacker.getAppearance().setNameColor(attacker.OriginalColor);
			attacker.setTitle(attacker.OriginalTitle);
			attacker.broadcastUserInfo();
			attacker.teleToLocation(82724, 148307, -3469);
			attacker.isinZodiac = false;
		}
		for (L2Npc flags : _flags)
		{
			flags.deleteMe();
			
		}
		CastleWarsRunning = false;
		flagskilled = 0;
		_flags.clear();
		_defenders.clear();
		_attackers.clear();
		DoorData.getInstance().getDoor(22130001).closeMe();
		DoorData.getInstance().getDoor(22130002).closeMe();
		DoorData.getInstance().getDoor(22130003).closeMe();
		DoorData.getInstance().getDoor(22130004).closeMe();
		DoorData.getInstance().getDoor(22130005).closeMe();
		DoorData.getInstance().getDoor(22130010).closeMe();
		DoorData.getInstance().getDoor(22130007).closeMe();
		DoorData.getInstance().getDoor(22130011).closeMe();
		DoorData.getInstance().getDoor(22130009).closeMe();
		DoorData.getInstance().getDoor(22130008).closeMe();
		DoorData.getInstance().getDoor(22130010).closeMe();
		DoorData.getInstance().getDoor(22130006).closeMe();
	}
	
	public static void defendersWin()
	{
		Announcements.getInstance().announceToAll("The Defending side Won the event! They successfully protected the flags!");
		for (L2PcInstance defender : _defenders)
		{
			if (defender != null)
			{
				defender.sendMessage("Congratulations! Here is a reward for your effort!");
				defender.addItem("Reward", Config.ZODIAC_REWARD, Config.ZODIAC_REWARD_COUN, defender, true);
			}
		}
		isFinished = true;
		CastleWarsRunning = false;
		cleanevent();
	}
	
	public static boolean isattacker(L2PcInstance player)
	{
		
		return (_attackers.contains(player));
		
	}
	
	public static boolean isdefender(L2PcInstance player)
	{
		
		return (_defenders.contains(player));
		
	}
	
	public static void OnDeath(L2PcInstance player)
	{
		if (player != null)
		{
			if (isattacker(player))
			{
				
				player.teleToLocation(attackersx, attackersy, attackersz);
			}
			if (isdefender(player))
			{
				
				player.teleToLocation(defendersx, defendersy, defendersz);
				
			}
			
			player.doRevive();
		}
	}
	
	public static void OnRevive(L2PcInstance player)
	{
		player.sendMessage("You are revived in your spot");
	}
	
	public static void IncreaseKilledFlags()
	{
		flagskilled++;
		for (L2PcInstance player : _attackers)
		{
			player.sendPacket(new ExShowScreenMessage(+flagskilled + " Flags have been killed out of 3 .", 3000, SMPOS.TOP_CENTER, true));
		}
		for (L2PcInstance player : _defenders)
		{
			player.sendPacket(new ExShowScreenMessage(+flagskilled + " Flags have been killed out of 3 .", 3000, SMPOS.TOP_CENTER, true));
		}
		if (flagskilled == 3)
		{
			attackersWin();
		}
		
	}
	
	public static void attackersWin()
	{
		Announcements.getInstance().announceToAll(" The Attacking side Won the event! They successfully eliminated the flags!!");
		for (L2PcInstance attacker : _attackers)
		{
			attacker.sendMessage("Congratulations! Here is a reward for your effort!");
			attacker.addItem("Reward", Config.ZODIAC_REWARD, Config.ZODIAC_REWARD_COUN, attacker, true);
		}
		isFinished = true;
		CastleWarsRunning = false;
		cleanevent();
	}
	
	public static void teleportThem()
	{
		for (L2PcInstance defender : _defenders)
		{
			defender.teleToLocation(defendersx, defendersy, defendersz);
		}
		for (L2PcInstance attacker : _attackers)
		{
			attacker.teleToLocation(attackersx, attackersy, attackersz);
		}
	}
	
	public static void stopRegi()
	{
		Announcements.getInstance().announceToAll("Registrations are now over!");
	}
	
	public static void shufflePlayers()
	{
		for (L2PcInstance player : L2World.getInstance().getAllPlayers().values())
		{
			if (player.isinZodiac)
			{
				if (alaksokolies)
				{
					_defenders.add(player);
					player.OriginalColor = player.getAppearance().getNameColor();
					player.getAppearance().setNameColor(0xFF0000);
					player.OriginalTitle = player.getTitle();
					player.setTitle("Defender");
					player.broadcastUserInfo();
					alaksokolies = false;
				}
				else
				{
					_attackers.add(player);
					player.OriginalColor = player.getAppearance().getNameColor();
					player.getAppearance().setNameColor(0x0000FF);
					player.setTitle("Attacker");
					player.OriginalTitle = player.getTitle();
					player.broadcastUserInfo();
					alaksokolies = true;
				}
			}
		}
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