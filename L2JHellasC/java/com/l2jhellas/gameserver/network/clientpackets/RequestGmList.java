package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.datatables.xml.AdminData;

public final class RequestGmList extends L2GameClientPacket
{
	private static final String _C__81_REQUESTGMLIST = "[C] 81 RequestGmList";
	
	@Override
	protected void readImpl()
	{
		
	}
	
	@Override
	protected void runImpl()
	{
		if (getClient().getActiveChar() == null)
			return;
		AdminData.getInstance().sendListToPlayer(getClient().getActiveChar());
	}
	
	@Override
	public String getType()
	{
		return _C__81_REQUESTGMLIST;
	}
}