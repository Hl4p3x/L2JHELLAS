package com.l2jhellas.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import com.l2jhellas.gameserver.model.L2TradeList;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;

public final class BuyListSeed extends L2GameServerPacket
{
	private static final String _S__E8_BUYLISTSEED = "[S] E8 BuyListSeed";
	
	private final int _manorId;
	private List<L2ItemInstance> _list = new ArrayList<>();
	private final int _money;
	
	public BuyListSeed(L2TradeList list, int manorId, int currentMoney)
	{
		_money = currentMoney;
		_manorId = manorId;
		_list = list.getItems();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xE8);
		
		writeD(_money); // current money
		writeD(_manorId); // manor id
		
		writeH(_list.size()); // list length
		
		for (L2ItemInstance item : _list)
		{
			writeH(0x04); // item->type1
			writeD(0x00); // objectId
			writeD(item.getItemId()); // item id
			writeD(item.getCount()); // item count
			writeH(0x04); // item->type2
			writeH(0x00); // unknown :)
			writeD(item.getPriceToSell());// price
		}
	}
	
	@Override
	public String getType()
	{
		return _S__E8_BUYLISTSEED;
	}
}