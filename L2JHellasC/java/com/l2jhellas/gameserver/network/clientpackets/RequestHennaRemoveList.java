package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.HennaRemoveList;

public final class RequestHennaRemoveList extends L2GameClientPacket
{
	private static final String _C__0XBD_RequestHennaRemoveList = "[C] 0xbd RequestHennaRemoveList";
	
	@SuppressWarnings("unused")
	private int _unknown;
	
	@Override
	protected void readImpl()
	{
		_unknown = readD(); // ??
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		activeChar.sendPacket(new HennaRemoveList(activeChar));
	}
	
	@Override
	public String getType()
	{
		return _C__0XBD_RequestHennaRemoveList;
	}
}