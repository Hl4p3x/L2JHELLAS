package com.l2jhellas.gameserver.holder;

import com.l2jhellas.gameserver.templates.StatsSet;

public class GeneralSkillNode extends SkillNode
{
	private final int _cost;
	
	public GeneralSkillNode(StatsSet set)
	{
		super(set);
		
		_cost = set.getInteger("cost");
	}
	
	public int getCost()
	{
		return _cost;
	}

	public int getCorrectedCost()
	{
		return Math.max(0, _cost);
	}
}