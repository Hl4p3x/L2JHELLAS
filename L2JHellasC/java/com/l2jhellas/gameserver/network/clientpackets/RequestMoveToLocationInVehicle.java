package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.enums.items.L2WeaponType;
import com.l2jhellas.gameserver.geometry.Point3D;
import com.l2jhellas.gameserver.instancemanager.BoatManager;
import com.l2jhellas.gameserver.model.actor.L2Vehicle;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.StopMoveInVehicle;

public final class RequestMoveToLocationInVehicle extends L2GameClientPacket
{
	private final Point3D _pos = new Point3D(0, 0, 0);
	private final Point3D _origin_pos = new Point3D(0, 0, 0);
	
	private int _boatId;
	private int _targetX, _targetY, _targetZ;
	private int _originX, _originY, _originZ;
	
	@Override
	protected void readImpl()
	{
		_boatId = readD(); // objectId of boat
		_targetX = readD();
		_targetY = readD();
		_targetZ = readD();
		_pos.setXYZ(_targetX, _targetY, _targetZ);
		_originX = readD();
		_originY = readD();
		_originZ = readD();
		_origin_pos.setXYZ(_originX, _originY, _originZ);
		
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		if (_targetX == _originX && _targetY == _originY && _targetZ == _originZ)
		{
			activeChar.sendPacket(new StopMoveInVehicle(activeChar, _boatId));
			return;
		}
		
		if (activeChar.isAttacking() && activeChar.getActiveWeaponItem() != null && activeChar.getActiveWeaponItem().getItemType().equals(L2WeaponType.BOW))
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (activeChar.isSitting() || activeChar.isMovementDisabled())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (activeChar.getPet() != null)
		{
			activeChar.sendPacket(SystemMessageId.RELEASE_PET_ON_BOAT);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final L2Vehicle boat;
		if (activeChar.isInBoat())
		{
			boat = activeChar.getBoat();
			if (boat.getObjectId() != _boatId)
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		else
		{
			boat = BoatManager.getInstance().getBoat(_boatId);
			if (boat == null || !boat.isInsideRadius(activeChar, 300, true, false))
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			activeChar.setVehicle(boat);
		}
		
		activeChar.setInVehiclePosition(_pos);
		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO_IN_A_BOAT, new Location(_pos.getX(), _pos.getY(), _pos.getZ(), 0), new Location(_origin_pos.getX(), _origin_pos.getY(), _origin_pos.getZ(), 0));
	}
	
	@Override
	public String getType()
	{
		return null;
	}
}