package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.datatables.sql.ClanTable;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;

public final class RequestReplyStartPledgeWar extends L2GameClientPacket
{
	private static final String _C__4e_REQUESTREPLYSTARTPLEDGEWAR = "[C] 4e RequestReplyStartPledgeWar";
	
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
			ClanTable.getInstance().storeclanswars(requestor.getClanId(), activeChar.getClanId());
		}
		else
		{
			requestor.sendPacket(SystemMessageId.WAR_PROCLAMATION_HAS_BEEN_REFUSED);
		}
		activeChar.setActiveRequester(null);
		requestor.onTransactionResponse();
	}
	
	@Override
	public String getType()
	{
		return _C__4e_REQUESTREPLYSTARTPLEDGEWAR;
	}
}