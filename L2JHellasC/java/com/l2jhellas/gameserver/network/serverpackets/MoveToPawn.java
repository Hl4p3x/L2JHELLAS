package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.actor.L2Character;

public class MoveToPawn extends L2GameServerPacket
{
	private static final String _S__75_MOVETOPAWN = "[S] 60 MoveToPawn";
	private final int _charObjId;
	private final int _targetId;
	private final int _distance;
	private final int _x, _y, _z;
	
	public MoveToPawn(L2Character cha, L2Character target, int distance)
	{
		_charObjId = cha.getObjectId();
		_targetId = target.getObjectId();
		_distance = distance;
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
	}
	
	public MoveToPawn(L2Character cha, L2Object target, int distance)
	{
		_charObjId = cha.getObjectId();
		_targetId = target.getObjectId();
		_distance = distance;
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x60);
		
		writeD(_charObjId);
		writeD(_targetId);
		writeD(_distance);
		
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
	
	@Override
	public String getType()
	{
		return _S__75_MOVETOPAWN;
	}
}