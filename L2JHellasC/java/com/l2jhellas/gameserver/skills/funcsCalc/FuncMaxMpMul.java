package com.l2jhellas.gameserver.skills.funcsCalc;

import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.Formulas;
import com.l2jhellas.gameserver.skills.Stats;
import com.l2jhellas.gameserver.skills.funcs.Func;

public class FuncMaxMpMul extends Func
{
	static final FuncMaxMpMul _fmmm_instance = new FuncMaxMpMul();
	
	public static Func getInstance()
	{
		return _fmmm_instance;
	}
	
	private FuncMaxMpMul()
	{
		super(Stats.MAX_MP, 0x20, null);
	}
	
	@Override
	public void calc(Env env)
	{
		L2Character p = env.player;
		env.value *= Formulas.MENbonus[p.getMEN()];
	}
}
