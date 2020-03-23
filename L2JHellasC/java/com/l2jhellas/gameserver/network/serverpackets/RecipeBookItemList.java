package com.l2jhellas.gameserver.network.serverpackets;

import java.util.Collection;

import com.l2jhellas.gameserver.model.L2RecipeList;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class RecipeBookItemList extends L2GameServerPacket
{
	private static final String _S__D6_RECIPEBOOKITEMLIST = "[S] D6 RecipeBookItemList";
	private Collection<L2RecipeList> _recipes;
	private final boolean _isDwarvenCraft;
	private final int _maxMp;
	
	public RecipeBookItemList(L2PcInstance player,boolean isDwarvenCraft)
	{
		_recipes = (isDwarvenCraft) ? player.getDwarvenRecipeBook() : player.getCommonRecipeBook();
		_isDwarvenCraft = isDwarvenCraft;
		_maxMp = player.getMaxMp();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xD6);
		
		writeD(_isDwarvenCraft ? 0x00 : 0x01); // 0 = Dwarven - 1 = Common
		writeD(_maxMp);
		
		if (_recipes == null)
			writeD(0);
		else
		{
			writeD(_recipes.size());// number of items in recipe book
			
			int i = 0;
			for (L2RecipeList recipe : _recipes)
			{		
				writeD(recipe.getId());
				writeD(++i);
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _S__D6_RECIPEBOOKITEMLIST;
	}
}