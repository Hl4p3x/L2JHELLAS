package com.l2jhellas.gameserver.network.serverpackets;

import java.util.Collection;

import com.l2jhellas.gameserver.model.L2ManufactureItem;
import com.l2jhellas.gameserver.model.L2ManufactureList;
import com.l2jhellas.gameserver.model.L2RecipeList;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class RecipeShopManageList extends L2GameServerPacket
{
	private static final String _S__D8_RecipeShopManageList = "[S] d8 RecipeShopManageList";
	private final L2PcInstance _seller;
	private final boolean _isDwarven;
	private final Collection<L2RecipeList> _recipes;

	
	public RecipeShopManageList(L2PcInstance seller, boolean isDwarven)
	{
		_seller = seller;
		_isDwarven = isDwarven;
		_recipes = (isDwarven && seller.hasDwarvenCraft()) ? seller.getDwarvenRecipeBook() : seller.getCommonRecipeBook();

		// clean previous recipes
		if (_seller.getCreateList() != null)
		{
			L2ManufactureList list = _seller.getCreateList();
			for (L2ManufactureItem item : list.getList())
			{
				if (item.isDwarven() != _isDwarven)
					list.getList().remove(item);
			}
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xd8);
		writeD(_seller.getObjectId());
		writeD(_seller.getAdena());
		writeD(_isDwarven ? 0x00 : 0x01);
		
		if (_recipes == null)
			writeD(0);
		else
		{
			writeD(_recipes.size());// number of items in recipe book
			
			int i = 0;
			for (L2RecipeList rp : _recipes)
			{
				writeD(rp.getId());
				writeD(++i);
			}
		}
		
		if (_seller.getCreateList() == null)
			writeD(0);
		else
		{
			L2ManufactureList list = _seller.getCreateList();
			writeD(list.size());
			
			for (L2ManufactureItem item : list.getList())
			{
				writeD(item.getRecipeId());
				writeD(0x00);
				writeD(item.getCost());
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _S__D8_RecipeShopManageList;
	}
}