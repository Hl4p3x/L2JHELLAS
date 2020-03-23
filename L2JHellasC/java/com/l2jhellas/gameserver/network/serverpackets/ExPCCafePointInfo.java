package com.l2jhellas.gameserver.network.serverpackets;

public class ExPCCafePointInfo extends L2GameServerPacket
{
	private static final String _S__FE_31_EXPCCAFEPOINTINFO = "[S] FE:31 ExPCCafePointInfo";
	private final int _unk1, _unk2, _unk3, _unk4;
	private int _unk5 = 0;
	
	public ExPCCafePointInfo(int val1, int val2, int val3, int val4, int val5)
	{
		_unk1 = val1;
		_unk2 = val2;
		_unk3 = val3;
		_unk4 = val4;
		_unk5 = val5;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x31);
		writeD(_unk1);
		writeD(_unk2);
		writeC(_unk3);
		writeD(_unk4);
		writeC(_unk5);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_31_EXPCCAFEPOINTINFO;
	}
}