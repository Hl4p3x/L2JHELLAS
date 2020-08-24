package com.l2jhellas.gameserver.skills;

import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.controllers.GameTimeController;
import com.l2jhellas.gameserver.enums.ZoneId;
import com.l2jhellas.gameserver.enums.items.L2WeaponType;
import com.l2jhellas.gameserver.enums.player.Position;
import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.instancemanager.ClanHallManager;
import com.l2jhellas.gameserver.instancemanager.SiegeManager;
import com.l2jhellas.gameserver.instancemanager.ZoneManager;
import com.l2jhellas.gameserver.model.L2SiegeClan;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PetInstance;
import com.l2jhellas.gameserver.model.entity.ClanHall;
import com.l2jhellas.gameserver.model.entity.Siege;
import com.l2jhellas.gameserver.model.zone.type.L2MotherTreeZone;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.funcsCalc.FuncAtkAccuracy;
import com.l2jhellas.gameserver.skills.funcsCalc.FuncAtkCritical;
import com.l2jhellas.gameserver.skills.funcsCalc.FuncAtkEvasion;
import com.l2jhellas.gameserver.skills.funcsCalc.FuncBowAtkRange;
import com.l2jhellas.gameserver.skills.funcsCalc.FuncHennaCON;
import com.l2jhellas.gameserver.skills.funcsCalc.FuncHennaDEX;
import com.l2jhellas.gameserver.skills.funcsCalc.FuncHennaINT;
import com.l2jhellas.gameserver.skills.funcsCalc.FuncHennaMEN;
import com.l2jhellas.gameserver.skills.funcsCalc.FuncHennaSTR;
import com.l2jhellas.gameserver.skills.funcsCalc.FuncHennaWIT;
import com.l2jhellas.gameserver.skills.funcsCalc.FuncMAtkCritical;
import com.l2jhellas.gameserver.skills.funcsCalc.FuncMAtkMod;
import com.l2jhellas.gameserver.skills.funcsCalc.FuncMAtkSpeed;
import com.l2jhellas.gameserver.skills.funcsCalc.FuncMDefMod;
import com.l2jhellas.gameserver.skills.funcsCalc.FuncMaxCpAdd;
import com.l2jhellas.gameserver.skills.funcsCalc.FuncMaxCpMul;
import com.l2jhellas.gameserver.skills.funcsCalc.FuncMaxHpAdd;
import com.l2jhellas.gameserver.skills.funcsCalc.FuncMaxHpMul;
import com.l2jhellas.gameserver.skills.funcsCalc.FuncMaxMpAdd;
import com.l2jhellas.gameserver.skills.funcsCalc.FuncMaxMpMul;
import com.l2jhellas.gameserver.skills.funcsCalc.FuncMoveSpeed;
import com.l2jhellas.gameserver.skills.funcsCalc.FuncPAtkMod;
import com.l2jhellas.gameserver.skills.funcsCalc.FuncPAtkSpeed;
import com.l2jhellas.gameserver.skills.funcsCalc.FuncPDefMod;
import com.l2jhellas.gameserver.templates.L2Armor;
import com.l2jhellas.gameserver.templates.L2Weapon;
import com.l2jhellas.util.Rnd;
import com.l2jhellas.util.Util;

public final class Formulas
{	
	protected static final Logger _log = Logger.getLogger(L2Character.class.getName());
	private static final int HP_REGENERATE_PERIOD = 3000; // 3 secs
	public static final int MAX_STAT_VALUE = 100;
	
	private static final double[] STRCompute = new double[]
	{
		1.036,
		34.845
	}; // {1.016, 28.515}; for C1
	private static final double[] INTCompute = new double[]
	{
		1.020,
		31.375
	}; // {1.020, 31.375}; for C1
	private static final double[] DEXCompute = new double[]
	{
		1.009,
		19.360
	}; // {1.009, 19.360}; for C1
	private static final double[] WITCompute = new double[]
	{
		1.050,
		20.000
	}; // {1.050, 20.000}; for C1
	private static final double[] CONCompute = new double[]
	{
		1.030,
		27.632
	}; // {1.015, 12.488}; for C1
	private static final double[] MENCompute = new double[]
	{
		1.010,
		-0.060
	}; // {1.010, -0.060}; for C1
	
	public static final double[] WITbonus = new double[MAX_STAT_VALUE];
	public static final double[] MENbonus = new double[MAX_STAT_VALUE];
	public static final double[] INTbonus = new double[MAX_STAT_VALUE];
	public static final double[] STRbonus = new double[MAX_STAT_VALUE];
	public static final double[] DEXbonus = new double[MAX_STAT_VALUE];
	public static final double[] CONbonus = new double[MAX_STAT_VALUE];
	
	// These values are 100% matching retail tables, no need to change and no need add
	// calculation into the stat bonus when accessing (not efficient),
	// better to have everything precalculated and use values directly (saves CPU)
	static
	{
		for (int i = 0; i < STRbonus.length; i++)
			STRbonus[i] = Math.floor(Math.pow(STRCompute[0], i - STRCompute[1]) * 100 + .5d) / 100;
		for (int i = 0; i < INTbonus.length; i++)
			INTbonus[i] = Math.floor(Math.pow(INTCompute[0], i - INTCompute[1]) * 100 + .5d) / 100;
		for (int i = 0; i < DEXbonus.length; i++)
			DEXbonus[i] = Math.floor(Math.pow(DEXCompute[0], i - DEXCompute[1]) * 100 + .5d) / 100;
		for (int i = 0; i < WITbonus.length; i++)
			WITbonus[i] = Math.floor(Math.pow(WITCompute[0], i - WITCompute[1]) * 100 + .5d) / 100;
		for (int i = 0; i < CONbonus.length; i++)
			CONbonus[i] = Math.floor(Math.pow(CONCompute[0], i - CONCompute[1]) * 100 + .5d) / 100;
		for (int i = 0; i < MENbonus.length; i++)
			MENbonus[i] = Math.floor(Math.pow(MENCompute[0], i - MENCompute[1]) * 100 + .5d) / 100;
	}

