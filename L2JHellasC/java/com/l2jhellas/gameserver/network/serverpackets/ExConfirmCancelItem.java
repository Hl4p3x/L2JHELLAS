package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;

public class ExConfirmCancelItem extends L2GameServerPacket
{
	private static final String _S__FE_56_EXCONFIRMCANCELITEM = "[S] FE:56 ExConfirmCancelItem";
	
	private final int _itemObjId;
	private final int _itemId;
	private final int _itemAug1;
	private final int _itemAug2;
	private final int _price;
	
	public ExConfirmCancelItem(L2ItemInstance item, int price)
	{
		_itemObjId = item.getObjectId();
		_itemId = item.getItemId();
		_price = price;
		_itemAug1 = ((short) item.getAugmentation().getAugmentationId());
		_itemAug2 = item.getAugmentation().getAugmentationId() >> 16;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x56);
		writeD(_itemObjId);
		writeD(_itemId);
		writeD(_itemAug1);
		writeD(_itemAug2);
		writeQ(_price);
		writeD(0x01);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_56_EXCONFIRMCANCELITEM;
	}
}