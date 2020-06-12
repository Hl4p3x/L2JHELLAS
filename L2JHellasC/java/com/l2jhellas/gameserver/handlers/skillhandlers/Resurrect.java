package com.l2jhellas.gameserver.handlers.skillhandlers;

import com.l2jhellas.gameserver.enums.skills.L2SkillTargetType;
import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.handler.ISkillHandler;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PetInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.Formulas;
import com.l2jhellas.gameserver.taskmanager.DecayTaskManager;

public class Resurrect implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.RESURRECT
	};

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		for (L2Object cha : targets)
		{
			final L2Character target = (L2Character) cha;
			if (activeChar.isPlayer())
			{		
				if (cha.isPlayer())
				{
					// Check for same party or for same clan, if target is for clan.
					if (skill.getTargetType() == L2SkillTargetType.TARGET_CORPSE_CLAN && activeChar.getClanId() != ((L2PcInstance) cha).getClanId())
					{
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_MUST_BE_IN_CLAN));
						continue;
					}
					
					((L2PcInstance) cha).reviveRequest((L2PcInstance) activeChar, skill, false);
				}
				else if (cha instanceof L2PetInstance)
				{
					if (((L2PetInstance) cha).getOwner() == activeChar)
						target.doRevive(Formulas.calculateSkillResurrectRestorePercent(skill.getPower(), activeChar.getWIT()));
					else
						((L2PetInstance) cha).getOwner().reviveRequest((L2PcInstance) activeChar, skill, true);
				}
				else
					target.doRevive(Formulas.calculateSkillResurrectRestorePercent(skill.getPower(), activeChar.getWIT()));
			}
			else
			{
				DecayTaskManager.getInstance().cancelDecayTask(target);
				target.doRevive(Formulas.calculateSkillResurrectRestorePercent(skill.getPower(), activeChar.getWIT()));
			}
		}
		activeChar.rechargeShots(false, true);
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}