package com.l2jhellas.gameserver.model;

public class L2RecipeInstance
{
	
	private final int _itemId;
	
	private final int _quantity;
	
	public L2RecipeInstance(int itemId, int quantity)
	{
		_itemId = itemId;
		_quantity = quantity;
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public int getQuantity()
	{
		return _quantity;
	}
}