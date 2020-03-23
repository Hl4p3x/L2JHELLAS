package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.enums.player.StoreType;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public final class RequestPrivateStoreQuitSell extends L2GameClientPacket
{
	private static final String _C__76_REQUESTPRIVATESTOREQUITSELL = "[C] 76 RequestPrivateStoreQuitSell";
	
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		
		player.setPrivateStoreType(StoreType.NONE);
		player.standUp();
		player.broadcastUserInfo();
	}
	
	@Override
	public String getType()
	{
		return _C__76_REQUESTPRIVATESTOREQUITSELL;
	}
}