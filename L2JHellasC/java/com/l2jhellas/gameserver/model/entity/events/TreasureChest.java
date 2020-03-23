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
import com.l2jhellas.util.Rnd;

public class TreasureChest
{
	public static boolean TreasureRunning = false;
	private static int LuckyChest = 0, Counter = 0;
	private static List<L2PcInstance> _players = new ArrayList<>();
	public static List<L2Npc> _Npcs = new ArrayList<>();
	private static int x = 87377, y = 20459, z = -5270, i;
	
	public static void registration()
	{
		ZodiacMain.ZodiacRegisterActive = true;
		Announcements.getInstance().announceToAll("TreasureChest Event has Started!");
		Announcements.getInstance().announceToAll("Type .join to enter or .leave to leave!");
		int minutes = Config.TIME_TO_REGISTER;
		Announcements.getInstance().announceToAll("You have " + minutes + " minutes to register!");
		waitSecs(minutes / 2 * 60);
		Announcements.getInstance().announceToAll("You have " + minutes / 2 + " minutes to register!");
		waitSecs(minutes / 2 * 60);
		for (L2PcInstance players : L2World.getInstance().getAllPlayers().values())
		{
			if (players.isinZodiac)
				_players.add(players);
		}
		if (_players != null)
			startevent();
		else
			Announcements.getInstance().announceToAll("Event was cancelled due to lack of participation!");
		
	}
	
	public static void startevent()
	{
		ZodiacMain.ZodiacRegisterActive = false;
		TreasureRunning = true;
		for (L2PcInstance players : _players)
		{
			if (players == null)
				continue;
			
			players.teleToLocation(x, y, z, true);
			players.sendMessage("Kill as many chest as you can!");
		}
		L2Npc npcs = null;
		LuckyChest = Rnd.get(39);
		for (i = 0; i < 40; i++)
		{
			npcs = addSpawn(18286, x + Rnd.get(-500, +500), y + Rnd.get(-500, +500), z);
			_Npcs.add(npcs);
		}
	}
	
	public static void onDeath(L2PcInstance player)
	{
		player.teleToLocation(x, y, z);
		player.doRevive();
		
	}
	
	public static void onRevive(L2PcInstance player)
	{
		player.setCurrentHp(player.getMaxHp());
		player.setCurrentMp(player.getMaxMp());
		player.setCurrentCp(player.getMaxCp());
		L2Skill skill = SkillTable.getInstance().getInfo(1323, 1);
		skill.getEffects(player, player);
	}
	
	public static void LuckyOne(L2PcInstance killer)
	{
		Counter++;
		if (Counter == LuckyChest)
		{
			Announcements.getInstance().announceToAll(killer + " killed the lucky chest!");
			killer.addItem("Reward", Config.ZODIAC_REWARD, Config.ZODIAC_REWARD_COUN, killer, true);
			cleanthemess();
		}
	}
	
	public static void cleanthemess()
	{
		for (L2PcInstance players : _players)
		{
			players.teleToLocation(83225, 148068, -3430, true);
		}
		for (L2Npc npc : _Npcs)
		{
			npc.deleteMe();
		}
		Counter = 0;
		LuckyChest = 0;
		TreasureRunning = false;
		_players.clear();
		_Npcs.clear();
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