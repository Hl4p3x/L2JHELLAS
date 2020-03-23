package com.l2jhellas.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;

public class GMViewItemList extends L2GameServerPacket
{
	private static final String _S__AD_GMVIEWITEMLIST = "[S] 94 GMViewItemList";
	private final List<L2ItemInstance> _items = new ArrayList<>();

	private final int _limit;
	private final String _playerName;
	
	public GMViewItemList(L2PcInstance cha)
	{
		_playerName = cha.getName();
		_limit = cha.getInventoryLimit();
		
		for (L2ItemInstance item : cha.getInventory().getItems())
		{
			_items.add(item);
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x94);
		writeS(_playerName);
		writeD(_limit); // inventory limit
		writeH(0x01); // show window ??
		writeH(_items.size());
		
		for (L2ItemInstance temp : _items)
		{
			if (temp == null || temp.getItem() == null)
				continue;
			
			writeH(temp.getItem().getType1());
			
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getCount());
			writeH(temp.getItem().getType2());
			writeH(temp.getCustomType1());
			writeH(temp.isEquipped() ? 0x01 : 0x00);
			writeD(temp.getItem().getBodyPart());
			writeH(temp.getEnchantLevel());
			writeH(temp.getCustomType2());
			writeD((temp.isAugmented()) ? temp.getAugmentation().getAugmentationId() : 0x00);
			writeD(temp.getMana());
		}
	}
	
	@Override
	public String getType()
	{
		return _S__AD_GMVIEWITEMLIST;
	}
}