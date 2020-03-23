package com.l2jhellas.gameserver.network.serverpackets;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import com.l2jhellas.gameserver.model.actor.item.ItemInfo;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;

public class InventoryUpdate extends L2GameServerPacket
{
	private static final String _S__37_INVENTORYUPDATE = "[S] 27 InventoryUpdate";
	private final Map<Integer, ItemInfo> _items = new ConcurrentSkipListMap<>();

	public InventoryUpdate()
	{

	}
	
	public InventoryUpdate(List<ItemInfo> items)
	{
		for (ItemInfo item : items)
		{
			_items.put(item.getObjectId(), item);
		}
	}
	
	public void addItem(L2ItemInstance item)
	{
		if (item != null)
			_items.put(item.getObjectId(),new ItemInfo(item));
	}
	
	public void addNewItem(L2ItemInstance item)
	{
		if (item != null)
			_items.put(item.getObjectId(),new ItemInfo(item, 1));
	}
	
	public void addModifiedItem(L2ItemInstance item)
	{
		if (item != null)
			_items.put(item.getObjectId(),new ItemInfo(item, 2));
	}
	
	public void addRemovedItem(L2ItemInstance item)
	{
		if (item != null)
			_items.put(item.getObjectId(),new ItemInfo(item, 3));
	}
	
	public void addItems(List<L2ItemInstance> items)
	{
		if (items != null)
		for (L2ItemInstance item : items)
			if (item != null)
			_items.put(item.getObjectId(), new ItemInfo(item));
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x27);
		int count = _items.size();
		writeH(count);
		for (ItemInfo item : _items.values())
		{
			writeH(item.getChange()); // Update type : 01-add, 02-modify, 03-remove
			writeH(item.getItem().getType1()); // Item Type 1 : 00-weapon/ring/earring/necklace, 01-armor/shield, 04-item/questitem/adena
			
			writeD(item.getObjectId()); // ObjectId
			writeD(item.getItem().getItemId()); // ItemId
			writeD(item.getCount()); // Quantity
			writeH(item.getItem().getType2()); // Item Type 2 : 00-weapon, 01-shield/armor, 02-ring/earring/necklace, 03-questitem, 04-adena, 05-item
			writeH(item.getCustomType1()); // Filler (always 0)
			writeH(item.getEquipped()); // Equipped : 00-No, 01-yes
			writeD(item.getItem().getBodyPart()); // Slot : 0006-lr.ear, 0008-neck, 0030-lr.finger, 0040-head, 0100-l.hand, 0200-gloves, 0400-chest, 0800-pants, 1000-feet,
			// 4000-r.hand, 8000-r.hand
			writeH(item.getEnchant()); // Enchant level (pet level shown in control item)
			writeH(item.getCustomType2()); // Pet name exists or not shown in control item
			
			writeD(item.getAugemtationBoni());
			writeD(item.getMana());
		}
	}
	
	@Override
	public String getType()
	{
		return _S__37_INVENTORYUPDATE;
	}
}