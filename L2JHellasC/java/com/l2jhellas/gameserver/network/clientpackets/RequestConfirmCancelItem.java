package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ExConfirmCancelItem;

public final class RequestConfirmCancelItem extends L2GameClientPacket
{
	private static final String _C__D0_2D_REQUESTCONFIRMCANCELITEM = "[C] D0:2D RequestConfirmCancelItem";
	private int _objectId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
			return;
		
		final L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);

		if (item == null)
			return;
		
		if (item.getOwnerId() != activeChar.getObjectId())
			return;
		
		if (!item.isAugmented())
		{
			activeChar.sendPacket(SystemMessageId.AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM);
			return;
		}
		
		int price = 0;
		switch (item.getItem().getCrystalType())
		{
			case C:
				if (item.getCrystalCount() < 1720)
					price = 95000;
				else if (item.getCrystalCount() < 2452)
					price = 150000;
				else
					price = 210000;
				break;
			case B:
				if (item.getCrystalCount() < 1746)
					price = 240000;
				else
					price = 270000;
				break;
			case A:
				if (item.getCrystalCount() < 2160)
					price = 330000;
				else if (item.getCrystalCount() < 2824)
					price = 390000;
				else
					price = 420000;
				break;
			case S:
				price = 480000;
				break;
			// any other item type is not augmentable
			default:
				return;
		}
		activeChar.sendPacket(new ExConfirmCancelItem(item, price));
	}
	
	@Override
	public String getType()
	{
		return _C__D0_2D_REQUESTCONFIRMCANCELITEM;
	}
}