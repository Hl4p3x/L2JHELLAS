package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.ItemList;

public final class RequestItemList extends L2GameClientPacket
{
	private static final String _C__0F_REQUESTITEMLIST = "[C] 0F RequestItemList";
	
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		
		if (player.isInvetoryDisabled())
			return;
		
		sendPacket(new ItemList(player, true));
	}
	
	@Override
	public String getType()
	{
		return _C__0F_REQUESTITEMLIST;
	}
}