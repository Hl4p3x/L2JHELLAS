package com.l2jhellas.gameserver.skills.funcsCalc;

import com.l2jhellas.gameserver.enums.items.L2WeaponType;
import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.Stats;
import com.l2jhellas.gameserver.skills.conditions.ConditionUsingItemType;
import com.l2jhellas.gameserver.skills.funcs.Func;

public class FuncBowAtkRange extends Func
{
	private static final FuncBowAtkRange _fbar_instance = new FuncBowAtkRange();
	
	public static Func getInstance()
	{
		return _fbar_instance;
	}
	
	private FuncBowAtkRange()
	{
		super(Stats.POWER_ATTACK_RANGE, 0x10, null);
		setCondition(new ConditionUsingItemType(L2WeaponType.BOW.mask()));
	}
	
	@Override
	public void calc(Env env)
	{
		if (!cond.test(env))
			return;
		env.value += 370;
	}
}