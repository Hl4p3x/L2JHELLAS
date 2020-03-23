package com.l2jhellas.gameserver.network.clientpackets;

import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;

public final class Action extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(Action.class.getName());
	private static final String ACTION__C__04 = "[C] 04 Action";
	
	// cddddc
	private int _objectId;
	@SuppressWarnings("unused")
	private int _originX, _originY, _originZ;
	private int _actionId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD(); // Target object Identifier
		_originX = readD();
		_originY = readD();
		_originZ = readD();
		_actionId = readC(); // Action identifier : 0-Simple click, 1-Shift click
	}
	
	@Override
	protected void runImpl()
	{
		if (Config.DEBUG)
			_log.fine("Action: " + _actionId + ", objectId: " + _objectId);
		
		// Get the current L2PcInstance of the player
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		if (activeChar.inObserverMode())
		{
			activeChar.sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// If the player is the requester of a transaction
		if (activeChar.getActiveRequester() != null || activeChar.isOutOfControl())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// If requested object doesn't exist
		final L2Object obj = (activeChar.getTargetId() == _objectId) ? activeChar.getTarget() : L2World.getInstance().findObject(_objectId);
		if (obj == null)
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (!obj.isVisible())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		activeChar.onActionRequest();
		
		if (_actionId == 0)
			obj.onAction(activeChar);
		else if (_actionId == 1)
		{
			if (!activeChar.isGM() && !(obj instanceof L2Npc && Config.ALT_GAME_VIEWNPC))
				obj.onAction(activeChar);
			else
				obj.onActionShift(activeChar);
		}
	}
	
	@Override
	public String getType()
	{
		return ACTION__C__04;
	}
}