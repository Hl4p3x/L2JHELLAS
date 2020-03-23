package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.actor.L2Character;

public final class FlyToLocation extends L2GameServerPacket
{
	private final int _destX, _destY, _destZ;
	private final int _chaObjId, _chaX, _chaY, _chaZ;
	private final FlyType _type;
	
	public enum FlyType
	{
		THROW_UP,
		THROW_HORIZONTAL,
		DUMMY; // no effect
	}
	
	public FlyToLocation(L2Character cha, int destX, int destY, int destZ, FlyType type)
	{
		_chaObjId = cha.getObjectId();
		_chaX = cha.getX();
		_chaY = cha.getY();
		_chaZ = cha.getZ();
		_destX = destX;
		_destY = destY;
		_destZ = destZ;
		_type = type;
	}
	
	public FlyToLocation(L2Character cha, L2Object dest, FlyType type)
	{
		this(cha, dest.getX(), dest.getY(), dest.getZ(), type);
	}
	
	@Override
	public String getType()
	{
		return "[S] 0xd4 FlyToLocation";
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xC5);
		writeD(_chaObjId);
		writeD(_destX);
		writeD(_destY);
		writeD(_destZ);
		writeD(_chaX);
		writeD(_chaY);
		writeD(_chaZ);
		writeD(_type.ordinal());
	}
}