	private static final Formulas _instance = new Formulas();
	
	public static Formulas getInstance()
	{
		return _instance;
	}
	
	private Formulas()
	{
	}
	
	public static int getRegeneratePeriod(L2Character cha)
	{
		return (cha instanceof L2DoorInstance) ? HP_REGENERATE_PERIOD * 100 : HP_REGENERATE_PERIOD;
	}
	
	public static Calculator[] getStdNPCCalculators()
	{
		Calculator[] std = new Calculator[Stats.NUM_STATS];
		
		// Add the FuncAtkAccuracy to the Standard Calculator of ACCURACY_COMBAT
		std[Stats.ACCURACY_COMBAT.ordinal()] = new Calculator();
		std[Stats.ACCURACY_COMBAT.ordinal()].addFunc(FuncAtkAccuracy.getInstance());
		
		// Add the FuncAtkEvasion to the Standard Calculator of EVASION_RATE
		std[Stats.EVASION_RATE.ordinal()] = new Calculator();
		std[Stats.EVASION_RATE.ordinal()].addFunc(FuncAtkEvasion.getInstance());
		
		return std;
	}
	
	public static void addFuncsToNewSummon(L2Summon summon)
	{
		summon.addStatFunc(FuncMaxHpMul.getInstance());
		summon.addStatFunc(FuncMaxMpMul.getInstance());
		summon.addStatFunc(FuncPAtkMod.getInstance());
		summon.addStatFunc(FuncMAtkMod.getInstance());
		summon.addStatFunc(FuncPDefMod.getInstance());
		summon.addStatFunc(FuncMDefMod.getInstance());
		summon.addStatFunc(FuncAtkCritical.getInstance());
		summon.addStatFunc(FuncMAtkCritical.getInstance());
		summon.addStatFunc(FuncAtkAccuracy.getInstance());
		summon.addStatFunc(FuncAtkEvasion.getInstance());
		summon.addStatFunc(FuncMoveSpeed.getInstance());
		summon.addStatFunc(FuncPAtkSpeed.getInstance());
		summon.addStatFunc(FuncMAtkSpeed.getInstance());
	}
	
	
	public static void addFuncsToNewPlayer(L2PcInstance player)
	{
		player.addStatFunc(FuncMaxHpAdd.getInstance());
		player.addStatFunc(FuncMaxHpMul.getInstance());
		player.addStatFunc(FuncMaxCpAdd.getInstance());
		player.addStatFunc(FuncMaxCpMul.getInstance());
		player.addStatFunc(FuncMaxMpAdd.getInstance());
		player.addStatFunc(FuncMaxMpMul.getInstance());
		// cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_HP_RATE));
		// cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_CP_RATE));
		// cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_MP_RATE));
		player.addStatFunc(FuncBowAtkRange.getInstance());
		// cha.addStatFunc(FuncMultLevelMod.getInstance(Stats.POWER_ATTACK));
		// cha.addStatFunc(FuncMultLevelMod.getInstance(Stats.POWER_DEFENCE));
		// cha.addStatFunc(FuncMultLevelMod.getInstance(Stats.MAGIC_DEFENCE));
		player.addStatFunc(FuncPAtkMod.getInstance());
		player.addStatFunc(FuncMAtkMod.getInstance());
		player.addStatFunc(FuncPDefMod.getInstance());
		player.addStatFunc(FuncMDefMod.getInstance());
		player.addStatFunc(FuncAtkCritical.getInstance());
		player.addStatFunc(FuncMAtkCritical.getInstance());
		player.addStatFunc(FuncAtkAccuracy.getInstance());
		player.addStatFunc(FuncAtkEvasion.getInstance());
		player.addStatFunc(FuncPAtkSpeed.getInstance());
		player.addStatFunc(FuncMAtkSpeed.getInstance());
		player.addStatFunc(FuncMoveSpeed.getInstance());
		
		player.addStatFunc(FuncHennaSTR.getInstance());
		player.addStatFunc(FuncHennaDEX.getInstance());
		player.addStatFunc(FuncHennaINT.getInstance());
		player.addStatFunc(FuncHennaMEN.getInstance());
		player.addStatFunc(FuncHennaCON.getInstance());
		player.addStatFunc(FuncHennaWIT.getInstance());
	}
	
	public final static double calcHpRegen(L2Character cha)
	{
		double init = cha.getTemplate().baseHpReg;
		double hpRegenMultiplier = cha.isRaid() || cha.isBoss() ? Config.RAID_HP_REGEN_MULTIPLIER : Config.HP_REGEN_MULTIPLIER;
		double hpRegenBonus = 0;
		
		if (cha.isChampion())
			hpRegenMultiplier *= Config.CHAMPION_HP_REGEN;
		
		if (cha instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) cha;
			
			// Calculate correct baseHpReg value for certain level of PC
			init += (player.getLevel() > 10) ? ((player.getLevel() - 1) / 10.0) : 0.5;

			double siegeModifier = calcSiegeRegenModifer(player);
			if (siegeModifier > 0)
				hpRegenMultiplier *= siegeModifier;
			
			if (player.isInsideZone(ZoneId.CLAN_HALL) && player.getClan() != null)
			{
				int clanHallIndex = player.getClan().hasHideout();
				if (clanHallIndex > 0)
				{
					ClanHall clansHall = ClanHallManager.getInstance().getClanHallById(clanHallIndex);
					if (clansHall != null)
						if (clansHall.getFunction(ClanHall.FUNC_RESTORE_HP) != null)
							hpRegenMultiplier *= 1 + (double) clansHall.getFunction(ClanHall.FUNC_RESTORE_HP).getLvl() / 100;
				}
			}
			
			// Mother Tree effect is calculated at last
			if (player.isInsideZone(ZoneId.MOTHER_TREE))
			{
				L2MotherTreeZone zone = ZoneManager.getInstance().getZone(player, L2MotherTreeZone.class);
				int hpBonus = zone == null ? 0 : zone.getHpRegenBonus();
				hpRegenBonus += hpBonus;
			}
			
			// Calculate Movement bonus
			if (player.isSitting())
				hpRegenMultiplier *= 1.5; // Sitting
			else if (!player.isMoving())
				hpRegenMultiplier *= 1.1; // Staying
			else if (player.isRunning())
				hpRegenMultiplier *= 0.7; // Running
				
			// Add CON bonus
			init *= cha.getLevelMod() * CONbonus[cha.getCON()];
		}
		else if (cha instanceof L2PetInstance)
			init = ((L2PetInstance) cha).getPetData().getPetRegenHP();
		
