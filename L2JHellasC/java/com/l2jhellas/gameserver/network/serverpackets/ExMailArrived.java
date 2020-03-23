package com.l2jhellas.gameserver.network.serverpackets;

public class ExMailArrived extends L2GameServerPacket
{
	private static final String _S__FE_2D_EXMAILARRIVED = "[S] FE:2D ExMailArrived";
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x2d);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_2D_EXMAILARRIVED;
	}
}