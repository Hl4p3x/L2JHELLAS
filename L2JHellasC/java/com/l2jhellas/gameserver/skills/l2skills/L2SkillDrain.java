package com.l2jhellas.gameserver.skills.l2skills;

import com.l2jhellas.gameserver.enums.skills.L2SkillTargetType;
import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.StatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.Formulas;
import com.l2jhellas.gameserver.templates.StatsSet;

public class L2SkillDrain extends L2Skill
{
	
	private final float _absorbPart;
	private final int _absorbAbs;
	
	public L2SkillDrain(StatsSet set)
	{
		super(set);
		
		_absorbPart = set.getFloat("absorbPart", 0.f);
		_absorbAbs = set.getInteger("absorbAbs", 0);
	}
	
	@Override
	public void useSkill(L2Character activeChar, L2Object[] targets)
	{
		if (activeChar.isAlikeDead())
			return;
		
		boolean ss = false;
		boolean bss = false;
		
		for (L2Object target2 : targets)
		{
			L2Character target = (L2Character) target2;
			if (target.isAlikeDead() && getTargetType() != L2SkillTargetType.TARGET_CORPSE_MOB)
				continue;
			
			if (activeChar != target && target.isInvul())
				continue; // No effect on invulnerable chars unless they cast it themselves.
				
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
			
			boolean mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(target, this));
			int damage = (int) Formulas.calcMagicDam(activeChar, target, this, ss, bss, mcrit);
			
			double hpAdd = _absorbAbs + _absorbPart * damage;
			double hp = ((activeChar.getCurrentHp() + hpAdd) > activeChar.getMaxHp() ? activeChar.getMaxHp() : (activeChar.getCurrentHp() + hpAdd));
			
			activeChar.setCurrentHp(hp);
			
			StatusUpdate suhp = new StatusUpdate(activeChar.getObjectId());
			suhp.addAttribute(StatusUpdate.CUR_HP, (int) hp);
			activeChar.sendPacket(suhp);
			
			// Check to see if we should damage the target
			if (damage > 0 && (!target.isDead() || getTargetType() != L2SkillTargetType.TARGET_CORPSE_MOB))
			{
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && !target.isBoss() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}
				
				activeChar.sendDamageMessage(target, damage, mcrit, false, false);
				
				if (hasEffects() && getTargetType() != L2SkillTargetType.TARGET_CORPSE_MOB)
				{
					if ((Formulas.calcSkillReflect(target, this) & 1) > 0)
					{
						activeChar.stopSkillEffects(getId());
						getEffects(target, activeChar);
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(getId()));
					}
					else
					{
						// activate attacked effects, if any
						target.stopSkillEffects(getId());
						if (Formulas.calcSkillSuccess(activeChar, target, this, false, ss, bss))
							getEffects(activeChar, target);
						else
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(getId()));
					}
				}
				
				target.reduceCurrentHp(damage, activeChar);
			}
			
			// Check to see if we should do the decay right after the cast
			if (target.isDead() && getTargetType() == L2SkillTargetType.TARGET_CORPSE_MOB && target instanceof L2Npc)
			{
				((L2Npc) target).endDecayTask();
			}
		}
		// effect self :]
		L2Effect effect = activeChar.getFirstEffect(getId());
		if (effect != null && effect.isSelfEffect())
		{
			// Replace old effect with new one.
			effect.exit();
		}
		// cast self effect if any
		getEffectsSelf(activeChar);
	}
}