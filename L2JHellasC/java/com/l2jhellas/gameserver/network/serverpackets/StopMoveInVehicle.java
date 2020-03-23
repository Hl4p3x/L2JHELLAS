package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.geometry.Point3D;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class StopMoveInVehicle extends L2GameServerPacket
{
	private final int _charObjId;
	private final int _boatId;
	private final Point3D _pos;
	private final int _heading;
	
	public StopMoveInVehicle(L2PcInstance player, int boatId)
	{
		_charObjId = player.getObjectId();
		_boatId = boatId;
		_pos = player.getInVehiclePosition();
		_heading = player.getHeading();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x72);
		writeD(_charObjId);
		writeD(_boatId);
		writeD(_pos.getX());
		writeD(_pos.getY());
		writeD(_pos.getZ());
		writeD(_heading);
	}
	
	@Override
	public String getType()
	{
		return "[S] 72 StopMoveInVehicle";
	}
}