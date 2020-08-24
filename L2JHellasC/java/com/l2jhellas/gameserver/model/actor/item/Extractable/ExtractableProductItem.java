package com.l2jhellas.gameserver.model.actor.item.Extractable;

import java.util.List;

import com.l2jhellas.gameserver.holder.IntIntHolder;

public class ExtractableProductItem
{
	private final List<IntIntHolder> _items;
	private final double _chance;
	
	public ExtractableProductItem(List<IntIntHolder> items, double chance)
	{
		_items = items;
		_chance = chance;
	}

	public List<IntIntHolder> getItems()
	{
		return _items;
	}
	
	public double getChance()
	{
		return _chance;
	}
}