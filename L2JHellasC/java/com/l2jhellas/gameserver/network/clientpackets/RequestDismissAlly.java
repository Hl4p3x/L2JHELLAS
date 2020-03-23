package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;

public final class RequestDismissAlly extends L2GameClientPacket
{
	private static final String _C__86_REQUESTDISMISSALLY = "[C] 86 RequestDismissAlly";
	
	@Override
	protected void readImpl()
	{
		// trigger packet
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		if (!activeChar.isClanLeader())
		{
			activeChar.sendPacket(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER);
			return;
		}
		activeChar.getClan().dissolveAlly(activeChar);
	}
	
	@Override
	public String getType()
	{
		return _C__86_REQUESTDISMISSALLY;
	}
}