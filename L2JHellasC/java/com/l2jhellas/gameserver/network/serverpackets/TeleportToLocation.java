package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.L2Object;

public class TeleportToLocation extends L2GameServerPacket
{
	private static final String _S__38_TELEPORTTOLOCATION = "[S] 28 TeleportToLocation";
	private final int _targetObjId;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _h;
	
	public TeleportToLocation(L2Object obj, int x, int y, int z, int h)
	{
		_targetObjId = obj.getObjectId();
		_x = x;
		_y = y;
		_z = z;
		_h = h;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x28);
		writeD(_targetObjId);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(0x00);
		writeD(_h);
	}
	
	@Override
	public String getType()
	{
		return _S__38_TELEPORTTOLOCATION;
	}
}