package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ExConfirmVariationItem;

public final class RequestConfirmTargetItem extends AbstractRefinePacket
{
	private static final String _C__D0_29_REQUESTCONFIRMTARGETITEM = "[C] D0:29 RequestConfirmTargetItem";
	private int _itemObjId;
	
	@Override
	protected void readImpl()
	{
		_itemObjId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		final L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_itemObjId);
		
		if (item == null)
			return;
		
		if (!isValid(activeChar, item))
		{
			if (item.isAugmented())
			{
				activeChar.sendPacket(SystemMessageId.ONCE_AN_ITEM_IS_AUGMENTED_IT_CANNOT_BE_AUGMENTED_AGAIN);
				return;
			}
			
			activeChar.sendPacket(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM);
			return;
		}
		
		activeChar.sendPacket(new ExConfirmVariationItem(_itemObjId));
	}
	
	@Override
	public String getType()
	{
		return _C__D0_29_REQUESTCONFIRMTARGETITEM;
	}
}