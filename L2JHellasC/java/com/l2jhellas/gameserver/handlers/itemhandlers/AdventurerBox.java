package com.l2jhellas.gameserver.handlers.itemhandlers;

import com.l2jhellas.gameserver.handler.IItemHandler;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.shield.antiflood.FloodProtectors;
import com.l2jhellas.shield.antiflood.FloodProtectors.Action;
import com.l2jhellas.util.Rnd;

/**
 * @author AbsolutePower
 */
public class AdventurerBox implements IItemHandler
{				
	private static final int CRADLE_OF_CREATION = 8175;
	
	private static final int[][] ACCESSORY =
	{
	  {6845},{8564},{7682},{7837},{7059},{6843},{8560},{8561},{7836},{8922},{8919},
	  {8562},{8563},{8565},{8567},{8568},{8923},{8916},{8918},{8936},{8569}
	};
	
	private static final int[][] HAIR_ACCESSORY =
	{
	  {8187},{8552},{7683},{7695},{8910},
	  {8912},{8913},{8920},{7696}
	};
	
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable.isPlayer()))
			return;
			
		final L2PcInstance activeChar = (L2PcInstance) playable;

		if (!FloodProtectors.performAction(activeChar.getClient(), Action.ITEM_HANDLER))
		{
			activeChar.sendMessage("You are using this action too fast.");
			return;
		}
		
		final L2ItemInstance box = activeChar.getInventory().getItemByObjectId(item.getObjectId());
		
		if(box == null)
		{
			activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			return;
		}
		
		final int itemId = box.getItemId();

		if (!activeChar.destroyItem("AdventurerBox",box.getObjectId(), 1, null, false))
		{
			activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			return;
		}
		
		activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_DISAPPEARED).addItemName(itemId));
	
		if (Rnd.get(100) < 40)
		{
			switch (itemId)
			{
				case 8534:
				case 8535:
				case 8536:
				case 8537:
				case 8538:
				case 8360:
				case 8361:
					addItem(activeChar,ACCESSORY[Rnd.get(21)][0]);
					break;

				case 8539:
				case 8362:
					addItem(activeChar,HAIR_ACCESSORY[Rnd.get(9)][0]);
					break;

				case 8540:
				case 8363:
					addItem(activeChar,CRADLE_OF_CREATION);
					break;
			}			
		}
	}
	
	public void addItem(L2PcInstance activeChar, int itemId)
	{
		activeChar.addItem("AdventurerBox", itemId, 1, null,false);
		activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1).addItemName(itemId));
	}
	
	//TODO grade check
	private static final int[] ITEM_IDS =
	{
		8534,//C-Grade Accessory (Low Grade)
		8535,//C-Grade Accessory (Medium Grade)
		8536,//C-Grade Accessory (High Grade)
		8537,//B-Grade Accessory (Low Grade)
		8538,//B-Grade Accessory (High Grade)
		8539,//Hair Accessory
		8540,//Cradle of Creation
		8360,//C Accessory
		8361,//B Accessory
		8362,//Hair Accessory
		8363//Cradle of Creation							
	};
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}