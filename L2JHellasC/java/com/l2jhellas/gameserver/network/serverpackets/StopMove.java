package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.L2Character;

public class StopMove extends L2GameServerPacket
{
	private static final String _S__59_STOPMOVE = "[S] 47 StopMove";
	private final int _objectId;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _heading;
	
	public StopMove(L2Character cha)
	{
		this(cha.getObjectId(), cha.getX(), cha.getY(), cha.getZ(), cha.getHeading());
	}
	
	public StopMove(int objectId, int x, int y, int z, int heading)
	{
		_objectId = objectId;
		_x = x;
		_y = y;
		_z = z;
		_heading = heading;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x47);
		writeD(_objectId);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_heading);
	}
	
	@Override
	public String getType()
	{
		return _S__59_STOPMOVE;
	}
}