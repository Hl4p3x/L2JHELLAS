package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public final class RequestOustPartyMember extends L2GameClientPacket
{
	private static final String _C__2C_REQUESTOUSTPARTYMEMBER = "[C] 2C RequestOustPartyMember";
	
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
		if (activeChar == null)
			return;
		
		if (activeChar.isInParty() && activeChar.getParty().isLeader(activeChar))
		{
			if (activeChar.getParty().isInDimensionalRift() && !activeChar.getParty().getDimensionalRift().getRevivedAtWaitingRoom().contains(activeChar))
				activeChar.sendMessage("You can't dismiss party member when you are in Dimensional Rift.");
			else
				activeChar.getParty().removePartyMember(_name);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__2C_REQUESTOUSTPARTYMEMBER;
	}
}