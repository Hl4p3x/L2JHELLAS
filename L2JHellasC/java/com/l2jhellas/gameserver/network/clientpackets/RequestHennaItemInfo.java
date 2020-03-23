package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.datatables.xml.HennaData;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.HennaItemInfo;
import com.l2jhellas.gameserver.templates.L2Henna;

public final class RequestHennaItemInfo extends L2GameClientPacket
{
	private static final String _C__BB_RequestHennaItemInfo = "[C] bb RequestHennaItemInfo";
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
		
		final L2Henna template = HennaData.getInstance().getTemplate(_symbolId);
		if (template == null)
			return;
		
		activeChar.sendPacket(new HennaItemInfo(template, activeChar));
	}
	
	@Override
	public String getType()
	{
		return _C__BB_RequestHennaItemInfo;
	}
}