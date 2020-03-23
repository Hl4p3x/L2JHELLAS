package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.enums.player.StoreType;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public final class RequestRecipeShopManageQuit extends L2GameClientPacket
{
	private static final String _C__B3_RequestRecipeShopManageQuit = "[C] b2 RequestRecipeShopManageQuit";
	
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		
		player.setPrivateStoreType(StoreType.NONE);
		player.broadcastUserInfo();
	}
	
	@Override
	public String getType()
	{
		return _C__B3_RequestRecipeShopManageQuit;
	}
}