package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.Inventory;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ItemList;
import com.l2jhellas.util.IllegalPlayerAction;
import com.l2jhellas.util.Util;

public class AdminEnchant implements IAdminCommandHandler
{
	protected static final Logger _log = Logger.getLogger(AdminEnchant.class.getName());
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_seteh",// 6
		"admin_setec",// 10
		"admin_seteg",// 9
		"admin_setel",// 11
		"admin_seteb",// 12
		"admin_setew",// 7
		"admin_setes",// 8
		"admin_setle",// 1
		"admin_setre",// 2
		"admin_setlf",// 4
		"admin_setrf",// 5
		"admin_seten",// 3
		"admin_setun",// 0
		"admin_setba",// 13
		"admin_enchant"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.equals("admin_enchant"))
			showMainPage(activeChar);
		else
		{
			int armorType = -1;
			
			if (command.startsWith("admin_seteh"))
				armorType = Inventory.PAPERDOLL_HEAD;
			else if (command.startsWith("admin_setec"))
				armorType = Inventory.PAPERDOLL_CHEST;
			else if (command.startsWith("admin_seteg"))
				armorType = Inventory.PAPERDOLL_GLOVES;
			else if (command.startsWith("admin_seteb"))
				armorType = Inventory.PAPERDOLL_FEET;
			else if (command.startsWith("admin_setel"))
				armorType = Inventory.PAPERDOLL_LEGS;
			else if (command.startsWith("admin_setew"))
				armorType = Inventory.PAPERDOLL_RHAND;
			else if (command.startsWith("admin_setes"))
				armorType = Inventory.PAPERDOLL_LHAND;
			else if (command.startsWith("admin_setle"))
				armorType = Inventory.PAPERDOLL_LEAR;
			else if (command.startsWith("admin_setre"))
				armorType = Inventory.PAPERDOLL_REAR;
			else if (command.startsWith("admin_setlf"))
				armorType = Inventory.PAPERDOLL_LFINGER;
			else if (command.startsWith("admin_setrf"))
				armorType = Inventory.PAPERDOLL_RFINGER;
			else if (command.startsWith("admin_seten"))
				armorType = Inventory.PAPERDOLL_NECK;
			else if (command.startsWith("admin_setun"))
				armorType = Inventory.PAPERDOLL_UNDER;
			else if (command.startsWith("admin_setba"))
				armorType = Inventory.PAPERDOLL_BACK;

			if ((armorType != -1))
			{
				try
				{
					int ench = Integer.parseInt(command.substring(12));
					
					// check value
					if (ench < 0 || ench > 65535)
						activeChar.sendMessage("You must set the enchant level to be between 0-65535.");
					else
					{
						L2Object target = activeChar.getTarget();
						L2PcInstance player = (L2PcInstance) target;
						if ((ench > Config.GM_OVER_ENCHANT) && (Config.GM_OVER_ENCHANT != 0) && (player != null) && !player.isGM())
						{
							player.sendMessage("A GM tried to overenchant you. You will both be banned.");
							Util.handleIllegalPlayerAction(player, "The player " + player.getName() + " has been edited. BAN!", IllegalPlayerAction.PUNISH_KICKBAN);
							activeChar.sendMessage("You tried to overenchant somebody. You will both be banned.");
							Util.handleIllegalPlayerAction(activeChar, "The GM " + activeChar.getName() + " has overenchanted the player " + player.getName() + ". BAN!", IllegalPlayerAction.PUNISH_KICKBAN);
						}
						else
							setEnchant(activeChar, ench, armorType);
					}
				}
				catch (StringIndexOutOfBoundsException e)
				{
					activeChar.sendMessage("Please specify a new enchant value.");
				}
				catch (NumberFormatException e)
				{
					activeChar.sendMessage("Please specify a valid new enchant value.");
				}
			}
			else
				activeChar.sendMessage("Your target has no item equipted in your selected slot.");
			
			// show the enchant menu after an action
			showMainPage(activeChar);
		}
		return true;
	}
	
	private static void setEnchant(L2PcInstance activeChar, int ench, int armorType)
	{
		final L2PcInstance  player = activeChar.getTarget() != null ? activeChar.getTarget().getActingPlayer() : activeChar;
		
		if (player == null)
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		int curEnchant = 0; 
		L2ItemInstance itemInstance = null;
		
		L2ItemInstance parmorInstance = player.getInventory().getPaperdollItem(armorType);
		if (parmorInstance != null && parmorInstance.getEquipSlot() == armorType)
			itemInstance = parmorInstance;
		
		if (itemInstance == null)
		{
			activeChar.sendMessage("Selected slot is empty in your targets inventory.");
			return;
		}
		
		curEnchant = itemInstance.getEnchantLevel();
		itemInstance.setEnchantLevel(ench);
		itemInstance.updateDatabase();

		player.sendPacket(new ItemList(player, false));		
		player.broadcastUserInfo();
		
		activeChar.sendMessage("Changed enchantment of " + player.getName() + "'s " + itemInstance.getItem().getItemName() + " from " + curEnchant + " to " + ench + ".");
		player.sendMessage("Admin has changed the enchantment of your " + itemInstance.getItem().getItemName() + " from " + curEnchant + " to " + ench + ".");
	}
	
	private static void showMainPage(L2PcInstance activeChar)
	{
		AdminHelpPage.showHelpPage(activeChar, "enchant.htm");
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}