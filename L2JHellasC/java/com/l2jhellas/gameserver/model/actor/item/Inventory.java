package com.l2jhellas.gameserver.model.actor.item;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.sql.ItemTable;
import com.l2jhellas.gameserver.datatables.xml.ArmorSetsData;
import com.l2jhellas.gameserver.enums.items.ItemLocation;
import com.l2jhellas.gameserver.enums.items.L2EtcItemType;
import com.l2jhellas.gameserver.enums.items.L2WeaponType;
import com.l2jhellas.gameserver.enums.player.StoreType;
import com.l2jhellas.gameserver.model.L2ArmorSet;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.gameserver.templates.L2Armor;
import com.l2jhellas.gameserver.templates.L2Weapon;
import com.l2jhellas.util.database.L2DatabaseFactory;

public abstract class Inventory extends ItemContainer
{
	protected static final Logger _log = Logger.getLogger(Inventory.class.getName());
	
	public interface PaperdollListener
	{
		public void notifyEquiped(int slot, L2ItemInstance inst);
		
		public void notifyUnequiped(int slot, L2ItemInstance inst);
	}

	public static final int PAPERDOLL_UNDER = 0;
	public static final int PAPERDOLL_LEAR = 1;
	public static final int PAPERDOLL_REAR = 2;
	public static final int PAPERDOLL_NECK = 3;
	public static final int PAPERDOLL_LFINGER = 4;
	public static final int PAPERDOLL_RFINGER = 5;
	public static final int PAPERDOLL_HEAD = 6;
	public static final int PAPERDOLL_RHAND = 7;
	public static final int PAPERDOLL_LHAND = 8;
	public static final int PAPERDOLL_GLOVES = 9;
	public static final int PAPERDOLL_CHEST = 10;
	public static final int PAPERDOLL_LEGS = 11;
	public static final int PAPERDOLL_FEET = 12;
	public static final int PAPERDOLL_BACK = 13;
	public static final int PAPERDOLL_FACE = 14;
	public static final int PAPERDOLL_HAIR = 15;
	public static final int PAPERDOLL_HAIRALL = 16;
	public static final int PAPERDOLL_TOTALSLOTS = 17;
	
	// Speed percentage mods
	public static final double MAX_ARMOR_WEIGHT = 12000;
	public static final int ADENA_ID = 57;
	
	private final L2ItemInstance[] _paperdoll;
	private final List<PaperdollListener> _paperdollListeners;
	
	// protected to be accessed from child classes only
	protected int _totalWeight;
	
	// used to quickly check for using of items of special type
	private int _wearedMask;
	
	final class FormalWearListener implements PaperdollListener
	{
		@Override
		public void notifyUnequiped(int slot, L2ItemInstance item)
		{
			if (!(getOwner() != null && getOwner() instanceof L2PcInstance))
				return;
			
			L2PcInstance owner = (L2PcInstance) getOwner();
			
			if (item.getItemId() == 6408)
				owner.setIsWearingFormalWear(false);
		}
		
		@Override
		public void notifyEquiped(int slot, L2ItemInstance item)
		{
			if (!(getOwner() != null && getOwner() instanceof L2PcInstance))
				return;
			
			L2PcInstance owner = (L2PcInstance) getOwner();
			
			// If player equip Formal Wear unequip weapons and abort cast/attack
			if (item.getItemId() == 6408)
			{
				owner.setIsWearingFormalWear(true);
			}
			else
			{
				if (!owner.isWearingFormalWear())
					return;
			}
		}
	}
	
	public static final class ChangeRecorder implements PaperdollListener
	{
		private final Inventory _inventory;
		private final List<L2ItemInstance> _changed;
		
		ChangeRecorder(Inventory inventory)
		{
			_inventory = inventory;
			_changed = new ArrayList<>();
			_inventory.addPaperdollListener(this);
		}
		
		@Override
		public void notifyEquiped(int slot, L2ItemInstance item)
		{
			if (!_changed.contains(item))
				_changed.add(item);
		}
		
		@Override
		public void notifyUnequiped(int slot, L2ItemInstance item)
		{
			if (!_changed.contains(item))
				_changed.add(item);
		}
		
		public L2ItemInstance[] getChangedItems()
		{
			return _changed.toArray(new L2ItemInstance[_changed.size()]);
		}
	}
	
	final class BowListener implements PaperdollListener
	{
		@Override
		public void notifyUnequiped(int slot, L2ItemInstance item)
		{
			if (item.getItemType() == L2WeaponType.BOW)
			{
				L2ItemInstance arrow = getPaperdollItem(PAPERDOLL_LHAND);
				if (arrow != null)
					setPaperdollItem(PAPERDOLL_LHAND, null);
			}
		}
		
