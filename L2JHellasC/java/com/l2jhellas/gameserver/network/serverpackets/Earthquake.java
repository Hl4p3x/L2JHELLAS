package com.l2jhellas.gameserver.network.serverpackets;

public class Earthquake extends L2GameServerPacket
{
	private static final String _S__C4_EARTHQUAKE = "[S] C4 Earthquake";
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _intensity;
	private final int _duration;
	private final int _isNpc;


	public Earthquake(int x, int y, int z, int intensity, int duration)
	{
		_x = x;
		_y = y;
		_z = z;
		_intensity = intensity;
		_duration = duration;
		_isNpc = 0;
	}
	
	public Earthquake(int x, int y, int z, int intensity, int duration , boolean isNpc)
	{
		_x = x;
		_y = y;
		_z = z;
		_intensity = intensity;
		_duration = duration;
		_isNpc = (isNpc) ? 1 : 0;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xc4);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_intensity);
		writeD(_duration);
		writeD(_isNpc);
	}
	
	@Override
	public String getType()
	{
		return _S__C4_EARTHQUAKE;
	}
}