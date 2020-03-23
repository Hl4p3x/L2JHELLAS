package com.l2jhellas.gameserver.skills.l2skills;

import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.templates.StatsSet;

public final class L2SkillMagicOnGround extends L2Skill
{
	public int effectNpcId;
	public int triggerEffectId;
	
	public L2SkillMagicOnGround(StatsSet set)
	{
		super(set);
		effectNpcId = set.getInteger("effectNpcId", -1);
		triggerEffectId = set.getInteger("triggerEffectId", -1);
	}
	
	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
		if (caster.isAlikeDead())
			return;
		
		getEffectsSelf(caster);
	}
}