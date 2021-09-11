package com.l2jhellas.gameserver.holder;

import com.l2jhellas.gameserver.templates.StatsSet;

public final class ClanSkillNode extends GeneralSkillNode
{
	private final int _itemId;
	
	public ClanSkillNode(StatsSet set)
	{
		super(set);
		
		_itemId = set.getInteger("itemId");
	}
	
	public int getItemId()
	{
		return _itemId;
	}
}