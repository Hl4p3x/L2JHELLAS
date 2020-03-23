package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.TradeList;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class PrivateStoreListBuy extends L2GameServerPacket
{
	// private static final String _S__D1_PRIVATEBUYLISTBUY = "[S] b8 PrivateBuyListBuy";
	private static final String _S__D1_PRIVATESTORELISTBUY = "[S] b8 PrivateStoreListBuy";
	private final L2PcInstance _storePlayer;
	private final L2PcInstance _activeChar;
	private final int _playerAdena;
	private final TradeList.TradeItem[] _items;
	
	public PrivateStoreListBuy(L2PcInstance player, L2PcInstance storePlayer)
	{
		_storePlayer = storePlayer;
		_activeChar = player;
		_playerAdena = _activeChar.getAdena();
		_storePlayer.getSellList().updateItems(); // Update SellList for case inventory content has changed
		_items = _storePlayer.getBuyList().getAvailableItems(_activeChar.getInventory());
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xb8);
		writeD(_storePlayer.getObjectId());
		writeD(_playerAdena);
		
		writeD(_items.length);
		
		for (TradeList.TradeItem item : _items)
		{
			writeD(item.getObjectId());
			writeD(item.getItem().getItemId());
			writeH(item.getEnchant());
			writeD(item.getCount()); // give max possible sell amount
			
			writeD(item.getItem().getReferencePrice());
			writeH(0);
			
			writeD(item.getItem().getBodyPart());
			writeH(item.getItem().getType2());
			writeD(item.getPrice()); // buyers price
			
			writeD(item.getCount()); // maximum possible tradecount
		}
	}
	
	@Override
	public String getType()
	{
		return _S__D1_PRIVATESTORELISTBUY;
	}
}