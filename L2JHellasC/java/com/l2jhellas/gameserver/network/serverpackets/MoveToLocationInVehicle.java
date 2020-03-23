package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.position.Location;

public class MoveToLocationInVehicle extends L2GameServerPacket
{
	private int _charObjId;
	private int _boatId;
	private Location _destination;
	private Location _origin;
	
	public MoveToLocationInVehicle(L2Character actor, Location destination, Location origin)
	{
		if (!(actor instanceof L2PcInstance))
			return;
		
		L2PcInstance player = (L2PcInstance) actor;
		
		if (player.getBoat() == null)
			return;
		
		_charObjId = player.getObjectId();
		_boatId = player.getBoat().getObjectId();
		_destination = destination;
		_origin = origin;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x71);
		writeD(_charObjId);
		writeD(_boatId);
		writeD(_destination.getX());
		writeD(_destination.getY());
		writeD(_destination.getZ());
		writeD(_origin.getX());
		writeD(_origin.getY());
		writeD(_origin.getZ());
	}
	
	@Override
	public String getType()
	{
		return "[S] 71 MoveToLocationInVehicle";
	}
}