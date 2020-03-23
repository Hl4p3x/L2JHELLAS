package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.RecipeShopSellList;

public final class RequestRecipeShopManagePrev extends L2GameClientPacket
{
	private static final String _C__B7_RequestRecipeShopPrev = "[C] b7 RequestRecipeShopPrev";
	
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
			
		// Player shouldn't be able to set stores if he/she is alike dead (dead or fake death)
		if (player.isAlikeDead())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final L2PcInstance target = player.getTarget().getActingPlayer();		
		if(target == null)
			return;
		
		player.sendPacket(new RecipeShopSellList(player, target));
	}
	
	@Override
	public String getType()
	{
		return _C__B7_RequestRecipeShopPrev;
	}
}