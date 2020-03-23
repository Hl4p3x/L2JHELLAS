package com.l2jhellas.gameserver.model.entity.events;

import java.util.ArrayList;
import java.util.List;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.Announcements;
import com.l2jhellas.gameserver.datatables.sql.NpcData;
import com.l2jhellas.gameserver.model.L2Spawn;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.entity.events.engines.ZodiacMain;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public class PeloponnesianWar
{
	private static boolean alaksokolies = true;
	public static boolean PeloRunning = false, continuez, continuez2;
	private static int i, countA, countS;
	private static List<L2PcInstance> _participants = new ArrayList<>();
	private static List<L2PcInstance> _athenians = new ArrayList<>();
	private static List<L2PcInstance> _spartans = new ArrayList<>();
	private static List<L2Npc> _protectors = new ArrayList<>();
	private static int athenianx = 72923;
	private static int atheniany = 142764;
	private static int athenianz = -3773;
	private static int spartanx = -87891;
	private static int spartany = 142198;
	private static int spartanz = -3646;
	private static int protectors = 30627;
	private static int[] protectorsx =
	{
		72931,
		72995,
		73011,
		73070,
		-87921,
		-87998,
		-87908,
		-87836,
	};
	private static int[] protectorsy =
	{
		143270,
		143250,
		142272,
		142282,
		142707,
		142679,
		141758,
		141777,
	};
	private static int[] protectorsz =
	{
		-3773,
		-3773,
		-3773,
		-3773,
		-3646,
		-3646,
		-3646,
		-3646,
	};
	
	public static void startevent()
	{
		PeloRunning = true;
		ZodiacMain.ZodiacRegisterActive = true;
		Announcements.getInstance().announceToAll("Peloponnesian War Event has Started!");
		Announcements.getInstance().announceToAll("Type .join to enter or .leave to leave!");
		int minutes = Config.TIME_TO_REGISTER;
		Announcements.getInstance().announceToAll("You have " + minutes + " minutes to register!");
		waitSecs(minutes / 2 * 60);
		Announcements.getInstance().announceToAll("You have " + minutes / 2 + " minutes to register!");
		waitSecs(minutes / 2 * 60);
		ZodiacMain.ZodiacRegisterActive = false;
		for (L2PcInstance player : L2World.getInstance().getAllPlayers().values())
		{
			if (player.isinZodiac)
			{
				_participants.add(player);
				if (alaksokolies)
				{
					_spartans.add(player);
					player.OriginalTitle = player.getTitle();
					player.setTitle("Spartan");
					player.OriginalColor = player.getAppearance().getNameColor();
					player.getAppearance().setNameColor(0x0000FF);
					player.broadcastUserInfo();
					alaksokolies = false;
				}
				else
				{
					_athenians.add(player);
					player.OriginalTitle = player.getTitle();
					player.setTitle("Athenian");
					player.OriginalColor = player.getAppearance().getNameColor();
					player.getAppearance().setNameColor(0xFF0000);
					player.broadcastUserInfo();
					alaksokolies = true;
				}
				
			}
		}
		if (_participants.size() < 2)
		{
			Announcements.getInstance().announceToAll("Event canceled due to lack of participation.");
			cleanthemess();
			return;
		}
		spawnProtectors();
		teleportplayers();
		Announcements.getInstance().announceToAll("Civil War round started!");
		Announcements.getInstance().announceToAll("You have 3 minutes to fight! The one who survives will face the opponent race!!");
		waitSecs(180);
		for (L2PcInstance finals : _athenians)
		{
			if (finals.isinZodiac)
				countA++;
		}
		for (L2PcInstance finals : _spartans)
		{
			if (finals.isinZodiac)
				countS++;
		}
		if (countA > 1)
		{
			Announcements.getInstance().announceToAll("Athenians cheated and tried to send more than two players. Spartans won!");
			reward(false);
		}
		else if (countS > 1)
		{
			Announcements.getInstance().announceToAll("Spartans cheated and tried to send more than two players Athenians won!");
			reward(true);
		}
		else
		{
			finalround();
			Announcements.getInstance().announceToAll("Spartans and Athenians representative have 2 minutes to face each other");
			waitSecs(120);
			checkwhowon();
		}
		cleanthemess();
	}
	
	public static void cleanthemess()
	{
		for (L2PcInstance participant : _participants)
		{
			if (participant.isinZodiac)
			{
				participant.setTitle(participant.OriginalTitle);
				participant.getAppearance().setNameColor(participant.OriginalColor);
				participant.broadcastUserInfo();
				participant.teleToLocation(82698, 148638, -3473);
			}
			participant.isinZodiac = false;
		}
		for (L2Npc protectors : _protectors)
		{
			protectors.deleteMe();
		}
		continuez = false;
		continuez2 = false;
		PeloRunning = false;
		_protectors.clear();
		_participants.clear();
		_athenians.clear();
		_spartans.clear();
	}
	
	private static void reward(boolean Athenians)
	{
		if (Athenians)
		{
			for (L2PcInstance athenians : _athenians)
			{
				
				if (athenians != null)
					athenians.addItem("Reward", Config.ZODIAC_REWARD, Config.ZODIAC_REWARD_COUN, athenians, true);
				
			}
		}
		else
		{
			for (L2PcInstance spartans : _spartans)
			{
				if (spartans != null)
					spartans.addItem("Reward", Config.ZODIAC_REWARD, Config.ZODIAC_REWARD_COUN, spartans, true);
				
			}
		}
	}
	
	public static void checkwhowon()
	{
		for (L2PcInstance winner : _participants)
		{
			if (winner.isinZodiac && _athenians.contains(winner))
				continuez = true;
			if (winner.isinZodiac && _spartans.contains(winner))
				continuez2 = true;
			if (continuez && continuez2)
				Announcements.getInstance().announceToAll("Both representatives are alive blame them for not getting a reward!");
			else if (continuez)
			{
				Announcements.getInstance().announceToAll("Athenians won!");
				reward(true);
			}
			else if (continuez2)
			{
				Announcements.getInstance().announceToAll("Spartans won!");
				reward(false);
			}
		}
	}
	
	public static void finalround()
	{
		for (L2PcInstance athenian : _athenians)
		{
			if (athenian.isinZodiac)
			{
				athenian.sendMessage("You are the best of the Athenians. You will now face the best of the Spartans.");
				athenian.setTitle("FinalAthenian");
				athenian.setCurrentHp(athenian.getMaxHp());
				athenian.setCurrentMp(athenian.getMaxMp());
				athenian.setCurrentCp(athenian.getMaxCp());
				athenian.broadcastUserInfo();
				athenian.teleToLocation(83522, 259003, -11676);
			}
		}
		for (L2PcInstance spartan : _spartans)
		{
			if (spartan.isinZodiac)
			{
				spartan.sendMessage("You are the best of the Spartans. You will now face the best of the Athenians.");
				spartan.setTitle("FinalSpartan");
				spartan.setCurrentHp(spartan.getMaxHp());
				spartan.setCurrentMp(spartan.getMaxMp());
				spartan.setCurrentCp(spartan.getMaxCp());
				spartan.broadcastUserInfo();
				spartan.teleToLocation(87522, 254940, -11676);
			}
		}
	}
	
	public static void teleportplayers()
	{
		for (L2PcInstance player : _participants)
		{
			if (player == null)
				return;
			
			if (_athenians.contains(player))
				player.teleToLocation(athenianx, atheniany, athenianz);
			else
				player.teleToLocation(spartanx, spartany, spartanz);
		}
	}
	
	public static void onDeath(L2PcInstance player)
	{
		player.isinZodiac = false;
		player.teleToLocation(82698, 148638, -3473);
		player.getAppearance().setNameColor(0xFFFFFF);
		player.setTitle("");
		player.broadcastUserInfo();
		player.doRevive();
	}
	
	public static void spawnProtectors()
	{
		L2Npc protector = null;
		for (i = 0; i < 8; i++)
		{
			protector = addSpawn(protectors, protectorsx[i], protectorsy[i], protectorsz[i]);
			_protectors.add(protector);
		}
	}
	
	public static void RemoveAthenian(String name)
	{
		for (L2PcInstance player : _athenians)
			if (player.getName() == name)
				_athenians.remove(player);
	}
	
	public static void RemoveSpartan(String name)
	{
		for (L2PcInstance player : _spartans)
			if (player.getName() == name)
				_spartans.remove(player);
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