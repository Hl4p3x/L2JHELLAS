package com.l2jhellas.gameserver.templates;

public class L2CharTemplate
{
	// BaseStats
	public int baseSTR;
	public int baseCON;
	public int baseDEX;
	public int baseINT;
	public int baseWIT;
	public int baseMEN;
	public double baseHpMax;
	public double baseCpMax;
	public double baseMpMax;
	
	public final float baseHpReg;
	
	public final float baseMpReg;
	
	public double basePAtk;
	public double baseMAtk;
	public double basePDef;
	public double baseMDef;
	
	public int basePAtkSpd;
	public int baseMAtkSpd;
	public float baseMReuseRate;
	public final int baseShldDef;
	public final int baseAtkRange;
	public final int baseShldRate;
	public int baseCritRate;
	public final int baseMCritRate;
	public int baseWalkSpd;
	public int baseRunSpd;
	
	// SpecialStats
	public final int baseBreath;
	public final int baseAggression;
	public final int baseBleed;
	public final int basePoison;
	public final int baseStun;
	public final int baseRoot;
	public final int baseMovement;
	public final int baseConfusion;
	public final int baseSleep;
	public final int baseFire;
	public final int baseWind;
	public final int baseWater;
	public final int baseEarth;
	public final int baseHoly;
	public final int baseDark;
	public final double baseAggressionVuln;
	public final double baseBleedVuln;
	public final double basePoisonVuln;
	public final double baseStunVuln;
	public final double baseRootVuln;
	public final double baseMovementVuln;
	public final double baseConfusionVuln;
	public final double baseSleepVuln;
	public final double baseFireVuln;
	public final double baseWindVuln;
	public final double baseWaterVuln;
	public final double baseEarthVuln;
	public final double baseHolyVuln;
	public final double baseDarkVuln;
	public final double baseCritVuln;
	
	public final boolean isUndead;
	
	// C4 Stats
	public final int baseMpConsumeRate;
	public final int baseHpConsumeRate;
	
	public double collisionRadius;
	public double collisionHeight;
	
	public L2CharTemplate(StatsSet set)
	{		
		// Base stats
		baseSTR = set.getInteger("str", 40);
		baseCON = set.getInteger("con", 21);
		baseDEX = set.getInteger("dex", 30);
		baseINT = set.getInteger("int", 20);
		baseWIT = set.getInteger("wit", 43);
		baseMEN = set.getInteger("men", 20);
		
		baseHpMax = set.getDouble("hp", 0);
		baseCpMax = set.getDouble("baseCpMax", 0);
		baseMpMax = set.getDouble("mp", 0);
		baseHpReg = set.getFloat("baseHpReg", 0);
		baseMpReg = set.getFloat("baseMpReg", 0);
		
		basePAtk = set.getDouble("pAtk", 4);
		baseMAtk = set.getDouble("mAtk", 6);
		basePDef = set.getDouble("pDef", 60);
		baseMDef = set.getDouble("mDef", 41);
		
		basePAtkSpd = set.getInteger("atkSpd", 300);	
		baseMAtkSpd = set.getInteger("baseMAtkSpd", 333);
		
		baseMReuseRate = set.getFloat("baseMReuseDelay", 1.f);
		baseShldDef = set.getInteger("baseShldDef", 0);
		baseAtkRange = set.getInteger("baseAtkRange", 40);
		baseShldRate = set.getInteger("baseShldRate", 0);
		baseCritRate = set.getInteger("crit", 4);
		baseMCritRate = set.getInteger("baseMCritRate", 8);
		baseWalkSpd = set.getInteger("walkSpd", 1);
		baseRunSpd = set.getInteger("runSpd", 2);
		
		// Special Stats
		baseBreath = set.getInteger("baseBreath", 100);
		baseAggression = set.getInteger("baseAggression", 0);
		baseBleed = set.getInteger("baseBleed", 0);
		basePoison = set.getInteger("basePoison", 0);
		baseStun = set.getInteger("baseStun", 0);
		baseRoot = set.getInteger("baseRoot", 0);
		baseMovement = set.getInteger("baseMovement", 0);
		baseConfusion = set.getInteger("baseConfusion", 0);
		baseSleep = set.getInteger("baseSleep", 0);
		baseFire = set.getInteger("baseFire", 0);
		baseWind = set.getInteger("baseWind", 0);
		baseWater = set.getInteger("baseWater", 0);
		baseEarth = set.getInteger("baseEarth", 0);
		baseHoly = set.getInteger("baseHoly", 0);
		baseDark = set.getInteger("baseDark", 0);
		baseAggressionVuln = set.getInteger("baseAaggressionVuln", 1);
		baseBleedVuln = set.getInteger("baseBleedVuln", 1);
		basePoisonVuln = set.getInteger("basePoisonVuln", 1);
		baseStunVuln = set.getInteger("baseStunVuln", 1);
		baseRootVuln = set.getInteger("baseRootVuln", 1);
		baseMovementVuln = set.getInteger("baseMovementVuln", 1);
		baseConfusionVuln = set.getInteger("baseConfusionVuln", 1);
		baseSleepVuln = set.getInteger("baseSleepVuln", 1);
		baseFireVuln = set.getInteger("baseFireVuln", 1);
		baseWindVuln = set.getInteger("baseWindVuln", 1);
		baseWaterVuln = set.getInteger("baseWaterVuln", 1);
		baseEarthVuln = set.getInteger("baseEarthVuln", 1);
		baseHolyVuln = set.getInteger("baseHolyVuln", 1);
		baseDarkVuln = set.getInteger("baseDarkVuln", 1);
		baseCritVuln = set.getInteger("baseCritVuln", 1);
		
		isUndead = (set.getInteger("isUndead", 0) == 1);
		
		// C4 Stats
		baseMpConsumeRate = set.getInteger("baseMpConsumeRate", 0);
		baseHpConsumeRate = set.getInteger("baseHpConsumeRate", 0);
		
		// Geometry
		collisionRadius = set.getDouble("radius", 0);
		collisionHeight = set.getDouble("height", 0);
		
	}
	
	public double getBaseHpMax(int level)
	{
		return baseHpMax;
	}
	
	public double getBaseMpMax(int level)
	{
		return baseMpMax;
	}
	
	public double getBaseHpRegen(int level)
	{
		return baseHpReg;
	}
	
	public double getBaseMpRegen(int level)
	{
		return baseMpReg;
	}
	
	public double getCollisionRadius()
	{
		return collisionRadius;
	}
	
	public double getCollisionHeight()
	{
		return collisionHeight;
	}
}