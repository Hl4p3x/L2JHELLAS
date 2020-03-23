package com.l2jhellas.gameserver.network.serverpackets;

public class ExRedSky extends L2GameServerPacket
{
	private static final String _S__FE_40_EXREDSKYPACKET = "[S] FE:40 ExRedSkyPacket";
	private final int _duration;
	
	public ExRedSky(int duration)
	{
		_duration = duration;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x40);
		writeD(_duration);
	}
	
	@Override
	public String getType()
	{
		return _S__FE_40_EXREDSKYPACKET;
	}
}