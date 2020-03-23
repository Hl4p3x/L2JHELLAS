package com.l2jhellas.gameserver.handlers.skillhandlers;

import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.handler.ISkillHandler;
import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.Formulas;

public class Mdam implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.MDAM,
		L2SkillType.DEATHLINK
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (activeChar.isAlikeDead())
			return;
		
		boolean ss = false;
		boolean bss = false;
		
		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		
		if (weaponInst != null)
		{
			if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
			{
				bss = true;
				weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
			}
			else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
			{
				ss = true;
				weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
			}
		}
		// If there is no weapon equipped, check for an active summon.
		else if (activeChar instanceof L2Summon)
		{
			L2Summon activeSummon = (L2Summon) activeChar;
			
			if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
			{
				bss = true;
				activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
			}
			else if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT)
			{
				ss = true;
				activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
			}
		}
		
		for (L2Object target2 : targets)
		{
			L2Character target = (L2Character) target2;
			
			if (activeChar instanceof L2PcInstance && target instanceof L2PcInstance && target.isAlikeDead() && target.isFakeDeath())
			{
				target.getActingPlayer().stopFakeDeath(null);
			}
			else if (target.isAlikeDead())
			{
				continue;
			}
			// if (skill != null)
			// if (skill.isOffensive())
			// {
			
			// boolean acted;
			// if (skill.getSkillType() == L2Skill.SkillType.DOT ||
			// skill.getSkillType() == L2Skill.SkillType.MDOT)
			// acted = Formulas.getInstance().calcSkillSuccess(
			// activeChar, target, skill);
			// else
			// acted = Formulas.getInstance().calcMagicAffected(
			// activeChar, target, skill);
			// if (!acted) {
			// activeChar.sendPacket(new
			// SystemMessage(SystemMessage.MISSED_TARGET));
			// continue;
			// }
			//
			// }
			
			boolean mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(target, skill));
			
			int damage = (int) Formulas.calcMagicDam(activeChar, target, skill, ss, bss, mcrit);
			final byte reflect = Formulas.calcSkillReflect(target, skill);

			// Why are we trying to reduce the current target HP here?
			// Why not inside the below "if" condition, after the effects
			// processing as it should be?
			// It doesn't seem to make sense for me. I'm moving this line inside
			// the "if" condition, right after the effects processing...
			// [changed by nexus - 2006-08-15]
			// target.reduceCurrentHp(damage, activeChar);
			
			if (damage > 0)
			{
				// Manage attack or cast break of the target (calculating rate,
				// sending message...)
				if (!target.isRaid() && !target.isBoss() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}
				
				if ((reflect & 2) != 0)
					activeChar.reduceCurrentHp(damage, target);
				else
				{
					activeChar.sendDamageMessage(target, damage, mcrit, false, false);
					target.reduceCurrentHp(damage, activeChar);
				}
				
				if (skill.hasEffects())
				{
					if ((reflect & 1) != 0)
					{
						activeChar.stopSkillEffects(skill.getId());
						skill.getEffects(target, activeChar);
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill));
					}
					else
					{
						// activate attacked effects, if any
						target.stopSkillEffects(skill.getId());
						if (Formulas.calcSkillSuccess(activeChar, target, skill, false, ss, bss))
							skill.getEffects(activeChar, target);
					}
				}			
			}
		}
		// self Effect :]
		L2Effect effect = activeChar.getFirstEffect(skill.getId());
		if (effect != null && effect.isSelfEffect())
		{
			// Replace old effect with new one.
			effect.exit();
		}
		skill.getEffectsSelf(activeChar);
		
		if (skill.isSuicideAttack())
		{
			activeChar.doDie(null);
			activeChar.setCurrentHp(0);
		}
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}