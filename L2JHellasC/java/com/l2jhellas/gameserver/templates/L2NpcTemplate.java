package com.l2jhellas.gameserver.templates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.l2jhellas.gameserver.enums.player.ClassId;
import com.l2jhellas.gameserver.model.L2DropCategory;
import com.l2jhellas.gameserver.model.L2DropData;
import com.l2jhellas.gameserver.model.L2MinionData;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestEventType;
import com.l2jhellas.gameserver.skills.Stats;

public final class L2NpcTemplate extends L2CharTemplate
{
	protected static final Logger _log = Logger.getLogger(L2NpcTemplate.class.getName());
	
	public final int npcId;
	public final int idTemplate;
	public final String type;
	public final String name;
	public final boolean serverSideName;
	public final String title;
	public final boolean serverSideTitle;
	public final String sex;
	public final byte level;
	public final int rewardExp;
	public final int rewardSp;
	public final int aggroRange;
	public final String factionId;
	public final int factionRange;
	private int _dodge;
	public final int rhand;
	public final int lhand;
	public final int armor;
	public final int absorbLevel;
	public final AbsorbCrystalType absorbType;
	public Race race;
	public final boolean dropherb;
	public boolean isQuestMonster; // doesn't include all mobs that are involved in
	// quests, just plain quest monsters for preventing champion spawn
	
	// Skill AI
	public List<L2Skill> _buffskills;
	public List<L2Skill> _negativeskills;
	public List<L2Skill> _debuffskills;
	public List<L2Skill> _atkskills;
	public List<L2Skill> _rootskills;
	public List<L2Skill> _stunskills;
	public List<L2Skill> _sleepskills;
	public List<L2Skill> _paralyzeskills;
	public List<L2Skill> _fossilskills;
	public List<L2Skill> _floatskills;
	public List<L2Skill> _immobiliseskills;
	public List<L2Skill> _healskills;
	public List<L2Skill> _resskills;
	public List<L2Skill> _dotskills;
	public List<L2Skill> _cotskills;
	public List<L2Skill> _universalskills;
	public List<L2Skill> _manaskills;
	public List<L2Skill> _Lrangeskills;
	public List<L2Skill> _Srangeskills;
	public List<L2Skill> _generalskills;
	
	private boolean _hasbuffskills;
	private boolean _hasnegativeskills;
	private boolean _hasdebuffskills;
	private boolean _hasatkskills;
	private boolean _hasrootskills;
	private boolean _hasstunskills;
	private boolean _hassleepskills;
	private boolean _hasparalyzeskills;
	private boolean _hasfossilskills;
	private boolean _hasfloatskills;
	private boolean _hasimmobiliseskills;
	private boolean _hashealskills;
	private boolean _hasresskills;
	private boolean _hasdotskills;
	private boolean _hascotskills;
	private boolean _hasuniversalskills;
	private boolean _hasmanaskills;
	private boolean _hasLrangeskills;
	private boolean _hasSrangeskills;
	private boolean _hasgeneralskills;
	private final StatsSet _npcStatsSet;
	
	public static enum AbsorbCrystalType
	{
		LAST_HIT,
		FULL_PARTY,
		PARTY_ONE_RANDOM
	}
	
	public static enum AIType
	{
		FIGHTER,
		ARCHER,
		BALANCED,
		MAGE,
		HEALER,
		CORPSE
	}
	
	public static enum Race
	{
		UNDEAD,
		MAGICCREATURE,
		BEAST,
		ANIMAL,
		PLANT,
		HUMANOID,
		SPIRIT,
		ANGEL,
		DEMON,
		DRAGON,
		GIANT,
		BUG,
		FAIRIE,
		HUMAN,
		ELVE,
		DARKELVE,
		ORC,
		DWARVE,
		OTHER,
		NONLIVING,
		SIEGEWEAPON,
		DEFENDINGARMY,
		MERCENARIE,
		NONE
	}
	
	// private final StatsSet _npcStatsSet;
	
	private ArrayList<L2DropCategory> _categories = null;
	
	private List<L2MinionData> _minions = null;
	
	private List<ClassId> _teachInfo;
	private Map<Integer, L2Skill> _skills;
	
	private Map<Stats, Double> _vulnerabilities;
	// contains a list of quests for each event type (questStart, questAttack, questKill, etc)
	private final Map<QuestEventType, List<Quest>> _questEvents = new HashMap<>();
	
