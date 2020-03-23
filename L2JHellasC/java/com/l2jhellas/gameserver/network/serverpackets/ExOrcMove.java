package com.l2jhellas.gameserver.network.serverpackets;

public class ExOrcMove extends L2GameServerPacket
{
	private static final String _S__FE_44_EXORCMOVE = "[S] FE:44 ExOrcMove";
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x44);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_44_EXORCMOVE;
	}
}