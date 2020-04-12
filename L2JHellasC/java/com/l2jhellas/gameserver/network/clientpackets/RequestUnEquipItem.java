package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2Item;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public class RequestUnEquipItem extends L2GameClientPacket
{
	private static final String _C__11_REQUESTUNEQUIPITEM = "[C] 11 RequestUnequipItem";
	
	// cd
	private int _slot;
	
	@Override
	protected void readImpl()
	{
		_slot = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;

		final L2ItemInstance item = activeChar.getInventory().getPaperdollItemByL2ItemId(_slot);
		
		// Null - Wear-items are not to be unequipped
		if(item == null || item.isWear())
			return;
		
		// Prevent of unequiping a cursed weapon
		if (_slot == L2Item.SLOT_LR_HAND && activeChar.isCursedWeaponEquiped())
			return;
		
		// Prevent player from unequipping items in special conditions
		if (activeChar.isStunned() || activeChar.isSleeping() || activeChar.isParalyzed() || activeChar.isAfraid() || activeChar.isAlikeDead())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addItemName(item));
			return;
		}
		
		if (activeChar.isAttackingNow() || activeChar.isCastingNow())
			return;
		
		// Remove augmentation boni
		if (item.isAugmented())
			item.getAugmentation().removeBoni(activeChar);
		
		final L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInBodySlotAndRecord(item);
		
		// show the update in the inventory
		final InventoryUpdate iu = new InventoryUpdate();
		
		for (L2ItemInstance element : unequiped)
		{
			activeChar.checkSSMatch(null, element);		
			iu.addModifiedItem(element);
		}
		
		activeChar.sendPacket(iu);
		
		activeChar.abortAttack();
		activeChar.broadcastUserInfo();
		
		// this can be 0 if the user pressed the right mouse button twice very fast
		if (unequiped.length > 0)
		{
			if (unequiped[0].getEnchantLevel() > 0)
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED).addNumber(unequiped[0].getEnchantLevel()).addItemName(unequiped[0].getItemId()));
			else
			    activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED).addItemName(unequiped[0].getItemId()));
		}
	}
	
	@Override
	public String getType()
	{
		return _C__11_REQUESTUNEQUIPITEM;
	}
}