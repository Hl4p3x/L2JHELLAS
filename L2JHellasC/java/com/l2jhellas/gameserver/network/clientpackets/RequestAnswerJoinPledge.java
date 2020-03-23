package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.L2Clan.SubPledge;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.JoinPledge;
import com.l2jhellas.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import com.l2jhellas.gameserver.network.serverpackets.PledgeShowMemberListAdd;
import com.l2jhellas.gameserver.network.serverpackets.PledgeShowMemberListAll;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public final class RequestAnswerJoinPledge extends L2GameClientPacket
{
	private static final String _C__25_REQUESTANSWERJOINPLEDGE = "[C] 25 RequestAnswerJoinPledge";
	
	private int _answer;
	
	@Override
	protected void readImpl()
	{
		_answer = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		final L2PcInstance requestor = activeChar.getRequest().getPartner();
		if (requestor == null)
		{
			return;
		}
		
		if (_answer == 0)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_DID_NOT_RESPOND_TO_S1_CLAN_INVITATION);
			sm.addString(requestor.getName());
			activeChar.sendPacket(sm);
			sm = null;
			sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DID_NOT_RESPOND_TO_CLAN_INVITATION);
			sm.addString(activeChar.getName());
			requestor.sendPacket(sm);
			sm = null;
		}
		else
		{
			if (!(requestor.getRequest().getRequestPacket() instanceof RequestJoinPledge))
				return;
			
			final RequestJoinPledge requestPacket = (RequestJoinPledge) requestor.getRequest().getRequestPacket();
			final L2Clan clan = requestor.getClan();
			// we must double check this cause during response time conditions can be changed, i.e. another player could join clan
			if (clan.checkClanJoinCondition(requestor, activeChar, requestPacket.getPledgeType()))
			{
				final JoinPledge jp = new JoinPledge(requestor.getClanId());
				activeChar.sendPacket(jp);
				
				activeChar.setPledgeType(requestPacket.getPledgeType());
				
				switch (requestPacket.getPledgeType())
				{
					case L2Clan.SUBUNIT_ACADEMY:
						activeChar.setPowerGrade(9);
						activeChar.setLvlJoinedAcademy(activeChar.getLevel());
						break;
					
					case L2Clan.SUBUNIT_ROYAL1:
					case L2Clan.SUBUNIT_ROYAL2:
						activeChar.setPowerGrade(7);
						break;
					
					case L2Clan.SUBUNIT_KNIGHT1:
					case L2Clan.SUBUNIT_KNIGHT2:
					case L2Clan.SUBUNIT_KNIGHT3:
					case L2Clan.SUBUNIT_KNIGHT4:
						activeChar.setPowerGrade(8);
						break;
					
					default:
						activeChar.setPowerGrade(6);
				}
				
				clan.addClanMember(activeChar);
				activeChar.setClanPrivileges(activeChar.getClan().getRankPrivs(activeChar.getPowerGrade()));
				
				activeChar.sendPacket(SystemMessageId.ENTERED_THE_CLAN);
				clan.broadcastToOtherOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_JOINED_CLAN).addCharName(activeChar), activeChar);
				clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListAdd(activeChar), activeChar);
				clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
				
				activeChar.sendPacket(new PledgeShowMemberListAll(clan, 0));
				
				for (SubPledge sp : activeChar.getClan().getAllSubPledges())
					activeChar.sendPacket(new PledgeShowMemberListAll(clan, sp.getId()));
				
				activeChar.setClanJoinExpiryTime(0);
				activeChar.broadcastUserInfo();
			}
		}
		activeChar.getRequest().onRequestResponse();
	}
	
	@Override
	public String getType()
	{
		return _C__25_REQUESTANSWERJOINPLEDGE;
	}
}