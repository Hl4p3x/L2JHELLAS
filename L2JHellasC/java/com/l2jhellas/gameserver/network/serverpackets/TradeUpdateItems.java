package com.l2jhellas.gameserver.network.serverpackets;

import java.util.Collection;

import com.l2jhellas.gameserver.model.TradeList;
import com.l2jhellas.gameserver.model.TradeList.TradeItem;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;

public class TradeUpdateItems extends L2GameServerPacket
{
	private final Collection<L2ItemInstance> _items;
	private final TradeItem[] _currentTrade;
	
	public TradeUpdateItems(TradeList trade, L2PcInstance player)
	{
		_items = player.getInventory().getItems();
		_currentTrade = trade.getItems();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x74);
		
		writeH(_currentTrade.length);
		
		for (TradeItem item : _currentTrade)
		{
			int availableCount = getItemCount(item.getObjectId()) - item.getCount();
			boolean stackable = item.getItem().isStackable();
			
			if (availableCount == 0)
			{
				availableCount = 1;
				stackable = false;
			}
			
			writeH(stackable ? 3 : 2);
			writeH(item.getItem().getType1());
			writeD(item.getObjectId());
			writeD(item.getItem().getItemId());
			writeD(availableCount);
			writeH(item.getItem().getType2());
			writeH(0x00);
			writeD(item.getItem().getBodyPart());
			writeH(item.getEnchant());
			writeH(0x00);
			writeH(0x00);
		}
	}
	
	private int getItemCount(int objectId)
	{
		for (L2ItemInstance item : _items)
			if (item.getObjectId() == objectId)
				return item.getCount();
			
		return 0;
	}
}