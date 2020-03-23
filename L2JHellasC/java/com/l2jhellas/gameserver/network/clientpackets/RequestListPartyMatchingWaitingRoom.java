package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.ExListPartyMatchingWaitingRoom;

public class RequestListPartyMatchingWaitingRoom extends L2GameClientPacket
{
	private static final String _C__D0_16_REQUESTLISTPARTYMATCHINGWAITINGROOM = "[C] D0:16 RequestListPartyMatchingWaitingRoom";
	
	private static int _page;
	private static int _minlvl;
	private static int _maxlvl;
	private static int _mode; // 1 waitlist, 0 room waitlist
	
	@Override
	protected void readImpl()
	{
		_page = readD();
		_minlvl = readD();
		_maxlvl = readD();
		_mode = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		activeChar.sendPacket(new ExListPartyMatchingWaitingRoom(activeChar, _page, _minlvl, _maxlvl, _mode));
	}
	
	@Override
	public String getType()
	{
		return _C__D0_16_REQUESTLISTPARTYMATCHINGWAITINGROOM;
	}
}