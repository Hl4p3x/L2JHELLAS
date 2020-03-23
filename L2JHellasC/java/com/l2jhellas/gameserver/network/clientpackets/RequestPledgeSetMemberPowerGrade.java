package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.L2ClanMember;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public final class RequestPledgeSetMemberPowerGrade extends L2GameClientPacket
{
	private static final String _C__D0_1C_REQUESTPLEDGESETMEMBERPOWERGRADE = "[C] D0:1C RequestPledgeSetMemberPowerGrade";
	private int _powerGrade;
	private String _member;
	
	@Override
	protected void readImpl()
	{
		_member = readS();
		_powerGrade = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		L2Clan clan = activeChar.getClan();
		if (clan == null)
			return;
		L2ClanMember member = clan.getClanMember(_member);
		if (member == null)
			return;
		if (member.getPledgeType() == L2Clan.SUBUNIT_ACADEMY)
		{
			// also checked from client side
			activeChar.sendMessage("You cannot change academy member grade");
			return;
		}
		member.setPowerGrade(_powerGrade);
		clan.broadcastClanStatus();
	}
	
	@Override
	public String getType()
	{
		return _C__D0_1C_REQUESTPLEDGESETMEMBERPOWERGRADE;
	}
}