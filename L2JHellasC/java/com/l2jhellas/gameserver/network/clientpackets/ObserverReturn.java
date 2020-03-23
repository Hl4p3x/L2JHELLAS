package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public final class ObserverReturn extends L2GameClientPacket
{
	private static final String OBSRETURN__C__04 = "[C] b8 ObserverReturn";
	
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		if (activeChar.inObserverMode())
			activeChar.leaveObserverMode();
		
	}
	
	@Override
	public String getType()
	{
		return OBSRETURN__C__04;
	}
}