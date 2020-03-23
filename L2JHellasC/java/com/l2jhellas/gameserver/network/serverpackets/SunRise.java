package com.l2jhellas.gameserver.network.serverpackets;

public class SunRise extends L2GameServerPacket
{
	private static final String _S__28_SUNRISE = "[S] 1c SunRise";
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x1c);
	}
	
	@Override
	public String getType()
	{
		return _S__28_SUNRISE;
	}
}