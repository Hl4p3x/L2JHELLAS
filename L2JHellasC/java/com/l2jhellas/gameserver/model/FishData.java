package com.l2jhellas.gameserver.model;

import com.l2jhellas.gameserver.templates.StatsSet;

public class FishData implements Cloneable
{
	private final int _fishId;
	private final int _itemId;
	private final String _itemName;
	private int _fishGroup;
	private final int _fishLevel;
	private final double _fishBiteRate;
	private final double _fishGuts;
	private final int _fishHp;
	private final int _fishMaxLength;
	private final double _fishLengthRate;
	private final double _hpRegen;
	private final int _startCombatTime;
	private final int _combatDuration;
	private final int _gutsCheckTime;
	private final double _gutsCheckProbability;
	private final double _cheatingProb;
	private final int _fishGrade;
	
	public FishData(StatsSet set)
	{
		_fishId = set.getInteger("fishId");
		_itemId = set.getInteger("itemId");
		_itemName = set.getString("itemName");
		_fishGroup = getGroupId(set.getString("fishGroup"));
		_fishLevel = set.getInteger("fishLevel");
		_fishBiteRate = set.getDouble("fishBiteRate");
		_fishGuts = set.getDouble("fishGuts");
		_fishHp = set.getInteger("fishHp");
		_fishMaxLength = set.getInteger("fishMaxLength");
		_fishLengthRate = set.getDouble("fishLengthRate");
		_hpRegen = set.getDouble("hpRegen");
		_startCombatTime = set.getInteger("startCombatTime");
		_combatDuration = set.getInteger("combatDuration");
		_gutsCheckTime = set.getInteger("gutsCheckTime");
		_gutsCheckProbability = set.getDouble("gutsCheckProbability");
		_cheatingProb = set.getDouble("cheatingProb");
		_fishGrade = getGradeId(set.getString("fishGrade"));
	}
	
	@Override
	public FishData clone()
	{
		try
		{
			return (FishData) super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			return null;
		}
	}
	
	public int getFishId()
	{
		return _fishId;
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public String getItemName()
	{
		return _itemName;
	}
	
	public int getFishGroup()
	{
		return _fishGroup;
	}
	
	public int getFishLevel()
	{
		return _fishLevel;
	}
	
	public double getFishBiteRate()
	{
		return _fishBiteRate;
	}
	
	public double getFishGuts()
	{
		return _fishGuts;
	}
	
	public int getFishHp()
	{
		return _fishHp;
	}
	
	public int getFishMaxLength()
	{
		return _fishMaxLength;
	}
	
	public double getFishLengthRate()
	{
		return _fishLengthRate;
	}
	
	public double getHpRegen()
	{
		return _hpRegen;
	}
	
	public int getStartCombatTime()
	{
		return _startCombatTime;
	}
	
	public int getCombatDuration()
	{
		return _combatDuration;
	}
	
	public int getGutsCheckTime()
	{
		return _gutsCheckTime;
	}
	
	public double getGutsCheckProbability()
	{
		return _gutsCheckProbability;
	}
	
	public double getCheatingProb()
	{
		return _cheatingProb;
	}
	
	public int getFishGrade()
	{
		return _fishGrade;
	}
	
	public void setFishGroup(int fg)
	{
		_fishGroup = fg;
	}
	
	private static int getGroupId(String name)
	{
		switch (name)
		{
			case "swift":
				return 1;
			case "ugly":
				return 2;
			case "fish_box":
				return 3;
			case "easy_wide":
				return 4;
			case "easy_swift":
				return 5;
			case "easy_ugly":
				return 6;
			case "hard_wide":
				return 7;
			case "hard_swift":
				return 8;
			case "hard_ugly":
				return 9;
			case "hs_fish":
				return 10;
			case "wide":
			default:
				return 0;
		}
	}
	
	private static int getGradeId(String name)
	{
		switch (name)
		{
			case "fish_easy":
				return 0;
			case "fish_hard":
				return 2;
			case "fish_normal":
			default:
				return 1;
		}
	}
}