	public L2NpcTemplate(StatsSet set)
	{
		super(set);
		npcId = set.getInteger("npcId");
		idTemplate = set.getInteger("idTemplate");
		type = set.getString("type");
		name = set.getString("name");
		serverSideName = set.getBool("serverSideName");
		title = set.getString("title");
		if (title.equalsIgnoreCase("Quest Monster"))
			isQuestMonster = true;
		else
			isQuestMonster = false;
		serverSideTitle = set.getBool("serverSideTitle");
		sex = set.getString("sex");
		level = set.getByte("level");
		rewardExp = set.getInteger("rewardExp");
		rewardSp = set.getInteger("rewardSp");
		aggroRange = set.getInteger("aggroRange");
		String f = set.getString("factionId", null);
		if (f == null)
			factionId = null;
		else
			factionId = f.intern();
		factionRange = set.getInteger("factionRange");
		_dodge = set.getInteger("dodge", 0);
		rhand = set.getInteger("rhand");
		lhand = set.getInteger("lhand");
		armor = set.getInteger("armor");
		absorbLevel = set.getInteger("absorb_level", 0);
		absorbType = AbsorbCrystalType.valueOf(set.getString("absorb_type"));
		race = null;
		dropherb = set.getBool("drop_herbs", false);
		// _npcStatsSet = set;
		_teachInfo = null;
		_npcStatsSet = set;
	}
	
	public void addTeachInfo(ClassId classId)
	{
		if (_teachInfo == null)
			_teachInfo = new ArrayList<>();
		_teachInfo.add(classId);
	}
	
	public ClassId[] getTeachInfo()
	{
		if (_teachInfo == null)
			return null;
		return _teachInfo.toArray(new ClassId[_teachInfo.size()]);
	}
	
	public boolean canTeach(ClassId classId)
	{
		if (_teachInfo == null)
			return false;
		
		// If the player is on a third class, fetch the class teacher
		// information for its parent class.
		if (classId.level() == 3)
			return _teachInfo.contains(classId.getParent());
		
		return _teachInfo.contains(classId);
	}
	
	// add a drop to a given category. If the category does not exist, create it.
	public void addDropData(L2DropData drop, int categoryType)
	{
		if (drop.isQuestDrop())
		{
			// if (_questDrops == null)
			// _questDrops = new ArrayList<L2DropData>(0);
			// _questDrops.add(drop);
		}
		else
		{
			if (_categories == null)
				_categories = new ArrayList<>();
			// if the category doesn't already exist, create it first
			synchronized (_categories)
			{
				boolean catExists = false;
				for (L2DropCategory cat : _categories)
					// if the category exists, add the drop to this category.
					if (cat.getCategoryType() == categoryType)
					{
						cat.addDropData(drop, type.equalsIgnoreCase("L2RaidBoss") || type.equalsIgnoreCase("L2GrandBoss"));
						catExists = true;
						break;
					}
				// if the category doesn't exit, create it and add the drop
				if (!catExists)
				{
					L2DropCategory cat = new L2DropCategory(categoryType);
					cat.addDropData(drop, type.equalsIgnoreCase("L2RaidBoss") || type.equalsIgnoreCase("L2GrandBoss"));
					_categories.add(cat);
				}
			}
		}
	}
	
	public void addRaidData(L2MinionData minion)
	{
		if (_minions == null)
			_minions = new ArrayList<>();
		_minions.add(minion);
	}
	
	public void addVulnerability(Stats id, double vuln)
	{
		if (_vulnerabilities == null)
			_vulnerabilities = new HashMap<>();
		_vulnerabilities.put(id, new Double(vuln));
	}
	
	public double getVulnerability(Stats id)
	{
		if (_vulnerabilities == null || _vulnerabilities.get(id) == null)
			return 1;
		return _vulnerabilities.get(id);
	}
	
