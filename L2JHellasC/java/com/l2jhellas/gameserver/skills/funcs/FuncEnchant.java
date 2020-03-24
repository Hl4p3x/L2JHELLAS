package com.l2jhellas.gameserver.skills.funcs;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.enums.items.L2CrystalType;
import com.l2jhellas.gameserver.enums.items.L2WeaponType;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.Stats;

public class FuncEnchant extends Func
{
	public FuncEnchant(Stats pStat, int pOrder, Object owner, Lambda lambda)
	{
		super(pStat, pOrder, owner);
	}
	
	@Override
	public void calc(Env env)
	{
		if (cond != null && !cond.test(env))
			return;
		L2ItemInstance item = (L2ItemInstance) funcOwner;
		L2CrystalType cristall = item.getItem().getCrystalType();
		Enum<?> itemType = item.getItemType();
		
		if (cristall == L2CrystalType.NONE)
			return;
		int enchant = item.getEnchantLevel();
		
		int overenchant = 0;
		if (enchant > 3)
		{
			overenchant = enchant - 3;
			enchant = 3;
		}
		
		if (env.player != null && env.player instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) env.player;
			if (player.isInOlympiadMode() && Config.OLY_ENCHANT_LIMIT >= 0 && (enchant + overenchant) > Config.OLY_ENCHANT_LIMIT)
			{
				if (Config.OLY_ENCHANT_LIMIT > 3)
					overenchant = Config.OLY_ENCHANT_LIMIT - 3;
				else
				{
					overenchant = 0;
					enchant = Config.OLY_ENCHANT_LIMIT;
				}
			}
		}
		
		if (stat == Stats.MAGIC_DEFENCE || stat == Stats.POWER_DEFENCE)
		{
			env.value += enchant + 3 * overenchant;
			return;
		}
		
		if (stat == Stats.MAGIC_ATTACK)
		{
			switch (item.getItem().getCrystalType())
			{
				case S:
					env.value += 4 * enchant + 8 * overenchant;
					break;
				case A:
					env.value += 3 * enchant + 6 * overenchant;
					break;
				case B:
					env.value += 3 * enchant + 6 * overenchant;
					break;
				case C:
					env.value += 3 * enchant + 6 * overenchant;
					break;
				case D:
					env.value += 2 * enchant + 4 * overenchant;
					break;
			}
			return;
		}
		
		switch (item.getItem().getCrystalType())
		{
			case A:
				if (itemType == L2WeaponType.BOW)
					env.value += 8 * enchant + 16 * overenchant;
				else if (itemType == L2WeaponType.DUALFIST || itemType == L2WeaponType.DUAL || (itemType == L2WeaponType.SWORD && item.getItem().getBodyPart() == 16384))
					env.value += 5 * enchant + 10 * overenchant;
				else
					env.value += 4 * enchant + 8 * overenchant;
				break;
			case B:
				if (itemType == L2WeaponType.BOW)
					env.value += 6 * enchant + 12 * overenchant;
				else if (itemType == L2WeaponType.DUALFIST || itemType == L2WeaponType.DUAL || (itemType == L2WeaponType.SWORD && item.getItem().getBodyPart() == 16384))
					env.value += 4 * enchant + 8 * overenchant;
				else
					env.value += 3 * enchant + 6 * overenchant;
				break;
			case C:
				if (itemType == L2WeaponType.BOW)
					env.value += 6 * enchant + 12 * overenchant;
				else if (itemType == L2WeaponType.DUALFIST || itemType == L2WeaponType.DUAL || (itemType == L2WeaponType.SWORD && item.getItem().getBodyPart() == 16384))
					env.value += 4 * enchant + 8 * overenchant;
				else
					env.value += 3 * enchant + 6 * overenchant;
				
				break;
			case D:
				if (itemType == L2WeaponType.BOW)
					env.value += 4 * enchant + 8 * overenchant;
				else
					env.value += 2 * enchant + 4 * overenchant;
				break;
			case S:
				if (itemType == L2WeaponType.BOW)
					env.value += 10 * enchant + 20 * overenchant;
				else if (itemType == L2WeaponType.DUALFIST || itemType == L2WeaponType.DUAL || (itemType == L2WeaponType.SWORD && item.getItem().getBodyPart() == 16384))
					env.value += 4 * enchant + 12 * overenchant;
				else
					env.value += 4 * enchant + 10 * overenchant;
				break;
		}
		return;
	}
}