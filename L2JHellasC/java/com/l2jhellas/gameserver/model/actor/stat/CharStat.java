package com.l2jhellas.gameserver.model.actor.stat;

import Extensions.Balancer.BalanceLoad;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.enums.ZoneId;
import com.l2jhellas.gameserver.enums.items.L2WeaponType;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.skills.Calculator;
import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.Stats;

public class CharStat
{
	private final L2Character _activeChar;
	private long _exp = 0;
	private int _sp = 0;
	private byte _level = 1;
	
	private int _fire = 0;
	private int _water = 0;
	private int _wind = 0;
	private int _earth = 0;
	private int _holy = 0;
	private int _dark = 0;
	
	public CharStat(L2Character activeChar)
	{
		_activeChar = activeChar;
	}
	
	public final double calcStat(Stats stat, double init, L2Character target, L2Skill skill)
	{
		if (_activeChar == null)
			return init;
		
		int id = stat.ordinal();
		
		Calculator c = _activeChar.getCalculators()[id];
		
		// If no Func object found, no modifier is applied
		if (c == null || c.size() == 0)
			return init;
		
		// Create and init an Env object to pass parameters to the Calculator
		Env env = new Env();
		env.player = _activeChar;
		env.target = target;
		env.skill = skill;
		env.value = init;
		
		// Launch the calculation
		c.calc(env);
		// avoid some troubles with negative stats (some stats should never be
		// negative)
		if (env.value <= 0 && ((stat == Stats.MAX_HP) || (stat == Stats.MAX_MP) || (stat == Stats.MAX_CP) || (stat == Stats.MAGIC_DEFENCE) || (stat == Stats.POWER_DEFENCE) || (stat == Stats.POWER_ATTACK) || (stat == Stats.MAGIC_ATTACK) || (stat == Stats.POWER_ATTACK_SPEED) || (stat == Stats.MAGIC_ATTACK_SPEED) || (stat == Stats.SHIELD_DEFENCE) || (stat == Stats.STAT_CON) || (stat == Stats.STAT_DEX) || (stat == Stats.STAT_INT) || (stat == Stats.STAT_MEN) || (stat == Stats.STAT_STR) || (stat == Stats.STAT_WIT)))
		{
			env.value = 1;
		}
		
		return env.value;
	}
	
	public final int getSTR()
	{
		int str = (int) calcStat(Stats.STAT_STR, _activeChar.getTemplate().baseSTR, null, null);
		if (_activeChar instanceof L2PcInstance && ((L2PcInstance) _activeChar).getClassId().getId() >= 88)
			str += BalanceLoad.STR[((L2PcInstance) _activeChar).getClassId().getId() - 88];
		return str;
	}
	
	public final int getDEX()
	{
		int DEX = (int) calcStat(Stats.STAT_DEX, _activeChar.getTemplate().baseDEX, null, null);
		if (_activeChar instanceof L2PcInstance && ((L2PcInstance) _activeChar).getClassId().getId() >= 88)
			DEX += BalanceLoad.DEX[((L2PcInstance) _activeChar).getClassId().getId() - 88];
		return DEX;
	}
	
	public final int getCON()
	{
		int CON = (int) calcStat(Stats.STAT_CON, _activeChar.getTemplate().baseCON, null, null);
		if (_activeChar instanceof L2PcInstance && ((L2PcInstance) _activeChar).getClassId().getId() >= 88)
			CON += BalanceLoad.CON[((L2PcInstance) _activeChar).getClassId().getId() - 88];
		return CON;
	}
	
	public int getINT()
	{
		int INT = (int) calcStat(Stats.STAT_INT, _activeChar.getTemplate().baseINT, null, null);
		if (_activeChar instanceof L2PcInstance && ((L2PcInstance) _activeChar).getClassId().getId() >= 88)
			INT += BalanceLoad.INT[((L2PcInstance) _activeChar).getClassId().getId() - 88];
		return INT;
	}
	
	public final int getMEN()
	{
		int MEN = (int) calcStat(Stats.STAT_MEN, _activeChar.getTemplate().baseMEN, null, null);
		if (_activeChar instanceof L2PcInstance && ((L2PcInstance) _activeChar).getClassId().getId() >= 88)
			MEN += BalanceLoad.MEN[((L2PcInstance) _activeChar).getClassId().getId() - 88];
		return MEN;
	}
	
