package com.l2jhellas.gameserver.model.actor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ItemsAutoDestroy;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.ai.CtrlEvent;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.ai.L2AttackableAI;
import com.l2jhellas.gameserver.ai.L2CharacterAI;
import com.l2jhellas.gameserver.ai.L2SiegeGuardAI;
import com.l2jhellas.gameserver.datatables.sql.ItemTable;
import com.l2jhellas.gameserver.enums.player.ChatType;
import com.l2jhellas.gameserver.instancemanager.BotsPreventionManager;
import com.l2jhellas.gameserver.instancemanager.CursedWeaponsManager;
import com.l2jhellas.gameserver.model.L2DropCategory;
import com.l2jhellas.gameserver.model.L2DropData;
import com.l2jhellas.gameserver.model.L2Manor;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.group.party.L2CommandChannel;
import com.l2jhellas.gameserver.model.actor.group.party.L2Party;
import com.l2jhellas.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2MinionInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PetInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2RaidBossInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.model.base.SoulCrystal;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestEventType;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.CreatureSay;
import com.l2jhellas.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.Stats;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;
import com.l2jhellas.util.Rnd;
import com.l2jhellas.util.Util;

public class L2Attackable extends L2Npc
{
	private boolean _isRaid = false;
	private boolean _isRaidMinion = false;

	public void addDamageHate(L2Character attacker, int damage, int aggro)
	{	
		if (attacker == null || isDead())
			return;
		
		final L2PcInstance targetPlayer = attacker.getActingPlayer();
		
		final AggroInfo ai = _aggroListPro.computeIfAbsent(attacker, AggroInfo::new);
		ai.addDamage(damage);

		// If aggro is negative, its comming from SEE_SPELL, buffs use constant 150
		if (targetPlayer != null && aggro == 0)
		{
			if (getTemplate().getEventQuests(QuestEventType.ON_AGGRO_RANGE_ENTER) != null)
				for (Quest quest : getTemplate().getEventQuests(QuestEventType.ON_AGGRO_RANGE_ENTER))
					quest.notifyAggroRangeEnter(this, targetPlayer, (attacker instanceof L2Summon));
		}
		else if (aggro < 0)
		{
			aggro = 1;
			ai.addHate(1);
		}
		else
			ai.addHate((aggro * 100) / (getLevel() + 7));
		
		// Set the intention to the L2Attackable to AI_INTENTION_ACTIVE
		if (aggro > 0 && getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		
		// Notify the L2Attackable AI with EVT_ATTACKED
		if (damage > 0)
		{
			getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, attacker);
			
				try
				{
					if (attacker instanceof L2PcInstance || attacker instanceof L2Summon)
					{
						L2PcInstance player = attacker instanceof L2PcInstance ? (L2PcInstance) attacker : ((L2Summon) attacker).getOwner();
						
						if (getTemplate().getEventQuests(QuestEventType.ON_ATTACK) != null)
							for (Quest quest : getTemplate().getEventQuests(QuestEventType.ON_ATTACK))
							{
								if (quest != null)
									quest.notifyAttack(this, player, damage, attacker instanceof L2Summon);
							}
					}
				}
				catch (Exception e)
				{
					_log.severe("Could not set attackable script aggro!!");
					for (StackTraceElement stack : e.getStackTrace())
						_log.severe(stack.toString());
				}
		}
	}
	
	public void reduceHate(L2Character target, int amount)
	{
		if (getAI() instanceof L2SiegeGuardAI)
		{
			stopHating(target);
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			setTarget(null);
			return;
		}
		
		if (target == null) // whole aggrolist
		{
			L2Character mostHated = getMostHated();
			
			if (mostHated == null) // makes target passive for a moment more
			{
				getAI().setGlobalAggro(-25);
				return;
			}
			for (L2Character aggroed : _aggroListPro.keySet())
			{
				AggroInfo ai = _aggroListPro.get(aggroed);
				
				if (ai == null)
					return;
				ai.addHate(-amount);
			}
			
			amount = getHating(mostHated);
			
			if (amount <= 0)
			{
				getAI().setGlobalAggro(-25);
				clearAggroList();
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				setWalking();
			}
			return;
		}
		AggroInfo ai = _aggroListPro.get(target);
		
		if (ai == null)
			return;
		ai.addHate(-amount);
		
		if (ai.getHate() <= 0)
		{
			if (getMostHated() == null)
			{
				getAI().setGlobalAggro(-25);
				clearAggroList();
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				setWalking();
			}
		}
	}
	
	public void stopHating(L2Character target)
	{
		if (target == null)
			return;
		AggroInfo ai = _aggroListPro.get(target);
		if (ai != null)
			ai.stopHate();
	}
	
	public L2Character getMostHated()
	{
		if (_aggroListPro.isEmpty() || isAlikeDead())
			return null;
		
		return getAggroList().values().stream().filter(Objects::nonNull).sorted(Comparator.comparingInt(AggroInfo::getHate).reversed()).map(AggroInfo::getAttacker).findFirst().orElse(null);
	}
	
	public List<L2Character> get2MostHated()
	{
		if (getAggroList().isEmpty() || isAlikeDead())
			return null;
		
		L2Character mostHated = null;
		L2Character secondMostHated = null;
		int maxHate = 0;
		List<L2Character> result = new ArrayList<>();
		
		// Go through the aggroList of the L2Attackable
		for (AggroInfo ai : getAggroList().values())
		{
			if (ai == null)
				continue;
			
			if (ai.checkHate(this) > maxHate)
			{
				secondMostHated = mostHated;
				mostHated = ai.getAttacker();
				maxHate = ai.getHate();
			}
		}
		
		result.add(mostHated);
		
		final L2Character secondMostHatedFinal = secondMostHated;
		if (getAttackByList().stream().anyMatch(o -> o == secondMostHatedFinal))
			result.add(secondMostHated);
		else
			result.add(null);

		return result;
	}
	
	public List<L2Character> getHateList()
	{
		if (getAggroList().isEmpty() || isAlikeDead())
			return null;
		
		final List<L2Character> result = new ArrayList<>();
		for (AggroInfo ai : getAggroList().values())
		{
			if (ai == null)
				continue;
			
			ai.checkHate(this);
			
			result.add(ai.getAttacker());
		}
		return result;
	}
	
	public int getHating(final L2Character target)
	{
		if (_aggroListPro.isEmpty() || target == null)
			return 0;
		
		AggroInfo ai = _aggroListPro.get(target);
		
		if (ai == null)
			return 0;
		
		if (ai.getAttacker() instanceof L2PcInstance && !(((L2PcInstance) ai.getAttacker()).getAppearance().isVisible() || ai.getAttacker().isInvul()))
		{
			// Remove Object Should Use This Method and Can be Blocked While Interating
			_aggroListPro.remove(target);
			return 0;
		}
		
		if (!ai.getAttacker().isVisible())
		{
			_aggroListPro.remove(target);
			return 0;
		}
		
		if (ai.getAttacker().isAlikeDead())
		{
			ai.stopHate();
			return 0;
		}
		return ai.getHate();
	}
	
	public final class AggroInfo
	{
		private final L2Character _attacker;
		private int _hate = 0;
		private int _damage = 0;
		
		public AggroInfo(L2Character pAttacker)
		{
			_attacker = pAttacker;
		}
		
		public final L2Character getAttacker()
		{
			return _attacker;
		}
		
		public final int getHate()
		{
			return _hate;
		}
		
		public final int checkHate(L2Character owner)
		{
			if (_attacker.isAlikeDead() || !_attacker.isVisible() || !owner.isInSurroundingRegion(_attacker))
				_hate = 0;
			
			return _hate;
		}
		
		public final void addHate(int value)
		{
			_hate = Math.min(_hate + value, 999999999);
		}
		
		public final void stopHate()
		{
			_hate = 0;
		}
		
		public final int getDamage()
		{
			return _damage;
		}
		
		public final void addDamage(int value)
		{
			_damage = Math.min(_damage + value, 999999999);
		}
		
		@Override
		public final boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			
			if (obj instanceof AggroInfo)
				return (((AggroInfo) obj).getAttacker() == _attacker);
			
