package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.xml.HennaData;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.templates.L2Henna;
import com.l2jhellas.util.Util;

public final class RequestHennaEquip extends L2GameClientPacket
{
	private static final String _C__BC_RequestHennaEquip = "[C] bc RequestHennaEquip";
	private int _symbolId;
	
	// format cd
	
	@Override
	protected void readImpl()
	{
		_symbolId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		final L2Henna henna = HennaData.getInstance().getTemplate(_symbolId);
		if (henna == null)
			return;
		
		if (!henna.isForThisClass(activeChar))
		{
			activeChar.sendPacket(SystemMessageId.CANT_DRAW_SYMBOL);
			Util.handleIllegalPlayerAction(activeChar, activeChar.getName() + " of account " + activeChar.getAccountName() + " tried to add a forbidden henna.", Config.DEFAULT_PUNISH);
			return;
		}
		
		if (activeChar.getHennaEmptySlots() == 0)
		{
			activeChar.sendPacket(SystemMessageId.SYMBOLS_FULL);
			return;
		}
		
		final L2ItemInstance ownedDyes = activeChar.getInventory().getItemByItemId(henna.getDyeId());
		final int count = (ownedDyes == null) ? 0 : ownedDyes.getCount();
		
		if (count < L2Henna.getAmountDyeRequire())
		{
			activeChar.sendPacket(SystemMessageId.CANT_DRAW_SYMBOL);
			return;
		}
		
		// reduceAdena sends a message.
		if (!activeChar.reduceAdena("Henna", henna.getPrice(), activeChar.getLastFolkNPC(), true))
			return;
		
		// destroyItemByItemId sends a message.
		if (!activeChar.destroyItemByItemId("Henna", henna.getDyeId(), L2Henna.getAmountDyeRequire(), activeChar, true))
			return;
		
		activeChar.addHenna(henna);
	}
	
	@Override
	public String getType()
	{
		return _C__BC_RequestHennaEquip;
	}
}