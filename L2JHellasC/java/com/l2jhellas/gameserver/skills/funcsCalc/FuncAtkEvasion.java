package com.l2jhellas.gameserver.skills.funcsCalc;

import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.Stats;
import com.l2jhellas.gameserver.skills.funcs.Func;

public class FuncAtkEvasion extends Func
{
	static final FuncAtkEvasion _fae_instance = new FuncAtkEvasion();
	
	public static Func getInstance()
	{
		return _fae_instance;
	}
	
	private FuncAtkEvasion()
	{
		super(Stats.EVASION_RATE, 0x10, null);
	}
	
	@Override
	public void calc(Env env)
	{
		L2Character p = env.player;
		// [Square(DEX)]*6 + lvl;
		env.value += Math.sqrt(p.getDEX()) * 6;
		env.value += p.getLevel();
	}
}