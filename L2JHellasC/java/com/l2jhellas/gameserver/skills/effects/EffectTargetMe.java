package com.l2jhellas.gameserver.skills.effects;

import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jhellas.gameserver.skills.Env;

public class EffectTargetMe extends L2Effect
{
	public EffectTargetMe(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.TARGET_ME;
	}
	
	@Override
	public boolean onStart()
	{
		if (getEffected() instanceof L2PcInstance)
		{
			getEffected().setTarget(getEffector());
			getEffected().sendPacket(new MyTargetSelected(getEffector().getObjectId(), 0));
			getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getEffector());
			return true;
		}
		return false;
	}
	
	@Override
	public void onExit()
	{
		// nothing
	}
	
	@Override
	public boolean onActionTime()
	{
		// nothing
		return false;
	}
}