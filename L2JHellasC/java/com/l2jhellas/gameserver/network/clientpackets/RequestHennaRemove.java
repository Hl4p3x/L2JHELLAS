package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.templates.L2Henna;

public final class RequestHennaRemove extends L2GameClientPacket
{
	private static final String _C__0XBF_RequestHennaRemove = "[C] 0xbf RequestHennaRemove";
	
	private int _symbolId;
	
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
		
		for (int i = 1; i <= 3; i++)
		{
			L2Henna henna = activeChar.getHenna(i);
			if (henna != null && henna.getSymbolId() == _symbolId)
			{
				if (activeChar.getAdena() >= (henna.getPrice() / 5))
				{
					activeChar.removeHenna(i);
					break;
				}
				activeChar.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _C__0XBF_RequestHennaRemove;
	}
}