	public final int getWIT()
	{
		int WIT = (int) calcStat(Stats.STAT_WIT, _activeChar.getTemplate().baseWIT, null, null);
		if (_activeChar instanceof L2PcInstance && ((L2PcInstance) _activeChar).getClassId().getId() >= 88)
			WIT += BalanceLoad.WIT[((L2PcInstance) _activeChar).getClassId().getId() - 88];
		return WIT;
	}
	
	public int getCriticalHit(L2Character target, L2Skill skill)
	{
		int criticalHit = (int) (calcStat(Stats.CRITICAL_RATE, _activeChar.getTemplate().baseCritRate, target, skill) * 10.0 + 0.5);
		criticalHit /= 10;
		if (_activeChar instanceof L2PcInstance && ((L2PcInstance) _activeChar).getClassId().getId() >= 88)
			criticalHit += BalanceLoad.Critical[((L2PcInstance) _activeChar).getClassId().getId() - 88];
		// Set a cap of Critical Hit at 500
		if (criticalHit > Config.MAX_PCRIT_RATE)
			criticalHit = Config.MAX_PCRIT_RATE;
		
		return criticalHit;
	}
	
	public final int getMCriticalHit(L2Character target, L2Skill skill)
	{
		int mrate = (int) calcStat(Stats.MCRITICAL_RATE, _activeChar.getTemplate().baseMCritRate, target, skill);
		if (_activeChar instanceof L2PcInstance && ((L2PcInstance) _activeChar).getClassId().getId() >= 88)
			mrate += BalanceLoad.MagicCritical[((L2PcInstance) _activeChar).getClassId().getId() - 88];
		if (mrate > Config.MAX_MCRIT_RATE)
			mrate = Config.MAX_MCRIT_RATE;
		return mrate;
	}
	
	public int getEvasionRate(L2Character target)
	{
		int val = (int) (calcStat(Stats.EVASION_RATE, 0, target, null) / _activeChar.getArmourExpertisePenalty());
		if (_activeChar instanceof L2PcInstance && ((L2PcInstance) _activeChar).getClassId().getId() >= 88)
			val += BalanceLoad.Evasion[((L2PcInstance) _activeChar).getClassId().getId() - 88];
		return val;
	}
	
	public int getAccuracy()
	{
		int val = (int) calcStat(Stats.ACCURACY_COMBAT, 0, null, null);
		if (_activeChar instanceof L2PcInstance && ((L2PcInstance) _activeChar).getClassId().getId() >= 88)
			val += BalanceLoad.Accuracy[((L2PcInstance) _activeChar).getClassId().getId() - 88];
		return val;
	}
	
	public int getMaxHp()
	{
		int val = (int) calcStat(Stats.MAX_HP, _activeChar.getTemplate().baseHpMax, null, null);
		if (_activeChar instanceof L2PcInstance && ((L2PcInstance) _activeChar).getClassId().getId() >= 88)
			val += BalanceLoad.HP[((L2PcInstance) _activeChar).getClassId().getId() - 88];
		return val;
	}
	
	public int getMaxCp()
	{
		int val = (int) calcStat(Stats.MAX_CP, _activeChar.getTemplate().baseCpMax, null, null);
		if (_activeChar instanceof L2PcInstance && ((L2PcInstance) _activeChar).getClassId().getId() >= 88)
			val += BalanceLoad.CP[((L2PcInstance) _activeChar).getClassId().getId() - 88];
		return val;
	}
	
	public int getMaxMp()
	{
		int val = (int) calcStat(Stats.MAX_MP, _activeChar.getTemplate().baseMpMax, null, null);
		if (_activeChar instanceof L2PcInstance && ((L2PcInstance) _activeChar).getClassId().getId() >= 88)
			val += BalanceLoad.MP[((L2PcInstance) _activeChar).getClassId().getId() - 88];
		return val;
	}
	
