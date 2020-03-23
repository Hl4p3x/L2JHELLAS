package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.geometry.Point3D;
import com.l2jhellas.gameserver.instancemanager.BoatManager;
import com.l2jhellas.gameserver.model.actor.L2Vehicle;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.GetOnVehicle;

public final class RequestGetOnVehicle extends L2GameClientPacket
{
	private static final String _C__5C_GETONVEHICLE = "[C] 5C GetOnVehicle";
	
	private int _boatId;
	private Point3D _pos;
	
	@Override
	protected void readImpl()
	{
		int x, y, z;
		_boatId = readD();
		x = readD();
		y = readD();
		z = readD();
		_pos = new Point3D(x, y, z);
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		L2Vehicle boat;
		if (activeChar.isInBoat())
		{
			boat = activeChar.getBoat();
			if (boat.getObjectId() != _boatId)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		else
		{
			boat = BoatManager.getInstance().getBoat(_boatId);
			if (boat == null || boat.isMoving() || !activeChar.isInsideRadius(boat, 1000, true, false))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		activeChar.setInVehiclePosition(_pos);
		activeChar.setVehicle(boat);
		activeChar.broadcastPacket(new GetOnVehicle(activeChar.getObjectId(), boat.getObjectId(), _pos));
		
		activeChar.setXYZ(boat.getX(), boat.getY(), boat.getZ());
		activeChar.revalidateZone(true);
	}
	
	@Override
	public String getType()
	{
		return _C__5C_GETONVEHICLE;
	}
}