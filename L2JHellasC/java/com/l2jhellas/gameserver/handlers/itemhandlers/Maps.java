package com.l2jhellas.gameserver.handlers.itemhandlers;

import com.l2jhellas.gameserver.handler.IItemHandler;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.serverpackets.RadarControl;
import com.l2jhellas.gameserver.network.serverpackets.ShowMiniMap;

public class Maps implements IItemHandler
{
	private static final int[] ITEM_IDS ={1665,1863,7063};
	
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;
			
		final L2PcInstance activeChar = (L2PcInstance) playable;
		
		final int itemId = item.getItemId();
		
		if (itemId == 7063)
		{
			activeChar.sendPacket(new ShowMiniMap(1665));
			activeChar.sendPacket(new RadarControl(0, 1, 51995, -51265, -3104));
		}
		else
			activeChar.sendPacket(new ShowMiniMap(itemId));
		return;
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}