	public void addSkill(L2Skill skill)
	{
		
		if (_skills == null)
			_skills = new HashMap<>();
		
		if (!skill.isPassive())
		{
			addGeneralSkill(skill);
			switch (skill.getSkillType())
			{
				case BUFF:
				case REFLECT:
					addBuffSkill(skill);
					break;
				case HEAL:
				case HOT:
				case HEAL_PERCENT:
				case HEAL_STATIC:
				case BALANCE_LIFE:
					addHealSkill(skill);
					break;
				case RESURRECT:
					addResSkill(skill);
					break;
				case DEBUFF:
				case WEAKNESS:
					addDebuffSkill(skill);
					addCOTSkill(skill);
					addRangeSkill(skill);
					break;
				case ROOT:
					addRootSkill(skill);
					addImmobiliseSkill(skill);
					addRangeSkill(skill);
					break;
				case SLEEP:
					addSleepSkill(skill);
					addImmobiliseSkill(skill);
					break;
				case STUN:
					addRootSkill(skill);
					addImmobiliseSkill(skill);
					addRangeSkill(skill);
					break;
				case PARALYZE:
					addParalyzeSkill(skill);
					addImmobiliseSkill(skill);
					addRangeSkill(skill);
					break;
				case PDAM:
				case MDAM:
				case BLOW:
				case DRAIN:
				case CHARGEDAM:
				case DEATHLINK:
				case CPDAM:
				case MANADAM:
					addAtkSkill(skill);
					addUniversalSkill(skill);
					addRangeSkill(skill);
					break;
				case POISON:
				case DOT:
				case MDOT:
				case BLEED:
					addDOTSkill(skill);
					addRangeSkill(skill);
					break;
				case MUTE:
				case FEAR:
					addCOTSkill(skill);
					addRangeSkill(skill);
					break;
				case CANCEL:
				case NEGATE:
					addNegativeSkill(skill);
					addRangeSkill(skill);
					break;
				default:
					addUniversalSkill(skill);
					break;
			
			}
		}
		
		_skills.put(skill.getId(), skill);
	}
	
	public double removeVulnerability(Stats id)
	{
		return _vulnerabilities.remove(id);
	}
	
	public List<L2DropCategory> getDropData()
	{
		return _categories;
	}
	
	public List<L2DropData> getAllDropData()
	{
		if (_categories == null)
			return null;
		List<L2DropData> lst = new ArrayList<>();
		for (L2DropCategory tmp : _categories)
		{
			lst.addAll(tmp.getAllDrops());
		}
		return lst;
	}
	
	public synchronized void clearAllDropData()
	{
		if (_categories == null)
			return;
		
		while (!_categories.isEmpty())
		{
			_categories.clear();
		}
		
	}
	
	public List<L2MinionData> getMinionData()
	{
		return _minions;
	}
	
	public Map<Integer, L2Skill> getSkills()
	{
		return _skills;
	}
	
	public void addQuestEvent(QuestEventType QuestEventType, Quest quest)
	{
		List<Quest> eventList = _questEvents.get(QuestEventType);
		if (eventList == null)
		{
			eventList = new ArrayList<>();
			eventList.add(quest);
			_questEvents.put(QuestEventType, eventList);
		}
		else
		{
			eventList.remove(quest);
			
			if (QuestEventType.isMultipleRegistrationAllowed() || eventList.isEmpty())
				eventList.add(quest);
			else
				_log.warning(L2NpcTemplate.class.getName() + ": Quest event not allow multiple quest registrations. Skipped addition of QuestEventType \"" + QuestEventType + "\" for NPC \"" + getName() + "\" and quest \"" + quest.getName() + "\".");
		}
	}
	
	public Map<QuestEventType, List<Quest>> getEventQuests()
	{
		return _questEvents;
	}
	
	public List<Quest> getEventQuests(QuestEventType QuestEventType)
	{
		return _questEvents.get(QuestEventType);
	}
	
	public static boolean isAssignableTo(Object obj, Class<?> clazz)
	{
		return L2NpcTemplate.isAssignableTo(obj.getClass(), clazz);
	}
	
	public static boolean isAssignableTo(Class<?> sub, Class<?> clazz)
	{
		// if clazz represents an interface
		if (clazz.isInterface())
		{
			// check if obj implements the clazz interface
			Class<?>[] interfaces = sub.getInterfaces();
			for (Class<?> interface1 : interfaces)
			{
				if (clazz.getName().equals(interface1.getName()))
				{
					return true;
				}
			}
		}
		else
		{
			do
			{
				if (sub.getName().equals(clazz.getName()))
				{
					return true;
				}
				
				sub = sub.getSuperclass();
			}
			while (sub != null);
		}
		
		return false;
	}
	
