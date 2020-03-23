package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.TradeList.TradeItem;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class TradeOtherAdd extends L2GameServerPacket
{
	private static final String _S__31_TRADEOTHERADD = "[S] 21 TradeOtherAdd";
	private final TradeItem _item;
	
	public TradeOtherAdd(TradeItem item)
	{
		_item = item;
	}
	
	@Override
	protected final void writeImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		if (activeChar.getActiveTradeList() == null)
			return;
		
		if (activeChar.getActiveTradeList().getPartner().getActiveTradeList() == null)
			return;
		
		writeC(0x21);
		
		writeH(1); // item count
		
		writeH(_item.getItem().getType1()); // item type1
		writeD(_item.getObjectId());
		writeD(_item.getItem().getItemId());
		writeD(_item.getCount());
		writeH(_item.getItem().getType2()); // item type2
		writeH(0x00);
		
		writeD(_item.getItem().getBodyPart()); // slot
		writeH(_item.getEnchant()); // enchant level
		writeH(0x00);
		writeH(0x00);
	}
	
	@Override
	public String getType()
	{
		return _S__31_TRADEOTHERADD;
	}
}