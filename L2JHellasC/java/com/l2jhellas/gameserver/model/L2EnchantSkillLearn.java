package com.l2jhellas.gameserver.model;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public final class L2EnchantSkillLearn
{
	// these two build the primary key
	private final int _id;
	private final int _level;
	
	// not needed, just for easier debug
	private final String _name;
	
	private final int _spCost;
	private final int _baseLvl;
	private final int _minSkillLevel;
	private final int _exp;
	private final byte _rate76;
	private final byte _rate77;
	private final byte _rate78;
	
	public L2EnchantSkillLearn(int id, int lvl, int minSkillLvl, int baseLvl, String name, int cost, int exp, byte rate76, byte rate77, byte rate78)
	{
		_id = id;
		_level = lvl;
		_baseLvl = baseLvl;
		_minSkillLevel = minSkillLvl;
		_name = name.intern();
		_spCost = cost;
		_exp = exp;
		_rate76 = rate76;
		_rate77 = rate77;
		_rate78 = rate78;
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
	
	public int getMinSkillLevel()
	{
		return _minSkillLevel;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public int getSpCost()
	{
		return _spCost;
	}
	
	public int getExp()
	{
		return _exp;
	}
	
	public byte getRate(L2PcInstance ply)
	{
		byte result;
		switch (ply.getLevel())
		{
			case 76:
				result = _rate76;
				break;
			case 77:
				result = _rate77;
				break;
			case 78:
				result = _rate78;
				break;
			default:
				result = _rate78;
				break;
		}
		return result;
	}
}