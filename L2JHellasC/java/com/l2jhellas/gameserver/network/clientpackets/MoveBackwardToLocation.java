package com.l2jhellas.gameserver.network.clientpackets;

import java.nio.BufferUnderflowException;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.TaskPriority;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.enums.items.L2WeaponType;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.MoveToLocation;
import com.l2jhellas.gameserver.network.serverpackets.StopMove;
import com.l2jhellas.util.IllegalPlayerAction;
import com.l2jhellas.util.MathUtil;
import com.l2jhellas.util.Util;

public class MoveBackwardToLocation extends L2GameClientPacket
{
	// cdddddd
	private int _targetX;
	private int _targetY;
	private int _targetZ;
	private int _originX;
	private int _originY;
	private int _originZ;
	private int _moveMovement;
	
	// For geodata
	private int _curX;
	private int _curY;
	@SuppressWarnings("unused")
	private int _curZ;
	
	public TaskPriority getPriority()
	{
		return TaskPriority.PR_HIGH;
	}
	
	private static final String _C__01_MOVEBACKWARDTOLOC = "[C] 01 MoveBackwardToLoc";
	
	@Override
	protected void readImpl()
	{
		_targetX = readD();
		_targetY = readD();
		_targetZ = readD();
		_originX = readD();
		_originY = readD();
		_originZ = readD();

		try
		{
			_moveMovement = readD(); 
		}
		catch (BufferUnderflowException e)
		{
			final L2PcInstance activeChar = getClient().getActiveChar();
			if (activeChar != null)
			{
				activeChar.sendPacket(SystemMessageId.HACKING_TOOL);
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				Util.handleIllegalPlayerAction(activeChar,"Player " + activeChar.getName() + " trying to use L2Walker!", IllegalPlayerAction.PUNISH_KICK);
				activeChar.closeNetConnection(false);
			}
		}
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null || activeChar.isDead() || activeChar.isFakeDeath())
			return;
				
		if (activeChar.isSitting())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (activeChar.isOutOfControl())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (activeChar.isTeleporting())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (_targetX == _originX && _targetY == _originY && _targetZ == _originZ)
		{
			activeChar.sendPacket(new StopMove(activeChar));
			return;
		}

		_curX = activeChar.getX();
		_curY = activeChar.getY();
		_curZ = activeChar.getZ();
		
		if (activeChar.getTeleMode() > 0)
		{
			if (activeChar.getTeleMode() == 1)
				activeChar.setTeleMode(0);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			activeChar.teleToLocation(_targetX, _targetY, _targetZ, false);
			return;
		}
		
		if (activeChar.getActiveEnchantItem() != null)
			activeChar.cancellEnchant();

		if (_moveMovement == 0 && !Config.GEODATA) // cursor movement without geodata is disabled
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (activeChar.isAttackingNow() && activeChar.getActiveWeaponItem() != null && (activeChar.getActiveWeaponItem().getItemType() == L2WeaponType.BOW))
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		double dx = _targetX - _curX;
		double dy = _targetY - _curY;
		// Can't move if character is confused, or trying to move a huge
		// distance
		if (((dx * dx + dy * dy) > 98010000)) // 9900*9900
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		// This is to avoid exploit with Hit + Fast movement
		if ((activeChar.isMoving() && activeChar.isAttackingNow()))
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (activeChar.isInBoat())
		{
			activeChar.setHeading(MathUtil.calculateHeadingFrom(_originX, _originY, _targetX, _targetY));
			activeChar.broadcastPacket(new MoveToLocation(activeChar, new Location(_targetX, _targetY, _targetZ)));
		}
		else
			activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,new Location(_targetX, _targetY, _targetZ));

		if (activeChar.getPet() != null)
		{
			double distance = Math.hypot(_targetX - activeChar.getX(), _targetY - activeChar.getY());
			activeChar.SummonRotate(activeChar.getPet(), distance);
		}	
		
		if(activeChar.isSpawnProtected())
		   activeChar.onActionRequest();
	}

	@Override
	public String getType()
	{
		return _C__01_MOVEBACKWARDTOLOC;
	}
}