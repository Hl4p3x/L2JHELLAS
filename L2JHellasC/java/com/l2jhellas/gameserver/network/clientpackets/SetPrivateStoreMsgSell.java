package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.PrivateStoreMsgSell;

public class SetPrivateStoreMsgSell extends L2GameClientPacket
{
	private static final String _C__77_SETPRIVATESTOREMSGSELL = "[C] 77 SetPrivateStoreMsgSell";
	
	private String _storeMsg;
	
	@Override
	protected void readImpl()
	{
		_storeMsg = readS();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if ((player == null) || (player.getSellList() == null))
			return;
		
		if (_storeMsg != null && _storeMsg.length() > 29)
			return;
		
		player.getSellList().setTitle(_storeMsg);
		sendPacket(new PrivateStoreMsgSell(player));
	}
	
	@Override
	public String getType()
	{
		return _C__77_SETPRIVATESTOREMSGSELL;
	}
}