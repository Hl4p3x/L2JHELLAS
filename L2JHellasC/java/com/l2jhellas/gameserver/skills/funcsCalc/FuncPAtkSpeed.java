package com.l2jhellas.gameserver.skills.funcsCalc;

import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.Formulas;
import com.l2jhellas.gameserver.skills.Stats;
import com.l2jhellas.gameserver.skills.funcs.Func;

public class FuncPAtkSpeed extends Func
{
	static final FuncPAtkSpeed _fas_instance = new FuncPAtkSpeed();
	
	public static Func getInstance()
	{
		return _fas_instance;
	}
	
	private FuncPAtkSpeed()
	{
		super(Stats.POWER_ATTACK_SPEED, 0x20, null);
	}
	
	@Override
	public void calc(Env env)
	{
		L2Character p = env.player;
		env.value *= Formulas.DEXbonus[p.getDEX()];
	}
}