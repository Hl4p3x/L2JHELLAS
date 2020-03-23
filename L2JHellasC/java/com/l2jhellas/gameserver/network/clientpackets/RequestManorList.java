package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.ExSendManorList;

public class RequestManorList extends L2GameClientPacket
{
	private static final String _C__FE_08_REQUESTMANORLIST = "[S] FE:08 RequestManorList";
	
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		
		if (player == null)
			return;
		
		player.sendPacket(ExSendManorList.STATIC_PACKET);
	}
	
	@Override
	public String getType()
	{
		return _C__FE_08_REQUESTMANORLIST;
	}
}