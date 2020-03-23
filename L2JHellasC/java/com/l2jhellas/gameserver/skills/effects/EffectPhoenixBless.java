package com.l2jhellas.gameserver.skills.effects;

import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.skills.Env;

final class EffectPhoenixBless extends L2Effect
{
	public EffectPhoenixBless(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.PHOENIX_BLESSING;
	}
	
	@Override
	public boolean onStart()
	{
		if (getEffected() instanceof L2Playable)
		{
			((L2Playable) getEffected()).startPhoenixBlessing();
			return true;
		}
		return false;
	}
	
	@Override
	public void onExit()
	{
		if (getEffected() instanceof L2Playable)
			((L2Playable) getEffected()).stopPhoenixBlessing(this);
	}
	
	@Override
	public boolean onActionTime()
	{
		// just stop this effect
		return false;
	}
}