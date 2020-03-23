package com.l2jhellas.gameserver.network.clientpackets;

import java.util.HashMap;
import java.util.Map;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.xml.ArmorSetsData;
import com.l2jhellas.gameserver.enums.items.ItemLocation;
import com.l2jhellas.gameserver.enums.items.L2WeaponType;
import com.l2jhellas.gameserver.model.L2ArmorSet;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.Inventory;
import com.l2jhellas.gameserver.model.actor.item.L2Item;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.gameserver.templates.L2Weapon;

public abstract class AbstractEnchantPacket extends L2GameClientPacket
{
	public static final Map<Integer, EnchantScroll> _scrolls = new HashMap<>();
	
	static
	{
		// Scrolls: Enchant Weapon
		_scrolls.put(729, new EnchantScroll(true, false, false, L2Item.CRYSTAL_A));
		_scrolls.put(947, new EnchantScroll(true, false, false, L2Item.CRYSTAL_B));
		_scrolls.put(951, new EnchantScroll(true, false, false, L2Item.CRYSTAL_C));
		_scrolls.put(955, new EnchantScroll(true, false, false, L2Item.CRYSTAL_D));
		_scrolls.put(959, new EnchantScroll(true, false, false, L2Item.CRYSTAL_S));
		
		// Scrolls: Enchant Armor
		_scrolls.put(730, new EnchantScroll(false, false, false, L2Item.CRYSTAL_A));
		_scrolls.put(948, new EnchantScroll(false, false, false, L2Item.CRYSTAL_B));
		_scrolls.put(952, new EnchantScroll(false, false, false, L2Item.CRYSTAL_C));
		_scrolls.put(956, new EnchantScroll(false, false, false, L2Item.CRYSTAL_D));
		_scrolls.put(960, new EnchantScroll(false, false, false, L2Item.CRYSTAL_S));
		
		// Blessed Scrolls: Enchant Weapon
		_scrolls.put(6569, new EnchantScroll(true, true, false, L2Item.CRYSTAL_A));
		_scrolls.put(6571, new EnchantScroll(true, true, false, L2Item.CRYSTAL_B));
		_scrolls.put(6573, new EnchantScroll(true, true, false, L2Item.CRYSTAL_C));
		_scrolls.put(6575, new EnchantScroll(true, true, false, L2Item.CRYSTAL_D));
		_scrolls.put(6577, new EnchantScroll(true, true, false, L2Item.CRYSTAL_S));
		// Blessed Scrolls: Enchant Armor
		_scrolls.put(6570, new EnchantScroll(false, true, false, L2Item.CRYSTAL_A));
		_scrolls.put(6572, new EnchantScroll(false, true, false, L2Item.CRYSTAL_B));
		_scrolls.put(6574, new EnchantScroll(false, true, false, L2Item.CRYSTAL_C));
		_scrolls.put(6576, new EnchantScroll(false, true, false, L2Item.CRYSTAL_D));
		_scrolls.put(6578, new EnchantScroll(false, true, false, L2Item.CRYSTAL_S));
		
		// Crystal Scrolls: Enchant Weapon
		_scrolls.put(731, new EnchantScroll(true, false, true, L2Item.CRYSTAL_A));
		_scrolls.put(949, new EnchantScroll(true, false, true, L2Item.CRYSTAL_B));
		_scrolls.put(953, new EnchantScroll(true, false, true, L2Item.CRYSTAL_C));
		_scrolls.put(957, new EnchantScroll(true, false, true, L2Item.CRYSTAL_D));
		_scrolls.put(961, new EnchantScroll(true, false, true, L2Item.CRYSTAL_S));
		// Crystal Scrolls: Enchant Armor
		_scrolls.put(732, new EnchantScroll(false, false, true, L2Item.CRYSTAL_A));
		_scrolls.put(950, new EnchantScroll(false, false, true, L2Item.CRYSTAL_B));
		_scrolls.put(954, new EnchantScroll(false, false, true, L2Item.CRYSTAL_C));
		_scrolls.put(958, new EnchantScroll(false, false, true, L2Item.CRYSTAL_D));
		_scrolls.put(962, new EnchantScroll(false, false, true, L2Item.CRYSTAL_S));
	}
	
	public static class EnchantItem
	{
		protected final boolean _isWeapon;
		protected final int _grade;
		
		public EnchantItem(boolean wep, int grade)
		{
			_isWeapon = wep;
			_grade = grade;
		}
		
		public boolean isValid(L2ItemInstance enchantItem)
		{
			if (enchantItem == null)
				return false;
			
			int type2 = enchantItem.getItem().getType2();
			
			switch (type2)
			{
				case L2Item.TYPE2_WEAPON:
					if (!_isWeapon || ((Config.ENCHANT_MAX_WEAPON > 0) && (enchantItem.getEnchantLevel() >= Config.ENCHANT_MAX_WEAPON)))
						return false;
					break;
				case L2Item.TYPE2_SHIELD_ARMOR:
					if (_isWeapon || ((Config.ENCHANT_MAX_ARMOR > 0) && (enchantItem.getEnchantLevel() >= Config.ENCHANT_MAX_ARMOR)))
						return false;
					break;
				case L2Item.TYPE2_ACCESSORY:
					if (_isWeapon || ((Config.ENCHANT_MAX_JEWELRY > 0) && (enchantItem.getEnchantLevel() >= Config.ENCHANT_MAX_JEWELRY)))
						return false;
					break;
				default:
					return false;
			}
			
			if (_grade != enchantItem.getItem().getCrystalType())
				return false;
			
			return true;
		}
	}
	
