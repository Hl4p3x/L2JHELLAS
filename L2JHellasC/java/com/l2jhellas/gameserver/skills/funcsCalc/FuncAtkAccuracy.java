package com.l2jhellas.gameserver.skills.funcsCalc;

import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.Stats;
import com.l2jhellas.gameserver.skills.funcs.Func;

public class FuncAtkAccuracy extends Func
{
	static final FuncAtkAccuracy _faa_instance = new FuncAtkAccuracy();
	
	public static Func getInstance()
	{
		return _faa_instance;
	}
	
	private FuncAtkAccuracy()
	{
		super(Stats.ACCURACY_COMBAT, 0x10, null);
	}
	
	@Override
	public void calc(Env env)
	{
		L2Character p = env.player;
		// [Square(DEX)]*6 + lvl + weapon hitbonus;
		env.value += Math.sqrt(p.getDEX()) * 6;
		env.value += p.getLevel();
		if (p instanceof L2Summon)
			env.value += (p.getLevel() < 60) ? 4 : 5;
	}
}