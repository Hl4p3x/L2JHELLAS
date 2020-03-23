package com.l2jhellas.gameserver.skills.funcs;

import com.l2jhellas.gameserver.skills.Env;

public final class LambdaCalc extends Lambda
{
	
	public Func[] funcs;
	
	public LambdaCalc()
	{
		funcs = new Func[0];
	}
	
	@Override
	public double calc(Env env)
	{
		double saveValue = env.value;
		try
		{
			env.value = 0;
			for (Func f : funcs)
				f.calc(env);
			return env.value;
		}
		finally
		{
			env.value = saveValue;
		}
	}
	
	public void addFunc(Func f)
	{
		int len = funcs.length;
		Func[] tmp = new Func[len + 1];
		for (int i = 0; i < len; i++)
			tmp[i] = funcs[i];
		tmp[len] = f;
		funcs = tmp;
	}
}