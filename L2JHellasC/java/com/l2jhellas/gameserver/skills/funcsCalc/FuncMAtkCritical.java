package com.l2jhellas.gameserver.skills.funcsCalc;

import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.Formulas;
import com.l2jhellas.gameserver.skills.Stats;
import com.l2jhellas.gameserver.skills.funcs.Func;

public class FuncMAtkCritical extends Func
{
	static final FuncMAtkCritical _fac_instance = new FuncMAtkCritical();
	
	public static Func getInstance()
	{
		return _fac_instance;
	}
	
	private FuncMAtkCritical()
	{
		super(Stats.MCRITICAL_RATE, 0x30, null);
	}
	
	@Override
	public void calc(Env env)
	{
		L2Character p = env.player;
		if (p instanceof L2Summon)
			env.value = 8;
		else if (p instanceof L2PcInstance && p.getActiveWeaponInstance() == null)
			env.value = 8;
		else
		{
			env.value *= Formulas.WITbonus[p.getWIT()];
		}
	}
}