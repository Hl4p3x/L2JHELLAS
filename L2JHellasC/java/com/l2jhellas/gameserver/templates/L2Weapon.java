package com.l2jhellas.gameserver.templates;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.l2jhellas.gameserver.enums.items.L2WeaponType;
import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.handler.ISkillHandler;
import com.l2jhellas.gameserver.handler.SkillHandler;
import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2Item;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestEventType;
import com.l2jhellas.gameserver.skills.Env;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.gameserver.skills.conditions.ConditionGameChance;
import com.l2jhellas.gameserver.skills.funcs.Func;
import com.l2jhellas.gameserver.skills.funcs.FuncTemplate;

public final class L2Weapon extends L2Item
{
	private final int _soulShotCount;
	private final int _spiritShotCount;
	private final int _pDam;
	private final int _rndDam;
	private final int _critical;
	private final double _hitModifier;
	private final int _avoidModifier;
	private final int _shieldDef;
	private final double _shieldDefRate;
	private final int _atkSpeed;
	private final int _atkReuse;
	private final int _mpConsume;
	private final int _mDam;
	private L2Skill _itemSkill = null; // for passive skill
	private L2Skill _enchant4Skill = null; // skill that activates when item is enchanted +4 (for duals)
	
	// Attached skills for Special Abilities
	protected L2Skill[] _skillsOnCast;
	protected L2Skill[] _skillsOnCrit;
	
	public L2Weapon(L2WeaponType type, StatsSet set)
	{
		super(type, set);
		_soulShotCount = set.getInteger("soulshots");
		_spiritShotCount = set.getInteger("spiritshots");
		_pDam = set.getInteger("p_dam");
		_rndDam = set.getInteger("rnd_dam");
		_critical = set.getInteger("critical");
		_hitModifier = set.getDouble("hit_modify");
		_avoidModifier = set.getInteger("avoid_modify");
		_shieldDef = set.getInteger("shield_def");
		_shieldDefRate = set.getDouble("shield_def_rate");
		_atkSpeed = set.getInteger("atk_speed");
		_atkReuse = set.getInteger("atk_reuse", type == L2WeaponType.BOW ? 1500 : 0);
		_mpConsume = set.getInteger("mp_consume");
		_mDam = set.getInteger("m_dam");
		
		int sId = set.getInteger("item_skill_id");
		int sLv = set.getInteger("item_skill_lvl");
		if (sId > 0 && sLv > 0)
			_itemSkill = SkillTable.getInstance().getInfo(sId, sLv);
		
		sId = set.getInteger("enchant4_skill_id");
		sLv = set.getInteger("enchant4_skill_lvl");
		if (sId > 0 && sLv > 0)
			_enchant4Skill = SkillTable.getInstance().getInfo(sId, sLv);
		
		sId = set.getInteger("onCast_skill_id");
		sLv = set.getInteger("onCast_skill_lvl");
		int sCh = set.getInteger("onCast_skill_chance");
		if (sId > 0 && sLv > 0 && sCh > 0)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(sId, sLv);
			skill.attach(new ConditionGameChance(sCh), true);
			attachOnCast(skill);
		}
		
