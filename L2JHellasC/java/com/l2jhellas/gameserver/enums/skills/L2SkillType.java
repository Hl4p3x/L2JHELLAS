package com.l2jhellas.gameserver.enums.skills;

import java.lang.reflect.Constructor;

import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.skills.l2skills.L2SkillCharge;
import com.l2jhellas.gameserver.skills.l2skills.L2SkillChargeDmg;
import com.l2jhellas.gameserver.skills.l2skills.L2SkillChargeEffect;
import com.l2jhellas.gameserver.skills.l2skills.L2SkillCreateItem;
import com.l2jhellas.gameserver.skills.l2skills.L2SkillDefault;
import com.l2jhellas.gameserver.skills.l2skills.L2SkillDrain;
import com.l2jhellas.gameserver.skills.l2skills.L2SkillMagicOnGround;
import com.l2jhellas.gameserver.skills.l2skills.L2SkillSeed;
import com.l2jhellas.gameserver.skills.l2skills.L2SkillSignet;
import com.l2jhellas.gameserver.skills.l2skills.L2SkillSignetCasttime;
import com.l2jhellas.gameserver.skills.l2skills.L2SkillSpawn;
import com.l2jhellas.gameserver.skills.l2skills.L2SkillSummon;
import com.l2jhellas.gameserver.templates.StatsSet;

public enum L2SkillType
{
	// Damage
	PDAM,
	MDAM,
	CPDAM,
	MANADAM,
	DOT,
	SIGNET(L2SkillSignet.class),
	SIGNET_CASTTIME(L2SkillSignetCasttime.class),
	MDOT,
	DRAIN_SOUL,
	DRAIN(L2SkillDrain.class),
	DEATHLINK,
	FATAL,
	BLOW,
	MOG(L2SkillMagicOnGround.class),
	
	// Disablers
	BLEED,
	POISON,
	STUN,
	ROOT,
	CONFUSION,
	FEAR,
	SLEEP,
	CONFUSE_MOB_ONLY,
	MUTE,
	PARALYZE,
	WEAKNESS,
	
	// hp, mp, cp
	HEAL,
	HOT,
	BALANCE_LIFE,
	HEAL_PERCENT,
	HEAL_STATIC,
	COMBATPOINTHEAL,
	CPHOT,
	MANAHEAL,
	MANA_BY_LEVEL,
	MANAHEAL_PERCENT,
	MANARECHARGE,
	MPHOT,
	GIVE_SP,
	
	// Aggro
	AGGDAMAGE,
	AGGREDUCE,
	AGGREMOVE,
	AGGREDUCE_CHAR,
	AGGDEBUFF,
	
	// Fishing
	FISHING,
	PUMPING,
	REELING,
	
	// MISC
	UNLOCK,
	ENCHANT_ARMOR,
	ENCHANT_WEAPON,
	SOULSHOT,
	SPIRITSHOT,
	SIEGEFLAG,
	TAKECASTLE,
	WEAPON_SA,
	DELUXE_KEY_UNLOCK,
	SOW,
	HARVEST,
	GET_PLAYER,
	EXTRACTABLE,
	EXTRACTABLE_FISH,
	
	// Creation
	COMMON_CRAFT,
	DWARVEN_CRAFT,
	CREATE_ITEM(L2SkillCreateItem.class),
	SUMMON_TREASURE_KEY,
	
	// Summons
	SUMMON(L2SkillSummon.class),
	FEED_PET,
	DEATHLINK_PET,
	STRSIEGEASSAULT,
	ERASE,
	BETRAY,
	
	SPAWN(L2SkillSpawn.class),
	
	// Cancel
	CANCEL,
	MAGE_BANE,
	WARRIOR_BANE,
	NEGATE,
	
	BUFF,
	DEBUFF,
	PASSIVE,
	CONT,
	
	RESURRECT,
	CHARGE(L2SkillCharge.class),
	CHARGE_EFFECT(L2SkillChargeEffect.class),
	CHARGEDAM(L2SkillChargeDmg.class),
	CHARGE_NEGATE,
	MHOT,
	DETECT_WEAKNESS,
	LUCK,
	RECALL,
	SUMMON_FRIEND,
	REFLECT,
	SPOIL,
	SWEEP,
	FAKE_DEATH,
	UNBLEED,
	UNPOISON,
	UNDEAD_DEFENSE,
	SEED(L2SkillSeed.class),
	BEAST_FEED,
	FORCE_BUFF,
	FUSION,
	CLAN_GATE,
	
	// Done in core
	COREDONE,
	
	// unimplemented
	NOTDONE;
	
	private final Class<? extends L2Skill> _class;
	
	public L2Skill makeSkill(StatsSet set)
	{
		try
		{
			Constructor<? extends L2Skill> c = _class.getConstructor(StatsSet.class);
			
			return c.newInstance(set);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private L2SkillType()
	{
		_class = L2SkillDefault.class;
	}
	
	private L2SkillType(Class<? extends L2Skill> classType)
	{
		_class = classType;
	}
}