package com.l2jhellas.gameserver.skills.effects;

import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.skills.Env;

public final class EffectBetray extends L2Effect
{
	public EffectBetray(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.BETRAY;
	}
	
	@Override
	public boolean onStart()
	{
		if ((getEffected() != null) && getEffected() instanceof L2Summon)
		{
			L2PcInstance targetOwner = null;
			targetOwner = ((L2Summon) getEffected()).getOwner();
			targetOwner.setIsBetrayed(true);
			getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, targetOwner);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onActionTime()
	{
		if ((getEffected() != null) && getEffected() instanceof L2Summon)
		{
			L2PcInstance targetOwner = null;
			targetOwner = ((L2Summon) getEffected()).getOwner();
			targetOwner.setIsBetrayed(false);
			getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
		return true;
	}
}