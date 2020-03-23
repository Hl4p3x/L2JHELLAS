package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.L2Character;

public class ValidateLocation extends L2GameServerPacket
{
	private static final String _S__76_SETTOLOCATION = "[S] 61 ValidateLocation";
	private final int _charObjId;
	private final int _x, _y, _z, _heading;
	
	public ValidateLocation(L2Character cha)
	{
		_charObjId = cha.getObjectId();
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_heading = cha.getHeading();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x61);
		writeD(_charObjId);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_heading);
	}
	
	@Override
	public String getType()
	{
		return _S__76_SETTOLOCATION;
	}
}