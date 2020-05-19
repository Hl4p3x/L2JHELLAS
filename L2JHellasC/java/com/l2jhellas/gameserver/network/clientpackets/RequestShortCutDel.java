package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public final class RequestShortCutDel extends L2GameClientPacket
{
	private static final String _C__35_REQUESTSHORTCUTDEL = "[C] 35 RequestShortCutDel";
	
	private int _slot;
	private int _page;
	
	@Override
	protected void readImpl()
	{
		int id = readD();
		_slot = id % 12;
		_page = id / 12;
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		if (_page > 10 || _page < 0)
			return;
		
		activeChar.deleteShortCut(_slot, _page);
	}
	
	@Override
	public String getType()
	{
		return _C__35_REQUESTSHORTCUTDEL;
	}
}