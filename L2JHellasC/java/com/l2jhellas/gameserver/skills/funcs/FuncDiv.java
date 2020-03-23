package com.l2jhellas.gameserver.skills.funcs;

import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.Stats;

public class FuncDiv extends Func
{
	private final Lambda _lambda;
	
	public FuncDiv(Stats pStat, int pOrder, Object owner, Lambda lambda)
	{
		super(pStat, pOrder, owner);
		_lambda = lambda;
	}
	
	@Override
	public void calc(Env env)
	{
		if (cond == null || cond.test(env))
			env.value /= _lambda.calc(env);
	}
}