package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import com.l2jhellas.gameserver.datatables.sql.NpcData;
import com.l2jhellas.gameserver.datatables.sql.SpawnTable;
import com.l2jhellas.gameserver.datatables.xml.AdminData;
import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.instancemanager.DayNightSpawnManager;
import com.l2jhellas.gameserver.instancemanager.RaidBossSpawnManager;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Spawn;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public class AdminSpawn implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_show_spawns",
		"admin_spawn",
		"admin_spawn_monster",
		"admin_spawn_index",
		"admin_npc_index",
		"admin_spawn_once",
		"admin_show_npcs",
		"admin_spawnnight",
		"admin_spawnday"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.equals("admin_show_spawns"))
		{
			AdminHelpPage.showHelpPage(activeChar, "spawns.htm");
		}
		else if (command.startsWith("admin_spawn_index"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			try
			{
				st.nextToken();
				int level = Integer.parseInt(st.nextToken());
				int from = 0;
				try
				{
					from = Integer.parseInt(st.nextToken());
				}
				catch (NoSuchElementException nsee)
				{
				}
				showMonsters(activeChar, level, from);
			}
			catch (Exception e)
			{
				AdminHelpPage.showHelpPage(activeChar, "spawns.htm");
			}
		}
		else if (command.equals("admin_show_npcs"))
		{
			AdminHelpPage.showHelpPage(activeChar, "npcs.htm");
		}
		else if (command.startsWith("admin_npc_index"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			try
			{
				st.nextToken();
				String letter = st.nextToken();
				int from = 0;
				try
				{
					from = Integer.parseInt(st.nextToken());
				}
				catch (NoSuchElementException nsee)
				{
				}
				showNpcs(activeChar, letter, from);
			}
			catch (Exception e)
			{
				AdminHelpPage.showHelpPage(activeChar, "npcs.htm");
			}
		}
		else if (command.startsWith("admin_spawn") || command.startsWith("admin_spawn_monster"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			try
			{
				String cmd = st.nextToken();
				String id = st.nextToken();
				int respawnTime = 0;
				int mobCount = 1;
				
				if (st.hasMoreTokens())
					mobCount = Integer.parseInt(st.nextToken());
				if (st.hasMoreTokens())
					respawnTime = Integer.parseInt(st.nextToken());
				
				if (cmd.equalsIgnoreCase("admin_spawn_once"))
					spawnMonster(activeChar, id, respawnTime, mobCount, false);
				else
					spawnMonster(activeChar, id, respawnTime, mobCount, true);
			}
			catch (Exception e)
			{ // Case of wrong or missing monster data
				AdminHelpPage.showHelpPage(activeChar, "spawns.htm");
			}
		}
		else if (command.startsWith("admin_spawnday"))
		{
			DayNightSpawnManager.getInstance().spawnCreatures(false);	
			AdminData.getInstance().broadcastMessageToGMs("Spawning day creatures.");
		}
		else if (command.startsWith("admin_spawnnight"))
		{
			DayNightSpawnManager.getInstance().spawnCreatures(true);	
			AdminData.getInstance().broadcastMessageToGMs("Spawning night creatures.");
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private static void spawnMonster(L2PcInstance activeChar, String monsterId, int respawnTime, int mobCount, boolean permanent)
	{
		L2Object target = activeChar.getTarget();
		if (target == null)
		{
			target = activeChar;
		}
		if (target != activeChar)
			return;
		
		L2NpcTemplate template1;
		if (monsterId.matches("[0-9]*"))
		{
			// First parameter was an ID number
			int monsterTemplate = Integer.parseInt(monsterId);
			template1 = NpcData.getInstance().getTemplate(monsterTemplate);
		}
		else
		{
			// First parameter wasn't just numbers so go by name not ID
			monsterId = monsterId.replace('_', ' ');
			template1 = NpcData.getInstance().getTemplateByName(monsterId);
		}
		
		try
		{
			L2Spawn spawn = new L2Spawn(template1);
			spawn.setLocx(target.getX());
			spawn.setLocy(target.getY());
			spawn.setLocz(target.getZ());
			spawn.setAmount(mobCount);
			spawn.setHeading(activeChar.getHeading());
			spawn.setRespawnDelay(respawnTime);
			if (RaidBossSpawnManager.getInstance().isDefined(spawn.getNpcid()))
			{
				activeChar.sendMessage("You cannot spawn another instance of " + template1.name + ".");
			}
			else
			{
				if (RaidBossSpawnManager.getInstance().getValidTemplate(spawn.getNpcid()) != null)
					RaidBossSpawnManager.getInstance().addNewSpawn(spawn, 0, template1.getStatsSet().getDouble("baseHpMax"), template1.getStatsSet().getDouble("baseMpMax"), permanent);
				else
					SpawnTable.getInstance().addNewSpawn(spawn, permanent);

				spawn.init();
				
				if (!permanent)
					spawn.stopRespawn();
				
				activeChar.sendMessage("Created " + template1.name + " on " + target.getObjectId());
			}
		}
		catch (Exception e)
		{
			activeChar.sendPacket(SystemMessageId.TARGET_CANT_FOUND);
		}
	}
	
	private static void showMonsters(L2PcInstance activeChar, int level, int from)
	{
		StringBuilder tb = new StringBuilder();
		
		L2NpcTemplate[] mobs = NpcData.getInstance().getAllMonstersOfLevel(level);
		
		// Start
		tb.append("<html><title>Spawn Monster:</title><body><p> Level " + level + ":<br>Total Npc's : " + mobs.length + "<br>");
		String end1 = "<br><center><button value=\"Next\" action=\"bypass -h admin_spawn_index " + level + " $from$\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>";
		String end2 = "<br><center><button value=\"Back\" action=\"bypass -h admin_show_spawns\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>";
		
		// Loop
		boolean ended = true;
		for (int i = from; i < mobs.length; i++)
		{
			String txt = "<a action=\"bypass -h admin_spawn_monster " + mobs[i].npcId + "\">" + mobs[i].name + "</a><br1>";
			
			if ((tb.length() + txt.length() + end2.length()) > 8192)
			{
				end1 = end1.replace("$from$", "" + i);
				ended = false;
				break;
			}
			
			tb.append(txt);
		}
		
		// End
		if (ended)
			tb.append(end2);
		else
			tb.append(end1);
		
		activeChar.sendPacket(new NpcHtmlMessage(5, tb.toString()));
	}
	
	private static void showNpcs(L2PcInstance activeChar, String starting, int from)
	{
		StringBuilder tb = new StringBuilder();
		L2NpcTemplate[] mobs = NpcData.getInstance().getAllNpcStartingWith(starting);
		// Start
		tb.append("<html><title>Spawn Monster:</title><body><p> There are " + mobs.length + " Npcs whose name starts with " + starting + ":<br>");
		String end1 = "<br><center><button value=\"Next\" action=\"bypass -h admin_npc_index " + starting + " $from$\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>";
		String end2 = "<br><center><button value=\"Back\" action=\"bypass -h admin_show_npcs\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>";
		// Loop
		boolean ended = true;
		for (int i = from; i < mobs.length; i++)
		{
			String txt = "<a action=\"bypass -h admin_spawn_monster " + mobs[i].npcId + "\">" + mobs[i].name + "</a><br1>";
			
			if ((tb.length() + txt.length() + end2.length()) > 8192)
			{
				end1 = end1.replace("$from$", "" + i);
				ended = false;
				break;
			}
			tb.append(txt);
		}
		// End
		if (ended)
			tb.append(end2);
		else
			tb.append(end1);

		activeChar.sendPacket(new NpcHtmlMessage(5, tb.toString()));
	}
}