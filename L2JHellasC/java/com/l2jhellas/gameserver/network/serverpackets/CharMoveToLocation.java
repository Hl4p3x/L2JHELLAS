package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.L2Character;

public class CharMoveToLocation extends L2GameServerPacket
{
	private static final String _S__01_CHARMOVETOLOCATION = "[S] 01 CharMoveToLocation";
	private final int _charObjId, _x, _y, _z, _xDst, _yDst, _zDst;
	
	public CharMoveToLocation(L2Character cha)
	{
		_charObjId = cha.getObjectId();
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_xDst = cha.getXdestination();
		_yDst = cha.getYdestination();
		_zDst = cha.getZdestination();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x01);
		
		writeD(_charObjId);
		
		writeD(_xDst);
		writeD(_yDst);
		writeD(_zDst);
		
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
	
	@Override
	public String getType()
	{
		return _S__01_CHARMOVETOLOCATION;
	}
}