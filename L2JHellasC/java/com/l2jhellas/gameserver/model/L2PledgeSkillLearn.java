package com.l2jhellas.gameserver.model;

public final class L2PledgeSkillLearn
{
	// these two build the primary key
	private final int _id;
	private final int _level;
	
	// not needed, just for easier debug
	private final String _name;
	
	private final int _repCost;
	private final int _baseLvl;
	private final int _itemId;
	
	public L2PledgeSkillLearn(int id, int lvl, int baseLvl, String name, int cost, int itemId)
	{
		_id = id;
		_level = lvl;
		_baseLvl = baseLvl;
		_name = name.intern();
		_repCost = cost;
		_itemId = itemId;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public int getBaseLevel()
	{
		return _baseLvl;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public int getRepCost()
	{
		return _repCost;
	}
	
	public int getItemId()
	{
		return _itemId;
	}
}