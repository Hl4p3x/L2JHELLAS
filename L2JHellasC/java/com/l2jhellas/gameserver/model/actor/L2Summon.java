package com.l2jhellas.gameserver.model.actor;

import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.ai.L2CharacterAI;
import com.l2jhellas.gameserver.ai.L2SummonAI;
import com.l2jhellas.gameserver.enums.skills.AbnormalEffect;
import com.l2jhellas.gameserver.enums.skills.L2SkillTargetType;
import com.l2jhellas.gameserver.geodata.GeoEngine;
import com.l2jhellas.gameserver.handler.IItemHandler;
import com.l2jhellas.gameserver.handler.ItemHandler;
import com.l2jhellas.gameserver.instancemanager.ZoneManager;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.PetInventory;
import com.l2jhellas.gameserver.model.actor.group.party.L2Party;
import com.l2jhellas.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PetInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.model.actor.stat.SummonStat;
import com.l2jhellas.gameserver.model.actor.status.SummonStatus;
import com.l2jhellas.gameserver.model.base.Experience;
import com.l2jhellas.gameserver.model.zone.ZoneRegion;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.AbstractNpcInfo.SummonInfo;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jhellas.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jhellas.gameserver.network.serverpackets.PetDelete;
import com.l2jhellas.gameserver.network.serverpackets.PetInfo;
import com.l2jhellas.gameserver.network.serverpackets.PetItemList;
import com.l2jhellas.gameserver.network.serverpackets.PetStatusShow;
import com.l2jhellas.gameserver.network.serverpackets.PetStatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.RelationChanged;
import com.l2jhellas.gameserver.network.serverpackets.StatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.Formulas;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.gameserver.taskmanager.DecayTaskManager;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;
import com.l2jhellas.gameserver.templates.L2Weapon;

public abstract class L2Summon extends L2Playable
{
	public static Logger _log = Logger.getLogger(L2Summon.class.getName());
	
	private L2PcInstance _owner;
	private int _attackRange = 36; // Melee range
	private boolean _follow = true;
	private boolean _previousFollowStatus = true;
	private int _maxLoad;
	
	private int _chargedSoulShot;
	private int _chargedSpiritShot;

	private final int _soulShotsPerHit = 1;
	private final int _spiritShotsPerHit = 1;
	protected boolean _showSummonAnimation;
	

	public L2Summon getSummon()
	{
		return L2Summon.this;
	}
		
	public boolean isAutoFollow()
	{
		return getFollowStatus();
	}

	public L2Summon(int objectId, L2NpcTemplate template, L2PcInstance owner)
	{
		super(objectId, template);
		getStat(); // init stats
		getStatus(); // init status

		_owner = owner;
		_ai = new L2SummonAI(this);
		
		//setXYZInvisible(owner.getX() + 30, owner.getY() + 30, owner.getZ());
		Formulas.addFuncsToNewSummon(this);
		setShowSummonAnimation(true);
	}
	
	@Override
	public SummonStat getStat()
	{
		if (super.getStat() == null || !(super.getStat() instanceof SummonStat))
			setStat(new SummonStat(this));

		return (SummonStat) super.getStat();
	}
	
	@Override
	public SummonStatus getStatus()
	{
		if (super.getStatus() == null || !(super.getStatus() instanceof SummonStatus))
			setStatus(new SummonStatus(this));
		
		return (SummonStatus) super.getStatus();
	}
	
	@Override
	public L2CharacterAI getAI()
	{
		if (_ai == null)
		{
			synchronized (this)
			{
				if (_ai == null)
					_ai = new L2SummonAI(this);
			}
		}
		
		return _ai;
	}
	
	@Override
	public L2NpcTemplate getTemplate()
	{
		return (L2NpcTemplate) super.getTemplate();
	}
	
	// this defines the action buttons, 1 for Summon, 2 for Pets
	public abstract int getSummonType();
	
