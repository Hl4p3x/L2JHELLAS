package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.datatables.sql.ClanTable;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public final class RequestReplySurrenderPledgeWar extends L2GameClientPacket
{
	private static final String _C__52_REQUESTREPLYSURRENDERPLEDGEWAR = "[C] 52 RequestReplySurrenderPledgeWar";
	
	private int _answer;
	
	@Override
	protected void readImpl()
	{
		readS();
		_answer = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		L2PcInstance requestor = activeChar.getActiveRequester();
		if (requestor == null)
			return;
		
		if (_answer == 1)
		{
			requestor.deathPenalty(false, false, false);
			ClanTable.getInstance().deleteclanswars(requestor.getClanId(), activeChar.getClanId());
		}
		
		activeChar.onTransactionRequest(requestor);
	}
	
	@Override
	public String getType()
	{
		return _C__52_REQUESTREPLYSURRENDERPLEDGEWAR;
	}
}