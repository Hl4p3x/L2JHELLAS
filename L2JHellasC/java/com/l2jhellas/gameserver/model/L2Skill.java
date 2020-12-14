package com.l2jhellas.gameserver.model;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.xml.SkillTreeData;
import com.l2jhellas.gameserver.enums.ZoneId;
import com.l2jhellas.gameserver.enums.items.L2ArmorType;
import com.l2jhellas.gameserver.enums.player.ClassId;
import com.l2jhellas.gameserver.enums.player.Position;
import com.l2jhellas.gameserver.enums.skills.HeroSkills;
import com.l2jhellas.gameserver.enums.skills.L2SkillTargetType;
import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.geodata.GeoEngine;
import com.l2jhellas.gameserver.holder.IntIntHolder;
import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.actor.instance.L2ArtefactInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2ChestInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PetInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2SiegeFlagInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jhellas.gameserver.model.actor.item.L2Item;
import com.l2jhellas.gameserver.model.actor.item.Extractable.ExtractableProductItem;
import com.l2jhellas.gameserver.model.actor.item.Extractable.ExtractableSkill;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.EtcStatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.Formulas;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.gameserver.skills.Stats;
import com.l2jhellas.gameserver.skills.conditions.Condition;
import com.l2jhellas.gameserver.skills.effects.EffectCharge;
import com.l2jhellas.gameserver.skills.effects.EffectTemplate;
import com.l2jhellas.gameserver.skills.funcs.Func;
import com.l2jhellas.gameserver.skills.funcs.FuncTemplate;
import com.l2jhellas.gameserver.templates.L2Armor;
import com.l2jhellas.gameserver.templates.StatsSet;
import com.l2jhellas.util.Util;

public abstract class L2Skill
{
	protected static final Logger _log = Logger.getLogger(L2Skill.class.getName());
	
	public static final int SKILL_CUBIC_MASTERY = 143;
	public static final int SKILL_LUCKY = 194;
	public static final int SKILL_CREATE_COMMON = 1320;
	public static final int SKILL_CREATE_DWARVEN = 172;
	public static final int SKILL_CRYSTALLIZE = 248;
	public static final int SKILL_DIVINE_INSPIRATION = 1405;
	
	public static final int SKILL_FAKE_INT = 9001;
	public static final int SKILL_FAKE_WIT = 9002;
	public static final int SKILL_FAKE_MEN = 9003;
	public static final int SKILL_FAKE_CON = 9004;
	public static final int SKILL_FAKE_DEX = 9005;
	public static final int SKILL_FAKE_STR = 9006;
	
	private final int _targetConsumeId;
	private final int _targetConsume;
	
	public static enum SkillOpType
	{
		OP_PASSIVE,
		OP_ACTIVE,
		OP_TOGGLE,
		OP_CHANCE
	}
	
	// elements
	public final static int ELEMENT_WIND = 1;
	public final static int ELEMENT_FIRE = 2;
	public final static int ELEMENT_WATER = 3;
	public final static int ELEMENT_EARTH = 4;
	public final static int ELEMENT_HOLY = 5;
	public final static int ELEMENT_DARK = 6;
	
	// save vs
	public final static int SAVEVS_INT = 1;
	public final static int SAVEVS_WIT = 2;
	public final static int SAVEVS_MEN = 3;
	public final static int SAVEVS_CON = 4;
	public final static int SAVEVS_DEX = 5;
	public final static int SAVEVS_STR = 6;
	
	// stat effected
	public final static int STAT_PATK = 301; // pAtk
	public final static int STAT_PDEF = 302; // pDef
	public final static int STAT_MATK = 303; // mAtk
	public final static int STAT_MDEF = 304; // mDef
	public final static int STAT_MAXHP = 305; // maxHp
	public final static int STAT_MAXMP = 306; // maxMp
	public final static int STAT_CURHP = 307;
	public final static int STAT_CURMP = 308;
	public final static int STAT_HPREGEN = 309; // regHp
	public final static int STAT_MPREGEN = 310; // regMp
	public final static int STAT_CASTINGSPEED = 311; // sCast
	public final static int STAT_ATKSPD = 312; // sAtk
	public final static int STAT_CRITDAM = 313; // critDmg
	public final static int STAT_CRITRATE = 314; // critRate
	public final static int STAT_FIRERES = 315; // fireRes
	public final static int STAT_WINDRES = 316; // windRes
	public final static int STAT_WATERRES = 317; // waterRes
	public final static int STAT_EARTHRES = 318; // earthRes
	public final static int STAT_HOLYRES = 336; // holyRes
	public final static int STAT_DARKRES = 337; // darkRes
	public final static int STAT_ROOTRES = 319; // rootRes
	public final static int STAT_SLEEPRES = 320; // sleepRes
	public final static int STAT_CONFUSIONRES = 321; // confusRes
	public final static int STAT_BREATH = 322; // breath
	public final static int STAT_AGGRESSION = 323; // aggr
	public final static int STAT_BLEED = 324; // bleed
	public final static int STAT_POISON = 325; // poison
	public final static int STAT_STUN = 326; // stun
	public final static int STAT_ROOT = 327; // root
	public final static int STAT_MOVEMENT = 328; // move
	public final static int STAT_EVASION = 329; // evas
	public final static int STAT_ACCURACY = 330; // accu
	public final static int STAT_COMBAT_STRENGTH = 331;
	public final static int STAT_COMBAT_WEAKNESS = 332;
	public final static int STAT_ATTACK_RANGE = 333; // rAtk
	public final static int STAT_NOAGG = 334; // noagg
	public final static int STAT_SHIELDDEF = 335; // sDef
	public final static int STAT_MP_CONSUME_RATE = 336; // Rate of mp consume per skill use
	public final static int STAT_HP_CONSUME_RATE = 337; // Rate of hp consume per skill use
	public final static int STAT_MCRITRATE = 338; // Magic Crit Rate
	
	// COMBAT DAMAGE MODIFIER SKILLS...DETECT WEAKNESS AND WEAKNESS/STRENGTH
	public final static int COMBAT_MOD_ANIMAL = 200;
	public final static int COMBAT_MOD_BEAST = 201;
	public final static int COMBAT_MOD_BUG = 202;
	public final static int COMBAT_MOD_DRAGON = 203;
	public final static int COMBAT_MOD_MONSTER = 204;
	public final static int COMBAT_MOD_PLANT = 205;
	public final static int COMBAT_MOD_HOLY = 206;
	public final static int COMBAT_MOD_UNHOLY = 207;
	public final static int COMBAT_MOD_BOW = 208;
	public final static int COMBAT_MOD_BLUNT = 209;
	public final static int COMBAT_MOD_DAGGER = 210;
	public final static int COMBAT_MOD_FIST = 211;
	public final static int COMBAT_MOD_DUAL = 212;
	public final static int COMBAT_MOD_SWORD = 213;
	public final static int COMBAT_MOD_POISON = 214;
	public final static int COMBAT_MOD_BLEED = 215;
	public final static int COMBAT_MOD_FIRE = 216;
	public final static int COMBAT_MOD_WATER = 217;
	public final static int COMBAT_MOD_EARTH = 218;
	public final static int COMBAT_MOD_WIND = 219;
	public final static int COMBAT_MOD_ROOT = 220;
	public final static int COMBAT_MOD_STUN = 221;
	public final static int COMBAT_MOD_CONFUSION = 222;
	public final static int COMBAT_MOD_DARK = 223;
	
	// conditional values
	public final static int COND_RUNNING = 0x0001;
	public final static int COND_WALKING = 0x0002;
	public final static int COND_SIT = 0x0004;
	public final static int COND_BEHIND = 0x0008;
	public final static int COND_CRIT = 0x0010;
	public final static int COND_LOWHP = 0x0020;
	public final static int COND_ROBES = 0x0040;
	public final static int COND_CHARGES = 0x0080;
	public final static int COND_SHIELD = 0x0100;
	public final static int COND_GRADEA = 0x010000;
	public final static int COND_GRADEB = 0x020000;
	public final static int COND_GRADEC = 0x040000;
	public final static int COND_GRADED = 0x080000;
	public final static int COND_GRADES = 0x100000;
	
