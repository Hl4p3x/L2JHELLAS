package com.l2jhellas.gameserver.network.serverpackets;

public class ExOpenMPCC extends L2GameServerPacket
{
	private static final String _S__FE_25_EXOPENMPCC = "[S] FE:25 ExOpenMPCC";
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x25);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_25_EXOPENMPCC;
	}
}