package com.l2jhellas.gameserver.network.serverpackets;

public class ObservationMode extends L2GameServerPacket
{
	// ddSS
	private static final String _S__DF_OBSERVMODE = "[S] DF ObservationMode";
	private final int _x, _y, _z;
	
	public ObservationMode(int x, int y, int z)
	{
		_x = x;
		_y = y;
		_z = z;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xdf);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeC(0x00);
		writeC(0xc0);
		writeC(0x00);
	}
	
	@Override
	public String getType()
	{
		return _S__DF_OBSERVMODE;
	}
}