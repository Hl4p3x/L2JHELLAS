package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.geometry.Point3D;

public class GetOnVehicle extends L2GameServerPacket
{
	private final int _charObjId;
	private final int _boatObjId;
	private final Point3D _pos;
	
	public GetOnVehicle(int charObjId, int boatObjId, Point3D pos)
	{
		_charObjId = charObjId;
		_boatObjId = boatObjId;
		_pos = pos;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x5C);
		writeD(_charObjId);
		writeD(_boatObjId);
		writeD(_pos.getX());
		writeD(_pos.getY());
		writeD(_pos.getZ());
	}
	
	@Override
	public String getType()
	{
		return "[S] 5C GetOnVehicle";
	}
}