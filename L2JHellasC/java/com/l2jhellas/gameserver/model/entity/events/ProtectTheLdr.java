package com.l2jhellas.gameserver.model.entity.events;

import java.util.ArrayList;
import java.util.List;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.Announcements;
import com.l2jhellas.gameserver.datatables.sql.NpcData;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.L2Spawn;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.entity.events.engines.ZodiacMain;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public class ProtectTheLdr
{
	public static List<L2PcInstance> _Team1 = new ArrayList<>();
	public static List<L2PcInstance> _Team2 = new ArrayList<>();
	public static int team1x = 86535;
	public static int team1y = 257189;
	public static int team1z = -11672;
	private static int leader1 = 36007;
	public static int team2x = 86504, team2y = 258865, team2z = -11672, leader2 = 36008;
	private static int leader2x = 86402, leader2y = 258733, leader2z = -11672;
	private static int leader1x = 86417, leader1y = 257244, leader1z = -11672;
	public static boolean ProtectisRunning = false, alaksokolies;
	private static List<L2Npc> _leaders = new ArrayList<>();
	
	public static void startevent()
	{
		ZodiacMain.ZodiacRegisterActive = true;
		Announcements.getInstance().announceToAll("ProtectTheLeader Event has Started!");
		Announcements.getInstance().announceToAll("Type .join to enter or .leave to leave!");
		int minutes = Config.TIME_TO_REGISTER;
		Announcements.getInstance().announceToAll("You have " + minutes + " minutes to register!");
		waitSecs(minutes / 2 * 60);
		Announcements.getInstance().announceToAll("You have " + minutes / 2 + " minutes to register!");
		waitSecs(minutes / 2 * 60);
		ZodiacMain.ZodiacRegisterActive = false;
		ProtectisRunning = true;
		Announcements.getInstance().announceToAll("Registration are now over!");
		shufflePlayers();
		teleportplayers();
		L2Npc spawn1 = null;
		L2Npc spawn2 = null;
		spawn1 = addSpawn(leader1, leader1x, leader1y, leader1z);
		spawn2 = addSpawn(leader2, leader2x, leader2y, leader2z);
		_leaders.add(spawn1);
		_leaders.add(spawn2);
		Announcements.getInstance().announceToAll("Go kill the enemy's Leader rb!");
	}
	
	public static void team1wins()
	{
		Announcements.getInstance().announceToAll("Team 1 won! The leader of team 2 is dead!");
		for (L2PcInstance member : _Team1)
		{
			
			member.sendMessage("Congratulations! The enemy leader is dead!");
			member.addItem("Reward", Config.ZODIAC_REWARD, Config.ZODIAC_REWARD_COUN, member, true);
		}
		cleanthemess();
	}
	
	public static void team2wins()
	{
		Announcements.getInstance().announceToAll("Team 2 won! The leader of team 1 is dead!");
		for (L2PcInstance member : _Team2)
		{
			
			member.sendMessage("Congratulations! The enemy leader is dead!");
			member.addItem("Reward", Config.ZODIAC_REWARD, Config.ZODIAC_REWARD_COUN, member, true);
		}
		cleanthemess();
	}
	
	public static void cleanthemess()
	{
		for (L2PcInstance member1 : _Team1)
		{
			member1.getAppearance().setNameColor(member1.OriginalColor);
			member1.setTitle(member1.OriginalTitle);
			member1.broadcastUserInfo();
			member1.isinZodiac = false;
			member1.teleToLocation(82743, 148219, -3470);
		}
		for (L2PcInstance member2 : _Team2)
		{
			member2.getAppearance().setNameColor(member2.OriginalColor);
			member2.setTitle(member2.OriginalTitle);
			member2.broadcastUserInfo();
			member2.isinZodiac = false;
			member2.teleToLocation(82743, 148219, -3470);
		}
		for (L2Npc leader : _leaders)
		{
			leader.deleteMe();
			
		}
		ProtectisRunning = false;
	}
	
	public static void teleportplayers()
	{
		for (L2PcInstance member : _Team1)
		{
			member.teleToLocation(team1x, team1y, team1z);
		}
		for (L2PcInstance member : _Team2)
		{
			member.teleToLocation(team2x, team2y, team2z);
		}
	}
	
	public static void shufflePlayers()
	{
		for (L2PcInstance player : L2World.getInstance().getAllPlayers().values())
		{
			if (player.isinZodiac)
			{
				if (alaksokolies)
				{
					_Team1.add(player);
					player.OriginalColor = player.getAppearance().getNameColor();
					player.getAppearance().setNameColor(0xFF0000);
					player.OriginalTitle = player.getTitle();
					player.setTitle("Team1");
					player.broadcastUserInfo();
					alaksokolies = false;
				}
				else
				{
					_Team2.add(player);
					player.OriginalColor = player.getAppearance().getNameColor();
					player.getAppearance().setNameColor(0x0000FF);
					player.OriginalTitle = player.getTitle();
					player.setTitle("Team2");
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
	
	public static void onDeath(L2PcInstance player)
	{
		if (ProtectTheLdr._Team1.contains(player))
		{
			player.teleToLocation(ProtectTheLdr.team1x, ProtectTheLdr.team1y, ProtectTheLdr.team1z);
		}
		if (ProtectTheLdr._Team2.contains(player))
		{
			player.teleToLocation(ProtectTheLdr.team2x, ProtectTheLdr.team2y, ProtectTheLdr.team2z);
		}
		player.doRevive();
		
	}
	
	public static void OnRevive(L2PcInstance player)
	{
		player.getStatus().setCurrentHp(player.getMaxHp());
		player.getStatus().setCurrentMp(player.getMaxMp());
		player.getStatus().setCurrentCp(player.getMaxCp());
		L2Skill skill;
		skill = SkillTable.getInstance().getInfo(1323, 1);
		skill.getEffects(player, player);
	}
}