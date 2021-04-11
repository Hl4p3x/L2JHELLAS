package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.enums.player.ChatType;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.CreatureSay;
import com.l2jhellas.shield.antiflood.FloodProtectors;
import com.l2jhellas.shield.antiflood.FloodProtectors.FloodAction;

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
			
		if (activeChar.getTarget() != null && activeChar.getTarget().getObjectId() == _objectId && !FloodProtectors.performAction(activeChar.getClient(),FloodAction.ACTION_REQUEST))
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (!activeChar.getAppearance().isVisible())
		{
			activeChar.sendPacket(new CreatureSay(0, ChatType.GENERAL, "SYS", "You cannot do this action in hide mode."));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (activeChar.inObserverMode())
		{
			activeChar.sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		final L2Object target = (activeChar.getTargetId() == _objectId) ? activeChar.getTarget() : L2World.getInstance().findObject(_objectId);

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
		
		if (activeChar.isAttacking() && activeChar.isMoving())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
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
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__0A_ATTACKREQUEST;
	}
}