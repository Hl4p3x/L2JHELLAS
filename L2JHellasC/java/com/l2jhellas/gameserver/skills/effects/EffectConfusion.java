package com.l2jhellas.gameserver.skills.effects;

import java.util.ArrayList;
import java.util.List;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.util.Rnd;

public final class EffectConfusion extends L2Effect
{
	public EffectConfusion(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.CONFUSION;
	}
	
	@Override
	public boolean onStart()
	{
		getEffected().startConfused();
		if (Config.DEBUG)
			System.out.println(getEffected());
		List<L2Character> targetList = new ArrayList<>();
		
		// Getting the possible targets
		
		for (L2Character obj : L2World.getInstance().getVisibleObjects(getEffected(), L2Character.class))
		{
			if (obj == null)
				continue;
			
			if ((obj != getEffected()))
				targetList.add(obj);
		}
		// if there is no target, exit function
		if (targetList.size() == 0)
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
	
	@Override
	public boolean onActionTime()
	{
		getEffected().stopConfused(this);
		return false;
	}
}