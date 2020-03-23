package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2DoorInstance;

public class DoorStatusUpdate extends L2GameServerPacket
{
	private static final String _S__61_DOORSTATUSUPDATE = "[S] 4d DoorStatusUpdate";
	
	private final L2DoorInstance _door;
	private final boolean _hp;
	
	public DoorStatusUpdate(L2DoorInstance door)
	{
		_door = door;
		_hp = door.getCastle() != null && door.getCastle().getSiege().getIsInProgress();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x4d);
		writeD(_door.getObjectId());
		writeD(_door.getOpen() ? 0 : 1);
		writeD(_door.getDamage());
		writeD((_hp) ? 1 : 0);
		writeD(_door.getDoorId());
		writeD(_door.getMaxHp());
		writeD((int) _door.getCurrentHp());
	}
	
	@Override
	public String getType()
	{
		return _S__61_DOORSTATUSUPDATE;
	}
}