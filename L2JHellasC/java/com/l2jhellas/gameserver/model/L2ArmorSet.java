package com.l2jhellas.gameserver.model;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.Inventory;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;

public final class L2ArmorSet
{
	private final int _chest;
	private final int _legs;
	private final int _head;
	private final int _gloves;
	private final int _feet;
	private final int _skillId;
	
	private final int _shield;
	private final int _shieldSkillId;
	
	private final int _enchant6Skill;
	
	public L2ArmorSet(int chest, int legs, int head, int gloves, int feet, int skill_id, int shield, int shield_skill_id, int enchant6skill)
	{
		_chest = chest;
		_legs = legs;
		_head = head;
		_gloves = gloves;
		_feet = feet;
		_skillId = skill_id;
		
		_shield = shield;
		_shieldSkillId = shield_skill_id;
		
		_enchant6Skill = enchant6skill;
	}
	
	public boolean containAll(L2PcInstance player)
	{
		Inventory inv = player.getInventory();
		
		L2ItemInstance legsItem = inv.getPaperdollItem(Inventory.PAPERDOLL_LEGS);
		L2ItemInstance headItem = inv.getPaperdollItem(Inventory.PAPERDOLL_HEAD);
		L2ItemInstance glovesItem = inv.getPaperdollItem(Inventory.PAPERDOLL_GLOVES);
		L2ItemInstance feetItem = inv.getPaperdollItem(Inventory.PAPERDOLL_FEET);
		
		int legs = 0;
		int head = 0;
		int gloves = 0;
		int feet = 0;
		
		if (legsItem != null)
			legs = legsItem.getItemId();
		if (headItem != null)
			head = headItem.getItemId();
		if (glovesItem != null)
			gloves = glovesItem.getItemId();
		if (feetItem != null)
			feet = feetItem.getItemId();
		
		return containAll(_chest, legs, head, gloves, feet);
		
	}
	
	public boolean containAll(int chest, int legs, int head, int gloves, int feet)
	{
		if (_chest != 0 && _chest != chest)
			return false;
		if (_legs != 0 && _legs != legs)
			return false;
		if (_head != 0 && _head != head)
			return false;
		if (_gloves != 0 && _gloves != gloves)
			return false;
		if (_feet != 0 && _feet != feet)
			return false;
		
		return true;
	}
	
	public boolean containItem(int slot, int itemId)
	{
		switch (slot)
		{
			case Inventory.PAPERDOLL_CHEST:
				return _chest == itemId;
			case Inventory.PAPERDOLL_LEGS:
				return _legs == itemId;
			case Inventory.PAPERDOLL_HEAD:
				return _head == itemId;
			case Inventory.PAPERDOLL_GLOVES:
				return _gloves == itemId;
			case Inventory.PAPERDOLL_FEET:
				return _feet == itemId;
			default:
				return false;
		}
	}
	
	public int getSkillId()
	{
		return _skillId;
	}
	
	public boolean containShield(L2PcInstance player)
	{
		Inventory inv = player.getInventory();
		
		L2ItemInstance shieldItem = inv.getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if (shieldItem != null && shieldItem.getItemId() == _shield)
			return true;
		
		return false;
	}
	
	public boolean containShield(int shield_id)
	{
		if (_shield == 0)
			return false;
		
		return _shield == shield_id;
	}
	
	public int getShieldSkillId()
	{
		return _shieldSkillId;
	}
	
	public int getEnchant6skillId()
	{
		return _enchant6Skill;
	}
	
	public boolean isEnchanted6(L2PcInstance player)
	{
		// Player don't have full set
		if (!containAll(player))
			return false;
		
		Inventory inv = player.getInventory();
		
		L2ItemInstance chestItem = inv.getPaperdollItem(Inventory.PAPERDOLL_CHEST);
		L2ItemInstance legsItem = inv.getPaperdollItem(Inventory.PAPERDOLL_LEGS);
		L2ItemInstance headItem = inv.getPaperdollItem(Inventory.PAPERDOLL_HEAD);
		L2ItemInstance glovesItem = inv.getPaperdollItem(Inventory.PAPERDOLL_GLOVES);
		L2ItemInstance feetItem = inv.getPaperdollItem(Inventory.PAPERDOLL_FEET);
		
		if (chestItem.getEnchantLevel() < 6)
			return false;
		if (_legs != 0 && legsItem.getEnchantLevel() < 6)
			return false;
		if (_gloves != 0 && glovesItem.getEnchantLevel() < 6)
			return false;
		if (_head != 0 && headItem.getEnchantLevel() < 6)
			return false;
		if (_feet != 0 && feetItem.getEnchantLevel() < 6)
			return false;
		
		return true;
	}
}