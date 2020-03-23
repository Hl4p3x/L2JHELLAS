package com.l2jhellas.gameserver.network.clientpackets;

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
		if ((getClient() != null) && (getClient().getActiveChar() != null) && !getClient().getActiveChar().isInvetoryDisabled())
		{
			ItemList il = new ItemList(getClient().getActiveChar(), true);
			sendPacket(il);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__0F_REQUESTITEMLIST;
	}
}