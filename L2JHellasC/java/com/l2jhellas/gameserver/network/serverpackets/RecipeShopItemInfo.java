package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class RecipeShopItemInfo extends L2GameServerPacket
{
	private static final String _S__DA_RecipeShopItemInfo = "[S] da RecipeShopItemInfo";
	private final int _shopId;
	private final int _recipeId;
	
	public RecipeShopItemInfo(int shopId, int recipeId)
	{
		_shopId = shopId;
		_recipeId = recipeId;
	}
	
	@Override
	protected final void writeImpl()
	{
		final L2PcInstance manufacturer = L2World.getInstance().getPlayer(_shopId);
		
		if (manufacturer == null)
			return;
		
		writeC(0xda);
		writeD(_shopId);
		writeD(_recipeId);
		writeD((int) manufacturer.getCurrentMp());
		writeD(manufacturer.getMaxMp());
		writeD(0xffffffff);
	}
	
	@Override
	public String getType()
	{
		return _S__DA_RecipeShopItemInfo;
	}
}