		@Override
		public void notifyEquiped(int slot, L2ItemInstance item)
		{
			if (item.getItemType() == L2WeaponType.BOW)
			{
				L2ItemInstance arrow = findArrowForBow(item.getItem());
				if (arrow != null)
					setPaperdollItem(PAPERDOLL_LHAND, arrow);
			}
		}
	}
	
	final class StatsListener implements PaperdollListener
	{
		@Override
		public void notifyUnequiped(int slot, L2ItemInstance item)
		{
			getOwner().removeStatsOwner(item);
		}
		
		@Override
		public void notifyEquiped(int slot, L2ItemInstance item)
		{
			getOwner().addStatFuncs(item.getStatFuncs(getOwner()));
		}
	}
	
	final class ItemPassiveSkillsListener implements PaperdollListener
	{
		@Override
		public void notifyUnequiped(int slot, L2ItemInstance item)
		{
			L2PcInstance player;
			
			if (getOwner() instanceof L2PcInstance)
			{
				player = (L2PcInstance) getOwner();
			}
			else
				return;
			
			L2Skill passiveSkill = null;
			L2Skill enchant4Skill = null;
			
			L2Item it = item.getItem();
			
			if (it instanceof L2Weapon)
			{
				passiveSkill = ((L2Weapon) it).getSkill();
				enchant4Skill = ((L2Weapon) it).getEnchant4Skill();
			}
			else if (it instanceof L2Armor)
				passiveSkill = ((L2Armor) it).getSkill();
			
			if (passiveSkill != null)
			{
				player.removeSkill(passiveSkill, false);
				player.sendSkillList();
			}
			if (enchant4Skill != null)
			{
				player.removeSkill(enchant4Skill, false);
				player.sendSkillList();
			}
		}
		
		@Override
		public void notifyEquiped(int slot, L2ItemInstance item)
		{
			L2PcInstance player;
			
			if (getOwner() instanceof L2PcInstance)
			{
				player = (L2PcInstance) getOwner();
			}
			else
				return;
			
			L2Skill passiveSkill = null;
			L2Skill enchant4Skill = null;
			
			L2Item it = item.getItem();
			
			if (it instanceof L2Weapon)
			{
				passiveSkill = ((L2Weapon) it).getSkill();
				
				if (item.getEnchantLevel() >= 4)
					enchant4Skill = ((L2Weapon) it).getEnchant4Skill();
			}
			else if (it instanceof L2Armor)
				passiveSkill = ((L2Armor) it).getSkill();
			
			if (passiveSkill != null)
			{
				player.addSkill(passiveSkill, false);
				player.sendSkillList();
			}
			if (enchant4Skill != null)
			{
				player.addSkill(enchant4Skill, false);
				player.sendSkillList();
			}
			
		}
	}
	
	final class ItemAugmentationSkillsListener implements PaperdollListener
	{
		@Override
		public void notifyEquiped(int slot, L2ItemInstance item)
		{
			if (getOwner().isPlayer())
			{
				L2PcInstance activeChar = (L2PcInstance) getOwner();
				if (item.isAugmented())
					item.getAugmentation().applyBoni(activeChar);
			}
		}
		
		@Override
		public void notifyUnequiped(int slot, L2ItemInstance item)
		{
			if (getOwner().isPlayer())
			{
				L2PcInstance activeChar = (L2PcInstance) getOwner();
				if (item.isAugmented())
					item.getAugmentation().removeBoni(activeChar);
			}
		}
	}
	
