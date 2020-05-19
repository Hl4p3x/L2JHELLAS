package com.l2jhellas.gameserver.skills.effects;

import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.skills.Env;

public final class EffectSleep extends L2Effect
{
	public EffectSleep(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.SLEEP;
	}
	
	@Override
	public boolean onStart()
	{
		getEffected().startSleeping();
		return true;
	}
	
	@Override
	public boolean onSameEffect(L2Effect effect)
	{
		return false;
	}
	
	@Override
	public void onExit()
	{
		getEffected().stopSleeping(this);
	}
	
	@Override
	public boolean onActionTime()
	{
		getEffected().stopSleeping(this);
		// just stop this effect
		return false;
	}
}