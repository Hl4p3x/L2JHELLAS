package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class RecipeShopMsg extends L2GameServerPacket
{
	private static final String _S__DB_RecipeShopMsg = "[S] db RecipeShopMsg";
	private final L2PcInstance _activeChar;
	
	public RecipeShopMsg(L2PcInstance player)
	{
		_activeChar = player;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xdb);
		writeD(_activeChar.getObjectId());
		writeS(_activeChar.getCreateList().getStoreName());// _activeChar.getTradeList().getSellStoreName());
	}
	
	@Override
	public String getType()
	{
		return _S__DB_RecipeShopMsg;
	}
}