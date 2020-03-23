package com.l2jhellas.gameserver.network.serverpackets;

import com.l2jhellas.gameserver.datatables.xml.RecipeData;
import com.l2jhellas.gameserver.model.L2RecipeList;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class RecipeItemMakeInfo extends L2GameServerPacket
{
	private static final String _S__D7_RECIPEITEMMAKEINFO = "[S] D7 RecipeItemMakeInfo";
	
	private final int _id;
	private final L2PcInstance _activeChar;
	private final boolean _success;
	
	public RecipeItemMakeInfo(int id, L2PcInstance player, boolean success)
	{
		_id = id;
		_activeChar = player;
		_success = success;
	}
	
	public RecipeItemMakeInfo(int id, L2PcInstance player)
	{
		_id = id;
		_activeChar = player;
		_success = true;
	}
	
	@Override
	protected final void writeImpl()
	{
		L2RecipeList recipe = RecipeData.getInstance().getRecipeById(_id);
		
		if (recipe != null)
		{
			writeC(0xD7);
			
			writeD(_id);
			writeD(recipe.isDwarvenRecipe() ? 0 : 1); // 0 = Dwarven - 1 = Common
			writeD((int) _activeChar.getCurrentMp());
			writeD(_activeChar.getMaxMp());
			writeD(_success ? 1 : 0); // item creation success/failed
		}
	}
	
	@Override
	public String getType()
	{
		return _S__D7_RECIPEITEMMAKEINFO;
	}
}