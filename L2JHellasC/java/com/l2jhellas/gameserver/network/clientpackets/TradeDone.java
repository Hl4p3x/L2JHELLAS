package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.TradeList;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;

public final class TradeDone extends L2GameClientPacket
{
	private static final String _C__17_TRADEDONE = "[C] 17 TradeDone";
	
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
		
		final TradeList trade = player.getActiveTradeList();
		
		if (trade == null)
			return;
		
		if (trade.isLocked())
			return;
		
		if (_response != 1)
		{
			player.cancelActiveTrade();
			return;
		}
		
		final L2PcInstance owner = trade.getOwner();
		
		if (owner == null || !owner.equals(player))
			return;
		
		final L2PcInstance partner = trade.getPartner();
		
		if (partner == null || L2World.getInstance().getPlayer(partner.getObjectId()) == null)
		{
			player.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			player.cancelActiveTrade();
			return;
		}
		
		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			player.cancelActiveTrade();
			return;
		}
		
		if (owner.getActiveEnchantItem() != null)
		{
			owner.cancellEnchant();
		}
		
		if (partner.getActiveEnchantItem() != null)
		{
			partner.cancellEnchant();
		}
		
		trade.confirm();
	}
	
	@Override
	public String getType()
	{
		return _C__17_TRADEDONE;
	}
}