	public static final class EnchantScroll extends EnchantItem
	{
		private final boolean _isBlessed;
		private final boolean _isCrystal;
		
		public EnchantScroll(boolean wep, boolean bless, boolean crystal, int grade)
		{
			super(wep, grade);
			
			_isBlessed = bless;
			_isCrystal = crystal;
		}
		
		public final boolean isBlessed()
		{
			return _isBlessed;
		}
		
		public final boolean isCrystal()
		{
			return _isCrystal;
		}
		
		public final int getChance(L2ItemInstance enchantItem)
		{
			if (!isValid(enchantItem))
				return -1;
			
			boolean fullBody = enchantItem.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR;
			if ((enchantItem.getEnchantLevel() < Config.ENCHANT_SAFE_MAX) || (fullBody && (enchantItem.getEnchantLevel() < Config.ENCHANT_SAFE_MAX_FULL)))
				return 100;
			
			boolean isAccessory = enchantItem.getItem().getType2() == L2Item.TYPE2_ACCESSORY;
			int chance = 0;
			
			if (_isBlessed)
			{
				if (_isWeapon)
					chance = Config.ENCHANT_CHANCE_WEAPON_BLESSED;
				else if (isAccessory)
					chance = Config.ENCHANT_CHANCE_JEWELRY_BLESSED;
				else
					chance = Config.ENCHANT_CHANCE_ARMOR_BLESSED;
			}
			else
			{
				if (_isWeapon)
					chance = Config.ENCHANT_CHANCE_WEAPON;
				else if (isAccessory)
					chance = Config.ENCHANT_CHANCE_JEWELRY;
				else
					chance = Config.ENCHANT_CHANCE_ARMOR;
			}
			
			return chance;
		}
	}
	
	protected static final EnchantScroll getEnchantScroll(L2ItemInstance scroll)
	{
		return _scrolls.get(scroll.getItemId());
	}
	
	protected static final boolean isEnchantable(L2ItemInstance item)
	{
		if (item.isHeroItem())
			return false;
		if (item.isShadowItem())
			return false;
		if (item.isWear())
			return false;
		if (item.isEtcItem())
			return false;
		if (item.getItem().getItemType() == L2WeaponType.ROD)
			return false;
		if (item.getItem().getBodyPart() == L2Item.SLOT_BACK)
			return false;
		if ((item.getLocation() != ItemLocation.INVENTORY) && (item.getLocation() != ItemLocation.PAPERDOLL))
			return false;
		
		return true;
	}
	
	protected static final void checkForSkills(L2PcInstance activeChar, L2ItemInstance item, boolean success)
	{
		if (item.isEquipped())
		{
			if (success)
			{
				if (item.isWeapon() && item.getEnchantLevel() == 4)
				{
					final L2Item it = item.getItem();
					final L2Skill enchant4Skill = ((L2Weapon) it).getEnchant4Skill();
					if (enchant4Skill != null)
					{
						activeChar.addSkill(enchant4Skill, false);
						activeChar.sendSkillList();
					}
				}
				else if (item.isArmor() && item.getEnchantLevel() == 6)
				{
					final L2ItemInstance chestItem = activeChar.getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
					if (chestItem != null)
					{
						final L2ArmorSet armorSet = ArmorSetsData.getInstance().getSet(chestItem.getItemId());
						if (armorSet != null && armorSet.isEnchanted6(activeChar))
						{
							final int skillId = armorSet.getEnchant6skillId();
							if (skillId > 0)
							{
								final L2Skill skill = SkillTable.getInstance().getInfo(skillId, 1);
								if (skill != null)
								{
									activeChar.addSkill(skill, false);
									activeChar.sendSkillList();
								}
							}
						}
					}
				}
			}
			else
			{
				if (item.isWeapon() && item.getEnchantLevel() >= 4)
				{
					final L2Item it = item.getItem();
					final L2Skill enchant4Skill = ((L2Weapon) it).getEnchant4Skill();
					if (enchant4Skill != null)
					{
						activeChar.removeSkill(enchant4Skill, false);
						activeChar.sendSkillList();
					}
				}
				else if (item.isArmor() && item.getEnchantLevel() >= 6)
				{
					final L2ItemInstance chestItem = activeChar.getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
					if (chestItem != null)
					{
						final L2ArmorSet armorSet = ArmorSetsData.getInstance().getSet(chestItem.getItemId());
						if (armorSet != null && armorSet.isEnchanted6(activeChar))
						{
							final L2Skill skill = activeChar.getKnownSkill(armorSet.getEnchant6skillId());
							
							if (skill != null)
							{
								activeChar.removeSkill(skill, false);
								activeChar.sendSkillList();
							}
						}
					}
				}
			}
		}
	}
}