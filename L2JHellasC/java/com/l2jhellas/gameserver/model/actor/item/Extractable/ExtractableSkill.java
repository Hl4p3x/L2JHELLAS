package com.l2jhellas.gameserver.model.actor.item.Extractable;

import java.util.List;

public class ExtractableSkill
{
	private final int _hash;
	private final List<ExtractableProductItem> _product;
	
	public ExtractableSkill(int hash, List<ExtractableProductItem> products)
	{
		_hash = hash;
		_product = products;
	}
	
	public int getSkillHash()
	{
		return _hash;
	}
	
	public List<ExtractableProductItem> getProductItemsArray()
	{
		return _product;
	}
}