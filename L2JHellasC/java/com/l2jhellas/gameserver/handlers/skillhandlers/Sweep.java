package com.l2jhellas.gameserver.handlers.skillhandlers;

import java.util.List;

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
		
		for (L2Object target : targets)
		{
			if (!(target instanceof L2MonsterInstance))
				continue;
			
			final L2MonsterInstance mob = ((L2MonsterInstance) target);
			
			if (mob.isSweepActive() && mob.getIsSpoiledBy() == player.getObjectId())
			{
				final List<RewardItem> items = mob.getSweepItems();

				if (items.isEmpty())
					continue;

				for (RewardItem item : items)
				{
					if (player.isInParty())
						player.getParty().distributeItem(player, item, true,mob);
					else
						player.addItem("Sweep", item.getItemId(),item.getCount(), player, true);
				}	
				mob.getSweepItems().clear();
				mob.endDecayTask();
			}
			
		}
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}