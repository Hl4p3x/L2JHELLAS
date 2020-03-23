package com.l2jhellas.gameserver.skills.funcsCalc;

import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.Stats;
import com.l2jhellas.gameserver.skills.conditions.ConditionPlayerState;
import com.l2jhellas.gameserver.skills.conditions.ConditionPlayerState.CheckPlayerState;
import com.l2jhellas.gameserver.skills.funcs.Func;

public class FuncMultRegenResting extends Func
{
	static final FuncMultRegenResting[] _instancies = new FuncMultRegenResting[Stats.NUM_STATS];
	
	static Func getInstance(Stats stat)
	{
		int pos = stat.ordinal();
		
		if (_instancies[pos] == null)
			_instancies[pos] = new FuncMultRegenResting(stat);
		
		return _instancies[pos];
	}
	
	private FuncMultRegenResting(Stats pStat)
	{
		super(pStat, 0x20, null);
		setCondition(new ConditionPlayerState(CheckPlayerState.RESTING, true));
	}
	
	@Override
	public void calc(Env env)
	{
		if (!cond.test(env))
			return;
		
		env.value *= 1.45;
	}
}