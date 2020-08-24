package com.l2jhellas.gameserver.handlers.itemhandlers;

import com.l2jhellas.gameserver.handler.IItemHandler;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PetInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.serverpackets.SSQStatus;

public class SevenSignsRecord implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		5707
	};
	
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		L2PcInstance activeChar;
		
		if (playable instanceof L2PcInstance)
			activeChar = (L2PcInstance) playable;
		
		else if (playable instanceof L2PetInstance)
			activeChar = ((L2PetInstance) playable).getOwner();
		else
			return;
		
		activeChar.sendPacket(new SSQStatus(activeChar, 1));
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}