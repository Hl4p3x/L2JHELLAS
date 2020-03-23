package com.l2jhellas.gameserver.skills.funcsCalc;

import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.Formulas;
import com.l2jhellas.gameserver.skills.Stats;
import com.l2jhellas.gameserver.skills.funcs.Func;

public class FuncMAtkMod extends Func
{
	static final FuncMAtkMod _fma_instance = new FuncMAtkMod();
	
	public static Func getInstance()
	{
		return _fma_instance;
	}
	
	private FuncMAtkMod()
	{
		super(Stats.MAGIC_ATTACK, 0x20, null);
	}
	
	@Override
	public void calc(Env env)
	{
		double intb = Formulas.INTbonus[env.player.getINT()];
		double lvlb = env.player.getLevelMod();
		env.value *= (lvlb * lvlb) * (intb * intb);
	}
}