package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.geometry.Point3D;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.StopMoveInVehicle;

public final class CannotMoveAnymoreInVehicle extends L2GameClientPacket
{
	private int _x;
	private int _y;
	private int _z;
	private int _heading;
	private int _boatId;
	
	@Override
	protected void readImpl()
	{
		_boatId = readD();
		_x = readD();
		_y = readD();
		_z = readD();
		_heading = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		
		if (player == null)
			return;
		
		if (player.isInBoat() && player.getBoat().getObjectId() == _boatId)
		{
			player.setInBoatPosition(new Point3D(_x, _y, _z));
			player.setHeading(_heading);
			player.broadcastPacket(new StopMoveInVehicle(player, _boatId));
		}
		
	}
	
	@Override
	public String getType()
	{
		return "[C] 5D CannotMoveAnymoreInVehicle";
	}
}