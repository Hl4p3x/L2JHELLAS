package com.l2jhellas.gameserver.skills.effects;

import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.skills.Env;

public final class EffectImmobilePetBuff extends L2Effect
{
	private L2Summon _pet;
	
	public EffectImmobilePetBuff(Env env, EffectTemplate template)
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
		_pet = null;
		
		if (getEffected() instanceof L2Summon && getEffector() instanceof L2PcInstance && ((L2Summon) getEffected()).getOwner() == getEffector())
		{
			_pet = (L2Summon) getEffected();
			_pet.setIsImmobilized(true);
			return true;
		}
		return false;
	}
	
	@Override
	public void onExit()
	{
		if (_pet != null)
			_pet.setIsImmobilized(false);
	}
	
	@Override
	public boolean onActionTime()
	{
		// just stop this effect
		return false;
	}
}