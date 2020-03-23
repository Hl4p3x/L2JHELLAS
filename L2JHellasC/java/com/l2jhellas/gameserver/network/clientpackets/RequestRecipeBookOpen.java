package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.controllers.RecipeController;
import com.l2jhellas.gameserver.enums.player.StoreType;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;

public final class RequestRecipeBookOpen extends L2GameClientPacket
{
	private static final String _C__AC_REQUESTRECIPEBOOKOPEN = "[C] AC RequestRecipeBookOpen";
	
	private boolean _isDwarvenCraft;
	
	@Override
	protected void readImpl()
	{
		_isDwarvenCraft = (readD() == 0);
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
			return;
		
		if (activeChar.getActiveTradeList()!= null || activeChar.getPrivateStoreType() != StoreType.NONE)
		{
			activeChar.sendMessage("Cannot use recipe book while trading");
			return;
		}
		
		if (activeChar.isCastingNow() || activeChar.isAllSkillsDisabled())
		{
			activeChar.sendPacket(SystemMessageId.NO_RECIPE_BOOK_WHILE_CASTING);
			return;
		}
		
		RecipeController.getInstance().requestBookOpen(activeChar, _isDwarvenCraft);
	}
	
	@Override
	public String getType()
	{
		return _C__AC_REQUESTRECIPEBOOKOPEN;
	}
}