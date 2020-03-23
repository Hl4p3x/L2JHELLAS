package com.l2jhellas.gameserver.skills.effects;

import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.skills.Env;

public final class EffectBigHead extends L2Effect
{
	public EffectBigHead(Env env, EffectTemplate template)
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
		getEffected().startAbnormalEffect(0x02000);
		return true;
	}
	
	@Override
	public void onExit()
	{
		getEffected().stopAbnormalEffect(0x02000);
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}