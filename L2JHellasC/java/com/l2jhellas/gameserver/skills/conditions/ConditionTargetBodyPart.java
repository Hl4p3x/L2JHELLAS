package com.l2jhellas.gameserver.skills.conditions;

import com.l2jhellas.gameserver.model.actor.item.Inventory;
import com.l2jhellas.gameserver.model.actor.item.L2Item;
import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.templates.L2Armor;

public class ConditionTargetBodyPart extends Condition
{
	private final L2Armor _armor;
	
	public ConditionTargetBodyPart(L2Armor armor)
	{
		_armor = armor;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		// target is attacker
		if (env.target == null)
			return true;
		int bodypart = env.target.getAttackingBodyPart();
		int armor_part = _armor.getBodyPart();
		switch (bodypart)
		{
			case Inventory.PAPERDOLL_CHEST:
				return (armor_part & (L2Item.SLOT_CHEST | L2Item.SLOT_FULL_ARMOR | L2Item.SLOT_UNDERWEAR)) != 0;
			case Inventory.PAPERDOLL_LEGS:
				return (armor_part & (L2Item.SLOT_LEGS | L2Item.SLOT_FULL_ARMOR)) != 0;
			case Inventory.PAPERDOLL_HEAD:
				return (armor_part & L2Item.SLOT_HEAD) != 0;
			case Inventory.PAPERDOLL_FEET:
				return (armor_part & L2Item.SLOT_FEET) != 0;
			case Inventory.PAPERDOLL_GLOVES:
				return (armor_part & L2Item.SLOT_GLOVES) != 0;
			default:
				return true;
		}
	}
}