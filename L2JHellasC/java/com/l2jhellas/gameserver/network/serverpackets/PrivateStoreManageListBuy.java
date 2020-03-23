package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.TradeList;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;

public class PrivateStoreManageListBuy extends L2GameServerPacket
{
	private static final String _S__D0_PRIVATESELLLISTBUY = "[S] b7 PrivateSellListBuy";
	private final L2PcInstance _activeChar;
	private final int _playerAdena;
	private final L2ItemInstance[] _itemList;
	private final TradeList.TradeItem[] _buyList;
	
	public PrivateStoreManageListBuy(L2PcInstance player)
	{
		_activeChar = player;
		_playerAdena = _activeChar.getAdena();
		_itemList = _activeChar.getInventory().getUniqueItems(false, true);
		_buyList = _activeChar.getBuyList().getItems();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xb7);
		// section 1
		writeD(_activeChar.getObjectId());
		writeD(_playerAdena);
		
		// section2
		writeD(_itemList.length); // inventory items for potential buy
		for (L2ItemInstance item : _itemList)
		{
			writeD(item.getItemId());
			writeH(0); // show enchant lvl as 0, as you can't buy enchanted weapons
			writeD(item.getCount());
			writeD(item.getReferencePrice());
			writeH(0x00);
			writeD(item.getItem().getBodyPart());
			writeH(item.getItem().getType2());
		}
		
		// section 3
		writeD(_buyList.length); // count for all items already added for buy
		for (TradeList.TradeItem item : _buyList)
		{
			writeD(item.getItem().getItemId());
			writeH(0);
			writeD(item.getCount());
			writeD(item.getItem().getReferencePrice());
			writeH(0x00);
			writeD(item.getItem().getBodyPart());
			writeH(item.getItem().getType2());
			writeD(item.getPrice());// your price
			writeD(item.getItem().getReferencePrice());// fixed store price
		}
	}
	
	@Override
	public String getType()
	{
		return _S__D0_PRIVATESELLLISTBUY;
	}
}