package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.enums.ZoneId;
import com.l2jhellas.gameserver.enums.player.StoreType;
import com.l2jhellas.gameserver.model.TradeList;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.Inventory;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.PrivateStoreManageListSell;
import com.l2jhellas.gameserver.network.serverpackets.PrivateStoreMsgSell;
import com.l2jhellas.gameserver.taskmanager.AttackStanceTaskManager;

public class SetPrivateStoreListSell extends L2GameClientPacket
{
	private static final String _C__74_SETPRIVATESTORELISTSELL = "[C] 74 SetPrivateStoreListSell";
	
	private boolean _packageSale;
	private Item[] _items = null;
	private int _count;

	
	@Override
	protected void readImpl()
	{
		_packageSale = (readD() == 1);
		_count = readD();
		if (_count < 1 || _count * 12 > _buf.remaining() || _count > Config.MAX_ITEM_IN_PACKET)
			return;

		_items = new Item[_count];

		for (int i = 0; i < _count; i++)
		{
			int objectId = readD();
			int cnt = readD();
			int price = readD();
			
			if ((objectId < 1) || (cnt < 1) || cnt > Integer.MAX_VALUE || (price < 0))
			{
				_items = null;
				return;
			}
			_items[i] = new Item(objectId, cnt, price);
		}
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		
		final TradeList tradeList = player.getSellList();
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
		
		if (AttackStanceTaskManager.getInstance().isInAttackStance(player) || (player.isCastingNow() || player.isInDuel()))
		{
			player.sendPacket(SystemMessageId.CANT_OPERATE_PRIVATE_STORE_DURING_COMBAT);
			player.sendPacket(new PrivateStoreManageListSell(player, _packageSale));
			return;
		}
		
		if (player.isInsideZone(ZoneId.NO_STORE))
		{
			player.sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);
			player.sendPacket(new PrivateStoreManageListSell(player, _packageSale));
			return;
		}
		
		if (player.isSitting() && !player.isInStoreMode())
		    return;
		
		if (player.isAlikeDead() || player.isMounted() || player.isProcessingRequest())
			 return;
		
		
		// Check maximum number of allowed slots for pvt shops
		if (_items.length > player.getPrivateSellStoreLimit())
		{
			player.sendPacket(new PrivateStoreManageListSell(player, _packageSale));
			player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}

		tradeList.setPackaged(_packageSale);
		
		long totalCost = player.getAdena();
		for (Item i : _items)
		{
			if (!i.addToTradeList(tradeList))
			{
				player.sendPacket(SystemMessageId.EXCEEDED_THE_MAXIMUM);
				player.sendPacket(new PrivateStoreManageListSell(player, _packageSale));
				return;
			}
			
			totalCost += i.getPrice();
			if (!validateCount(Inventory.ADENA_ID, totalCost))
			{
				player.sendPacket(SystemMessageId.EXCEEDED_THE_MAXIMUM);
				player.sendPacket(new PrivateStoreManageListSell(player, _packageSale));
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

		player.sitDown();
		player.setPrivateStoreType((_packageSale) ? StoreType.PACKAGE_SELL : StoreType.SELL);
		player.broadcastUserInfo();
		player.broadcastPacket(new PrivateStoreMsgSell(player));
	}
	
	private static class Item
	{
		private final int _objectId;
		private final long _count;
		private final long _price;
		
		public Item(int objectId, long count, long price)
		{
			_objectId = objectId;
			_count = count;
			_price = price;
		}
		
		public boolean addToTradeList(TradeList list)
		{
			if (!validateCount(Inventory.ADENA_ID, _price * _count))
				return false;
			
			list.addItem(_objectId, (int)_count, (int)_price);
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
	@Override
	public String getType()
	{
		return _C__74_SETPRIVATESTORELISTSELL;
	}
}