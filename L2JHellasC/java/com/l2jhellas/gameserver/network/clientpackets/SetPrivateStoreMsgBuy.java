package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.PrivateStoreMsgBuy;

public final class SetPrivateStoreMsgBuy extends L2GameClientPacket
{
	private static final String _C__94_SETPRIVATESTOREMSGBUY = "[C] 94 SetPrivateStoreMsgBuy";
	
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
		if ((player == null) || (player.getBuyList() == null))
			return;
		
		if (_storeMsg != null && _storeMsg.length() > 29)
			return;
		
		player.getBuyList().setTitle(_storeMsg);
		player.sendPacket(new PrivateStoreMsgBuy(player));
	}
	
	@Override
	public String getType()
	{
		return _C__94_SETPRIVATESTOREMSGBUY;
	}
}