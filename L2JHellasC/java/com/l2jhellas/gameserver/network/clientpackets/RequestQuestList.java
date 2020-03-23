package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.QuestList;

public final class RequestQuestList extends L2GameClientPacket
{
	private static final String _C__63_REQUESTQUESTLIST = "[C] 63 RequestQuestList";
	
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
		
		activeChar.sendPacket(new QuestList(activeChar));
	}
	
	@Override
	public String getType()
	{
		return _C__63_REQUESTQUESTLIST;
	}
}