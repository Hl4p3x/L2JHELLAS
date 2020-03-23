package com.l2jhellas.gameserver.datatables.xml;

public final class LevelingInfo
{
	public enum AbsorbCrystalType
	{
		LAST_HIT,
		FULL_PARTY,
		PARTY_ONE_RANDOM
	}
	
	private final AbsorbCrystalType _absorbCrystalType;
	private final boolean _skillRequired;
	private final int _chanceStage;
	private final int _chanceBreak;
	private final int[] _levelList;
	
	public LevelingInfo(AbsorbCrystalType absorbCrystalType, boolean skillRequired, int chanceStage, int chanceBreak, int[] levelList)
	{
		_absorbCrystalType = absorbCrystalType;
		_skillRequired = skillRequired;
		_chanceStage = chanceStage;
		_chanceBreak = chanceBreak;
		_levelList = levelList;
	}
	
	public AbsorbCrystalType getAbsorbCrystalType()
	{
		return _absorbCrystalType;
	}
	
	public boolean skillRequired()
	{
		return _skillRequired;
	}
	
	public int getChanceStage()
	{
		return _chanceStage;
	}
	
	public int getChanceBreak()
	{
		return _chanceBreak;
	}
	
	public int[] getLevelList()
	{
		return _levelList;
	}
	
	public boolean isInLevelList(int level)
	{
		for (int lvl : _levelList)
		{
			if (lvl == level)
				return true;
		}
		return false;
	}
}