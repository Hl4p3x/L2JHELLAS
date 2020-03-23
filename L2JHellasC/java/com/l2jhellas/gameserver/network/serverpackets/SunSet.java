package com.l2jhellas.gameserver.network.serverpackets;

public class SunSet extends L2GameServerPacket
{
	private static final String _S__29_SUNSET = "[S] 1d SunSet";
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x1d);
	}
	
	@Override
	public String getType()
	{
		return _S__29_SUNSET;
	}
}