package com.l2jhellas.gameserver.skills.funcsCalc;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.Formulas;
import com.l2jhellas.gameserver.skills.Stats;
import com.l2jhellas.gameserver.skills.funcs.Func;

public class FuncAtkCritical extends Func
{
	static final FuncAtkCritical _fac_instance = new FuncAtkCritical();
	
	public static Func getInstance()
	{
		return _fac_instance;
	}
	
	private FuncAtkCritical()
	{
		super(Stats.CRITICAL_RATE, 0x09, null);
	}
	
	@Override
	public void calc(Env env)
	{
		L2Character p = env.player;
		if (p instanceof L2Summon)
			env.value = 40;
		else if (p instanceof L2PcInstance && p.getActiveWeaponInstance() == null)
			env.value = 40;
		else
		{
			env.value *= Formulas.DEXbonus[p.getDEX()];
			env.value *= 10;
			if (env.value > Config.MAX_PCRIT_RATE)
				env.value = Config.MAX_PCRIT_RATE;
			
		}
	}
}