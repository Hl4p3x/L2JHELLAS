package com.l2jhellas.gameserver.skills.funcs;

import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.util.Rnd;

public final class LambdaRnd extends Lambda
{
	private final Lambda _max;
	private final boolean _linear;
	
	public LambdaRnd(Lambda max, boolean linear)
	{
		_max = max;
		_linear = linear;
	}
	
	@Override
	public double calc(Env env)
	{
		if (_linear)
			return _max.calc(env) * Rnd.nextDouble();
		return _max.calc(env) * Rnd.nextGaussian();
	}
}