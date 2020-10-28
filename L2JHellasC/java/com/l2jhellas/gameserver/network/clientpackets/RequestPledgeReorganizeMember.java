package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.L2ClanMember;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.PledgeReceiveMemberInfo;

public final class RequestPledgeReorganizeMember extends L2GameClientPacket
{
	private static final String _C__D0_24_REQUESTPLEDGEREORGANIZEMEMBER = "[C] D0:24 RequestPledgeReorganizeMember";
	
	private int _SelectedMember;
	private String _memberName;
	private int _newPledgeType;
	private String _selectedMemberName;
	
	@Override
	protected void readImpl()
	{
		_SelectedMember = readD();
		_memberName = readS();
		_newPledgeType = readD();
		_selectedMemberName = readS();
	}
	
	@Override
	protected void runImpl()
	{
        final L2PcInstance activeChar = getClient().getActiveChar();
        
		if (activeChar == null)
			return;
		
		final L2Clan clan = activeChar.getClan();
		
		if (clan == null)
			return;
		
		if (!activeChar.hasClanPrivileges(L2Clan.CP_CL_MANAGE_RANKS))
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}

		final L2ClanMember member = clan.getClanMember(_memberName);
		
		if (_SelectedMember == 0)
		{
			if (member != null)
				activeChar.sendPacket(new PledgeReceiveMemberInfo(member));
			
			return;
		}
		
		final L2ClanMember member1 = clan.getClanMember(_selectedMemberName);
		
		if (member == null || member.getObjectId() == clan.getLeaderId() || member1 == null || member1.getObjectId() == clan.getLeaderId())
			return;
		
		if (clan.isSubPledgeLeader(member.getName()))
		{
			activeChar.sendPacket(new PledgeReceiveMemberInfo(member));
			return;
		}
		
		final int oldPledgeType = member.getPledgeType();
		
		if (oldPledgeType == _newPledgeType)
			return;
		
		member.setPledgeType(_newPledgeType);
		member1.setPledgeType(oldPledgeType);
		
		clan.broadcastClanStatus();
	}
	
	@Override
	public String getType()
	{
		return _C__D0_24_REQUESTPLEDGEREORGANIZEMEMBER;
	}
}