	@SuppressWarnings("incomplete-switch")
	public int getMAtk(L2Character target, L2Skill skill)
	{
		if (_activeChar == null)
			return 1;
		float bonusAtk = 1;
		if (_activeChar.isChampion())
			bonusAtk = Config.CHAMPION_ATK;
		double attack = _activeChar.getTemplate().baseMAtk * bonusAtk;
		// Get the skill type to calculate its effect in function of base stats
		// of the L2Character target
		Stats stat = skill == null ? null : skill.getStat();
		
		if (stat != null)
		{
			switch (stat)
			{
				case AGGRESSION:
					attack += _activeChar.getTemplate().baseAggression;
					break;
				case BLEED:
					attack += _activeChar.getTemplate().baseBleed;
					break;
				case POISON:
					attack += _activeChar.getTemplate().basePoison;
					break;
				case STUN:
					attack += _activeChar.getTemplate().baseStun;
					break;
				case ROOT:
					attack += _activeChar.getTemplate().baseRoot;
					break;
				case MOVEMENT:
					attack += _activeChar.getTemplate().baseMovement;
					break;
				case CONFUSION:
					attack += _activeChar.getTemplate().baseConfusion;
					break;
				case SLEEP:
					attack += _activeChar.getTemplate().baseSleep;
					break;
				case FIRE:
					attack += _activeChar.getTemplate().baseFire;
					break;
				case WIND:
					attack += _activeChar.getTemplate().baseWind;
					break;
				case WATER:
					attack += _activeChar.getTemplate().baseWater;
					break;
				case EARTH:
					attack += _activeChar.getTemplate().baseEarth;
					break;
				case HOLY:
					attack += _activeChar.getTemplate().baseHoly;
					break;
				case DARK:
					attack += _activeChar.getTemplate().baseDark;
					break;
			}
		}
		
		// Add the power of the skill to the attack effect
		if (skill != null)
			attack += skill.getPower();
		
		// Calculate modifiers Magic Attack
		int val = (int) calcStat(Stats.MAGIC_ATTACK, attack, target, skill);
		if (_activeChar instanceof L2PcInstance && ((L2PcInstance) _activeChar).getClassId().getId() >= 88)
			val += BalanceLoad.MAtk[((L2PcInstance) _activeChar).getClassId().getId() - 88];
		return val;
	}

	public int getMDef(L2Character target, L2Skill skill)
	{
		// Get the base MDef of the L2Character
		double defence = _activeChar.getTemplate().baseMDef;
		
		// Calculate modifier for Raid Bosses
		if (_activeChar.isRaid() || _activeChar.isBoss())
			defence *= Config.RAID_M_DEFENCE_MULTIPLIER;
		
		// Calculate modifiers Magic Attack
		int val = (int) calcStat(Stats.MAGIC_DEFENCE, defence, target, skill);
		if (_activeChar instanceof L2PcInstance && ((L2PcInstance) _activeChar).getClassId().getId() >= 88)
			val += BalanceLoad.MDef[((L2PcInstance) _activeChar).getClassId().getId() - 88];
		return val;
	}
	
	public int getPAtk(L2Character target)
	{
		float bonusAtk = 1;
		if (_activeChar.isChampion())
			bonusAtk = Config.CHAMPION_ATK;
		int val = (int) calcStat(Stats.POWER_ATTACK, _activeChar.getTemplate().basePAtk * bonusAtk, target, null);
		if (_activeChar instanceof L2PcInstance && ((L2PcInstance) _activeChar).getClassId().getId() >= 88)
			val += BalanceLoad.PAtk[((L2PcInstance) _activeChar).getClassId().getId() - 88];
		return val;
	}
	
	public int getPAtkSpd()
	{
		float bonusAtk = 1;
		if (_activeChar.isChampion())
			bonusAtk = Config.CHAMPION_SPD_ATK;
		
		int val = (int) (calcStat(Stats.POWER_ATTACK_SPEED, _activeChar.getTemplate().basePAtkSpd * bonusAtk, null, null) / _activeChar.getArmourExpertisePenalty());
		if (_activeChar instanceof L2PcInstance && ((L2PcInstance) _activeChar).getClassId().getId() >= 88)
			val += BalanceLoad.PAtkSpd[((L2PcInstance) _activeChar).getClassId().getId() - 88];

		return Math.min(val, Config.MAX_PATK_SPEED);
	}
		
	public int getMAtkSpd()
	{
		float bonusSpdAtk = 1;
		if (_activeChar.isChampion())
			bonusSpdAtk = Config.CHAMPION_SPD_ATK;
		
		int val = (int) calcStat(Stats.MAGIC_ATTACK_SPEED, _activeChar.getTemplate().baseMAtkSpd * bonusSpdAtk, null, null);
		val /= _activeChar.getArmourExpertisePenalty();
		if (_activeChar instanceof L2PcInstance && ((L2PcInstance) _activeChar).getClassId().getId() >= 88)
			val += BalanceLoad.MAtkSpd[((L2PcInstance) _activeChar).getClassId().getId() - 88];
		
		return Math.min(val, Config.MAX_MATK_SPEED);
	}

