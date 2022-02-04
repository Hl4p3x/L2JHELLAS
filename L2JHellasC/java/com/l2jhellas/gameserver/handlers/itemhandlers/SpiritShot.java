package com.l2jhellas.gameserver.handlers.itemhandlers;

import com.l2jhellas.gameserver.enums.items.L2CrystalType;
import com.l2jhellas.gameserver.enums.items.ShotType;
import com.l2jhellas.gameserver.handler.IItemHandler;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.ExAutoSoulShot;
import com.l2jhellas.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.templates.L2Weapon;
import com.l2jhellas.util.Broadcast;

public class SpiritShot implements IItemHandler
{
	// All the item IDs that this handler knows.
	private static final int[] ITEM_IDS =
	{
		5790,
		2509,
		2510,
		2511,
		2512,
		2513,
		2514
	};
	private static final int[] SKILL_IDS =
	{
		2061,
		2155,
		2156,
		2157,
		2158,
		2159
	};
	
	@Override
	public synchronized void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		
		L2PcInstance activeChar = (L2PcInstance) playable;
		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		L2Weapon weaponItem = activeChar.getActiveWeaponItem();
		int itemId = item.getItemId();
		
		// Check if Spiritshot can be used
		if (weaponInst == null || weaponItem.getSpiritShotCount() == 0)
		{
			if (!activeChar.getAutoSoulShot().contains(itemId))
				activeChar.sendPacket(SystemMessageId.CANNOT_USE_SPIRITSHOTS);
			return;
		}
		
		if (activeChar.isParalyzed())
		{
			activeChar.sendMessage("You can't use this while you are Paralyzed.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if Spiritshot is already active
		if (activeChar.isChargedShot(ShotType.SPIRITSHOT))
			return;
		
		// Check for correct grade
		L2CrystalType weaponGrade = weaponItem.getCrystalType();
		if ((weaponGrade == L2CrystalType.NONE && itemId != 5790 && itemId != 2509) || (weaponGrade == L2CrystalType.D && itemId != 2510) || (weaponGrade == L2CrystalType.C && itemId != 2511) || (weaponGrade == L2CrystalType.B && itemId != 2512) || (weaponGrade == L2CrystalType.A && itemId != 2513) || (weaponGrade == L2CrystalType.S && itemId != 2514))
		{
			if (!activeChar.getAutoSoulShot().contains(itemId))
				activeChar.sendPacket(SystemMessageId.SPIRITSHOTS_GRADE_MISMATCH);
			return;
		}
		
		// Consume Spiritshot if player has enough of them
		if (!activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), weaponItem.getSpiritShotCount(), null, false))
		{
			if (activeChar.getAutoSoulShot().contains(itemId))
			{
				activeChar.removeAutoSoulShot(itemId);
				activeChar.sendPacket(new ExAutoSoulShot(itemId, 0));
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED).addString(item.getItem().getItemName()));
			}
			else
				activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_SPIRITSHOTS);
			return;
		}
		
		// Charge Spiritshot
		activeChar.setChargedShot(ShotType.SPIRITSHOT, true);
		
		// Send message to client
		activeChar.sendPacket(SystemMessageId.ENABLED_SPIRITSHOT);
		
		if (!activeChar.getSSRefusal())
			Broadcast.toSelfAndKnownPlayersInRadius(activeChar, new MagicSkillUse(activeChar, activeChar, SKILL_IDS[weaponGrade.getId()], 1, 0, 0), 500);
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}