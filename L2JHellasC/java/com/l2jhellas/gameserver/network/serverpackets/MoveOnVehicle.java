package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class MoveOnVehicle extends L2GameServerPacket
{
	private static final String _S__71_MOVEONVEICLE = "[S] 71 MoveOnVehicle";
	private final int _id;
	private final int _x, _y, _z;
	private final L2PcInstance _activeChar;
	
	public MoveOnVehicle(int vehicleID, L2PcInstance player, int x, int y, int z)
	{
		_id = vehicleID;
		_activeChar = player;
		_x = x;
		_y = y;
		_z = z;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x71);
		
		writeD(_activeChar.getObjectId());
		writeD(_id);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_activeChar.getX());
		writeD(_activeChar.getY());
		writeD(_activeChar.getZ());
	}
	
	@Override
	public String getType()
	{
		return _S__71_MOVEONVEICLE;
	}
}