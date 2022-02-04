package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import java.util.StringTokenizer;

import Extensions.Balancer.BalanceLoad;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.SevenSigns;
import com.l2jhellas.gameserver.cache.CrestCache;
import com.l2jhellas.gameserver.cache.HtmCache;
import com.l2jhellas.gameserver.controllers.TradeController;
import com.l2jhellas.gameserver.datatables.sql.ItemTable;
import com.l2jhellas.gameserver.datatables.sql.NpcData;
import com.l2jhellas.gameserver.datatables.xml.AdminData;
import com.l2jhellas.gameserver.datatables.xml.AugmentationData;
import com.l2jhellas.gameserver.datatables.xml.DoorData;
import com.l2jhellas.gameserver.datatables.xml.MultisellData;
import com.l2jhellas.gameserver.datatables.xml.NpcWalkerRoutesData;
import com.l2jhellas.gameserver.datatables.xml.SkillSpellbookData;
import com.l2jhellas.gameserver.datatables.xml.SkillTreeData;
import com.l2jhellas.gameserver.datatables.xml.SummonItemsData;
import com.l2jhellas.gameserver.datatables.xml.TeleportLocationData;
import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.instancemanager.CursedWeaponsManager;
import com.l2jhellas.gameserver.instancemanager.DayNightSpawnManager;
import com.l2jhellas.gameserver.instancemanager.Manager;
import com.l2jhellas.gameserver.instancemanager.QuestManager;
import com.l2jhellas.gameserver.instancemanager.RaidBossSpawnManager;
import com.l2jhellas.gameserver.instancemanager.ZoneManager;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.spawn.SpawnData;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.scrips.loaders.ScriptLoader;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.util.Util;

