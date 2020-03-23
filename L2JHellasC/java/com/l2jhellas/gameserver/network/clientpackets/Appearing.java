package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.UserInfo;

public final class Appearing extends L2GameClientPacket
{
	private static final String _C__30_APPEARING = "[C] 30 Appearing";
	
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
		
		if (activeChar.isOnline() == 0)
			return;
		
		if (activeChar.isTeleporting())
			activeChar.onTeleported();
		
		sendPacket(new UserInfo(activeChar));
	}
	
	@Override
	public String getType()
	{
		return _C__30_APPEARING;
	}
}