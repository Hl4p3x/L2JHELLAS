package com.l2jhellas.gameserver.model;

public final class TradeItem
{
	private int _objectId;
	private int _itemId;
	private int _price;
	private int _storePrice;
	private int _count;
	private int _enchantLevel;
	
	public TradeItem()
	{
	}
	
	public void setObjectId(int id)
	{
		_objectId = id;
	}
	
	public int getObjectId()
	{
		return _objectId;
	}
	
	public void setItemId(int id)
	{
		_itemId = id;
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public void setOwnersPrice(int price)
	{
		_price = price;
	}
	
	public int getOwnersPrice()
	{
		return _price;
	}
	
	public void setstorePrice(int price)
	{
		_storePrice = price;
	}
	
	public int getStorePrice()
	{
		return _storePrice;
	}
	
	public void setCount(int count)
	{
		_count = count;
	}
	
	public int getCount()
	{
		return _count;
	}
	
	public void setEnchantLevel(int enchant)
	{
		_enchantLevel = enchant;
	}
	
	public int getEnchantLevel()
	{
		return _enchantLevel;
	}
}