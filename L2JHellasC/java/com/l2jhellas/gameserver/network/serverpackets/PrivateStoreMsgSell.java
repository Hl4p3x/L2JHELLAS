package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class PrivateStoreMsgSell extends L2GameServerPacket
{
	private static final String _S__B5_PRIVATESTOREMSGSELL = "[S] 9c PrivateStoreMsgSell";
	private final L2PcInstance _activeChar;
	private String _storeMsg;
	
	public PrivateStoreMsgSell(L2PcInstance player)
	{
		_activeChar = player;
		if (_activeChar.getSellList() != null)
			_storeMsg = _activeChar.getSellList().getTitle();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x9c);
		writeD(_activeChar.getObjectId());
		writeS(_storeMsg);
	}
	
	@Override
	public String getType()
	{
		return _S__B5_PRIVATESTOREMSGSELL;
	}
}