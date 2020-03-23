package com.l2jhellas.gameserver.skills.l2skills;

import com.l2jhellas.gameserver.enums.skills.L2SkillTargetType;
import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.skills.effects.EffectSeed;
import com.l2jhellas.gameserver.templates.StatsSet;

public class L2SkillSeed extends L2Skill
{
	public L2SkillSeed(StatsSet set)
	{
		super(set);
	}
	
	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
		if (caster.isAlikeDead())
			return;
		
		// Update Seeds Effects
		for (L2Object target2 : targets)
		{
			L2Character target = (L2Character) target2;
			if (target.isAlikeDead() && getTargetType() != L2SkillTargetType.TARGET_CORPSE_MOB)
				continue;
			
			EffectSeed oldEffect = (EffectSeed) target.getFirstEffect(getId());
			if (oldEffect == null)
				getEffects(caster, target);
			else
				oldEffect.increasePower();
			
			L2Effect[] effects = target.getAllEffects();
			for (L2Effect effect : effects)
				if (effect.getEffectType() == L2Effect.EffectType.SEED)
					effect.rescheduleEffect();
			
		}
	}
}