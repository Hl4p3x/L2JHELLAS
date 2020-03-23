package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.model.L2ManufactureItem;
import com.l2jhellas.gameserver.model.L2ManufactureList;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class RecipeShopSellList extends L2GameServerPacket
{
	private static final String _S__D9_RecipeShopSellList = "[S] d9 RecipeShopSellList";
	private final L2PcInstance _buyer, _manufacturer;
	
	public RecipeShopSellList(L2PcInstance buyer, L2PcInstance manufacturer)
	{
		_buyer = buyer;
		_manufacturer = manufacturer;
	}
	
	@Override
	protected final void writeImpl()
	{
		L2ManufactureList createList = _manufacturer.getCreateList();
		
		if (createList != null)
		{
			// dddd d(ddd)
			writeC(0xd9);
			writeD(_manufacturer.getObjectId());
			writeD((int) _manufacturer.getCurrentMp());// Creator's MP
			writeD(_manufacturer.getMaxMp());// Creator's MP
			writeD(_buyer.getAdena());// Buyer Adena
			
			int count = createList.size();
			writeD(count);
			L2ManufactureItem temp;
			
			for (int i = 0; i < count; i++)
			{
				temp = createList.getList().get(i);
				writeD(temp.getRecipeId());
				writeD(0x00); // unknown
				writeD(temp.getCost());
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _S__D9_RecipeShopSellList;
	}
}