	final class ArmorSetListener implements PaperdollListener
	{
		@Override
		public void notifyEquiped(int slot, L2ItemInstance item)
		{
			if (!(getOwner() instanceof L2PcInstance))
				return;
			
			L2PcInstance player = (L2PcInstance) getOwner();
			
			// checks if player worns chest item
			L2ItemInstance chestItem = getPaperdollItem(PAPERDOLL_CHEST);
			if (chestItem == null)
				return;
			
			// checks if there is armorset for chest item that player worns
			L2ArmorSet armorSet = ArmorSetsData.getInstance().getSet(chestItem.getItemId());
			if (armorSet == null)
				return;
			
			// checks if equiped item is part of set
			if (armorSet.containItem(slot, item.getItemId()))
			{
				if (armorSet.containAll(player))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(armorSet.getSkillId(), 1);
					if (skill != null)
					{
						player.addSkill(skill, false);
						player.sendSkillList();
					}
					else
						_log.warning(Inventory.class.getName() + ": .ArmorSetListener: Incorrect skill: " + armorSet.getSkillId() + ".");
					
					if (armorSet.containShield(player)) // has shield from set
					{
						L2Skill skills = SkillTable.getInstance().getInfo(armorSet.getShieldSkillId(), 1);
						if (skills != null)
						{
							player.addSkill(skills, false);
							player.sendSkillList();
						}
						else
							_log.warning(Inventory.class.getName() + ": .ArmorSetListener: Incorrect skill: " + armorSet.getShieldSkillId() + ".");
					}
					if (armorSet.isEnchanted6(player)) // has all parts of set enchanted to 6 or more
					{
						int skillId = armorSet.getEnchant6skillId();
						if (skillId > 0)
						{
							L2Skill skille = SkillTable.getInstance().getInfo(skillId, 1);
							if (skille != null)
							{
								player.addSkill(skille, false);
								player.sendSkillList();
							}
							else
								_log.warning(Inventory.class.getName() + ": .ArmorSetListener: Incorrect skill: " + armorSet.getEnchant6skillId() + ".");
						}
					}
				}
			}
			else if (armorSet.containShield(item.getItemId()))
			{
				if (armorSet.containAll(player))
				{
					L2Skill skills = SkillTable.getInstance().getInfo(armorSet.getShieldSkillId(), 1);
					if (skills != null)
					{
						player.addSkill(skills, false);
						player.sendSkillList();
					}
					else
						_log.warning(Inventory.class.getName() + ": .ArmorSetListener: Incorrect skill: " + armorSet.getShieldSkillId() + ".");
				}
			}
		}
		
