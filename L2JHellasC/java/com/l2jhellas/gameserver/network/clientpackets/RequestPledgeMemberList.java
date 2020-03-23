package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.L2Clan.SubPledge;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.PledgeShowMemberListAll;

public final class RequestPledgeMemberList extends L2GameClientPacket
{
	private static final String _C__3C_REQUESTPLEDGEMEMBERLIST = "[C] 3C RequestPledgeMemberList";
	
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		final L2Clan clan = activeChar.getClan();
		
		if (clan != null)
		{
			activeChar.sendPacket(new PledgeShowMemberListAll(clan, 0));
			
			for (SubPledge sp : clan.getAllSubPledges())
				activeChar.sendPacket(new PledgeShowMemberListAll(clan, sp.getId()));
		}
	}
	
	@Override
	public String getType()
	{
		return _C__3C_REQUESTPLEDGEMEMBERLIST;
	}
}