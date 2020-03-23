package com.l2jhellas.gameserver.skills.effects;

import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.skills.Env;

public class EffectInvincible extends L2Effect
{
	public EffectInvincible(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return L2Effect.EffectType.INVINCIBLE;
	}
	
	@Override
	public boolean onStart()
	{
		getEffected().setIsInvul(true);
		return true;
	}
	
	@Override
	public boolean onActionTime()
	{
		// Simply stop the effect
		getEffected().setIsInvul(false);
		return false;
	}
	
	@Override
	public void onExit()
	{
		getEffected().setIsInvul(false);
	}
}