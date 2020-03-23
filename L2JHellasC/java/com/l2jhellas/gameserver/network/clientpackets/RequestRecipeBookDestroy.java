package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.datatables.xml.RecipeData;
import com.l2jhellas.gameserver.enums.player.StoreType;
import com.l2jhellas.gameserver.model.L2RecipeList;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.RecipeBookItemList;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public final class RequestRecipeBookDestroy extends L2GameClientPacket
{
	private static final String _C__AC_REQUESTRECIPEBOOKDESTROY = "[C] AD RequestRecipeBookDestroy";
	
	private int _recipeID;
	
	@Override
	protected void readImpl()
	{
		_recipeID = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar != null)
		{
			if (activeChar.getPrivateStoreType() != StoreType.NONE)
			{
				activeChar.sendPacket(SystemMessageId.CANT_ALTER_RECIPEBOOK_WHILE_CRAFTING);
				return;
			}
			
			final L2RecipeList rp = RecipeData.getInstance().getRecipeList(_recipeID - 1);
			if (rp == null)
				return;
			
			activeChar.unregisterRecipeList(_recipeID);	
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_DELETED).addItemName(rp.getRecipeId()));
			activeChar.sendPacket(new RecipeBookItemList(activeChar, rp.isDwarvenRecipe()));
		}
	}
	
	@Override
	public String getType()
	{
		return _C__AC_REQUESTRECIPEBOOKDESTROY;
	}
}