	public void setRace(int raceId)
	{
		switch (raceId)
		{
			case 1:
				race = L2NpcTemplate.Race.UNDEAD;
				break;
			case 2:
				race = L2NpcTemplate.Race.MAGICCREATURE;
				break;
			case 3:
				race = L2NpcTemplate.Race.BEAST;
				break;
			case 4:
				race = L2NpcTemplate.Race.ANIMAL;
				break;
			case 5:
				race = L2NpcTemplate.Race.PLANT;
				break;
			case 6:
				race = L2NpcTemplate.Race.HUMANOID;
				break;
			case 7:
				race = L2NpcTemplate.Race.SPIRIT;
				break;
			case 8:
				race = L2NpcTemplate.Race.ANGEL;
				break;
			case 9:
				race = L2NpcTemplate.Race.DEMON;
				break;
			case 10:
				race = L2NpcTemplate.Race.DRAGON;
				break;
			case 11:
				race = L2NpcTemplate.Race.GIANT;
				break;
			case 12:
				race = L2NpcTemplate.Race.BUG;
				break;
			case 13:
				race = L2NpcTemplate.Race.FAIRIE;
				break;
			case 14:
				race = L2NpcTemplate.Race.HUMAN;
				break;
			case 15:
				race = L2NpcTemplate.Race.ELVE;
				break;
			case 16:
				race = L2NpcTemplate.Race.DARKELVE;
				break;
			case 17:
				race = L2NpcTemplate.Race.ORC;
				break;
			case 18:
				race = L2NpcTemplate.Race.DWARVE;
				break;
			case 19:
				race = L2NpcTemplate.Race.OTHER;
				break;
			case 20:
				race = L2NpcTemplate.Race.NONLIVING;
				break;
			case 21:
				race = L2NpcTemplate.Race.SIEGEWEAPON;
				break;
			case 22:
				race = L2NpcTemplate.Race.DEFENDINGARMY;
				break;
			case 23:
				race = L2NpcTemplate.Race.MERCENARIE;
				break;
			default:
				race = L2NpcTemplate.Race.NONE;
				break;
		}
	}
	
	public void addBuffSkill(L2Skill skill)
	{
		if (_buffskills == null)
			_buffskills = new ArrayList<>();
		_buffskills.add(skill);
		_hasbuffskills = true;
	}
	
	public void addHealSkill(L2Skill skill)
	{
		if (_healskills == null)
			_healskills = new ArrayList<>();
		_healskills.add(skill);
		_hashealskills = true;
	}
	
	public void addResSkill(L2Skill skill)
	{
		if (_resskills == null)
			_resskills = new ArrayList<>();
		_resskills.add(skill);
		_hasresskills = true;
	}
	
	public void addAtkSkill(L2Skill skill)
	{
		if (_atkskills == null)
			_atkskills = new ArrayList<>();
		_atkskills.add(skill);
		_hasatkskills = true;
	}
	
	public void addDebuffSkill(L2Skill skill)
	{
		if (_debuffskills == null)
			_debuffskills = new ArrayList<>();
		_debuffskills.add(skill);
		_hasdebuffskills = true;
	}
	
	public void addRootSkill(L2Skill skill)
	{
		if (_rootskills == null)
			_rootskills = new ArrayList<>();
		_rootskills.add(skill);
		_hasrootskills = true;
	}
	
	public void addSleepSkill(L2Skill skill)
	{
		if (_sleepskills == null)
			_sleepskills = new ArrayList<>();
		_sleepskills.add(skill);
		_hassleepskills = true;
	}
	
	public void addStunSkill(L2Skill skill)
	{
		if (_stunskills == null)
			_stunskills = new ArrayList<>();
		_stunskills.add(skill);
		_hasstunskills = true;
	}
	
	public void addParalyzeSkill(L2Skill skill)
	{
		if (_paralyzeskills == null)
			_paralyzeskills = new ArrayList<>();
		_paralyzeskills.add(skill);
		_hasparalyzeskills = true;
	}
	
	public void addFloatSkill(L2Skill skill)
	{
		if (_floatskills == null)
			_floatskills = new ArrayList<>();
		_floatskills.add(skill);
		_hasfloatskills = true;
	}
	
	public void addFossilSkill(L2Skill skill)
	{
		if (_fossilskills == null)
			_fossilskills = new ArrayList<>();
		_fossilskills.add(skill);
		_hasfossilskills = true;
	}
	
	public void addNegativeSkill(L2Skill skill)
	{
		if (_negativeskills == null)
			_negativeskills = new ArrayList<>();
		_negativeskills.add(skill);
		_hasnegativeskills = true;
	}
	
