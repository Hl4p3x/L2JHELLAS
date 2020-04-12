package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.enums.items.L2CrystalType;
import com.l2jhellas.gameserver.enums.player.StoreType;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.PcInventory;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jhellas.gameserver.network.serverpackets.ItemList;
import com.l2jhellas.gameserver.network.serverpackets.StatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.shield.antiflood.FloodProtectors;
import com.l2jhellas.shield.antiflood.FloodProtectors.Action;

public final class RequestCrystallizeItem extends L2GameClientPacket
{
	private static final String _C__72_REQUESTDCRYSTALLIZEITEM = "[C] 72 RequestCrystallizeItem";
	
	private int _objectId;
	private int _count;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_count = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		// Flood protect
		if (!FloodProtectors.performAction(getClient(),Action.USE_ITEM))
		{
			activeChar.sendMessage("You are using this action too fast!");
			return;
		}
		
		if (_count <= 0)
			return;
		
		if (activeChar.getPrivateStoreType() != StoreType.NONE || activeChar.isInCrystallize())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
			return;
		}
		
		final int skillLevel = activeChar.getSkillLevel(L2Skill.SKILL_CRYSTALLIZE);
		if (skillLevel <= 0)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final PcInventory inventory = activeChar.getInventory();
		if (inventory != null)
		{
			final L2ItemInstance item = inventory.getItemByObjectId(_objectId);
			if ((item == null) || item.isWear())
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			final int itemId = item.getItemId();
			if (((itemId >= 6611) && (itemId <= 6621)) || (itemId == 6842))
				return;
			
			if (_count > item.getCount())
				_count = activeChar.getInventory().getItemByObjectId(_objectId).getCount();
		}
		
		final L2ItemInstance itemToRemove = activeChar.getInventory().getItemByObjectId(_objectId);
		if ((itemToRemove == null) || itemToRemove.isWear())
			return;
		if (!itemToRemove.getItem().isCrystallizable() || (itemToRemove.getItem().getCrystalCount() <= 0) || (itemToRemove.getItem().getCrystalType() == L2CrystalType.NONE))
			return;
		
		// Check if the char can crystallize C items and return if false;
		if (itemToRemove.getItem().getCrystalType() == L2CrystalType.C && skillLevel <= 1)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if the user can crystallize B items and return if false;
		if ((itemToRemove.getItem().getCrystalType() == L2CrystalType.B) && (skillLevel <= 2))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if the user can crystallize A items and return if false;
		if ((itemToRemove.getItem().getCrystalType() == L2CrystalType.A) && (skillLevel <= 3))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if the user can crystallize S items and return if false;
		if (itemToRemove.getItem().getCrystalType() == L2CrystalType.S && skillLevel <= 4)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		activeChar.setInCrystallize(true);
		
		if (itemToRemove.isEquipped())
		{
			InventoryUpdate iu = new InventoryUpdate();
			for (L2ItemInstance items : activeChar.getInventory().unEquipItemInSlotAndRecord(itemToRemove.getEquipSlot()))
				iu.addModifiedItem(items);
			
			activeChar.sendPacket(iu);
			
			SystemMessage msg;
			if (itemToRemove.getEnchantLevel() > 0)
				msg = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED).addNumber(itemToRemove.getEnchantLevel()).addItemName(itemToRemove.getItemId());
			else
				msg = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED).addItemName(itemToRemove.getItemId());
			
			activeChar.sendPacket(msg);
		}
		
		// remove from inventory
		final L2ItemInstance removedItem = activeChar.getInventory().destroyItem("Crystalize", _objectId, _count, activeChar, null);
		
		activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CRYSTALLIZED).addItemName(removedItem.getItemId()));

		// add crystals
		int crystalId = itemToRemove.getItem().getCrystalItemId();
		int crystalAmount = itemToRemove.getCrystalCount();
		
		activeChar.getInventory().addItem("Crystalize", crystalId, crystalAmount, activeChar, itemToRemove);		
		activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(crystalId).addNumber(crystalAmount));
		
		activeChar.sendPacket(new ItemList(activeChar, false));
		
		// status & user info
		StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
		activeChar.sendPacket(su);
				
		L2World.getInstance().removeObject(removedItem);
		activeChar.setInCrystallize(false);
		activeChar.broadcastUserInfo();
	}
	
	@Override
	public String getType()
	{
		return _C__72_REQUESTDCRYSTALLIZEITEM;
	}
}