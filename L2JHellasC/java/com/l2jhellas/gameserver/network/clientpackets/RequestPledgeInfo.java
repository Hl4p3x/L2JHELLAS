package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.datatables.sql.ClanTable;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.PledgeInfo;

public final class RequestPledgeInfo extends L2GameClientPacket
{
	private static final String _C__66_REQUESTPLEDGEINFO = "[C] 66 RequestPledgeInfo";
	
	private int _clanId;
	
	@Override
	protected void readImpl()
	{
		_clanId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		final L2Clan clan = ClanTable.getInstance().getClan(_clanId);
		
		if (clan == null)
			return;
		
		activeChar.sendPacket(new PledgeInfo(clan));
	}
	
	@Override
	public String getType()
	{
		return _C__66_REQUESTPLEDGEINFO;
	}
}