	public void addImmobiliseSkill(L2Skill skill)
	{
		if (_immobiliseskills == null)
			_immobiliseskills = new ArrayList<>();
		_immobiliseskills.add(skill);
		_hasimmobiliseskills = true;
	}
	
	public void addDOTSkill(L2Skill skill)
	{
		if (_dotskills == null)
			_dotskills = new ArrayList<>();
		_dotskills.add(skill);
		_hasdotskills = true;
	}
	
	public void addUniversalSkill(L2Skill skill)
	{
		if (_universalskills == null)
			_universalskills = new ArrayList<>();
		_universalskills.add(skill);
		_hasuniversalskills = true;
	}
	
	public void addCOTSkill(L2Skill skill)
	{
		if (_cotskills == null)
			_cotskills = new ArrayList<>();
		_cotskills.add(skill);
		_hascotskills = true;
	}
	
	public void addManaHealSkill(L2Skill skill)
	{
		if (_manaskills == null)
			_manaskills = new ArrayList<>();
		_manaskills.add(skill);
		_hasmanaskills = true;
	}
	
	public void addGeneralSkill(L2Skill skill)
	{
		if (_generalskills == null)
			_generalskills = new ArrayList<>();
		_generalskills.add(skill);
		_hasgeneralskills = true;
	}
	
	public void addRangeSkill(L2Skill skill)
	{
		if (skill.getCastRange() <= 150 && skill.getCastRange() > 0)
		{
			if (_Srangeskills == null)
				_Srangeskills = new ArrayList<>();
			_Srangeskills.add(skill);
			_hasSrangeskills = true;
		}
		else if (skill.getCastRange() > 150)
		{
			if (_Lrangeskills == null)
				_Lrangeskills = new ArrayList<>();
			_Lrangeskills.add(skill);
			_hasLrangeskills = true;
		}
	}
	
	// --------------------------------------------------------------------
	public boolean hasBuffSkill()
	{
		return _hasbuffskills;
	}
	
	public boolean hasHealSkill()
	{
		return _hashealskills;
	}
	
	public boolean hasResSkill()
	{
		return _hasresskills;
	}
	
	public boolean hasAtkSkill()
	{
		return _hasatkskills;
	}
	
	public boolean hasDebuffSkill()
	{
		return _hasdebuffskills;
	}
	
	public boolean hasRootSkill()
	{
		return _hasrootskills;
	}
	
	public boolean hasSleepSkill()
	{
		return _hassleepskills;
	}
	
	public boolean hasStunSkill()
	{
		return _hasstunskills;
	}
	
	public boolean hasParalyzeSkill()
	{
		return _hasparalyzeskills;
	}
	
	public boolean hasFloatSkill()
	{
		return _hasfloatskills;
	}
	
	public boolean hasFossilSkill()
	{
		return _hasfossilskills;
	}
	
	public boolean hasNegativeSkill()
	{
		return _hasnegativeskills;
	}
	
	public boolean hasImmobiliseSkill()
	{
		return _hasimmobiliseskills;
	}
	
	public boolean hasDOTSkill()
	{
		return _hasdotskills;
	}
	
	public boolean hasUniversalSkill()
	{
		return _hasuniversalskills;
	}
	
	public boolean hasCOTSkill()
	{
		return _hascotskills;
	}
	
	public boolean hasManaHealSkill()
	{
		return _hasmanaskills;
	}
	
	public boolean hasAutoLrangeSkill()
	{
		return _hasLrangeskills;
	}
	
	public boolean hasAutoSrangeSkill()
	{
		return _hasSrangeskills;
	}
	
	public boolean hasSkill()
	{
		return _hasgeneralskills;
	}
	
	public L2NpcTemplate.Race getRace()
	{
		if (race == null)
			race = L2NpcTemplate.Race.NONE;
		
		return race;
	}
	
	public boolean isCustom()
	{
		return npcId != idTemplate;
	}
	
	public String getName()
	{
		return name;
	}
	
	public boolean isServerSideName()
	{
		return serverSideName;
	}
	
	public StatsSet getStatsSet()
	{
		return _npcStatsSet;
	}
	
	public int getNpcId()
	{
		return npcId;
	}
	
	public int getDodge()
	{
		return _dodge;
	}
	
	public boolean isQuestMonster()
	{
		return isQuestMonster;
	}
	
}
