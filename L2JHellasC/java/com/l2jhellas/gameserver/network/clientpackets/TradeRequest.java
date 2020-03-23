package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SendTradeRequest;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public final class TradeRequest extends L2GameClientPacket
{
	private static final String TRADEREQUEST__C__15 = "[C] 15 TradeRequest";
	
	private int _objectId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		
		if (player == null || _objectId == 0)
			return;
		
		final L2PcInstance target = L2World.getInstance().getPlayer(_objectId);
		
		if (player.canRequestTrade(target))
		{
			player.onTransactionRequest(target);
			target.sendPacket(new SendTradeRequest(player.getObjectId()));
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.REQUEST_S1_FOR_TRADE).addPcName(target));
		}
	}
	
	@Override
	public String getType()
	{
		return TRADEREQUEST__C__15;
	}
}