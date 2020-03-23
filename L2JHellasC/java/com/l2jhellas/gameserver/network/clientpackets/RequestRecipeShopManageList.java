package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
public final class RequestRecipeShopManageList extends L2GameClientPacket
{
	private static final String _C__B0_RequestRecipeShopManageList = "[C] b0 RequestRecipeShopManageList";
	
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
		
		player.openWorkshop(true);
		
	}
	
	@Override
	public String getType()
	{
		return _C__B0_RequestRecipeShopManageList;
	}
}