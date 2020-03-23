package com.l2jhellas.gameserver.network.serverpackets;

public class ExCloseMPCC extends L2GameServerPacket
{
	private static final String _S__FE_26_EXCLOSEMPCC = "[S] FE:26 ExCloseMPCC";
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x26);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_26_EXCLOSEMPCC;
	}
}