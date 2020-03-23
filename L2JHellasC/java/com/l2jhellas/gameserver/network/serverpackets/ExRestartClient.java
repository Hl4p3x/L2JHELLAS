package com.l2jhellas.gameserver.network.serverpackets;

public class ExRestartClient extends L2GameServerPacket
{
	private static final String _S__FE_47_EXRESTARTCLIENT = "[S] FE:47 ExRestartClient";
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x47);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_47_EXRESTARTCLIENT;
	}
}