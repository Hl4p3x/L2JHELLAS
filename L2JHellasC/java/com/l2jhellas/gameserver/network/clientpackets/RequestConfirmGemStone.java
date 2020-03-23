package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ExConfirmVariationGemstone;

public final class RequestConfirmGemStone extends AbstractRefinePacket
{
	private static final String _C__D0_2B_REQUESTCONFIRMGEMSTONE = "[C] D0:2B RequestConfirmGemStone";
	
	private int _targetItemObjId;
	private int _refinerItemObjId;
	private int _gemstoneItemObjId;
	private int _gemStoneCount;
	
	@Override
	protected void readImpl()
	{
		_targetItemObjId = readD();
		_refinerItemObjId = readD();
		_gemstoneItemObjId = readD();
		_gemStoneCount = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		final L2ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);
		
		if (targetItem == null)
			return;
		
		final L2ItemInstance refinerItem = activeChar.getInventory().getItemByObjectId(_refinerItemObjId);
		
		if (refinerItem == null)
			return;
		
		final L2ItemInstance gemStoneItem = activeChar.getInventory().getItemByObjectId(_gemstoneItemObjId);
		
		if (gemStoneItem == null)
			return;
		
		if (!isValid(activeChar, targetItem, refinerItem, gemStoneItem))
		{
			activeChar.sendPacket(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM);
			return;
		}
		
		final LifeStone ls = getLifeStone(refinerItem.getItemId());
		
		if (ls == null)
			return;
		
		if (_gemStoneCount != getGemStoneCount(targetItem.getItem().getCrystalType()))
		{
			activeChar.sendPacket(SystemMessageId.GEMSTONE_QUANTITY_IS_INCORRECT);
			return;
		}
		
		activeChar.sendPacket(new ExConfirmVariationGemstone(_gemstoneItemObjId, _gemStoneCount));
		activeChar.sendPacket(SystemMessageId.PRESS_THE_AUGMENT_BUTTON_TO_BEGIN);
	}
	
	@Override
	public String getType()
	{
		return _C__D0_2B_REQUESTCONFIRMGEMSTONE;
	}
}