	private static final Func[] _emptyFunctionSet = new Func[0];
	private static final L2Effect[] _emptyEffectSet = new L2Effect[0];
	
	// these two build the primary key
	private final int _id;
	private final int _level;
	
	private int _displayId;
	
	// not needed, just for easier debug
	private final String _name;
	private final SkillOpType _operateType;
	private final boolean _magic;
	private final int _mpConsume;
	private final int _mpInitialConsume;
	private final int _hpConsume;
	private final int _itemConsume;
	private final int _itemConsumeId;
	// item consume count over time
	private final int _itemConsumeOT;
	// item consume id over time
	private final int _itemConsumeIdOT;
	// how many times to consume an item
	private final int _itemConsumeSteps;
	// for summon spells:
	// a) What is the total lifetime of summons (in millisecs)
	private final int _summonTotalLifeTime;
	// b) how much lifetime is lost per second of idleness (non-fighting)
	private final int _summonTimeLostIdle;
	// c) how much time is lost per second of activity (fighting)
	private final int _summonTimeLostActive;
	
	// item consume time in milliseconds
	private final int _itemConsumeTime;
	private final int _castRange;
	private final int _effectRange;
	
	// all times in milliseconds
	private final int _hitTime;
	// private final int _skillInterruptTime;
	private final int _coolTime;
	private final int _reuseDelay;
	private final int _buffDuration;
	
	private final L2SkillTargetType _targetType;
	
	private final double _power;
	private final float _abnormalLvl;
	private final float _negateLvl;
	private final int _effectPoints;
	private final int _magicLevel;
	private final String[] _negateStats;
	private final float _negatePower;
	private final int _negateId;
	private final int _levelDepend;
	
	// Effecting area of the skill, in radius.
	// The radius center varies according to the _targetType:
	// "caster" if targetType = AURA/PARTY/CLAN or "target" if targetType = AREA
	private final int _skillRadius;
	
	private final L2SkillType _skillType;
	private final L2SkillType _effectType;
	private final int _effectPower;
	private final int _effectId;
	private final int _effectLvl;
	
	private final boolean _ispotion;
	private final int _element;
	private final int _savevs;
	
	private final int _initialEffectDelay;
	
	private final boolean _isSuicideAttack;
	private final boolean _cancelIfHit;
	
	private final Stats _stat;
	
	private final int _condition;
	private final int _conditionValue;
	private final boolean _overhit;
	private final int _weaponsAllowed;
	private final int _armorsAllowed;
	
	private final int _addCrossLearn; // -1 disable, otherwice SP price for others classes, default 1000
	private final float _mulCrossLearn; // multiplay for others classes, default 2
	private final float _mulCrossLearnRace; // multiplay for others races, default 2
	private final float _mulCrossLearnProf; // multiplay for fighter/mage missmatch, default 3
	private final List<ClassId> _canLearn; // which classes can learn
	private final List<Integer> _teachers; // which NPC teaches
	private final int _minPledgeClass;
	
	// OP Chance
	private final boolean _isOffensive;
	private final int _numCharges;
	private final int _triggeredId;
	private final int _triggeredLevel;
	private final boolean _bestow;
	private final boolean _bestowed;
	protected ChanceCondition _chanceCondition = null;
	private final int _forceId;
	private final boolean _isHeroSkill; // If true the skill is a Hero Skill
	private final boolean _isDebuff;
	private final int _baseCritRate; // percent of success for skill critical hit (especially for PDAM & BLOW - they're not affected by rCrit values or buffs). Default loads -1 for all other skills but 0 to PDAM & BLOW
	private final int _lethalEffect1; // percent of success for lethal 1st effect (hit cp to 1 or if mob hp to 50%) (only for PDAM skills)
	private final int _lethalEffect2; // percent of success for lethal 2nd effect (hit cp,hp to 1 or if mob hp to 1) (only for PDAM skills)
	private final boolean _directHpDmg; // If true then dmg is being make directly
	private final boolean _isDance; // If true then casting more dances will cost more MP
	private final int _nextDanceCost;
	private final float _sSBoost; // If true skill will have SoulShot boost (power*2)
	private final int _aggroPoints;
	
	protected Condition _preCondition;
	protected Condition _itemPreCondition;
	protected FuncTemplate[] _funcTemplates;
	protected EffectTemplate[] _effectTemplates;
	protected EffectTemplate[] _effectTemplatesSelf;
	private ExtractableSkill _extractableItems = null;

	private L2Character target;

	private L2PcInstance trg;
	
	protected L2Skill(StatsSet set)
	{
		_id = set.getInteger("skill_id");
		_level = set.getInteger("level");
		
		_displayId = set.getInteger("displayId", _id);
		_name = set.getString("name");
		_operateType = set.getEnum("operateType", SkillOpType.class);
		_magic = set.getBool("isMagic", false);
		_ispotion = set.getBool("isPotion", false);
		_mpConsume = set.getInteger("mpConsume", 0);
		_mpInitialConsume = set.getInteger("mpInitialConsume", 0);
		_hpConsume = set.getInteger("hpConsume", 0);
		_itemConsume = set.getInteger("itemConsumeCount", 0);
		_itemConsumeId = set.getInteger("itemConsumeId", 0);
		_itemConsumeOT = set.getInteger("itemConsumeCountOT", 0);
		_itemConsumeIdOT = set.getInteger("itemConsumeIdOT", 0);
		_itemConsumeTime = set.getInteger("itemConsumeTime", 0);
		_itemConsumeSteps = set.getInteger("itemConsumeSteps", 0);
		_summonTotalLifeTime = set.getInteger("summonTotalLifeTime", 1200000); // 20 minutes default
		_summonTimeLostIdle = set.getInteger("summonTimeLostIdle", 0);
		_summonTimeLostActive = set.getInteger("summonTimeLostActive", 0);
		
		_castRange = set.getInteger("castRange", 0);
		_effectRange = set.getInteger("effectRange", -1);
		
		_hitTime = set.getInteger("hitTime", 0);
		_coolTime = set.getInteger("coolTime", 0);
		_isDebuff = set.getBool("isDebuff", false);
		_initialEffectDelay = set.getInteger("initialEffectDelay", 0);
		// _skillInterruptTime = set.getInteger("hitTime", _hitTime / 2);
		_reuseDelay = set.getInteger("reuseDelay", 0);
		_buffDuration = set.getInteger("buffDuration", 0);
		_skillRadius = set.getInteger("skillRadius", 80);
		
		_targetType = set.getEnum("target", L2SkillTargetType.class);
		_power = set.getFloat("power", 0.f);
		_abnormalLvl = set.getInteger("abnormalLvl", 0);
		_negateLvl = set.getInteger("negateLvl", 0);
		_effectPoints = set.getInteger("effectPoints", 0);
		_negateStats = set.getString("negateStats", "").split(" ");
		_negatePower = set.getFloat("negatePower", 0.f);
		_negateId = set.getInteger("negateId", 0);
		_magicLevel = set.getInteger("magicLvl", SkillTreeData.getInstance().getMinSkillLevel(_id, _level));
		_levelDepend = set.getInteger("lvlDepend", 0);
		_stat = set.getEnum("stat", Stats.class, null);
		
		_skillType = set.getEnum("skillType", L2SkillType.class);
		_effectType = set.getEnum("effectType", L2SkillType.class, null);
		_effectPower = set.getInteger("effectPower", 0);
		_effectId = set.getInteger("effectId", 0);
		_effectLvl = set.getInteger("effectLevel", 0);
		
		_element = set.getInteger("element", 0);
		_savevs = set.getInteger("save", 0);
		
		_condition = set.getInteger("condition", 0);
		_conditionValue = set.getInteger("conditionValue", 0);
		_overhit = set.getBool("overHit", false);
		_isSuicideAttack = set.getBool("isSuicideAttack", false);
		_cancelIfHit = set.getBool("cancelIfHit", false);
		_weaponsAllowed = set.getInteger("weaponsAllowed", 0);
		_armorsAllowed = set.getInteger("armorsAllowed", 0);
		
		_addCrossLearn = set.getInteger("addCrossLearn", 1000);
		_mulCrossLearn = set.getFloat("mulCrossLearn", 2.f);
		_mulCrossLearnRace = set.getFloat("mulCrossLearnRace", 2.f);
		_mulCrossLearnProf = set.getFloat("mulCrossLearnProf", 3.f);
		_minPledgeClass = set.getInteger("minPledgeClass", 0);
		_isOffensive = set.getBool("offensive", isSkillTypeOffensive());
		_numCharges = set.getInteger("num_charges", getLevel());
		_triggeredId = set.getInteger("triggeredId", 0);
		_triggeredLevel = set.getInteger("triggeredLevel", 0);
		_bestow = set.getBool("bestowTriggered", false);
		_bestowed = set.getBool("bestowed", false);
		_forceId = set.getInteger("forceId", 0);
		_targetConsume = set.getInteger("targetConsumeCount", 0);
		_targetConsumeId = set.getInteger("targetConsumeId", 0);
		
		if (_operateType == SkillOpType.OP_CHANCE)
			_chanceCondition = ChanceCondition.parse(set);
		
		_isHeroSkill = HeroSkills.isHeroSkill(_id);
		
		_baseCritRate = set.getInteger("baseCritRate", (_skillType == L2SkillType.PDAM || _skillType == L2SkillType.BLOW) ? 0 : -1);
		_lethalEffect1 = set.getInteger("lethal1", 0);
		_lethalEffect2 = set.getInteger("lethal2", 0);
		
		_directHpDmg = set.getBool("dmgDirectlyToHp", false);
		_isDance = set.getBool("isDance", false);
		_nextDanceCost = set.getInteger("nextDanceCost", 0);
		_sSBoost = set.getFloat("SSBoost", 0.f);
		_aggroPoints = set.getInteger("aggroPoints", 0);
		
		String canLearn = set.getString("canLearn", null);
		if (canLearn == null)
		{
			_canLearn = null;
		}
		else
		{
			_canLearn = new ArrayList<>();
			StringTokenizer st = new StringTokenizer(canLearn, " \r\n\t,;");
			while (st.hasMoreTokens())
			{
				String cls = st.nextToken();
				try
				{
					_canLearn.add(ClassId.valueOf(cls));
				}
				catch (Throwable t)
				{
					_log.severe(L2Skill.class.getName() + ": Bad class " + cls + " to learn skill");
					if (Config.DEVELOPER)
						t.printStackTrace();
				}
			}
		}
		
		String teachers = set.getString("teachers", null);
		if (teachers == null)
		{
			_teachers = null;
		}
		else
		{
			_teachers = new ArrayList<>();
			StringTokenizer st = new StringTokenizer(teachers, " \r\n\t,;");
			while (st.hasMoreTokens())
			{
				String npcid = st.nextToken();
				try
				{
					_teachers.add(Integer.parseInt(npcid));
				}
				catch (Throwable t)
				{
					_log.severe(L2Skill.class.getName() + ": Bad teacher id " + npcid + " to teach skill");
					if (Config.DEVELOPER)
						t.printStackTrace();
				}
			}
		}
		
		String capsuled_items = set.getString("capsuled_items_skill", null);
		if (capsuled_items != null)
		{
			if (capsuled_items.isEmpty())
				_log.warning("Empty extractable data for skill: " + _id);
			
			_extractableItems = parseExtractableSkill(_id, _level, capsuled_items);
		}
	}
	
