package com.l2jhellas.gameserver.skills.effects;

import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.skills.Env;

public class EffectStunSelf extends L2Effect
{
	public EffectStunSelf(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.STUN_SELF;
	}
	
	@Override
	public boolean onStart()
	{
		getEffector().startStunning();
		return true;
	}
	
	@Override
	public void onExit()
	{
		getEffector().stopStunning(this);
	}
	
	@Override
	public boolean onActionTime()
	{
		// just stop this effect
		return false;
	}
}