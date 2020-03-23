package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.enums.ZoneId;
import com.l2jhellas.gameserver.enums.player.StoreType;
import com.l2jhellas.gameserver.model.TradeList;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.Inventory;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.PrivateStoreManageListBuy;
import com.l2jhellas.gameserver.network.serverpackets.PrivateStoreMsgBuy;
import com.l2jhellas.gameserver.taskmanager.AttackStanceTaskManager;

public final class SetPrivateStoreListBuy extends L2GameClientPacket
{
	private static final String _C__91_SETPRIVATESTORELISTBUY = "[C] 91 SetPrivateStoreListBuy";
	
	private Item[] _items = null;
	private int _count;
	
	@Override
	protected void readImpl()
	{
		_count = readD();

		if (_count < 1 || _count * 12 > _buf.remaining() || _count > Config.MAX_ITEM_IN_PACKET)
			return;
		_items = new Item[_count];
		for (int i = 0; i < _count; i++)
		{
			int itemId = readD();
			int enchant = readH();
			readH(); // TODO analyse this
			int cnt = readD();
			int price = readD();
			
			if (itemId < 1 || cnt < 1 || cnt > Integer.MAX_VALUE || price < 0 || enchant < 0 || enchant > 65535)
			{
				_items = null;
				return;
			}
			_items[i] = new Item(itemId, cnt, price, enchant);
		}
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;	

		final TradeList tradeList = player.getBuyList();
		tradeList.clear();
		
		if (_items == null)
		{
			player.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT);
			player.setPrivateStoreType(StoreType.NONE);
			player.broadcastUserInfo();
			return;
		}
		
		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Transactions are disabled for your Access Level.");
			return;
		}
		
		if (player.isSitting() && !player.isInStoreMode())
		    return;
		
		if (player.isAlikeDead() || player.isMounted() || player.isProcessingRequest())
			 return;

		if (player.isInDuel()  || player.isCastingNow() || AttackStanceTaskManager.getInstance().isInAttackStance(player) || player.isInOlympiadMode())
		{
			player.sendPacket(SystemMessageId.CANT_OPERATE_PRIVATE_STORE_DURING_COMBAT);
			player.sendPacket(new PrivateStoreManageListBuy(player));
			return;
		}
		
		if (player.isInsideZone(ZoneId.NO_STORE))
		{
			player.sendPacket(new PrivateStoreManageListBuy(player));
			player.sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check maximum number of allowed slots for pvt shops
		if (_items.length > player.getPrivateBuyStoreLimit())
		{
			player.sendPacket(new PrivateStoreManageListBuy(player));
			player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}

		long totalCost = 0;
		for (Item i : _items)
		{
			if (!i.addToTradeList(tradeList))
			{
				player.sendPacket(SystemMessageId.EXCEEDED_THE_MAXIMUM);
				player.sendPacket(new PrivateStoreManageListBuy(player));
				return;
			}
			
			totalCost += i.getPrice();

			if (!validateCount(Inventory.ADENA_ID, totalCost))
			{
				player.sendPacket(SystemMessageId.EXCEEDED_THE_MAXIMUM);
				player.sendPacket(new PrivateStoreManageListBuy(player));
				return;
			}
		}
		
		if (_count <= 0)
		{
			player.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT);
			player.setPrivateStoreType(StoreType.NONE);
			player.broadcastUserInfo();
			return;
		}
	
		// Check for available funds
		if (totalCost > player.getAdena())
		{
			player.sendPacket(SystemMessageId.THE_PURCHASE_PRICE_IS_HIGHER_THAN_MONEY);
			player.sendPacket(new PrivateStoreManageListBuy(player));
			return;
		}
		
		player.sitDown();
		player.setPrivateStoreType(StoreType.BUY);
		player.broadcastUserInfo();
		player.broadcastPacket(new PrivateStoreMsgBuy(player));
	}
	
	@Override
	public String getType()
	{
		return _C__91_SETPRIVATESTORELISTBUY;
	}
	
	private static class Item
	{
		private final int _objectId;
		private final long _count;
		private final long _price;
		private final int _enchant;
		
		public Item(int objectId, long count, long price,int enchant)
		{
			_objectId = objectId;
			_count = count;
			_price = price;
			_enchant = enchant;
		}
		
		public boolean addToTradeList(TradeList list)
		{
			if (!validateCount(Inventory.ADENA_ID, _price * _count))
				return false;
			
			list.addItemByItemId(_objectId, (int)_count, (int)_price,_enchant);
			return true;
		}
		
		public long getPrice()
		{
			return _count * _price;
		}
		
	}
	
	protected static boolean validateCount(int itemId, long count)
	{
		return (count > 0) && (count <= getMaximumAllowedCount(itemId));
	}
	
	protected static long getMaximumAllowedCount(int itemId)
	{
		return itemId == Inventory.ADENA_ID ? 2000000001 : Integer.MAX_VALUE;
	}
}