	public abstract void useSkill(L2Character caster, L2Object[] targets);
	
	public final boolean isPotion()
	{
		return _ispotion;
	}
	
	public final int getArmorsAllowed()
	{
		return _armorsAllowed;
	}
	
	public final int getConditionValue()
	{
		return _conditionValue;
	}
	
	public final L2SkillType getSkillType()
	{
		return _skillType;
	}
	
	public final int getSavevs()
	{
		return _savevs;
	}
	
	public final int getElement()
	{
		return _element;
	}
	
	public final L2SkillTargetType getTargetType()
	{
		return _targetType;
	}
	
	public final int getCondition()
	{
		return _condition;
	}
	
	public final boolean isOverhit()
	{
		return _overhit;
	}
	
	public final boolean isCancelIfHit()
	{
		return _cancelIfHit;
	}
	
	public final boolean isSuicideAttack()
	{
		return _isSuicideAttack;
	}
	
	public final double getPower(L2Character activeChar)
	{
		if (_skillType == L2SkillType.DEATHLINK && activeChar != null)
			return _power * Math.pow(1.7165 - activeChar.getCurrentHp() / activeChar.getMaxHp(), 2) * 0.577;
		else if (_skillType == L2SkillType.FATAL && activeChar != null)
			return _power * Math.pow(1.7165 - activeChar.getCurrentHp() / activeChar.getMaxHp(), 2) * 0.577;
		else
			return _power;
	}
	
	public final double getPower()
	{
		return _power;
	}
	
	public final double getAbnormal()
	{
		return _abnormalLvl;
	}
	
	public final double getNegateLvl()
	{
		return _negateLvl;
	}
	
	public final int getEffectPoints()
	{
		return _effectPoints;
	}
	
	public final String[] getNegateStats()
	{
		return _negateStats;
	}
	
	public final float getNegatePower()
	{
		return _negatePower;
	}
	
	public final int getNegateId()
	{
		return _negateId;
	}
	
	public final int getMagicLevel()
	{
		return _magicLevel;
	}
	
	public final int getLevelDepend()
	{
		return _levelDepend;
	}
	
	public final int getEffectPower()
	{
		return _effectPower;
	}
	
	public final int getEffectId()
	{
		return _effectId;
	}
	
	public final int getEffectLvl()
	{
		return _effectLvl;
	}
	
	public final L2SkillType getEffectType()
	{
		return _effectType;
	}
	
	public final int getBuffDuration()
	{
		return _buffDuration;
	}
	
	public final int getCastRange()
	{
		return _castRange;
	}
	
	public final int getEffectRange()
	{
		return _effectRange;
	}
	
	public final int getHpConsume()
	{
		return _hpConsume;
	}
	
	public final int getId()
	{
		return _id;
	}
	
	public final boolean isDebuff()
	{
		return _isDebuff;
	}
	
	public int getDisplayId()
	{
		return _displayId;
	}
	
	public void setDisplayId(int id)
	{
		_displayId = id;
	}
	
	public int getTriggeredId()
	{
		return _triggeredId;
	}
	
	public int getTriggeredLevel()
	{
		return _triggeredLevel;
	}
	
	public boolean bestowTriggered()
	{
		return _bestow;
	}
	
	public boolean bestowed()
	{
		return _bestowed;
	}
	
	public boolean triggerAnotherSkill()
	{
		return _triggeredId > 1;
	}
	
	public int getForceId()
	{
		return _forceId;
	}
	
	public final int getInitialEffectDelay()
	{
		return _initialEffectDelay;
	}
	
	public final Stats getStat()
	{
		return _stat;
	}
	
	public final int getItemConsume()
	{
		return _itemConsume;
	}
	
	public final int getItemConsumeId()
	{
		return _itemConsumeId;
	}
	
	public final int getItemConsumeOT()
	{
		return _itemConsumeOT;
	}
	
	public final int getItemConsumeIdOT()
	{
		return _itemConsumeIdOT;
	}
	
	public final int getItemConsumeSteps()
	{
		return _itemConsumeSteps;
	}
	
	public final int getTotalLifeTime()
	{
		return _summonTotalLifeTime;
	}
	
	public final int getTimeLostIdle()
	{
		return _summonTimeLostIdle;
	}
	
	public final int getTimeLostActive()
	{
		return _summonTimeLostActive;
	}
	
	public final int getItemConsumeTime()
	{
		return _itemConsumeTime;
	}
	
	public final int getLevel()
	{
		return _level;
	}
	
	public final boolean isMagic()
	{
		return _magic;
	}
	
	public final int getMpConsume()
	{
		return _mpConsume;
	}
	
	public final int getMpInitialConsume()
	{
		return _mpInitialConsume;
	}
	
	public final String getName()
	{
		return _name;
	}
	
	public final int getReuseDelay()
	{
		return _reuseDelay;
	}
	
	@Deprecated
	public final int getSkillTime()
	{
		return _hitTime;
	}
	
	public final int getHitTime()
	{
		return _hitTime;
	}
	
	public final int getCoolTime()
	{
		return _coolTime;
	}
	
