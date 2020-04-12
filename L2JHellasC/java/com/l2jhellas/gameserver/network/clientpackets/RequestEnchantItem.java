package com.l2jhellas.gameserver.network.clientpackets;

import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.enums.items.ItemLocation;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.Inventory;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.EnchantResult;
import com.l2jhellas.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jhellas.gameserver.network.serverpackets.ItemList;
import com.l2jhellas.gameserver.network.serverpackets.StatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.shield.antiflood.FloodProtectors;
import com.l2jhellas.shield.antiflood.FloodProtectors.Action;
import com.l2jhellas.util.Rnd;

public final class RequestEnchantItem extends AbstractEnchantPacket
{
	protected static final Logger _log = Logger.getLogger(Inventory.class.getName());
	private static final String _C__58_REQUESTENCHANTITEM = "[C] 58 RequestEnchantItem";
	
	private int _objectId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}
	
	@Override
	protected void runImpl()
	{	
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null || _objectId == 0)
			return;
		
		// flood protect for fast auto-enchant.
		if (!FloodProtectors.performAction(getClient(), Action.USE_ITEM))
		{
			//custom message
			activeChar.sendMessage("System:fast enchant-autoenchant program not allowed.");
			return;
		}
		
		if (!activeChar.canEnchant())
		{
			activeChar.cancellEnchant();
			return;
		}
		
		final L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
		L2ItemInstance scroll = activeChar.getActiveEnchantItem();
		
		if (item == null || scroll == null)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION));
			activeChar.cancellEnchant();
			return;
		}
		
		final EnchantScroll scrollTemplate = getEnchantScroll(scroll);
		
		if (scrollTemplate == null)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION));
			activeChar.cancellEnchant();
			return;
		}
		
		if (!scrollTemplate.isValid(item) || !isEnchantable(item))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION));
			activeChar.cancellEnchant();
			return;
		}
		
		scroll = scroll.isStackable() ? activeChar.getInventory().destroyItem("Enchant", scroll.getObjectId(), 1, activeChar, item) 
		: activeChar.getInventory().destroyItem("Enchant", scroll, activeChar, item);

		if (scroll == null)
		{
			activeChar.cancellEnchant();
			activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			return;
		}
				
		synchronized (item)
		{
			final int chance = scrollTemplate.getChance(item);
			
			if ((item.getOwnerId() != activeChar.getObjectId()) || !isEnchantable(item) || (chance < 0))
			{
				activeChar.cancellEnchant();
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION));
				return;
			}
			
			if (Rnd.get(100) < chance)
			{
				if (item.getLocation() != ItemLocation.INVENTORY && item.getLocation() != ItemLocation.PAPERDOLL)
				{
					activeChar.cancellEnchant();
					activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
					return;
				}

				item.setEnchantLevel(item.getEnchantLevel() + 1);
				item.updateDatabase();
				checkForSkills(activeChar,item,true);
				
				activeChar.sendPacket(item.getEnchantLevel() == 0 ? SystemMessage.getSystemMessage(SystemMessageId.S1_SUCCESSFULLY_ENCHANTED).addItemName(item.getItemId()) 
				: SystemMessage.getSystemMessage(SystemMessageId.S1_S2_SUCCESSFULLY_ENCHANTED).addNumber(item.getEnchantLevel()).addItemName(item.getItemId()));
		
				activeChar.sendPacket(EnchantResult.SUCCESS);
			}
			else
			{
				checkForSkills(activeChar,item,false);
				
				if (!scrollTemplate.isBlessed())
				{					
					if (item.isEquipped())
					{
						final L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(item.getEquipSlot());

						InventoryUpdate iu = new InventoryUpdate();
						
						for (L2ItemInstance element : unequiped)
							iu.addModifiedItem(element);
						
						activeChar.sendPacket(iu);
						
						activeChar.sendPacket(item.getEnchantLevel() > 0 ? SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED).addNumber(item.getEnchantLevel()).addItemName(item.getItemId()) 
						: SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED).addItemName(item.getItemId()));
						
						activeChar.broadcastUserInfo();
					}
					
					final int crystalId = item.getItem().getCrystalItemId();
					int count = item.getCrystalCount() - (item.getItem().getCrystalCount() + 1) / 2;
					
					if (count < 1)
						count = 1;
					
					final L2ItemInstance destroyItem = activeChar.getInventory().destroyItem("Enchant", item, activeChar, null);
					
					if (destroyItem == null)
					{
						activeChar.setActiveEnchantItem(null);
						activeChar.sendPacket(EnchantResult.CANCELLED);
						return;
					}
					
					L2ItemInstance crystals = null;
					if (crystalId != 0)
					{
						crystals = activeChar.getInventory().addItem("Enchant", crystalId, count, activeChar, destroyItem);
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(crystals.getItemId()).addNumber(count));
					}
					
					if (!Config.FORCE_INVENTORY_UPDATE)
					{
						InventoryUpdate iu = new InventoryUpdate();
						if (destroyItem.getCount() == 0)
							iu.addRemovedItem(destroyItem);
						else
							iu.addModifiedItem(destroyItem);
						
						if (crystals != null)
							iu.addItem(crystals);
						
						activeChar.sendPacket(iu);
					}
					else
						activeChar.sendPacket(new ItemList(activeChar, true));
					
					StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
					su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
					activeChar.sendPacket(su);
					
					activeChar.broadcastUserInfo();
					
					L2World.getInstance().removeObject(destroyItem);										
					
					activeChar.sendPacket(item.getEnchantLevel() > 0 ? SystemMessage.getSystemMessage(SystemMessageId.ENCHANTMENT_FAILED_S1_S2_EVAPORATED).addNumber(item.getEnchantLevel()).addItemName(item.getItemId())
					: SystemMessage.getSystemMessage(SystemMessageId.ENCHANTMENT_FAILED_S1_EVAPORATED).addItemName(item.getItemId()));
					
					activeChar.sendPacket(crystalId == 0 ? EnchantResult.FAILED_NO_CRYSTALS : EnchantResult.FAILED_CRYSTALS);	
				}
				else
				{
					item.setEnchantLevel(0);
					item.updateDatabase();
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.BLESSED_ENCHANT_FAILED));
					activeChar.sendPacket(EnchantResult.BLESSED_FAILED);
				}
			}
			
			StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
			su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
			activeChar.sendPacket(su);
			
			activeChar.sendPacket(new ItemList(activeChar, false));
			activeChar.broadcastUserInfo();
			activeChar.setActiveEnchantItem(null);

			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__58_REQUESTENCHANTITEM;
	}
}