package com.l2jhellas.gameserver.network.serverpackets;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;

public class ItemList extends L2GameServerPacket
{
	private static final String _S__27_ITEMLIST = "[S] 1b ItemList";	
	
	private final List<L2ItemInstance> _items;
	private final boolean _showWindow;
	
	public ItemList(L2PcInstance cha, boolean showWindow)
	{
		_items = cha.getInventory().getItems(Objects::nonNull).stream().collect(Collectors.toList());
		_showWindow = showWindow;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x1b);
		writeH(_showWindow ? 0x01 : 0x00);
		
		writeH(_items.size());
		
		for (L2ItemInstance temp : _items)
		{
			if ((temp == null) || (temp.getItem() == null))
				continue;
			
			writeH(temp.getItem().getType1()); // item type1
			
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getCount());
			writeH(temp.getItem().getType2()); // item type2
			writeH(temp.getCustomType1()); // item type3
			writeH(temp.isEquipped() ? 0x01 : 0x00);
			writeD(temp.getItem().getBodyPart());
			
			writeH(temp.getEnchantLevel()); // enchant level
			// race tickets
			writeH(temp.getCustomType2()); // item type3

			writeD(temp.isAugmented() ? temp.getAugmentation().getAugmentationId() : 0x00);
			
			writeD(temp.getMana());
		}
	}
	
	@Override
	public String getType()
	{
		return _S__27_ITEMLIST;
	}
}