package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;

public class TradeStart extends L2GameServerPacket
{
	private static final String _S__2E_TRADESTART = "[S] 1E TradeStart";
	private final L2PcInstance _activeChar;
	private final L2ItemInstance[] _itemList;
	
	public TradeStart(L2PcInstance player)
	{
		_activeChar = player;
		_itemList = _activeChar.getInventory().getAvailableItems(true);
	}
	
	@Override
	protected final void writeImpl()
	{
		
		if (_activeChar.getActiveEnchantItem() != null || _activeChar.getActiveWarehouse() != null || _activeChar.getActiveTradeList().getPartner().getActiveEnchantItem() != null || _activeChar.getActiveTradeList().getPartner().getActiveWarehouse() != null)
			return;
		
		// 0x2e TradeStart d h (h dddhh dhhh)
		if (_activeChar.getActiveTradeList() == null || _activeChar.getActiveTradeList().getPartner() == null)
			return;
		
		writeC(0x1E);
		writeD(_activeChar.getActiveTradeList().getPartner().getObjectId());
		// writeD((_activeChar != null || _activeChar.getTransactionRequester() != null)? _activeChar.getTransactionRequester().getObjectId() : 0);
		
		writeH(_itemList.length);
		for (L2ItemInstance item : _itemList)// int i = 0; i < count; i++)
		{
			writeH(item.getItem().getType1());// item type1
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeD(item.getCount());
			writeH(item.getItem().getType2());// item type2
			writeH(0x00);// ?
			
			writeD(item.getItem().getBodyPart());// slot 0006-lr.ear 0008-neck 0030-lr.finger 0040-head 0080-?? 0100-l.hand 0200-gloves 0400-chest 0800-pants 1000-feet 2000-?? 4000-r.hand 8000-r.hand
			writeH(item.getEnchantLevel());// enchant level
			writeH(0x00);// ?
			writeH(0x00);
		}
	}
	
	@Override
	public String getType()
	{
		return _S__2E_TRADESTART;
	}
}