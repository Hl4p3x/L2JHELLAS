package com.l2jhellas.gameserver.skills.effects;

import java.util.ArrayList;
import java.util.List;

import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.util.Rnd;

public final class EffectConfuseMob extends L2Effect
{
	
	public EffectConfuseMob(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.CONFUSE_MOB_ONLY;
	}
	
	@Override
	public boolean onStart()
	{
		getEffected().startConfused();
		onActionTime();
		return true;
	}
	
	@Override
	public void onExit()
	{
		getEffected().stopConfused(this);
	}
	
	@Override
	public boolean onActionTime()
	{
		List<L2Character> targetList = new ArrayList<>();
		
		// Getting the possible targets
		
		for (L2Character obj : L2World.getInstance().getVisibleObjects(getEffected(), L2Attackable.class))	
		{
			if ((obj != getEffected()))
				targetList.add(obj);
		}
		
		// if there is no target, exit function
		if (targetList.isEmpty())
		{
			return true;
		}
		
		// Choosing randomly a new target
		int nextTargetIdx = Rnd.nextInt(targetList.size());
		L2Object target = targetList.get(nextTargetIdx);
		
		// Attacking the target
		// getEffected().setTarget(target);
		getEffected().setTarget(target);
		getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
		((L2Attackable) getEffected()).addDamageHate((L2Character) target, 0, (4 + Rnd.get(4)) * getEffector().getLevel());
		return true;
	}
}