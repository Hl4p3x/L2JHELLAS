package com.l2jhellas.gameserver.model.actor.item;

import java.util.Objects;


public class ItemInfo
{
	
	private int _objectId;
	
	private L2Item _item;
	
	private int _enchant;
	
	private int _augmentation;
	
	private int _count;
	
	private int _price;
	
	private int _type1;
	private int _type2;
	
	private int _equipped;
	
	private int _change;
	
	private int _mana;
	
	public ItemInfo(L2ItemInstance item)
	{
		Objects.requireNonNull(item);
		
		// Get the Identifier of the L2ItemInstance
		_objectId = item.getObjectId();
		
		// Get the L2Item of the L2ItemInstance
		_item = item.getItem();
		
		// Get the enchant level of the L2ItemInstance
		_enchant = item.getEnchantLevel();
		
		// Get the augmentation boni
		_augmentation = item.isAugmented() ? item.getAugmentation().getAugmentationId() : 0;
		
		// Get the quantity of the L2ItemInstance
		_count = item.getCount();
		
		// Get custom item types (used loto, race tickets)
		_type1 = item.getCustomType1();
		_type2 = item.getCustomType2();
		
		// Verify if the L2ItemInstance is equipped
		_equipped = item.isEquipped() ? 1 : 0;
		
		// Get the action to do clientside
		switch (item.getLastChange())
		{
			case (L2ItemInstance.ADDED):
			{
				_change = 1;
				break;
			}
			case (L2ItemInstance.MODIFIED):
			{
				_change = 2;
				break;
			}
			case (L2ItemInstance.REMOVED):
			{
				_change = 3;
				break;
			}
		}
		
		// Get shadow item mana
		_mana = item.getMana();
	}
	
	public ItemInfo(L2ItemInstance item, int change)
	{
		if (item == null)
			return;
		
		// Get the Identifier of the L2ItemInstance
		_objectId = item.getObjectId();
		
		// Get the L2Item of the L2ItemInstance
		_item = item.getItem();
		
		// Get the enchant level of the L2ItemInstance
		_enchant = item.getEnchantLevel();
		
		// Get the augmentation boni
		_augmentation = item.isAugmented() ? item.getAugmentation().getAugmentationId() : 0;

		// Get the quantity of the L2ItemInstance
		_count = item.getCount();
		
		// Get custom item types (used loto, race tickets)
		_type1 = item.getCustomType1();
		_type2 = item.getCustomType2();
		
		// Verify if the L2ItemInstance is equipped
		_equipped = item.isEquipped() ? 1 : 0;
		
		// Get the action to do clientside
		_change = change;
		
		// Get shadow item mana
		_mana = item.getMana();
	}
	
	public int getObjectId()
	{
		return _objectId;
	}
	
	public L2Item getItem()
	{
		return _item;
	}
	
	public int getEnchant()
	{
		return _enchant;
	}
	
	public int getAugemtationBoni()
	{
		return _augmentation;
	}
	
	public int getCount()
	{
		return _count;
	}
	
	public int getPrice()
	{
		return _price;
	}
	
	public int getCustomType1()
	{
		return _type1;
	}
	
	public int getCustomType2()
	{
		return _type2;
	}
	
	public int getEquipped()
	{
		return _equipped;
	}
	
	public int getChange()
	{
		return _change;
	}
	
	public int getMana()
	{
		return _mana;
	}
}