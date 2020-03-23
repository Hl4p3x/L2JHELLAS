package com.l2jhellas.gameserver.skills.funcs;

import com.l2jhellas.gameserver.skills.Env;

public final class LambdaConst extends Lambda
{
	private final double _value;
	
	public LambdaConst(double value)
	{
		_value = value;
	}
	
	@Override
	public double calc(Env env)
	{
		return _value;
	}
}