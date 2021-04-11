package com.l2jhellas.gameserver.handlers.itemhandlers;

import com.l2jhellas.gameserver.datatables.xml.ExtractableItemData;
import com.l2jhellas.gameserver.handler.IItemHandler;
import com.l2jhellas.gameserver.holder.IntIntHolder;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.model.actor.item.Extractable.ExtractableItem;
import com.l2jhellas.gameserver.model.actor.item.Extractable.ExtractableProductItem;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ItemList;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.shield.antiflood.FloodProtectors;
import com.l2jhellas.shield.antiflood.FloodProtectors.FloodAction;
import com.l2jhellas.util.Rnd;

/**
 * @author AbsolutePower
 */
public class ExtractableItems implements IItemHandler
{	
	private static final int[] ITEM_IDS = ExtractableItemData.getInstance().getAllItemIds();

	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable.isPlayer()))
			return;

		final L2PcInstance player = (L2PcInstance) playable;

		if (!FloodProtectors.performAction(player.getClient(), FloodAction.ITEM_HANDLER))
		{
			player.sendMessage("You may not use the item at this time. Try again later.");
			return;
		}

		final L2ItemInstance extitem = player.getInventory().getItemByObjectId(item.getObjectId());

		if(extitem == null)
		{
			player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			return;
		}

		if (!player.destroyItem("Extract",extitem.getObjectId(), 1, null, false))
		{
			player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			return;
		}

		final int itemId = extitem.getItemId();

		final ExtractableItem extractable = ExtractableItemData.getInstance().getExtractableItem(itemId);

		if (extractable == null)
			return;

		player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_DISAPPEARED).addItemName(itemId));

		boolean created =  false;
		for (ExtractableProductItem expi : extractable.getProductItems())
		{
			final double chance = expi.getChance();
			if (Rnd.get(100) <= chance )
			{		
				IntIntHolder expit = expi.getItems().get(Rnd.get(expi.getItems().size()));

				if (!player.getInventory().validateCapacityByItemId(expit.getId()))
				{
					player.sendPacket(SystemMessageId.SLOTS_FULL);
					break;
				}

				player.addItem("extract", expit.getId(), expit.getValue(), null, false);
				
				player.sendPacket(expit.getValue() > 1 ? new SystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(expit.getId()).addNumber(expit.getValue())
				: new SystemMessage(SystemMessageId.EARNED_ITEM_S1).addItemName(expit.getId()));
				
				player.sendPacket(new ItemList(player,false));
				created = true;
				break;
			}
		}

		if(!created)
			player.sendPacket(SystemMessageId.NOTHING_INSIDE_THAT);		
	}

	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}