package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class GameGuardReply extends L2GameClientPacket
{
	private static final String _C__CA_GAMEGUARDREPLY = "[C] CA GameGuardReply";
	
	@Override
	protected void readImpl()
	{
		
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		getClient().setGameGuardOk(true);
	}
	
	@Override
	public String getType()
	{
		return _C__CA_GAMEGUARDREPLY;
	}
}