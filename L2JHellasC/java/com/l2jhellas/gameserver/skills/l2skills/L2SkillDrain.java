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
		
		boolean sps = false;
		boolean bsps = false;
		final boolean isPlayable = activeChar.isPlayable();
		

		final L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		
		if (weaponInst != null)
		{
			switch (weaponInst.getChargedSpiritshot())
			{
				case L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT:
					weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
					bsps = true;
					break;
				case L2ItemInstance.CHARGED_SPIRITSHOT:
					weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
					sps = true;
					break;
			}
		}	
		else if (activeChar instanceof L2Summon)
		{
			L2Summon activeSummon = (L2Summon) activeChar;
			switch (activeSummon.getChargedSpiritShot())
			{
				case L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT:
					activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
					bsps = true;
					break;
				case L2ItemInstance.CHARGED_SPIRITSHOT:
					activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
					sps = true;
					break;
			}
		}
		
		for (L2Object obj : targets)
		{
			if (!(obj instanceof L2Character))
				continue;
			
			final L2Character target = ((L2Character) obj);
			if (target.isAlikeDead() && getTargetType() != L2SkillTargetType.TARGET_CORPSE_MOB)
				continue;
			
			if (activeChar != target && target.isInvul())
				continue;
				
			final boolean mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(target, this));
			final int damage = (int) Formulas.calcMagicDam(activeChar, target, this, sps, bsps, mcrit);
			
			if (damage > 0)
			{
				int _CurrCp = (int) target.getCurrentCp();
				int _CurrHp = (int) target.getCurrentHp();
				int _drain = 0;
				
				if (isPlayable && _CurrCp > 0)
				{
					if (damage < _CurrCp)
						_drain = 0;
					else
						_drain = damage - _CurrCp;
				}
				else if (damage > _CurrHp)
					_drain = _CurrHp;
				else
					_drain = damage;
				
				final double hpAdd = _absorbAbs + _absorbPart * _drain;
				if (hpAdd > 0)
				{
					final double hp = ((activeChar.getCurrentHp() + hpAdd) > activeChar.getMaxHp() ? activeChar.getMaxHp() : (activeChar.getCurrentHp() + hpAdd));
					
					activeChar.setCurrentHp(hp);
					
					StatusUpdate suhp = new StatusUpdate(activeChar.getObjectId());
					suhp.addAttribute(StatusUpdate.CUR_HP, (int) hp);
					activeChar.sendPacket(suhp);
				}
				
				if (!target.isDead() || getTargetType() != L2SkillTargetType.TARGET_CORPSE_MOB)
				{
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
							target.stopSkillEffects(getId());
							if (Formulas.calcSkillSuccess(activeChar, target, this, false,sps,bsps))
								getEffects(activeChar, target);
							else
								activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(getId()));
						}
					}
					target.reduceCurrentHp(damage, activeChar);
				}
			}
			
			if (target.isDead() && getTargetType() == L2SkillTargetType.TARGET_CORPSE_MOB && target instanceof L2Npc)
				((L2Npc) target).endDecayTask();
		}
		
		if (hasEffects())
		{
			final L2Effect effect = activeChar.getFirstEffect(getId());
			if (effect != null && effect.isSelfEffect())
				effect.exit();
			
			getEffectsSelf(activeChar);
		}

		activeChar.rechargeShots(false, true);
	}
}