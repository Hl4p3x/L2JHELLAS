package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.L2ClanMember;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.PledgeShowMemberListUpdate;

public final class RequestPledgeReorganizeMember extends L2GameClientPacket
{
	private static final String _C__D0_24_REQUESTPLEDGEREORGANIZEMEMBER = "[C] D0:24 RequestPledgeReorganizeMember";
	
	@SuppressWarnings("unused")
	private int _unk1;
	private String _memberName;
	private int _newPledgeType;
	@SuppressWarnings("unused")
	private String _unk2;
	
	@Override
	protected void readImpl()
	{
		_unk1 = readD();
		_memberName = readS();
		_newPledgeType = readD();
		_unk2 = readS();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		// do we need powers to do that??
		L2Clan clan = activeChar.getClan();
		if (clan == null)
			return;
		L2ClanMember member = clan.getClanMember(_memberName);
		if (member == null)
			return;
		member.setPledgeType(_newPledgeType);
		clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(member));
	}
	
	@Override
	public String getType()
	{
		return _C__D0_24_REQUESTPLEDGEREORGANIZEMEMBER;
	}
}