		if (init < 1)
			init = 1;
		
		return cha.calcStat(Stats.REGENERATE_HP_RATE, init, null, null) * hpRegenMultiplier + hpRegenBonus;
	}
	
	public final static double calcMpRegen(L2Character cha)
	{
		double init = cha.getTemplate().baseMpReg;
		double mpRegenMultiplier = cha.isRaid() || cha.isBoss() ? Config.RAID_MP_REGEN_MULTIPLIER : Config.MP_REGEN_MULTIPLIER;
		double mpRegenBonus = 0;
		
		if (cha instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) cha;
			
			// Calculate correct baseMpReg value for certain level of PC
			init += 0.3 * ((player.getLevel() - 1) / 10.0);

			// Mother Tree effect is calculated at last
			if (player.isInsideZone(ZoneId.MOTHER_TREE))
			{
				L2MotherTreeZone zone = ZoneManager.getInstance().getZone(player, L2MotherTreeZone.class);
				int mpBonus = zone == null ? 0 : zone.getMpRegenBonus();
				mpRegenBonus += mpBonus;
			}
			
			if (player.isInsideZone(ZoneId.CLAN_HALL) && player.getClan() != null)
			{
				int clanHallIndex = player.getClan().hasHideout();
				if (clanHallIndex > 0)
				{
					ClanHall clansHall = ClanHallManager.getInstance().getClanHallById(clanHallIndex);
					if (clansHall != null)
						if (clansHall.getFunction(ClanHall.FUNC_RESTORE_MP) != null)
							mpRegenMultiplier *= 1 + clansHall.getFunction(ClanHall.FUNC_RESTORE_MP).getLvl() / 100;
				}
			}
			
			// Calculate Movement bonus
			if (player.isSitting())
				mpRegenMultiplier *= 1.5; // Sitting
			else if (!player.isMoving())
				mpRegenMultiplier *= 1.1; // Staying
			else if (player.isRunning())
				mpRegenMultiplier *= 0.7; // Running
				
			// Add MEN bonus
			init *= cha.getLevelMod() * MENbonus[cha.getMEN()];
		}
		else if (cha instanceof L2PetInstance)
			init = ((L2PetInstance) cha).getPetData().getPetRegenMP();
		
		if (init < 1)
			init = 1;
		
		return cha.calcStat(Stats.REGENERATE_MP_RATE, init, null, null) * mpRegenMultiplier + mpRegenBonus;
	}
	
	public final static double calcCpRegen(L2PcInstance player)
	{
		double init = player.getTemplate().baseHpReg + ((player.getLevel() > 10) ? ((player.getLevel() - 1) / 10.0) : 0.5);
		double cpRegenMultiplier = Config.CP_REGEN_MULTIPLIER;
		
		// Calculate Movement bonus
		if (player.isSitting())
			cpRegenMultiplier *= 1.5; // Sitting
		else if (!player.isMoving())
			cpRegenMultiplier *= 1.1; // Staying
		else if (player.isRunning())
			cpRegenMultiplier *= 0.7; // Running
			
		// Apply CON bonus
		init *= player.getLevelMod() * CONbonus[player.getCON()];
		
		if (init < 1)
			init = 1;
		
		return player.calcStat(Stats.REGENERATE_CP_RATE, init, null, null) * cpRegenMultiplier;
		
	}
	
	public final static double calcSiegeRegenModifer(L2PcInstance activeChar)
	{
		if (activeChar == null || activeChar.getClan() == null)
			return 0;
		
		Siege siege = SiegeManager.getSiege(activeChar.getPosition().getWorldPosition().getX(), activeChar.getPosition().getWorldPosition().getY(), activeChar.getPosition().getWorldPosition().getZ());
		if (siege == null || !siege.getIsInProgress())
			return 0;
		
		L2SiegeClan siegeClan = siege.getAttackerClan(activeChar.getClan().getClanId());
		if (siegeClan == null || siegeClan.getFlag().size() == 0 || !Util.checkIfInRange(200, activeChar, siegeClan.getFlag().get(0), true))
			return 0;
		
		return 1.5; // If all is true, then modifier will be 50% more
	}
	
	public static double calcBlowDamage(L2Character attacker, L2Character target, L2Skill skill, byte shld, boolean ss)
	{
		double defence = target.getPDef(attacker);
		switch (shld)
		{
			case 1:
				defence += target.getShldDef();
				break;
			
			case 2: // perfect block
				return 1;
		}

		double power = skill.getPower();
		double damage = 0;

		if (ss)
		{
			damage *= 2;
			
			if (skill.getSSBoost() > 0)
				power *= skill.getSSBoost();
		}
		
		damage += power;
		damage *= attacker.calcStat(Stats.CRITICAL_DAMAGE, 1, target, skill);
		damage += attacker.calcStat(Stats.CRITICAL_DAMAGE_ADD, 0, target, skill) * 6.5;
		damage *= target.calcStat(Stats.CRIT_VULN, 1, target, skill);

		// get the natural vulnerability for the template
		if (target instanceof L2Npc)
			damage *= ((L2Npc) target).getTemplate().getVulnerability(Stats.DAGGER_WPN_VULN);

		damage = target.calcStat(Stats.DAGGER_WPN_VULN, damage, target, null);
		damage *= 70 / defence;

		damage *= attacker.getRandomDamage();
		
		if (target.isPlayer())
		{
			final L2Armor armor = ((L2PcInstance) target).getActiveChestArmorItem();
			if (armor != null)
			{
				if (((L2PcInstance) target).isWearingHeavyArmor())
					damage /= 1.2; // 2
				// if(((L2PcInstance)target).isWearingLightArmor())
				// damage /= 1.1; // 1.5
				// if(((L2PcInstance)target).isWearingMagicArmor())
				// damage /= 1; // 1.3
			}
		}
		
		return Math.max(damage, 1);
	}
	
	public final static double calcPhysDam(L2Character attacker, L2Character target, L2Skill skill, byte shld, boolean crit, boolean dual, boolean ss)
	{
		if (attacker instanceof L2PcInstance)
		{
			L2PcInstance pcInst = (L2PcInstance) attacker;
			if (pcInst.isGM() && !pcInst.getAccessLevel().canGiveDamage())
				return 0;
		}
		
		double damage = attacker.getPAtk(target);
		double defence = target.getPDef(attacker);
		
		switch (shld)
		{
			case 1:
			{
				if (!Config.ALT_GAME_SHIELD_BLOCKS)
					defence += target.getShldDef();
				break;
			}
			case 2: // perfect block
				return 1;
		}
		
		if (ss)
			damage *= 2;
		if (skill != null)
		{
			double skillpower = skill.getPower(attacker);
			float ssboost = skill.getSSBoost();
			if (ssboost <= 0)
				damage += skillpower;
			else if (ssboost > 0)
			{
				if (ss)
				{
					skillpower *= ssboost;
					damage += skillpower;
				}
				else
					damage += skillpower;
			}
		}
		// Defense modifier depending of the attacker weapon
		L2Weapon weapon = attacker.getActiveWeaponItem();
		Stats stat = null;
		if (weapon != null)
		{
			switch (weapon.getItemType())
			{
				case BOW:
					stat = Stats.BOW_WPN_VULN;
					break;
				case BLUNT:
				case BIGBLUNT:
					stat = Stats.BLUNT_WPN_VULN;
					break;
				case DAGGER:
					stat = Stats.DAGGER_WPN_VULN;
					break;
				case DUAL:
					stat = Stats.DUAL_WPN_VULN;
					break;
				case DUALFIST:
					stat = Stats.DUALFIST_WPN_VULN;
					break;
				case ETC:
					stat = Stats.ETC_WPN_VULN;
					break;
				case FIST:
					stat = Stats.FIST_WPN_VULN;
					break;
				case POLE:
					stat = Stats.POLE_WPN_VULN;
					break;
				case SWORD:
					stat = Stats.SWORD_WPN_VULN;
					break;
				case BIGSWORD:
					stat = Stats.BIGSWORD_WPN_VULN;
					break;
				default :
					break;
			}
		}
		
		if (crit)
		{
			// Finally retail like formula
			damage = 2 * attacker.calcStat(Stats.CRITICAL_DAMAGE, 1, target, skill) * target.calcStat(Stats.CRIT_VULN, target.getTemplate().baseCritVuln, target, null) * (70 * damage / defence);
			// Crit dmg add is almost useless in normal hits...
			damage += (attacker.calcStat(Stats.CRITICAL_DAMAGE_ADD, 0, target, skill) * 70 / defence);
		}
		else
			damage = 70 * damage / defence;
		
		// In C5 summons make 10 % less dmg in PvP.
		if (attacker instanceof L2Summon && target instanceof L2PcInstance)
			damage *= 0.9;
		
		if (stat != null)
		{
			// get the vulnerability due to skills (buffs, passives, toggles, etc)
			damage = target.calcStat(stat, damage, target, null);
			if (target instanceof L2Npc)
			{
				// get the natural vulnerability for the template
				damage *= ((L2Npc) target).getTemplate().getVulnerability(stat);
			}
		}
		
		damage += Rnd.nextDouble() * damage / 10;

		if (target instanceof L2Npc)
		{
			switch (((L2Npc) target).getTemplate().getRace())
			{
				case UNDEAD:
					damage *= attacker.getPAtkUndead(target);
					break;
				case BEAST:
					damage *= attacker.getPAtkMonsters(target);
					break;
				case ANIMAL:
					damage *= attacker.getPAtkAnimals(target);
					break;
				case PLANT:
					damage *= attacker.getPAtkPlants(target);
					break;
				case DRAGON:
					damage *= attacker.getPAtkDragons(target);
					break;
				case BUG:
					damage *= attacker.getPAtkInsects(target);
					break;
				case GIANT:
					damage *= attacker.getPAtkGiants(target);
					break;
				default:
					// nothing
					break;
			}
		}		
		return Math.max(damage, 1);
	}
	
	public final static double calcMagicDam(L2Character attacker, L2Character target, L2Skill skill, boolean ss, boolean bss, boolean mcrit)
	{
		if (attacker instanceof L2PcInstance)
		{
			L2PcInstance pcInst = (L2PcInstance) attacker;
			if (pcInst.isGM() && !pcInst.getAccessLevel().canGiveDamage())
				return 0;
		}
		
		double mAtk = attacker.getMAtk(target, skill);
		double mDef = target.getMDef(attacker, skill);
		if (bss)
			mAtk *= 4;
		else if (ss)
			mAtk *= 2;
		
		double damage = 91 * Math.sqrt(mAtk) / mDef * skill.getPower(attacker) * calcSkillVulnerability(target, skill);
		
		// In C5 summons make 10 % less dmg in PvP.
		if (attacker instanceof L2Summon && target instanceof L2PcInstance)
			damage *= 0.9;
		
		// if(attacker instanceof L2PcInstance && target instanceof L2PcInstance) damage *= 0.9; // PvP modifier (-10%)
		
		// Failure calculation
		if (Config.ALT_GAME_MAGICFAILURES && !calcMagicSuccess(attacker, target, skill))
		{
			if (attacker instanceof L2PcInstance)
			{
				if (calcMagicSuccess(attacker, target, skill) && (target.getLevel() - attacker.getLevel()) <= 9)
				{
					if (skill.getSkillType() == L2SkillType.DRAIN)
						attacker.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DRAIN_HALF_SUCCESFUL));
					else
						attacker.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ATTACK_FAILED));
					
					damage /= 2;
				}
				else
				{
					attacker.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill));					
					damage = 1;
				}
			}
			
			if (target instanceof L2PcInstance)
			{
				if (skill.getSkillType() == L2SkillType.DRAIN)
					target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.RESISTED_S1_DRAIN).addCharName(attacker));
				else
					target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.RESISTED_S1_MAGIC).addCharName(attacker));
			}
		}
		else if (mcrit)
			damage *= 4;
		
		return damage;
	}

	public final static boolean calcCrit(double rate)
	{
		return rate > Rnd.get(1000);
	}
	
	public final static boolean calcBlow(L2Character activeChar, L2Character target, int chance)
	{
		return activeChar.calcStat(Stats.BLOW_RATE, chance * (1.0 + (activeChar.getDEX() - 20) / 100), target, null) > Rnd.get(100);
	}
	
	public final static double calcLethal(L2Character activeChar, L2Character target, int baseLethal)
	{
		return activeChar.calcStat(Stats.LETHAL_RATE, (baseLethal * ((double) activeChar.getLevel() / target.getLevel())), target, null);
	}
	
	public final static boolean calcMCrit(double mRate)
	{
		return mRate > Rnd.get(1000);
	}
	
	public final static boolean calcAtkBreak(L2Character target, double dmg)
	{
		if (target instanceof L2PcInstance)
		{
			if (((L2PcInstance) target).getForceBuff() != null)
				return true;
			
			if (target.isCastingNow() && target.getLastSkillCast() != null)
				if (target.getLastSkillCast().isCancelIfHit())
					return true;
		}
		
		double init = 0;
		
		if (Config.ALT_GAME_CANCEL_CAST && target.isCastingNow())
			init = 15;
		if (Config.ALT_GAME_CANCEL_BOW && target.isAttackingNow())
		{
			L2Weapon wpn = target.getActiveWeaponItem();
			if (wpn != null && wpn.getItemType() == L2WeaponType.BOW)
				init = 15;
		}
		
		if (target.isRaid() || target.isInvul() || init <= 0)
			return false; // No attack break
			
		// Chance of break is higher with higher dmg
		init += Math.sqrt(13 * dmg);
		
		// Chance is affected by target MEN
		init -= (MENbonus[target.getMEN()] * 100 - 100);
		
		// Calculate all modifiers for ATTACK_CANCEL
		double rate = target.calcStat(Stats.ATTACK_CANCEL, init, null, null);
		
		// Adjust the rate to be between 1 and 99
		rate = Math.max(Math.min(rate, 99), 1);
		
		return Rnd.get(100) < rate;
	}
	
	public static int calculateTimeBetweenAttacks(int attackSpeed)
	{
		return Math.max(50, (500000 / attackSpeed));
	}

	public final static int calcMAtkSpd(L2Character attacker, L2Skill skill, double skillTime)
	{
		return skill.isMagic()? (int) (skillTime * 333 / attacker.getMAtkSpd()) : (int) (skillTime * 333 / attacker.getPAtkSpd());
	}
	
	public static boolean calcHitMiss(L2Character attacker, L2Character target)
	{
		int chance = (80 + (2 * (attacker.getAccuracy() - target.getEvasionRate(attacker)))) * 10;
		
		double modifier = 100;
		
		// Get high or low Z bonus.
		if (attacker.getZ() - target.getZ() > 50)
			modifier += 3;
		else if (attacker.getZ() - target.getZ() < -50)
			modifier -= 3;
		
		// Get weather bonus. todo rain support (-3%).
		if (GameTimeController.getInstance().isNight())
			modifier -= 10;
		
		// Get position bonus.
		final Position position = Position.getPosition(attacker,target);
		final int positionbonus = position == Position.BACK ? 10 : position == Position.SIDE ? 7 : 4;
		modifier += positionbonus;
		
		chance *= modifier / 100;
		
		return Math.max(Math.min(chance, 980), 200) < Rnd.get(1000);
	}
	
	public static byte calcShldUse(L2Character attacker, L2Character target, boolean sendSysMsg)
	{
		double shldRate = target.calcStat(Stats.SHIELD_RATE, 0, attacker, null) * DEXbonus[target.getDEX()];
		if (shldRate == 0.0)
			return 0;
		
		// Check for passive skill Aegis (316) or Aegis Stance (318)
		// Like L2OFF you can't parry if your target is behind you
		if (target.getKnownSkill(316) == null && target.getFirstEffect(318) == null)
			if (target.isBehindOf(attacker) || !target.isFrontOf(attacker) || !attacker.isFrontOf(target))
				return 0;
		
		byte shldSuccess = 0;
		// if attacker use bow and target wear shield, shield block rate is multiplied by 1.3 (30%)
     	boolean isUsingBow = (attacker.getAttackType() == L2WeaponType.BOW);
     	
		if (isUsingBow)
			shldRate *= 1.3;
		
		if (shldRate > 0 && 100 - Config.ALT_PERFECT_SHLD_BLOCK < Rnd.get(100))
			shldSuccess = 2;
		else if (shldRate > Rnd.get(100))
			shldSuccess = 1;
		
		if (sendSysMsg && target instanceof L2PcInstance)
		{
			L2PcInstance enemy = (L2PcInstance) target;
			
			switch (shldSuccess)
			{
				case 1:
					enemy.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SHIELD_DEFENCE_SUCCESSFULL));
					break;
				case 2:
					enemy.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS));
					break;
			}
		}
		
		return shldSuccess;
	}
	
	public static byte calcShldUse(L2Character attacker, L2Character target)
	{
		return calcShldUse(attacker, target, true);
	}
	
	public static boolean calcMagicAffected(L2Character actor, L2Character target, L2Skill skill)
	{
		L2SkillType type = skill.getSkillType();
		if (target.isRaid() || target.isBoss() && (type == L2SkillType.CONFUSION || type == L2SkillType.MUTE || type == L2SkillType.PARALYZE || type == L2SkillType.ROOT || type == L2SkillType.FEAR || type == L2SkillType.SLEEP || type == L2SkillType.STUN || type == L2SkillType.DEBUFF || type == L2SkillType.AGGDEBUFF))
			return false; // these skills should have only 1/1000 chance on raid, now it's 0.
			
		double defence = 0;
		
		if (skill.isActive() && skill.isOffensive())
			defence = target.getMDef(actor, skill);
		
		double attack = 2 * actor.getMAtk(target, skill) * calcSkillVulnerability(target, skill);
		double d = (attack - defence) / (attack + defence);
		
		d += 0.5 * Rnd.nextGaussian();
		return d > 0;
	}
	
	@SuppressWarnings("incomplete-switch")
	public static double calcSkillVulnerability(L2Character target, L2Skill skill)
	{
		double multiplier = 1; // initialize...
		
		// Get the skill type to calculate its effect in function of base stats
		// of the L2Character target
		if (skill != null)
		{
			// first, get the natural template vulnerability values for the target
			Stats stat = skill.getStat();
			if (stat != null)
			{
				switch (stat)
				{
					case AGGRESSION:
						multiplier *= target.getTemplate().baseAggressionVuln;
						break;
					case BLEED:
						multiplier *= target.getTemplate().baseBleedVuln;
						break;
					case POISON:
						multiplier *= target.getTemplate().basePoisonVuln;
						break;
					case STUN:
						multiplier *= target.getTemplate().baseStunVuln;
						break;
					case ROOT:
						multiplier *= target.getTemplate().baseRootVuln;
						break;
					case MOVEMENT:
						multiplier *= target.getTemplate().baseMovementVuln;
						break;
					case CONFUSION:
						multiplier *= target.getTemplate().baseConfusionVuln;
						break;
					case SLEEP:
						multiplier *= target.getTemplate().baseSleepVuln;
						break;
					case FIRE:
						multiplier *= target.getTemplate().baseFireVuln;
						break;
					case WIND:
						multiplier *= target.getTemplate().baseWindVuln;
						break;
					case WATER:
						multiplier *= target.getTemplate().baseWaterVuln;
						break;
					case EARTH:
						multiplier *= target.getTemplate().baseEarthVuln;
						break;
					case HOLY:
						multiplier *= target.getTemplate().baseHolyVuln;
						break;
					case DARK:
						multiplier *= target.getTemplate().baseDarkVuln;
						break;
				}
			}
			
			// Next, calculate the elemental vulnerabilities
			switch (skill.getElement())
			{
				case L2Skill.ELEMENT_EARTH:
					multiplier = target.calcStat(Stats.EARTH_POWER, multiplier, target, skill);
					multiplier = target.calcStat(Stats.EARTH_VULN, multiplier, target, skill);
					break;
				case L2Skill.ELEMENT_FIRE:
					multiplier = target.calcStat(Stats.FIRE_POWER, multiplier, target, skill);
					multiplier = target.calcStat(Stats.FIRE_VULN, multiplier, target, skill);
					break;
				case L2Skill.ELEMENT_WATER:
					multiplier = target.calcStat(Stats.WATER_POWER, multiplier, target, skill);
					multiplier = target.calcStat(Stats.WATER_VULN, multiplier, target, skill);
					break;
				case L2Skill.ELEMENT_WIND:
					multiplier = target.calcStat(Stats.WIND_POWER, multiplier, target, skill);
					multiplier = target.calcStat(Stats.WIND_VULN, multiplier, target, skill);
					break;
				case L2Skill.ELEMENT_HOLY:
					multiplier = target.calcStat(Stats.HOLY_POWER, multiplier, target, skill);
					multiplier = target.calcStat(Stats.HOLY_VULN, multiplier, target, skill);
					break;
				case L2Skill.ELEMENT_DARK:
					multiplier = target.calcStat(Stats.DARK_POWER, multiplier, target, skill);
					multiplier = target.calcStat(Stats.DARK_VULN, multiplier, target, skill);
					break;
			}
			
			// Finally, calculate skilltype vulnerabilities
			L2SkillType type = skill.getSkillType();
			
			// For additional effects on PDAM and MDAM skills (like STUN, SHOCK, PARALYZE...)
			if (type != null && (type == L2SkillType.PDAM || type == L2SkillType.MDAM))
				type = skill.getEffectType();
			
			if (type != null)
			{
				switch (type)
				{
					case BLEED:
						multiplier = target.calcStat(Stats.BLEED_VULN, multiplier, target, null);
						break;
					case POISON:
						multiplier = target.calcStat(Stats.POISON_VULN, multiplier, target, null);
						break;
					case STUN:
						multiplier = target.calcStat(Stats.STUN_VULN, multiplier, target, null);
						break;
					case PARALYZE:
						multiplier = target.calcStat(Stats.PARALYZE_VULN, multiplier, target, null);
						break;
					case ROOT:
						multiplier = target.calcStat(Stats.ROOT_VULN, multiplier, target, null);
						break;
					case SLEEP:
						multiplier = target.calcStat(Stats.SLEEP_VULN, multiplier, target, null);
						break;
					case MUTE:
					case FEAR:
					case BETRAY:
					case AGGREDUCE_CHAR:
						multiplier = target.calcStat(Stats.DERANGEMENT_VULN, multiplier, target, null);
						break;
					case CONFUSION:
						multiplier = target.calcStat(Stats.CONFUSION_VULN, multiplier, target, null);
						break;
					case DEBUFF:
					case WEAKNESS:
						multiplier = target.calcStat(Stats.DEBUFF_VULN, multiplier, target, null);
						break;
					default:
				}
			}
			
		}
		return multiplier;
	}
	
	public static double calcSkillStatModifier(L2SkillType type, L2Character target)
	{
		double multiplier = 1;
		
		switch (type)
		{
			case STUN:
			case BLEED:
			case POISON:
				multiplier = 2 - Math.sqrt(CONbonus[target.getCON()]);
				break;
			case SLEEP:
			case DEBUFF:
			case WEAKNESS:
			case ERASE:
			case ROOT:
			case MUTE:
			case FEAR:
			case BETRAY:
			case CONFUSION:
			case AGGREDUCE_CHAR:
			case PARALYZE:
				multiplier = 2 - Math.sqrt(MENbonus[target.getMEN()]);
				break;
			default:
				break;
		}
		
		return Math.max(0, multiplier);
	}

	public static boolean calcSkillSuccess(L2Character attacker, L2Character target, L2Skill skill, boolean ss, boolean sps, boolean bss)
	{
		L2SkillType type = skill.getSkillType();
		
		if (target.isRaid() || target.isBoss() && (type == L2SkillType.CONFUSION || type == L2SkillType.MUTE || type == L2SkillType.PARALYZE || type == L2SkillType.ROOT || type == L2SkillType.FEAR || type == L2SkillType.SLEEP || type == L2SkillType.STUN || type == L2SkillType.DEBUFF || type == L2SkillType.AGGDEBUFF))
			return false; // these skills should not work on RaidBoss
			
		int value = (int) skill.getPower();
		int lvlDepend = skill.getLevelDepend();
		
		if (type == L2SkillType.PDAM || type == L2SkillType.MDAM) // For additional effects on PDAM skills (like STUN, SHOCK,...)
		{
			value = skill.getEffectPower();
			type = skill.getEffectType();
		}
		
		if (value == 0 || type == null)
		{
			if (skill.getSkillType() == L2SkillType.PDAM)
			{
				value = skill.getEffectPower() > 0 ? skill.getEffectPower() : 40;
				type = L2SkillType.STUN;
			}
			
			if (skill.getSkillType() == L2SkillType.MDAM)
			{
				value = skill.getEffectPower() > 0 ? skill.getEffectPower() : 20;
				type = skill.getSkillType() != null ? L2SkillType.MDAM : L2SkillType.PARALYZE;
			}
		}
		
		if (value == 0)
			value = (type == L2SkillType.PARALYZE) ? 30 : (type == L2SkillType.FEAR) ? 20 : 40;
		if (lvlDepend == 0)
			lvlDepend = (type == L2SkillType.PARALYZE || type == L2SkillType.FEAR) ? 1 : 2;
		
		// todo: Temporary fix for NPC skills with MagicLevel not set
		// int lvlmodifier = (skill.getMagicLevel() - target.getLevel()) * lvlDepend;
		int lvlmodifier = ((skill.getMagicLevel() > 0 ? skill.getMagicLevel() : attacker.getLevel()) - target.getLevel()) * lvlDepend;
		double statmodifier = calcSkillStatModifier(type, target);
		double resmodifier = calcSkillVulnerability(target, skill);
		
		int ssmodifier = 100;
		if (bss)
			ssmodifier = 200;
		else if (sps)
			ssmodifier = 150;
		else if (ss)
			ssmodifier = 150;
		
		int rate = (int) ((value * statmodifier + lvlmodifier) * resmodifier);
		if (skill.isMagic())
			rate = (int) (rate * Math.pow((double) attacker.getMAtk(target, skill) / target.getMDef(attacker, skill), 0.2));
		
		if (rate > 99)
			rate = 99;
		else if (rate < 1)
			rate = 1;
		
		if (ssmodifier != 100)
		{
			if (rate > 10000 / (100 + ssmodifier))
				rate = 100 - (100 - rate) * 100 / ssmodifier;
			else
				rate = rate * ssmodifier / 100;
		}
		
		if (Config.DEVELOPER)
			System.out.println(skill.getName() + ": " + value + ", " + statmodifier + ", " + lvlmodifier + ", " + resmodifier + ", " + ((int) (Math.pow((double) attacker.getMAtk(target, skill) / target.getMDef(attacker, skill), 0.2) * 100) - 100) + ", " + ssmodifier + " ==> " + rate);
		return (Rnd.get(100) < rate);
	}
	
	public static boolean calcMagicSuccess(L2Character attacker, L2Character target, L2Skill skill)
	{
		double lvlDifference = (target.getLevel() - (skill.getMagicLevel() > 0 ? skill.getMagicLevel() : attacker.getLevel()));
		int rate = Math.round((float) (Math.pow(1.3, lvlDifference) * 100));
		
		return (Rnd.get(10000) > rate);
	}

	public static boolean calculateUnlockChance(L2Skill skill)
	{
		int level = skill.getLevel();
		int chance = 0;
		switch (level)
		{
			case 1:
				chance = 30;
				break;
			
			case 2:
				chance = 50;
				break;
			
			case 3:
				chance = 75;
				break;
			
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			case 11:
			case 12:
			case 13:
			case 14:
				chance = 100;
				break;
		}
		if (Rnd.get(120) > chance)
			return false;
		return true;
	}
	
	public static double calcManaDam(L2Character attacker, L2Character target, L2Skill skill, boolean ss, boolean bss)
	{
		// Mana Burnt = (SQR(M.Atk)*Power*(Target Max MP/97))/M.Def
		double mAtk = attacker.getMAtk(target, skill);
		double mDef = target.getMDef(attacker, skill);
		double mp = target.getMaxMp();
		if (bss)
			mAtk *= 4;
		else if (ss)
			mAtk *= 2;
		
		double damage = (Math.sqrt(mAtk) * skill.getPower(attacker) * (mp / 97)) / mDef;
		damage *= calcSkillVulnerability(target, skill);
		return damage;
	}
	
	public static double calculateSkillResurrectRestorePercent(double baseRestorePercent, int casterWIT)
	{
		double restorePercent = baseRestorePercent;
		double modifier = WITbonus[casterWIT];
		
		if (restorePercent != 100 && restorePercent != 0)
		{
			
			restorePercent = baseRestorePercent * modifier;
			
			if (restorePercent - baseRestorePercent > 20.0)
				restorePercent = baseRestorePercent + 20.0;
		}
		
		if (restorePercent > 100)
			restorePercent = 100;
		if (restorePercent < baseRestorePercent)
			restorePercent = baseRestorePercent;
		
		return restorePercent;
	}
	
	public static double getSTRBonus(L2Character activeChar)
	{
		return STRbonus[activeChar.getSTR()];
	}
	
	public static boolean calcSkillMastery(L2Character actor)
	{
		if (actor == null)
			return false;
		
		double val = actor.getStat().calcStat(Stats.SKILL_MASTERY, 0, null, null);
		
		if (actor instanceof L2PcInstance && ((L2PcInstance) actor).isMageClass())
			val *= INTbonus[actor.getINT()];
		else
			val *= STRbonus[actor.getSTR()];
		
		return Rnd.get(100) < val;
	}
	
	public static boolean calcPhysicalSkillEvasion(L2Character target, L2Skill skill)
	{
		if (skill.isMagic() && skill.getSkillType() != L2SkillType.BLOW)
			return false;
		
		return Rnd.get(100) < target.calcStat(Stats.P_SKILL_EVASION, 0, null, skill);
	}
	
	public static double calcFallDam(L2Character cha, int fallHeight)
	{
		if (fallHeight < 0)
			return 0;
		
		return cha.calcStat(Stats.FALL, fallHeight * cha.getMaxHp() / 1000.0, null, null);
	}
	
	private static final double[] karma =
	{
		0,
		0.772184315,
		2.069377971,
		2.315085083,
		2.467800843,
		2.514219611,
		2.510075822,
		2.532083418,
		2.473028945,
		2.377178509,
		2.285526643,
		2.654635163,
		2.963434737,
		3.266100461,
		3.561112664,
		3.847320291,
		4.123878064,
		4.390194136,
		4.645886341,
		4.890745518,
		5.124704707,
		6.97914069,
		7.270620642,
		7.548951721,
		7.81438302,
		8.067235867,
		8.307889422,
		8.536768399,
		8.754332624,
		8.961068152,
		9.157479758,
		11.41901707,
		11.64989746,
		11.87007991,
		12.08015809,
		12.28072687,
		12.47237891,
		12.65570177,
		12.83127553,
		12.99967093,
		13.16144786,
		15.6563607,
		15.84513182,
		16.02782135,
		16.20501182,
		16.37727218,
		16.54515749,
		16.70920885,
		16.86995336,
		17.02790439,
		17.18356182,
		19.85792061,
		20.04235517,
		20.22556446,
		20.40806338,
		20.59035551,
		20.77293378,
		20.95628115,
		21.1408714,
		21.3271699,
		21.51563446,
		24.3895427,
		24.61486587,
		24.84389213,
		25.07711247,
		25.31501442,
		25.55808296,
		25.80680152,
		26.06165297,
		26.32312062,
		26.59168923,
		26.86784604,
		27.15208178,
		27.44489172,
		27.74677676,
		28.05824444,
		28.37981005,
		28.71199773,
		29.05534154,
		29.41038662,
		29.77769028
	};
	
	public static int calculateKarmaLost(int playerLevel, long exp)
	{
		return (int) (exp / karma[playerLevel] / 15);
	}
	
	public static int calculateKarmaGain(int pkCount, boolean isSummon)
	{
		int result = 14400;
		if (pkCount < 100)
			result = (int) (((((pkCount - 1) * 0.5) + 1) * 60) * 4);
		else if (pkCount < 180)
			result = (int) (((((pkCount + 1) * 0.125) + 37.5) * 60) * 4);
		
		if (isSummon)
			result = ((pkCount & 3) + result) >> 2;
		
		return result;
	}

	public static byte calcSkillReflect(L2Character target, L2Skill skill)
	{
		// Only magic and melee skills can be reflected.
		if (!skill.isMagic() && (skill.getCastRange() == -1 || skill.getCastRange() > 40))
			return 0;
		
		byte reflect = 0;
		
		// Check for non-reflected skilltypes, need additional retail check.
		switch (skill.getSkillType())
		{
			case BUFF:
			case REFLECT:
			case HEAL_PERCENT:
			case MANAHEAL_PERCENT:
			case HOT:
			case CPHOT:
			case MPHOT:
			case UNDEAD_DEFENSE:
			case AGGDEBUFF:
			case CONT:
				return 0;
				
			case PDAM:
			case BLOW:
			case MDAM:
			case DEATHLINK:
			case CHARGEDAM:
				final double venganceChance = target.getStat().calcStat((skill.isMagic()) ? Stats.REFLECT_SKILL_MAGIC : Stats.REFLECT_SKILL_PHYSIC, 0, target, skill);
				if (venganceChance > Rnd.get(100))
					reflect |= 2;
				break;
			default :
				break;
		}
		
		final double reflectChance = target.calcStat((skill.isMagic()) ? Stats.REFLECT_SKILL_MAGIC : Stats.REFLECT_SKILL_PHYSIC, 0, null, skill);
		if (Rnd.get(100) < reflectChance)
			reflect |= 1;
		
		return reflect;
	}
}