	public final int getSkillRadius()
	{
		return _skillRadius;
	}
	
	public final boolean isActive()
	{
		return _operateType == SkillOpType.OP_ACTIVE;
	}
	
	public final boolean isPassive()
	{
		return _operateType == SkillOpType.OP_PASSIVE;
	}
	
	public final boolean isToggle()
	{
		return _operateType == SkillOpType.OP_TOGGLE;
	}
	
	public final boolean isChance()
	{
		return _operateType == SkillOpType.OP_CHANCE;
	}
	
	public ChanceCondition getChanceCondition()
	{
		return _chanceCondition;
	}
	
	public final boolean isDance()
	{
		return _isDance;
	}
	
	public final int getNextDanceMpCost()
	{
		return _nextDanceCost;
	}
	
	public final float getSSBoost()
	{
		return _sSBoost;
	}
	
	public final int getAggroPoints()
	{
		return _aggroPoints;
	}
	
	public final boolean useSoulShot()
	{
		return ((getSkillType() == L2SkillType.PDAM) || (getSkillType() == L2SkillType.STUN) || (getSkillType() == L2SkillType.CHARGEDAM) || (getSkillType() == L2SkillType.BLOW));
	}
	
	public final boolean useSpiritShot()
	{
		return isMagic();
	}
	
	public final boolean useFishShot()
	{
		return ((getSkillType() == L2SkillType.PUMPING) || (getSkillType() == L2SkillType.REELING));
	}
	
	public final int getWeaponsAllowed()
	{
		return _weaponsAllowed;
	}
	
	public final int getCrossLearnAdd()
	{
		return _addCrossLearn;
	}
	
	public final float getCrossLearnMul()
	{
		return _mulCrossLearn;
	}
	
	public final float getCrossLearnRace()
	{
		return _mulCrossLearnRace;
	}
	
	public final float getCrossLearnProf()
	{
		return _mulCrossLearnProf;
	}
	
	public final boolean getCanLearn(ClassId cls)
	{
		return _canLearn == null || _canLearn.contains(cls);
	}
	
	public final boolean canTeachBy(int npcId)
	{
		return _teachers == null || _teachers.contains(npcId);
	}
	
	public int getMinPledgeClass()
	{
		return _minPledgeClass;
	}
	
	public final boolean isPvpSkill()
	{
		switch (_skillType)
		{
			case DOT:
			case AGGREDUCE:
			case AGGDAMAGE:
			case AGGREDUCE_CHAR:
			case CONFUSE_MOB_ONLY:
			case BLEED:
			case CONFUSION:
			case POISON:
			case DEBUFF:
			case AGGDEBUFF:
			case STUN:
			case ROOT:
			case FEAR:
			case SLEEP:
			case MDOT:
			case MUTE:
			case WEAKNESS:
			case PARALYZE:
			case CANCEL:
			case MAGE_BANE:
			case WARRIOR_BANE:
			case BETRAY:
				return true;
			default:
				return false;
		}
	}
	
	public final boolean isBuffSkill()
	{
		switch (_skillType) 
		{
		   case BUFF:
		   case COMBATPOINTHEAL:
		   case REFLECT:
		   case HEAL_PERCENT:
		   case HEAL_STATIC:
		   case MANAHEAL_PERCENT:
			   return true;
		   default:
			   return false;
		}		
	}
	
	public final boolean isOffensive()
	{
		return _isOffensive;
	}
	
	public final boolean isHeroSkill()
	{
		return _isHeroSkill;
	}
	
	public final int getNumCharges()
	{
		return _numCharges;
	}
	
	public final int getBaseCritRate()
	{
		return _baseCritRate;
	}
	
	public final int getLethalChance1()
	{
		return _lethalEffect1;
	}
	
	public final int getLethalChance2()
	{
		return _lethalEffect2;
	}
	
	public final boolean getDmgDirectlyToHP()
	{
		return _directHpDmg;
	}
	
	public final boolean isSkillTypeOffensive()
	{
		switch (_skillType)
		{
			case PDAM:
			case MDAM:
			case CPDAM:
			case DOT:
			case BLEED:
			case POISON:
			case AGGDAMAGE:
			case DEBUFF:
			case AGGDEBUFF:
			case STUN:
			case ROOT:
			case CONFUSION:
			case ERASE:
			case BLOW:
			case FEAR:
			case DRAIN:
			case SLEEP:
			case FATAL:
			case CHARGEDAM:
			case CONFUSE_MOB_ONLY:
			case DEATHLINK:
			case DETECT_WEAKNESS:
			case MANADAM:
			case MDOT:
			case MUTE:
			case SOULSHOT:
			case SPIRITSHOT:
			case SPOIL:
			case WEAKNESS:
			case MANA_BY_LEVEL:
			case SWEEP:
			case PARALYZE:
			case DRAIN_SOUL:
			case AGGREDUCE:
			case CANCEL:
			case MAGE_BANE:
			case WARRIOR_BANE:
			case AGGREMOVE:
			case AGGREDUCE_CHAR:
			case BETRAY:
			case DELUXE_KEY_UNLOCK:
			case SOW:
			case HARVEST:
				return true;
			default:
				return isDebuff();
		}
	}
	
