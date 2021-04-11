package com.l2jhellas.gameserver.network.clientpackets;


import com.l2jhellas.gameserver.controllers.RecipeController;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.shield.antiflood.FloodProtectors;
import com.l2jhellas.shield.antiflood.FloodProtectors.FloodAction;

public final class RequestRecipeItemMakeSelf extends L2GameClientPacket
{
	private static final String _C__AF_REQUESTRECIPEITEMMAKESELF = "[C] AF RequestRecipeItemMakeSelf";
	
	private int _id;
	
	@Override
	protected void readImpl()
	{
		_id = readD();
	}
	
	@Override
	protected void runImpl()
	{
		if (!FloodProtectors.performAction(getClient(), FloodAction.MANUFACTURE))
			return;
		
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if (activeChar.isInStoreMode() || activeChar.isInCraftMode())
			return;

		RecipeController.getInstance().requestMakeItem(activeChar, _id);
	}
	
	@Override
	public String getType()
	{
		return _C__AF_REQUESTRECIPEITEMMAKESELF;
	}
}