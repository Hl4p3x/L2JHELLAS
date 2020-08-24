package com.l2jhellas.gameserver.model.actor.item.Extractable;

import java.util.List;


public class ExtractableItem
{
	private final int _itemId;
	private final List<ExtractableProductItem> _products;
	
	public ExtractableItem(int itemid, List<ExtractableProductItem> products)
	{
		_itemId = itemid;
		_products = products;
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public List<ExtractableProductItem> getProductItems()
	{
		return _products;
	}
}
