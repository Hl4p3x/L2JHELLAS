package com.l2jhellas.gameserver.handlers.skillhandlers;

import java.io.IOException;

import com.l2jhellas.gameserver.ai.CtrlEvent;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.ai.L2AttackableAI;
import com.l2jhellas.gameserver.enums.skills.L2SkillTargetType;
import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.handler.ISkillHandler;
import com.l2jhellas.gameserver.handler.SkillHandler;
import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2SiegeSummonInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.model.base.Experience;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.Formulas;
import com.l2jhellas.gameserver.skills.Stats;
import com.l2jhellas.gameserver.skills.funcs.Func;
import com.l2jhellas.util.Rnd;

public class Disablers implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.STUN,
		L2SkillType.ROOT,
		L2SkillType.SLEEP,
		L2SkillType.CONFUSION,
		L2SkillType.AGGDAMAGE,
		L2SkillType.AGGREDUCE,
		L2SkillType.AGGREDUCE_CHAR,
		L2SkillType.AGGREMOVE,
		L2SkillType.UNBLEED,
		L2SkillType.UNPOISON,
		L2SkillType.MUTE,
		L2SkillType.FAKE_DEATH,
		L2SkillType.CONFUSE_MOB_ONLY,
		L2SkillType.NEGATE,
		L2SkillType.CANCEL,
		L2SkillType.PARALYZE,
		L2SkillType.ERASE,
		L2SkillType.MAGE_BANE,
		L2SkillType.WARRIOR_BANE,
		L2SkillType.BETRAY
	};
	
	private String[] _negateStats = null;
	private final float _negatePower = 0.f;
	private int _negateId = 0;

	private L2Character target;
	
	@Override
	@SuppressWarnings("incomplete-switch")
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		L2SkillType type = skill.getSkillType();
		
		boolean ss = false;
		boolean sps = false;
		boolean bss = false;
		
		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		
		if (activeChar instanceof L2PcInstance)
		{
			if (weaponInst == null && skill.isOffensive())
			{
				SystemMessage sm2 = SystemMessage.getSystemMessage(SystemMessageId.S1_S2);
				sm2.addString("You must equip a weapon before casting a spell.");
				activeChar.sendPacket(sm2);
				return;
			}
		}
		
		if (weaponInst != null)
		{
			if (skill.isMagic())
			{
				if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
				{
					bss = true;
					if (skill.getId() != 1020) // vitalize
					{
						weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
					}
				}
				else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
				{
					sps = true;
					if (skill.getId() != 1020) // vitalize
					{
						weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
					}
				}
			}
			else if (weaponInst.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT)
			{
				ss = true;
				if (skill.getId() != 1020) // vitalize
				{
					weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
				}
			}
		}
		// If there is no weapon equipped, check for an active summon.
		else if (activeChar instanceof L2Summon)
		{
			L2Summon activeSummon = (L2Summon) activeChar;
			
			if (skill.isMagic())
			{
				if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
				{
					bss = true;
					activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
				}
				else if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT)
				{
					sps = true;
					activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
				}
			}
			else if (activeSummon.getChargedSoulShot() == L2ItemInstance.CHARGED_SOULSHOT)
			{
				ss = true;
				activeSummon.setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
			}
		}
		
		for (int index = 0; index < targets.length; index++)
		{
			// Get a target
			if (!(targets[index] instanceof L2Character))
			{
				continue;
			}
			
			target = (L2Character) targets[index];
			
			if (target == null || target.isDead()) // bypass if target is null or dead
			{
				continue;
			}
			
			switch (type)
			{
				case BETRAY:
				{
					if (Formulas.calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
					{
						skill.getEffects(activeChar, target);
					}
					else
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2);
						sm.addString(target.getName());
						sm.addSkillName(skill.getId());
						activeChar.sendPacket(sm);
					}
					break;
				}
				case FAKE_DEATH:
				{
					// stun/fakedeath is not mdef dependant, it depends on lvl
					// difference, target CON and power of stun
					skill.getEffects(activeChar, target);
					break;
				}
				case ROOT:
				case STUN:
				{
					if (Formulas.calcSkillReflect(target, skill) == 1)
						target = activeChar;
					
					if (Formulas.calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
						skill.getEffects(activeChar, target);
					else
					{
						if (activeChar instanceof L2PcInstance)
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2);
							sm.addString(target.getName());
							sm.addSkillName(skill.getDisplayId());
							activeChar.sendPacket(sm);
						}
					}
					break;
				}
				case SLEEP:
				case PARALYZE: // use same as root for now
				{
					if (Formulas.calcSkillReflect(target, skill) == 1)
						target = activeChar;
					
					if (Formulas.calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
					{
						skill.getEffects(activeChar, target);
					}
					else
					{
						if (activeChar instanceof L2PcInstance)
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2);
							sm.addString(target.getName());
							sm.addSkillName(skill.getDisplayId());
							activeChar.sendPacket(sm);
						}
					}
					break;
				}
				case CONFUSION:
				case MUTE:
				{
					if (Formulas.calcSkillReflect(target, skill) == 1)
						target = activeChar;
					
					if (Formulas.calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
					{
						// stop same type effect if avaiable
						L2Effect[] effects = target.getAllEffects();
						for (L2Effect e : effects)
							if (e.getSkill().getSkillType() == type)
							{
								e.exit();
							}
						// then restart
						// Make above skills mdef dependant
						if (Formulas.calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
						{
							// if(Formulas.getInstance().calcMagicAffected(activeChar,
							// target, skill))
							skill.getEffects(activeChar, target);
						}
						else
						{
							if (activeChar instanceof L2PcInstance)
							{
								SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2);
								sm.addString(target.getName());
								sm.addSkillName(skill.getDisplayId());
								activeChar.sendPacket(sm);
							}
						}
					}
					else
					{
						if (activeChar instanceof L2PcInstance)
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2);
							sm.addString(target.getName());
							sm.addSkillName(skill.getDisplayId());
							activeChar.sendPacket(sm);
						}
					}
					break;
				}
				case CONFUSE_MOB_ONLY:
				{
					if (Formulas.calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
					{
						L2Effect[] effects = target.getAllEffects();
						for (L2Effect e : effects)
						{
							if (e.getSkill().getSkillType() == type)
							{
								e.exit();
							}
						}
						skill.getEffects(activeChar, target);
					}
					else
					{
						if (activeChar instanceof L2PcInstance)
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2);
							sm.addString(target.getName());
							sm.addSkillName(skill.getId());
							activeChar.sendPacket(sm);
						}
					}
					break;
				}
				case AGGDAMAGE:
				{
					if (target instanceof L2Attackable)
					{
						target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, (int) ((150 * skill.getPower()) / (target.getLevel() + 7)));
					}
					skill.getEffects(activeChar, target);
					break;
				}
				case AGGREDUCE:
				{
					// these skills needs to be rechecked
					if (target instanceof L2Attackable)
					{
						skill.getEffects(activeChar, target);
						
						double aggdiff = ((L2Attackable) target).getHating(activeChar) - target.calcStat(Stats.AGGRESSION, ((L2Attackable) target).getHating(activeChar), target, skill);
						
						if (skill.getPower() > 0)
						{
							((L2Attackable) target).reduceHate(null, (int) skill.getPower());
						}
						else if (aggdiff > 0)
						{
							((L2Attackable) target).reduceHate(null, (int) aggdiff);
						}
					}
					break;
				}
				case AGGREDUCE_CHAR:
				{
					// these skills needs to be rechecked
					if (Formulas.calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
					{
						if (target instanceof L2Attackable)
						{
							L2Attackable targ = (L2Attackable) target;
							targ.stopHating(activeChar);
							if (targ.getMostHated() == null)
							{
								((L2AttackableAI) targ.getAI()).setGlobalAggro(-25);
								targ.clearAggroList();
								targ.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
								targ.setWalking();
							}
						}
						skill.getEffects(activeChar, target);
					}
					else
					{
						if (activeChar instanceof L2PcInstance)
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2);
							sm.addString(target.getName());
							sm.addSkillName(skill.getId());
							activeChar.sendPacket(sm);
						}
					}
					break;
				}
				case AGGREMOVE:
				{
					// these skills needs to be rechecked
					if (target instanceof L2Attackable && !target.isRaid() && !target.isBoss())
					{
						if (Formulas.calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
						{
							if (skill.getTargetType() == L2SkillTargetType.TARGET_UNDEAD)
							{
								if (target.isUndead())
								{
									((L2Attackable) target).reduceHate(null, ((L2Attackable) target).getHating(((L2Attackable) target).getMostHated()));
								}
							}
							else
							{
								((L2Attackable) target).reduceHate(null, ((L2Attackable) target).getHating(((L2Attackable) target).getMostHated()));
							}
						}
						else
						{
							if (activeChar instanceof L2PcInstance)
							{
								SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2);
								sm.addString(target.getName());
								sm.addSkillName(skill.getId());
								activeChar.sendPacket(sm);
							}
						}
					}
					break;
				}
				case UNBLEED:
				{
					negateEffect(target, L2SkillType.BLEED, skill.getNegateLvl());
					break;
				}
				case UNPOISON:
				{
					negateEffect(target, L2SkillType.POISON, skill.getNegateLvl());
					break;
				}
				case ERASE:
				{
					if (Formulas.calcSkillSuccess(activeChar, target, skill, ss, sps, bss)
					// doesn't affect siege golem or wild hog cannon
					&& !(target instanceof L2SiegeSummonInstance))
					{
						L2PcInstance summonOwner = null;
						L2Summon summonPet = null;
						summonOwner = ((L2Summon) target).getOwner();
						summonPet = summonOwner.getPet();
						summonPet.unSummon(summonOwner);
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.LETHAL_STRIKE);
						summonOwner.sendPacket(sm);
					}
					else
					{
						if (activeChar instanceof L2PcInstance)
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2);
							sm.addString(target.getName());
							sm.addSkillName(skill.getId());
							activeChar.sendPacket(sm);
						}
					}
					break;
				}
				case MAGE_BANE:
				{
					for (L2Object t : targets)
					{
						L2Character target1 = (L2Character) t;

						if (Formulas.calcSkillReflect(target1, skill) == 1)
							target1 = activeChar;
						
						if (!Formulas.calcSkillSuccess(activeChar, target1, skill, ss, sps, bss))
						{
							continue;
						}
						
						L2Effect[] effects = target1.getAllEffects();
						for (L2Effect e : effects)
						{
							for (Func f : e.getStatFuncs())
							{
								if (f.stat == Stats.MAGIC_ATTACK || f.stat == Stats.MAGIC_ATTACK_SPEED)
								{
									e.exit();
									break;
								}
							}
						}
					}
					break;
				}
				case WARRIOR_BANE:
				{
					for (L2Object t : targets)
					{
						L2Character target1 = (L2Character) t;
						
						if (Formulas.calcSkillReflect(target1, skill) == 1)
							target1 = activeChar;
						
						if (!Formulas.calcSkillSuccess(activeChar, target1, skill, ss, sps, bss))
						{
							continue;
						}
						
						L2Effect[] effects = target1.getAllEffects();
						for (L2Effect e : effects)
						{
							for (Func f : e.getStatFuncs())
							{
								if (f.stat == Stats.RUN_SPEED || f.stat == Stats.POWER_ATTACK_SPEED)
								{
									e.exit();
									break;
								}
							}
						}
					}
					break;
				}
				case CANCEL:
				case NEGATE:
				{
					if (Formulas.calcSkillReflect(target, skill) == 1)
						target = activeChar;
					
					// cancel
					if (skill.getId() == 1056)
					{
						int lvlmodifier = 52 + skill.getMagicLevel() * 2;
						if (skill.getMagicLevel() == 12)
						{
							lvlmodifier = (Experience.MAX_LEVEL - 1);
						}
						int landrate = 90;
						if ((target.getLevel() - lvlmodifier) > 0)
						{
							landrate = 90 - 4 * (target.getLevel() - lvlmodifier);
						}
						
						landrate = (int) activeChar.calcStat(Stats.CANCEL_VULN, landrate, target, null);
						
						if (Rnd.get(100) < landrate)
						{
							L2Effect[] effects = target.getAllEffects();
							int maxfive = 5;
							for (L2Effect e : effects)
							{
								// do not delete signet effects!
								switch (e.getEffectType())
								{
									case SIGNET:
									case SIGNET_GROUND:
									case SIGNET_EFFECT:
										continue;
								}
								
								if (e.getSkill().getId() != 4082 && e.getSkill().getId() != 4215 && e.getSkill().getId() != 5182 && e.getSkill().getId() != 4515 && e.getSkill().getId() != 110 && e.getSkill().getId() != 111 && e.getSkill().getId() != 1323 && e.getSkill().getId() != 1325) // Cannot
								// cancel
								// skills
								// 4082,
								// 4215,
								// 4515,
								// 110, 111,
								// 1323,
								// 1325
								{
									if (e.getSkill().getSkillType() != L2SkillType.BUFF)
									{
										e.exit();
									}
									else
									{
										int rate = 100;
										int level = e.getLevel();
										if (level > 0)
										{
											rate = Integer.valueOf(150 / (1 + level));
										}
										if (rate > 95)
										{
											rate = 95;
										}
										else if (rate < 5)
										{
											rate = 5;
										}
										if (Rnd.get(100) < rate)
										{
											e.exit();
											maxfive--;
											if (maxfive == 0)
											{
												break;
											}
										}
									}
								}
							}
						}
						else
						{
							if (activeChar instanceof L2PcInstance)
							{
								SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2);
								sm.addString(target.getName());
								sm.addSkillName(skill.getDisplayId());
								activeChar.sendPacket(sm);
							}
						}
						break;
					}
					// fishing potion
					else if (skill.getId() == 2275)
					{
						_negateId = skill.getNegateId();
						
						negateEffect(target, L2SkillType.BUFF, _negatePower, _negateId);
					}
					// all others negate type skills
					else
					{
						_negateStats = skill.getNegateStats();
						
						for (String stat : _negateStats)
						{
							stat = stat.toLowerCase().intern();
							if (stat == "buff")
							{
								int lvlmodifier = 52 + skill.getMagicLevel() * 2;
								if (skill.getMagicLevel() == 12)
								{
									lvlmodifier = (Experience.MAX_LEVEL - 1);
								}
								int landrate = 90;
								if ((target.getLevel() - lvlmodifier) > 0)
								{
									landrate = 90 - 4 * (target.getLevel() - lvlmodifier);
								}
								
								landrate = (int) activeChar.calcStat(Stats.CANCEL_VULN, landrate, target, null);
								
								if (Rnd.get(100) < landrate)
								{
									negateEffect(target, L2SkillType.BUFF, -1);
								}
							}
							if (stat == "debuff")
							{
								negateEffect(target, L2SkillType.DEBUFF, -1);
							}
							if (stat == "weakness")
							{
								negateEffect(target, L2SkillType.WEAKNESS, -1);
							}
							if (stat == "stun")
							{
								negateEffect(target, L2SkillType.STUN, -1);
							}
							if (stat == "sleep")
							{
								negateEffect(target, L2SkillType.SLEEP, -1);
							}
							if (stat == "confusion")
							{
								negateEffect(target, L2SkillType.CONFUSION, -1);
							}
							if (stat == "mute")
							{
								negateEffect(target, L2SkillType.MUTE, -1);
							}
							if (stat == "fear")
							{
								negateEffect(target, L2SkillType.FEAR, -1);
							}
							if (stat == "poison")
							{
								negateEffect(target, L2SkillType.POISON, skill.getNegateLvl());
							}
							if (stat == "bleed")
							{
								negateEffect(target, L2SkillType.BLEED, skill.getNegateLvl());
							}
							if (stat == "paralyze")
							{
								negateEffect(target, L2SkillType.PARALYZE, -1);
							}
							if (stat == "root")
							{
								negateEffect(target, L2SkillType.ROOT, -1);
							}
							if (stat == "heal")
							{
								ISkillHandler Healhandler = SkillHandler.getInstance().getHandler(L2SkillType.HEAL);
								if (Healhandler == null)
								{
									System.out.println("Couldn't find skill handler for HEAL.");
									continue;
								}
								L2Object tgts[] = new L2Object[]
								{
									target
								};
								try
								{
									Healhandler.useSkill(activeChar, skill, tgts);
								}
								catch (IOException e)
								{
									e.printStackTrace();
								}
							}
						}// end for
					}// end else
				}// end case
			}// end switch
		}// end for
		
		// self Effect :]
		if (skill.hasSelfEffects())
		{
			final L2Effect effect = activeChar.getFirstEffect(skill.getId());
			if (effect != null && effect.isSelfEffect())
				effect.exit();
			
			skill.getEffectsSelf(activeChar);
		}
		
	} // end void
	
	private static void negateEffect(L2Character target, L2SkillType type, double power)
	{
		negateEffect(target, type, power, 0);
	}
	
	private static void negateEffect(L2Character target, L2SkillType type, double power, int skillId)
	{
		L2Effect[] effects = target.getAllEffects();
		for (L2Effect e : effects)
			if (e.getSkill().getSkillType() == type || (e.getSkill().getEffectType() != null && e.getSkill().getEffectType() == type))
			{
				if (power == -1) // if power is -1 the effect is always removed
				// without power/lvl check ^^
				{
					if (skillId != 0)
					{
						if (skillId == e.getSkill().getId())
						{
							e.exit();
						}
					}
					else
					{
						e.exit();
					}
				}
				else if ((e.getSkill().getAbnormal() != 0 && e.getSkill().getAbnormal() <= power) || (e.getSkill().getEffectLvl() != 0 && e.getSkill().getEffectLvl() <= power))
				{
					if (skillId != 0)
					{
						if (skillId == e.getSkill().getId())
						{
							e.exit();
						}
					}
					else
					{
						e.exit();
					}
				}
			}
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}