		@Override
		public void notifyUnequiped(int slot, L2ItemInstance item)
		{
			if (!(getOwner() instanceof L2PcInstance))
				return;
			
			L2PcInstance player = (L2PcInstance) getOwner();
			
			boolean remove = false;
			int removeSkillId1 = 0; // set skill
			int removeSkillId2 = 0; // shield skill
			int removeSkillId3 = 0; // enchant +6 skill
			
			if (slot == PAPERDOLL_CHEST)
			{
				L2ArmorSet armorSet = ArmorSetsData.getInstance().getSet(item.getItemId());
				if (armorSet == null)
					return;
				
				remove = true;
				removeSkillId1 = armorSet.getSkillId();
				removeSkillId2 = armorSet.getShieldSkillId();
				removeSkillId3 = armorSet.getEnchant6skillId();
			}
			else
			{
				L2ItemInstance chestItem = getPaperdollItem(PAPERDOLL_CHEST);
				if (chestItem == null)
					return;
				
				L2ArmorSet armorSet = ArmorSetsData.getInstance().getSet(chestItem.getItemId());
				if (armorSet == null)
					return;
				
				if (armorSet.containItem(slot, item.getItemId())) // removed part of set
				{
					remove = true;
					removeSkillId1 = armorSet.getSkillId();
					removeSkillId2 = armorSet.getShieldSkillId();
					removeSkillId3 = armorSet.getEnchant6skillId();
				}
				else if (armorSet.containShield(item.getItemId())) // removed shield
				{
					remove = true;
					removeSkillId2 = armorSet.getShieldSkillId();
				}
			}
			
			if (remove)
			{
				if (removeSkillId1 != 0)
				{
					L2Skill skill = SkillTable.getInstance().getInfo(removeSkillId1, 1);
					if (skill != null)
						player.removeSkill(skill);
					else
						_log.warning(Inventory.class.getName() + ": .ArmorSetListener: Incorrect skill: " + removeSkillId1 + ".");
				}
				if (removeSkillId2 != 0)
				{
					L2Skill skill = SkillTable.getInstance().getInfo(removeSkillId2, 1);
					if (skill != null)
						player.removeSkill(skill);
					else
						_log.warning(Inventory.class.getName() + ": .ArmorSetListener: Incorrect skill: " + removeSkillId2 + ".");
				}
				if (removeSkillId3 != 0)
				{
					L2Skill skill = SkillTable.getInstance().getInfo(removeSkillId3, 1);
					if (skill != null)
						player.removeSkill(skill);
					else
						_log.warning(Inventory.class.getName() + ": .ArmorSetListener: Incorrect skill: " + removeSkillId3 + ".");
				}
				player.sendSkillList();
			}
		}
	}
	
	protected Inventory()
	{
		_paperdoll = new L2ItemInstance[PAPERDOLL_TOTALSLOTS];
		_paperdollListeners = new ArrayList<>();
		addPaperdollListener(new ArmorSetListener());
		addPaperdollListener(new BowListener());
		addPaperdollListener(new ItemPassiveSkillsListener());
		addPaperdollListener(new ItemAugmentationSkillsListener());
		addPaperdollListener(new StatsListener());
	}
	
	protected abstract ItemLocation getEquipLocation();
	
	public ChangeRecorder newRecorder()
	{
		return new ChangeRecorder(this);
	}
	
	public L2ItemInstance dropItem(String process, L2ItemInstance item, L2PcInstance actor, L2Object reference)
	{
		synchronized (item)
		{
			if (!_items.containsKey(item.getObjectId()))
				return null;
			
			removeItem(item);
			item.setOwnerId(process, 0, actor, reference);
			item.setLocation(ItemLocation.VOID);
			item.setLastChange(L2ItemInstance.REMOVED);
			
			item.updateDatabase();
			refreshWeight();
		}
		return item;
	}
	
	public L2ItemInstance dropItem(String process, int objectId, int count, L2PcInstance actor, L2Object reference)
	{
		L2ItemInstance item = getItemByObjectId(objectId);
		if (item == null)
			return null;
		
		// Adjust item quantity and create new instance to drop
		if (item.getCount() > count)
		{
			item.changeCount(process, -count, actor, reference);
			item.setLastChange(L2ItemInstance.MODIFIED);
			item.updateDatabase();
			
			item = ItemTable.getInstance().createItem(process, item.getItemId(), count, actor, reference);
			
			item.updateDatabase();
			refreshWeight();
			return item;
		}
		
		// Directly drop entire item
		return dropItem(process, item, actor, reference);
	}
	
	@Override
	protected void addItem(L2ItemInstance item)
	{
		super.addItem(item);
		if (item.isEquipped())
			equipItem(item);
	}
	
	@Override
	protected boolean removeItem(L2ItemInstance item)
	{
		// Unequip item if equiped
		// if (item.isEquipped()) unEquipItemInSlotAndRecord(item.getEquipSlot());
		for (int i = 0; i < _paperdoll.length; i++)
			if (_paperdoll[i] == item)
				unEquipItemInSlot(i);
		
		return super.removeItem(item);
	}
	
	public L2ItemInstance getPaperdollItem(int slot)
	{
		return _paperdoll[slot];
	}
	
	public List<L2ItemInstance> getPaperdollItems()
	{
		final ArrayList<L2ItemInstance> itemsList = new ArrayList<>();
		
		for (final L2ItemInstance item : _paperdoll)
		{
			if (item != null)
				itemsList.add(item);
		}
		return itemsList;
	}
	
	public static int getPaperdollIndex(int slot)
	{
		switch (slot)
		{
			case L2Item.SLOT_UNDERWEAR:
				return PAPERDOLL_UNDER;
			case L2Item.SLOT_R_EAR:
				return PAPERDOLL_REAR;
			case L2Item.SLOT_L_EAR:
				return PAPERDOLL_LEAR;
			case L2Item.SLOT_NECK:
				return PAPERDOLL_NECK;
			case L2Item.SLOT_R_FINGER:
				return PAPERDOLL_RFINGER;
			case L2Item.SLOT_L_FINGER:
				return PAPERDOLL_LFINGER;
			case L2Item.SLOT_HEAD:
				return PAPERDOLL_HEAD;
			case L2Item.SLOT_R_HAND:
			case L2Item.SLOT_LR_HAND:
				return PAPERDOLL_RHAND;
			case L2Item.SLOT_L_HAND:
				return PAPERDOLL_LHAND;
			case L2Item.SLOT_GLOVES:
				return PAPERDOLL_GLOVES;
			case L2Item.SLOT_CHEST:
			case L2Item.SLOT_FULL_ARMOR:
			case L2Item.SLOT_ALLDRESS:
				return PAPERDOLL_CHEST;
			case L2Item.SLOT_LEGS:
				return PAPERDOLL_LEGS;
			case L2Item.SLOT_FEET:
				return PAPERDOLL_FEET;
			case L2Item.SLOT_BACK:
				return PAPERDOLL_BACK;
			case L2Item.SLOT_FACE:
			case L2Item.SLOT_HAIRALL:
				return PAPERDOLL_FACE;
			case L2Item.SLOT_HAIR:
				return PAPERDOLL_HAIR;
		}
		return -1;
	}
	
	public L2ItemInstance getPaperdollItemByL2ItemId(int slot)
	{
		int index = getPaperdollIndex(slot);
		return index == -1 ? null : _paperdoll[index];
	}
	
	public int getPaperdollItemId(int slot)
	{
		L2ItemInstance item = _paperdoll[slot];
		if (item != null)
			return item.getItemId();
		
		return 0;
	}
	
	public int getPaperdollAugmentationId(int slot)
	{
		L2ItemInstance item = _paperdoll[slot];
		if (item != null)
		{
			if (item.getAugmentation() != null)
				return item.getAugmentation().getAugmentationId();			
		}
		return 0;
	}
	
	public int getPaperdollObjectId(int slot)
	{
		L2ItemInstance item = _paperdoll[slot];
		if (item != null)
			return item.getObjectId();
		
		return 0;
	}
	
	public synchronized void addPaperdollListener(PaperdollListener listener)
	{
		_paperdollListeners.add(listener);
	}
	
	public synchronized void removePaperdollListener(PaperdollListener listener)
	{
		_paperdollListeners.remove(listener);
	}
	
	public synchronized L2ItemInstance setPaperdollItem(int slot, L2ItemInstance item)
	{
		L2ItemInstance old = _paperdoll[slot];
		
		if (old != item)
		{
			if (old != null)
			{
				_paperdoll[slot] = null;
				old.setLocation(getBaseLocation());
				old.setLastChange(L2ItemInstance.MODIFIED);
				
				_wearedMask &= ~old.getItem().getItemMask();
				
				for (PaperdollListener listener : _paperdollListeners)
				{
					if (listener == null)
						continue;
					
					listener.notifyUnequiped(slot, old);
				}
				old.updateDatabase();
			}
			if (item != null)
			{
				_paperdoll[slot] = item;
				item.setLocation(getEquipLocation(), slot);
				item.setLastChange(L2ItemInstance.MODIFIED);
				
				L2Item armor = item.getItem();
				
				if (armor.getBodyPart() == L2Item.SLOT_CHEST)
				{
					L2ItemInstance legs = _paperdoll[PAPERDOLL_LEGS];
					if (legs != null && legs.getItem().getItemMask() == armor.getItemMask())
						_wearedMask |= armor.getItemMask();
				}
				else if (armor.getBodyPart() == L2Item.SLOT_LEGS)
				{
					L2ItemInstance legs = _paperdoll[PAPERDOLL_CHEST];
					if (legs != null && legs.getItem().getItemMask() == armor.getItemMask())
						_wearedMask |= armor.getItemMask();
				}
				else
					_wearedMask |= armor.getItemMask();
				
				for (PaperdollListener listener : _paperdollListeners)
				{
					if (listener == null)
						continue;
					
					listener.notifyEquiped(slot, item);
				}
				item.updateDatabase();
			}
		}
		return old;
	}
	
	public int getWearedMask()
	{
		return _wearedMask;
	}
	
	public int getSlotFromItem(L2ItemInstance item)
	{
		int slot = -1;
		int location = item.getLocationSlot();
		
		switch (location)
		{
			case PAPERDOLL_UNDER:
				slot = L2Item.SLOT_UNDERWEAR;
				break;
			case PAPERDOLL_LEAR:
				slot = L2Item.SLOT_L_EAR;
				break;
			case PAPERDOLL_REAR:
				slot = L2Item.SLOT_R_EAR;
				break;
			case PAPERDOLL_NECK:
				slot = L2Item.SLOT_NECK;
				break;
			case PAPERDOLL_RFINGER:
				slot = L2Item.SLOT_R_FINGER;
				break;
			case PAPERDOLL_LFINGER:
				slot = L2Item.SLOT_L_FINGER;
				break;
			case PAPERDOLL_HAIR:
				slot = L2Item.SLOT_HAIR;
				break;
			case PAPERDOLL_FACE:
				slot = L2Item.SLOT_FACE;
				break;
			case PAPERDOLL_HEAD:
				slot = L2Item.SLOT_HEAD;
				break;
			case PAPERDOLL_RHAND:
				slot = L2Item.SLOT_R_HAND;
				break;
			case PAPERDOLL_LHAND:
				slot = L2Item.SLOT_L_HAND;
				break;
			case PAPERDOLL_GLOVES:
				slot = L2Item.SLOT_GLOVES;
				break;
			case PAPERDOLL_CHEST:
				slot = item.getItem().getBodyPart();
				break;
			case PAPERDOLL_LEGS:
				slot = L2Item.SLOT_LEGS;
				break;
			case PAPERDOLL_BACK:
				slot = L2Item.SLOT_BACK;
				break;
			case PAPERDOLL_FEET:
				slot = L2Item.SLOT_FEET;
				break;
		}
		
		return slot;
	}
	
	public L2ItemInstance[] unEquipItemInBodySlotAndRecord(int slot)
	{
		Inventory.ChangeRecorder recorder = newRecorder();
		
		try
		{
			unEquipItemInBodySlot(slot);
		}
		finally
		{
			removePaperdollListener(recorder);
		}
		return recorder.getChangedItems();
	}
	
	public synchronized L2ItemInstance[] unEquipItemInBodySlotAndRecord(L2ItemInstance item)
	{
		Inventory.ChangeRecorder recorder = newRecorder();
		try
		{
			unEquipItemInBodySlot(getSlotFromItem(item));
		}
		finally
		{
			removePaperdollListener(recorder);
		}
		return recorder.getChangedItems();
	}
	
	public synchronized L2ItemInstance unEquipItemInSlot(int pdollSlot)
	{
		return setPaperdollItem(pdollSlot, null);
	}
	
	public synchronized L2ItemInstance[] unEquipItemInSlotAndRecord(int slot)
	{
		
		Inventory.ChangeRecorder recorder = newRecorder();
		try
		{
			unEquipItemInSlot(slot);
			if (getOwner() instanceof L2PcInstance)
				((L2PcInstance) getOwner()).refreshExpertisePenalty();
		}
		finally
		{
			removePaperdollListener(recorder);
		}
		return recorder.getChangedItems();
	}
	
	public L2ItemInstance unEquipItemInBodySlot(int slot)
	{
		int pdollSlot = -1;
		
		switch (slot)
		{
			case L2Item.SLOT_L_EAR:
				pdollSlot = PAPERDOLL_LEAR;
				break;
			case L2Item.SLOT_R_EAR:
				pdollSlot = PAPERDOLL_REAR;
				break;
			case L2Item.SLOT_NECK:
				pdollSlot = PAPERDOLL_NECK;
				break;
			case L2Item.SLOT_R_FINGER:
				pdollSlot = PAPERDOLL_RFINGER;
				break;
			case L2Item.SLOT_L_FINGER:
				pdollSlot = PAPERDOLL_LFINGER;
				break;
			case L2Item.SLOT_HAIR:
				pdollSlot = PAPERDOLL_HAIR;
				break;
			case L2Item.SLOT_FACE:
				pdollSlot = PAPERDOLL_FACE;
				break;
			case L2Item.SLOT_HAIRALL:
				setPaperdollItem(PAPERDOLL_FACE, null);
				pdollSlot = PAPERDOLL_FACE;
				break;
			case L2Item.SLOT_HEAD:
				pdollSlot = PAPERDOLL_HEAD;
				break;
			case L2Item.SLOT_R_HAND:
			case L2Item.SLOT_LR_HAND:
				pdollSlot = PAPERDOLL_RHAND;
				break;
			case L2Item.SLOT_L_HAND:
				pdollSlot = PAPERDOLL_LHAND;
				break;
			case L2Item.SLOT_GLOVES:
				pdollSlot = PAPERDOLL_GLOVES;
				break;
			case L2Item.SLOT_CHEST:
			case L2Item.SLOT_FULL_ARMOR:
			case L2Item.SLOT_ALLDRESS:
				pdollSlot = PAPERDOLL_CHEST;
				break;
			case L2Item.SLOT_LEGS:
				pdollSlot = PAPERDOLL_LEGS;
				break;
			case L2Item.SLOT_BACK:
				pdollSlot = PAPERDOLL_BACK;
				break;
			case L2Item.SLOT_FEET:
				pdollSlot = PAPERDOLL_FEET;
				break;
			case L2Item.SLOT_UNDERWEAR:
				pdollSlot = PAPERDOLL_UNDER;
				break;
			default:
				_log.warning(Inventory.class.getName() + ": Slot type is unhandled :" + slot);
		}
		
		if (pdollSlot >= 0)
		{
			L2ItemInstance old = setPaperdollItem(pdollSlot, null);
			if (old != null)
			{
				if (getOwner().isPlayer())
					((L2PcInstance) getOwner()).refreshExpertisePenalty();
			}
			return old;
		}
		return null;
	}
	
	public L2ItemInstance[] equipItemAndRecord(L2ItemInstance item)
	{
		Inventory.ChangeRecorder recorder = newRecorder();
		
		try
		{
			equipItem(item);
		}
		finally
		{
			removePaperdollListener(recorder);
		}
		
		return recorder.getChangedItems();
	}
	
	public void equipItem(L2ItemInstance item)
	{
		if (getOwner().isPlayer())
		{
			if (((L2PcInstance) getOwner()).getPrivateStoreType() != StoreType.NONE || item.isHeroItem() && !((L2PcInstance) getOwner()).isHero())
				return;
		}
		
		int targetSlot = item.getItem().getBodyPart();
		
		// check if player wear formal
		L2ItemInstance formal = getPaperdollItem(PAPERDOLL_CHEST);
		if (formal != null && formal.getItem().getBodyPart() == L2Item.SLOT_ALLDRESS)
		{
			// only chest target can pass this
			switch (targetSlot)
			{
				case L2Item.SLOT_LR_HAND:
				case L2Item.SLOT_L_HAND:
				case L2Item.SLOT_R_HAND:
					unEquipItemInBodySlotAndRecord(L2Item.SLOT_ALLDRESS);
					break;
				case L2Item.SLOT_LEGS:
				case L2Item.SLOT_FEET:
				case L2Item.SLOT_GLOVES:
				case L2Item.SLOT_HEAD:
					return;
			}
		}
		
		switch (targetSlot)
		{
			case L2Item.SLOT_LR_HAND:
				setPaperdollItem(PAPERDOLL_LHAND, null);
				setPaperdollItem(PAPERDOLL_RHAND, item);
				break;
			
			case L2Item.SLOT_L_HAND:
				L2ItemInstance rh = getPaperdollItem(PAPERDOLL_RHAND);
				if (rh != null && rh.getItem().getBodyPart() == L2Item.SLOT_LR_HAND && !((rh.getItemType() == L2WeaponType.BOW && item.getItemType() == L2EtcItemType.ARROW) || (rh.getItemType() == L2WeaponType.ROD)))
					setPaperdollItem(PAPERDOLL_RHAND, null);
				
				setPaperdollItem(PAPERDOLL_LHAND, item);
				break;
			
			case L2Item.SLOT_R_HAND:
				setPaperdollItem(PAPERDOLL_RHAND, item);
				break;
			
			case L2Item.SLOT_L_EAR:
			case L2Item.SLOT_R_EAR:
			case L2Item.SLOT_L_EAR | L2Item.SLOT_R_EAR:
				if (_paperdoll[PAPERDOLL_LEAR] == null)
					setPaperdollItem(PAPERDOLL_LEAR, item);
				else if (_paperdoll[PAPERDOLL_REAR] == null)
					setPaperdollItem(PAPERDOLL_REAR, item);
				else
				{
					if (_paperdoll[PAPERDOLL_REAR].getItemId() == item.getItemId())
						setPaperdollItem(PAPERDOLL_LEAR, item);
					else if (_paperdoll[PAPERDOLL_LEAR].getItemId() == item.getItemId())
						setPaperdollItem(PAPERDOLL_REAR, item);
					else
						setPaperdollItem(PAPERDOLL_LEAR, item);
				}
				break;
			
			case L2Item.SLOT_L_FINGER:
			case L2Item.SLOT_R_FINGER:
			case L2Item.SLOT_L_FINGER | L2Item.SLOT_R_FINGER:
				if (_paperdoll[PAPERDOLL_LFINGER] == null)
					setPaperdollItem(PAPERDOLL_LFINGER, item);
				else if (_paperdoll[PAPERDOLL_RFINGER] == null)
					setPaperdollItem(PAPERDOLL_RFINGER, item);
				else
				{
					if (_paperdoll[PAPERDOLL_RFINGER].getItemId() == item.getItemId())
						setPaperdollItem(PAPERDOLL_LFINGER, item);
					else if (_paperdoll[PAPERDOLL_LFINGER].getItemId() == item.getItemId())
						setPaperdollItem(PAPERDOLL_RFINGER, item);
					else
						setPaperdollItem(PAPERDOLL_LFINGER, item);
				}
				break;
			
			case L2Item.SLOT_NECK:
				setPaperdollItem(PAPERDOLL_NECK, item);
				break;
			
			case L2Item.SLOT_FULL_ARMOR:
				setPaperdollItem(PAPERDOLL_LEGS, null);
				setPaperdollItem(PAPERDOLL_CHEST, item);
				break;
			
			case L2Item.SLOT_CHEST:
				setPaperdollItem(PAPERDOLL_CHEST, item);
				break;
			
			case L2Item.SLOT_LEGS:
				// handle full armor
				L2ItemInstance chest = getPaperdollItem(PAPERDOLL_CHEST);
				if (chest != null && chest.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				
				setPaperdollItem(PAPERDOLL_LEGS, item);
				break;
			
			case L2Item.SLOT_FEET:
				setPaperdollItem(PAPERDOLL_FEET, item);
				break;
			
			case L2Item.SLOT_GLOVES:
				setPaperdollItem(PAPERDOLL_GLOVES, item);
				break;
			
			case L2Item.SLOT_HEAD:
				setPaperdollItem(PAPERDOLL_HEAD, item);
				break;
			
			case L2Item.SLOT_FACE:
				L2ItemInstance hair = getPaperdollItem(PAPERDOLL_HAIR);
				if (hair != null && hair.getItem().getBodyPart() == L2Item.SLOT_HAIRALL)
					setPaperdollItem(PAPERDOLL_HAIR, null);
				
				setPaperdollItem(PAPERDOLL_FACE, item);
				break;
			
			case L2Item.SLOT_HAIR:
				L2ItemInstance face = getPaperdollItem(PAPERDOLL_FACE);
				if (face != null && face.getItem().getBodyPart() == L2Item.SLOT_HAIRALL)
					setPaperdollItem(PAPERDOLL_FACE, null);
				
				setPaperdollItem(PAPERDOLL_HAIR, item);
				break;
			
			case L2Item.SLOT_HAIRALL:
				setPaperdollItem(PAPERDOLL_FACE, null);
				setPaperdollItem(PAPERDOLL_HAIR, item);
				break;
			
			case L2Item.SLOT_UNDERWEAR:
				setPaperdollItem(PAPERDOLL_UNDER, item);
				break;
			
			case L2Item.SLOT_BACK:
				setPaperdollItem(PAPERDOLL_BACK, item);
				break;
			
			case L2Item.SLOT_ALLDRESS:
				// formal dress
				setPaperdollItem(PAPERDOLL_LEGS, null);
				setPaperdollItem(PAPERDOLL_LHAND, null);
				setPaperdollItem(PAPERDOLL_RHAND, null);
				setPaperdollItem(PAPERDOLL_HEAD, null);
				setPaperdollItem(PAPERDOLL_FEET, null);
				setPaperdollItem(PAPERDOLL_GLOVES, null);
				setPaperdollItem(PAPERDOLL_CHEST, item);
				break;
			
			default:
				_log.warning(Inventory.class.getName() + ": unknown body slot:" + targetSlot);
		}
	}
	
	@Override
	protected void refreshWeight()
	{
		try
		{
			_totalWeight = !Config.DISABLE_WEIGHT_PENALTY ? Math.toIntExact(_items.values().stream().filter(Objects::nonNull).mapToLong(L2ItemInstance::getTotalWeight).reduce(Math::addExact).orElse(0L)) : 0;
		}
		catch (ArithmeticException ae)
		{
			_totalWeight = Integer.MAX_VALUE;
		}
	}
	
	public int getTotalWeight()
	{
		return _totalWeight;
	}
	
	public L2ItemInstance findArrowForBow(L2Item bow)
	{
		int arrowsId = 0;
		
		switch (bow.getCrystalType())
		{
			default: // broken weapon.csv ??
			case NONE:
				arrowsId = 17;
				break; // Wooden arrow
			case D:
				arrowsId = 1341;
				break; // Bone arrow
			case C:
				arrowsId = 1342;
				break; // Fine steel arrow
			case B:
				arrowsId = 1343;
				break; // Silver arrow
			case A:
				arrowsId = 1344;
				break; // Mithril arrow
			case S:
				arrowsId = 1345;
				break; // Shining arrow
		}
		
		// Get the L2ItemInstance corresponding to the item identifier and return it
		return getItemByItemId(arrowsId);
	}
	
	@Override
	public void restore()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement("SELECT object_id, item_id, count, enchant_level, loc, loc_data, price_sell, price_buy, custom_type1, custom_type2, mana_left, time_of_use FROM items WHERE owner_id=? AND (loc=? OR loc=?) ORDER BY loc_data"))
		{
			ps.setInt(1, getOwnerId());
			ps.setString(2, getBaseLocation().name());
			ps.setString(3, getEquipLocation().name());
			
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					// Restore the item.
					int objectId = rs.getInt(1);
					final L2ItemInstance item = L2ItemInstance.restoreFromDb(getOwnerId(), rs);
					
					if (item == null)
						continue;
						
					if (L2World.getInstance().findObject(objectId) != null)
					{
						_log.warning(Inventory.class.getSimpleName() + ": Item: " + item.getObjectId() + " Has duplied on world and cannot be load");
						L2World.getInstance().removeObject(item);
						if (getOwner() instanceof L2PcInstance)
						{
							final L2PcInstance player = (L2PcInstance) getOwner();
							player.closeNetConnection(false);
						}
						continue;
					}
					
					if (getOwner().isPlayer())
					{
						final L2PcInstance player = (L2PcInstance) getOwner();
						
						if (!player.isGM())
						{
							if (!player.isHero() && item.isHeroItem())
								item.setLocation(ItemLocation.INVENTORY);
						}
					}
					
					L2World.getInstance().storeObject(item);

					// If stackable item is found in inventory just add to current quantity
					if (item.isStackable() && getItemByItemId(item.getItemId()) != null)
						addItem("Restore", item, null, getOwner());
					else
						addItem(item);
				}
			}
		}
		catch (Exception e)
		{
			_log.warning("Owner id : " + getOwnerId() + ", Could not restore inventory : " + e);
		}
		refreshWeight();
	}
	
	public void reloadEquippedItems()
	{	
		for (L2ItemInstance element : _paperdoll)
		{
			if (element == null)
				continue;
			int slot = element.getEquipSlot();
			
			for (PaperdollListener listener : _paperdollListeners)
			{
				if (listener == null)
					continue;
				listener.notifyUnequiped(slot, element);
				listener.notifyEquiped(slot, element);
			}
		}
	}
}