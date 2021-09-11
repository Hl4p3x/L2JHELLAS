package com.l2jhellas.gameserver.handlers.skillhandlers;

import com.l2jhellas.gameserver.enums.items.L2WeaponType;
import com.l2jhellas.gameserver.enums.items.ShotType;
import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.handler.ISkillHandler;
import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.EtcStatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.Formulas;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.gameserver.skills.effects.EffectCharge;
import com.l2jhellas.util.Rnd;

public class Pdam implements ISkillHandler
{
	
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.PDAM,
		L2SkillType.FATAL
	// SkillType.CHARGEDAM
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (activeChar.isAlikeDead())
			return;
		
		int damage = 0;
		
		for (L2Object target2 : targets)
		{
			L2Character target = (L2Character) target2;
			L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
			
			if (target.isDead())
				continue;
			
			if (activeChar instanceof L2PcInstance && target.isPlayer() && target.isAlikeDead() && target.isFakeDeath())
				target.getActingPlayer().stopFakeDeath(null);
		
			boolean dual = activeChar.isUsingDualWeapon();
			byte shld = Formulas.calcShldUse(activeChar, target);
			// PDAM critical chance not affected by buffs, only by STR. Only
			// some skills are meant to crit.
			boolean crit = false;
			if (skill.getBaseCritRate() > 0)
				crit = Formulas.calcCrit(skill.getBaseCritRate() * 10 * Formulas.getSTRBonus(activeChar));
			
			final boolean soul = activeChar.isChargedShot(ShotType.SOULSHOT) && (weapon != null && weapon.getItemType() != L2WeaponType.DAGGER);
			
			if (!crit && (skill.getCondition() & L2Skill.COND_CRIT) != 0)
				damage = 0;
			else
				damage = (int) Formulas.calcPhysDam(activeChar, target, skill, shld, false, dual, soul);
			if (crit)
				damage *= 2; // PDAM Critical damage always 2x and not affected
			// by buffs
			
			if (soul && weapon != null)
			    activeChar.setChargedShot(ShotType.SOULSHOT, false);
		
			boolean skillIsEvaded = Formulas.calcPhysicalSkillEvasion(target, skill);
			
			if (!skillIsEvaded)
			{
				if (damage > 0)
				{
					activeChar.sendDamageMessage(target, damage, false, crit, false);
					
					final byte reflect = Formulas.calcSkillReflect(target, skill);

					if (skill.hasEffects())
					{
						L2Effect[] effects;
						if ((reflect & 1) != 0)
						{
							activeChar.stopSkillEffects(skill.getId());
							effects = skill.getEffects(target, activeChar);
							if (effects != null && effects.length > 0)
								activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill));
						}
						else
						{
							target.stopSkillEffects(skill.getId());
							if (Formulas.calcSkillSuccess(activeChar, target, skill, false, false, false))
							{
								skill.getEffects(activeChar, target);
								target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill.getId()));
							}
							else
								activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addString(target.getName()).addSkillName(skill.getDisplayId()));
						}
					}
					
					// Success of lethal effect
					int chance = Rnd.get(100);
					if (!target.isRaid() && !target.isBoss() && !(target instanceof L2GrandBossInstance) && chance < skill.getLethalChance1() && !(target instanceof L2DoorInstance) && !(target instanceof L2MonsterInstance && ((L2MonsterInstance) target).getNpcId() == 36006) && (target instanceof L2Npc && ((L2Npc) target).getNpcId() != 35062))
					{
						// 1st lethal effect activate (cp to 1 or if target is
						// npc then hp to 50%)
						if (skill.getLethalChance2() > 0 && chance >= skill.getLethalChance2())
						{
							if (target.isPlayer())
							{
								L2PcInstance player = (L2PcInstance) target;
								if (!player.isInvul())
								{
									player.setCurrentCp(1); // Set CP to 1
									player.reduceCurrentHp(damage, activeChar);
								}
							}
							else if (target instanceof L2MonsterInstance) // If is a monster remove first damage and after 50% of current hp
							{
								target.reduceCurrentHp(damage, activeChar);
								target.reduceCurrentHp(target.getCurrentHp() / 2, activeChar);
							}
						}
						else
						// 2nd lethal effect activate (cp,hp to 1 or if target is npc then hp to 1)
						{
							// If is a monster damage is (CurrentHp - 1) so HP = 1
							if (target instanceof L2Npc)
								target.reduceCurrentHp(target.getCurrentHp() - 1, activeChar);
							else if (target.isPlayer()) // If is a active player set his HP and CP to 1
							{
								L2PcInstance player = (L2PcInstance) target;
								if (!player.isInvul())
								{
									player.setCurrentHp(1);
									player.setCurrentCp(1);
								}
							}
						}
						// Lethal Strike was succefful!
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.LETHAL_STRIKE_SUCCESSFUL));
					}
					else
					{
						// Make damage directly to HP
						if (skill.getDmgDirectlyToHP())
						{
							if (target instanceof L2PcInstance)
							{
								L2PcInstance player = (L2PcInstance) target;
								if (!player.isInvul())
								{
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
								
								player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_GAVE_YOU_S2_DMG).addString(activeChar.getName()).addNumber(damage));
							}
							
							else
								target.reduceCurrentHp(damage, activeChar);
						}
						else
							target.reduceCurrentHp(damage, activeChar);
					}
					
					if ((reflect & 2) != 0)
					{
						if (target.isPlayer())
							target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.COUNTERED_S1_ATTACK).addCharName(activeChar));
						
						if (activeChar.isPlayer())
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_PERFORMING_COUNTERATTACK).addCharName(target));
						
						double vdamage = (700 * target.getPAtk(activeChar) / activeChar.getPDef(target));
						activeChar.reduceCurrentHp(vdamage, target);
					}
				}
				else
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ATTACK_FAILED));
				
				if (skill.getId() == 345 || skill.getId() == 346) // Sonic Rage or Raging Force
				{
					EffectCharge effect = (EffectCharge) activeChar.getFirstEffect(L2Effect.EffectType.CHARGE);
					if (effect != null)
					{
						int effectcharge = effect.getLevel();
						if (effectcharge < 7)
						{
							effectcharge++;
							effect.addNumCharges(1);
							if (activeChar instanceof L2PcInstance)
							{
								activeChar.sendPacket(new EtcStatusUpdate((L2PcInstance) activeChar));
								activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FORCE_INCREASED_TO_S1).addNumber(effectcharge));
							}
						}
						else
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FORCE_MAXLEVEL_REACHED));
					}
					else
					{
						if (skill.getId() == 345) // Sonic Rage
						{
							L2Skill dummy = SkillTable.getInstance().getInfo(8, 7); // Lv7 Sonic Focus
							dummy.getEffects(activeChar, activeChar);
						}
						else if (skill.getId() == 346) // Raging Force
						{
							L2Skill dummy = SkillTable.getInstance().getInfo(50, 7); // Lv7 Focused Force
							dummy.getEffects(activeChar, activeChar);
						}
					}
				}
				// self Effect :]
				if (skill.hasSelfEffects())
				{
					final L2Effect effect = activeChar.getFirstEffect(skill.getId());
					if (effect != null && effect.isSelfEffect())
						effect.exit();
					
					skill.getEffectsSelf(activeChar);
				}
			}
			
			if (skill.isSuicideAttack())
				activeChar.doDie(activeChar);
		}
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}