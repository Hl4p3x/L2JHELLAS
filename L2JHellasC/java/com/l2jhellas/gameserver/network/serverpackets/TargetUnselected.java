package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.L2Character;

public class TargetUnselected extends L2GameServerPacket
{
	private static final String _S__3A_TARGETUNSELECTED = "[S] 2A TargetUnselected";
	private final int _targetObjId;
	private final int _x;
	private final int _y;
	private final int _z;
	
	public TargetUnselected(L2Character character)
	{
		_targetObjId = character.getObjectId();
		_x = character.getX();
		_y = character.getY();
		_z = character.getZ();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x2a);
		writeD(_targetObjId);
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
	
	@Override
	public String getType()
	{
		return _S__3A_TARGETUNSELECTED;
	}
}