	@Override
	public void updateAbnormalEffect(AbnormalEffect ae)
	{	
		if(ae != null && !ae.equals(AbnormalEffect.NULL))
		{
			for (L2PcInstance player : L2World.getInstance().getVisibleObjects(this, L2PcInstance.class))
				player.sendPacket(new SummonInfo(this, player, 1));
		}
	}
	
	public boolean isMountable()
	{
		return false;
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		
		setFollowStatus(true);
		
		if (Config.SHOW_NPC_CREST)
			sendPacket(new SummonInfo(this, getOwner(), 0));

		sendPacket(new RelationChanged(this, getOwner().getRelation(getOwner()), false));
		L2World.getInstance().forEachVisibleObject(getOwner(), L2PcInstance.class, player ->
		{
			if (isVisible())
				player.sendPacket(new RelationChanged(this, getOwner().getRelation(player), isAutoAttackable(player)));
		});
		
		final L2Party party = getOwner().getParty();
		if (party != null)
			party.broadcastToPartyMembers(getOwner(), new PetStatusUpdate(this));
		
		setShowSummonAnimation(false); 
		rechargeShots(true, true);
	}
	
	@Override
	public void onAction(L2PcInstance player)
	{
		if (player == _owner && player.getTarget() == this)
		{
			player.sendPacket(new PetStatusShow(this));
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else if (player.getTarget() != this)
		{
			player.setTarget(this);
			MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
			player.sendPacket(my);
			
			// sends HP/MP status of the summon to other characters
			StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
			su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
			su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
			su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
			player.sendPacket(su);
		}
		else if (player.getTarget() == this)
		{
			if (isAutoAttackable(player))
			{
				if (Config.GEODATA && GeoEngine.canSeeTarget(player, this, player.isFlying()))
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
				else
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
			}
			else
			{
				if (Config.GEODATA && GeoEngine.canSeeTarget(player, this, player.isFlying()))
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
				else
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
			}
			
	        if(player.isSpawnProtected())
	        	player.onActionRequest();
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public long getExpForThisLevel()
	{
		if (getLevel() >= Experience.MAX_LEVEL)
			return 0;
		return Experience.LEVEL[getLevel()];
	}
	
	public long getExpForNextLevel()
	{
		if (getLevel() >= (Experience.MAX_LEVEL - 1))
			return 0;
		return Experience.LEVEL[getLevel() + 1];
	}
	
	public final L2PcInstance getOwner()
	{		
		return _owner;
	}
	
	public final int getNpcId()
	{
		return getTemplate().npcId;
	}
	
	public final int getMaxLoad()
	{
		return _maxLoad;
	}
	
	public final int getSoulShotsPerHit()
	{
		return _soulShotsPerHit;
	}
	
	public final int getSpiritShotsPerHit()
	{
		return _spiritShotsPerHit;
	}
	
	public void setMaxLoad(int maxLoad)
	{
		_maxLoad = maxLoad;
	}
	
	public void setChargedSoulShot(int shotType)
	{
		_chargedSoulShot = shotType;
	}
	
	public void setChargedSpiritShot(int shotType)
	{
		_chargedSpiritShot = shotType;
	}
	
	public void followOwner()
	{
		setFollowStatus(true);
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;
		DecayTaskManager.getInstance().addDecayTask(this);
		return true;
	}
	
	public boolean doDie(L2Character killer, boolean decayed)
	{
		if (!super.doDie(killer))
			return false;
		if (!decayed)
			DecayTaskManager.getInstance().addDecayTask(this);
		return true;
	}
	
	public void stopDecay()
	{
		DecayTaskManager.getInstance().cancelDecayTask(this);
	}
	
	@Override
	public void onDecay()
	{
		deleteMe(_owner);
	}
	
	public void updateAndBroadcastStatus(int val)
	{
		if (getOwner() == null)
			return;
		
		sendPacket(new PetInfo(this, val));
		sendPacket(new PetStatusUpdate(this));
		updateEffectIcons(true);
		
		if (isVisible())
			broadcastNpcInfo(val);

		final L2Party party = getOwner().getParty();
		if (party != null)
			party.broadcastToPartyMembers(getOwner(), new PetStatusUpdate(this));
		
	}
	
	public void broadcastNpcInfo(int val)
	{
		L2World.getInstance().forEachVisibleObject(this, L2PcInstance.class, player ->
		{
			if ((player == getOwner()))
				return;

			player.sendPacket(new SummonInfo(this, player, val));
		});
	}
	
	@Override
	public void broadcastStatusUpdate()
	{
		super.broadcastStatusUpdate();
		updateAndBroadcastStatus(1);
	}
	
	public void deleteMe(L2PcInstance owner)
	{
		getAI().stopFollow();
		owner.sendPacket(new PetDelete(getSummonType(), getObjectId()));

		owner.setPet(null);
		
		decayMe();
		
		super.deleteMe();
	}
	
	public synchronized void unSummon(L2PcInstance owner)
	{
		if (isVisible() && !isDead())
		{
			abortAllAttacks();
			getAI().stopFollow();
			setTarget(null);
			
			stopHpMpRegeneration();
			stopAllEffects();
			store();
			
			owner.sendPacket(new PetDelete(getSummonType(), getObjectId()));

			final ZoneRegion oldRegion = ZoneManager.getInstance().getRegion(this);
			decayMe();
			oldRegion.removeFromZones(this);

			owner.setPet(null);

		}
	}
	
	public int getAttackRange()
	{
		return _attackRange;
	}
	
	public void setAttackRange(int range)
	{
		_attackRange = (range < 36) ? 36 : range;
	}
	
	public void setFollowStatus(boolean state)
	{
		_follow = state;
		
		if (_follow)
			getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, getOwner());
		else
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
	}
	
	public boolean getFollowStatus()
	{
		return _follow;
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return (_owner != null) && _owner.isAutoAttackable(attacker);
	}
	
	public int getChargedSoulShot()
	{
		return _chargedSoulShot;
	}
	
	public int getChargedSpiritShot()
	{
		return _chargedSpiritShot;
	}
	
	public int getControlItemId()
	{
		return 0;
	}
	
	public L2Weapon getActiveWeapon()
	{
		return null;
	}
	
	public PetInventory getInventory()
	{
		return null;
	}
	
	public void doPickupItem(L2Object object)
	{
		return;
	}
	
	public void giveAllToOwner()
	{
		return;
	}
	
	public void store()
	{
		return;
	}
	
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return null;
	}
	
	@Override
	public L2Weapon getActiveWeaponItem()
	{
		return null;
	}
	
	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}
	
	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		return null;
	}
	
	@Override
	public L2Party getParty()
	{
		if (_owner == null)
			return null;

		return _owner.getParty();
	}
	
	@Override
	public boolean isInParty()
	{
		if (_owner == null)
			return false;

		return _owner.getParty() != null;
	}
	
	@Override
	public boolean isOutOfControl()
	{
		return super.isOutOfControl() || isBetrayed();
	}
	
	@Override
	public boolean isInCombat()
	{
		return getOwner() != null ? getOwner().isInCombat() : false;
	}
	
	@Override
	public final boolean isAttackingNow()
	{
		return isInCombat();
	}
	
	@Override
	public L2PcInstance getActingPlayer()
	{
		return getOwner();
	}
	
	@Override
	public void sendPacket(L2GameServerPacket mov)
	{
		if (getOwner() != null)
			getOwner().sendPacket(mov);
	}
	
	@Override
	public int getKarma()
	{
		return (getOwner() != null) ? getOwner().getKarma() : 0;
	}
	
	@Override
	public final byte getPvpFlag()
	{
		return (getOwner() != null) ? getOwner().getPvpFlag() : 0;
	}
	
	public final int getTeam()
	{
		return (getOwner() != null) ? getOwner().getTeam().getId() : 0;
	}
	
	@Override
	public int getClanId()
	{
		return (getOwner() != null) ? getOwner().getClanId() : 0;
	}
	
	@Override
	public int getAllyId()
	{
		return (getOwner() != null) ? getOwner().getAllyId() : 0;
	}
	
	public void useMagic(L2Skill skill, boolean forceUse, boolean dontMove)
	{
		if (skill == null || isDead())
			return;
		
		// Check if the skill is active
		if (skill.isPassive())
			// just ignore the passive skill request. why does the client send it anyway ??
			return;
		
		// ************************************* Check Casting in Progress *******************************************
		
		// If a skill is currently being used
		if (isCastingNow())
			return;
		
		getOwner().setCurrentPetSkill(skill, forceUse, dontMove);
		
		// ************************************* Check Target *******************************************
		
		// Get the target for the skill
		L2Object target = null;
		
		switch (skill.getTargetType())
		{
		// OWNER_PET should be cast even if no target has been found
			case TARGET_OWNER_PET:
				target = getOwner();
				break;
			// PARTY, AURA, SELF should be cast even if no target has been found
			case TARGET_PARTY:
			case TARGET_AURA:
			case TARGET_SELF:
				target = this;
				break;
			default:
				// Get the first target of the list
				target = skill.getFirstOfTargetList(this);
				break;
		}
		
		// Check the validity of the target
		if (target == null)
		{
			if (getOwner() != null)
			{
				getOwner().sendPacket(SystemMessageId.TARGET_CANT_FOUND);
			}
			return;
		}
		
		// ************************************* Check skill availability *******************************************
		
		// Check if this skill is enabled (ex : reuse time)
		if (isSkillDisabled(skill.getId()) && getOwner() != null && (getOwner().getAccessLevel().allowPeaceAttack()))
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE);
			sm.addString(skill.getName());
			getOwner().sendPacket(sm);
			return;
		}
		
		// Check if all skills are disabled
		if (isAllSkillsDisabled() && getOwner() != null && (getOwner().getAccessLevel().allowPeaceAttack()))
			return;
		
		// ************************************* Check Consumables *******************************************
		
		// Check if the summon has enough MP
		if (getCurrentMp() < getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill))
		{
			// Send a System Message to the caster
			if (getOwner() != null)
			{
				getOwner().sendPacket(SystemMessageId.NOT_ENOUGH_MP);
			}
			return;
		}
		
		// Check if the summon has enough HP
		if (getCurrentHp() <= skill.getHpConsume())
		{
			// Send a System Message to the caster
			if (getOwner() != null)
			{
				getOwner().sendPacket(SystemMessageId.NOT_ENOUGH_HP);
			}
			return;
		}
		
		// ************************************* Check Summon State *******************************************
		
		// Check if this is offensive magic skill
		if (skill.isOffensive())
		{
			
			if (target.getObjectId() == getOwner().getObjectId())
				return;
			
			if (isInsidePeaceZone(this, target) && getOwner() != null && (getOwner().getAccessLevel().allowPeaceAttack()))
			{
				// If summon or target is in a peace zone, send a system message TARGET_IN_PEACEZONE
				getOwner().sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
				return;
			}
			
			if (isInFunEvent() || target.isInFunEvent())
			{
				// If summon or target is in a peace zone, send a system message TARGET_IN_PEACEZONE
				getOwner().sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
				return;
			}
			
			if (getOwner() != null && getOwner().isInOlympiadMode() && !getOwner().isOlympiadStart())
			{
				// if L2PcInstance is in Olympia and the match isn't already start, send a Server->Client packet ActionFailed
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			// Check if the target is attackable
			if (target instanceof L2DoorInstance)
			{
				if (!((L2DoorInstance) target).isAttackable(getOwner()))
					return;
			}
			else
			{
				if (!target.isAttackable() && getOwner() != null && (getOwner().getAccessLevel().allowPeaceAttack()))
					return;
				
				// Check if a Forced ATTACK is in progress on non-attackable target
				if (!target.isAutoAttackable(this) && !forceUse && skill.getTargetType() != L2SkillTargetType.TARGET_AURA && skill.getTargetType() != L2SkillTargetType.TARGET_CLAN && skill.getTargetType() != L2SkillTargetType.TARGET_ALLY && skill.getTargetType() != L2SkillTargetType.TARGET_PARTY && skill.getTargetType() != L2SkillTargetType.TARGET_SELF)
					return;
			}
		}
		
		// Notify the AI with AI_INTENTION_CAST and target
		getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
	}
	
	@Override
	public void setIsImmobilized(boolean value)
	{
		super.setIsImmobilized(value);
		
		if (value)
		{
			_previousFollowStatus = getFollowStatus();
			
			if (_previousFollowStatus)
				setFollowStatus(false);
		}
		else
			setFollowStatus(_previousFollowStatus);
	}
	
	public void setOwner(L2PcInstance newOwner)
	{
		_owner = newOwner;
	}
	
	@Override
	public boolean isShowSummonAnimation()
	{
		return _showSummonAnimation;
	}
	
	@Override
	public void setShowSummonAnimation(boolean showSummonAnimation)
	{
		_showSummonAnimation = showSummonAnimation;
	}
	
	public int getWeapon()
	{
		return 0;
	}
	
	public int getArmor()
	{
		return 0;
	}
	
	@Override
	protected void onHitTimer(L2Character target, int damage, boolean crit, boolean miss, boolean soulshot, byte shld)
	{
		super.onHitTimer(target, damage, crit, miss, soulshot, shld);
		rechargeShots(true,false);
	}
	
	@Override
	public void rechargeShots(boolean physical, boolean magic)
	{
		if (getOwner().getAutoSoulShot() == null || getOwner().getAutoSoulShot().isEmpty())
			return;
		
		for (int itemId : getOwner().getAutoSoulShot())
		{
			L2ItemInstance item = getOwner().getInventory().getItemByItemId(itemId);
			if (item != null)
			{
				// Check if Soulshot is already active
				if (item.getChargedSoulshot() != L2ItemInstance.CHARGED_NONE)
					continue;
				
				if (magic && itemId == 6646 || itemId == 6647)
				{
					final IItemHandler handler = ItemHandler.getInstance().getHandler(itemId);
					
					if (handler != null)
						handler.useItem(getOwner(), item);
				}
				
				if (physical && itemId == 6645)
				{
					final IItemHandler handler = ItemHandler.getInstance().getHandler(itemId);
					
					if (handler != null)
						handler.useItem(this, item);
				}
			}
			else
				getOwner().removeAutoSoulShot(itemId);
		}
	}

	@Override
	public void doCast(L2Skill skill)
	{
		int petLevel = getLevel();
		int skillLevel = petLevel / 10;
		if (petLevel >= 70)
		{
			skillLevel += (petLevel - 65) / 10;
		}
		
		// adjust the level for servitors less than lv 10
		if (skillLevel < 1)
		{
			skillLevel = 1;
		}
		
		L2Skill skillToCast = SkillTable.getInstance().getInfo(skill.getId(), skillLevel);
		
		if (skillToCast != null)
		{
			super.doCast(skillToCast);
		}
		else
		{
			super.doCast(skill);
		}
	}

	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		// Check if the Player is the owner of the Pet
		if (activeChar == getOwner())
		{
			activeChar.sendPacket(new PetInfo(this, 0));
			
			// The PetInfo packet wipes the PartySpelled (list of active spells' icons). Re-add them
			updateEffectIcons(true);
			
			if (this instanceof L2PetInstance)
				activeChar.sendPacket(new PetItemList((L2PetInstance) this));
		}
		else
		    activeChar.sendPacket(new SummonInfo(this, activeChar, 0));
	}
	
	@Override
	public void setXYZ(int x, int y, int z)
	{	
		super.setXYZ(x, y, z);		
	}
}