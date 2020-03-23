package com.l2jhellas.gameserver.network.serverpackets;

public class ExConfirmVariationItem extends L2GameServerPacket
{
	private static final String _S__FE_52_EXCONFIRMVARIATIONITEM = "[S] FE:52 ExConfirmVariationItem";
	
	private final int _itemObjId;
	private final int _unk1;
	private final int _unk2;
	
	public ExConfirmVariationItem(int itemObjId)
	{
		_itemObjId = itemObjId;
		_unk1 = 1;
		_unk2 = 1;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x52);
		writeD(_itemObjId);
		writeD(_unk1);
		writeD(_unk2);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_52_EXCONFIRMVARIATIONITEM;
	}
}