	public int getPDef(L2Character target)
	{
		// Get the base PDef of the L2Character
		double defence = _activeChar.getTemplate().basePDef;
		
		// Calculate modifier for Raid Bosses
		if (_activeChar.isRaid() || _activeChar.isBoss())
			defence *= Config.RAID_P_DEFENCE_MULTIPLIER;
		
		// Calculate modifiers Magic Attack
		int val = (int) calcStat(Stats.POWER_DEFENCE, defence, target, null);
		if (_activeChar instanceof L2PcInstance && ((L2PcInstance) _activeChar).getClassId().getId() >= 88)
			val += BalanceLoad.PDef[((L2PcInstance) _activeChar).getClassId().getId() - 88];
		return val;
	}
	
	public final double getPAtkAnimals(L2Character target)
	{
		return calcStat(Stats.PATK_ANIMALS, 1, target, null);
	}
	
	public final double getPAtkDragons(L2Character target)
	{
		return calcStat(Stats.PATK_DRAGONS, 1, target, null);
	}
	
	public final double getPAtkInsects(L2Character target)
	{
		return calcStat(Stats.PATK_INSECTS, 1, target, null);
	}
	
	public final double getPAtkMonsters(L2Character target)
	{
		return calcStat(Stats.PATK_BEAST, 1, target, null);
	}
	
	public final double getPAtkPlants(L2Character target)
	{
		return calcStat(Stats.PATK_PLANTS, 1, target, null);
	}
	
	public final double getPAtkGiants(L2Character target)
	{
		return calcStat(Stats.PATK_PLANTS, 1, target, null);
	}
	
	public final double getPAtkUndead(L2Character target)
	{
		return calcStat(Stats.PATK_UNDEAD, 1, target, null);
	}
	
	public final double getPDefUndead(L2Character target)
	{
		return calcStat(Stats.PDEF_UNDEAD, 1, target, null);
	}
	
	public final int getPhysicalAttackRange()
	{
		return getActiveChar().getAttackType().equals(L2WeaponType.BOW)? (int) calcStat(Stats.POWER_ATTACK_RANGE, _activeChar.getTemplate().baseAtkRange, null, null) : getActiveChar().getAttackType().getRange();
	}
	
	public final double getWeaponReuseModifier(L2Character target)
	{
		return calcStat(Stats.ATK_REUSE, 1, target, null);
	}
		
	public final int getShldDef()
	{
		return (int) calcStat(Stats.SHIELD_DEFENCE, 0, null, null);
	}
	
	public final int getMpConsume(L2Skill skill)
	{
		if (skill == null)
			return 1;
		double mpconsume = skill.getMpConsume();
		if (skill.isDance())
			if (_activeChar != null && _activeChar.getDanceCount() > 0)
				mpconsume += _activeChar.getDanceCount() * skill.getNextDanceMpCost();
		return (int) calcStat(Stats.MP_CONSUME, mpconsume, null, skill);
	}
	
	public final int getMpInitialConsume(L2Skill skill)
	{
		if (skill == null)
			return 1;
		
		return (int) calcStat(Stats.MP_CONSUME, skill.getMpInitialConsume(), null, skill);
	}
	
	public int getAttackElementValue(int attackAttribute)
	{
		switch (attackAttribute)
		{
			case -2:
				return 0;
			case 0:
				return _fire = (int) calcStat(Stats.FIRE_POWER, 0, null, null);
			case 1:
				return _water = (int) calcStat(Stats.WATER_POWER, 0, null, null);
			case 2:
				return _wind = (int) calcStat(Stats.WIND_POWER, 0, null, null);
			case 3:
				return _earth = (int) calcStat(Stats.EARTH_POWER, 0, null, null);
			case 4:
				return _holy = (int) calcStat(Stats.HOLY_POWER, 0, null, null);
			case 5:
				return _dark = (int) calcStat(Stats.DARK_POWER, 0, null, null);
		}
		return 0;
	}
	
	public int getAttackElement()
	{
		double tempVal = 0, stats[] =
		{
			_fire,
			_water,
			_wind,
			_earth,
			_holy,
			_dark
		};
		
		int returnVal = -2;
		_earth = (int) calcStat(Stats.EARTH_POWER, 0, null, null);
		_fire = (int) calcStat(Stats.FIRE_POWER, 0, null, null);
		_water = (int) calcStat(Stats.WATER_POWER, 0, null, null);
		_wind = (int) calcStat(Stats.WIND_POWER, 0, null, null);
		_holy = (int) calcStat(Stats.HOLY_POWER, 0, null, null);
		_dark = (int) calcStat(Stats.DARK_POWER, 0, null, null);
		
		for (int x = 0; x < stats.length; x++)
		{
			if (stats[x] > tempVal)
			{
				returnVal = x;
				tempVal = stats[x];
			}
		}
		
		return returnVal;
	}
	
