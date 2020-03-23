package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.datatables.sql.ClanTable;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public final class RequestSurrenderPledgeWar extends L2GameClientPacket
{
	private static final String _C__51_REQUESTSURRENDERPLEDGEWAR = "[C] 51 RequestSurrenderPledgeWar";
	
	private String _pledgeName;
	
	@Override
	protected void readImpl()
	{
		_pledgeName = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		final L2Clan playerClan = activeChar.getClan();
		if (playerClan == null)
			return;
		
		// Check if player who does the request has the correct rights to do it
		if ((activeChar.getClanPrivileges() & L2Clan.CP_CL_PLEDGE_WAR) != L2Clan.CP_CL_PLEDGE_WAR)
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		final L2Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);
		if (clan == null)
			return;
		
		if (!playerClan.isAtWarWith(clan.getClanId()))
		{
			activeChar.sendPacket(SystemMessageId.NOT_INVOLVED_IN_WAR);
			return;
		}
		
		activeChar.deathPenalty(false, false, false);
		activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_SURRENDERED_TO_THE_S1_CLAN).addString(_pledgeName));
		ClanTable.getInstance().deleteclanswars(playerClan.getClanId(), clan.getClanId());
	}
	
	@Override
	public String getType()
	{
		return _C__51_REQUESTSURRENDERPLEDGEWAR;
	}
}