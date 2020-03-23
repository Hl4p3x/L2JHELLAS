package com.l2jhellas.gameserver.network.serverpackets;

import java.util.List;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.model.L2TradeList;
import com.l2jhellas.gameserver.model.actor.item.L2Item;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;

public final class BuyList extends L2GameServerPacket
{
	private static final String _S__1D_BUYLIST = "[S] 11 BuyList";
	private final int _listId;
	private final L2ItemInstance[] _list;
	private final int _money;
	private double _taxRate = 0;
	
	public BuyList(L2TradeList list, int currentMoney)
	{
		_listId = list.getListId();
		List<L2ItemInstance> lst = list.getItems();
		_list = lst.toArray(new L2ItemInstance[lst.size()]);
		_money = currentMoney;
	}
	
	public BuyList(L2TradeList list, int currentMoney, double taxRate)
	{
		_listId = list.getListId();
		List<L2ItemInstance> lst = list.getItems();
		_list = lst.toArray(new L2ItemInstance[lst.size()]);
		_money = currentMoney;
		_taxRate = taxRate;
	}
	
	public BuyList(List<L2ItemInstance> lst, int listId, int currentMoney)
	{
		_listId = listId;
		_list = lst.toArray(new L2ItemInstance[lst.size()]);
		_money = currentMoney;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x11);
		writeD(_money); // current money
		writeD(_listId);
		
		writeH(_list.length);
		
		for (L2ItemInstance item : _list)
		{
			if (item.getCount() > 0 || item.getCount() == -1)
			{
				writeH(item.getItem().getType1()); // item type1
				writeD(item.getObjectId());
				writeD(item.getItemId());
				if (item.getCount() < 0)
					writeD(0x00); // max amount of items that a player can buy at a time (with this itemid)
				else
					writeD(item.getCount());
				writeH(item.getItem().getType2()); // item type2
				writeH(0x00); // ?
				
				if (item.getItem().getType1() != L2Item.TYPE1_ITEM_QUESTITEM_ADENA)
				{
					writeD(item.getItem().getBodyPart());
					// slot 0006-lr.ear 0008-neck 0030-lr.finger 0040-head 0080-?? 0100-l.hand 0200-gloves 0400-chest 0800-pants 1000-feet 2000-?? 4000-r.hand 8000-r.hand
					writeH(item.getEnchantLevel()); // enchant level
					writeH(0x00); // ?
					writeH(0x00);
				}
				else
				{
					writeD(0x00);
					// slot 0006-lr.ear 0008-neck 0030-lr.finger 0040-head 0080-?? 0100-l.hand 0200-gloves 0400-chest 0800-pants 1000-feet 2000-?? 4000-r.hand 8000-r.hand
					writeH(0x00); // enchant level
					writeH(0x00); // ?
					writeH(0x00);
				}
				
				if (item.getItemId() >= 3960 && item.getItemId() <= 4026)// Config.RATE_SIEGE_GUARDS_PRICE-//'
					writeD((int) (item.getPriceToSell() * Config.RATE_SIEGE_GUARDS_PRICE * (1 + _taxRate)));
				else
					writeD((int) (item.getPriceToSell() * (1 + _taxRate)));
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _S__1D_BUYLIST;
	}
}