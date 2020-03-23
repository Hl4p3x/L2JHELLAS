package com.l2jhellas.gameserver.skills.effects;

import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.skills.Env;

public class EffectCharmOfLuck extends L2Effect
{
	public EffectCharmOfLuck(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.CHARM_OF_LUCK;
	}
	
	@Override
	public boolean onStart()
	{
		if (getEffected() instanceof L2Playable)
		{
			((L2Playable) getEffected()).startCharmOfLuck();
			return true;
		}
		return false;
	}
	
	@Override
	public void onExit()
	{
		if (getEffected() instanceof L2Playable)
			((L2Playable) getEffected()).stopCharmOfLuck(this);
	}
	
	@Override
	public boolean onActionTime()
	{
		// just stop this effect
		return false;
	}
}