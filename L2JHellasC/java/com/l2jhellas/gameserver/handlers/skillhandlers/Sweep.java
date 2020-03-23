package com.l2jhellas.gameserver.handlers.skillhandlers;

import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.handler.ISkillHandler;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Attackable.RewardItem;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class Sweep implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.SWEEP
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (!(activeChar instanceof L2PcInstance))
			return;
		
		final L2PcInstance player = (L2PcInstance) activeChar;

		for(L2Object target : targets)
		{			
			if (!(target instanceof L2MonsterInstance))
				continue;
			
			final L2MonsterInstance mob = ((L2MonsterInstance) target);
							
			if (mob.isSweepActive())
			{
				final RewardItem[] items = mob.takeSweep();
				
				if (items == null || items.length == 0)
					continue;	
				
				for (RewardItem ritem : items)
				{
				    if (player.isInParty())
					    player.getParty().distributeItem(player, ritem, true, mob);
				    else
						player.getInventory().addItem("Sweep", ritem.getItemId(), ritem.getCount(), player, target);				
				}
			
			}
			
			mob.endDecayTask();
		}
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}