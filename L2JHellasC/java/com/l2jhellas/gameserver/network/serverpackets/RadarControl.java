package com.l2jhellas.gameserver.network.serverpackets;

public class RadarControl extends L2GameServerPacket
{
	private static final String _S__EB_RadarControl = "[S] EB RadarControl";
	private final int _showRadar;
	private final int _type;
	private final int _x;
	private final int _y;
	private final int _z;
	
	public RadarControl(int showRadar, int type, int x, int y, int z)
	{
		_showRadar = showRadar; // showRader?? 0 = showradar; 1 = delete radar;
		_type = type; // radar type??
		_x = x;
		_y = y;
		_z = z;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xEB);
		writeD(_showRadar);
		writeD(_type); // maybe type
		writeD(_x); // x
		writeD(_y); // y
		writeD(_z); // z
	}
	
	@Override
	public String getType()
	{
		return _S__EB_RadarControl;
	}
}