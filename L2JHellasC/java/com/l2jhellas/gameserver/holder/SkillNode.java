package com.l2jhellas.gameserver.holder;

import com.l2jhellas.gameserver.templates.StatsSet;

public class SkillNode extends IntIntHolder
{
	private final int _minLvl;
	
	public SkillNode(StatsSet set)
	{
		super(set.getInteger("id"), set.getInteger("lvl"));
		
		_minLvl = set.getInteger("minLvl");
	}
	
	public int getMinLvl()
	{
		return _minLvl;
	}
}