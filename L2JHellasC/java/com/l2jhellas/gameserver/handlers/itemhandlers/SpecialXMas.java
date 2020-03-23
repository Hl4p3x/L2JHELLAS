package com.l2jhellas.gameserver.handlers.itemhandlers;

import com.l2jhellas.gameserver.handler.IItemHandler;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.ShowXMasSeal;

public class SpecialXMas implements IItemHandler
{
	private static int[] _itemIds =
	{
		5555
	};
	
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
		{
			return;
		}
		
		L2PcInstance activeChar = (L2PcInstance) playable;
		int itemId = item.getItemId();
		
		if (activeChar.isParalyzed())
		{
			activeChar.sendMessage("You can not use this while You are paralyzed.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (itemId == 5555)
		{
			ShowXMasSeal SXS = new ShowXMasSeal(5555);
			activeChar.sendPacket(SXS);
			SXS = null;
		}
		activeChar = null;
	}
	
	@Override
	public int[] getItemIds()
	{
		return _itemIds;
	}
}