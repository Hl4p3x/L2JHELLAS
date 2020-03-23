package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SendTradeDone;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public final class AnswerTradeRequest extends L2GameClientPacket
{
	private static final String _C__40_ANSWERTRADEREQUEST = "[C] 40 AnswerTradeRequest";
	
	private int _response;
	
	@Override
	protected void readImpl()
	{
		_response = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		
		if (player == null)
			return;
		
		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		final L2PcInstance partner = player.getActiveRequester();
		
		if (partner == null || L2World.getInstance().getPlayer(partner.getObjectId()) == null)
		{
			// Trade partner not found, cancel trade
			player.sendPacket(new SendTradeDone(0));
			player.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			player.setActiveRequester(null);
			return;
		}
		
		if (_response == 1 && !partner.isRequestExpired())
			player.startTrade(partner);
		else
			partner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DENIED_TRADE_REQUEST).addPcName(player));
		
		// Clears requesting status
		player.setActiveRequester(null);
		partner.onTransactionResponse();
	}
	
	@Override
	public String getType()
	{
		return _C__40_ANSWERTRADEREQUEST;
	}
}