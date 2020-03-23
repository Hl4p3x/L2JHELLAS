package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.group.party.PartyMatchWaitingList;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public final class RequestExitPartyMatchingWaitingRoom extends L2GameClientPacket
{
	private static final String _C__D0_17_REQUESTEXITPARTYMATCHINGWAITINGROOM = "[C] D0:17 RequestExitPartyMatchingWaitingRoom";
	
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		PartyMatchWaitingList.getInstance().removePlayer(activeChar);
	}
	
	@Override
	public String getType()
	{
		return _C__D0_17_REQUESTEXITPARTYMATCHINGWAITINGROOM;
	}
}