package com.l2jhellas.gameserver.skills.effects;

import com.l2jhellas.gameserver.enums.skills.AbnormalEffect;
import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.skills.Env;

public class EffectPetrification extends L2Effect
{
	public EffectPetrification(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return L2Effect.EffectType.PETRIFICATION;
	}
	
	@Override
	public boolean onStart()
	{
		getEffected().startAbnormalEffect(AbnormalEffect.HOLD_2);
		getEffected().setIsParalyzed(true);
		getEffected().setIsInvul(true);
		return true;
	}
	
	@Override
	public void onExit()
	{
		getEffected().stopAbnormalEffect(AbnormalEffect.HOLD_2);
		getEffected().setIsParalyzed(false);
		getEffected().setIsInvul(false);
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}