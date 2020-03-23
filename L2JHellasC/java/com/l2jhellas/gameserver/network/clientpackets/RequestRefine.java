package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.datatables.xml.AugmentationData;
import com.l2jhellas.gameserver.model.L2Augmentation;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.ExVariationResult;
import com.l2jhellas.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jhellas.gameserver.network.serverpackets.ShortCutInit;
import com.l2jhellas.gameserver.network.serverpackets.SkillList;
import com.l2jhellas.gameserver.network.serverpackets.StatusUpdate;

public final class RequestRefine extends AbstractRefinePacket
{
	private static final String _C__D0_2C_REQUESTREFINE = "[C] D0:2C RequestRefine";
	private int _targetItemObjId;
	private int _refinerItemObjId;
	private int _gemStoneItemObjId;
	private int _gemStoneCount;
	
	@Override
	protected void readImpl()
	{
		_targetItemObjId = readD();
		_refinerItemObjId = readD();
		_gemStoneItemObjId = readD();
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
		{
			activeChar.sendPacket(new ExVariationResult(0, 0, 0));
			activeChar.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
			return;
		}
		
		final L2ItemInstance refinerItem = activeChar.getInventory().getItemByObjectId(_refinerItemObjId);
		if (refinerItem == null)
		{
			activeChar.sendPacket(new ExVariationResult(0, 0, 0));
			activeChar.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
			return;
		}
		
		final L2ItemInstance gemStoneItem = activeChar.getInventory().getItemByObjectId(_gemStoneItemObjId);
		if (gemStoneItem == null)
		{
			activeChar.sendPacket(new ExVariationResult(0, 0, 0));
			activeChar.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
			return;
		}
		
		if (!isValid(activeChar, targetItem, refinerItem, gemStoneItem))
		{
			activeChar.sendPacket(new ExVariationResult(0, 0, 0));
			activeChar.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
			return;
		}
		
		final LifeStone ls = getLifeStone(refinerItem.getItemId());
		if (ls == null)
		{
			activeChar.sendPacket(new ExVariationResult(0, 0, 0));
			activeChar.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
			return;
		}
		
		final int lifeStoneLevel = ls.getLevel();
		final int lifeStoneGrade = ls.getGrade();
		if (_gemStoneCount != getGemStoneCount(targetItem.getItem().getCrystalType()))
		{
			activeChar.sendPacket(new ExVariationResult(0, 0, 0));
			activeChar.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
			return;
		}
		
		if (activeChar.getActiveTradeList() != null || activeChar.getActiveWarehouse() != null || activeChar.getActiveEnchantItem() != null)
		{
			activeChar.sendMessage("You can't augment items when you got active warehouse or active trade or active enchant.");
			activeChar.sendPacket(new ActionFailed());
			return;
		}
		
		// unequip item
		if (targetItem.isEquipped())
		{
			activeChar.disarmWeapons();
			activeChar.broadcastUserInfo();
		}
		
		// Consume the life stone
		if (!activeChar.destroyItem("RequestRefine", refinerItem, null, false))
		{
			activeChar.sendPacket(new ExVariationResult(0, 0, 0));
			activeChar.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
			return;
		}
		// Consume gemstones
		if (!activeChar.destroyItem("RequestRefine", gemStoneItem.getObjectId(), _gemStoneCount, null, false))
		{
			activeChar.sendPacket(new ExVariationResult(0, 0, 0));
			activeChar.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
			return;
		}
		
		final L2Augmentation aug = AugmentationData.getInstance().generateRandomAugmentation(targetItem, lifeStoneLevel, lifeStoneGrade);
		targetItem.setAugmentation(aug);
		
		final int stat12 = 0x0000FFFF & aug.getAugmentationId();
		final int stat34 = aug.getAugmentationId() >> 16;
		activeChar.sendPacket(new ExVariationResult(stat12, stat34, 1));
		
		InventoryUpdate iu = new InventoryUpdate();
		iu.addModifiedItem(targetItem);
		activeChar.sendPacket(iu);
		
		StatusUpdate su = new StatusUpdate(activeChar.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, activeChar.getCurrentLoad());
		activeChar.sendPacket(su);
		activeChar.sendPacket(new SkillList());
		activeChar.sendPacket(new ShortCutInit(activeChar));
	}
	
	@Override
	public String getType()
	{
		return _C__D0_2C_REQUESTREFINE;
	}
}