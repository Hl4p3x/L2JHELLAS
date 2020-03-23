package com.l2jhellas.gameserver.skills.effects;

import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.skills.Env;

public class EffectImmobileUntilAttacked extends L2Effect
{
	public EffectImmobileUntilAttacked(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.IMMOBILEUNTILATTACKED;
	}
	
	@Override
	public boolean onStart()
	{
		getEffector().startImmobileUntilAttacked();
		return true;
	}
	
	@Override
	public void onExit()
	{
		getEffected().stopImmobileUntilAttacked(this);
	}
	
	@Override
	public boolean onActionTime()
	{
		// just stop this effect
		return false;
	}
}