	public int getWalkSpeed()
	{
		int walkspd = (getRunSpeed() * 70) / 100;
		
		if (_activeChar instanceof L2PcInstance)
		{
			
			if (_activeChar.isInsideZone(ZoneId.SWAMP) || _activeChar.isInsideZone(ZoneId.WATER))
				walkspd /= 1;
			
			return walkspd;
		}
		return (int) calcStat(Stats.WALK_SPEED, getBaseWalkSpeed(), null, null);
	}
	
	public int getRunSpeed()
	{
		// err we should be adding TO the persons run speed not making it a constant
		int val = (int) (calcStat(Stats.RUN_SPEED, getBaseRunSpeed(), null, null));
		if (_activeChar instanceof L2PcInstance && ((L2PcInstance) _activeChar).getClassId().getId() >= 88)
			val += BalanceLoad.Speed[((L2PcInstance) _activeChar).getClassId().getId() - 88];
		
		if (_activeChar.isFlying())
			return val += Config.WYVERN_SPEED;
		if (_activeChar.isRiding())
			return val += Config.STRIDER_SPEED;
		
		val /= _activeChar.getArmourExpertisePenalty();
		
		// swamp zone -50% speed
		if (_activeChar.isInsideZone(ZoneId.SWAMP))
			val /= 2;
		
		if(_activeChar.isInsideZone(ZoneId.WATER))
			val /= 1.4;

		return Math.min(val, Config.MAX_RUN_SPEED);
	}

	public int getBaseRunSpeed()
	{
		return _activeChar.getTemplate().baseRunSpd;
	}

	public int getBaseWalkSpeed()
	{
		return _activeChar.getTemplate().baseWalkSpd;
	}

	protected final int getBaseMoveSpeed()
	{
		return _activeChar.isRunning() ? getBaseRunSpeed() : getWalkSpeed();
	}
	
	public float getMovementSpeedMultiplier()
	{		
		return (float) (getMoveSpeed() * (1. / getBaseMoveSpeed()));
	}

	public final float getAttackSpeedMultiplier()
	{
		return (float) ((1.1) * getPAtkSpd() / _activeChar.getTemplate().basePAtkSpd);
	}
	
	public float getMoveSpeed()
	{
		return (_activeChar.isRunning()) ? getRunSpeed() : getWalkSpeed();
	}
	
	public long getExp()
	{
		return _exp;
	}
	
	public void setExp(long value)
	{
		_exp = value;
	}
	
	public int getSp()
	{
		return _sp;
	}
	
	public void setSp(int value)
	{
		_sp = value;
	}
	
	public byte getLevel()
	{
		return _level;
	}
	
	public void setLevel(byte value)
	{
		_level = value;
	}
	
	public L2Character getActiveChar()
	{
		return _activeChar;
	}
	
	public final double getCriticalDmg(L2Character target, double init)
	{
		return calcStat(Stats.CRITICAL_DAMAGE, init, target, null);
	}
	
	public final int getMagicalAttackRange(L2Skill skill)
	{
		if (skill != null)
		{
			int MAtkRange = (int) calcStat(Stats.MAGIC_ATTACK_RANGE, skill.getCastRange(), null, skill);
			if (_activeChar instanceof L2PcInstance && ((L2PcInstance) _activeChar).getClassId().getId() >= 88)
				MAtkRange += BalanceLoad.MAtkRange[((L2PcInstance) _activeChar).getClassId().getId() - 88];
			return MAtkRange;
		}
		return _activeChar.getTemplate().baseAtkRange;
	}
	
	public final double getMReuseRate(L2Skill skill)
	{
		double MReuseRate = calcStat(Stats.MAGIC_REUSE_RATE, _activeChar.getTemplate().baseMReuseRate, null, skill);
		if (_activeChar instanceof L2PcInstance && ((L2PcInstance) _activeChar).getClassId().getId() >= 88)
			MReuseRate += BalanceLoad.MReuseRate[((L2PcInstance) _activeChar).getClassId().getId() - 88];
		return MReuseRate;
	}
	
	public final double getPReuseRate(L2Skill skill)
	{
		double PReuseRate = calcStat(Stats.P_REUSE, _activeChar.getTemplate().baseMReuseRate, null, skill);
		if (_activeChar instanceof L2PcInstance && ((L2PcInstance) _activeChar).getClassId().getId() >= 88)
			PReuseRate += BalanceLoad.PReuseRate[((L2PcInstance) _activeChar).getClassId().getId() - 88];
		return PReuseRate;
	}
}