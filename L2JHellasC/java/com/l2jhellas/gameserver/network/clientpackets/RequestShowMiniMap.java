package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.ShowMiniMap;

public final class RequestShowMiniMap extends L2GameClientPacket
{
	private static final String _C__cd_REQUESTSHOWMINIMAP = "[C] cd RequestShowMiniMap";
	
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	@Override
	protected final void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		activeChar.sendPacket(new ShowMiniMap(1665));
	}
	
	@Override
	public String getType()
	{
		return _C__cd_REQUESTSHOWMINIMAP;
	}
}