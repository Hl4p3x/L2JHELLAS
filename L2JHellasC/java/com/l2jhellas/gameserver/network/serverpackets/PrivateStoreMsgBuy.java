package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class PrivateStoreMsgBuy extends L2GameServerPacket
{
	private static final String _S__D2_PRIVATESTOREMSGBUY = "[S] b9 PrivateStoreMsgBuy";
	private final L2PcInstance _activeChar;
	private String _storeMsg;
	
	public PrivateStoreMsgBuy(L2PcInstance player)
	{
		_activeChar = player;
		if (_activeChar.getBuyList() != null)
			_storeMsg = _activeChar.getBuyList().getTitle();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xb9);
		writeD(_activeChar.getObjectId());
		writeS(_storeMsg);
	}
	
	@Override
	public String getType()
	{
		return _S__D2_PRIVATESTOREMSGBUY;
	}
}