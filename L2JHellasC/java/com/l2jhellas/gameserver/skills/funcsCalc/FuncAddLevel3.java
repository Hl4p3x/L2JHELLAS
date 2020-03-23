package com.l2jhellas.gameserver.skills.funcsCalc;

import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.Stats;
import com.l2jhellas.gameserver.skills.funcs.Func;

public class FuncAddLevel3 extends Func
{
	static final FuncAddLevel3[] _instancies = new FuncAddLevel3[Stats.NUM_STATS];
	
	static Func getInstance(Stats stat)
	{
		int pos = stat.ordinal();
		if (_instancies[pos] == null)
			_instancies[pos] = new FuncAddLevel3(stat);
		return _instancies[pos];
	}
	
	private FuncAddLevel3(Stats pStat)
	{
		super(pStat, 0x10, null);
	}
	
	@Override
	public void calc(Env env)
	{
		env.value += env.player.getLevel() / 3.0;
	}
}