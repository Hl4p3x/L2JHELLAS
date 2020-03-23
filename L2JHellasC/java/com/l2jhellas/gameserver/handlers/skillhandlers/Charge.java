package com.l2jhellas.gameserver.handlers.skillhandlers;

import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.handler.ISkillHandler;
import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class Charge implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = {
	// SkillType.CHARGE
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		
		for (int index = 0; index < targets.length; index++)
		{
			if (!(targets[index] instanceof L2PcInstance))
				continue;
			L2PcInstance target = (L2PcInstance) targets[index];
			skill.getEffects(activeChar, target);
		}
		// self Effect :]
		L2Effect effect = activeChar.getFirstEffect(skill.getId());
		if (effect != null && effect.isSelfEffect())
		{
			// Replace old effect with new one.
			effect.exit();
		}
		skill.getEffectsSelf(activeChar);
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}