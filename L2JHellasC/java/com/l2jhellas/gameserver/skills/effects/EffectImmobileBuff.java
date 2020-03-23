package com.l2jhellas.gameserver.skills.effects;

import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.skills.Env;

public final class EffectImmobileBuff extends L2Effect
{
	public EffectImmobileBuff(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.BUFF;
	}
	
	@Override
	public boolean onStart()
	{
		getEffector().setIsImmobilized(true);
		return super.onStart();
	}
	
	@Override
	public void onExit()
	{
		getEffector().setIsImmobilized(false);
		super.onExit();
	}
	
	@Override
	public boolean onActionTime()
	{
		// just stop this effect
		return false;
	}
}