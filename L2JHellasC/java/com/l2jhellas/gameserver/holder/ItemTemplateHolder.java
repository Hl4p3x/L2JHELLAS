package com.l2jhellas.gameserver.holder;

import com.l2jhellas.gameserver.templates.StatsSet;

public class ItemTemplateHolder extends IntIntHolder
{
	private final boolean _isEquipped;
	
	public ItemTemplateHolder(StatsSet set)
	{
		super(set.getInteger("id"), set.getInteger("count"));
		
		_isEquipped = set.getBool("isEquipped", true);
	}
	
	public final boolean isEquipped()
	{
		return _isEquipped;
	}
}