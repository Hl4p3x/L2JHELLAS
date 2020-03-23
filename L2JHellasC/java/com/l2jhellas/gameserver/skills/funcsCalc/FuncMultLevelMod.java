package com.l2jhellas.gameserver.skills.funcsCalc;

import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.Stats;
import com.l2jhellas.gameserver.skills.funcs.Func;

public class FuncMultLevelMod extends Func
{
	static final FuncMultLevelMod[] _instancies = new FuncMultLevelMod[Stats.NUM_STATS];
	
	static Func getInstance(Stats stat)
	{
		int pos = stat.ordinal();
		if (_instancies[pos] == null)
			_instancies[pos] = new FuncMultLevelMod(stat);
		return _instancies[pos];
	}
	
	private FuncMultLevelMod(Stats pStat)
	{
		super(pStat, 0x20, null);
	}
	
	@Override
	public void calc(Env env)
	{
		env.value *= env.player.getLevelMod();
	}
}