	public final boolean getWeaponDependancy(L2Character activeChar)
	{
		// check to see if skill has a weapon dependency.
		final int weaponsAllowed = getWeaponsAllowed();
		
		if (weaponsAllowed == 0)
			return true;
		
		int mask = 0;
		
		if (activeChar.getActiveWeaponItem() != null)
			mask |= activeChar.getActiveWeaponItem().getItemType().mask();
		
		final L2Item shield = activeChar.getSecondaryWeaponItem();
		if (shield != null && shield instanceof L2Armor)
			mask |= ((L2ArmorType) shield.getItemType()).mask();
		
		if ((mask & weaponsAllowed) != 0)
			return true;
		
		activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(this));
		return false;
	}
	
	public boolean checkCondition(L2Character activeChar, L2Object target, boolean itemOrWeapon)
	{
		if ((getCondition() & L2Skill.COND_SHIELD) != 0)
		{
			
		}
		
		Condition preCondition = _preCondition;
		if (itemOrWeapon)
			preCondition = _itemPreCondition;
		if (preCondition == null)
			return true;
		
		Env env = new Env();
		env.player = activeChar;
		if (target instanceof L2Character)
			env.target = (L2Character) target;
		env.skill = this;
		
		if (!preCondition.test(env))
		{
			String msg = preCondition.getMessage();
			if (msg != null)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_S2);
				sm.addString(msg);
				activeChar.sendPacket(sm);
			}
			return false;
		}
		return true;
	}

	@SuppressWarnings("cast")
	public final L2Object[] getTargetList(L2Character activeChar, boolean onlyFirst)
	{
		List<L2Character> targetList = new ArrayList<>();
		
		// Get the target type of the skill
		// (ex : ONE, SELF, HOLY, PET, AURA, AURA_CLOSE, AREA, MULTIFACE, PARTY, CLAN, CORPSE_PLAYER, CORPSE_MOB, CORPSE_CLAN, UNLOCKABLE, ITEM, UNDEAD)
		L2SkillTargetType targetType = getTargetType();
		
		target = null;

		L2Object objTarget = activeChar.getTarget();
		
		// Get the type of the skill
		// (ex : PDAM, MDAM, DOT, BLEED, POISON, HEAL, HOT, MANAHEAL, MANARECHARGE, AGGDAMAGE, BUFF, DEBUFF, STUN, ROOT, RESURRECT, PASSIVE...)
		L2SkillType skillType = getSkillType();
		
		// If the L2Object targeted is a L2Character, it becomes the L2Character target
		if (objTarget instanceof L2Character)
		{
			target = (L2Character) objTarget;
		}
		
		switch (targetType)
		{
		// The skill can only be used on the L2Character targeted, or on the caster itself
			case TARGET_ONE:
			{
				boolean canTargetSelf = false;
				switch (skillType)
				{
					case BUFF:
					case HEAL:
					case HOT:
					case HEAL_PERCENT:
					case MANARECHARGE:
					case MANAHEAL:
					case NEGATE:
					case REFLECT:
					case UNBLEED:
					case UNPOISON:
					case SEED:
					case COMBATPOINTHEAL:
					case MAGE_BANE:
					case WARRIOR_BANE:
					case BETRAY:
					case BALANCE_LIFE:
					case FORCE_BUFF:
					case GIVE_SP:
						canTargetSelf = true;
						break;
					default:
						canTargetSelf = false;
						break;
				}
				
				if (target == null)
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_CANT_FOUND));
					return null;
				}

				if (target.isDead() || (target == activeChar && !canTargetSelf))
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return null;
				}
				
				// If a target is found, return it in a table else send a system message TARGET_IS_INCORRECT
				return new L2Character[]
				{
					target
				};
			}
			case TARGET_SELF:
			{
				return new L2Character[]
				{
					activeChar
				};
			}
			case TARGET_HOLY:
			{
				if (activeChar instanceof L2PcInstance)
				{
					if (activeChar.getTarget() instanceof L2ArtefactInstance)
						return new L2Character[]
						{
							(L2ArtefactInstance) activeChar.getTarget()
						};
				}
				
				return null;
			}
			case TARGET_PET:
			{
				target = activeChar.getPet();
				if (target != null && !target.isDead())
					return new L2Character[]
					{
						target
					};
				
				return null;
			}
			case TARGET_OWNER_PET:
			{
				if (activeChar instanceof L2Summon)
				{
					target = ((L2Summon) activeChar).getOwner();
					if (target != null && !target.isDead())
						return new L2Character[]
						{
							target
						};
				}
				
				return null;
			}
			case TARGET_CORPSE_PET:
			{
				if (activeChar instanceof L2PcInstance)
				{
					target = activeChar.getPet();
					if (target != null && target.isDead())
						return new L2Character[]
						{
							target
						};
				}
				
				return null;
			}
			case TARGET_AURA:
			{
				int radius = getSkillRadius();
				boolean srcInArena = (activeChar.isInsideZone(ZoneId.PVP) && !activeChar.isInsideZone(ZoneId.SIEGE));
				
				L2PcInstance src = null;
				if (activeChar instanceof L2PcInstance)
					src = (L2PcInstance) activeChar;
				if (activeChar instanceof L2Summon)
					src = ((L2Summon) activeChar).getOwner();
				
				// Go through the L2Character _knownList
				for (L2Object obj : L2World.getInstance().getVisibleObjects(activeChar, L2Object.class, radius))
				{
					if (src instanceof L2PcInstance && obj != null && (obj instanceof L2Attackable || obj instanceof L2Playable))
					{
						
						// Don't add this target if this is a Pc->Pc pvp casting and pvp condition not met
						if (obj == activeChar || obj == src)
							continue;

						if (((Config.GEODATA) ? !GeoEngine.canSeeTarget(activeChar, obj, activeChar.isFlying()) : !GeoEngine.canSeeTarget(activeChar, obj)))
								continue;
						
							// check if both attacker and target are L2PcInstances and if they are in same party
							if (obj instanceof L2PcInstance)
							{
								if (!src.checkPvpSkill(obj, this))
									continue;
								if ((src.getParty() != null && ((L2PcInstance) obj).getParty() != null) && src.getParty().getPartyLeaderOID() == ((L2PcInstance) obj).getParty().getPartyLeaderOID())
									continue;
								if (!srcInArena && !(((L2Character) obj).isInsideZone(ZoneId.PVP) && !((L2Character) obj).isInsideZone(ZoneId.SIEGE)))
								{
									if (src.getClanId() != 0 && src.getClanId() == ((L2PcInstance) obj).getClanId())
										continue;
								}
							}
							if (obj instanceof L2Summon)
							{
								L2PcInstance trg = ((L2Summon) obj).getOwner();
								if (trg == src)
									continue;
								if (!src.checkPvpSkill(trg, this))
									continue;
								if ((src.getParty() != null && trg.getParty() != null) && src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
									continue;
								if (!srcInArena && !(((L2Character) obj).isInsideZone(ZoneId.PVP) && !((L2Character) obj).isInsideZone(ZoneId.SIEGE)))
								{
									if (src.getClanId() != 0 && src.getClanId() == trg.getClanId())
										continue;
								}
							}
						
						if (!Util.checkIfInRange(radius, activeChar, obj, true))
							continue;
						
						if (onlyFirst == false)
							targetList.add((L2Character) obj);
						else
							return new L2Character[]
							{
								(L2Character) obj
							};
					}
				}
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_AREA:
			{
				if ((!(target instanceof L2Attackable || target instanceof L2Playable)) || (getCastRange() >= 0 && (target == null || target == activeChar || target.isAlikeDead()))) // target is null or self or dead/faking
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return null;
				}
				
				L2Character cha;
				
				if (getCastRange() >= 0)
				{
					cha = target;
					
					if (!onlyFirst)
						targetList.add(cha); // Add target to target list
					else
						return new L2Character[]
						{
							cha
						};
				}
				else
					cha = activeChar;
				
				boolean effectOriginIsL2PlayableInstance = (cha instanceof L2Playable);
				
				L2PcInstance src = null;
				if (activeChar instanceof L2PcInstance)
					src = (L2PcInstance) activeChar;
				else if (activeChar instanceof L2Summon)
					src = ((L2Summon) activeChar).getOwner();
				
				int radius = getSkillRadius();
				
				boolean srcInArena = (activeChar.isInsideZone(ZoneId.PVP) && !activeChar.isInsideZone(ZoneId.SIEGE));
				
				for (L2Object obj : L2World.getInstance().getVisibleObjects(activeChar, L2Object.class, radius))
				{
					if (obj == null)
						continue;
					if (!(obj instanceof L2Attackable || obj instanceof L2Playable))
						continue;
					if (obj == cha)
						continue;
					target = (L2Character) obj;
					
					if (((Config.GEODATA) ? !GeoEngine.canSeeTarget(activeChar, obj, activeChar.isFlying()) : !GeoEngine.canSeeTarget(activeChar, obj)))
						continue;
					
					if (!target.isAlikeDead() && (target != activeChar))
					{
						if (src != null) // caster is l2playableinstance and exists
						{
							
							if (obj instanceof L2PcInstance)
							{
								L2PcInstance trg = (L2PcInstance) obj;
								if (trg == src)
									continue;
								if ((src.getParty() != null && trg.getParty() != null) && src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
									continue;
								
								if (trg.isInsideZone(ZoneId.PEACE))
									continue;
								
								if (!srcInArena && !(trg.isInsideZone(ZoneId.PVP) && !trg.isInsideZone(ZoneId.SIEGE)))
								{
									if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0)
										continue;
									
									if (src.getClan() != null && trg.getClan() != null)
									{
										if (src.getClan().getClanId() == trg.getClan().getClanId())
											continue;
									}
									
									if (!src.checkPvpSkill(obj, this))
										continue;
								}
							}
							if (obj instanceof L2Summon)
							{
								L2PcInstance trg = ((L2Summon) obj).getOwner();
								if (trg == src)
									continue;
								
								if ((src.getParty() != null && trg.getParty() != null) && src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
									continue;
								
								if (!srcInArena && !(trg.isInsideZone(ZoneId.PVP) && !trg.isInsideZone(ZoneId.SIEGE)))
								{
									if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0)
										continue;
									
									if (src.getClan() != null && trg.getClan() != null)
									{
										if (src.getClan().getClanId() == trg.getClan().getClanId())
											continue;
									}
									
									if (!src.checkPvpSkill(trg, this))
										continue;
								}
								
								if (((L2Summon) obj).isInsideZone(ZoneId.PEACE))
									continue;
							}
						}
						else
						// Skill user is not L2PlayableInstance
						{
							if (effectOriginIsL2PlayableInstance && // If effect starts at L2PlayableInstance and
							!(obj instanceof L2Playable)) // Object is not L2PlayableInstance
								continue;
						}
						
						targetList.add((L2Character) obj);
					}
				}
				
				if (targetList.size() == 0)
					return null;
				
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_MULTIFACE:
			{
				if ((!(target instanceof L2Attackable) && !(target instanceof L2PcInstance)))
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return null;
				}
				
				if (onlyFirst == false)
					targetList.add(target);
				else
					return new L2Character[]
					{
						target
					};
				
				int radius = getSkillRadius();
				for (L2Character obj : L2World.getInstance().getVisibleObjects(activeChar, L2Character.class, radius))
				{
					if (obj == null)
						continue;
					
					if (!Util.checkIfInRange(radius, activeChar, obj, true))
						continue;
					
					final Position position = Position.getPosition(activeChar,obj);
					final boolean isFront = position != Position.BACK;
					
					if (isFront && obj instanceof L2Attackable && obj != target)
						targetList.add((L2Character) obj);
					
					if (targetList.size() == 0)
					{
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_CANT_FOUND));
						return null;
					}
				}
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_PARTY:
			{
				if (onlyFirst)
					return new L2Character[]
					{
						activeChar
					};
				
				targetList.add(activeChar);
				
				L2PcInstance player = null;
				
				if (activeChar instanceof L2Summon)
				{
					player = ((L2Summon) activeChar).getOwner();
					targetList.add(player);
				}
				else if (activeChar instanceof L2PcInstance)
				{
					player = (L2PcInstance) activeChar;
					if (activeChar.getPet() != null)
						targetList.add(activeChar.getPet());
				}
				
				if (activeChar.getParty() != null)
				{
					// Get all visible objects in a spheric area near the L2Character
					// Get a list of Party Members
					List<L2PcInstance> partyList = activeChar.getParty().getPartyMembers();
					
					for (L2PcInstance partyMember : partyList)
					{
						if (partyMember == null)
							continue;
						if (partyMember == player)
							continue;
						if (!partyMember.isDead() && Util.checkIfInRange(getSkillRadius(), activeChar, partyMember, true))
						{
							targetList.add(partyMember);
							if (partyMember.getPet() != null && !partyMember.getPet().isDead())
							{
								targetList.add(partyMember.getPet());
							}
						}
					}
				}
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_PARTY_MEMBER:
			{
				if ((target != null && target == activeChar) || (target != null && activeChar.getParty() != null && target.getParty() != null && activeChar.getParty().getPartyLeaderOID() == target.getParty().getPartyLeaderOID()) || (target != null && activeChar instanceof L2PcInstance && target instanceof L2Summon && activeChar.getPet() == target) || (target != null && activeChar instanceof L2Summon && target instanceof L2PcInstance && activeChar == target.getPet()))
				{
					if (!target.isDead())
					{
						// If a target is found, return it in a table else send a system message TARGET_IS_INCORRECT
						return new L2Character[]
						{
							target
						};
					}
					return null;
				}
				
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				return null;
			}
			case TARGET_PARTY_OTHER:
			{
				if (target != null && target != activeChar && activeChar.getParty() != null && target.getParty() != null && activeChar.getParty().getPartyLeaderOID() == target.getParty().getPartyLeaderOID())
				{
					if (!target.isDead())
					{
						if (target instanceof L2PcInstance)
						{
							L2PcInstance player = (L2PcInstance) target;
							switch (getId())
							{
								case 426:
									if (!player.isMageClass())
										return new L2Character[]
										{
											target
										};
									return null;
								case 427:
									if (player.isMageClass())
										return new L2Character[]
										{
											target
										};
									return null;
							}
						}
						return new L2Character[]
						{
							target
						};
					}
					return null;
				}
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				return null;
			}
			case TARGET_CORPSE_ALLY:
			case TARGET_ALLY:
			{
				if (activeChar instanceof L2PcInstance)
				{
					int radius = getSkillRadius();
					L2PcInstance player = (L2PcInstance) activeChar;
					L2Clan clan = player.getClan();
					
					if (player.isInOlympiadMode())
						return new L2Character[]
						{
							player
						};
					
					if (targetType != L2SkillTargetType.TARGET_CORPSE_ALLY)
					{
						if (onlyFirst == false)
							targetList.add(player);
						else
							return new L2Character[]
							{
								player
							};
					}
					
					if (clan != null)
					{
						// Get all visible objects in a spheric area near the L2Character
						// Get Clan Members
						for (L2PcInstance newTarget : L2World.getInstance().getVisibleObjects(activeChar, L2PcInstance.class, radius))
						{
							if (newTarget == null)
								continue;
							
							if ((newTarget.getAllyId() == 0 || (newTarget.getAllyId() != player.getAllyId()) && newTarget.getClan() == null || newTarget.getClanId() != player.getClanId()))
								continue;
							
							if (player.isInDuel() && (player.getDuelId() != ((L2PcInstance) newTarget).getDuelId() || (player.getParty() != null && !player.getParty().getPartyMembers().contains(newTarget))))
								continue;

							if (targetType == L2SkillTargetType.TARGET_CORPSE_ALLY)
							{
								if (!newTarget.isDead())
									continue;
								
								if (getSkillType() == L2SkillType.RESURRECT)
								{
									// check target is not in a active siege zone
									if (newTarget.isInsideZone(ZoneId.SIEGE))
										continue;
								}
							}
							
							// Don't add this target if this is a Pc->Pc pvp casting and pvp condition not met
							if (!player.checkPvpSkill(newTarget, this))
								continue;
							
							if (onlyFirst == false)
								targetList.add((L2Character) newTarget);
							else
								return new L2Character[]
								{
									(L2Character) newTarget
								};
						}
					}
				}
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_CORPSE_CLAN:
			case TARGET_CLAN:
			{
				if (activeChar instanceof L2PcInstance)
				{
					int radius = getSkillRadius();
					L2PcInstance player = (L2PcInstance) activeChar;
					L2Clan clan = player.getClan();
					
					if (player.isInOlympiadMode())
						return new L2Character[]
						{
							player
						};
					
					if (targetType != L2SkillTargetType.TARGET_CORPSE_CLAN)
					{
						if (onlyFirst == false)
							targetList.add(player);
						else
							return new L2Character[]
							{
								player
							};
					}
					
					if (clan != null)
					{
						// Get all visible objects in a spheric area near the L2Character
						// Get Clan Members
						for (L2ClanMember member : clan.getMembers())
						{
							L2PcInstance newTarget = member.getPlayerInstance();
							
							if (newTarget == null)
								continue;
							
							if (targetType == L2SkillTargetType.TARGET_CORPSE_CLAN)
							{
								if (!newTarget.isDead())
									continue;
								if (getSkillType() == L2SkillType.RESURRECT)
								{
									// check target is not in a active siege zone
									if (newTarget.isInsideZone(ZoneId.SIEGE))
										continue;
								}
							}
							if (player.isInDuel() && (player.getDuelId() != newTarget.getDuelId() || (player.getParty() != null && !player.getParty().getPartyMembers().contains(newTarget))))
								continue;
							
							if (!Util.checkIfInRange(radius, activeChar, newTarget, true))
								continue;
							
							// Don't add this target if this is a Pc->Pc pvp casting and pvp condition not met
							if (!player.checkPvpSkill(newTarget, this))
								continue;
							
							if (onlyFirst == false)
								targetList.add(newTarget);
							else
								return new L2Character[]
								{
									newTarget
								};
						}
					}
				}
				
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_CORPSE_PLAYER:
			{
				if (target != null && target.isDead())
				{
					L2PcInstance player = null;
					
					if (activeChar instanceof L2PcInstance)
						player = (L2PcInstance) activeChar;
					L2PcInstance targetPlayer = null;
					
					if (target instanceof L2PcInstance)
						targetPlayer = (L2PcInstance) target;
					L2PetInstance targetPet = null;
					
					if (target instanceof L2PetInstance)
						targetPet = (L2PetInstance) target;
					
					if (player != null && (targetPlayer != null || targetPet != null))
					{
						boolean condGood = true;
						
						if (getSkillType() == L2SkillType.RESURRECT)
						{
							// check target is not in a active siege zone
							if (target.isInsideZone(ZoneId.SIEGE))
							{
								condGood = false;
								player.sendPacket(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE);
							}
							
							if (targetPlayer != null)
							{
								if (targetPlayer.isReviveRequested())
								{
									if (targetPlayer.isRevivingPet())
										player.sendPacket(SystemMessageId.MASTER_CANNOT_RES); // While a pet is attempting to resurrect, it cannot help in resurrecting its master.
									else
										player.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED); // Resurrection is already been proposed.
									condGood = false;
								}
							}
							else if (targetPet != null)
							{
								if (targetPet.getOwner() != player)
								{
									condGood = false;
									player.sendMessage("You are not the owner of this pet");
								}
							}
						}
						
						if (condGood)
						{
							if (onlyFirst == false)
							{
								targetList.add(target);
								return targetList.toArray(new L2Object[targetList.size()]);
							}
							return new L2Character[]
							{
								target
							};
							
						}
					}
				}
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				return null;
			}
			case TARGET_CORPSE_MOB:
			{
				if (!(target instanceof L2Attackable) || !target.isDead())
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return null;
				}
				
				if (onlyFirst == false)
				{
					targetList.add(target);
					return targetList.toArray(new L2Object[targetList.size()]);
				}
				return new L2Character[]
				{
					target
				};
				
			}
			case TARGET_AREA_CORPSE_MOB:
			{
				if ((!(target instanceof L2Attackable)) || !target.isDead())
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return null;
				}
				
				if (onlyFirst == false)
					targetList.add(target);
				else
					return new L2Character[]
					{
						target
					};
				
				boolean srcInArena = (activeChar.isInsideZone(ZoneId.PVP) && !activeChar.isInsideZone(ZoneId.SIEGE));
				L2PcInstance src = null;
				if (activeChar instanceof L2PcInstance)
					src = (L2PcInstance) activeChar;
				trg = null;
				
				int radius = getSkillRadius();
				
				for (L2Object obj : L2World.getInstance().getVisibleObjects(activeChar, L2Object.class, radius))
				{
					if (obj == null)
						continue;
					if (!(obj instanceof L2Attackable || obj instanceof L2Playable) || ((L2Character) obj).isDead() || ((L2Character) obj) == activeChar)
						continue;
					
					if (((Config.GEODATA) ? !GeoEngine.canSeeTarget(activeChar, obj, activeChar.isFlying()) : !GeoEngine.canSeeTarget(activeChar, obj)))
						continue;
					
					if (obj instanceof L2PcInstance && src != null)
					{
						trg = (L2PcInstance) obj;
						
						if ((src.getParty() != null && trg.getParty() != null) && src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
							continue;
						
						if (trg.isInsideZone(ZoneId.PEACE))
							continue;
						
						if (!srcInArena && !(trg.isInsideZone(ZoneId.PVP) && !trg.isInsideZone(ZoneId.SIEGE)))
						{
							if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0)
								continue;
							
							if (src.getClan() != null && trg.getClan() != null)
							{
								if (src.getClan().getClanId() == trg.getClan().getClanId())
									continue;
							}
							
							if (!src.checkPvpSkill(obj, this))
								continue;
						}
					}
					if (obj instanceof L2Summon && src != null)
					{
						trg = ((L2Summon) obj).getOwner();
						
						if ((src.getParty() != null && trg.getParty() != null) && src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
							continue;
						
						if (!srcInArena && !(trg.isInsideZone(ZoneId.PVP) && !trg.isInsideZone(ZoneId.SIEGE)))
						{
							if (src.getAllyId() == trg.getAllyId() && src.getAllyId() != 0)
								continue;
							
							if (src.getClan() != null && trg.getClan() != null)
							{
								if (src.getClan().getClanId() == trg.getClan().getClanId())
									continue;
							}
							
							if (!src.checkPvpSkill(trg, this))
								continue;
						}
						
						if (((L2Summon) obj).isInsideZone(ZoneId.PEACE))
							continue;
					}
					
					targetList.add((L2Character) obj);
				}
				
				if (targetList.size() == 0)
					return null;
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_UNLOCKABLE:
			{
				if (!(target instanceof L2DoorInstance) && !(target instanceof L2ChestInstance))
				{
					// activeChar.sendPacket(SystemMessage.TARGET_IS_INCORRECT));
					return null;
				}
				
				if (onlyFirst == false)
				{
					targetList.add(target);
					return targetList.toArray(new L2Object[targetList.size()]);
				}
				return new L2Character[]
				{
					target
				};
				
			}
			case TARGET_ITEM:
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_S2);
				sm.addString("Target type of skill is not currently handled");
				activeChar.sendPacket(sm);
				return null;
			}
			case TARGET_UNDEAD:
			{
				if (target instanceof L2Npc || target instanceof L2SummonInstance)
				{
					if (!target.isUndead() || target.isDead())
					{
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
						return null;
					}
					
					if (onlyFirst == false)
						targetList.add(target);
					else
						return new L2Character[]
						{
							target
						};
					return targetList.toArray(new L2Object[targetList.size()]);
				}
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
				return null;
			}
			case TARGET_AREA_UNDEAD:
			{
				L2Character cha;
				int radius = getSkillRadius();
				if (getCastRange() >= 0 && (target instanceof L2Npc || target instanceof L2SummonInstance) && target.isUndead() && !target.isAlikeDead())
				{
					cha = target;
					if (onlyFirst == false)
						targetList.add(cha); // Add target to target list
					else
						return new L2Character[]
						{
							cha
						};
					
				}
				else
					cha = activeChar;
				
				for (L2Object obj : L2World.getInstance().getVisibleObjects(activeChar, L2Object.class, radius))
				{
					if (obj == null)
						continue;
					if (obj instanceof L2Npc)
						target = (L2Npc) obj;
					else if (obj instanceof L2SummonInstance)
						target = (L2SummonInstance) obj;
					else
						continue;
					
					if (((Config.GEODATA) ? !GeoEngine.canSeeTarget(activeChar, obj, activeChar.isFlying()) : !GeoEngine.canSeeTarget(activeChar, obj)))
						continue;
					
					if (!target.isAlikeDead()) // If target is not dead/fake death and not self
					{
						if (!target.isUndead())
							continue;
						
						if (onlyFirst == false)
							targetList.add((L2Character) obj); // Add obj to target lists
						else
							return new L2Character[]
							{
								(L2Character) obj
							};
					}
				}
				
				if (targetList.size() == 0)
					return null;
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_ENEMY_SUMMON:
			{
				if (target != null && target instanceof L2Summon)
				{
					L2Summon targetSummon = (L2Summon) target;
					if (targetSummon.getOwner().getPvpFlag() != 0 || targetSummon.getOwner().getKarma() > 0 || targetSummon.getOwner().isInsideZone(ZoneId.PVP))
						return new L2Character[]
						{
							targetSummon
						};
				}
				else
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_S2);
					sm.addString("Target is not your enemy or not a summon");
					activeChar.sendPacket(sm);
					return null;
				}
			}
			case TARGET_AREA_ANGEL:
			{
				L2Character cha;
				int radius = getSkillRadius();
				if (getCastRange() >= 0 && target.getSkillLevel(4416) == 8 && !target.isAlikeDead())
				{
					cha = target;
					if (onlyFirst == false)
						targetList.add(cha); // Add target to target list
					else
						return new L2Character[]
						{
							cha
						};
					
				}
				else
					cha = activeChar;
				
				for (L2Object obj : L2World.getInstance().getVisibleObjects(activeChar, L2Object.class, radius))
				{
					if (obj == null)
						continue;
					
					if (((Config.GEODATA) ? !GeoEngine.canSeeTarget(activeChar, obj, activeChar.isFlying()) : !GeoEngine.canSeeTarget(activeChar, obj)))
						continue;
					
					if (!target.isAlikeDead()) // If target is not dead/fake death and not self
					{
						if (target.isUndead())
							continue; // Go to next obj if obj isn't a angel
							
						if (onlyFirst == false)
							targetList.add((L2Character) obj); // Add obj to target lists
						else
							return new L2Character[]
							{
								(L2Character) obj
							};
					}
				}
				
				if (targetList.size() == 0)
					return null;
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			default:
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_S2);
				sm.addString("Target type of skill is not currently handled");
				activeChar.sendPacket(sm);
				return null;
			}
		}// end switch
	}
	
	public final L2Object[] getTargetList(L2Character activeChar)
	{
		return getTargetList(activeChar, false);
	}
	
	public final L2Object getFirstOfTargetList(L2Character activeChar)
	{
		L2Object[] targets = getTargetList(activeChar, true);
		
		if (targets == null || targets.length == 0)
			return null;

		return targets[0];
	}
	
	public final Func[] getStatFuncs(L2Effect effect, L2Character player)
	{
		if (!(player instanceof L2PcInstance) && !(player instanceof L2Attackable) && !(player instanceof L2Summon))
			return _emptyFunctionSet;
		if (_funcTemplates == null)
			return _emptyFunctionSet;
		List<Func> funcs = new ArrayList<>();
		for (FuncTemplate t : _funcTemplates)
		{
			Env env = new Env();
			env.player = player;
			env.skill = this;
			Func f = t.getFunc(env, this); // skill is owner
			if (f != null)
				funcs.add(f);
		}
		if (funcs.size() == 0)
			return _emptyFunctionSet;
		return funcs.toArray(new Func[funcs.size()]);
	}
	
	public boolean hasEffects()
	{
		return (_effectTemplates != null && _effectTemplates.length > 0);
	}
	
	public boolean hasSelfEffects()
	{
		return (_effectTemplatesSelf != null && _effectTemplatesSelf.length > 0);
	}
	
	public final L2Effect[] getEffects(L2Character effector, L2Character effected)
	{
		if (!hasEffects() || isPassive())
			return _emptyEffectSet;

		// doors and siege flags cannot receive any effects
		if (effected instanceof L2DoorInstance || effected instanceof L2SiegeFlagInstance)
			return _emptyEffectSet;
		
		if ((effector != effected) && effected.isInvul())
			return _emptyEffectSet;
		
		List<L2Effect> effects = new ArrayList<>();
		
		boolean skillMastery = false;
		
		if (!isToggle() && Formulas.calcSkillMastery(effector))
			skillMastery = true;
		
		for (EffectTemplate et : _effectTemplates)
		{
			Env env = new Env();
			env.player = effector;
			env.target = effected;
			env.skill = this;
			env.skillMastery = skillMastery;
			L2Effect e = et.getEffect(env);
			if (e != null)
			{
				e.scheduleEffect();
				effects.add(e);
			}
		}
		
		if (effects.isEmpty())
			return _emptyEffectSet;
		
		return effects.toArray(new L2Effect[effects.size()]);
	}
	
	public final L2Effect[] getEffectsSelf(L2Character effector)
	{
		if (isPassive())
			return _emptyEffectSet;
		
		if (_effectTemplatesSelf == null)
			return _emptyEffectSet;
		
		List<L2Effect> effects = new ArrayList<>();
		
		for (EffectTemplate et : _effectTemplatesSelf)
		{
			Env env = new Env();
			env.player = effector;
			env.target = effector;
			env.skill = this;
			L2Effect e = et.getEffect(env);
			if (e != null)
			{
				// Implements effect charge
				if (e.getEffectType() == L2Effect.EffectType.CHARGE)
				{
					env.skill = SkillTable.getInstance().getInfo(8, effector.getSkillLevel(8));
					EffectCharge effect = (EffectCharge) env.target.getFirstEffect(L2Effect.EffectType.CHARGE);
					if (effect != null)
					{
						int effectcharge = effect.getLevel();
						if (effectcharge < _numCharges)
						{
							effectcharge++;
							effect.addNumCharges(effectcharge);
							if (env.target instanceof L2PcInstance)
							{
								env.target.sendPacket(new EtcStatusUpdate((L2PcInstance) env.target));
								SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FORCE_INCREASED_TO_S1);
								sm.addNumber(effectcharge);
								env.target.sendPacket(sm);
							}
						}
					}
					else
					{
						e.scheduleEffect();
						effects.add(e);
					}
				}
				else
				{
					e.scheduleEffect();
					effects.add(e);
				}
			}
		}
		if (effects.isEmpty())
			return _emptyEffectSet;
		
		return effects.toArray(new L2Effect[effects.size()]);
	}
	
	private ExtractableSkill parseExtractableSkill(int skillId, int skillLvl, String values)
	{
		final String[] prodLists = values.split(";");
		final List<ExtractableProductItem> products = new ArrayList<>();
		
		for (String prodList : prodLists)
		{
			final String[] prodData = prodList.split(",");
			
			if (prodData.length < 3)
				_log.warning("Extractable skills data: Error in Skill Id: " + skillId + " Level: " + skillLvl + " -> wrong seperator!");
			
			final int lenght = prodData.length - 1;
			
			List<IntIntHolder> items = null;
			double chance = 0;
			int prodId = 0;
			int quantity = 0;
			
			try
			{
				items = new ArrayList<>(lenght / 2);
				for (int j = 0; j < lenght; j++)
				{
					prodId = Integer.parseInt(prodData[j]);
					quantity = Integer.parseInt(prodData[j += 1]);
					
					if (prodId <= 0 || quantity <= 0)
						_log.warning("Extractable skills data: Error in Skill Id: " + skillId + " Level: " + skillLvl + " wrong production Id: " + prodId + " or wrond quantity: " + quantity + "!");
					
					items.add(new IntIntHolder(prodId, quantity));
				}
				chance = Double.parseDouble(prodData[lenght]);
			}
			catch (Exception e)
			{
				_log.warning("Extractable skills data: Error in Skill Id: " + skillId + " Level: " + skillLvl + " -> incomplete/invalid production data or wrong seperator!");
			}
			products.add(new ExtractableProductItem(items, chance));
		}
		
		if (products.isEmpty())
			_log.warning("Extractable skills data: Error in Skill Id: " + skillId + " Level: " + skillLvl + " -> There are no production items!");
		
		return new ExtractableSkill(SkillTable.getSkillHashCode(this), products);
	}
	
	public ExtractableSkill getExtractableSkill()
	{
		return _extractableItems;
	}
	
	public final void attach(FuncTemplate f)
	{
		if (_funcTemplates == null)
		{
			_funcTemplates = new FuncTemplate[]
			{
				f
			};
		}
		else
		{
			int len = _funcTemplates.length;
			FuncTemplate[] tmp = new FuncTemplate[len + 1];
			System.arraycopy(_funcTemplates, 0, tmp, 0, len);
			tmp[len] = f;
			_funcTemplates = tmp;
		}
	}
	
	public final void attach(EffectTemplate effect)
	{
		if (_effectTemplates == null)
		{
			_effectTemplates = new EffectTemplate[]
			{
				effect
			};
		}
		else
		{
			int len = _effectTemplates.length;
			EffectTemplate[] tmp = new EffectTemplate[len + 1];
			System.arraycopy(_effectTemplates, 0, tmp, 0, len);
			tmp[len] = effect;
			_effectTemplates = tmp;
		}
		
	}
	
	public final void attachSelf(EffectTemplate effect)
	{
		if (_effectTemplatesSelf == null)
		{
			_effectTemplatesSelf = new EffectTemplate[]
			{
				effect
			};
		}
		else
		{
			int len = _effectTemplatesSelf.length;
			EffectTemplate[] tmp = new EffectTemplate[len + 1];
			System.arraycopy(_effectTemplatesSelf, 0, tmp, 0, len);
			tmp[len] = effect;
			_effectTemplatesSelf = tmp;
		}
	}
	
	public final void attach(Condition c, boolean itemOrWeapon)
	{
		if (itemOrWeapon)
			_itemPreCondition = c;
		else
			_preCondition = c;
	}
	
	public EffectTemplate[] getEffectTemplates()
	{
		return _effectTemplates;
	}
	
	@Override
	public String toString()
	{
		return "" + _name + "[id=" + _id + ",lvl=" + _level + "]";
	}
	
	public final int getTargetConsumeId()
	{
		return _targetConsumeId;
	}
	
	public final int getTargetConsume()
	{
		return _targetConsume;
	}
	
	public boolean isClanSkill()
	{
		return SkillTreeData.getInstance().isClanSkill(_id, _level);
	}

	public final boolean is7Signs()
	{
		if (_id > 4360 && _id < 4367)
			return true;
		return false;
	}

	public boolean canBeRemoved(L2SkillType skillType, int negateLvl)
	{
		return ((getSkillType() == skillType || (getEffectType() != null && getEffectType() == skillType)) && (negateLvl == -1 || (getEffectType() != null && getEffectLvl() >= 0 && getEffectLvl() <= negateLvl) || (getLevel() >= 0 && getLevel() <= negateLvl)));
	}
}