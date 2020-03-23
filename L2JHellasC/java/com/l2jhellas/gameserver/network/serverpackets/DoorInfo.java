package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2DoorInstance;

public class DoorInfo extends L2GameServerPacket
{
	private static final String _S__60_DOORINFO = "[S] 4c DoorInfo";
	
	private final L2DoorInstance _door;
	private final boolean _Hp;
	
	public DoorInfo(L2DoorInstance door)
	{
		_door = door;
		_Hp = door.getCastle() != null && door.getCastle().getSiege().getIsInProgress();
	}
	
	@Override
	protected final void writeImpl()
	{		
        writeC(0x4c);
        writeD(_door.getObjectId());
        writeD(_door.getDoorId());
        writeD((_Hp) ? 1 : 0);
        writeD(1);
        writeD(!_door.getOpen() ? 1 : 0);
        writeD((int) _door.getCurrentHp());
        writeD(_door.getMaxHp());
        writeD((_Hp) ? 1 : 0);
        writeD(_door.getDamage());
	}
	
	@Override
	public String getType()
	{
		return _S__60_DOORINFO;
	}
}