			return false;
		}
		
		@Override
		public final int hashCode()
		{
			return _attacker.getObjectId();
		}
	}
	
	protected static final class RewardInfo
	{
		protected L2Character _attacker;
		protected int _dmg = 0;
		
		public RewardInfo(L2Character pAttacker, int pDmg)
		{
			_attacker = pAttacker;
			_dmg = pDmg;
		}
		
		public void addDamage(int pDmg)
		{
			_dmg += pDmg;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			
			if (obj instanceof RewardInfo)
				return (((RewardInfo) obj)._attacker == _attacker);
			
			return false;
		}
		
		@Override
		public int hashCode()
		{
			return _attacker.getObjectId();
		}
	}
	
	public static final class AbsorberInfo
	{
		public int _objId;
		public double _absorbedHP;
		
		AbsorberInfo(int objId, double pAbsorbedHP)
		{
			_objId = objId;
			_absorbedHP = pAbsorbedHP;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			
			if (obj instanceof AbsorberInfo)
				return (((AbsorberInfo) obj)._objId == _objId);
			
			return false;
		}
		
		public static boolean isRegistered()
		{
			return isAbsorbed();
		}
		
		public boolean isValid(int itemObjectId)
		{
			return _objId == itemObjectId && _absorbedHP < 50;
		}
		
		@Override
		public int hashCode()
		{
			return _objId;
		}
	}
	
	public static final class RewardItem
	{
		protected int _itemId;
		protected int _count;
		
		public RewardItem(int itemId, int count)
		{
			_itemId = itemId;
			_count = count;
		}
		
		public int getItemId()
		{
			return _itemId;
		}
		
		public int getCount()
		{
			return _count;
		}
	}
	
	private final Map<L2Character, AggroInfo> _aggroListPro = new ConcurrentHashMap<>();
	
	public final Map<L2Character, AggroInfo> getAggroList()
	{
		return _aggroListPro;
	}
	
	private boolean _isReturningToSpawnPoint = false;
	private boolean _seeThroughSilentMove = false;
	
	public final boolean isReturningToSpawnPoint()
	{
		return _isReturningToSpawnPoint;
	}
	
	public final void setIsReturningToSpawnPoint(boolean value)
	{
		_isReturningToSpawnPoint = value;
	}
	
	public boolean canSeeThroughSilentMove()
	{
		return _seeThroughSilentMove;
	}
	
	public void seeThroughSilentMove(boolean val)
	{
		_seeThroughSilentMove = val;
	}
	
	private final List<RewardItem> _sweepItems = new ArrayList<>();
	
	private RewardItem[] _harvestItems;
	private int _seedType = 0;
	private int _seederObjId = 0;
	
	private boolean _overhit;
	private double _overhitDamage;
	private L2Character _overhitAttacker;
	
	private L2CommandChannel _firstCommandChannelAttacked = null;
	private CommandChannelTimer _commandChannelTimer = null;
	private long _commandChannelLastAttack = 0;
	
	private static boolean _absorbed;
	private final Map<Integer, AbsorberInfo> _absorbersList = new ConcurrentHashMap<>();
	
	public L2Attackable(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		_mustGiveExpSp = true;
		
	}
	
	@Override
	public L2CharacterAI getAI()
	{
		L2CharacterAI ai = _ai;
		if (ai == null)
		{
			synchronized (this)
			{
				ai = _ai;
				if (ai == null)
					_ai = ai = new L2AttackableAI(this);
			}
		}
		return ai;
	}

	@Override
	public void useMagic(L2Skill skill)
	{
		if (skill == null || isAlikeDead())
			return;
		
		if (skill.isPassive())
			return;
		
		if (isCastingNow())
			return;
		
		if (isSkillDisabled(skill.getId()))
			return;
		
		if (getCurrentMp() < getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill))
			return;
		
		if (getCurrentHp() <= skill.getHpConsume())
			return;
		
		if (skill.isMagic())
		{
			if (isMuted())
				return;
		}
		else
		{
			if (isPsychicalMuted())
				return;
		}
		
		L2Object target = skill.getFirstOfTargetList(this);
		if (target == null)
			return;
		
		getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
	}
	
	@Override
	public void reduceCurrentHp(double damage, L2Character attacker)
	{
		reduceCurrentHp(damage, attacker, true);
	}
	
	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake)
	{
		if (isRaid() && !isMinion() && attacker != null && attacker.getParty() != null && attacker.getParty().isInCommandChannel() && attacker.getParty().getCommandChannel().meetRaidWarCondition(this))
		{
			if (_firstCommandChannelAttacked == null) // looting right isn't set
			{
				synchronized (this)
				{
					if (_firstCommandChannelAttacked == null)
					{
						_firstCommandChannelAttacked = attacker.getParty().getCommandChannel();
						if (_firstCommandChannelAttacked != null)
						{
							_commandChannelTimer = new CommandChannelTimer(this);
							_commandChannelLastAttack = System.currentTimeMillis();
							ThreadPoolManager.getInstance().scheduleGeneral(_commandChannelTimer, 10000); // check for last attack
							_firstCommandChannelAttacked.broadcastToChannelMembers(new CreatureSay(0, ChatType.PARTYROOM_ALL.getClientId(), "", "You have looting rights!")); // TODO: retail msg
						}
					}
				}
			}
			else if (attacker.getParty().getCommandChannel().equals(_firstCommandChannelAttacked)) // is in same channel
				_commandChannelLastAttack = System.currentTimeMillis(); // update last attack time
		}
		
		// Add damage and hate to the attacker AggroInfo of the L2Attackable _aggroList
		if (attacker != null)
			addDamageHate(attacker, (int) damage, (int) damage);
		
		// Reduce the current HP of the L2Attackable and launch the doDie Task if necessary
		super.reduceCurrentHp(damage, attacker, awake);
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		// Kill the L2Npc (the corpse disappeared after 7 seconds)
		if (!super.doDie(killer))
			return false;
		
		// Notify the Quest Engine of the L2Attackable death if necessary		
		if (killer.isPlayer())
		{
			levelSoulCrystals(killer);

			final List<Quest> scripts = getTemplate().getEventQuests(QuestEventType.ON_KILL);
			if (scripts != null)
				for (Quest quest : scripts)
					ThreadPoolManager.getInstance().scheduleEffect(() -> quest.notifyKill(this, killer.getActingPlayer(),killer instanceof L2Summon), 3000);
		}
			
		return true;
	}
	
	@Override
	protected void calculateRewards(L2Character lastAttacker)
	{
		// Creates an empty list of rewards
		Map<L2Character, RewardInfo> rewards = new ConcurrentHashMap<>();
		
		try
		{
			if (_aggroListPro.isEmpty())
				return;
			
			int damage;
			L2Character attacker, ddealer;
			
			L2PcInstance maxDealer = null;
			int maxDamage = 0;
			
			// Go through the _aggroList of the L2Attackable
			for (AggroInfo info : _aggroListPro.values())
			{
				if (info == null)
					continue;
				
				// Get the L2Character corresponding to this attacker
				attacker = info.getAttacker();
				
				// Get damages done by this attacker
				damage = info.getDamage();
				
				// Prevent unwanted behavior
				if (damage > 1)
				{
					if ((attacker instanceof L2SummonInstance) || ((attacker instanceof L2PetInstance) && ((L2PetInstance) attacker).getPetData().getOwnerExpTaken() > 0))
						ddealer = ((L2Summon) attacker).getOwner();
					else
						ddealer = info.getAttacker();
					
					// Check if ddealer isn't too far from this (killed monster)
					if (!Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, ddealer, true))
						continue;
					
					// Calculate real damages (Summoners should get own damage plus summon's damage)
					RewardInfo reward = rewards.get(ddealer);
					
					if (reward == null)
						reward = new RewardInfo(ddealer, damage);
					else
						reward.addDamage(damage);
					
					rewards.put(ddealer, reward);
					
					if (ddealer.getActingPlayer() != null && reward._dmg > maxDamage)
					{
						maxDealer = ddealer.getActingPlayer();
						maxDamage = reward._dmg;
					}
				}
			}
			
			// Manage Base, Quests and Sweep drops of the L2Attackable
			doItemDrop(maxDealer != null && maxDealer.isOnline() == 1 ? maxDealer : lastAttacker);
			
			if (lastAttacker == null)
				return;
			
			if (!getMustRewardExpSP())
				return;
			
			if (!rewards.isEmpty())
			{
				long exp, exp_premium = 0;
				int levelDiff, partyDmg, partyLvl, sp, sp_premium;
				float partyMul, penalty;
				RewardInfo reward2;
				int[] tmp;
				
				for (RewardInfo reward : rewards.values())
				{
					if (reward == null)
						continue;
					
					// Penalty applied to the attacker's XP
					penalty = 0;
					
					// Attacker to be rewarded
					attacker = reward._attacker;
					
					// Total amount of damage done
					damage = reward._dmg;
					
					// If the attacker is a Pet, get the party of the owner
					if (attacker instanceof L2PetInstance)
						attackerParty = ((L2PetInstance) attacker).getParty();
					else if (attacker instanceof L2PcInstance)
						attackerParty = ((L2PcInstance) attacker).getParty();
					else
						return;
					
					// If this attacker is a L2PcInstance with a summoned L2SummonInstance, get Exp Penalty applied for the current summoned L2SummonInstance
					if (attacker instanceof L2PcInstance && ((L2PcInstance) attacker).getPet() instanceof L2SummonInstance)
						penalty = ((L2SummonInstance) ((L2PcInstance) attacker).getPet()).getExpPenalty();
					
					// We must avoid "over damage", if any
					if (damage > getMaxHp())
						damage = getMaxHp();
					
					// If there's NO party in progress
					if (attackerParty == null && !attacker.isDead() && isInSurroundingRegion(attacker))
					{
						levelDiff = attacker.getLevel() - getLevel();
						
						tmp = calculateExpAndSp(levelDiff, damage, attacker.getPremiumService());
						exp = tmp[0];
						exp *= 1 - penalty;
						sp = tmp[1];
						
						if (isChampion())
						{
							exp *= Config.CHAMPION_REWARDS;
							sp *= Config.CHAMPION_REWARDS;
						}
						
						// Check for an over-hit enabled strike
						if (attacker instanceof L2PcInstance)
						{
							L2PcInstance player = (L2PcInstance) attacker;

							if (isOverhit() && attacker == getOverhitAttacker())
							{
								((L2PcInstance) attacker).sendPacket(SystemMessageId.OVER_HIT);
								exp += calculateOverhitExp(exp);
								exp_premium += calculateOverhitExp(exp_premium);
							}
							player.updateKarmaLoss(exp);
						}
						
						// Distribute the Exp and SP between the L2PcInstance and its L2Summon
						attacker.addExpAndSp(Math.round(exp), sp);
					}
					else
					{
						// share with party members
						partyDmg = 0;
						partyMul = 1.f;
						partyLvl = 0;
						
						// Get all L2Character that can be rewarded in the party
						List<L2Playable> rewardedMembers = new ArrayList<>();
						
						// Go through all L2PcInstance in the party
						List<L2PcInstance> groupMembers;
						if (attackerParty.isInCommandChannel())
							groupMembers = attackerParty.getCommandChannel().getMembers();
						else
							groupMembers = attackerParty.getPartyMembers();
						
						for (L2PcInstance pl : groupMembers)
						{
							if (pl == null || pl.isDead())
								continue;
							
							// Get the RewardInfo of this L2PcInstance from L2Attackable rewards
							reward2 = rewards.get(pl);
							
							// If the L2PcInstance is in the L2Attackable rewards add its damages to party damages
							if (reward2 != null)
							{
								if (Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, pl, true))
								{
									partyDmg += reward2._dmg; // Add L2PcInstance damages to party damages
									rewardedMembers.add(pl);
									
									if (pl.getLevel() > partyLvl)
									{
										if (attackerParty.isInCommandChannel())
											partyLvl = attackerParty.getCommandChannel().getLevel();
										else
											partyLvl = pl.getLevel();
									}
								}
								rewards.remove(pl); // Remove the L2PcInstance from the L2Attackable rewards
							}
							else
							{
								// Add L2PcInstance of the party (that have attacked or not) to members that can be rewarded
								// and in range of the monster.
								if (Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, pl, true))
								{
									rewardedMembers.add(pl);
									if (pl.getLevel() > partyLvl)
									{
										if (attackerParty.isInCommandChannel())
											partyLvl = attackerParty.getCommandChannel().getLevel();
										else
											partyLvl = pl.getLevel();
									}
								}
							}
							
							L2Playable summon = pl.getPet();
							if (summon != null && summon instanceof L2PetInstance)
							{
								reward2 = rewards.get(summon);
								if (reward2 != null) // Pets are only added if they have done damage
								{
									if (Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, summon, true))
									{
										partyDmg += reward2._dmg; // Add summon damages to party damages
										rewardedMembers.add(summon);
										
										if (summon.getLevel() > partyLvl)
											partyLvl = summon.getLevel();
									}
									rewards.remove(summon); // Remove the summon from the L2Attackable rewards
								}
							}
						}
						
						// If the party didn't killed this L2Attackable alone
						if (partyDmg < getMaxHp())
							partyMul = ((float) partyDmg / (float) getMaxHp());
						
						// Avoid "over damage"
						if (partyDmg > getMaxHp())
							partyDmg = getMaxHp();
						
						// Calculate the level difference between Party and L2Attackable
						levelDiff = partyLvl - getLevel();
						
						// Calculate Exp and SP rewards
						tmp = calculateExpAndSp(levelDiff, partyDmg, 1);
						exp_premium = tmp[0];
						sp_premium = tmp[1];
						tmp = calculateExpAndSp(levelDiff, partyDmg, 0);
						exp = tmp[0];
						sp = tmp[1];
						
						if (isChampion())
						{
							exp *= Config.CHAMPION_REWARDS;
							sp *= Config.CHAMPION_REWARDS;
						}
						
						exp *= partyMul;
						sp *= partyMul;
						exp_premium *= partyMul;
						sp_premium *= partyMul;
						
						// Check for an over-hit enabled strike
						// (When in party, the over-hit exp bonus is given to the whole party and splitted proportionally through the party members)
						if (attacker instanceof L2PcInstance)
						{
							L2PcInstance player = (L2PcInstance) attacker;

							if (isOverhit() && player == getOverhitAttacker())
							{
								player.sendPacket(SystemMessageId.OVER_HIT);
								exp += calculateOverhitExp(exp);
							}
						}
						
						// Distribute Experience and SP rewards to L2PcInstance Party members in the known area of the last attacker
						if (partyDmg > 0)
							attackerParty.distributeXpAndSp(exp_premium, sp_premium, exp, sp, rewardedMembers, partyLvl);
					}
				}
			}
			rewards = null;
		}
		catch (Exception e)
		{

		}
	}
	
	@Override
	public void addAttackerToAttackByList(L2Character player)
	{
		if (player == null || player == this || getAttackByList().contains(player))
			return;
		
		getAttackByList().add(player);
	}
	
	private RewardItem calculateRewardItem(L2PcInstance lastAttacker, L2DropData drop, int levelModifier, boolean isSweep)
	{
		// Get default drop chance
		double dropChance = drop.getChance();
		
		if (Config.DEEPBLUE_DROP_RULES)
		{
			int deepBlueDrop = 1;
			if (levelModifier > 0)
			{
				// We should multiply by the server's drop rate, so we always get a low chance of drop for deep blue mobs.
				// NOTE: This is valid only for adena drops! Others drops will still obey server's rate
				deepBlueDrop = 3;
				if (drop.getItemId() == 57)
				{
					deepBlueDrop *= isRaid() && !isRaidMinion() ? (int) Config.RATE_DROP_ITEMS_BY_RAID : (int) Config.RATE_DROP_ITEMS;
					if (deepBlueDrop == 0) // avoid div by 0
						deepBlueDrop = 1;
				}
			}
			
			// Check if we should apply our maths so deep blue mobs will not drop that easy
			dropChance = ((drop.getChance() - ((drop.getChance() * levelModifier) / 100)) / deepBlueDrop);
		}
		
		// Applies Drop rates
		if (drop.getItemId() == 57)
			dropChance *= (lastAttacker.getPremiumService() == 1 ? Config.PREMIUM_RATE_DROP_ADENA : Config.RATE_DROP_ADENA);
		else if (isSweep)
			dropChance *= (lastAttacker.getPremiumService() == 1 ? Config.PREMIUM_RATE_DROP_SPOIL : Config.RATE_DROP_SPOIL);
		else if (lastAttacker.getPremiumService() == 1)
			dropChance *= isRaid() && !isRaidMinion() ? Config.PREMIUM_RATE_DROP_ITEMS_BY_RAID : Config.PREMIUM_RATE_DROP_ITEMS;
		else
			dropChance *= isRaid() && !isRaidMinion() ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS;
		
		if (isChampion())
			dropChance *= Config.CHAMPION_REWARDS;
		
		// Set our limits for chance of drop
		if (dropChance < 1)
			dropChance = 1;
		
		// Get min and max Item quantity that can be dropped in one time
		final int minCount = drop.getMinDrop();
		final int maxCount = drop.getMaxDrop();
		
		// Get the item quantity dropped
		int itemCount = 0;
		
		// Check if the Item must be dropped
		int random = Rnd.get(L2DropData.MAX_CHANCE);
		while (random < dropChance)
		{
			// Get the item quantity dropped
			if (minCount < maxCount)
				itemCount += Rnd.get(minCount, maxCount);
			else if (minCount == maxCount)
				itemCount += minCount;
			else
				itemCount++;
			
			// Prepare for next iteration if dropChance > L2DropData.MAX_CHANCE
			dropChance -= L2DropData.MAX_CHANCE;
		}
		
		if (isChampion())
			if (drop.getItemId() == 57 || (drop.getItemId() >= 6360 && drop.getItemId() <= 6362))
				itemCount *= Config.CHAMPION_ADENA;
		
		if (itemCount > 0)
			return new RewardItem(drop.getItemId(), itemCount);
		
		return null;
	}
	
	private RewardItem calculateCategorizedRewardItem(L2PcInstance lastAttacker, L2DropCategory categoryDrops, int levelModifier)
	{
		if (categoryDrops == null)
			return null;
		
		// Get default drop chance for the category (that's the sum of chances for all items in the category)
		// keep track of the base category chance as it'll be used later, if an item is drop from the category.
		// for everything else, use the total "categoryDropChance"
		int basecategoryDropChance = categoryDrops.getCategoryChance();
		int categoryDropChance = basecategoryDropChance;
		
		if (Config.DEEPBLUE_DROP_RULES)
		{
			int deepBlueDrop = (levelModifier > 0) ? 3 : 1;
			
			// Check if we should apply our maths so deep blue mobs will not drop that easy
			categoryDropChance = ((categoryDropChance - ((categoryDropChance * levelModifier) / 100)) / deepBlueDrop);
		}
		
		// Applies Drop rates
		categoryDropChance *= isRaid() && !isRaidMinion() ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS;
		
		if (isChampion())
			categoryDropChance *= Config.CHAMPION_REWARDS;
		
		// Set our limits for chance of drop
		if (categoryDropChance < 1)
			categoryDropChance = 1;
		
		// Check if an Item from this category must be dropped
		if (Rnd.get(L2DropData.MAX_CHANCE) < categoryDropChance)
		{
			L2DropData drop = categoryDrops.dropOne(isRaid() && !isRaidMinion());
			if (drop == null)
				return null;
			
			// Now decide the quantity to drop based on the rates and penalties. To get this value
			// simply divide the modified categoryDropChance by the base category chance. This
			// results in a chance that will dictate the drops amounts: for each amount over 100
			// that it is, it will give another chance to add to the min/max quantities.
			//
			// For example, If the final chance is 120%, then the item should drop between
			// its min and max one time, and then have 20% chance to drop again. If the final
			// chance is 330%, it will similarly give 3 times the min and max, and have a 30%
			// chance to give a 4th time.
			// At least 1 item will be dropped for sure. So the chance will be adjusted to 100%
			// if smaller.
			
			double dropChance = drop.getChance();
			
			if (drop.getItemId() == 57)
				dropChance *= (lastAttacker.getPremiumService() == 1 ? Config.PREMIUM_RATE_DROP_ADENA : Config.RATE_DROP_ADENA);
			else if (lastAttacker.getPremiumService() == 1)
				dropChance *= isRaid() && !isRaidMinion() ? Config.PREMIUM_RATE_DROP_ITEMS_BY_RAID : Config.PREMIUM_RATE_DROP_ITEMS;
			else
				dropChance *= isRaid() && !isRaidMinion() ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS;
			
			if (isChampion())
				dropChance *= Config.CHAMPION_REWARDS;
			
			if (dropChance < L2DropData.MAX_CHANCE)
				dropChance = L2DropData.MAX_CHANCE;
			
			// Get min and max Item quantity that can be dropped in one time
			final int min = drop.getMinDrop();
			final int max = drop.getMaxDrop();
			
			// Get the item quantity dropped
			int itemCount = 0;
			
			// Check if the Item must be dropped
			int random = Rnd.get(L2DropData.MAX_CHANCE);
			while (random < dropChance)
			{
				// Get the item quantity dropped
				if (min < max)
					itemCount += Rnd.get(min, max);
				else if (min == max)
					itemCount += min;
				else
					itemCount++;
				
				// Prepare for next iteration if dropChance > L2DropData.MAX_CHANCE
				dropChance -= L2DropData.MAX_CHANCE;
			}
			
			if (isChampion())
				if (drop.getItemId() == 57 || (drop.getItemId() >= 6360 && drop.getItemId() <= 6362))
					itemCount *= Config.CHAMPION_ADENA;
			
			if (itemCount > 0)
				return new RewardItem(drop.getItemId(), itemCount);
		}
		return null;
	}
	
	private int calculateLevelModifierForDrop(L2PcInstance lastAttacker)
	{
		if (Config.DEEPBLUE_DROP_RULES)
		{
			int highestLevel = lastAttacker.getLevel();
			
			// Check to prevent very high level player to nearly kill mob and let low level player do the last hit.
			if (!getAttackByList().isEmpty())
			{
				for (L2Character atkChar : getAttackByList())
					if (atkChar != null && atkChar.getLevel() > highestLevel)
						highestLevel = atkChar.getLevel();
			}
			
			// According to official data (Prima), deep blue mobs are 9 or more levels below players
			if (highestLevel - 9 >= getLevel())
				return ((highestLevel - (getLevel() + 8)) * 9);
		}
		return 0;
	}
	
	public void doItemDrop(L2Character mainDamageDealer)
	{
		doItemDrop(getTemplate(), mainDamageDealer);
	}
	
	public void doItemDrop(L2NpcTemplate npcTemplate, L2Character lastAttacker)
	{
		L2PcInstance player = null;
		if (lastAttacker instanceof L2PcInstance)
			player = (L2PcInstance) lastAttacker;
		else if (lastAttacker instanceof L2Summon)
			player = ((L2Summon) lastAttacker).getOwner();
		
		if (player == null)
			return;
		
		final int levelModifier = calculateLevelModifierForDrop(player); // level
		
		// Check the drop of a cursed weapon
		if (levelModifier == 0 && player.getLevel() > 20)
			CursedWeaponsManager.getInstance().checkDrop(this, player);
		
		// now throw all categorized drops and handle spoil.
		if (npcTemplate.getDropData() != null)
			// now throw all categorized drops and handle spoil.
			for (L2DropCategory cat : npcTemplate.getDropData())
			{
				RewardItem item = null;
				if (cat.isSweep())
				{
					// according to sh1ny, seeded mobs CAN be spoiled and swept.
					if (isSpoil())
					{						
						for (L2DropData drop : cat.getAllDrops())
						{
							item = calculateRewardItem(player, drop, levelModifier, true);
							if (item == null)
								continue;

							_sweepItems.add(item);
						}
					}
				}
				else
				{
					if (isSeeded())
					{
						L2DropData drop = cat.dropSeedAllowedDropsOnly();
						if (drop == null)
							continue;
						
						item = calculateRewardItem(player, drop, levelModifier, false);
					}
					else
						item = calculateCategorizedRewardItem(player, cat, levelModifier);
					
					if (item != null)
					{
						// Check if the autoLoot mode is active
						if (Config.AUTO_LOOT && !(this instanceof L2RaidBossInstance) && !(this instanceof L2MinionInstance) && !(this instanceof L2GrandBossInstance))
							player.doAutoLoot(this, item); // Give this or these Item(s) to the L2PcInstance that has killed the L2Attackable
						else if (Config.AUTO_LOOT_RAID && this instanceof L2RaidBossInstance && !(this instanceof L2MinionInstance))
							player.doAutoLoot(this, item);
						else if (Config.AUTO_LOOT_GRAND && this instanceof L2GrandBossInstance)
							player.doAutoLoot(this, item);
						else
							dropItem(player, item); // drop the item on the ground
							
						// Broadcast message if RaidBoss was defeated
						// if(this instanceof L2RaidBossInstance)
						if (isRaid() && !isRaidMinion())
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DIED_DROPPED_S3_S2);
							sm.addString(getName());
							sm.addItemName(item.getItemId());
							sm.addNumber(item.getCount());
							broadcastPacket(sm);
						}
					}
				}
			}
		
		// Apply Special Item drop with rnd qty for champions
		if (isChampion() && Math.abs(getLevel() - player.getLevel()) <= Config.CHAMPION_SPCL_LVL_DIFF && !getTemplate().isQuestMonster() && Config.CHAMPION_SPCL_CHANCE > 0 && Rnd.get(100) < Config.CHAMPION_SPCL_CHANCE)
		{
			int champqty = Rnd.get(Config.CHAMPION_SPCL_QTY) + 1;
			// quantity should actually vary between 1 and whatever admin specified as max, inclusive.
			// Give this or these Item(s) to the L2PcInstance that has killed
			// the L2Attackable
			RewardItem item = new RewardItem(Config.CHAMPION_SPCL_ITEM, champqty);
			if (Config.AUTO_LOOT)
				player.doAutoLoot(this, item);
			else
				dropItem(player, item);
		}
		
		// Instant Item Drop :>
		double rateHp = getStat().calcStat(Stats.MAX_HP, 1, this, null);
		if (rateHp <= 1 && String.valueOf(npcTemplate.type).contentEquals("L2Monster")) // only L2Monster with <= 1x HP can drop herbs
		{
			boolean _hp = false;
			boolean _mp = false;
			boolean _spec = false;
			
			// ptk - patk type enhance
			int random = Rnd.get(1000); // note *10
			if ((random < Config.RATE_DROP_SPECIAL_HERBS) && !_spec) // && !_spec useless yet
			{
				RewardItem item = new RewardItem(8612, 1); // Herb of Warrior
				if (Config.AUTO_LOOT && Config.AUTO_LOOT_HERBS)
					player.addItem("Loot", item.getItemId(), item.getCount(), this, true);
				else
					dropItem(player, item);
				_spec = true;
			}
			else
			{
				for (int i = 0; i < 3; i++)
				{
					random = Rnd.get(100);
					if (random < Config.RATE_DROP_COMMON_HERBS)
					{
						item = null;
						if (i == 0)
							item = new RewardItem(8606, 1); // Herb of Power
						if (i == 1)
							item = new RewardItem(8608, 1); // Herb of Atk. Spd.
						if (i == 2)
							item = new RewardItem(8610, 1); // Herb of Critical
							// Attack
						
						if (Config.AUTO_LOOT && Config.AUTO_LOOT_HERBS)
							player.addItem("Loot", item.getItemId(), item.getCount(), this, true);
						else
							dropItem(player, item);
						break;
					}
				}
			}
			
			// mtk - matk type enhance
			random = Rnd.get(1000); // note *10
			if ((random < Config.RATE_DROP_SPECIAL_HERBS) && !_spec)
			{
				RewardItem item = new RewardItem(8613, 1); // Herb of Mystic
				if (Config.AUTO_LOOT && Config.AUTO_LOOT_HERBS)
					player.addItem("Loot", item.getItemId(), item.getCount(), this, true);
				else
					dropItem(player, item);
				_spec = true;
			}
			else
			{
				for (int i = 0; i < 2; i++)
				{
					random = Rnd.get(100);
					if (random < Config.RATE_DROP_COMMON_HERBS)
					{
						item2 = null;
						if (i == 0)
							item2 = new RewardItem(8607, 1); // Herb of Magic
						if (i == 1)
							item2 = new RewardItem(8609, 1); // Herb of Casting
						
						if (Config.AUTO_LOOT && Config.AUTO_LOOT_HERBS)
							player.addItem("Loot", item2.getItemId(), item2.getCount(), this, true);
						else
							dropItem(player, item2);
						break;
					}
				}
			}
			
			// hp+mp type
			random = Rnd.get(1000); // note *10
			if ((random < Config.RATE_DROP_SPECIAL_HERBS) && !_spec)
			{
				RewardItem item = new RewardItem(8614, 1); // Herb of Recovery
				if (Config.AUTO_LOOT && Config.AUTO_LOOT_HERBS)
					player.addItem("Loot", item.getItemId(), item.getCount(), this, true);
				else
					dropItem(player, item);
				_mp = true;
				_hp = true;
				_spec = true;
			}
			// hp - restore hp type
			if (!_hp)
			{
				random = Rnd.get(100);
				if (random < Config.RATE_DROP_MP_HP_HERBS)
				{
					RewardItem item = new RewardItem(8600, 1); // Herb of Life
					if (Config.AUTO_LOOT && Config.AUTO_LOOT_HERBS)
						player.addItem("Loot", item.getItemId(), item.getCount(), this, true);
					else
						dropItem(player, item);
					
					_hp = true;
				}
			}
			if (!_hp)
			{
				random = Rnd.get(100);
				if (random < Config.RATE_DROP_GREATER_HERBS)
				{
					RewardItem item = new RewardItem(8601, 1); // Greater Herb
					// of Life
					if (Config.AUTO_LOOT && Config.AUTO_LOOT_HERBS)
						player.addItem("Loot", item.getItemId(), item.getCount(), this, true);
					else
						dropItem(player, item);

					_hp = true;
				}
			}
			if (!_hp)
			{
				random = Rnd.get(1000); // note *10
				if (random < Config.RATE_DROP_SUPERIOR_HERBS)
				{
					RewardItem item = new RewardItem(8602, 1); // Superior Herb
					// of Life
					if (Config.AUTO_LOOT && Config.AUTO_LOOT_HERBS)
						player.addItem("Loot", item.getItemId(), item.getCount(), this, true);
					else
						dropItem(player, item);
				}
			}
			// mp - restore mp type
			if (!_mp)
			{
				random = Rnd.get(100);
				if (random < Config.RATE_DROP_MP_HP_HERBS)
				{
					RewardItem item = new RewardItem(8603, 1); // Herb of Manna
					if (Config.AUTO_LOOT && Config.AUTO_LOOT_HERBS)
						player.addItem("Loot", item.getItemId(), item.getCount(), this, true);
					else
						dropItem(player, item);
					_mp = true;
				}
			}
			if (!_mp)
			{
				random = Rnd.get(100);
				if (random < Config.RATE_DROP_GREATER_HERBS)
				{
					RewardItem item = new RewardItem(8604, 1); // Greater Herb
					// of Mana
					if (Config.AUTO_LOOT && Config.AUTO_LOOT_HERBS)
						player.addItem("Loot", item.getItemId(), item.getCount(), this, true);
					else
						dropItem(player, item);
					_mp = true;
				}
			}
			if (!_mp)
			{
				random = Rnd.get(1000); // note *10
				if (random < Config.RATE_DROP_SUPERIOR_HERBS)
				{
					RewardItem item = new RewardItem(8605, 1); // Superior Herb
					// of Mana
					if (Config.AUTO_LOOT && Config.AUTO_LOOT_HERBS)
						player.addItem("Loot", item.getItemId(), item.getCount(), this, true);
					else
						dropItem(player, item);
				}
			}
			// speed enhance type
			random = Rnd.get(100);
			if (random < Config.RATE_DROP_COMMON_HERBS)
			{
				RewardItem item = new RewardItem(8611, 1); // Herb of Speed
				if (Config.AUTO_LOOT && Config.AUTO_LOOT_HERBS)
					player.addItem("Loot", item.getItemId(), item.getCount(), this, true);
				else
					dropItem(player, item);
			}
		}
		
		if (Config.ALLOW_PRIVATE_ANTI_BOT  && player != null && Rnd.get(1000) < 5)
		    BotsPreventionManager.getInstance().StartCheck(player);	
	}
	
	public L2ItemInstance dropItem(L2PcInstance mainDamageDealer, RewardItem item)
	{
		int randDropLim = 70;
		
		L2ItemInstance ditem = null;
		for (int i = 0; i < item.getCount(); i++)
		{
			// Randomize drop position
			int newX = getX() + Rnd.get(randDropLim * 2 + 1) - randDropLim;
			int newY = getY() + Rnd.get(randDropLim * 2 + 1) - randDropLim;
			int newZ = Math.max(getZ(), mainDamageDealer.getZ()) + 20;
			
			if (ItemTable.getInstance().getTemplate(item.getItemId()) != null)
			{
				// Init the dropped L2ItemInstance and add it in the world as a visible object at the position where mob was last
				ditem = ItemTable.getInstance().createItem("Loot", item.getItemId(), item.getCount(), mainDamageDealer, this);
				ditem.dropMe(this, newX, newY, newZ);
				
				// Add drop to auto destroy item task
				if (Config.AUTODESTROY_ITEM_AFTER > 0 && !Config.LIST_PROTECTED_ITEMS.contains(item.getItemId()))
					ItemsAutoDestroy.getInstance().addItem(ditem , ditem.isHerb());

				ditem.setProtected(false);
				
				// If stackable, end loop as entire count is included in 1 instance of item
				if (ditem.isStackable() || !Config.MULTIPLE_ITEM_DROP)
					break;
			}
			else
				_log.warning(L2Attackable.class.getName() + ": Item doesn't exist so cannot be dropped. Item ID: " + item.getItemId());
		}
		return ditem;
	}
	
	public L2ItemInstance dropItem(L2PcInstance lastAttacker, int itemId, int itemCount)
	{
		return dropItem(lastAttacker, new RewardItem(itemId, itemCount));
	}
	
	public L2ItemInstance getActiveWeapon()
	{
		return null;
	}
	
	public boolean gotNoTarget()
	{
		return _aggroListPro.isEmpty();
	}
	
	public boolean containsTarget(L2Character player)
	{
		return _aggroListPro.containsKey(player);
	}
	
	public void clearAggroList()
	{
		_aggroListPro.clear();
	}
	
	public boolean isSweepActive()
	{
		return !_sweepItems.isEmpty();
	}

	public synchronized List<RewardItem> getSweepItems()
	{
		return _sweepItems;
	}
	
	public synchronized RewardItem[] takeHarvest()
	{
		RewardItem[] harvest = _harvestItems;
		_harvestItems = null;
		return harvest;
	}
	
	public void overhitEnabled(boolean status)
	{
		_overhit = status;
	}
	
	public void setOverhitValues(L2Character attacker, double damage)
	{
		// Calculate the over-hit damage
		// Ex: mob had 10 HP left, over-hit skill did 50 damage total, over-hit damage is 40
		double overhitDmg = ((getCurrentHp() - damage) * (-1));
		if (overhitDmg < 0)
		{
			// we didn't killed the mob with the over-hit strike. (it wasn't really an over-hit strike)
			// let's just clear all the over-hit related values
			overhitEnabled(false);
			_overhitDamage = 0;
			_overhitAttacker = null;
			return;
		}
		overhitEnabled(true);
		_overhitDamage = overhitDmg;
		_overhitAttacker = attacker;
	}
	
	public L2Character getOverhitAttacker()
	{
		return _overhitAttacker;
	}
	
	public double getOverhitDamage()
	{
		return _overhitDamage;
	}
	
	public boolean isOverhit()
	{
		return _overhit;
	}
	
	public void absorbSoul()
	{
		_absorbed = true;
	}
	
	public static boolean isAbsorbed()
	{
		return _absorbed;
	}
	
	public void addAbsorber(L2PcInstance attacker)
	{
		// If we have no _absorbersList initiated, do it
		AbsorberInfo ai = _absorbersList.get(attacker.getObjectId());
		
		// If the L2Character attacker isn't already in the _absorbersList of this L2Attackable, add it
		if (ai == null)
		{
			ai = new AbsorberInfo(attacker.getObjectId(), getCurrentHp());
			_absorbersList.put(attacker.getObjectId(), ai);
		}
		else
		{
			ai._objId = attacker.getObjectId();
			ai._absorbedHP = getCurrentHp();
		}
		
		// Set this L2Attackable as absorbed
		absorbSoul();
	}
	
	public void resetAbsorbList()
	{
		_absorbed = false;
		_absorbersList.clear();
	}
	
	public Map<Integer, AbsorberInfo> getAbsorbersList()
	{
		return _absorbersList;
	}
	
	private int[] calculateExpAndSp(int diff, int damage, int IsPremium)
	{
		
		double xp;
		double sp;
		
		if (diff < -5)
		{
			diff = -5; // makes possible to use ALT_GAME_EXPONENT configuration
		}
		xp = (double) getExpReward(IsPremium) * damage / getMaxHp();
		if (Config.ALT_GAME_EXPONENT_XP != 0)
		{
			xp *= Math.pow(2., -diff / Config.ALT_GAME_EXPONENT_XP);
		}
		
		sp = (double) getSpReward(IsPremium) * damage / getMaxHp();
		if (Config.ALT_GAME_EXPONENT_SP != 0)
		{
			sp *= Math.pow(2., -diff / Config.ALT_GAME_EXPONENT_SP);
		}
		
		if (Config.ALT_GAME_EXPONENT_XP == 0 && Config.ALT_GAME_EXPONENT_SP == 0)
		{
			if (diff > 5) // formula revised May 07
			{
				double pow = Math.pow((double) 5 / 6, diff - 5);
				xp = xp * pow;
				sp = sp * pow;
			}
			
			if (xp <= 0)
			{
				xp = 0;
				sp = 0;
			}
			else if (sp <= 0)
			{
				sp = 0;
			}
		}
		
		int[] tmp =
		{
			(int) xp,
			(int) sp
		};
		
		return tmp;
	}
	
	public long calculateOverhitExp(long normalExp)
	{
		// Get the percentage based on the total of extra (over-hit) damage done relative to the total (maximum) ammount of HP on the L2Attackable
		double overhitPercentage = ((getOverhitDamage() * 100) / getMaxHp());
		
		// Over-hit damage percentages are limited to 25% max
		if (overhitPercentage > 25)
			overhitPercentage = 25;
		
		// Get the overhit exp bonus according to the above over-hit damage percentage
		// (1/1 basis - 13% of over-hit damage, 13% of extra exp is given, and so on...)
		double overhitExp = ((overhitPercentage / 100) * normalExp);
		
		// Return the rounded ammount of exp points to be added to the player's normal exp reward
		return Math.round(overhitExp);		
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		
		// Clear mob spoil/seed state
		setIsSpoiledBy(0);
		
		// Clear all aggro char from list
		clearAggroList();
		
		// Clear Harvester Reward List
		_harvestItems = null;
		
		// Clear mod Seeded stat
		_seedType = 0;
		_seederObjId = 0;
		
		// Clear overhit value
		overhitEnabled(false);
		
		_sweepItems.clear();
		resetAbsorbList();
		
		setWalking();
		
		// check the region where this mob is, do not activate the AI if region is inactive.
		if (hasAI() && !isInActiveRegion())
			getAI().stopAITask();
	}
	
	public void setSeeded(L2PcInstance seeder)
	{
		if (_seedType != 0 && _seederObjId == seeder.getObjectId())
			setSeeded(_seedType, seeder.getLevel());
	}
	
	public void setSeeded(int id, L2PcInstance seeder)
	{
		if (_seedType == 0)
		{
			_seedType = id;
			_seederObjId = seeder.getObjectId();
		}
	}
	
	private void setSeeded(int id, int seederLvl)
	{
		_seedType = id;
		int count = 1;
		
		for (int skillId : getTemplate().getSkills().keySet())
		{
			switch (skillId)
			{
				case 4303: // Strong type x2
					count *= 2;
					break;
				case 4304: // Strong type x3
					count *= 3;
					break;
				case 4305: // Strong type x4
					count *= 4;
					break;
				case 4306: // Strong type x5
					count *= 5;
					break;
				case 4307: // Strong type x6
					count *= 6;
					break;
				case 4308: // Strong type x7
					count *= 7;
					break;
				case 4309: // Strong type x8
					count *= 8;
					break;
				case 4310: // Strong type x9
					count *= 9;
					break;
			}
		}
		
		int diff = (getLevel() - (L2Manor.getInstance().getSeedLevel(_seedType) - 5));
		if (diff > 0)
			count += diff;
		
		List<RewardItem> harvested = new ArrayList<>();
		
		harvested.add(new RewardItem(L2Manor.getInstance().getCropType(_seedType), count * Config.RATE_DROP_MANOR));
		
		_harvestItems = harvested.toArray(new RewardItem[harvested.size()]);
	}
	
	public int getSeederId()
	{
		return _seederObjId;
	}
	
	public int getSeedType()
	{
		return _seedType;
	}
	
	public boolean isSeeded()
	{
		return _seedType > 0;
	}
	
	@Override
	public boolean hasRandomAnimation()
	{
		return ((Config.MAX_MONSTER_ANIMATION > 0) && !isRaid());
	}
	
	@Override
	public boolean isMob()
	{
		return true; // This means we use MAX_MONSTER_ANIMATION instead of MAX_NPC_ANIMATION
	}
	
	protected void setCommandChannelTimer(CommandChannelTimer commandChannelTimer)
	{
		_commandChannelTimer = commandChannelTimer;
	}
	
	public CommandChannelTimer getCommandChannelTimer()
	{
		return _commandChannelTimer;
	}
	
	public L2CommandChannel getFirstCommandChannelAttacked()
	{
		return _firstCommandChannelAttacked;
	}
	
	public void setFirstCommandChannelAttacked(L2CommandChannel firstCommandChannelAttacked)
	{
		_firstCommandChannelAttacked = firstCommandChannelAttacked;
	}
	
	public long getCommandChannelLastAttack()
	{
		return _commandChannelLastAttack;
	}
	
	public void setCommandChannelLastAttack(long channelLastAttack)
	{
		_commandChannelLastAttack = channelLastAttack;
	}
	
	private static class CommandChannelTimer implements Runnable
	{
		private final L2Attackable _monster;
		
		public CommandChannelTimer(L2Attackable monster)
		{
			_monster = monster;
		}
		
		@Override
		public void run()
		{
			if ((System.currentTimeMillis() - _monster.getCommandChannelLastAttack()) > 900000)
			{
				_monster.setCommandChannelTimer(null);
				_monster.setFirstCommandChannelAttacked(null);
				_monster.setCommandChannelLastAttack(0);
			}
			else
				ThreadPoolManager.getInstance().scheduleGeneral(this, 10000); // 10sec
		}
	}
	
	public boolean returnHome()
	{		
		if (hasAI() && !isDead() && !isInCombat() && getMoveSpeed() > 0 && getSpawn() != null && !isInsideRadius(getSpawn().getLocx(), getSpawn().getLocy(), Config.MAX_DRIFT_RANGE, false))
		{
			clearAggroList();
			setIsReturningToSpawnPoint(true);
			setWalking();
			getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(getSpawn().getLocx(), getSpawn().getLocy(), getSpawn().getLocz(), 0));
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean isRaid()
	{
		return _isRaid;
	}
	
	@Override
	public void setIsRaid(boolean isRaid)
	{
		_isRaid = isRaid;
	}
	
	@Override
	public void setIsRaidMinion(boolean val)
	{
		_isRaid = val;
		_isRaidMinion = val;
	}
	
	@Override
	public boolean isRaidMinion()
	{
		return _isRaidMinion;
	}
	
	public boolean isMinion()
	{
		return getLeader() != null;
	}
	
	public L2Attackable getLeader()
	{
		return null;
	}
	
	@Override
	public void moveToLocation(int x, int y, int z, int offset)
	{
		abortAllAttacks();		
		super.moveToLocation(x, y, z, offset);
	}
	
	private boolean _canReturnToSpawnPoint = true;
	
	public final boolean canReturnToSpawnPoint()
	{
		return _canReturnToSpawnPoint;
	}
	
	public final void setCanReturnToSpawnPoint(boolean value)
	{
		_canReturnToSpawnPoint = value;
	}
	
	public boolean isGuard()
	{
		return false;
	}
	
	private void levelSoulCrystals(L2Character attacker)
	{
		// Only L2PcInstance can absorb a soul
		if (!(attacker instanceof L2PcInstance) && !(attacker instanceof L2Summon))
		{
			resetAbsorbList();
			return;
		}
		
		int maxAbsorbLevel = getAbsorbLevel();
		int minAbsorbLevel = 0;
		
		// If this is not a valid L2Attackable, clears the _absorbersList and
		// just return
		if (maxAbsorbLevel == 0)
		{
			resetAbsorbList();
			return;
		}
		// All boss mobs with maxAbsorbLevel 13 have minAbsorbLevel of 12 else
		// 10
		if (maxAbsorbLevel > 10)
		{
			minAbsorbLevel = maxAbsorbLevel > 12 ? 12 : 10;
		}
		
		// Init some useful vars
		boolean isSuccess = true;
		boolean doLevelup = true;
		boolean isBossMob = maxAbsorbLevel > 10 ? true : false;
		
		L2NpcTemplate.AbsorbCrystalType absorbType = getTemplate().absorbType;
		
		// If this mob is a boss, then skip some checkings
		if (!isBossMob)
		{
			// Fail if this L2Attackable isn't absorbed or there's no one in its
			// _absorbersList
			if (!isAbsorbed())
			{
				resetAbsorbList();
				return;
			}
			
			// Fail if the killer isn't in the _absorbersList of this
			// L2Attackable and mob is not boss
			AbsorberInfo ai = _absorbersList.get((attacker instanceof L2Summon) ? ((L2Summon) attacker).getOwner() : (L2PcInstance) attacker);
			if (ai == null || ai._objId != ((attacker instanceof L2Summon) ? ((L2Summon) attacker).getOwner() : (L2PcInstance) attacker).getObjectId())
			{
				isSuccess = false;
			}
			
			// Check if the soul crystal was used when HP of this L2Attackable
			// wasn't higher than half of it
			if (ai != null && ai._absorbedHP > (getMaxHp() / 2.0))
			{
				isSuccess = false;
			}
			
			if (!isSuccess)
			{
				resetAbsorbList();
				return;
			}
		}
		
		// ********
		String[] crystalNFO = null;
		String crystalNME = "";
		
		int dice = Rnd.get(100);
		int crystalQTY = 0;
		int crystalLVL = 0;
		int crystalOLD = 0;
		int crystalNEW = 0;
		
		// ********
		// Now we have four choices:
		// 1- The Monster level is too low for the crystal. Nothing happens.
		// 2- Everything is correct, but it failed. Nothing happens. (57.5%)
		// 3- Everything is correct, but it failed. The crystal scatters. A
		// sound event is played. (10%)
		// 4- Everything is correct, the crystal level up. A sound event is
		// played. (32.5%)
		
		List<L2PcInstance> players = new ArrayList<>();
		
		if (absorbType == L2NpcTemplate.AbsorbCrystalType.FULL_PARTY && ((attacker instanceof L2Summon) ? ((L2Summon) attacker).getOwner() : (L2PcInstance) attacker).isInParty())
		{
			players = ((attacker instanceof L2Summon) ? ((L2Summon) attacker).getOwner() : (L2PcInstance) attacker).getParty().getPartyMembers();
		}
		else if (absorbType == L2NpcTemplate.AbsorbCrystalType.PARTY_ONE_RANDOM && ((attacker instanceof L2Summon) ? ((L2Summon) attacker).getOwner() : (L2PcInstance) attacker).isInParty())
		{
			// This is a naive method for selecting a random member. It gets any
			// random party member and
			// then checks if the member has a valid crystal. It does not select
			// the random party member
			// among those who have crystals, only. However, this might actually
			// be correct (same as retail).
			players.add(((attacker instanceof L2Summon) ? ((L2Summon) attacker).getOwner() : (L2PcInstance) attacker).getParty().getPartyMembers().get(Rnd.get(((attacker instanceof L2Summon) ? ((L2Summon) attacker).getOwner() : (L2PcInstance) attacker).getParty().getMemberCount())));
		}
		else
		{
			players.add((attacker instanceof L2Summon) ? ((L2Summon) attacker).getOwner() : (L2PcInstance) attacker);
		}
		
		for (L2PcInstance player : players)
		{
			if (player == null)
			{
				continue;
			}
			crystalQTY = 0;
			
			Collection<L2ItemInstance> inv = player.getInventory().getItems();
			for (L2ItemInstance item : inv)
			{
				int itemId = item.getItemId();
				for (int id : SoulCrystal.SoulCrystalTable)
				{
					// Find any of the 39 possible crystals.
					if (id == itemId)
					{
						crystalQTY++;
						// Keep count but make sure the player has no more than
						// 1 crystal
						if (crystalQTY > 1)
						{
							isSuccess = false;
							break;
						}
						
						// Validate if the crystal has already leveled
						if (id != SoulCrystal.RED_NEW_CRYSTAL && id != SoulCrystal.GRN_NEW_CYRSTAL && id != SoulCrystal.BLU_NEW_CRYSTAL)
						{
							try
							{
								if (item.getItem().getItemName().contains("Grade"))
								{
									// Split the name of the crystal into 'name'
									// & 'level'
									crystalNFO = item.getItem().getItemName().trim().replace(" Grade ", "-").split("-");
									// Set Level to 13
									crystalLVL = 13;
									// Get Name
									crystalNME = crystalNFO[0].toLowerCase();
								}
								else
								{
									// Split the name of the crystal into 'name'
									// & 'level'
									crystalNFO = item.getItem().getItemName().trim().replace(" Stage ", "").split("-");
									// Get Level
									crystalLVL = Integer.parseInt(crystalNFO[1].trim());
									// Get Name
									crystalNME = crystalNFO[0].toLowerCase();
								}
								// Allocate current and levelup ids' for higher
								// level crystals
								if (crystalLVL > 9)
								{
									for (int[] element : SoulCrystal.HighSoulConvert)
										// Get the next stage above 10 using
										// array.
										if (id == element[0])
										{
											crystalNEW = element[1];
											break;
										}
								}
								else
								{
									crystalNEW = id + 1;
								}
							}
							catch (NumberFormatException nfe)
							{
								_log.warning(L2Attackable.class.getName() + ": An attempt to identify a soul crystal failed, verify the names have not changed in etcitem table." + nfe);
								
								player.sendMessage("There has been an error handling your soul crystal. Please notify your server admin.");
								
								isSuccess = false;
								break;
							}
							catch (Exception e)
							{
								if (Config.DEVELOPER)
								{
									e.printStackTrace();
								}
								isSuccess = false;
								break;
							}
						}
						else
						{
							crystalNME = item.getItem().getItemName().toLowerCase().trim();
							crystalNEW = id + 1;
						}
						
						// Done
						crystalOLD = id;
						break;
					}
				}
				if (!isSuccess)
				{
					break;
				}
			}
			
			// If the crystal level is way too high for this mob, say that we
			// can't increase it
			if ((crystalLVL < minAbsorbLevel) || (crystalLVL >= maxAbsorbLevel))
			{
				doLevelup = false;
			}
			
			// The player doesn't have any crystals with him get to the next
			// player.
			if (crystalQTY < 1 || crystalQTY > 1 || !isSuccess || !doLevelup)
			{
				// Too many crystals in inventory.
				if (crystalQTY > 1)
				{
					player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_FAILED_RESONATION);
				}
				// The soul crystal stage of the player is way too high
				else if (!doLevelup)
				{
					player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_REFUSED);
				}
				
				crystalQTY = 0;
				continue;
			}
			
			int chanceLevelUp = isBossMob ? 70 : SoulCrystal.LEVEL_CHANCE;
			
			// If succeeds or it is a full party absorb, level up the crystal.
			if (((absorbType == L2NpcTemplate.AbsorbCrystalType.FULL_PARTY) && doLevelup) || (dice <= chanceLevelUp))
			{
				// Give staged crystal
				exchangeCrystal(player, crystalOLD, crystalNEW, false);
			}
			
			// If true and not a last-hit mob, break the crystal.
			else if ((!isBossMob) && dice >= (100.0 - SoulCrystal.BREAK_CHANCE))
			{
				// Remove current crystal an give a broken open.
				if (crystalNME.startsWith("red"))
				{
					exchangeCrystal(player, crystalOLD, SoulCrystal.RED_BROKEN_CRYSTAL, true);
				}
				else if (crystalNME.startsWith("gre"))
				{
					exchangeCrystal(player, crystalOLD, SoulCrystal.GRN_BROKEN_CYRSTAL, true);
				}
				else if (crystalNME.startsWith("blu"))
				{
					exchangeCrystal(player, crystalOLD, SoulCrystal.BLU_BROKEN_CRYSTAL, true);
				}
				resetAbsorbList();
			}
			else
			{
				player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_FAILED);
			}
		}
	}
	
	private void exchangeCrystal(L2PcInstance player, int takeid, int giveid, boolean broke)
	{
		L2ItemInstance Item = player.getInventory().destroyItemByItemId("SoulCrystal", takeid, 1, player, this);
		if (Item != null)
		{
			// Prepare inventory update packet
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addRemovedItem(Item);
			
			// Add new crystal to the killer's inventory
			Item = player.getInventory().addItem("SoulCrystal", giveid, 1, player, this);
			playerIU.addItem(Item);
			
			// Send a sound event and text message to the player
			if (broke)
			{
				player.sendPacket(SystemMessageId.SOUL_CRYSTAL_BROKE);
			}
			else
			{
				player.sendPacket(SystemMessageId.SOUL_CRYSTAL_ABSORBING_SUCCEEDED);
			}
			
			// Send system message
			SystemMessage sms = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
			sms.addItemName(giveid);
			player.sendPacket(sms);
			
			// Send inventory update packet
			player.sendPacket(playerIU);
		}
	}
	
	private int getAbsorbLevel()
	{
		return getTemplate().absorbLevel;
	}
	
	private boolean _mustGiveExpSp;
	private L2Party attackerParty;
	private RewardItem item;
	private RewardItem item2;
	
	public synchronized void setMustRewardExpSp(boolean value)
	{
		_mustGiveExpSp = value;
	}
	
	public synchronized boolean getMustRewardExpSP()
	{
		return _mustGiveExpSp;
	}
	
	public double distance2D(L2Character character)
	{
		return distance2D(character.getX(), character.getY());
	}
	
	@Override
	public double distance2D(int x, int y)
	{
		return Math.sqrt(Math.pow(getX() - x, 2) + Math.pow(getY() - y, 2));
	}
	
	@Override
	public void setTarget(L2Object object)
	{
		if (isDead())
			return;
		
		if (object == null)
		{
			final L2Object target = getTarget();
			final Map<L2Character, AggroInfo> aggroList = getAggroList();
			if (target != null)
			{
				if (aggroList != null)
					aggroList.remove(target);
			}
			if ((aggroList != null) && aggroList.isEmpty())
			{
				if (getAI() instanceof L2AttackableAI)
					((L2AttackableAI) getAI()).setGlobalAggro(-25);

				setWalking();
				clearAggroList();
			}
		}
		super.setTarget(object);
	}
}