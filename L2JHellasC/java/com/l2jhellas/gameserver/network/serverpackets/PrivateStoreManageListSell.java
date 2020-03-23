package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.TradeList;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class PrivateStoreManageListSell extends L2GameServerPacket
{
	private static final String _S__B3_PRIVATESELLLISTSELL = "[S] 9a PrivateSellListSell";
	private final L2PcInstance _activeChar;
	private final int _playerAdena;
	private final boolean _packageSale;
	private final TradeList.TradeItem[] _itemList;
	private final TradeList.TradeItem[] _sellList;
	
	public PrivateStoreManageListSell(L2PcInstance player, boolean isPackageSale)
	{
		_activeChar = player;
		_playerAdena = _activeChar.getAdena();
		_activeChar.getSellList().updateItems();
		_packageSale = (_activeChar.getSellList().isPackaged()) ? true : isPackageSale;
		_itemList = _activeChar.getInventory().getAvailableItems(_activeChar.getSellList());
		_sellList = _activeChar.getSellList().getItems();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x9a);
		// section 1
		writeD(_activeChar.getObjectId());
		writeD(_packageSale ? 1 : 0); // Package sell
		writeD(_playerAdena);
		
		// section2
		writeD(_itemList.length); // for potential sells
		for (TradeList.TradeItem item : _itemList)
		{
			writeD(item.getItem().getType2());
			writeD(item.getObjectId());
			writeD(item.getItem().getItemId());
			writeD(item.getCount());
			writeH(0);
			writeH(item.getEnchant());// enchant lvl
			writeH(0);
			writeD(item.getItem().getBodyPart());
			writeD(item.getPrice()); // store price
		}
		// section 3
		writeD(_sellList.length); // count for any items already added for sell
		for (TradeList.TradeItem item : _sellList)
		{
			writeD(item.getItem().getType2());
			writeD(item.getObjectId());
			writeD(item.getItem().getItemId());
			writeD(item.getCount());
			writeH(0);
			writeH(item.getEnchant());// enchant lvl
			writeH(0x00);
			writeD(item.getItem().getBodyPart());
			writeD(item.getPrice());// your price
			writeD(item.getItem().getReferencePrice()); // store price
		}
	}
	
	@Override
	public String getType()
	{
		return _S__B3_PRIVATESELLLISTSELL;
	}
}