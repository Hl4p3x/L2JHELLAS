package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public final class SnoopQuit extends L2GameClientPacket
{
	private static final String _C__AB_SNOOPQUIT = "[C] AB SnoopQuit";
	
	private int _snoopID;
	
	@Override
	protected void readImpl()
	{
		_snoopID = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		final L2PcInstance player = L2World.getInstance().getPlayer(_snoopID);
		
		if (player == null)
			return;
		
		player.removeSnooper(activeChar);
		activeChar.removeSnooped(player);
	}
	
	@Override
	public String getType()
	{
		return _C__AB_SNOOPQUIT;
	}
}