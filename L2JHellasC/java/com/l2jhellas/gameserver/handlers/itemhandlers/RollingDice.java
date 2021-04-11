package com.l2jhellas.gameserver.handlers.itemhandlers;

import com.l2jhellas.gameserver.handler.IItemHandler;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.Dice;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.shield.antiflood.FloodProtectors;
import com.l2jhellas.shield.antiflood.FloodProtectors.FloodAction;
import com.l2jhellas.util.Broadcast;
import com.l2jhellas.util.Rnd;

public class RollingDice implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		4625,
		4626,
		4627,
		4628
	};
	
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		
		L2PcInstance activeChar = (L2PcInstance) playable;
		
		if (!FloodProtectors.performAction(activeChar.getClient(), FloodAction.DICE_ROLL))
		{
			activeChar.sendPacket(SystemMessageId.YOU_MAY_NOT_THROW_THE_DICE_AT_THIS_TIME_TRY_AGAIN_LATER);
			return;
		}
		
		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			return;
		}
		
		int itemId = item.getItemId();
		
		if (itemId == 4625 || itemId == 4626 || itemId == 4627 || itemId == 4628)
		{
			final int number = Rnd.get(1, 6);
			Broadcast.toSelfAndKnownPlayers(activeChar, new Dice(activeChar.getObjectId(), item.getItemId(), number, activeChar.getX() - 30, activeChar.getY() - 30, activeChar.getZ()));
			Broadcast.toSelfAndKnownPlayers(activeChar, SystemMessage.getSystemMessage(SystemMessageId.S1_ROLLED_S2).addCharName(activeChar).addNumber(number));
			
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}