package com.l2jhellas.gameserver.skills.funcs;

import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.Stats;
import com.l2jhellas.gameserver.skills.conditions.Condition;

public abstract class Func
{
	
	public final Stats stat;
	
	public final int order;
	
	public final Object funcOwner;
	
	public Condition cond;
	
	public Func(Stats pStat, int pOrder, Object owner)
	{
		stat = pStat;
		order = pOrder;
		funcOwner = owner;
	}
	
	public void setCondition(Condition pCond)
	{
		cond = pCond;
	}
	
	public abstract void calc(Env env);
}