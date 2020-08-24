package com.l2jhellas.gameserver.handlers.itemhandlers;

import com.l2jhellas.gameserver.datatables.sql.ItemTable;
import com.l2jhellas.gameserver.datatables.xml.RecipeData;
import com.l2jhellas.gameserver.handler.IItemHandler;
import com.l2jhellas.gameserver.model.L2RecipeList;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ItemList;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public class Recipes implements IItemHandler
{
	private final int[] ITEM_IDS;
	
	public Recipes()
	{
		RecipeData rc = RecipeData.getInstance();
		ITEM_IDS = new int[rc.getRecipesCount()];
		for (int i = 0; i < rc.getRecipesCount(); i++)
		{
			ITEM_IDS[i] = rc.getRecipeList(i).getRecipeId();
		}
	}
	
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		
		final L2PcInstance activeChar = (L2PcInstance) playable;
		
		final L2RecipeList rp = RecipeData.getInstance().getRecipeByItemId(item.getItemId());
		
		if(rp == null)
			return;
		
		if (activeChar.hasRecipeList(rp.getId(),rp.isDwarvenRecipe()))
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.RECIPE_ALREADY_REGISTERED));
		else
		{
			if (rp.isDwarvenRecipe())
			{
				if (activeChar.hasDwarvenCraft())
				{
					// can't add recipe, because create item level too low
					if (rp.getLevel() > activeChar.getDwarvenCraft())
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CREATE_LVL_TOO_LOW_TO_REGISTER));
					else if (activeChar.getDwarvenRecipeBook().size() >= activeChar.getDwarfRecipeLimit())
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.UP_TO_S1_RECIPES_CAN_REGISTER).addNumber(activeChar.getDwarfRecipeLimit()));
					else
					{
						activeChar.registerDwarvenRecipeList(rp);
						activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
						String itemName = ItemTable.getInstance().getTemplate(rp.getItemId()).getItemName();
						activeChar.sendMessage("Added recipe " + itemName + " to Dwarven RecipeBook.");
						activeChar.sendPacket(new ItemList(activeChar,false));
					}
				}
				else
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_REGISTER_NO_ABILITY_TO_CRAFT));
			}
			else
			{
				if (activeChar.hasCommonCraft())
				{
					// can't add recipe, because create item level too low
					if (rp.getLevel() > activeChar.getCommonCraft())
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CREATE_LVL_TOO_LOW_TO_REGISTER));
					else if (activeChar.getCommonRecipeBook().size() >= activeChar.getCommonRecipeLimit())
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.UP_TO_S1_RECIPES_CAN_REGISTER).addNumber(activeChar.getCommonRecipeLimit()));
					else
					{
						activeChar.registerCommonRecipeList(rp);
						activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
						String itemName = ItemTable.getInstance().getTemplate(rp.getItemId()).getItemName();
						activeChar.sendMessage("Added recipe " + itemName + " to Common RecipeBook.");
						activeChar.sendPacket(new ItemList(activeChar,false));
					}
				}
				else
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_REGISTER_NO_ABILITY_TO_CRAFT));
			}
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}