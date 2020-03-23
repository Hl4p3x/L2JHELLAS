package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.network.serverpackets.AllyInfo;

public final class RequestAllyInfo extends L2GameClientPacket
{
	private static final String _C__8E_REQUESTALLYINFO = "[C] 8E RequestAllyInfo";
	
	@Override
	public void readImpl()
	{
		
	}
	
	@Override
	protected void runImpl()
	{
		AllyInfo ai = new AllyInfo(getClient().getActiveChar());
		sendPacket(ai);
	}
	
	@Override
	public String getType()
	{
		return _C__8E_REQUESTALLYINFO;
	}
}