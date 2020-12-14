package com.l2jhellas.gameserver.skills.effects;

import com.l2jhellas.gameserver.enums.skills.AbnormalEffect;
import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.skills.Env;

public class EffectClanGate extends L2Effect
{
	public EffectClanGate(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public boolean onStart()
	{
		getEffected().startAbnormalEffect(AbnormalEffect.MAGIC_CIRCLE);
		return true;
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
	
	@Override
	public void onExit()
	{
		getEffected().stopAbnormalEffect(AbnormalEffect.MAGIC_CIRCLE);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.CLAN_GATE;
	}
}