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
import com.l2jhellas.gameserver.skills.Stats;
import com.l2jhellas.gameserver.templates.L2Weapon;
import com.l2jhellas.util.Broadcast;

public class SoulShots implements IItemHandler
{
	// All the item IDs that this handler knows.
	private static final int[] ITEM_IDS =
	{
		5789,
		1835,
		1463,
		1464,
		1465,
		1466,
		1467
	};
	private static final int[] SKILL_IDS =
	{
		2039,
		2150,
		2151,
		2152,
		2153,
		2154
	};
	
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		
		L2PcInstance activeChar = (L2PcInstance) playable;
		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		L2Weapon weaponItem = activeChar.getActiveWeaponItem();
		int itemId = item.getItemId();
		
		// Check if Soulshot can be used
		if (weaponInst == null || weaponItem.getSoulShotCount() == 0)
		{
			if (!activeChar.getAutoSoulShot().contains(itemId))
				activeChar.sendPacket(SystemMessageId.CANNOT_USE_SOULSHOTS);
			return;
		}
		
		if (activeChar.isParalyzed())
		{
			activeChar.sendMessage("You can't use this while you are Paralyzed.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check for correct grade
		L2CrystalType weaponGrade = weaponItem.getCrystalType();
		if ((weaponGrade == L2CrystalType.NONE && itemId != 5789 && itemId != 1835) || (weaponGrade == L2CrystalType.D && itemId != 1463) || (weaponGrade == L2CrystalType.C && itemId != 1464) || (weaponGrade == L2CrystalType.B && itemId != 1465) || (weaponGrade == L2CrystalType.A && itemId != 1466) || (weaponGrade == L2CrystalType.S && itemId != 1467))
		{
			if (!activeChar.getAutoSoulShot().contains(itemId))
				activeChar.sendPacket(SystemMessageId.SOULSHOTS_GRADE_MISMATCH);
			return;
		}

		// Check if Soulshot is already active
		if (activeChar.isChargedShot(ShotType.SOULSHOT))
			return;
			
		// Consume Soulshots if player has enough of them
		int saSSCount = (int) activeChar.getStat().calcStat(Stats.SOULSHOT_COUNT, 0, null, null);
		int SSCount = saSSCount == 0 ? weaponItem.getSoulShotCount() : saSSCount;
			
		if (!activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), SSCount, null, false))
		{
			if (activeChar.getAutoSoulShot().contains(itemId))
			{
				activeChar.removeAutoSoulShot(itemId);
				activeChar.sendPacket(new ExAutoSoulShot(itemId, 0));
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED).addString(item.getItem().getItemName()));
			}
			else
				activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_SOULSHOTS);
			return;
		}
			
		// Charge soulshot
		weaponInst.setChargedShot(ShotType.SOULSHOT, true);

		// Send message to client
		activeChar.sendPacket(SystemMessageId.ENABLED_SOULSHOT);
		
		if (!activeChar.getSSRefusal())
			Broadcast.toSelfAndKnownPlayersInRadius(activeChar, new MagicSkillUse(activeChar, activeChar, SKILL_IDS[weaponGrade.getId()], 1, 0, 0), 500);
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}