		sId = set.getInteger("onCrit_skill_id");
		sLv = set.getInteger("onCrit_skill_lvl");
		sCh = set.getInteger("onCrit_skill_chance");
		if (sId > 0 && sLv > 0 && sCh > 0)
		{
			L2Skill skill = SkillTable.getInstance().getInfo(sId, sLv);
			skill.attach(new ConditionGameChance(sCh), true);
			attachOnCrit(skill);
		}
	}
	
	@Override
	public L2WeaponType getItemType()
	{
		return (L2WeaponType) super._type;
	}
	
	@Override
	public int getItemMask()
	{
		return getItemType().mask();
	}
	
	public int getSoulShotCount()
	{
		return _soulShotCount;
	}
	
	public int getSpiritShotCount()
	{
		return _spiritShotCount;
	}
	
	public int getPDamage()
	{
		return _pDam;
	}
	
	public int getRandomDamage()
	{
		return _rndDam;
	}
	
	public int getAttackSpeed()
	{
		return _atkSpeed;
	}
	
	public int getAttackReuseDelay()
	{
		return _atkReuse;
	}
	
	public int getAvoidModifier()
	{
		return _avoidModifier;
	}
	
	public int getCritical()
	{
		return _critical;
	}
	
	public double getHitModifier()
	{
		return _hitModifier;
	}
	
	public int getMDamage()
	{
		return _mDam;
	}
	
	public int getMpConsume()
	{
		return _mpConsume;
	}
	
	public int getShieldDef()
	{
		return _shieldDef;
	}
	
	public double getShieldDefRate()
	{
		return _shieldDefRate;
	}
	
	public L2Skill getSkill()
	{
		return _itemSkill;
	}
	
	public L2Skill getEnchant4Skill()
	{
		return _enchant4Skill;
	}
	
	@Override
	public Func[] getStatFuncs(L2ItemInstance instance, L2Character player)
	{
		List<Func> funcs = new ArrayList<>();
		if (_funcTemplates != null)
		{
			for (FuncTemplate t : _funcTemplates)
			{
				Env env = new Env();
				env.player = player;
				env.item = instance;
				Func f = t.getFunc(env, instance);
				if (f != null)
					funcs.add(f);
			}
		}
		return funcs.toArray(new Func[funcs.size()]);
	}
	
	public L2Effect[] getSkillEffects(L2Character caster, L2Character target, boolean crit)
	{
		if (_skillsOnCrit == null || !crit)
			return _emptyEffectSet;
		List<L2Effect> effects = new ArrayList<>();
		
		for (L2Skill skill : _skillsOnCrit)
		{
			if (target.isRaid() && ((skill.getSkillType() == L2SkillType.CONFUSION) || (skill.getSkillType() == L2SkillType.MUTE) || (skill.getSkillType() == L2SkillType.PARALYZE) || (skill.getSkillType() == L2SkillType.ROOT)))
				continue; // These skills should not work on RaidBoss
				
			if (!skill.checkCondition(caster, target, true))
				continue; // Skill condition not met
				
			if (target.getFirstEffect(skill.getId()) != null)
				target.getFirstEffect(skill.getId()).exit();
			for (L2Effect e : skill.getEffects(caster, target))
				effects.add(e);
		}
		if (effects.size() == 0)
			return _emptyEffectSet;
		return effects.toArray(new L2Effect[effects.size()]);
	}
	
	public L2Effect[] getSkillEffects(L2Character caster, L2Character target, L2Skill trigger)
	{
		if (_skillsOnCast == null)
			return _emptyEffectSet;
		List<L2Effect> effects = new ArrayList<>();
		
		for (L2Skill skill : _skillsOnCast)
		{
			if (trigger.isOffensive() != skill.isOffensive())
				continue; // Trigger only same type of skill
				
			if (trigger.getId() >= 1320 && trigger.getId() <= 1322)
				continue; // No buff with Common and Dwarven Craft...
				
			if (target.isRaid() || target.isBoss() && (skill.getSkillType() == L2SkillType.CONFUSION || skill.getSkillType() == L2SkillType.MUTE || skill.getSkillType() == L2SkillType.PARALYZE || skill.getSkillType() == L2SkillType.ROOT))
				continue; // These skills should not work on RaidBoss
				
			if (trigger.isToggle() && skill.getSkillType() == L2SkillType.BUFF)
				continue; // No buffing with toggle skills
				
			if (!skill.checkCondition(caster, target, true))
				continue; // Skill condition not met
				
			try
			{
				// Get the skill handler corresponding to the skill type
				ISkillHandler handler = SkillHandler.getInstance().getHandler(skill.getSkillType());
				
				L2Character[] targets = new L2Character[1];
				targets[0] = target;
				
				// Launch the magic skill and calculate its effects
				if (handler != null)
					handler.useSkill(caster, skill, targets);
				else
					skill.useSkill(caster, targets);
				
				// notify quests of a skill use
				if (caster instanceof L2PcInstance)
				{
					// Mobs in range 1000 see spell
					
					final Collection<L2Object> objs = L2World.getInstance().getVisibleObjects(caster, L2Object.class, 1000);
					// synchronized (caster.getKnownList().getKnownObjects())
					{
						for (L2Object spMob : objs)
						{
							if (spMob instanceof L2Npc)
							{
								L2Npc npcMob = (L2Npc) spMob;
								
								if (npcMob.getTemplate().getEventQuests(QuestEventType.ON_SKILL_SEE) != null)
									for (Quest quest : npcMob.getTemplate().getEventQuests(QuestEventType.ON_SKILL_SEE))
										quest.notifySkillSee(npcMob, (L2PcInstance) caster, _skillsOnCast[0], targets, false);// XXX not sure of this
							}
						}
					}
				}
			}
			catch (IOException e)
			{
			}
		}
		if (effects.size() == 0)
			return _emptyEffectSet;
		return effects.toArray(new L2Effect[effects.size()]);
	}
	
	public void attachOnCrit(L2Skill skill)
	{
		if (_skillsOnCrit == null)
		{
			_skillsOnCrit = new L2Skill[]
			{
				skill
			};
		}
		else
		{
			int len = _skillsOnCrit.length;
			L2Skill[] tmp = new L2Skill[len + 1];
			// Definition : arraycopy(array source, begins copy at this position of source, array destination, begins copy at this position in dest,
			// number of components to be copied)
			System.arraycopy(_skillsOnCrit, 0, tmp, 0, len);
			tmp[len] = skill;
			_skillsOnCrit = tmp;
		}
	}
	
	public void attachOnCast(L2Skill skill)
	{
		if (_skillsOnCast == null)
		{
			_skillsOnCast = new L2Skill[]
			{
				skill
			};
		}
		else
		{
			int len = _skillsOnCast.length;
			L2Skill[] tmp = new L2Skill[len + 1];
			// Definition : arraycopy(array source, begins copy at this position of source, array destination, begins copy at this position in dest,
			// number of components to be copied)
			System.arraycopy(_skillsOnCast, 0, tmp, 0, len);
			tmp[len] = skill;
			_skillsOnCast = tmp;
		}
	}
}