package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.enums.player.StoreType;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.RecipeShopItemInfo;

public final class RequestRecipeShopMakeInfo extends L2GameClientPacket
{
	private static final String _C__B5_RequestRecipeShopMakeInfo = "[C] b5 RequestRecipeShopMakeInfo";
	
	private int _playerObjectId;
	private int _recipeId;
	
	@Override
	protected void readImpl()
	{
		_playerObjectId = readD();
		_recipeId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		
		final L2PcInstance shop = L2World.getInstance().getPlayer(_playerObjectId);

		if (shop == null || _playerObjectId != shop.getObjectId() || shop.getPrivateStoreType() != StoreType.MANUFACTURE)
			return;
		
		player.sendPacket(new RecipeShopItemInfo(shop.getObjectId(), _recipeId));
	}
	
	@Override
	public String getType()
	{
		return _C__B5_RequestRecipeShopMakeInfo;
	}
}