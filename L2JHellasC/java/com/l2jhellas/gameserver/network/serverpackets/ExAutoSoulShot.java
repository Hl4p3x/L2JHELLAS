package com.l2jhellas.gameserver.network.serverpackets;

public class ExAutoSoulShot extends L2GameServerPacket
{
	private static final String _S__FE_12_EXAUTOSOULSHOT = "[S] FE:12 ExAutoSoulShot";
	private final int _itemId;
	private final int _type;
	
	public ExAutoSoulShot(int itemId, int type)
	{
		_itemId = itemId;
		_type = type;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0x12); // sub id
		writeD(_itemId);
		writeD(_type);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_12_EXAUTOSOULSHOT;
	}
}