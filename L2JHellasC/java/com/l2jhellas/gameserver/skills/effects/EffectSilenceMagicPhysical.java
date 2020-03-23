package com.l2jhellas.gameserver.skills.effects;

import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.skills.Env;

public class EffectSilenceMagicPhysical extends L2Effect
{
	public EffectSilenceMagicPhysical(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return L2Effect.EffectType.SILENCE_MAGIC_PHYSICAL;
	}
	
	@Override
	public boolean onStart()
	{
		getEffected().startMuted();
		getEffected().startPsychicalMuted();
		return true;
	}
	
	@Override
	public boolean onActionTime()
	{
		getEffected().stopMuted(this);
		getEffected().stopPsychicalMuted(this);
		return false;
	}
	
	@Override
	public void onExit()
	{
		getEffected().stopMuted(this);
		getEffected().stopPsychicalMuted(this);
	}
}