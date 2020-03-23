package com.l2jhellas.gameserver.skills.l2skills;

import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.Stats;
import com.l2jhellas.gameserver.skills.funcs.Func;
import com.l2jhellas.gameserver.templates.StatsSet;

public class L2SkillChargeNegate extends L2Skill
{
	public L2SkillChargeNegate(StatsSet set)
	{
		super(set);
	}
	
	public void useSkill(L2Character activeChar, L2Character... targets)
	{
		if (activeChar.isAlikeDead() || !(activeChar instanceof L2PcInstance))
			return;
		
		for (L2Character target : targets)
		{
			if (target.isAlikeDead())
				continue;
			
			String[] _negateStats = getNegateStats();
			int count = 0;
			for (String stat : _negateStats)
			{
				count++;
				if (count > getNumCharges())
					return; // ROOT=1 PARALYZE=2 SLOW=3
					
				if (stat == "root")
					negateEffect(target, L2SkillType.ROOT);
				if (stat == "slow")
				{
					negateEffect(target, L2SkillType.DEBUFF);
					negateEffect(target, L2SkillType.WEAKNESS);
				}
				if (stat == "paralyze")
					negateEffect(target, L2SkillType.PARALYZE);
			}
		}
	}
	
	private static void negateEffect(L2Character target, L2SkillType type)
	{
		L2Effect[] effects = target.getAllEffects();
		for (L2Effect e : effects)
		{
			if (type == L2SkillType.DEBUFF || type == L2SkillType.WEAKNESS)
			{
				if (e.getSkill().getSkillType() == type)
				{
					// Only exit debuffs and weaknesses affecting runSpd
					for (Func f : e.getStatFuncs())
					{
						if (f.stat == Stats.RUN_SPEED)
						{
							if (target instanceof L2PcInstance)
							{
								SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_WORN_OFF);
								sm.addSkillName(e.getSkill().getId());
								target.sendPacket(sm);
							}
							e.exit();
							break;
						}
					}
				}
			}
			else if (e.getSkill().getSkillType() == type)
			{
				if (target instanceof L2PcInstance)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_WORN_OFF);
					sm.addSkillName(e.getSkill().getId());
					target.sendPacket(sm);
				}
				e.exit();
			}
		}
	}
	
	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
	}
}