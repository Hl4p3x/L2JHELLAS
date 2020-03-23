package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.L2Character;

public class VehicleStarted extends L2GameServerPacket
{
	private final int _objectId;
	private final int _state;
	
	public VehicleStarted(L2Character boat, int state)
	{
		_objectId = boat.getObjectId();
		_state = state;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xBA);
		writeD(_objectId);
		writeD(_state);
	}
}