package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.TaskPriority;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.enums.ZoneId;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.GetOnVehicle;
import com.l2jhellas.gameserver.network.serverpackets.MoveToLocation;
import com.l2jhellas.gameserver.network.serverpackets.PartyMemberPosition;

public class ValidatePosition extends L2GameClientPacket
{
	private static final String _C__48_VALIDATEPOSITION = "[C] 48 ValidatePosition";

	public TaskPriority getPriority()
	{
		return TaskPriority.PR_HIGH;
	}

	private int _x;
	private int _y;
	private int _z;
	private int _heading;
	private int _boatObjId;

	@Override
	protected void readImpl()
	{
		_x = readD();
		_y = readD();
		_z = readD();
		_heading = readD();
		_boatObjId = readD();
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null || activeChar.isTeleporting() || activeChar.inObserverMode())
			return;

		boolean isFloating = activeChar.isFlying() || activeChar.isInsideZone(ZoneId.WATER);

		if ((activeChar.getX() == 0) && (activeChar.getY() == 0) && (activeChar.getZ() == 0))
		{
			activeChar.tsekarepos(isFloating);
			return;
		}

		if ((_x == 0) && (_y == 0) && activeChar.getX() != 0)
		{
			activeChar.tsekarepos(isFloating);
			return;
		}

		if (activeChar.isFalling(_z))
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		activeChar.setClientX(_x);
		activeChar.setClientY(_y);
		activeChar.setClientZ(_z);
		activeChar.setClientHeading(_heading);
		int realX = activeChar.getX();
		int realY = activeChar.getY();

		// int realZ = activeChar.getZ();
		double dx = _x - realX;
		double dy = _y - realY;
		double diffSq = Math.sqrt(dx * dx + dy * dy);

		if (!isFloating)
		{			
			if (_z < -15000 || _z > 15000)
			{
				activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				activeChar.setTarget(activeChar);
				activeChar.tsekarepos(false);
				return;
			}							
		}

		if (diffSq > 0 && diffSq < 1000) // if too large, messes observation
		{
			if (diffSq > activeChar.getStat().getMoveSpeed()) // more than can be considered to be result of latency
			{
				if (activeChar.isInBoat())
				{
					activeChar.setHeading(_heading);
					activeChar.sendPacket(new GetOnVehicle(activeChar.getObjectId(), _boatObjId, activeChar.getInBoatPosition()));
					return;
				}

				activeChar.broadcastPacket(new MoveToLocation(activeChar));
			}
		}
		activeChar.setLastClientPosition(_x, _y, _z);			
		activeChar.setLastServerPosition(activeChar.getX(), activeChar.getY(), activeChar.getZ());

		if (activeChar.getParty() != null)
			activeChar.getParty().broadcastToPartyMembers(activeChar, new PartyMemberPosition(activeChar.getParty()));
	}

	@Override
	public String getType()
	{
		return _C__48_VALIDATEPOSITION;
	}

	@Deprecated
	public boolean equal(ValidatePosition pos)
	{
		return _x == pos._x && _y == pos._y && _z == pos._z && _heading == pos._heading;
	}
}