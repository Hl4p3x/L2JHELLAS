package com.l2jhellas.gameserver.handlers.itemhandlers;

import com.l2jhellas.gameserver.handler.IItemHandler;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ItemList;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public class CompSpiritShotPacks implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		5140,
		5141,
		5142,
		5143,
		5144,
		5145,
		5256,
		5257,
		5258,
		5259,
		5260,
		5261
	};
	
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		L2PcInstance activeChar = (L2PcInstance) playable;
		
		int itemId = item.getItemId();
		int itemToCreateId;
		int amount;
		
		if (itemId < 5200)
		{ // Normal Compressed Package of SpiritShots
			itemToCreateId = itemId - 2631; // Gives id of matching item for this pack
			amount = 300;
		}
		else
		{ // Greater Compressed Package of Spirithots
			itemToCreateId = itemId - 2747; // Gives id of matching item for this pack
			amount = 1000;
		}
		
		activeChar.getInventory().destroyItem("Extract", item, activeChar, null);
		activeChar.getInventory().addItem("Extract", itemToCreateId, amount, activeChar, item);
		
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
		sm.addItemName(itemToCreateId);
		sm.addNumber(amount);
		activeChar.sendPacket(sm);
		
		ItemList playerUI = new ItemList(activeChar, false);
		activeChar.sendPacket(playerUI);
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}