package com.l2jhellas.gameserver.handlers.itemhandlers;

import com.l2jhellas.gameserver.handler.IItemHandler;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.instance.L2GourdInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.SkillTable;

public class Nectar implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		6391
	};
	
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
		{
			return;
		}
		
		L2PcInstance activeChar = (L2PcInstance) playable;
		
		if (!(activeChar.getTarget() instanceof L2GourdInstance))
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return;
		}
		
		if (activeChar.getName() != ((L2GourdInstance) activeChar.getTarget()).getOwner())
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return;
		}
		
		L2Object[] targets = new L2Object[1];
		targets[0] = activeChar.getTarget();
		
		int itemId = item.getItemId();
		if (itemId == 6391)
		{
			activeChar.useMagic(SkillTable.getInstance().getInfo(9999, 1), false, false);
		}
		
		activeChar = null;
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
	
}