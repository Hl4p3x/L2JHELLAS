package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public final class RequestOlympiadObserverEnd extends L2GameClientPacket
{
	private static final String _C__D0_12_REQUESTOLYMPIADOBSERVEREND = "[C] D0:12 RequestOlympiadObserverEnd";
	
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		if (activeChar.inObserverMode())
			activeChar.leaveOlympiadObserverMode();
	}
	
	@Override
	public String getType()
	{
		return _C__D0_12_REQUESTOLYMPIADOBSERVEREND;
	}
}