public class AdminReload implements IAdminCommandHandler
{
	private static final String RELOAD_USAGE = "Usage: //reload <multisell|doors|teleport|npc|respawn_npcs|zone|htm|crest|fix_crest|items|access|instancemanager|npcwalkers|configs|tradelist|pccolor|cw|levelupdata|summonitems|balancer|skill|sktrees|spellbooks|augment>";
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_reload"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command, " ");
		final String actualCommand = st.nextToken();
		if (actualCommand.equalsIgnoreCase("admin_reload"))
		{
			if (!st.hasMoreTokens())
			{
				sendReloadPage(activeChar);
				activeChar.sendMessage(RELOAD_USAGE);
				return true;
			}
			
			final String type = st.nextToken();
			switch (type.toLowerCase())
			{
				case "multisell":
				{
					MultisellData.getInstance().reload();
					sendReloadPage(activeChar);
					activeChar.sendMessage("Multisells has been reloaded.");
					break;
				}
				case "doors":
				{
					DoorData.getInstance().reloadAll();
					sendReloadPage(activeChar);
					activeChar.sendMessage("Door data reloaded.");
					break;
				}
				case "teleport":
				{
					TeleportLocationData.getInstance().load();
					sendReloadPage(activeChar);
					activeChar.sendMessage("Teleport locations has been reloaded.");
					break;
				}
				case "npc":
				{
					NpcData.getInstance().reloadAllNpc();
					QuestManager.getInstance().cleanQuests();					
					ScriptLoader.teleiwne();
					QuestManager.getInstance().report();
					sendReloadPage(activeChar);
					activeChar.sendMessage("NPCs and Scripts have been reloaded.");
					break;
				}
				case "respawn_npcs":
				{
					for (L2PcInstance player : L2World.getInstance().getAllPlayers().values())
					{
						if (player == null)
							continue;
						player.sendPacket(SystemMessageId.NPC_SERVER_NOT_OPERATING);
					}
					RaidBossSpawnManager.getInstance().cleanUp();
					DayNightSpawnManager.getInstance().cleanUp();
					L2World.getInstance().deleteVisibleNpcSpawns();
					AdminData.getInstance().broadcastMessageToGMs("NPC Unspawn completed!");
					// now respawn all
					NpcData.getInstance().reloadAllNpc();
					SpawnData.getInstance().reloadAll();
					RaidBossSpawnManager.getInstance().reloadBosses();
					SevenSigns.getInstance().spawnSevenSignsNPC();
					DayNightSpawnManager.getInstance().notifyChangeMode();
					AdminData.getInstance().broadcastMessageToGMs("NPC Respawn completed!");
					activeChar.sendMessage("All NPCs have been reloaded.");
					sendReloadPage(activeChar);
					break;
				}
				case "zone":
				{
					ZoneManager.getInstance().reload();
					sendReloadPage(activeChar);
					break;
				}
				case "htm":
				{
					HtmCache.getInstance().reload();
					sendReloadPage(activeChar);
					activeChar.sendMessage("The HTM cache has been reloaded.");
					break;
				}
				case "crest":
				{
					CrestCache.load();
					sendReloadPage(activeChar);
					activeChar.sendMessage("Crests have been reloaded.");
					break;
				}
				case "fix_crest":
				{
					CrestCache.convertOldPledgeFiles();
					sendReloadPage(activeChar);
					activeChar.sendMessage("Crets fixed.");
					break;
				}
				case "items":
				{
					ItemTable.getInstance().reload();
					sendReloadPage(activeChar);
					activeChar.sendMessage("Item table has been reloaded.");
					break;
				}
				case "access":
				{
					AdminData.getInstance().reload();
					sendReloadPage(activeChar);
					activeChar.sendMessage("Access Rights have been reloaded.");
					break;
				}
				case "instancemanager":
				{
					Manager.reloadAll();
					sendReloadPage(activeChar);
					activeChar.sendMessage("All instance managers has been reloaded.");
					break;
				}
				case "npcwalkers":
				{
					NpcWalkerRoutesData.getInstance();
					sendReloadPage(activeChar);
					activeChar.sendMessage("All NPC walker routes have been reloaded.");
					break;
				}
				case "configs":
				{
					Config.load();
					sendReloadPage(activeChar);
					activeChar.sendMessage("Server Configs has been Reloaded.");
					break;
				}
				case "tradelist":
				{
					TradeController.reload();
					sendReloadPage(activeChar);
					activeChar.sendMessage("TradeList Table has been reloaded.");
					break;
				}
				case "cw":
				{
					CursedWeaponsManager.reload();
					sendReloadPage(activeChar);
					activeChar.sendMessage("Cursed Weapons has been reloaded.");
					break;
				}
				case "summonitems":
				{
					SummonItemsData.getInstance();
					sendReloadPage(activeChar);
					activeChar.sendMessage("Summon Items has been reloaded.");
					break;
				}
				case "balancer":
				{
					BalanceLoad.LoadEm();
					sendReloadPage(activeChar);
					activeChar.sendMessage("Balance stats for classes has been reloaded.");
					break;
				}
				case "skill":
				{
					SkillTable.reload();
					sendReloadPage(activeChar);
					activeChar.sendMessage("All skills has been reloaded.");
					break;
				}
				case "sktrees":
				{
					SkillTreeData.getInstance();
					sendReloadPage(activeChar);
					activeChar.sendMessage("Skill Tree table has been reloaded.");
					break;
				}
				case "spellbooks":
				{
					SkillSpellbookData.getInstance();
					sendReloadPage(activeChar);
					activeChar.sendMessage("Spellbooks Table has been reloaded.");
					break;
				}
				case "augment":
				{
					AugmentationData.getInstance().reload();
					sendReloadPage(activeChar);
					activeChar.sendMessage("Augmentation Data has been reloaded.");
					break;
				}
				default:
				{
					sendReloadPage(activeChar);
					activeChar.sendMessage(RELOAD_USAGE);
					return true;
				}
			}
			activeChar.sendMessage("WARNING: There are several known issues regarding this feature. Reloading server data during runtime is STRONGLY NOT RECOMMENDED for live servers, just for developing environments.");
		}
		return true;
	}
	
	private static void sendReloadPage(L2PcInstance player)
	{
		String html = HtmCache.getInstance().getHtm("data/html/admin/reload_menu.htm");
		player.sendPacket(new NpcHtmlMessage(1, html));
		Util.printSection("Reload");
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}