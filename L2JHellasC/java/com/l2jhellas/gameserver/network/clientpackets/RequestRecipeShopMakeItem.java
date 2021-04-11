package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.controllers.RecipeController;
import com.l2jhellas.gameserver.enums.player.StoreType;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.shield.antiflood.FloodProtectors;
import com.l2jhellas.shield.antiflood.FloodProtectors.FloodAction;
import com.l2jhellas.util.Util;

public final class RequestRecipeShopMakeItem extends L2GameClientPacket
{
	private static final String _C__AF_REQUESTRECIPESHOPMAKEITEM = "[C] B6 RequestRecipeShopMakeItem";
	
	private int _id;
	private int _recipeId;
	@SuppressWarnings("unused")
	private int _unknow;
	
	@Override
	protected void readImpl()
	{
		_id = readD();
		_recipeId = readD();
		_unknow = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		if (!FloodProtectors.performAction(getClient(), FloodAction.MANUFACTURE))
			return;
		
		final L2PcInstance manufacturer = L2World.getInstance().getPlayer(_id);
		
		if (manufacturer == null)
			return;
		
		if (activeChar.getPrivateStoreType() != StoreType.NONE)
		{
			activeChar.sendMessage("Cannot make items while trading");
			return;
		}
		
		if (manufacturer.getPrivateStoreType() != StoreType.MANUFACTURE)
			return;
		
		if (activeChar.isInCraftMode() || manufacturer.isInCraftMode())
		{
			activeChar.sendMessage("Currently in Craft Mode");
			return;
		}
		if (manufacturer.isInDuel() || activeChar.isInDuel() || manufacturer.isInCombat() || activeChar.isInCombat())
		{
			activeChar.sendPacket(SystemMessageId.CANT_OPERATE_PRIVATE_STORE_DURING_COMBAT);
			return;
		}
		if (Util.checkIfInRange(150, activeChar, manufacturer, true))
			RecipeController.getInstance().requestManufactureItem(manufacturer, _recipeId, activeChar);
	}
	
	@Override
	public String getType()
	{
		return _C__AF_REQUESTRECIPESHOPMAKEITEM;
	}
}