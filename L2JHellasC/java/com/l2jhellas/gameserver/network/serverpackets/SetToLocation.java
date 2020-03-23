package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.L2Character;

public class SetToLocation extends L2GameServerPacket
{
	private static final String _S__76_SETTOLOCATION = "[S] 76 SetToLocation";
	private final int _charObjId;
	private final int _x, _y, _z, _heading;
	
	public SetToLocation(L2Character character)
	{
		_charObjId = character.getObjectId();
		_x = character.getX();
		_y = character.getY();
		_z = character.getZ();
		_heading = character.getHeading();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x76);
		
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