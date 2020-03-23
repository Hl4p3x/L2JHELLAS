package com.l2jhellas.gameserver.templates;

public class L2HelperBuff
{
	
	private final int _lowerLevel;
	
	private final int _upperLevel;
	
	private final int _skillID;
	
	private final int _skillLevel;
	
	private boolean _isMagicClass;
	
	public L2HelperBuff(StatsSet set)
	{
		_lowerLevel = set.getInteger("lowerLevel");
		_upperLevel = set.getInteger("upperLevel");
		_skillID = set.getInteger("skillID");
		_skillLevel = set.getInteger("skillLevel");
		
		if ("false".equals(set.getString("isMagicClass")))
			_isMagicClass = false;
		else
			_isMagicClass = true;
	}
	
	public int getLowerLevel()
	{
		return _lowerLevel;
	}
	
	public int getUpperLevel()
	{
		return _upperLevel;
	}
	
	public int getSkillID()
	{
		return _skillID;
	}
	
	public int getSkillLevel()
	{
		return _skillLevel;
	}
	
	public boolean isMagicClassBuff()
	{
		return _isMagicClass;
	}
}