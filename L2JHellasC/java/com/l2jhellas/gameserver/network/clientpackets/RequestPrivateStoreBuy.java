package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.enums.player.StoreType;
import com.l2jhellas.gameserver.model.ItemRequest;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.TradeList;
import com.l2jhellas.gameserver.model.TradeList.TradeItem;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.shield.antiflood.FloodProtectors;
import com.l2jhellas.shield.antiflood.FloodProtectors.Action;
import com.l2jhellas.util.Util;

public final class RequestPrivateStoreBuy extends L2GameClientPacket
{
	private static final String _C__79_REQUESTPRIVATESTOREBUY = "[C] 79 RequestPrivateStoreBuy";
	
	private int _storePlayerId;
	private int _count;
	private ItemRequest[] _items;
	
	@Override
	protected void readImpl()
	{
		_storePlayerId = readD();
		_count = readD();
		// count*12 is the size of a for iteration of each item
		if (_count < 0 || _count * 12 > _buf.remaining() || _count > Config.MAX_ITEM_IN_PACKET)
		{
			_count = 0;
		}
		_items = new ItemRequest[_count];
		
		for (int i = 0; i < _count; i++)
		{
			int objectId = readD();
			long count = readD();
			if (count > Integer.MAX_VALUE)
				count = Integer.MAX_VALUE;
			int price = readD();
			
			_items[i] = new ItemRequest(objectId, (int) count, price);
		}
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		
		if (player == null)
			return;
		
		if (!FloodProtectors.performAction(getClient(), Action.MANUFACTURE))
		{
			player.sendMessage("You buying items too fast.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.getActiveTradeList() != null || player.getActiveWarehouse() != null || player.getActiveEnchantItem() != null)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final L2PcInstance storePlayer = L2World.getInstance().getPlayer(_storePlayerId);
		
		if (storePlayer == null)
			return;
		
		if (!(storePlayer.getPrivateStoreType() == StoreType.SELL || storePlayer.getPrivateStoreType() == StoreType.PACKAGE_SELL))
			return;
		
		final TradeList storeList = storePlayer.getSellList();
		if (storeList == null)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Transactions are disabled for your Access Level.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// FIXME: this check should be (and most probably is) done in the TradeList mechanics
		long priceTotal = 0;
		
		for (ItemRequest ir : _items)
		{
			if (ir.getCount() > Integer.MAX_VALUE || ir.getCount() < 0)
			{
				String msgErr = "[RequestPrivateStoreBuy] player " + getClient().getActiveChar().getName() + " tried an overflow exploit, ban this player!";
				Util.handleIllegalPlayerAction(getClient().getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
				return;
			}
			TradeItem sellersItem = storeList.getItem(ir.getObjectId());
			if (sellersItem == null)
			{
				String msgErr = "[RequestPrivateStoreBuy] player " + getClient().getActiveChar().getName() + " tried to buy an item not sold in a private store (buy), ban this player!";
				Util.handleIllegalPlayerAction(getClient().getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
				return;
			}
			if (ir.getPrice() != sellersItem.getPrice())
			{
				String msgErr = "[RequestPrivateStoreBuy] player " + getClient().getActiveChar().getName() + " tried to change the seller's price in a private store (buy), ban this player!";
				Util.handleIllegalPlayerAction(getClient().getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
				return;
			}
			priceTotal += ir.getPrice() * ir.getCount();
		}
		
		// FIXME: this check should be (and most probably is) done in the TradeList mechanics
		if (priceTotal < 0 || priceTotal > Integer.MAX_VALUE)
		{
			String msgErr = "[RequestPrivateStoreBuy] player " + getClient().getActiveChar().getName() + " tried an overflow exploit, ban this player!";
			Util.handleIllegalPlayerAction(getClient().getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
			return;
		}
		
		if (player.getAdena() < priceTotal)
		{
			player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (storePlayer.getPrivateStoreType() == StoreType.PACKAGE_SELL)
		{
			if (storeList.getItemCount() > _count)
			{
				String msgErr = "[RequestPrivateStoreBuy] player " + getClient().getActiveChar().getName() + " tried to buy less items then sold by package-sell, ban this player for bot-usage!";
				Util.handleIllegalPlayerAction(getClient().getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
				return;
			}
		}
		
		if (!storeList.PrivateStoreBuy(player, _items, (int) priceTotal))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			_log.warning(RequestPrivateStoreBuy.class.getName() + ": PrivateStore buy has failed due to invalid list or request. Player: " + player.getName() + ", Private store of: " + storePlayer.getName());
			return;
		}
		
		if (storeList.getItemCount() == 0)
		{
			storePlayer.setPrivateStoreType(StoreType.NONE);
			storePlayer.broadcastUserInfo();
		}
		
		// Lease holders are currently not implemented
		// else if (_seller != null)
		// {
		// // lease shop sell
		// L2MerchantInstance seller = (L2MerchantInstance)_seller;
		// L2ItemInstance ladena = seller.getLeaseAdena();
		// for (TradeItem ti : buyerlist) {
		// L2ItemInstance li = seller.getLeaseItemByObjectId(ti.getObjectId());
		// if (li == null) {
		// if (ti.getObjectId() == ladena.getObjectId())
		// {
		// buyer.addAdena(ti.getCount());
		// ladena.setCount(ladena.getCount()-ti.getCount());
		// ladena.updateDatabase();
		// }
		// continue;
		// }
		// int cnt = li.getCount();
		// if (cnt < ti.getCount())
		// ti.setCount(cnt);
		// if (ti.getCount() <= 0)
		// continue;
		// L2ItemInstance inst = ItemTable.getInstance().createItem(li.getItemId());
		// inst.setCount(ti.getCount());
		// inst.setEnchantLevel(li.getEnchantLevel());
		// buyer.getInventory().addItem(inst);
		// li.setCount(li.getCount()-ti.getCount());
		// li.updateDatabase();
		// ladena.setCount(ladena.getCount()+ti.getCount()*ti.getOwnersPrice());
		// ladena.updateDatabase();
		// }
		// }
	}
	
	@Override
	public String getType()
	{
		return _C__79_REQUESTPRIVATESTOREBUY;
	}
}