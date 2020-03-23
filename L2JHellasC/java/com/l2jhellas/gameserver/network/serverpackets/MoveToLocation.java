package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.position.Location;

public class MoveToLocation extends L2GameServerPacket
{
	private static final String _S__2F_CHARMOVETOLOCATION = "[S] 2f CharMoveToLocation [ddddddd]";
	
	private final int charId;
	private final int _x, _y, _z, _xDst, _yDst, _zDst;
	
	public MoveToLocation(L2Character cha)
	{
		charId = cha.getObjectId();
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_xDst = cha.getXdestination();
		_yDst = cha.getYdestination();
		_zDst = cha.getZdestination();
	}
	
	public MoveToLocation(L2Character actor, Location destiny)
	{
		charId = actor.getObjectId();
		_x = actor.getX();
		_y = actor.getY();
		_z = actor.getZ();
		_xDst = destiny.getX();
		_yDst = destiny.getY();
		_zDst = destiny.getZ();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x01);
		
		writeD(charId);
		
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
		return _S__2F_CHARMOVETOLOCATION;
	}
}