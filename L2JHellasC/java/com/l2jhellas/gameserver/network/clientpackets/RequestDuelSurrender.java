package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.instancemanager.DuelManager;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public final class RequestDuelSurrender extends L2GameClientPacket
{
	private static final String _C__D0_30_REQUESTDUELSURRENDER = "[C] D0:30 RequestDuelSurrender";
	
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
		
		DuelManager.getInstance().doSurrender(activeChar);
	}
	
	@Override
	public String getType()
	{
		return _C__D0_30_REQUESTDUELSURRENDER;
	}
}