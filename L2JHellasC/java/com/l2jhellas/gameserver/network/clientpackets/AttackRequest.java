package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;

public final class AttackRequest extends L2GameClientPacket
{
	// cddddc
	private int _objectId;
	@SuppressWarnings("unused")
	private int _originX, _originY, _originZ;
	@SuppressWarnings("unused")
	private int _attackId;
	
	private static final String _C__0A_ATTACKREQUEST = "[C] 0A AttackRequest";
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_originX = readD();
		_originY = readD();
		_originZ = readD();
		_attackId = readC(); // 0 for simple click 1 for shift-click
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		activeChar.onActionRequest();
		
		if (activeChar.inObserverMode())
		{
			activeChar.sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		L2Object target;
		
		if (activeChar.getTargetId() == _objectId)
			target = activeChar.getTarget();
		else
			target = L2World.getInstance().findObject(_objectId);
		
		if (target == null)
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (!target.isVisible())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Like L2OFF
		if (activeChar.isAttackingNow() && activeChar.isMoving())
		{
			// If target is not attackable, send a Server->Client packet ActionFailed
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Checking if target has moved to peace zone
		if (activeChar.isInsidePeaceZone(activeChar, target))
		{
			activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (activeChar.getTarget() != target)
			target.onAction(activeChar);
		else
		{
			if ((target.getObjectId() != activeChar.getObjectId()) && !activeChar.isInStoreMode() && activeChar.getActiveRequester() == null)
				target.onForcedAttack(activeChar);
			else
				sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__0A_ATTACKREQUEST;
	}
}