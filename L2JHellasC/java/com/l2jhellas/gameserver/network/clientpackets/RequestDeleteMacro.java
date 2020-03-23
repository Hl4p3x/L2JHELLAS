package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public final class RequestDeleteMacro extends L2GameClientPacket
{
	private static final String _C__C2_REQUESTDELETEMACRO = "[C] C2 RequestDeleteMacro";
	private int _id;
	
	@Override
	protected void readImpl()
	{
		_id = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		activeChar.deleteMacro(_id);
	}
	
	@Override
	public String getType()
	{
		return _C__C2_REQUESTDELETEMACRO;
	}
}