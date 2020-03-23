package com.l2jhellas.gameserver.network.serverpackets;

public class ExRequestHackShield extends L2GameServerPacket
{
	private static final String _S__FE_48_EXREQUESTHACKSHIELD = "[S] FE:48 ExRequestHackShield";
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x48);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_48_EXREQUESTHACKSHIELD;
	}
}