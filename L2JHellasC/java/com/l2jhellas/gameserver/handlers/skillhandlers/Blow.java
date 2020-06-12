package com.l2jhellas.gameserver.handlers.skillhandlers;

import com.l2jhellas.gameserver.enums.items.L2WeaponType;
import com.l2jhellas.gameserver.enums.player.Position;
import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.enums.sound.Sound;
import com.l2jhellas.gameserver.handler.ISkillHandler;
import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.Formulas;
import com.l2jhellas.gameserver.skills.Stats;
import com.l2jhellas.gameserver.skills.funcs.Func;
import com.l2jhellas.util.Rnd;
import com.l2jhellas.util.Util;

public class Blow implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.BLOW
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (activeChar.isAlikeDead())
			return;
		
		for (L2Character target : (L2Character[]) targets)
		{
			if (target.isAlikeDead())
				continue;
			
			// Check firstly if target dodges skill
			final boolean skillIsEvaded = Formulas.calcPhysicalSkillEvasion(target, skill);
			
			final Position position = Position.getPosition(activeChar,target);
			final int _successChance = position == Position.BACK ? 70 : position == Position.SIDE ? 60 : 50;
						
			// If skill requires Crit or skill requires behind,
			// calculate chance based on DEX, Position and on self BUFF
			boolean success = true;
			if ((skill.getCondition() & L2Skill.COND_BEHIND) != 0)
				success = (_successChance == 70);
			if ((skill.getCondition() & L2Skill.COND_CRIT) != 0)
				success = (success && Formulas.calcBlow(activeChar, target, _successChance));
			if (!skillIsEvaded && success)
			{
				final byte reflect = Formulas.calcSkillReflect(target, skill);

				if (skill.hasEffects())
				{
					if (reflect == 1)
					{
						activeChar.stopSkillEffects(skill.getId());
						skill.getEffects(target, activeChar);
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill));
					}
				}
				L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
				boolean soul = (weapon != null && weapon.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT && weapon.getItemType() == L2WeaponType.DAGGER);
				byte shld = Formulas.calcShldUse(activeChar, target);
				
				// Crit rate base crit rate for skill, modified with STR bonus
				boolean crit = false;
				if (Formulas.calcCrit(skill.getBaseCritRate() * 10 * Formulas.getSTRBonus(activeChar)))
					crit = true;
				double damage = (int) Formulas.calcBlowDamage(activeChar, target, skill, shld, soul);
				if (crit)
				{
					damage *= 2;
					// Vicious Stance is special after C5, and only for BLOW
					// skills
					// Adds directly to damage
					L2Effect vicious = activeChar.getFirstEffect(312);
					if (vicious != null && damage > 1)
					{
						for (Func func : vicious.getStatFuncs())
						{
							Env env = new Env();
							env.player = activeChar;
							env.target = target;
							env.skill = skill;
							env.value = damage;
							func.calc(env);
							damage = (int) env.value;
						}
					}
				}
				
				if (soul && weapon != null)
					weapon.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
				if (skill.getDmgDirectlyToHP() && target instanceof L2PcInstance)
				{
					L2PcInstance player = (L2PcInstance) target;
					if (!player.isInvul())
					{
						// Check and calculate transfered damage
						L2Summon summon = player.getPet();
						if (summon != null && summon instanceof L2SummonInstance && Util.checkIfInRange(900, player, summon, true))
						{
							int tDmg = (int) damage * (int) player.getStat().calcStat(Stats.TRANSFER_DAMAGE_PERCENT, 0, null, null) / 100;
							
							// Only transfer dmg up to current HP, it should not
							// be killed
							if (summon.getCurrentHp() < tDmg)
								tDmg = (int) summon.getCurrentHp() - 1;
							if (tDmg > 0)
							{
								summon.reduceCurrentHp(tDmg, activeChar);
								damage -= tDmg;
							}
						}
						if (damage >= player.getCurrentHp())
						{
							if (player.isInDuel())
								player.setCurrentHp(1);
							else
							{
								player.setCurrentHp(0);
								if (player.isInOlympiadMode())
								{
									player.abortAttack();
									player.abortCast();
									player.getStatus().stopHpMpRegeneration();
								}
								else
									player.doDie(activeChar);
							}
						}
						else
							player.setCurrentHp(player.getCurrentHp() - damage);
					}
					SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.S1_GAVE_YOU_S2_DMG);
					smsg.addString(activeChar.getName());
					smsg.addNumber((int) damage);
					player.sendPacket(smsg);
				}
				else
					target.reduceCurrentHp(damage, activeChar);
				
				if ((reflect & 2) != 0)
				{
					if (target.isPlayer())
						target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.COUNTERED_S1_ATTACK).addCharName(activeChar));
					
					if (activeChar.isPlayer())
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_PERFORMING_COUNTERATTACK).addCharName(target));
					
					double vdamage = (700 * target.getPAtk(activeChar) / activeChar.getPDef(target));
					activeChar.reduceCurrentHp(vdamage, target);
				}
				
				if (activeChar instanceof L2PcInstance)
				{
					activeChar.broadcastPacket(Sound.SKILLSOUND_CRITICAL.getPacket());
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CRITICAL_HIT));
				}
				
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_DID_S1_DMG);
				sm.addNumber((int) damage);
				activeChar.sendPacket(sm);
			}
			// Possibility of a lethal strike
			if ((!target.isRaid() && !target.isBoss()) && !(target instanceof L2DoorInstance) && !(target instanceof L2GrandBossInstance) && !(target instanceof L2MonsterInstance && ((L2MonsterInstance) target).getNpcId() == 36006) && (target instanceof L2Npc && ((L2Npc) target).getNpcId() != 35062))
			{
				int chance = Rnd.get(100);
				// 2nd lethal effect activate (cp,hp to 1 or if target is npc
				// then hp to 1)
				if (skill.getLethalChance2() > 0 && chance < Formulas.calcLethal(activeChar, target, skill.getLethalChance2()))
				{
					if (target instanceof L2Npc)
						target.reduceCurrentHp(target.getCurrentHp() - 1, activeChar);
					else if (target instanceof L2PcInstance) // If is a active player set his HP and CP to 1
					{
						L2PcInstance player = (L2PcInstance) target;
						if (!player.isInvul())
						{
							player.setCurrentHp(1);
							player.setCurrentCp(1);
						}
					}
					activeChar.broadcastPacket(Sound.SKILLSOUND_CRITICAL.getPacket());
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.LETHAL_STRIKE));
					
				}
				else if (skill.getLethalChance1() > 0 && chance < Formulas.calcLethal(activeChar, target, skill.getLethalChance1()))
				{
					if (target instanceof L2PcInstance)
					{
						L2PcInstance player = (L2PcInstance) target;
						if (!player.isInvul())
							player.setCurrentCp(1); // Set CP to 1
					}
					else if (target instanceof L2Npc) // If is a monster remove first damage and after 50% of current hp
						target.reduceCurrentHp(target.getCurrentHp() / 2, activeChar);
					
					activeChar.broadcastPacket(Sound.SKILLSOUND_CRITICAL.getPacket());
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.LETHAL_STRIKE));
					
				}
			}
			
			if (skill.hasSelfEffects())
			{
				final L2Effect effect = activeChar.getFirstEffect(skill.getId());
				if (effect != null && effect.isSelfEffect())
					effect.exit();
				
				skill.getEffectsSelf(activeChar);
			}
		}
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}