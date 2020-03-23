package com.l2jhellas.gameserver.holder;

public class ItemHolder
{
	private final int _id;
	private final int _objectId;
	private final int _count;
	
	public ItemHolder(int id, int count)
	{
		_id = id;
		_objectId = -1;
		_count = count;
	}
	
	public ItemHolder(int id, int objectId, int count)
	{
		_id = id;
		_objectId = objectId;
		_count = count;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public int getObjectId()
	{
		return _objectId;
	}
	
	public int getCount()
	{
		return _count;
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + ": Id: " + _id + " Count: " + _count;
	}
}