package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.enums.player.StoreType;
import com.l2jhellas.gameserver.model.ItemRequest;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.TradeList;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.shield.antiflood.FloodProtectors;
import com.l2jhellas.shield.antiflood.FloodProtectors.FloodAction;
import com.l2jhellas.util.Util;

public final class RequestPrivateStoreSell extends L2GameClientPacket
{
	// private static final String _C__96_SENDPRIVATESTOREBUYBUYLIST = "[C] 96 SendPrivateStoreBuyBuyList";
	private static final String _C__96_REQUESTPRIVATESTORESELL = "[C] 96 RequestPrivateStoreSell";
	
	private int _storePlayerId;
	private int _count;
	private int _price;
	private ItemRequest[] _items;
	
	@Override
	protected void readImpl()
	{
		_storePlayerId = readD();
		_count = readD();
		// count*20 is the size of a for iteration of each item
		if (_count < 0 || _count * 20 > _buf.remaining() || _count > Config.MAX_ITEM_IN_PACKET)
			_count = 0;
		_items = new ItemRequest[_count];
		
		long priceTotal = 0;
		for (int i = 0; i < _count; i++)
		{
			int objectId = readD();
			int itemId = readD();
			readH(); // TODO analyse this
			readH(); // TODO analyse this
			long count = readD();
			int price = readD();
			
			if (count > Integer.MAX_VALUE || count < 0)
			{
				String msgErr = "[RequestPrivateStoreSell] player " + getClient().getActiveChar().getName() + " tried an overflow exploit, ban this player!";
				Util.handleIllegalPlayerAction(getClient().getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
				_count = 0;
				_items = null;
				return;
			}
			_items[i] = new ItemRequest(objectId, itemId, (int) count, price);
			priceTotal += price * count;
		}
		
		if (priceTotal < 0 || priceTotal > Integer.MAX_VALUE)
		{
			String msgErr = "[RequestPrivateStoreSell] player " + getClient().getActiveChar().getName() + " tried an overflow exploit, ban this player!";
			Util.handleIllegalPlayerAction(getClient().getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
			_count = 0;
			_items = null;
			return;
		}
		
		_price = (int) priceTotal;
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		
		if (!FloodProtectors.performAction(getClient(), FloodAction.MANUFACTURE))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			player.sendMessage("You selling items too fast");
			return;
		}
		
		final L2PcInstance storePlayer = L2World.getInstance().getPlayer(_storePlayerId);
		
		if (storePlayer == null)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (storePlayer.getPrivateStoreType() != StoreType.BUY)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		TradeList storeList = storePlayer.getBuyList();
		if (storeList == null)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check if player didn't choose any items
		if (_items == null || _items.length == 0)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.getActiveTradeList() != null || player.getActiveEnchantItem() != null)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			player.sendMessage("You can't use this action right now!");
			return;
		}
		
		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Transactions are disable for your Access Level.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (storePlayer.getAdena() < _price)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			storePlayer.sendMessage("You have not enough adena, canceling PrivateBuy.");
			storePlayer.setPrivateStoreType(StoreType.NONE);
			storePlayer.broadcastUserInfo();
			return;
		}
		
		if (!storeList.PrivateStoreSell(player, _items, _price))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			_log.warning(RequestPrivateStoreSell.class.getName() + ": PrivateStore sell has failed due to invalid list or request. Player: " + player.getName() + ", Private store of: " + storePlayer.getName());
			return;
		}
		
		if (storeList.getItemCount() == 0)
		{
			storePlayer.setPrivateStoreType(StoreType.NONE);
			storePlayer.broadcastUserInfo();
		}
	}
	
	@Override
	public String getType()
	{
		return _C__96_REQUESTPRIVATESTORESELL;
	}
}