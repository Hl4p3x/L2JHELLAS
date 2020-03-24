package com.l2jhellas.gameserver.handlers.itemhandlers;

import com.l2jhellas.gameserver.enums.items.L2CrystalType;
import com.l2jhellas.gameserver.enums.items.L2WeaponType;
import com.l2jhellas.gameserver.handler.IItemHandler;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jhellas.gameserver.templates.L2Weapon;
import com.l2jhellas.util.Broadcast;

public class FishShots implements IItemHandler
{
	// All the item IDs that this handler knows.
	private static final int[] ITEM_IDS =
	{
		6535,
		6536,
		6537,
		6538,
		6539,
		6540
	};
	private static final int[] SKILL_IDS =
	{
		2181,
		2182,
		2183,
		2184,
		2185,
		2186
	};
	
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		
		L2PcInstance activeChar = (L2PcInstance) playable;
		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		L2Weapon weaponItem = activeChar.getActiveWeaponItem();
		
		if (weaponInst == null || weaponItem.getItemType() != L2WeaponType.ROD)
		{
			return;
		}
		
		if (weaponInst.getChargedFishshot())
		{
			// spiritshot is already active
			return;
		}
		
		int FishshotId = item.getItemId();
		L2CrystalType grade = weaponItem.getCrystalType();
		int count = item.getCount();
		
		if ((grade == L2CrystalType.NONE && FishshotId != 6535) || (grade == L2CrystalType.D && FishshotId != 6536) || (grade == L2CrystalType.C && FishshotId != 6537) || (grade == L2CrystalType.B && FishshotId != 6538) || (grade == L2CrystalType.A && FishshotId != 6539) || (grade == L2CrystalType.S && FishshotId != 6540))
		{
			// 1479 - This fishing shot is not fit for the fishing pole crystal.
			activeChar.sendPacket(SystemMessageId.WRONG_FISHINGSHOT_GRADE);
			return;
		}
		
		if (count < 1)
		{
			return;
		}
		
		weaponInst.setChargedFishshot(true);
		activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), 1, null, false);
		L2Object oldTarget = activeChar.getTarget();
		activeChar.setTarget(activeChar);
		
		// activeChar.sendPacket(SystemMessage.ENABLED_SPIRITSHOT));
		
		MagicSkillUse MSU = new MagicSkillUse(activeChar, SKILL_IDS[grade.getId()], 1, 0, 0);
		Broadcast.toSelfAndKnownPlayersInRadius(activeChar, MSU, 1200);
		activeChar.setTarget(oldTarget);
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}