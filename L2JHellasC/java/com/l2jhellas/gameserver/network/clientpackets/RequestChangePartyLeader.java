package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public final class RequestChangePartyLeader extends L2GameClientPacket
{
	private static final String _C__EE_REQUESTCHANGEPARTYLEADER = "[C] EE RequestChangePartyLeader";
	
	private String _name;
	
	@Override
	protected void readImpl()
	{
		_name = readS();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null || _name.isEmpty())
			return;
		
		if (activeChar.isInParty() && activeChar.getParty().isLeader(activeChar))
			activeChar.getParty().changePartyLeader(_name);
	}
	
	@Override
	public String getType()
	{
		return _C__EE_REQUESTCHANGEPARTYLEADER;
	}
}