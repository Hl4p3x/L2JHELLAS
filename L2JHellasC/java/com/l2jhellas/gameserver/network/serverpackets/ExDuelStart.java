package com.l2jhellas.gameserver.network.serverpackets;

public class ExDuelStart extends L2GameServerPacket
{
	private static final String _S__FE_4D_EXDUELSTART = "[S] FE:4D ExDuelStart";
	private final int _unk1;
	
	public ExDuelStart(int unk1)
	{
		_unk1 = unk1;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x4d);
		writeD(_unk1);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_4D_EXDUELSTART;
	}
}