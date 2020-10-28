package com.l2jhellas.gameserver.model.actor;

import static com.l2jhellas.gameserver.ai.CtrlIntention.AI_INTENTION_FOLLOW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.ai.CtrlEvent;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.ai.L2AttackableAI;
import com.l2jhellas.gameserver.ai.L2CharacterAI;
import com.l2jhellas.gameserver.controllers.GameTimeController;
import com.l2jhellas.gameserver.datatables.xml.MapRegionTable;
import com.l2jhellas.gameserver.datatables.xml.MapRegionTable.TeleportWhereType;
import com.l2jhellas.gameserver.enums.ZoneId;
import com.l2jhellas.gameserver.enums.items.L2CrystalType;
import com.l2jhellas.gameserver.enums.items.L2WeaponType;
import com.l2jhellas.gameserver.enums.player.DuelState;
import com.l2jhellas.gameserver.enums.player.Position;
import com.l2jhellas.gameserver.enums.skills.AbnormalEffect;
import com.l2jhellas.gameserver.enums.skills.FrequentSkill;
import com.l2jhellas.gameserver.enums.skills.L2SkillTargetType;
import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.geodata.GeoEngine;
import com.l2jhellas.gameserver.geodata.GeoMove;
import com.l2jhellas.gameserver.geometry.Point3D;
import com.l2jhellas.gameserver.handler.ISkillHandler;
import com.l2jhellas.gameserver.handler.SkillHandler;
import com.l2jhellas.gameserver.instancemanager.CastleManager;
import com.l2jhellas.gameserver.instancemanager.DimensionalRiftManager;
import com.l2jhellas.gameserver.instancemanager.SiegeManager;
import com.l2jhellas.gameserver.instancemanager.ZoneManager;
import com.l2jhellas.gameserver.model.ChanceSkillList;
import com.l2jhellas.gameserver.model.CharEffectList;
import com.l2jhellas.gameserver.model.ForceBuff;
import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.L2WorldRegion;
import com.l2jhellas.gameserver.model.actor.group.party.L2Party;
import com.l2jhellas.gameserver.model.actor.instance.L2ArtefactInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2GuardInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2MinionInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance.SkillDat;
import com.l2jhellas.gameserver.model.actor.instance.L2PetInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2RiftInvaderInstance;
import com.l2jhellas.gameserver.model.actor.item.Inventory;
import com.l2jhellas.gameserver.model.actor.item.L2Item;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.model.actor.stat.CharStat;
import com.l2jhellas.gameserver.model.actor.status.CharStatus;
import com.l2jhellas.gameserver.model.entity.Castle;
import com.l2jhellas.gameserver.model.entity.events.engines.EventManager;
import com.l2jhellas.gameserver.model.entity.olympiad.OlympiadGameManager;
import com.l2jhellas.gameserver.model.entity.olympiad.OlympiadGameTask;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestEventType;
import com.l2jhellas.gameserver.model.quest.QuestState;
import com.l2jhellas.gameserver.model.zone.ZoneRegion;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.AbstractNpcInfo.NpcInfo;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.Attack;
import com.l2jhellas.gameserver.network.serverpackets.ChangeMoveType;
import com.l2jhellas.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jhellas.gameserver.network.serverpackets.MagicSkillCanceld;
import com.l2jhellas.gameserver.network.serverpackets.MagicSkillLaunched;
import com.l2jhellas.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jhellas.gameserver.network.serverpackets.MoveToLocation;
import com.l2jhellas.gameserver.network.serverpackets.Revive;
import com.l2jhellas.gameserver.network.serverpackets.ServerObjectInfo;
import com.l2jhellas.gameserver.network.serverpackets.SetupGauge;
import com.l2jhellas.gameserver.network.serverpackets.StatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.StopMove;
import com.l2jhellas.gameserver.network.serverpackets.StopRotation;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.network.serverpackets.TargetUnselected;
import com.l2jhellas.gameserver.network.serverpackets.TeleportToLocation;
import com.l2jhellas.gameserver.skills.Calculator;
import com.l2jhellas.gameserver.skills.Formulas;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.gameserver.skills.Stats;
import com.l2jhellas.gameserver.skills.effects.EffectCharge;
import com.l2jhellas.gameserver.skills.funcs.Func;
import com.l2jhellas.gameserver.taskmanager.AttackStanceTaskManager;
import com.l2jhellas.gameserver.templates.L2Armor;
import com.l2jhellas.gameserver.templates.L2CharTemplate;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;
import com.l2jhellas.gameserver.templates.L2Weapon;
import com.l2jhellas.shield.antiflood.FloodProtectors;
import com.l2jhellas.shield.antiflood.FloodProtectors.Action;
import com.l2jhellas.util.Broadcast;
import com.l2jhellas.util.MathUtil;
import com.l2jhellas.util.Rnd;
import com.l2jhellas.util.Util;

import Extensions.fake.roboto.FakePlayer;

public abstract class L2Character extends L2Object
{
	public static final Logger _log = Logger.getLogger(L2Character.class.getName());
	
	private List<L2Character> _attackByList;
	// private L2Character _attackingChar;
	private L2Skill _lastSkillCast;
	private boolean _isAfraid = false; // Flee in a random direction
	private boolean _isConfused = false; // Attack anyone randomly
	private boolean _isFakeDeath = false; // Fake death
	private boolean _isFlying = false; // Is flying Wyvern?
	private boolean _isRaid = false;
	private boolean _isMuted = false; // Cannot use magic
	private boolean _isPsychicalMuted = false; // Cannot use psychical skills
	private boolean _isKilledAlready = false;
	private boolean _isImmobilized = false;
	private boolean _isOverloaded = false; // the char is carrying too much
	private boolean _isParalyzed = false;
	private boolean _isRiding = false; // Is Riding strider?
	private boolean _isPendingRevive = false;
	private boolean _isRooted = false; // Cannot move until root timed out
	private boolean _isRunning = false;
	private boolean _isImmobileUntilAttacked = false; // Is in immobile until attacked.
	private boolean _isSleeping = false; // Cannot move/attack until sleep timed out or monster is attacked
	private boolean _isStunned = false; // Cannot move/attack until stun timed out
	private boolean _isBetrayed = false; // Betrayed by own summon
	protected boolean _showSummonAnimation = false;
	protected boolean _isTeleporting = false;
	private L2Character _lastBuffer = null;
	protected boolean _isInvul = false;
	private int _lastHealAmount = 0;
	private CharStat _stat;
	private CharStatus _status;
	private L2CharTemplate _template; // The link on the L2CharTemplate object containing generic and static properties of this L2Character type (ex : Max HP, Speed...)
	private String _title;
	private String _aiClass = "default";
	private double _hpUpdateIncCheck = .0;
	private double _hpUpdateDecCheck = .0;
	private double _hpUpdateInterval = .0;
	private boolean _champion = false;
	
	private Calculator[] _calculators;
	
	protected Map<Integer, L2Skill> _skills = new LinkedHashMap<>();
	
	protected ChanceSkillList _chanceSkills;
	
	private int _PremiumService;
	
	public boolean _enemy;
	
	private final byte[] _zones = new byte[ZoneId.getZoneCount()];
	protected byte _zoneValidateCounter = 4;
	
	@Override
	public boolean isInsideZone(ZoneId zone)
	{
		return zone == ZoneId.PVP ? _zones[ZoneId.PVP.getId()] > 0 && _zones[ZoneId.PEACE.getId()] == 0 : _zones[zone.getId()] > 0;
	}
	
	public void setInsideZone(ZoneId zone, boolean state)
	{
		if (state)
			_zones[zone.getId()]++;
		else
		{
			_zones[zone.getId()]--;
			if (_zones[zone.getId()] < 0)
				_zones[zone.getId()] = 0;
		}
	}
	
	public L2Character(int objectId, L2CharTemplate template)
	{
		super(objectId);
		
		// Set its template to the new L2Character
		_template = template;
		
		if (template != null && this instanceof L2Npc)
		{
			// Copy the Standard Calculators of the L2NPCInstance in _calculators
			_calculators = NPC_STD_CALCULATOR;
			
			// Copy the skills of the L2NPCInstance from its template to the L2Character Instance
			// The skills list can be affected by spell effects so it's necessary to make a copy
			// to avoid that a spell affecting a L2NPCInstance, affects others L2NPCInstance of the same type too.
			_skills = ((L2NpcTemplate) template).getSkills();
			if (_skills != null)
			{
				for (Map.Entry<Integer, L2Skill> skill : _skills.entrySet())
					addStatFuncs(skill.getValue().getStatFuncs(null, this));
			}
			if (!Config.NPCS_ATTACKABLE && !(this instanceof L2Attackable))
			{			
				setIsInvul(true);				
			}
		}
		else
		{
			// Initialize the FastMap _skills to null
			_skills = new LinkedHashMap<>();	
			// If L2Character is a L2PcInstance or a L2Summon, create the basic calculator set
			_calculators = new Calculator[Stats.NUM_STATS];
		}
	}
	
	protected void initCharStatusUpdateValues()
	{
		_hpUpdateInterval = getMaxHp() / 352.0; // MAX_HP div MAX_HP_BAR_PX
		_hpUpdateIncCheck = getMaxHp();
		_hpUpdateDecCheck = getMaxHp() - _hpUpdateInterval;
	}
	
	public void onDecay()
	{
		decayMe();
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		revalidateZone(true);
	}
	
	public void onTeleported()
	{
		if (!isTeleporting())
			return;
		
		spawnMe(getX(), getY(), getZ());
		
		setIsTeleporting(false);
		
		if (_isPendingRevive)
			doRevive();		
	}
	
	public void addAttackerToAttackByList(L2Character player)
	{
		if (player == null || player == this || getAttackByList() == null || getAttackByList().contains(player))
			return;
		getAttackByList().add(player);
	}

	protected boolean needHpUpdate(int barPixels)
	{
		double currentHp = getCurrentHp();
		
		if (currentHp <= 1.0 || getMaxHp() < barPixels)
			return true;
		
		if (currentHp <= _hpUpdateDecCheck || currentHp >= _hpUpdateIncCheck)
		{
			if (currentHp == getMaxHp())
			{
				_hpUpdateIncCheck = currentHp + 1;
				_hpUpdateDecCheck = currentHp - _hpUpdateInterval;
			}
			else
			{
				double doubleMulti = currentHp / _hpUpdateInterval;
				int intMulti = (int) doubleMulti;
				
				_hpUpdateDecCheck = _hpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
				_hpUpdateIncCheck = _hpUpdateDecCheck + _hpUpdateInterval;
			}
			
			return true;
		}
		
		return false;
	}
	
	public void broadcastStatusUpdate()
	{
		if (getStatus().getStatusListener().isEmpty())
			return;
		
		if (!needHpUpdate(352))
			return;
		
		// Create the Server->Client packet StatusUpdate with current HP and MP
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
		su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
		
		// Go through the StatusListener
		// Send the Server->Client packet StatusUpdate with current HP and MP
		for (L2Character temp : getStatus().getStatusListener())
		{
			if(temp != null)
				temp.sendPacket(su);
		}
	}
	
	public void sendPacket(L2GameServerPacket mov)
	{
		// default implementation
	}
	
	public void teleToLocation(int x, int y, int z, boolean allowRandomOffset)
	{
		if(this instanceof L2PcInstance)
		{
			final L2PcInstance player = (L2PcInstance) this;
			if(player.getActiveTradeList()!=null)
				player.cancelActiveTrade();
		}
		// Stop movement
		stopMove(null);
		abortAllAttacks();
		
		setIsTeleporting(true);
		setTarget(null);
		
		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		
		// Remove from world regions zones
		ZoneManager.getInstance().getRegion(this).removeFromZones(this);
		
		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		
		if (Config.RESPAWN_RANDOM_ENABLED && allowRandomOffset)
		{
			x += Rnd.get(-Config.RESPAWN_RANDOM_MAX_OFFSET, Config.RESPAWN_RANDOM_MAX_OFFSET);
			y += Rnd.get(-Config.RESPAWN_RANDOM_MAX_OFFSET, Config.RESPAWN_RANDOM_MAX_OFFSET);
		}
		
		z += 5;
		
		// Send a Server->Client packet TeleportToLocationt to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character
		broadcastPacket(new TeleportToLocation(this, x, y, z, getHeading()));	
		
		final L2WorldRegion reg = getWorldRegion();
		setIsVisible(false);	
		L2World.removeVisibleObject(this, reg);
		L2World.getInstance().removeTeleObject(this);
		
		// Set the x,y,z position of the L2Object and if necessary modify its _worldRegion
		setXYZ(x, y, z);
		
		if (!(this instanceof L2PcInstance) || (((L2PcInstance) this).getClient() != null && ((L2PcInstance) this).getClient().isDetached()))
			onTeleported();
		
		revalidateZone(true);
	}
	
	public void teleToLocation(int x, int y, int z)
	{
		teleToLocation(x, y, z, false);
	}
	
	public void teleToLocation(Location loc, boolean allowRandomOffset)
	{
		int x = loc.getX();
		int y = loc.getY();
		int z = loc.getZ();
		
		if (this instanceof L2PcInstance && DimensionalRiftManager.getInstance().checkIfInRiftZone(getX(), getY(), getZ(), true)) // true -> ignore waiting room :)
		{
			L2PcInstance player = (L2PcInstance) this;
			player.sendMessage("You have been sent to the waiting room.");
			if (player.isInParty() && player.getParty().isInDimensionalRift())
			{
				player.getParty().getDimensionalRift().usedTeleport(player);
			}
			int[] newCoords = DimensionalRiftManager.getInstance().getRoom((byte) 0, (byte) 0).getTeleportCoords();
			x = newCoords[0];
			y = newCoords[1];
			z = newCoords[2];
		}
		teleToLocation(x, y, z, allowRandomOffset);
	}
	
	public void teleToLocation(TeleportWhereType teleportWhere)
	{
		teleToLocation(MapRegionTable.getInstance().getTeleToLocation(this, teleportWhere), true);
	}
	
	public void doAttack(L2Character target,boolean stopMov)
	{			
		if (isAlikeDead() || target == null || !target.isVisible() || (this instanceof L2Npc && target.isAlikeDead()) || !isInSurroundingRegion(target) || (this instanceof L2PcInstance && target.isDead() && !target.isFakeDeath()) || (this instanceof L2PcInstance && isDead()) || (target instanceof L2PcInstance && ((L2PcInstance) target).getDuelState() == DuelState.DEAD))
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (isInsidePeaceZone(this, target))
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (!GeoEngine.canSeeTarget(this, target))
		{
			sendPacket(new SystemMessage(SystemMessageId.CANT_SEE_TARGET));
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (this instanceof L2Summon && target.getObjectId() == ((L2Summon) this).getOwner().getObjectId())
		{
			((L2Summon) this).getOwner().sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		
		if (isAttackingDisabled())
			return;

		if (target instanceof L2DoorInstance && !((L2DoorInstance) target).isAttackable(this))
			return;
		
		final L2PcInstance player = getActingPlayer();
		
		if (player != null)
		{		
			if (player.inObserverMode())
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE));
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			final L2PcInstance TargetPlayer = target.getActingPlayer();

			if (TargetPlayer != null)
			{
				if(TargetPlayer.getAppearance().getInvisible())
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				
				if(player.isInFunEvent() && TargetPlayer.isInFunEvent() && !EventManager.getInstance().canAttack(player,TargetPlayer))
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				
				final OlympiadGameTask OlyTask = OlympiadGameManager.getInstance().getOlympiadTask(player.getOlympiadGameId());
				if (player.isInOlympiadMode() && player.isOlympiadStart() && OlyTask != null && !OlyTask.isGameStarted())
				{
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}

				if (TargetPlayer.isCursedWeaponEquiped() && player.getLevel() <= 20)
				{
					player.sendMessage("Can't attack a cursed player when under level 21.");
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				
				if (player.isCursedWeaponEquiped() && TargetPlayer.getLevel() <= 20)
				{
					player.sendMessage("Can't attack a newbie player using a cursed weapon.");
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				if (player.getObjectId() == TargetPlayer.getObjectId())
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
		}
		
		if(player != null && stopMov)
			player.stopMove(null);
		
		// Get the active weapon instance (always equipped in the right hand)
		L2ItemInstance weaponInst = getActiveWeaponInstance();
		
		// Get the active weapon item corresponding to the active weapon instance (always equipped in the right hand)
		L2Weapon weaponItem = getActiveWeaponItem();
		final L2WeaponType weaponType = getAttackType();
		
		if (weaponType == L2WeaponType.ROD)
		{
			// You can't make an attack with a fishing pole.
			((L2PcInstance) this).sendPacket(SystemMessageId.CANNOT_ATTACK_WITH_FISHING_POLE);
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);			
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if ((target instanceof L2GrandBossInstance) && ((L2GrandBossInstance) target).getNpcId() == 29022)
		{
			if (Math.abs(getClientZ() - target.getZ()) > 200)
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_SEE_TARGET));
				getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		// Check for a bow
		if ((weaponType == L2WeaponType.BOW))
		{
			// Check for arrows and MP
			if (player != null)
			{
				// Checking if target has moved to peace zone - only for player-bow attacks at the moment
				// Other melee is checked in movement code and for offensive spells a check is done every time
				if (player.isInsidePeaceZone(player, target))
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				
				// Verify if the bow can be use
				if (_disableBowAttackEndTime <= GameTimeController.getInstance().getGameTicks())
				{
					// Verify if L2PcInstance owns enough MP
					int saMpConsume = (int) player.getStat().calcStat(Stats.MP_CONSUME, 0, null, null);
					int mpConsume = saMpConsume == 0 ? weaponItem.getMpConsume() : saMpConsume;
					
					if (player.getCurrentMp() < mpConsume)
					{
						ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000);						
						player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_MP));
						player.sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
					// If L2PcInstance have enough MP, the bow consumes it
					player.getStatus().reduceMp(mpConsume);
					
					// Set the period of bow non re-use
					_disableBowAttackEndTime = 5 * GameTimeController.TICKS_PER_SECOND + GameTimeController.getInstance().getGameTicks();
				}
				else
				{
					// Cancel the action because the bow can't be re-use at this
					// moment
					ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000);
					
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				
				// Equip arrows needed in left hand and send a Server->Client packet ItemList to the L2PcINstance then return True
				if (!player.checkAndEquipArrows())
				{
					// Cancel the action because the L2PcInstance have no arrow
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);					
					player.sendPacket(ActionFailed.STATIC_PACKET);
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ARROWS));
					return;
				}
			}
			else if (this instanceof L2Npc)
			{
				if (_disableBowAttackEndTime > GameTimeController.getInstance().getGameTicks())
					return;
			}
		}
		
		// Reduce the current CP if TIREDNESS configuration is activated
		if (Config.ALT_GAME_TIREDNESS)
			setCurrentCp(getCurrentCp() - 10);
		
		// Verify if soulshots are charged.
		boolean wasSSCharged;

		if (this instanceof L2Summon && !(this instanceof L2PetInstance))
			wasSSCharged = (((L2Summon) this).getChargedSoulShot() != L2ItemInstance.CHARGED_NONE);
		else
			wasSSCharged = (weaponInst != null && weaponInst.getChargedSoulshot() != L2ItemInstance.CHARGED_NONE);
		
		final int timeAtk = Formulas.calculateTimeBetweenAttacks(getPAtkSpd());

		_attackEndTime = GameTimeController.getInstance().getGameTicks();
		_attackEndTime += (timeAtk / GameTimeController.MILLIS_IN_TICK);
		_attackEndTime -= 1;
		
		L2CrystalType ssGrade = L2CrystalType.NONE;
		
		if (weaponItem != null)
			ssGrade = weaponItem.getCrystalType();
		
		// Create a Server->Client packet Attack
		Attack attack = new Attack(this, target, wasSSCharged, ssGrade.getId());
		
		boolean hitted;
		
		// Set the Attacking Body part to CHEST
		setAttackingBodypart();
		
		// Sets heading to target
		setHeading(Util.calculateHeadingFrom(this, target));

		// Get the Attack Reuse Delay of the L2Weapon
		int reuse = calculateReuseTime(target, weaponItem);
		
		// Select the type of attack to start
		switch (weaponType)
		{
			case BOW:
			{
				hitted = doAttackHitByBow(attack, target, timeAtk, reuse);
				break;
			}
			case POLE:
			{
				hitted = doAttackHitByPole(attack, target,(timeAtk/2));
				break;
			}
			case DUAL:
			case DUALFIST:
			{
				hitted = doAttackHitByDual(attack, target,(timeAtk/2));
				break;
			}
			case FIST:
			{
				if (getSecondaryWeaponItem() != null && getSecondaryWeaponItem() instanceof L2Armor)
					hitted = doAttackHitSimple(attack, target, timeAtk/2);
				else
					hitted = doAttackHitByDual(attack, target, timeAtk/2);
				break;
			}
			default:
			{
				hitted = doAttackHitSimple(attack, target,timeAtk/2);
			}
		}		

		getAI().clientStartAutoAttack();

		if (this instanceof L2Summon)
		((L2Summon) this).getOwner().getAI().clientStartAutoAttack();

		if (player != null && player.getPet() != target)
			player.updatePvPStatus(target);	
		
		// Check if hit isn't missed
		if (!hitted)
			abortAttack();
		else
		{
			if (player != null)
			{				
				if (player.isCursedWeaponEquiped())
				{
					// If hitted by a cursed weapon, Cp is reduced to 0
					if (!target.isInvul())
						target.setCurrentCp(0);
				}
				else if (player.isHero())
				{
					if (target.isPlayer() && ((L2PcInstance) target).isCursedWeaponEquiped())
						target.setCurrentCp(0);
				}

			}
			
			if (this instanceof L2Summon && !(this instanceof L2PetInstance))
				((L2Summon) this).setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
			else if (weaponInst != null)
				weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
		}
		
		if (attack.hasHits())
			broadcastPacket(attack);
		
		// l2off like. you give only one hit if the target is not autoattackable
		if (player!=null && (target instanceof L2PcInstance) && !target.isAutoAttackable(player))
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, this);		
		
		// Notify AI with EVT_READY_TO_ACT
		ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT),timeAtk + reuse);
	}
	
	private boolean doAttackHitByBow(Attack attack, L2Character target, int sAtk, int reuse)
	{
		int damage1 = 0;
		byte shld1 = 0;
		boolean crit1 = false;
		
		// Calculate if hit is missed or not
		boolean miss1 = Formulas.calcHitMiss(this, target);
		
		// Consume arrows
		reduceArrowCount();
		
		_move = null;
		
		// Check if hit isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient
			shld1 = Formulas.calcShldUse(this, target);
			
			// Calculate if hit is critical
			crit1 = Formulas.calcCrit(getStat().getCriticalHit(target, null));
			
			// Calculate physical damages
			damage1 = (int) Formulas.calcPhysDam(this, target, null, shld1, crit1, false, attack.hasSoulshot());
		}
		
		// Check if the L2Character is a L2PcInstance
		if (this instanceof L2PcInstance)
		{

			// Send a system message
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.GETTING_READY_TO_SHOOT_AN_ARROW));
			
			// Send a Server->Client packet SetupGauge
			SetupGauge sg = new SetupGauge(SetupGauge.RED, sAtk + reuse);
			sendPacket(sg);
		}
		
		// Create a new hit task with Medium priority
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack.hasSoulshot(), shld1), sAtk);
		
		// Calculate and set the disable delay of the bow in function of the Attack Speed
		_disableBowAttackEndTime = (sAtk + reuse) / GameTimeController.MILLIS_IN_TICK + GameTimeController.getInstance().getGameTicks();
		
		// Add this hit to the Server-Client packet Attack
		attack.addHit(target, damage1, miss1, crit1, shld1);
		
		// Return true if hit isn't missed
		return !miss1;
	}
	
	private boolean doAttackHitByDual(Attack attack, L2Character target, int sAtk)
	{
		int damage1 = 0;
		int damage2 = 0;
		byte shld1 = 0;
		byte shld2 = 0;
		boolean crit1 = false;
		boolean crit2 = false;
		
		// Calculate if hits are missed or not
		boolean miss1 = Formulas.calcHitMiss(this, target);
		boolean miss2 = Formulas.calcHitMiss(this, target);
		
		// Check if hit 1 isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient against hit 1
			shld1 = Formulas.calcShldUse(this, target);
			
			// Calculate if hit 1 is critical
			crit1 = Formulas.calcCrit(getStat().getCriticalHit(target, null));
			
			// Calculate physical damages of hit 1
			damage1 = (int) Formulas.calcPhysDam(this, target, null, shld1, crit1, true, attack.hasSoulshot());
			damage1 /= 2;
		}
		
		// Check if hit 2 isn't missed
		if (!miss2)
		{
			// Calculate if shield defense is efficient against hit 2
			shld2 = Formulas.calcShldUse(this, target);
			
			// Calculate if hit 2 is critical
			crit2 = Formulas.calcCrit(getStat().getCriticalHit(target, null));
			
			// Calculate physical damages of hit 2
			damage2 = (int) Formulas.calcPhysDam(this, target, null, shld2, crit2, true, attack.hasSoulshot());
			damage2 /= 2;
		}
		
		// Create a new hit task with Medium priority for hit 1
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack.hasSoulshot(), shld1), sAtk / 2);
		
		// Create a new hit task with Medium priority for hit 2 with a higher delay
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage2, crit2, miss2, attack.hasSoulshot(), shld2), sAtk);
		
		// Add those hits to the Server-Client packet Attack
		attack.addHit(target, damage1, miss1, crit1, shld1);
		attack.addHit(target, damage2, miss2, crit2, shld2);
		
		// Return true if hit 1 or hit 2 isn't missed
		return (!miss1 || !miss2);
	}
	
	private boolean doAttackHitByPole(Attack attack, L2Character target, int sAtk)
	{
		int maxRadius = getPhysicalAttackRange();
		int maxAngleDiff = (int) getStat().calcStat(Stats.POWER_ATTACK_ANGLE, 120, null, null);
		
		int attackRandomCountMax = (int) getStat().calcStat(Stats.ATTACK_COUNT_MAX, 0, null, null) - 1;
		int attackcount = 0;
		
		boolean hitted = doAttackHitSimple(attack, target, 100, sAtk);
		double attackpercent = 85;
		
		for (L2Character obj : L2World.getInstance().getVisibleObjects(this, L2Character.class))
		{
			if (obj == target || obj.isAlikeDead())
				continue;
			
			if (isPlayer())
			{
				if (obj instanceof L2PetInstance &&  ((L2PetInstance) obj).getOwner() == ((L2PcInstance) this))
					continue;
			}
			else if (this instanceof L2Attackable)
			{
				if (obj.isPlayer() && getTarget() instanceof L2Attackable)
					continue;
				
				if (obj instanceof L2Attackable && !isConfused())
					continue;
			}
			
			if (!MathUtil.checkIfInRange(maxRadius, this, obj, false))
				continue;
			
			if (Math.abs(obj.getZ() - getZ()) > 650)
				continue;

			if (!isFacing(obj, maxAngleDiff))
				continue;
			
			if (obj == getAI().getTarget() || obj.isAutoAttackable(this))
			{
				attackcount++;
				if (attackcount > attackRandomCountMax)
					break;
				
				hitted |= doAttackHitSimple(attack, obj, attackpercent, sAtk);
				attackpercent /= 1.15;
			}
		}
		return hitted;
	}
	
	private boolean doAttackHitSimple(Attack attack, L2Character target, int sAtk)
	{
		return doAttackHitSimple(attack, target, 100, sAtk);
	}
	
	private boolean doAttackHitSimple(Attack attack, L2Character target, double attackpercent, int sAtk)
	{
		int damage1 = 0;
		byte shld1 = 0;
		boolean crit1 = false;
		
		// Calculate if hit is missed or not
		boolean miss1 = Formulas.calcHitMiss(this, target);
		
		// Check if hit isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient
			shld1 = Formulas.calcShldUse(this, target);
			
			// Calculate if hit is critical
			crit1 = Formulas.calcCrit(getStat().getCriticalHit(target, null));
			
			// Calculate physical damages
			damage1 = (int) Formulas.calcPhysDam(this, target, null, shld1, crit1, false, attack.hasSoulshot());
			
			if (attackpercent != 100)
				damage1 = (int) (damage1 * attackpercent / 100);
		}
		
		// Create a new hit task with Medium priority
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack.hasSoulshot(), shld1), sAtk);
		
		// Add this hit to the Server-Client packet Attack
		attack.addHit(target, damage1, miss1, crit1, shld1);
		
		// Return true if hit isn't missed
		return !miss1;
	}
	
	public void doCast(L2Skill skill)
	{			
		if (skill == null)
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		if(!skill.isPotion())
		{
			if (isSkillDisabled(skill.getId()))
			{
				if (isPlayer())
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE).addSkillName(skill.getId(), skill.getLevel()));
				
				return;
			}
			
			// Check if the skill is a magic spell and if the L2Character is not muted
			if (skill.isMagic() && isMuted())
			{
				getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
				return;
			}
			
			// Check if the skill is psychical and if the L2Character is not psychical_muted
			if (!skill.isMagic() && isPsychicalMuted())
			{
				getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
				return;
			}	
		}
		
		if (isPlayer())
		{
			L2PcInstance player = (L2PcInstance) this;
			
			if (player.getTarget() != null && player.getTarget() == player && skill.getSkillType() == L2SkillType.CHARGEDAM)
				return;
			
			// Can't use Hero and resurrect skills during Olympiad
			if (player.isInOlympiadMode() && (skill.isHeroSkill() || skill.getSkillType() == L2SkillType.RESURRECT))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THIS_SKILL_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
				return;
			}
		}
		
		// prevent casting signets to peace zone
		if ((skill.getSkillType() == L2SkillType.SIGNET) || (skill.getSkillType() == L2SkillType.SIGNET_CASTTIME))
		{
			L2WorldRegion region = getWorldRegion();
			if (region == null)
				return;
			boolean canCast = true;
			if (skill.getTargetType() == L2SkillTargetType.TARGET_GROUND && this instanceof L2PcInstance)
			{
				Point3D wp = ((L2PcInstance) this).getCurrentSkillWorldPosition();
				
				if (!ZoneManager.getInstance().getRegion(this).checkEffectRangeInsidePeaceZone(skill, wp.getX(), wp.getY(), wp.getZ()))
					canCast = false;
			}
			else if (!ZoneManager.getInstance().getRegion(this).checkEffectRangeInsidePeaceZone(skill, getX(), getY(), getZ()))
				canCast = false;
			if (!canCast)
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill));
				return;
			}
		}

		// Get all possible targets of the skill in a table in function of the skill target type
		L2Object[] targets = skill.getTargetList(this);	
		// Set the target of the skill in function of Skill Type and Target Type
		L2Character target = null;
	
		if (skill.getSkillType() == L2SkillType.BUFF || skill.getSkillType() == L2SkillType.HEAL || skill.getSkillType() == L2SkillType.COMBATPOINTHEAL || skill.getSkillType() == L2SkillType.MANAHEAL || skill.getSkillType() == L2SkillType.REFLECT || skill.getSkillType() == L2SkillType.SEED || skill.getTargetType() == L2SkillTargetType.TARGET_SELF || skill.getTargetType() == L2SkillTargetType.TARGET_PET || skill.getTargetType() == L2SkillTargetType.TARGET_PARTY || skill.getTargetType() == L2SkillTargetType.TARGET_CLAN || skill.getTargetType() == L2SkillTargetType.TARGET_ALLY)
		{
			target = (L2Character) targets[0];
			
			if (isPlayer() && target.isPlayer() && target.getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
			{
				if (skill.getSkillType() == L2SkillType.BUFF || skill.getSkillType() == L2SkillType.HOT || skill.getSkillType() == L2SkillType.HEAL || skill.getSkillType() == L2SkillType.HEAL_PERCENT || skill.getSkillType() == L2SkillType.MANAHEAL || skill.getSkillType() == L2SkillType.MANAHEAL_PERCENT || skill.getSkillType() == L2SkillType.BALANCE_LIFE)
					target.setLastBuffer(this);
				
				if (((L2PcInstance) this).isInParty() && skill.getTargetType() == L2SkillTargetType.TARGET_PARTY)
				{
					for (L2PcInstance member : ((L2PcInstance) this).getParty().getPartyMembers())
						member.setLastBuffer(this);
				}
			}
		}
		else
			target = (L2Character) getTarget();
		
		// AURA and SIGNET skills should always be using caster as target
		switch (skill.getTargetType())
		{
			case TARGET_AREA_SUMMON:
				target = getPet();
				break;
			case TARGET_AURA:
			case TARGET_SELF:
			case TARGET_SIGNET_GROUND:
			case TARGET_SIGNET:
			case TARGET_FRONT_AURA:
			case TARGET_BEHIND_AURA:
			case TARGET_AURA_UNDEAD:
			case TARGET_GROUND:
				target = this;
				break;
			default:
				break;
		}
		
		if (target == null)	
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		setLastSkillCast(skill);
		
		// Get the Identifier of the skill
		int magicId = skill.getId();
		
		// Get the Display Identifier for a skill that client can't display
		int displayId = skill.getDisplayId();
		
		// Get the level of the skill
		int level = skill.getLevel();
		
		if (level < 1)
			level = 1;
		
		// Get the casting time of the skill (base)
		int hitTime = skill.getHitTime();
		int coolTime = skill.getCoolTime();
		
		boolean hasEffectDelay = skill.getInitialEffectDelay() > 0;
		
		// Calculate the casting time of the skill (base + modifier of MAtkSpd)
		// Don't modify the skill time for FORCE_BUFF skills. The skill time for those skills represent the buff time.
		if (!hasEffectDelay)
		{
			hitTime = Formulas.calcMAtkSpd(this, skill, hitTime);
			if (coolTime > 0)
				coolTime = Formulas.calcMAtkSpd(this, skill, coolTime);
		}
		
		// Calculate altered Cast Speed due to BSpS/SpS
		L2ItemInstance weaponInst = getActiveWeaponInstance();
		
		if (weaponInst != null && skill.isMagic() && !hasEffectDelay && skill.getTargetType() != L2SkillTargetType.TARGET_SELF)
		{
			if ((weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT) || (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT))
			{
				// Only takes 70% of the time to cast a BSpS/SpS cast
				hitTime = (int) (0.70 * hitTime);
				coolTime = (int) (0.70 * coolTime);
				
				// Because the following are magic skills that do not actively 'eat' BSpS/SpS,
				// I must 'eat' them here so players don't take advantage of infinite speed increase
				switch (skill.getSkillType())
				{
					case BUFF:
					case MANAHEAL:
					case RESURRECT:
					case RECALL:
					case DOT:
						weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
						break;
					default:
						break;
				}
			}
		}
		
		// Set the _castEndTime and _castInterruptTim. +10 ticks for lag situations, will be reseted in onMagicFinalizer
		_castEndTime = 10 + GameTimeController.getInstance().getGameTicks() + (coolTime + hitTime) / GameTimeController.MILLIS_IN_TICK;
		_castInterruptTime = -2 + GameTimeController.getInstance().getGameTicks() + hitTime / GameTimeController.MILLIS_IN_TICK;
		
		// Init the reuse time of the skill
		int reuseDelay;
		
		if (skill.isMagic())
			reuseDelay = (int) (skill.getReuseDelay() * getStat().getMReuseRate(skill));
		else
			reuseDelay = (int) (skill.getReuseDelay() * getStat().getPReuseRate(skill));
		
		reuseDelay *= 333.0 / (skill.isMagic() ? getMAtkSpd() : getPAtkSpd());
		
		boolean skillMastery = Formulas.calcSkillMastery(this);
		

		// Broadcast MagicSkillUse for non toggle skills.
		if (!skill.isToggle())
			broadcastPacket(!skill.isPotion() ? new MagicSkillUse(this, target, displayId, level, hitTime, reuseDelay, false) : new MagicSkillUse(this, target, displayId, level, 0, 0));	
		
		// Send a system message USE_S1 to the L2Character
		if (isPlayer() && magicId != 1312)
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.USE_S1).addSkillName(magicId, skill.getLevel()));

		// Skill reuse check
		if (reuseDelay > 30000 && !skillMastery)
			addTimeStamp(skill.getId(), reuseDelay);
		
		// Check if this skill consume mp on start casting
		int initmpcons = getStat().getMpInitialConsume(skill);
		if (initmpcons > 0)
		{
			StatusUpdate su = new StatusUpdate(getObjectId());
			getStatus().reduceMp(calcStat(Stats.MP_CONSUME_RATE, initmpcons, null, null));
			su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
			sendPacket(su);
		}
		
		// Disable the skill during the re-use delay and create a task EnableSkill with Medium priority to enable it at the end of the re-use delay
		if (reuseDelay > 10 && !skillMastery)
			disableSkill(skill.getId(), reuseDelay);
		
		// Make sure that char is facing selected target
		if (target != this)
			setHeading(Util.calculateHeadingFrom(this, target));
		
		if (skillMastery && getActingPlayer() != null) // only possible for L2PcInstance
		{
			reuseDelay = 0;
			
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE);
			sm.addSkillName(skill);
			getActingPlayer().sendPacket(sm);
			sm = null;
		}
		
		// launch the magic in hitTime milliseconds
		if (hitTime > 210)
		{
			// Send a Server->Client packet SetupGauge with the color of the gauge and the casting time
			int initialEffectDelay = skill.getInitialEffectDelay();
			if (isPlayer())
			{
				if (hasEffectDelay)
					sendPacket(new SetupGauge(SetupGauge.BLUE, initialEffectDelay));
				else
					sendPacket(new SetupGauge(SetupGauge.BLUE, hitTime));
			}
			
			// Disable all skills during the casting
			disableAllSkills();
			
			if (_skillCast != null)
			{
				try
				{
					_skillCast.cancel(true);
				}
				catch (NullPointerException e)
				{
				}
				_skillCast = null;
			}

			// Create a task MagicUseTask to launch the MagicSkill at the end of the casting time (hitTime)
			// For client animation reasons (party buffs especially) 200 ms before!
			if (hasEffectDelay)
				_skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 2, hitTime), initialEffectDelay);
			else
				_skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 1, 0), hitTime - 200);
		}
		else
			onMagicLaunchedTimer(targets, skill, coolTime, true);
	}
	
	public void addTimeStamp(int s, int r)
	{
		
	}
	
	public void removeTimeStamp(int s)
	{
		
	}
	
	public void startForceBuff(L2Character caster, L2Skill skill)
	{
		
	}
	
	public boolean doDie(L2Character killer)
	{
		// killing is only possible one time
		synchronized (this)
		{
			if (isKilledAlready())
				return false;
			
			setCurrentHp(0);
			
			setIsKilledAlready(true);
		}
		// Set target to null and cancel Attack or Cast
		setTarget(null);
		
		if (isAfraid())
			stopFear(null);
		
		// Stop movement
		stopMove(null);
		
		// Stop HP/MP/CP Regeneration task
		getStatus().stopHpMpRegeneration();
		
		// Stop all active skills effects in progress on the L2Character,
		// if the Character isn't a Noblesse Blessed L2PlayableInstance
		if (this instanceof L2Playable && ((L2Playable) this).isNoblesseBlessed())
		{
			((L2Playable) this).stopNoblesseBlessing(null);
			if (((L2Playable) this).getCharmOfLuck()) // remove Lucky Charm if player have Nobless blessing buff
				((L2Playable) this).stopCharmOfLuck(null);
		}
		// Same thing if the Character is affected by Soul of The Phoenix or Salvation
		else if (this instanceof L2Playable && ((L2Playable) this).isPhoenixBlessed())
		{
			if (((L2Playable) this).getCharmOfLuck()) // remove Lucky Charm if player has SoulOfThePhoenix/Salvation buff
				((L2Playable) this).stopCharmOfLuck(null);
		}
		else if (Config.REMOVE_BUFFS_ON_DIE)
			stopAllEffects();
		
		if(killer !=null)
		   calculateRewards(killer);
				
		// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		broadcastStatusUpdate();
		
		// Notify L2Character AI
		if (hasAI())
			getAI().notifyEvent(CtrlEvent.EVT_DEAD, null);
		
		ZoneManager.getInstance().getRegion(this).onDeath(this);
		
		// Notify Quest of character's death
		if(!getNotifyQuestOfDeath().isEmpty())
		{
			for (QuestState qs : getNotifyQuestOfDeath())
				qs.getQuest().notifyDeath((killer == null ? this : killer), this, qs.getPlayer());
		}
		
		getNotifyQuestOfDeath().clear();
		
		getAttackByList().clear();
		
		// If character is PhoenixBlessed a resurrection popup will show up
		if (this instanceof L2Playable && ((L2Playable) this).isPhoenixBlessed())
			((L2PcInstance) this).reviveRequest(((L2PcInstance) this), null, false);

		return true;
	}
	
	protected void calculateRewards(L2Character killer)
	{
	}
	
	public void doRevive()
	{
		if (!isDead())
			return;
		
		if (!isTeleporting())
		{
			setIsPendingRevive(false);
			setIsKilledAlready(false);
			
			if (this instanceof L2Playable && ((L2Playable) this).isPhoenixBlessed())
				((L2Playable) this).stopPhoenixBlessing(null);
			
			_status.setCurrentHp(getMaxHp() * Config.RESPAWN_RESTORE_HP);		
			
			// Start broadcast status
			broadcastPacket(new Revive(this));
			
			ZoneManager.getInstance().getRegion(this).onRevive(this);			
		}
		else
			setIsPendingRevive(true);
	}
	
	public void doRevive(double revivePower)
	{
		doRevive();
	}
	
	protected void useMagic(L2Skill skill)
	{
		if (skill == null || isDead())
			return;
		
		// Check if the L2Character can cast
		if (isAllSkillsDisabled())
			return;
		
		if (skill.isPassive())
			return;
		
		// Get the target for the skill
		L2Object target = null;
		
		switch (skill.getTargetType())
		{
			case TARGET_AURA: // AURA, SELF should be cast even if no target has been found
			case TARGET_SELF:
			case TARGET_SIGNET:
			case TARGET_SIGNET_GROUND:
				target = this;
				break;
			default:
				
				// Get the first target of the list
				target = skill.getFirstOfTargetList(this);
				break;
		}
		// Notify the AI with AI_INTENTION_CAST and target
		getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
	}
	
	public L2CharacterAI getAI()
	{
		L2CharacterAI ai = _ai;
		if (ai == null)
		{
			synchronized (this)
			{
				ai = _ai;
				if (ai == null)
					_ai = ai = new L2CharacterAI(this);
			}
		}
		return ai;
	}
	
	public void setAI(L2CharacterAI newAI)
	{
		L2CharacterAI oldAI = getAI();
		if (oldAI != null && oldAI != newAI && oldAI instanceof L2AttackableAI)
			((L2AttackableAI) oldAI).stopAITask();
		_ai = newAI;
	}
	
	public boolean hasAI()
	{
		return _ai != null;
	}
	
	public boolean isRaid()
	{
		return _isRaid;
	}
	
	public void setIsRaid(boolean isRaid)
	{
		_isRaid = isRaid;
	}
	
	public boolean isBoss()
	{
		return false;
	}
	
	public final List<L2Character> getAttackByList()
	{
		if (_attackByList == null)
			_attackByList = new ArrayList<>();
		return _attackByList;
	}
	
	public final L2Skill getLastSkillCast()
	{
		return _lastSkillCast;
	}
	
	public void setLastSkillCast(L2Skill skill)
	{
		_lastSkillCast = skill;
	}
	
	public final boolean isAfraid()
	{
		return _isAfraid;
	}
	
	public final void setIsAfraid(boolean value)
	{
		_isAfraid = value;
	}
		
	public final boolean isDead()
	{
		return !(isFakeDeath()) && !(getCurrentHp() > 0.5);
	}
	
	public final boolean isAlikeDead()
	{
		return isFakeDeath() || !(getCurrentHp() > 0.5);
	}
	
	public final boolean isAllSkillsDisabled()
	{
		return _allSkillsDisabled || isImmobileUntilAttacked() || isStunned() || isSleeping() || isParalyzed();
	}

	public boolean isAttackingDisabled()
	{
		return isImmobileUntilAttacked() || isStunned() || isSleeping() || _attackEndTime > GameTimeController.getInstance().getGameTicks() || isFakeDeath() || isParalyzed();
	}
	
	public final Calculator[] getCalculators()
	{
		return _calculators;
	}
	
	public final boolean isConfused()
	{
		return _isConfused;
	}
	
	public final void setIsConfused(boolean value)
	{
		_isConfused = value;
	}

	
	public final boolean isFakeDeath()
	{
		return _isFakeDeath;
	}
	
	public final void setIsFakeDeath(boolean value)
	{
		_isFakeDeath = value;
	}
	
	public boolean isFlying()
	{
		return _isFlying;
	}
	
	public final void setIsFlying(boolean mode)
	{
		_isFlying = mode;
	}
	
	public boolean isImmobilized()
	{
		return _isImmobilized;
	}
	
	public void setIsImmobilized(boolean value)
	{
		// Stop this if he is moving
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		
		_isImmobilized = value;
	}
	
	public final boolean isKilledAlready()
	{
		return _isKilledAlready;
	}
	
	public final void setIsKilledAlready(boolean value)
	{
		_isKilledAlready = value;
	}
	
	public final boolean isMuted()
	{
		return _isMuted;
	}
	
	public final void setIsMuted(boolean value)
	{
		_isMuted = value;
	}
	
	public final boolean isPsychicalMuted()
	{
		return _isPsychicalMuted;
	}
	
	public final void setIsPsychicalMuted(boolean value)
	{
		_isPsychicalMuted = value;
	}
	
	public boolean isMovementDisabled()
	{
		// check for isTeleporting to prevent teleport cheating (if appear packet not received)
		return isStunned() || isRooted() || isSleeping() || isOverloaded() || isParalyzed() || isImmobilized() || isFakeDeath() || isTeleporting();
	}
	
	public boolean isOutOfControl()
	{
		return isConfused() || isAfraid();
	}
	
	public final boolean isOverloaded()
	{
		return _isOverloaded;
	}
	
	public final void setIsOverloaded(boolean value)
	{
		_isOverloaded = value;
	}
	
	public final boolean isParalyzed()
	{
		return _isParalyzed;
	}
	
	public final void setIsParalyzed(boolean value)
	{
		_isParalyzed = value;
	}
	
	public final boolean isPendingRevive()
	{
		return isDead() && _isPendingRevive;
	}
	
	public final void setIsPendingRevive(boolean value)
	{
		_isPendingRevive = value;
	}
	
	public L2Summon getPet()
	{
		return null;
	}
	
	public final boolean isRiding()
	{
		return _isRiding;
	}
	
	public final void setIsRiding(boolean mode)
	{
		_isRiding = mode;
	}
	
	public final boolean isRooted()
	{
		return _isRooted;
	}
	
	public final void setIsRooted(boolean value)
	{
		_isRooted = value;
	}
	
	public final boolean isRunning()
	{
		return _isRunning;
	}
	
	public final void setIsRunning(boolean value)
	{
		if (_isRunning == value)
			return;
		
		_isRunning = value;
		
		if (getMoveSpeed() != 0)
			broadcastPacket(new ChangeMoveType(this));
		
		if (isPlayer())
			getActingPlayer().broadcastUserInfo();
		if (this instanceof L2Summon)
			broadcastStatusUpdate();
	}
	
	public final void setWalking()
	{
		if (isRunning())
			setIsRunning(false);
	}
	
	public final void setRunning()
	{
		if (!isRunning())
			setIsRunning(true);
	}
	
	public final boolean isImmobileUntilAttacked()
	{
		return _isImmobileUntilAttacked;
	}
	
	public final void setIsImmobileUntilAttacked(boolean value)
	{
		_isImmobileUntilAttacked = value;
	}
	
	public final boolean isSleeping()
	{
		return _isSleeping;
	}
	
	public final void setIsSleeping(boolean value)
	{
		_isSleeping = value;
	}
	
	public final boolean isStunned()
	{
		return _isStunned;
	}
	
	public final void setIsStunned(boolean value)
	{
		_isStunned = value;
	}
	
	public final boolean isBetrayed()
	{
		return _isBetrayed;
	}
	
	public final void setIsBetrayed(boolean value)
	{
		_isBetrayed = value;
	}
	
	public final boolean isTeleporting()
	{
		return _isTeleporting;
	}
	
	public final void setIsTeleporting(boolean value)
	{
		_isTeleporting = value;
	}
	
	public void setIsInvul(boolean b)
	{
		_isInvul = b;
		
	}
	
	public boolean isInvul()
	{
		return _isInvul || _isTeleporting;
	}

	public boolean isUndead()
	{
		return _template.isUndead;
	}
	
	public CharStat getStat()
	{
		if (_stat == null)
			_stat = new CharStat(this);
		return _stat;
	}
	
	public final void setStat(CharStat value)
	{
		_stat = value;
	}
	
	public CharStatus getStatus()
	{
		if (_status == null)
			_status = new CharStatus(this);
		return _status;
	}
	
	public final void setStatus(CharStatus value)
	{
		_status = value;
	}
	
	public L2CharTemplate getTemplate()
	{
		return _template;
	}
	
	protected final void setTemplate(L2CharTemplate template)
	{
		_template = template;
	}
	
	public final String getTitle()
	{
		return _title;
	}
	
	public final void setTitle(String value)
	{
		_title = value;
	}
	
	class EnableSkill implements Runnable
	{
		int _skillId;
		
		public EnableSkill(int skillId)
		{
			_skillId = skillId;
		}
		
		@Override
		public void run()
		{
			try
			{
				enableSkill(_skillId);
			}
			catch (Throwable e)
			{
				_log.severe(EnableSkill.class.getName() + ": Throwable: EnableSkill " +e);
			}
		}
	}
	
	class HitTask implements Runnable
	{
		L2Character _hitTarget;
		int _damage;
		boolean _crit;
		boolean _miss;
		byte _shld;
		boolean _soulshot;
		
		public HitTask(L2Character target, int damage, boolean crit, boolean miss, boolean soulshot, byte shld1)
		{
			_hitTarget = target;
			_damage = damage;
			_crit = crit;
			_shld = shld1;
			_miss = miss;
			_soulshot = soulshot;
		}
		
		@Override
		public void run()
		{
			onHitTimer(_hitTarget, _damage, _crit, _miss, _soulshot, _shld);
		}
	}
	
	class MagicUseTask implements Runnable
	{
		L2Object[] _targets;
		L2Skill _skill;
		int _hitTime;
		int _coolTime;
		int _phase;
		
		public MagicUseTask(L2Object[] targets, L2Skill skill, int coolTime, int phase, int hitTime)
		{
			_hitTime = hitTime;
			_targets = targets;
			_skill = skill;
			_coolTime = coolTime;
			_phase = phase;
		}
		
		@Override
		public void run()
		{
			try
			{
				switch (_phase)
				{
					case 1:
						onMagicLaunchedTimer(_targets, _skill, _coolTime, false);
						break;
					case 2:
						onMagicHitTimer(_targets, _skill, _coolTime, false, _hitTime);
						break;
					case 3:
						onMagicFinalizer(_skill, _skill != null && _targets.length <= 0 ? _skill.getFirstOfTargetList(getActor().isPlayer() ? getActor().getActingPlayer() : getActor()) : _targets[0]);
						break;
				}
			}
			catch (Throwable e)
			{
				_log.severe(MagicUseTask.class.getName() + ": Throwable: EnableSkill");
				e.printStackTrace();
				enableAllSkills();
			}
		}
	}
	
	class QueuedMagicUseTask implements Runnable
	{
		L2PcInstance _currPlayer;
		L2Skill _queuedSkill;
		boolean _isCtrlPressed;
		boolean _isShiftPressed;
		
		public QueuedMagicUseTask(L2PcInstance currPlayer, L2Skill queuedSkill, boolean isCtrlPressed, boolean isShiftPressed)
		{
			_currPlayer = currPlayer;
			_queuedSkill = queuedSkill;
			_isCtrlPressed = isCtrlPressed;
			_isShiftPressed = isShiftPressed;
		}
		
		@Override
		public void run()
		{
			try
			{
				_currPlayer.useMagic(_queuedSkill, _isCtrlPressed, _isShiftPressed);
			}
			catch (Throwable e)
			{
				_log.severe(QueuedMagicUseTask.class.getName() + ": Throwable: EnableSkill");
				e.printStackTrace();
			}
		}
	}
	
	
	public class NotifyAITask implements Runnable
	{
		private final CtrlEvent _evt;
		
		NotifyAITask(CtrlEvent evt)
		{
			_evt = evt;
		}
		
		@Override
		public void run()
		{
			try
			{
				if(getAI() != null)
				   getAI().notifyEvent(_evt, null);
			}
			catch (Throwable t)
			{
			}
		}
	}
	
	// =========================================================
	
	// =========================================================
	// Abnormal Effect - NEED TO REMOVE ONCE L2CHARABNORMALEFFECT IS COMPLETE
	
	private int _AbnormalEffects;
	
	protected CharEffectList _effects = new CharEffectList(this);
	
	protected Map<String, List<L2Effect>> _stackedEffects;
	
	public static final int SABNORMAL_EFFECT_BLEEDING = 0x000001;
	public static final int SABNORMAL_EFFECT_POISON = 0x000002;
	public static final int SABNORMAL_EFFECT_UNKNOWN_3 = 0x000004;
	public static final int SABNORMAL_EFFECT_UNKNOWN_4 = 0x000008;
	public static final int SABNORMAL_EFFECT_UNKNOWN_5 = 0x000010;
	public static final int SABNORMAL_EFFECT_UNKNOWN_6 = 0x000020;
	public static final int SABNORMAL_EFFECT_STUN = 0x000040;
	public static final int SABNORMAL_EFFECT_SLEEP = 0x000080;
	public static final int SABNORMAL_EFFECT_MUTED = 0x000100;
	public static final int SABNORMAL_EFFECT_ROOT = 0x000200;
	public static final int SABNORMAL_EFFECT_HOLD_1 = 0x000400;
	public static final int SABNORMAL_EFFECT_HOLD_2 = 0x000800;
	public static final int SABNORMAL_EFFECT_UNKNOWN_13 = 0x001000;
	public static final int SABNORMAL_EFFECT_BIG_HEAD = 0x002000;
	public static final int SABNORMAL_EFFECT_FLAME = 0x004000;
	public static final int SABNORMAL_EFFECT_UNKNOWN_16 = 0x008000;
	public static final int SABNORMAL_EFFECT_GROW = 0x010000;
	public static final int SABNORMAL_EFFECT_FLOATING_ROOT = 0x020000;
	public static final int SABNORMAL_EFFECT_DANCE_STUNNED = 0x040000;
	public static final int SABNORMAL_EFFECT_FIREROOT_STUN = 0x080000;
	public static final int SABNORMAL_EFFECT_STEALTH = 0x100000;
	public static final int SABNORMAL_EFFECT_IMPRISIONING_1 = 0x200000;
	public static final int SABNORMAL_EFFECT_IMPRISIONING_2 = 0x400000;
	public static final int SABNORMAL_EFFECT_MAGIC_CIRCLE = 0x800000;
	public static final int SABNORMAL_EFFECT_CONFUSED = 0x0020;
	public static final int SABNORMAL_EFFECT_AFRAID = 0x0010;
	
	public void addEffect(L2Effect newEffect)
	{
		_effects.queueEffect(newEffect, false);
	}
	
	public final void removeEffect(L2Effect effect)
	{
		_effects.queueEffect(effect, true);
	}
	
	public final void startAbnormalEffect(int mask)
	{
		_AbnormalEffects |= mask;
		updateAbnormalEffect();
	}
	
	public final void startAbnormalEffect(AbnormalEffect mask)
	{
		_AbnormalEffects |= mask.getMask();
		updateAbnormalEffect();
	}
	
	public final void startImmobileUntilAttacked()
	{
		setIsImmobileUntilAttacked(true);
		abortAttack();
		abortCast();
		getAI().notifyEvent(CtrlEvent.EVT_SLEEPING);
		updateAbnormalEffect();
	}
	
	public final void startConfused()
	{
		setIsConfused(true);
		getAI().notifyEvent(CtrlEvent.EVT_CONFUSED);
		updateAbnormalEffect();
	}
	
	public final void startFear()
	{
		setIsAfraid(true);
		getAI().notifyEvent(CtrlEvent.EVT_AFFRAID);
		updateAbnormalEffect();
	}
	
	public final void startMuted()
	{
		setIsMuted(true);
		
		abortCast();
		getAI().notifyEvent(CtrlEvent.EVT_MUTED);
		updateAbnormalEffect();
	}
	
	public final void startPsychicalMuted()
	{
		setIsPsychicalMuted(true);
		getAI().notifyEvent(CtrlEvent.EVT_MUTED);
		updateAbnormalEffect();
	}
	
	public final void startRooted()
	{
		setIsRooted(true);
		getAI().notifyEvent(CtrlEvent.EVT_ROOTED, null);
		updateAbnormalEffect();
	}
	
	public final void startSleeping()
	{
		setIsSleeping(true);
		
		abortAttack();
		abortCast();
		getAI().notifyEvent(CtrlEvent.EVT_SLEEPING, null);
		updateAbnormalEffect();
	}
	
	public final void startStunning()
	{
		setIsStunned(true);
		
		abortAttack();
		abortCast();
		getAI().stopFollow();
		getAI().notifyEvent(CtrlEvent.EVT_STUNNED, null);
		updateAbnormalEffect();
	}
	
	public final void startBetray()
	{
		setIsBetrayed(true);
		getAI().notifyEvent(CtrlEvent.EVT_BETRAYED, null);
		updateAbnormalEffect();
	}
	
	public final void stopBetray()
	{
		stopEffects(L2Effect.EffectType.BETRAY);
		setIsBetrayed(false);
		updateAbnormalEffect();
	}
	
	public final void stopAbnormalEffect(int mask)
	{
		_AbnormalEffects &= ~mask;
		updateAbnormalEffect();
	}
	
	public final void stopAbnormalEffect(AbnormalEffect mask)
	{
		_AbnormalEffects &= ~mask.getMask();
		updateAbnormalEffect();
	}
	
	public final void stopAllEffects()
	{
		// Get all active skills effects in progress on the L2Character
		L2Effect[] effects = getAllEffects();
		if (effects == null)
			return;
		
		// Go through all active skills effects
		for (L2Effect e : effects)
		{
			if (e != null)
			{
				e.exit(true);
			}
		}
		
		if (this instanceof L2PcInstance)
			((L2PcInstance) this).updateAndBroadcastStatus(2);
	}
	
	public final void stopImmobileUntilAttacked(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.IMMOBILEUNTILATTACKED);
		else
		{
			removeEffect(effect);
			stopSkillEffects(effect.getSkill().getNegateId());
		}
		
		setIsImmobileUntilAttacked(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK);
		updateAbnormalEffect();
	}
	
	public final void stopConfused(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.CONFUSION);
		else
			removeEffect(effect);
		
		setIsConfused(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}
	
	public final void stopSkillEffects(int skillId)
	{
		// Get all skills effects on the L2Character
		L2Effect[] effects = getAllEffects();
		if (effects == null)
			return;
		
		for (L2Effect e : effects)
		{
			if (e.getSkill().getId() == skillId)
				e.exit();
		}
	}
	
	public final void stopEffects(L2Effect.EffectType type)
	{
		// Get all active skills effects in progress on the L2Character
		L2Effect[] effects = getAllEffects();
		
		if (effects == null)
			return;
		
		// Go through all active skills effects
		for (L2Effect e : effects)
		{
			// Stop active skills effects of the selected type
			if (e.getEffectType() == type)
				e.exit();
		}
	}
	
	public final void stopFear(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.FEAR);
		else
			removeEffect(effect);
		
		stopMove(null);
		
		setIsAfraid(false);
		updateAbnormalEffect();
	}
	
	public final void stopMuted(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.MUTE);
		else
			removeEffect(effect);
		
		setIsMuted(false);
		updateAbnormalEffect();
	}
	
	public final void stopPsychicalMuted(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.PSYCHICAL_MUTE);
		else
			removeEffect(effect);
		
		setIsPsychicalMuted(false);
		updateAbnormalEffect();
	}
	
	public final void stopRooting(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.ROOT);
		else
			removeEffect(effect);
		
		setIsRooted(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}
	
	public final void stopSleeping(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.SLEEP);
		else
			removeEffect(effect);
		
		setIsSleeping(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}
	
	public final void stopStunning(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2Effect.EffectType.STUN);
		else
			removeEffect(effect);
		
		setIsStunned(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}
	
	public abstract void updateAbnormalEffect();
	
	public final void updateEffectIcons()
	{
		_effects.updateEffectIcons(false);
	}
	
	public final void updateEffectIcons(boolean partyOnly)
	{
		_effects.updateEffectIcons(partyOnly);
	}
	
	public int getAbnormalEffect()
	{
		int ae = _AbnormalEffects;
		if (isStunned())
			ae |= AbnormalEffect.STUN.getMask();
		if (isRooted())
			ae |= AbnormalEffect.ROOT.getMask();
		if (isSleeping())
			ae |= AbnormalEffect.SLEEP.getMask();
		if (isConfused())
			ae |= AbnormalEffect.FEAR.getMask();
		if (isAfraid())
			ae |= AbnormalEffect.FEAR.getMask();
		if (isMuted())
			ae |= AbnormalEffect.MUTED.getMask();
		if (isPsychicalMuted())
			ae |= AbnormalEffect.MUTED.getMask();
		if (isImmobileUntilAttacked())
			ae |= AbnormalEffect.FLOATING_ROOT.getMask();
		
		return ae;
	}
	
	public final L2Effect[] getAllEffects()
	{
		return _effects.getAllEffects();
	}
	
	public final L2Effect getFirstEffect(int skillId)
	{
		return _effects.getFirstEffect(skillId);
	}
	
	public final L2Effect getFirstEffect(L2Skill skill)
	{
		return _effects.getFirstEffect(skill);
	}
	
	public final L2Effect getFirstEffect(L2Effect.EffectType tp)
	{
		return _effects.getFirstEffect(tp);
	}
	
	public EffectCharge getChargeEffect()
	{
		L2Effect[] effects = getAllEffects();
		for (L2Effect e : effects)
		{
			if (e.getSkill().getSkillType() == L2SkillType.CHARGE)
			{
				return (EffectCharge) e;
			}
		}
		return null;
	}


		public L2Character getActor()
		{
			return L2Character.this;
		}
		
		public void moveTo(int x, int y, int z, int offset)
		{
			moveToLocation(x, y, z, offset);
		}
		
		public void moveTo(int x, int y, int z)
		{
			moveToLocation(x, y, z, 0);
		}

		public void doAttack(L2Character target)
		{
			L2Character.this.doAttack(target,true);
		}

		public NotifyAITask newNotifyTask(CtrlEvent evt)
		{
			return new NotifyAITask(evt);
		}
		
		public void detachAI()
		{
			_ai = null;
		}

	public static class MoveData
	{	
		public List<Location> geoPath = new ArrayList<>();

		public int _moveStartTime;
		public int _moveTimestamp; 
		public int _xDestination;
		public int _yDestination;
		public int _zDestination;
		public double _xAccurate;
		public double _yAccurate;
		public double _zAccurate;
		public int _heading;
		
		public boolean disregardingGeodata;
		public int onGeodataPathIndex;
		public int geoPathAccurateTx;
		public int geoPathAccurateTy;
		public int geoPathGtx;
		public int geoPathGty;
	}
			
	protected List<Integer> _disabledSkills;
	private boolean _allSkillsDisabled;
	
	// private int _flyingRunSpeed;
	// private int _floatingWalkSpeed;
	// private int _flyingWalkSpeed;
	// private int _floatingRunSpeed;
	
	protected MoveData _move;
	
	private int _heading;
	
	private L2Object _target;
	
	// set by the start of casting, in game ticks
	private int _castEndTime;
	private int _castInterruptTime;
	
	// set by the start of attack, in game ticks
	private int _attackEndTime;
	private int _attacking;
	private int _disableBowAttackEndTime;
	
	private static final Calculator[] NPC_STD_CALCULATOR;
	static
	{
		NPC_STD_CALCULATOR = Formulas.getStdNPCCalculators();
	}
	
	protected L2CharacterAI _ai;
	
	protected Future<?> _skillCast;
	
	private int _clientX;
	private int _clientY;
	private int _clientZ;
	private int _clientHeading;
	
	private List<QuestState> _NotifyQuestOfDeathList = new ArrayList<>();
	
	public void addNotifyQuestOfDeath(QuestState qs)
	{
		if (qs == null || _NotifyQuestOfDeathList.contains(qs))
			return;
		
		_NotifyQuestOfDeathList.add(qs);
	}
	
	public List<QuestState> getNotifyQuestOfDeath()
	{
		if (_NotifyQuestOfDeathList == null)
			_NotifyQuestOfDeathList = new ArrayList<>();
		
		return _NotifyQuestOfDeathList;
	}
	
	public final synchronized void addStatFunc(Func f)
	{
		if (f == null)
			return;
		
		// Check if Calculator set is linked to the standard Calculator set of
		// NPC
		if (_calculators == NPC_STD_CALCULATOR)
		{
			// Create a copy of the standard NPC Calculator set
			_calculators = new Calculator[Stats.NUM_STATS];
			
			for (int i = 0; i < Stats.NUM_STATS; i++)
			{
				if (NPC_STD_CALCULATOR[i] != null)
					_calculators[i] = new Calculator(NPC_STD_CALCULATOR[i]);
			}
		}
		
		// Select the Calculator of the affected state in the Calculator set
		int stat = f.stat.ordinal();
		
		if (_calculators[stat] == null)
			_calculators[stat] = new Calculator();
		
		// Add the Func to the calculator corresponding to the state
		_calculators[stat].addFunc(f);
		
	}
	
	public final synchronized void addStatFuncs(Func[] funcs)
	{
		List<Stats> modifiedStats = new ArrayList<>();
		
		for (Func f : funcs)
		{
			modifiedStats.add(f.stat);
			addStatFunc(f);
		}
		broadcastModifiedStats(modifiedStats);
	}
	
	public final synchronized void removeStatFunc(Func f)
	{
		if (f == null)
			return;
		
		// Select the Calculator of the affected state in the Calculator set
		int stat = f.stat.ordinal();
		
		if (_calculators[stat] == null)
			return;
		
		// Remove the Func object from the Calculator
		_calculators[stat].removeFunc(f);
		
		if (_calculators[stat].size() == 0)
			_calculators[stat] = null;
		
		// If possible, free the memory and just create a link on
		// NPC_STD_CALCULATOR
		if (this instanceof L2Npc)
		{
			int i = 0;
			for (; i < Stats.NUM_STATS; i++)
			{
				if (!Calculator.equalsCals(_calculators[i], NPC_STD_CALCULATOR[i]))
					break;
			}
			
			if (i >= Stats.NUM_STATS)
				_calculators = NPC_STD_CALCULATOR;
		}
	}
	
	public final synchronized void removeStatFuncs(Func[] funcs)
	{
		List<Stats> modifiedStats = new ArrayList<>();
		
		for (Func f : funcs)
		{
			modifiedStats.add(f.stat);
			removeStatFunc(f);
		}
		broadcastModifiedStats(modifiedStats);
	}
	
	public final synchronized void removeStatsOwner(Object owner)
	{
		
		List<Stats> modifiedStats = null;
		// Go through the Calculator set
		for (int i = 0; i < _calculators.length; i++)
		{
			if (_calculators[i] != null)
			{
				// Delete all Func objects of the selected owner
				if (modifiedStats != null)
					modifiedStats.addAll(_calculators[i].removeOwner(owner));
				else
					modifiedStats = _calculators[i].removeOwner(owner);
				
				if (_calculators[i].size() == 0)
					_calculators[i] = null;
			}
		}
		
		// If possible, free the memory and just create a link on
		// NPC_STD_CALCULATOR
		if (this instanceof L2Npc)
		{
			int i = 0;
			for (; i < Stats.NUM_STATS; i++)
			{
				if (!Calculator.equalsCals(_calculators[i], NPC_STD_CALCULATOR[i]))
					break;
			}
			
			if (i >= Stats.NUM_STATS)
				_calculators = NPC_STD_CALCULATOR;
		}

		if (owner instanceof L2Effect)
		{
			if (!((L2Effect) owner).preventExitUpdate)
				broadcastModifiedStats(modifiedStats);
		}
		else
			broadcastModifiedStats(modifiedStats);		
	}
	
	private void broadcastModifiedStats(List<Stats> stats)
	{
		if (stats == null || stats.isEmpty())
			return;
		
		boolean broadcastFull = false;
		StatusUpdate su = null;
		
		if (this instanceof L2Summon && ((L2Summon) this).getOwner() != null)
		    ((L2Summon) this).updateAndBroadcastStatus(1);
		else
		{
			for (final Stats stat : stats)
			{
				if (stat == Stats.POWER_ATTACK_SPEED)
				{
					if (su == null)
						su = new StatusUpdate(getObjectId());
					
					su.addAttribute(StatusUpdate.ATK_SPD, getPAtkSpd());
				}
				else if (stat == Stats.MAGIC_ATTACK_SPEED)
				{
					if (su == null)
						su = new StatusUpdate(getObjectId());
					
					su.addAttribute(StatusUpdate.CAST_SPD, getMAtkSpd());
				}
				else if (stat == Stats.MAX_HP && this instanceof L2Attackable)
				{
					if (su == null)
						su = new StatusUpdate(getObjectId());
					
					su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
				}
				else if (stat == Stats.RUN_SPEED)
					broadcastFull = true;
			}
		}
		
		if (isPlayer())
		{
			if (broadcastFull)
				((L2PcInstance) this).updateAndBroadcastStatus(2);
			else
			{
				((L2PcInstance) this).updateAndBroadcastStatus(1);
				if (su != null)
					broadcastPacket(su);
			}
		}
		else if (isNpc())
		{
			if (broadcastFull)
			{
				for (L2PcInstance player : L2World.getInstance().getVisibleObjects(this, L2PcInstance.class))
				{
					if (getMoveSpeed() == 0)
						player.sendPacket(new ServerObjectInfo((L2Npc) this, player));
					else
						player.sendPacket(new NpcInfo((L2Npc) this, player));
				}
			}
			else if (su != null)
				broadcastPacket(su);
		}
		else if (su != null)
			broadcastPacket(su);
	}
	
	@Override
	public final int getHeading()
	{
		return _heading;
	}
	
	public final void setHeading(int heading)
	{
		_heading = heading;
	}
	
	public final int getClientX()
	{
		return _clientX;
	}
	
	public final int getClientY()
	{
		return _clientY;
	}
	
	public final int getClientZ()
	{
		return _clientZ;
	}
	
	public final int getClientHeading()
	{
		return _clientHeading;
	}
	
	public final void setClientX(int val)
	{
		_clientX = val;
	}
	
	public final void setClientY(int val)
	{
		_clientY = val;
	}
	
	public final void setClientZ(int val)
	{
		_clientZ = val;
	}
	
	public final void setClientHeading(int val)
	{
		_clientHeading = val;
	}
	
	public final int getXdestination()
	{
		MoveData m = _move;
		
		if (m != null)
			return m._xDestination;
		
		return getX();
	}
	
	public final int getYdestination()
	{
		MoveData m = _move;
		
		if (m != null)
			return m._yDestination;
		
		return getY();
	}
	
	public final int getZdestination()
	{
		MoveData m = _move;
		
		if (m != null)
			return m._zDestination;
		
		return getZ();
	}
	
	public boolean isInCombat()
	{
		return hasAI() && AttackStanceTaskManager.getInstance().isInAttackStance(this);
	}
	
	public final boolean isMoving()
	{
		return _move != null;
	}
	
	public final boolean isOnGeodataPath() 
	{
		MoveData m = _move;
		
		if (m == null) 
			return false;
		if (m.onGeodataPathIndex == -1)
			return false;
		if ((m.geoPath.isEmpty()))
			return false;
		if (m.onGeodataPathIndex == (m.geoPath.size() - 1))
			return false;
		
		return true;
	}
	
	public final boolean isCastingNow()
	{
		return _castEndTime > GameTimeController.getInstance().getGameTicks();
	}

	public final boolean canAbortCast()
	{
		return _castInterruptTime > GameTimeController.getInstance().getGameTicks();
	}
	
	public boolean isAttackingNow()
	{
		return _attackEndTime > GameTimeController.getInstance().getGameTicks();
	}
	
	public final boolean isAttackAborted()
	{
		return _attacking <= 0;
	}
	
	public final void abortAttack()
	{
		if (isAttackingNow())
		{
			_attacking = 0;
			sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	public final void abortAllAttacks()
	{
		abortCast();
		abortAttack();
	}
	
	public final int getAttackingBodyPart()
	{
		return _attacking;
	}
	
	public final void abortCast()
	{
		if (isCastingNow())
		{
			_castEndTime = 0;
			_castInterruptTime = 0;
			if (_skillCast != null)
			{
				_skillCast.cancel(true);
				_skillCast = null;
			}
			
			if (getForceBuff() != null)
				getForceBuff().delete();
			
			L2Effect mog = getFirstEffect(L2Effect.EffectType.SIGNET_GROUND);
			if (mog != null)
				mog.exit();
			
			// cancels the skill hit scheduled task
			enableAllSkills(); // re-enables the skills
			if (isPlayer())
				getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING);

			broadcastPacket(new MagicSkillCanceld(getObjectId()));
			sendPacket(ActionFailed.STATIC_PACKET); 
		}
	}
	
	public void revalidateZone(boolean force)
	{
		
		if (getWorldRegion() == null)
			return;
		
		// This function is called too often from movement code
		if (force)
			_zoneValidateCounter = 4;
		else
		{
			_zoneValidateCounter--;
			if (_zoneValidateCounter < 0)
				_zoneValidateCounter = 4;
			else
				return;
		}
		
		ZoneManager.getInstance().getRegion(this).revalidateZones(this);
	}
	
	public void stopMove(Location pos)
	{
		// Delete movement data of the L2Character
		_move = null;
		
		// Set the current position and refresh the region if necessary.
		if (pos != null)
		{
			setXYZ(pos.getX(), pos.getY(), pos.getZ());
			setHeading(pos.getHeading());
			revalidateZone(true);
			broadcastPacket(new StopRotation(getObjectId(), pos.getHeading(), 0));
		}
		broadcastPacket(new StopMove(this));
	}

	public boolean isShowSummonAnimation()
	{
		return _showSummonAnimation;
	}
	
	public void setShowSummonAnimation(boolean showSummonAnimation)
	{
		_showSummonAnimation = showSummonAnimation;
	}
	
	public void setTarget(L2Object object)
	{
		if (object != null && !object.isVisible())
			object = null;
		
		// If object==null, Cancel Attak or Cast
		if (object == null && _target != null)
			broadcastPacket(new TargetUnselected(this));
		
		_target = object;
	}
	
	public final int getTargetId()
	{
		return (_target != null) ? _target.getObjectId() : -1;
	}
	
	public final L2Object getTarget()
	{
		return _target;
	}

	public void tryToFlee(L2Character attacker)
	{
		final double angle = Math.toRadians(Util.calculateAngleFrom(attacker, this));
		final int posX = (int) (getX() + (440 * Math.cos(angle)));
		final int posY = (int) (getY() + (440 * Math.sin(angle)));
		final int posZ = getZ();
		getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,new Location(posX, posY, posZ,getHeading()));
	}
	
	public void moveToLocation(int x, int y, int z, int offset)
	{
		if (!(this instanceof FakePlayer) && isPlayer() && !FloodProtectors.performAction(getActingPlayer().getClient(), Action.MOVE_ACTION))
			return;
		
		final float speed = (this instanceof L2Vehicle) ? ((L2Vehicle) this).getStat().getMoveSpeed() : getStat().getMoveSpeed();
		final boolean isFloating = isFlying() || isInsideZone(ZoneId.WATER);

		if (!(this instanceof L2Vehicle) && (isDead() || speed <= 0 || isMovementDisabled()))
		{
			if(isPlayer())
				sendPacket(new ActionFailed());
			return;
		}
		
		final int curX = super.getX();
		final int curY = super.getY();
		final int curZ = super.getZ();
		
		if (curX == x && curY == y && curZ == z)
		{
			if(isPlayer())
				sendPacket(new ActionFailed());
			return;
		}

		double dx = (x - curX);
		double dy = (y - curY);
		double dz = (z - curZ);
		double distance = Math.hypot(dx, dy);

		final boolean verticalMovementOnly = isFlying() && (distance == 0) && (dz != 0);
		
		if (verticalMovementOnly)
			distance = Math.abs(dz);
		
		if (isInsideZone(ZoneId.WATER) && distance > 700)
		{
			double divider = 700 / distance;
			x = curX + (int) (divider * dx);
			y = curY + (int) (divider * dy);
			z = curZ + (int) (divider * dz);
			dx = (x - curX);
			dy = (y - curY);
			dz = (z - curZ);
			distance = Math.hypot(dx, dy);
		}
		
		double cos;
		double sin;
		
		if (offset > 0 || distance < 1)
		{
			offset -= Math.abs(dz);
			if (offset < 5)
				offset = 5;
			
			if (distance < 1 || distance - offset <= 0)
			{
				getAI().notifyEvent(CtrlEvent.EVT_ARRIVED);
				return;
			}
			
			sin = dy / distance;
			cos = dx / distance;
			
			distance -= (offset - 5);
			
			x = curX + (int) (distance * cos);
			y = curY + (int) (distance * sin);
			
		}
		else
		{
			sin = dy / distance;
			cos = dx / distance;
		}
		
		final MoveData m = new MoveData();
		
		m.onGeodataPathIndex = -1;
		m.disregardingGeodata = false;

		if (Config.GEODATA && !isFloating)
		{
			double originalDistance = distance;
			int originalX = x;
			int originalY = y;
			int originalZ = z;
			int gtx = (originalX - L2World.WORLD_X_MIN) >> 4;
			int gty = (originalY - L2World.WORLD_Y_MIN) >> 4;

			if (this instanceof L2Attackable || this instanceof L2PcInstance
			|| (this instanceof L2Summon && !(getAI().getIntention() == AI_INTENTION_FOLLOW)) // assuming intention_follow only when following owner																																																																														 																										 
			|| isAfraid() || this instanceof L2RiftInvaderInstance)
			{
				if (isOnGeodataPath())
				{
					try
					{
						if (gtx == _move.geoPathGtx && gty == _move.geoPathGty)
							return;
						_move.onGeodataPathIndex = -1;

					}
					catch (NullPointerException e)
					{
					}
				}

				if (curX < L2World.WORLD_X_MIN || curX > L2World.WORLD_X_MAX || curY < L2World.WORLD_Y_MIN || curY > L2World.WORLD_Y_MAX)
				{
					getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

					if (isPlayer())
					{
						((L2PcInstance) this).sendMessage("Something happen with your coordinates,server teleporting you to nearest village");
						((L2PcInstance) this).teleToLocation(TeleportWhereType.TOWN);
					}
					else if (this instanceof L2Summon)
						return;
					else
						onDecay();
					
					return;
				}

				m.geoPath.clear();
				List<Location> path = findPath(curX, curY, curZ, originalX, originalY, originalZ, offset);
				if (path.size() > 0)
					m.geoPath.addAll(path);
				if ((m.geoPath == null) || (m.geoPath.isEmpty()))
				{
					if (isPlayer()|| (!(isPlayable()) && !(this instanceof L2MinionInstance) && Math.abs(z - curZ) > 140)
					|| (this instanceof L2Summon && !((L2Summon) this).getFollowStatus()))
					{			
						getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
						
						if(isPlayer())
							sendPacket(new ActionFailed());
						
						return;
					}
					m.disregardingGeodata = true;
					x = originalX;
					y = originalY;
					z = originalZ;
					distance = originalDistance;
				}
				else
				{
					m.onGeodataPathIndex = 0;
					m.geoPathGtx = gtx;
					m.geoPathGty = gty;
					m.geoPathAccurateTx = originalX;
					m.geoPathAccurateTy = originalY;

					x = m.geoPath.get(m.onGeodataPathIndex).getX();
					y = m.geoPath.get(m.onGeodataPathIndex).getY();
					z = m.geoPath.get(m.onGeodataPathIndex).getZ();

					dx = (x - curX);
					dy = (y - curY);

					distance = verticalMovementOnly ? Math.pow(dz, 2) : Math.hypot(dx, dy);

					sin = dy / distance;
					cos = dx / distance;
				}
			}

			if (distance < 1 && (this instanceof L2Playable || isAfraid() || this instanceof L2RiftInvaderInstance))
			{
				if (this instanceof L2Summon)
					((L2Summon) this).setFollowStatus(false);
				getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				
				if(isPlayer())
					sendPacket(new ActionFailed());
				
				return;
			}
		}

		if (isFloating && !verticalMovementOnly) 
			distance = Math.hypot(distance, dz);

		int ticksToMove = 1 + (int) (GameTimeController.TICKS_PER_SECOND * distance / speed);
		m._xDestination = x;
		m._yDestination = y;
		m._zDestination = z; 

		m._heading = 0; 
		
		if (!verticalMovementOnly) 
			setHeading(Util.calculateHeadingFrom(cos, sin));

		m._moveStartTime = GameTimeController.getInstance().getGameTicks();
		
		_move = m;

		GameTimeController.getInstance().registerMovingObject(this);

		if (ticksToMove * GameTimeController.MILLIS_IN_TICK > 3000)
			ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_ARRIVED_REVALIDATE), 2000);
	}
	
	public List<Location> findPath(int curX, int curY, int curZ, int originalX, int originalY, int originalZ, int _offset)
	{
		List<Location> path = new ArrayList<>(1);
		if (isFlying() || isInsideZone(ZoneId.WATER))
			path.add(new Location(originalX, originalY, originalZ));
		else if (GeoEngine.canMoveToCoord(getX(), getY(), getZ(), originalX, originalY, originalZ))
			path.add(new Location(originalX, originalY, originalZ));
		else
		{
			List<Location> tpath = GeoMove.findPath(getX(), getY(), getZ(), new Location(originalX, originalY, originalZ, 0), this);
			if (tpath.size() > 1)
			{
				tpath.remove(0);
				path = tpath;
			}
			else
			{
				Location nextloc = GeoEngine.moveCheck(getX(), getY(), getZ(), originalX, originalY);
				if (!nextloc.equals(getX(), getY(), getZ()))
					path.add(GeoEngine.moveCheck(getX(), getY(), getZ(), originalX, originalY));
			}
		}
		return path;
	}

	public boolean updatePosition()
	{
		final MoveData m = _move;
		
		if (m == null)
			return true;
		
		if (!isVisible())
		{
			_move = null;
			
			if(isPlayer())
				sendPacket(new ActionFailed());
			return true;
		}
		
		if (m._moveTimestamp == 0)
		{
			m._moveTimestamp = m._moveStartTime;
			m._xAccurate = getX();
			m._yAccurate = getY();
		}

		final int gameTicks = GameTimeController.getInstance().getGameTicks();
		
		if (m._moveTimestamp == gameTicks)
			return false;
		
		final int xPrev = getX();
		final int yPrev = getY();
		int zPrev = getZ(); 
		
		double dx;
		double dy;
		double dz;
		
		if (Config.COORD_SYNCHRONIZE == 1)
		{
			dx = m._xDestination - xPrev;
			dy = m._yDestination - yPrev;
		}
		else
		{
			dx = m._xDestination - m._xAccurate;
			dy = m._yDestination - m._yAccurate;
		}
		
		final boolean isFloating = isFlying() || isInsideZone(ZoneId.WATER);

		if (Config.GEODATA && (Config.COORD_SYNCHRONIZE == 2) && !(this instanceof L2Vehicle) && !isFloating && !m.disregardingGeodata && ((GameTimeController.getInstance().getGameTicks() % 15) == 0))
		{
			int geoHeight = GeoEngine.getHeight(xPrev, yPrev, zPrev);
			dz = m._zDestination - geoHeight;
			if (isPlayer() && (Math.abs(getActingPlayer().getClientZ() - geoHeight) > 200) && (Math.abs(getActingPlayer().getClientZ() - geoHeight) < 1500))
				dz = m._zDestination - zPrev; // allow diff
			else if (isInCombat() && (Math.abs(dz) > 200) && (((dx * dx) + (dy * dy)) < 40000)) // allow mob to climb up to pcinstance
				dz = m._zDestination - zPrev; // climbing
			else
				zPrev = geoHeight;
		}
		else
			dz = m._zDestination - zPrev;
		
		double delta = (dx * dx) + (dy * dy);
		if ((delta < 10000) && ((dz * dz) > 2500) && !isFloating)
			delta = Math.sqrt(delta);
		else
			delta = Math.sqrt(delta + (dz * dz));
		
		double distFraction = Double.MAX_VALUE;
		
		if (delta > 1)
		{
			final float speed = (this instanceof L2Vehicle) ? ((L2Vehicle) this).getStat().getMoveSpeed() : getStat().getMoveSpeed();
			final double distPassed = (speed * (gameTicks - m._moveTimestamp)) / GameTimeController.TICKS_PER_SECOND;
			distFraction = distPassed / delta;
		}
			
		int x,y,z;
		
		if (distFraction > 1)
		{
			x = m._xDestination;
			y = m._yDestination;
			z = m._zDestination;
		}
		else
		{
			m._xAccurate += dx * distFraction;
			m._yAccurate += dy * distFraction;
			
			x = (int) (m._xAccurate);
			y = (int) (m._yAccurate);
			z = zPrev + (int) ((dz * distFraction) + 0.5);
		}
	
		super.setXYZ(x,y,z);

		revalidateZone(false);
		
		m._moveTimestamp = gameTicks;

		if (distFraction > 1 && !moveToNextRoutePoint())
		{
			ThreadPoolManager.getInstance().executeAi(() -> getAI().notifyEvent(CtrlEvent.EVT_ARRIVED));
			return true;
		}	
		return false;
	}
	
	public boolean moveToNextRoutePoint()
	{
		if (!isOnGeodataPath()) 
		{
			_move = null;
			return false;
		}

		double speed = getMoveSpeed();
		
		if ((speed <= 0) || isMovementDisabled())
		{
			_move = null;
			return false;
		}

		MoveData md = _move;
		if (md == null) 
			return false;
		
		// Create and Init a MoveData object
		MoveData m = new MoveData();
		
		// Update MoveData object
		m.onGeodataPathIndex = md.onGeodataPathIndex + 1; // next segment
		m.geoPath = md.geoPath;
		m.geoPathGtx = md.geoPathGtx;
		m.geoPathGty = md.geoPathGty;
		m.geoPathAccurateTx = md.geoPathAccurateTx;
		m.geoPathAccurateTy = md.geoPathAccurateTy;
		
		if (md.onGeodataPathIndex == (md.geoPath.size() - 2)) 
		{
			m._xDestination = md.geoPathAccurateTx;
			m._yDestination = md.geoPathAccurateTy;
			m._zDestination = md.geoPath.get(m.onGeodataPathIndex).getZ();
		} 
		else
		{
			m._xDestination = md.geoPath.get(m.onGeodataPathIndex).getX();
			m._yDestination = md.geoPath.get(m.onGeodataPathIndex).getY();
			m._zDestination = md.geoPath.get(m.onGeodataPathIndex).getZ();
		}
		
		double distance = Math.hypot(m._xDestination - super.getX(), m._yDestination - super.getY());
		
		if (distance != 0) 
			setHeading(Util.calculateHeadingFrom(getX(), getY(), m._xDestination, m._yDestination));
		
		// Caclulate the Nb of ticks between the current position and the destination
		// One tick added for rounding reasons
		int ticksToMove = 1 + (int) ((GameTimeController.TICKS_PER_SECOND * distance) / speed);
		
		m._heading = 0; // initial value for coordinate sync
		
		m._moveStartTime = GameTimeController.getInstance().getGameTicks();
		
		// Set the L2Character _move object to MoveData object
		_move = m;
		
		// Add the character to movingObjects of the GameTimeController
		GameTimeController.getInstance().registerMovingObject(this);
		
		// Create a task to notify the AI that L2Character arrives at a check point of the movement
		if ((ticksToMove * GameTimeController.MILLIS_IN_TICK) > 3000)
			ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_ARRIVED_REVALIDATE), 2000);

		broadcastPacket(new MoveToLocation(this));
		return true;
	}
	
	public boolean validateMovementHeading(int heading)
	{
		MoveData md = _move;
		
		if (md == null)
			return true;
		
		boolean result = true;
		if (md._heading != heading)
		{
			result = (md._heading == 0);
			md._heading = heading;
		}
		
		return result;
	}
	
	public final double getDistance(int x, int y)
	{
		double dx = x - getX();
		double dy = y - getY();
		
		return Math.sqrt(dx * dx + dy * dy);
	}
	
	public final double getDistance(L2Character target)
	{
		double dx = target.getX() - getX();
		double dy = target.getY() - getY();
		
		return Math.sqrt(dx * dx + dy * dy);
	}
	
	@Deprecated
	public final double getDistance(int x, int y, int z)
	{
		double dx = x - getX();
		double dy = y - getY();
		double dz = z - getZ();
		
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}
	
	public final double getDistanceSq(L2Object object)
	{
		return getDistanceSq(object.getX(), object.getY(), object.getZ());
	}
	
	public final double getDistanceSq(int x, int y, int z)
	{
		double dx = x - getX();
		double dy = y - getY();
		double dz = z - getZ();
		
		return (dx * dx + dy * dy + dz * dz);
	}
	
	public final double getPlanDistanceSq(L2Object object)
	{
		return getPlanDistanceSq(object.getX(), object.getY());
	}
	
	public final double getPlanDistanceSq(int x, int y)
	{
		double dx = x - getX();
		double dy = y - getY();
		
		return (dx * dx + dy * dy);
	}

	public final boolean isInsideRadius(L2Object object, int radius, boolean checkZ, boolean strictCheck)
	{
		return isInsideRadius(object.getX(), object.getY(), object.getZ(), radius, checkZ, strictCheck);
	}
	
	public final boolean isInsideRadius(int x, int y, int radius, boolean strictCheck)
	{
		return isInsideRadius(x, y, 0, radius, false, strictCheck);
	}
	
	public final boolean isInsideRadius(int x, int y, int z, int radius, boolean checkZ, boolean strictCheck)
	{
		double dx = x - getX();
		double dy = y - getY();
		double dz = z - getZ();
		
		if (strictCheck)
		{
			if (checkZ)
				return (dx * dx + dy * dy + dz * dz) < radius * radius;
			return (dx * dx + dy * dy) < radius * radius;
		}
		if (checkZ)
			return (dx * dx + dy * dy + dz * dz) <= radius * radius;
		return (dx * dx + dy * dy) <= radius * radius;
	}
	
	public boolean isInRadius2d(L2Object pl, double radius)
	{
		return isInRadius2d(pl.getX(), pl.getY(), radius);
	}
	
	public boolean isInRadius2d(double x, double y, double radius)
	{
		return distance2d(x, y) <= radius;
	}
	
	public double distance2d(double x, double y)
	{
		return Math.sqrt(Math.pow(getX() - x, 2) + Math.pow(getY() - y, 2));
	}

	public float getWeaponExpertisePenalty()
	{
		return 1.f;
	}
	
	public float getArmourExpertisePenalty()
	{
		return 1.f;
	}
	
	public void setAttackingBodypart()
	{
		_attacking = Inventory.PAPERDOLL_CHEST;
	}
	
	protected boolean checkAndEquipArrows()
	{
		return true;
	}
	
	public void addExpAndSp(long addToExp, int addToSp)
	{
		// Dummy method (overridden by players and pets)
	}
	
	public abstract L2ItemInstance getActiveWeaponInstance();
	
	public abstract L2Weapon getActiveWeaponItem();
	
	public abstract L2ItemInstance getSecondaryWeaponInstance();
	
	public L2WeaponType getAttackType()
	{
		final L2Weapon weapon = getActiveWeaponItem();
		return (weapon == null) ? L2WeaponType.NONE : weapon.getItemType();
	}
	
	public abstract L2Item getSecondaryWeaponItem();
	
	protected void onHitTimer(L2Character target, int damage, boolean crit, boolean miss, boolean soulshot, byte shld)
	{
		if (isCastingNow())
			return;
		
		if (target == null || isAlikeDead() || (this instanceof L2Npc && ((L2Npc) this).isEventMob))
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		if ((this instanceof L2Npc && target.isAlikeDead()) || target.isDead())
		{
			// getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (miss)
		{
			if (target.getChanceSkills() != null)
				target.getChanceSkills().onEvadedHit(this);
			
			if (target.isPlayer())
				target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AVOIDED_S1_ATTACK).addCharName(this));
		}

		// If attack isn't aborted, send a message system (critical hit,
		// missed...) to attacker/target if they are L2PcInstance
		if (!isAttackAborted())
		{
			// Check Raidboss attack
			// Character will be petrified if attacking a raid that's more
			// than 8 levels lower
			if (target.isRaid() && !Config.RAID_DISABLE_CURSE && getActingPlayer() != null)
			{
				int level = getActingPlayer().getLevel();
				
				if (level > target.getLevel() + 8)
				{
					L2Skill skill = SkillTable.getInstance().getInfo(4515, 1);
					
					if (skill != null)
					{
						abortAttack();
						abortCast();
						getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
						skill.getEffects(target, this);
					}
					
					damage = 0; // prevents messing up drop calculation
				}
			}
			
			sendDamageMessage(target, damage, false, crit, miss);
					
			if (!miss && damage > 0)
			{
				// If L2Character target is a L2PcInstance, send a system message
				if (target instanceof L2PcInstance)
				{
					L2PcInstance enemy = (L2PcInstance) target;
					enemy.getAI().clientStartAutoAttack();
				}
				else if (target instanceof L2Summon)
				{
					L2Summon activeSummon = (L2Summon) target;
					
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PET_RECEIVED_S2_DAMAGE_BY_S1);
					sm.addString(getName());
					sm.addNumber(damage);
					activeSummon.getOwner().sendPacket(sm);
				}
				
				boolean isBow = (getAttackType() == L2WeaponType.BOW);
				
				if (!isBow) // Do not reflect or absorb if weapon is of type bow
				{
					// Reduce HP of the target and calculate reflection damage
					// to reduce HP of attacker if necessary
					double reflectPercent = target.getStat().calcStat(Stats.REFLECT_DAMAGE_PERCENT, 0, null, null);
					
					if (reflectPercent > 0)
					{
						int reflectedDamage = (int) (reflectPercent / 100. * damage);
						damage -= reflectedDamage;
						
						if (reflectedDamage > target.getMaxHp()) 
							reflectedDamage = target.getMaxHp();
						
						getStatus().reduceHp(reflectedDamage, target, true);
					}
					
					// Absorb HP from the damage inflicted
					double absorbPercent = getStat().calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0, null, null);
					
					if (absorbPercent > 0)
					{
						int maxCanAbsorb = (int) (getMaxHp() - getCurrentHp());
						int absorbDamage = (int) (absorbPercent / 100. * damage);
						
						if (absorbDamage > maxCanAbsorb)
							absorbDamage = maxCanAbsorb; // Can't absord more
						// than max hp
						
						if (absorbDamage > 0)
							setCurrentHp(getCurrentHp() + absorbDamage);
					}
				}
				
				target.reduceCurrentHp(damage, this);
				
				// Notify AI with EVT_ATTACKED
				target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
				getAI().clientStartAutoAttack();
				
				// Manage attack or cast break of the target (calculating rate,
				// sending message...)
				if (!target.isRaid() && !target.isBoss() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}
				
				if (_chanceSkills != null)
					_chanceSkills.onHit(target, false, crit);
				
				if (target.getChanceSkills() != null)
					target.getChanceSkills().onHit(this, true, crit);
			}
			
			// Launch weapon Special ability effect if available
			L2Weapon activeWeapon = getActiveWeaponItem();
			
			if (activeWeapon != null)
				activeWeapon.getSkillEffects(this, target, crit);
			
			return;
		}
		
		getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
	}
	
	public void breakAttack()
	{
		if (isAttackingNow())
		{
			// Abort the attack of the L2Character and send Server->Client
			// ActionFailed packet
			abortAttack();
			
			if (isPlayer())
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ATTACK_FAILED));
			}
		}
	}
	
	public void breakCast()
	{
		// damage can only cancel magical skills
		if (isCastingNow() && canAbortCast() && getLastSkillCast() != null && getLastSkillCast().isMagic())
		{
			// Abort the cast of the L2Character and send Server->Client
			// MagicSkillCanceld/ActionFailed packet.
			abortCast();
			
			if (isPlayer())
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CASTING_INTERRUPTED));
		}
	}
	
	protected void reduceArrowCount()
	{
		// default is to do nothin
	}
	
	@Override
	public void onForcedAttack(L2PcInstance player)
	{
		
		if (player.isInsidePeaceZone(player, this))
		{
			player.sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isInOlympiadMode() && player.getTarget() != null)
		{
			L2PcInstance target;
			if (player.getTarget() instanceof L2Summon)
				target = ((L2Summon) player.getTarget()).getOwner();
			else
				target = (L2PcInstance) player.getTarget();
			
			if (target == null || (target.isInOlympiadMode() && (!player.isOlympiadStart() || player.getOlympiadGameId() != target.getOlympiadGameId())))
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}

		if (player.getTarget() != null && !player.getTarget().isAttackable() && !player.getAccessLevel().allowPeaceAttack())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isConfused())
		{
			// If target is confused, send a Server->Client packet ActionFailed
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		
		if (this instanceof L2ArtefactInstance)
		{
			// If L2Character is a L2ArtefactInstance, send a Server->Client
			// packet ActionFailed
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		
		if (((Config.GEODATA) ? !GeoEngine.canSeeTarget(player, this, isFlying()) : !GeoEngine.canSeeTarget(player, this)))
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_SEE_TARGET));
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Notify AI with AI_INTENTION_ATTACK
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
	}
	
	public boolean isInsidePeaceZone(L2PcInstance attacker)
	{
		return isInsidePeaceZone(attacker, this);
	}
	
	public boolean isInsidePeaceZone(L2Object attacker, L2Object target)
	{
		if (target == null)
			return false;
		
		if (target instanceof L2MonsterInstance)
			return false;
		
		if (attacker instanceof L2MonsterInstance)
			return false;
		
		if (attacker instanceof L2GuardInstance)
			return false;
		
		// Summon or player check.
		if (attacker.getActingPlayer() != null && attacker.getActingPlayer().getAccessLevel().allowPeaceAttack())
			return false;
		
		if (Config.ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE)
		{
			// allows red to be attacked and red to attack flagged players
			if (target instanceof L2PcInstance && ((L2PcInstance) target).getKarma() > 0)
				return false;
			if (target instanceof L2Summon && ((L2Summon) target).getOwner().getKarma() > 0)
				return false;
			if (attacker instanceof L2PcInstance && ((L2PcInstance) attacker).getKarma() > 0)
			{
				if (target instanceof L2PcInstance && ((L2PcInstance) target).getPvpFlag() > 0)
					return false;
				if (target instanceof L2Summon && ((L2Summon) target).getOwner().getPvpFlag() > 0)
					return false;
			}
			if (attacker instanceof L2Summon && ((L2Summon) attacker).getOwner().getKarma() > 0)
			{
				if (target instanceof L2PcInstance && ((L2PcInstance) target).getPvpFlag() > 0)
					return false;
				if (target instanceof L2Summon && ((L2Summon) target).getOwner().getPvpFlag() > 0)
					return false;
			}
		}
		if (target instanceof L2Character)
			return (((L2Character) target).isInsideZone(ZoneId.PEACE) || (((L2Character) attacker).isInsideZone(ZoneId.PEACE)));
		
		return (MapRegionTable.getTown(target.getX(), target.getY(), target.getZ()) != null || (((L2Character) attacker).isInsideZone(ZoneId.PEACE)));
	}
	
	public Boolean isInActiveRegion()
	{
		try
		{
			L2WorldRegion region = L2World.getInstance().getRegion(getX(), getY(), getZ());
			return ((region != null) && (region.isActive()));
		}
		catch (Exception e)
		{
			if (this instanceof L2PcInstance)
			{
				_log.warning(L2Character.class.getName() + ":Player " + getName() + " at bad coords: (x: " + getX() + ", y: " + getY() + ", z: " + getZ() + ").");
				((L2PcInstance) this).sendMessage("Error with your coordinates! Please reboot your game fully!");
				((L2PcInstance) this).teleToLocation(80753, 145481, -3532, false); // Near
			}
			else
				decayMe();
			
			return false;
		}
	}
	
	public boolean isInParty()
	{
		return false;
	}
	
	public L2Party getParty()
	{
		return null;
	}

	public int calculateReuseTime(L2Character target, L2Weapon weapon)
	{
		if (weapon == null)
			return 0;
		
		int reuse = weapon.getAttackReuseDelay();
		// only bows should continue for now
		if (reuse == 0)
			return 0;
		// else if (reuse < 10) reuse = 1500;
		
		reuse *= getStat().getWeaponReuseModifier(target);
		double atkSpd = getStat().getPAtkSpd();
		switch (weapon.getItemType())
		{
			case BOW:
				return (int) (reuse * 345 / atkSpd);
			default:
				return (int) (reuse * 312 / atkSpd);
		}
	}
	
	public boolean isUsingDualWeapon()
	{
		return false;
	}
	
	public L2Skill addSkill(L2Skill newSkill)
	{
		L2Skill oldSkill = null;
		
		if (newSkill != null)
		{
			// Replace oldSkill by newSkill or Add the newSkill
			oldSkill = _skills.put(newSkill.getId(), newSkill);
			
			// If an old skill has been replaced, remove all its Func objects
			if (oldSkill != null)
			{		
				if (oldSkill.isPassive())
					stopSkillEffects(oldSkill.getId());
				
				if (oldSkill.triggerAnotherSkill())
					removeSkill(oldSkill.getTriggeredId());
				
				removeStatsOwner(oldSkill);				
			}
			
			// Add Func objects of newSkill to the calculator set of the
			// L2Character
			addStatFuncs(newSkill.getStatFuncs(null, this));
			
			if (oldSkill != null && _chanceSkills != null)
				removeChanceSkill(oldSkill.getId());

			if (newSkill.isChance())
				addChanceSkill(newSkill);	
		}
		
		return oldSkill;
	}
	
	public synchronized void addChanceSkill(L2Skill skill)
	{
		if (_chanceSkills == null)
			_chanceSkills = new ChanceSkillList(this);
		
		_chanceSkills.put(skill, skill.getChanceCondition());
	}
	
	public ChanceSkillList getChanceSkills()
	{
		return _chanceSkills;
	}
	
	public synchronized void removeChanceSkill(int id)
	{
		for (L2Skill skill : _chanceSkills.keySet())
		{
			if (skill.getId() == id)
				_chanceSkills.remove(skill);
		}
		
		if (_chanceSkills.isEmpty())
			_chanceSkills = null;
	}
	
	public L2Skill removeSkill(L2Skill skill)
	{
		if (skill == null)
			return null;
		
		// Remove the skill from the L2Character _skills
		return removeSkill(skill.getId());
	}
	
	public L2Skill removeSkill(int skillId)
	{
		// Remove the skill from the L2Character _skills
		L2Skill oldSkill = _skills.remove(skillId);
		// Remove all its Func objects from the L2Character calculator set
		if (oldSkill != null)
		{
			// Stop casting if this skill is used right now
			if (getLastSkillCast() != null && isCastingNow())
			{
				if (oldSkill.getId() == getLastSkillCast().getId())
					abortCast();
			}
			
			if (oldSkill.isChance() && _chanceSkills != null)
			{
				removeChanceSkill(oldSkill.getId());
			}
			removeStatsOwner(oldSkill);
		}
		return oldSkill;
	}
	
	public final L2Skill[] getAllSkills()
	{
		if (_skills == null)
			return new L2Skill[0];
		
		return _skills.values().toArray(new L2Skill[_skills.values().size()]);
	}
	
	public int getSkillLevel(int skillId)
	{
		final L2Skill skill = getKnownSkill(skillId);
		return (skill == null) ? -1 : skill.getLevel();
	}
	
	public L2Skill getKnownSkill(int skillId)
	{	
		return _skills.get(skillId);
	}
	
	public boolean hasSkill(int skillId)
	{
		return _skills != null && _skills.containsKey(skillId);
	}
	
	public int getBuffCount()
	{
		L2Effect[] effects = getAllEffects();
		int numBuffs = 0;
		if (effects != null)
		{
			for (L2Effect e : effects)
			{
				if (e != null)
				{
					if ((e.getSkill().getSkillType() == L2SkillType.BUFF || e.getSkill().getSkillType() == L2SkillType.DEBUFF || e.getSkill().getSkillType() == L2SkillType.REFLECT || e.getSkill().getSkillType() == L2SkillType.HEAL_PERCENT || e.getSkill().getSkillType() == L2SkillType.MANAHEAL_PERCENT) && !(e.getSkill().getId() > 4360 && e.getSkill().getId() < 4367))
					{ // 7s buffs
						numBuffs++;
					}
				}
			}
		}
		return numBuffs;
	}
	
	public void removeFirstBuff(int preferSkill)
	{
		L2Effect[] effects = getAllEffects();
		L2Effect removeMe = null;
		if (effects != null)
		{
			for (L2Effect e : effects)
			{
				if (e != null)
				{
					if ((e.getSkill().getSkillType() == L2SkillType.BUFF || e.getSkill().getSkillType() == L2SkillType.REFLECT || e.getSkill().getSkillType() == L2SkillType.HEAL_PERCENT || e.getSkill().getSkillType() == L2SkillType.MANAHEAL_PERCENT) && !(e.getSkill().getId() > 4360 && e.getSkill().getId() < 4367))
					{
						if (preferSkill == 0)
						{
							removeMe = e;
							break;
						}
						else if (e.getSkill().getId() == preferSkill)
						{
							removeMe = e;
							break;
						}
						else if (removeMe == null)
							removeMe = e;
					}
				}
			}
		}
		if (removeMe != null)
			removeMe.exit();
	}
	
	public void removeFirstDeBuff(int preferSkill)
	{
		L2Effect[] effects = getAllEffects();
		L2Effect removeMe = null;
		if (effects != null)
		{
			for (L2Effect e : effects)
			{
				if (e != null)
				{
					if (e.getSkill().getSkillType() == L2SkillType.DEBUFF)
					{
						if (preferSkill == 0)
						{
							removeMe = e;
							break;
						}
						else if (e.getSkill().getId() == preferSkill)
						{
							removeMe = e;
							break;
						}
						else if (removeMe == null)
							removeMe = e;
					}
				}
			}
		}
		if (removeMe != null)
			removeMe.exit();
	}
	
	public int getDanceCount()
	{
		int danceCount = 0;
		L2Effect[] effects = getAllEffects();
		for (L2Effect effect : effects)
		{
			if (effect == null)
				continue;
			if (effect.getSkill().isDance() && effect.getInUse())
				danceCount++;
		}
		return danceCount;
	}
	
	public void onMagicLaunchedTimer(L2Object[] targets, L2Skill skill, int coolTime, boolean instant)
	{
		if (skill == null)
		{
			_skillCast = null;
			enableAllSkills();
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		if (targets == null || targets.length == 0)
		{
			switch (skill.getTargetType())
			{
				case TARGET_SELF:
					targets = new L2Object[]{this};
					break;
				case TARGET_AURA:
				case TARGET_FRONT_AURA:
				case TARGET_BEHIND_AURA:
				case TARGET_AURA_UNDEAD:
				case TARGET_SIGNET:
					targets = skill.getTargetList(this);
					break;
				default:
					abortCast();
					return;
			}
		}
		
		// Escaping from under skill's radius and peace zone check. First
		// version, not perfect in AoE skills.
		int escapeRange = 0;
		if (skill.getEffectRange() > escapeRange)
			escapeRange = skill.getEffectRange();
		else if (skill.getCastRange() < 0 && skill.getSkillRadius() > 80)
			escapeRange = skill.getSkillRadius();
		
		if (escapeRange > 0 && skill.getTargetType() != L2SkillTargetType.TARGET_SIGNET)
		{
			int _range = 0;
			int _geo = 0;
			int _peace = 0;
			List<L2Character> targetList = new ArrayList<>();
			for (int i = 0; i < targets.length; i++)
			{
				if (targets[i] instanceof L2Character)
				{
					if (!Util.checkIfInRange(escapeRange, this, targets[i], true))
					{
						_range++;
						continue;
					}
					
					if (skill.getSkillRadius() > 0 && skill.isOffensive() && ((Config.GEODATA) ? !GeoEngine.canSeeTarget(this, targets[i], isFlying()) : !GeoEngine.canSeeTarget(this, targets[i])))
					{
						_geo++;
						continue;
					}
					
					if (skill.isOffensive() && isInsidePeaceZone(this, targets[i]))
					{
						_peace++;
						continue;
					}
					
					targetList.add((L2Character) targets[i]);
				}
			}
			if (targetList.isEmpty())
			{
				if (this instanceof L2PcInstance)
				{
					if (_range > 0)
						sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED));
					else if (_geo > 0)
						sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_SEE_TARGET));
					else if (_peace > 0)
						sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IN_PEACEZONE));
				}
				abortCast();
				return;
			}
			targets = targetList.toArray(new L2Character[targetList.size()]);
		}

		if (!isCastingNow() || (isAlikeDead() && !skill.isPotion()))
		{
			_skillCast = null;
			enableAllSkills();
			
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			
			_castEndTime = 0;
			_castInterruptTime = 0;
			return;
		}
		
		// Get the display identifier of the skill
		int magicId = skill.getDisplayId();
		
		int level = -1;
		 
		// Get the level of the skill
		if(hasSkill(skill.getId()))
		    level = getSkillLevel(skill.getId());
		
		if (level < 1)
			level = 1;
		
		// Send a Server->Client packet MagicSkillLaunched to the L2Character
		// AND to all L2PcInstance in the _KnownPlayers of the L2Character
		if (!skill.isPotion())
		    broadcastPacket(new MagicSkillLaunched(this, magicId, level, (targets == null || targets.length == 0) ? targets = new L2Object[0] : targets));
		
		if (instant)
			onMagicHitTimer(targets, skill, coolTime, true, 0);
		else
			_skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 2, 0), 200);
		
	}
	
	public void onMagicHitTimer(L2Object[] targets, L2Skill skill, int coolTime, boolean instant, int hitTime)
	{
		
		if (skill == null)
		{
			_skillCast = null;
			enableAllSkills();
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		boolean doit = false;
		switch (skill.getTargetType())
		{
			case TARGET_SELF:
				targets = new L2Object[]{this};
				break;
			case TARGET_AURA:
			case TARGET_SIGNET_GROUND:
			case TARGET_SIGNET:
			case TARGET_FRONT_AURA:
			case TARGET_BEHIND_AURA:
			case TARGET_AURA_UNDEAD:
			case TARGET_GROUND:
				doit = true;
				break;
			default:
				if (targets == null || targets.length == 0)
				{
					_skillCast = null;
					enableAllSkills();
					getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
					return;
				}
				break;
		}
		
		boolean forceBuff = skill.getSkillType() == L2SkillType.FORCE_BUFF;
		// For force buff skills, start the effect as long as the player is
		// casting.
		if (forceBuff)
			startForceBuff((L2Character) targets[0], skill);	
		try
		{
			// Go through targets table
			for (L2Object tgt : targets)
			{
				if (tgt instanceof L2Playable)
				{
					if (skill.getSkillType() == L2SkillType.BUFF || skill.getSkillType() == L2SkillType.FUSION || skill.getSkillType() == L2SkillType.SEED)
						((L2Character) tgt).sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill));
					
					if (this instanceof L2PcInstance && tgt instanceof L2Summon)
						((L2Summon) tgt).updateAndBroadcastStatus(1);
				}
			}
			
			StatusUpdate su = new StatusUpdate(getObjectId());
			boolean isSendStatus = false;
			
			// Consume MP of the L2Character and Send the Server->Client packet
			// StatusUpdate with current HP and MP to all other L2PcInstance to
			// inform
			double mpConsume = getStat().getMpConsume(skill);
			if (mpConsume > 0)
			{
				getStatus().reduceMp(calcStat(Stats.MP_CONSUME_RATE, mpConsume, null, null));
				su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
				isSendStatus = true;
			}
			
			// Consume HP if necessary and Send the Server->Client packet
			// StatusUpdate with current HP and MP to all other L2PcInstance to
			// inform
			if (skill.getHpConsume() > 0)
			{
				double consumeHp;
				
				consumeHp = calcStat(Stats.HP_CONSUME_RATE, skill.getHpConsume(), null, null);
				if (consumeHp + 1 >= getCurrentHp())
					consumeHp = getCurrentHp() - 1.0;
				
				getStatus().reduceHp(consumeHp, this);
				
				su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
				isSendStatus = true;
			}
			
			// Send a Server->Client packet StatusUpdate with MP modification to
			// the L2PcInstance
			if (isSendStatus)
				sendPacket(su);
			
			// Consume Items if necessary and Send the Server->Client packet
			// InventoryUpdate with Item modification to all the L2Character
			if (skill.getItemConsume() > 0)
				consumeItem(skill.getItemConsumeId(), skill.getItemConsume());
			
			// Launch the magic skill in order to calculate its effects
			if (!forceBuff)
				callSkill(skill, doit ? skill.getTargetList(this) : targets);
		}
		catch (NullPointerException e)
		{
		}
		
		if(targets == null)
		{
			_skillCast = null;
			enableAllSkills();
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}
		
		if (skill.getInitialEffectDelay() > 0)
			_skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 3, 0), hitTime);
		else if (instant || coolTime == 0)
			onMagicFinalizer(skill, doit && targets.length <= 0 ? skill.getFirstOfTargetList(this) : targets[0]);
		else
			_skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 3, 0), coolTime);
	}
	
	public void onMagicFinalizer(L2Skill skill, L2Object target)	
	{
		_skillCast = null;
		_castEndTime = 0;
		_castInterruptTime = 0;
		enableAllSkills();
		
		if (getForceBuff() != null)
			getForceBuff().delete();
		
		L2Effect mog = getFirstEffect(L2Effect.EffectType.SIGNET_GROUND);
		if (mog != null)
			mog.exit();
		
		// if the skill has changed the character's state to something other
		// than STATE_CASTING
		// then just leave it that way, otherwise switch back to STATE_IDLE.
		// if(isCastingNow())
		// getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
		
		// If the skill type is PDAM or DRAIN_SOUL, notify the AI of the target
		// with AI_INTENTION_ATTACK
		if (skill.getSkillType() == L2SkillType.PDAM || skill.getSkillType() == L2SkillType.BLOW || skill.getSkillType() == L2SkillType.DRAIN_SOUL || skill.getSkillType() == L2SkillType.SOW || skill.getSkillType() == L2SkillType.SPOIL)
		{
			if (getTarget() instanceof L2Character && getTarget() != this && target == getTarget())
				getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getTarget());
		}
		
		if (skill.isOffensive() && !(skill.getSkillType() == L2SkillType.UNLOCK) && !(skill.getSkillType() == L2SkillType.DELUXE_KEY_UNLOCK))
		{			
			getAI().clientStartAutoAttack();
			
			if (target.isPlayer())
			{				
				((L2PcInstance) target).getAI().clientStartAutoAttack();
				
				L2PcInstance player = null;
				
				if (this instanceof L2PcInstance)
					player = (L2PcInstance) this;
				else if (this instanceof L2Summon)
					player = ((L2Summon) this).getOwner();
				
				if (player != null)
					player.updatePvPStatus((L2PcInstance) target);			
			}
		}
		
		// Notify the AI of the L2Character with EVT_FINISH_CASTING
		getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING);
		
		if (this instanceof L2PcInstance)
		{
			L2PcInstance currPlayer = (L2PcInstance) this;
			SkillDat queuedSkill = currPlayer.getQueuedSkill();
			
			currPlayer.setCurrentSkill(null, false, false);
			
			if (queuedSkill != null)
			{
				currPlayer.setQueuedSkill(null, false, false);
				
				// DON'T USE : Recursive call to useMagic() method
				// currPlayer.useMagic(queuedSkill.getSkill(),
				// queuedSkill.isCtrlPressed(), queuedSkill.isShiftPressed());
				ThreadPoolManager.getInstance().executeTask(new QueuedMagicUseTask(currPlayer, queuedSkill.getSkill(), queuedSkill.isCtrlPressed(), queuedSkill.isShiftPressed()));
			}
		}
	}
	
	public void consumeItem(int itemConsumeId, int itemCount)
	{
	}

	public void enableSkill(int skillId)
	{
		if (_disabledSkills == null)
			return;
		
		_disabledSkills.remove(new Integer(skillId));
		
		if (this instanceof L2PcInstance)
			removeTimeStamp(skillId);
	}
	
	public void disableSkill(int skillId)
	{
		
		if (_disabledSkills == null)
			_disabledSkills = Collections.synchronizedList(new ArrayList<Integer>());
		
		_disabledSkills.add(skillId);
	}
	
	public void disableSkill(int skillId, long delay)
	{
		disableSkill(skillId);
		if (delay > 10)
			ThreadPoolManager.getInstance().scheduleAi(new EnableSkill(skillId), delay);
	}
	
	public boolean isSkillDisabled(int skillId)
	{
		if (isAllSkillsDisabled())
			return true;
		
		if (_disabledSkills == null)
			return false;
		
		return _disabledSkills.contains(skillId);
	}
	
	public void disableAllSkills()
	{
		_allSkillsDisabled = true;
	}
	
	public void enableAllSkills()
	{
		_allSkillsDisabled = false;
	}
	
	public void callSkill(L2Skill skill, L2Object[] targets)
	{
		if (skill == null || targets == null)
			return;
		try
		{
			// Check if the toggle skill effects are already in progress on the Creature
			if (skill.isToggle() && getFirstEffect(skill.getId()) != null)
				return;
			
			// Initial checks
			for (L2Object trg : targets)
			{
				if (!(trg instanceof L2Character))
					continue;
				
				// Set some values inside target's instance for later use
				final L2Character target = (L2Character) trg;
				
				if (this instanceof L2Playable)
				{
					
					// Raidboss curse.
					if (!Config.RAID_DISABLE_CURSE)
					{
						boolean isVictimTargetBoss = false;
						
						// If the skill isn't offensive, we check extra things such as target's target.
						if (!skill.isOffensive())
						{
							final L2Object victimTarget = (target.hasAI()) ? target.getAI().getTarget() : null;
							if (victimTarget != null)
								isVictimTargetBoss = victimTarget instanceof L2Character && ((L2Character) victimTarget).isRaid() && getLevel() > ((L2Character) victimTarget).getLevel() + 8;
						}
						
						// Target must be either a raid type, or if the skill is beneficial it checks the target's target.
						if ((target.isRaid() && getLevel() > target.getLevel() + 8) || isVictimTargetBoss)
						{
							final L2Skill curse = FrequentSkill.RAID_CURSE.getSkill();
							if (curse != null)
							{
								// Send visual and skill effects. Caster is the victim.
								broadcastPacket(new MagicSkillUse(this, this, curse.getId(), curse.getLevel(), 300, 0));
								curse.getEffects(this, this);
							}
							return;
						}
					}
					
					// Check if over-hit is possible
					if (skill.isOverhit() && target instanceof L2Attackable)
						((L2Attackable) target).overhitEnabled(true);
				}
				
				switch (skill.getSkillType())
				{
					case COMMON_CRAFT: // Crafting does not trigger any chance skills.
					case DWARVEN_CRAFT:
						break;
					
					default: // Launch weapon Special ability skill effect if available
						if (getActiveWeaponItem() != null && !target.isDead())
						{
							if (isPlayer() && getActiveWeaponItem().getSkillEffects(this, target, skill).length > 0)
								sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_ACTIVATED).addSkillName(skill));
						}
						
						// Maybe launch chance skills on us
						if (_chanceSkills != null)
							_chanceSkills.onSkillHit(target, false, skill.isMagic(), skill.isOffensive());
						
						// Maybe launch chance skills on target
						if (target._chanceSkills != null)
							target._chanceSkills.onSkillHit(this, true, skill.isMagic(), skill.isOffensive());
				}
			}
			
			// Launch the magic skill and calculate its effects
			final ISkillHandler handler = SkillHandler.getInstance().getHandler(skill.getSkillType());
			if (handler != null)
				handler.useSkill(this, skill, targets);
			else
				skill.useSkill(this, targets);
			
			L2PcInstance player = getActingPlayer();
			if (player != null)
			{
				for (L2Object target : targets)
				{
					// EVT_ATTACKED and PvPStatus
					if (target instanceof L2Character)
					{
						if (skill.isOffensive())
						{
							if(((L2Character) target).isDead())
								continue;
							
							if (target instanceof L2Playable)
							{
								// Signets are a special case, casted on target_self but don't harm self
								if (skill.getSkillType() != L2SkillType.SIGNET && skill.getSkillType() != L2SkillType.SIGNET_CASTTIME)
								{
									((L2Character) target).getAI().clientStartAutoAttack();
									
									// attack of the own pet does not flag player
									if (player.getPet() != target)
										player.updatePvPStatus((L2Character) target);
								}
							}
							// Add attacker into list
							else if (target instanceof L2Attackable && skill.getId() != 51)
								((L2Attackable) target).addAttackerToAttackByList(this);
							
							// notify target AI about the attack
							if (((L2Character) target).hasAI())
							{
								switch (skill.getSkillType())
								{
									case AGGREDUCE:
									case AGGREDUCE_CHAR:
									case AGGREMOVE:
										break;
									
									default:
										((L2Character) target).getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
								}
							}
						}
						else
						{
							if (target.isPlayer())
							{
								// Casting non offensive skill on player with pvp flag set or with karma
								if (!(target.equals(this) || target.equals(player)) && (((L2PcInstance) target).getPvpFlag() > 0 || ((L2PcInstance) target).getKarma() > 0))
									player.updatePvPStatus();
							}
							else if (target instanceof L2Attackable && !((L2Attackable) target).isGuard())
							{
								switch (skill.getSkillType())
								{
									case SUMMON:
									case BEAST_FEED:
									case UNLOCK:
									case DELUXE_KEY_UNLOCK:
										break;
									
									default:
										player.updatePvPStatus();
								}
							}
						}
						
						switch (skill.getTargetType())
						{
							case TARGET_CORPSE_MOB:
							case TARGET_AREA_CORPSE_MOB:
								if (((L2Character) target).isDead())
									((L2Npc) target).endDecayTask();
								break;
							default:
								break;
						}
					}
				}
				
				// Mobs in range 1000 see spell
				L2World.getInstance().forEachVisibleObjectInRange(player, L2Attackable.class, 1000, npcMob ->
				{
					if (npcMob != null)
					{

						final List<Quest> scripts = npcMob.getTemplate().getEventQuests(QuestEventType.ON_SKILL_SEE);
							if (scripts != null)
								for (Quest quest : scripts)
									quest.notifySkillSee(npcMob, player,skill, targets,this instanceof L2Summon);
					}

				});
			}
			
			// Notify AI
			if (skill.isOffensive())
			{
				switch (skill.getSkillType())
				{
					case AGGREDUCE:
					case AGGREDUCE_CHAR:
					case AGGREMOVE:
						break;
					
					default:
						for (L2Object target : targets)
						{
							if(((L2Character) target).isDead())
								continue;
							// notify target AI about the attack
							if (target instanceof L2Character && ((L2Character) target).hasAI())
								((L2Character) target).getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
						}
						break;
				}
			}
			
			if (skill.getAggroPoints() > 0)
			{
				for (L2Npc npcMob : L2World.getInstance().getVisibleObjects(player, L2Npc.class, 1000))
					if (npcMob != null && npcMob.hasAI() && !npcMob.isDead() && npcMob.getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
					{
						L2Object npcTarget = npcMob.getTarget();
						for (L2Object target : targets)
							if (npcTarget == target || npcMob == target)
								npcMob.seeSpell(player, target, skill);
					}
			}
		}
		catch (Exception e)
		{
			if(Config.DEBUG)
			  _log.warning(L2Character.class.getSimpleName() + " callSkill() failed on skill id: " + skill.getId() +" , " + e);
		}
	}
	
	public void seeSpell(L2PcInstance caster, L2Object target, L2Skill skill)
	{
		if (this instanceof L2Attackable)
			((L2Attackable) this).addDamageHate(caster, 0, -skill.getAggroPoints());
	}
	
	public boolean isBehindOf(L2Object target)
	{
		return target == null || getObjectId() == target.getObjectId() ? false : Position.getPosition(new Location(getX(),getY(),getZ()),target) == Position.BACK;
	}

	public boolean isBehindOfTarget()
	{
		return getTarget() == null || getObjectId() == getTarget().getObjectId() ? false : Position.getPosition(new Location(getX(),getY(),getZ()),getTarget()) == Position.BACK;
	}
	
	public boolean isFrontOf(L2Object target)
	{
		return target == null || getObjectId() == target.getObjectId() ? false : Position.getPosition(new Location(getX(),getY(),getZ()),target) == Position.FRONT;
	}
	
	public boolean isFrontOfTarget()
	{
		return getTarget() == null || getObjectId() == getTarget().getObjectId() ? false : Position.getPosition(new Location(getX(),getY(),getZ()),getTarget()) == Position.FRONT;
	}
	
	public boolean isSideOf(L2Object target)
	{
		return target == null || getObjectId() == target.getObjectId() ? false : Position.getPosition(new Location(getX(),getY(),getZ()),target) == Position.SIDE;
	}
	
	public boolean isSideOfTarget()
	{
		return isSideOf(getTarget());
	}
	
	public double getLevelMod()
	{
		return 1;
	}
	
	public final void setSkillCast(Future<?> newSkillCast)
	{
		_skillCast = newSkillCast;
	}
	
	public final void setSkillCastEndTime(int newSkillCastEndTime)
	{
		_castEndTime = newSkillCastEndTime;
		// for interrupt -12 ticks; first removing the extra second and then
		// -200 ms
		_castInterruptTime = newSkillCastEndTime - 12;
	}
	
	private boolean _isMinion = false;
	private boolean _AIdisabled = false;
	
	public final double getRandomDamage()
	{
		final L2Weapon activeWeapon = getActiveWeaponItem();
		int rdmg;
		
		if (activeWeapon != null)
			rdmg = activeWeapon.getRandomDamage();
		else
			rdmg = 5 + (int) Math.sqrt(getLevel());
		
		return (1 + ((double) Rnd.get(0 - rdmg, rdmg) / 100));
	}
	
	@Override
	public String toString()
	{
		return "mob " + getObjectId();
	}
	
	public int getAttackEndTime()
	{
		return _attackEndTime;
	}
	
	public abstract int getLevel();
	
	// =========================================================
	
	// =========================================================
	// Stat - NEED TO REMOVE ONCE L2CHARSTAT IS COMPLETE
	public final double calcStat(Stats stat, double init, L2Character target, L2Skill skill)
	{
		return getStat().calcStat(stat, init, target, skill);
	}
	
	public int getAccuracy()
	{
		return getStat().getAccuracy();
	}
	
	public final double getAttackSpeedMultiplier()
	{
		return getStat().getAttackSpeedMultiplier();
	}
	
	public final double getCriticalDmg(L2Character target, double init)
	{
		return getStat().getCriticalDmg(target, init);
	}
	
	public int getCriticalHit(L2Character target, L2Skill skill)
	{
		return getStat().getCriticalHit(target, skill);
	}
	
	public int getEvasionRate(L2Character target)
	{
		return getStat().getEvasionRate(target);
	}

	public final int getMagicalAttackRange(L2Skill skill)
	{
		return getStat().getMagicalAttackRange(skill);
	}
	
	public int getPAtk(L2Character target)
	{
		return getStat().getPAtk(target);
	}
	
	public final int getMaxCp()
	{
		return getStat().getMaxCp();
	}
	
	public int getMAtk(L2Character target, L2Skill skill)
	{
		return getStat().getMAtk(target, skill);
	}

	public int getMaxMp()
	{
		return getStat().getMaxMp();
	}
	
	public int getMaxHp()
	{
		return getStat().getMaxHp();
	}
	
	public final int getMCriticalHit(L2Character target, L2Skill skill)
	{
		return getStat().getMCriticalHit(target, skill);
	}
	
	public int getMDef(L2Character target, L2Skill skill)
	{
		return getStat().getMDef(target, skill);
	}

	public double getMReuseRate(L2Skill skill)
	{
		return getStat().getMReuseRate(skill);
	}
	
	public double getMovementSpeedMultiplier()
	{
		return getStat().getMovementSpeedMultiplier();
	}
	
	public double getPAtkAnimals(L2Character target)
	{
		return getStat().getPAtkAnimals(target);
	}
	
	public double getPAtkDragons(L2Character target)
	{
		return getStat().getPAtkDragons(target);
	}
	
	public double getPAtkInsects(L2Character target)
	{
		return getStat().getPAtkInsects(target);
	}
	
	public double getPAtkMonsters(L2Character target)
	{
		return getStat().getPAtkMonsters(target);
	}
	
	public double getPAtkGiants(L2Character target)
	{
		return getStat().getPAtkGiants(target);
	}
	
	public double getPAtkPlants(L2Character target)
	{
		return getStat().getPAtkPlants(target);
	}
		
	public int getMAtkSpd()
	{
		return getStat().getMAtkSpd();
	}
		
	public int getPAtkSpd()
	{
		return getStat().getPAtkSpd();
	}
	
	public double getPAtkUndead(L2Character target)
	{
		return getStat().getPAtkUndead(target);
	}
	
	public double getPDefUndead(L2Character target)
	{
		return getStat().getPDefUndead(target);
	}
	
	public int getPDef(L2Character target)
	{
		return getStat().getPDef(target);
	}
	
	public final int getPhysicalAttackRange()
	{
		return getStat().getPhysicalAttackRange();
	}

	public final int getShldDef()
	{
		return getStat().getShldDef();
	}
	
	
	public int getINT()
	{
		return getStat().getINT();
	}
	
	public int getSTR()
	{
		return getStat().getSTR();
	}
	
	public int getMEN()
	{
		return getStat().getMEN();
	}
	
	public int getCON()
	{
		return getStat().getCON();
	}	
	
	public int getWIT()
	{
		return getStat().getWIT();
	}
		
	public int getDEX()
	{
		return getStat().getDEX();
	}
	
	public int getRunSpeed()
	{
		return getStat().getRunSpeed();
	}
	
	public int getWalkSpeed()
	{
		return getStat().getWalkSpeed();
	}
		
	// =========================================================
	// Status - NEED TO REMOVE ONCE L2CHARTATUS IS COMPLETE
	public void addStatusListener(L2Character object)
	{
		getStatus().addStatusListener(object);
	}
	
	public void reduceCurrentHp(double i, L2Character attacker)
	{
		reduceCurrentHp(i, attacker, true);
	}
	
	public void reduceCurrentHp(double i, L2Character attacker, boolean awake)
	{
		getStatus().reduceHp(i, attacker, awake);
	}
	
	public void reduceCurrentMp(double i)
	{
		getStatus().reduceMp(i);
	}
	
	public void removeStatusListener(L2Character object)
	{
		getStatus().removeStatusListener(object);
	}
	
	protected void stopHpMpRegeneration()
	{
		getStatus().stopHpMpRegeneration();
	}
	
	public final double getCurrentCp()
	{
		return getStatus().getCurrentCp();
	}
	
	public final void setCurrentCp(Double newCp)
	{
		setCurrentCp((double) newCp);
	}
	
	public final void setCurrentCp(double newCp)
	{
		getStatus().setCurrentCp(newCp);
	}
	
	public final double getCurrentHp()
	{
		return getStatus().getCurrentHp();
	}
	
	public final int getCurrentHpPercent()
	{
		return (int) ((getCurrentHp() * 100) / getMaxHp());
	}
	
	public final void setCurrentHp(double newHp)
	{
		getStatus().setCurrentHp(newHp);
	}
	
	public final void setCurrentHpMp(double newHp, double newMp)
	{
		getStatus().setCurrentHpMp(newHp, newMp);
	}
	
	public final double getCurrentMp()
	{
		return getStatus().getCurrentMp();
	}
	
	public final void setCurrentMp(Double newMp)
	{
		setCurrentMp((double) newMp);
	}
	
	public final void setCurrentMp(double newMp)
	{
		getStatus().setCurrentMp(newMp);
	}
	
	// =========================================================
	
	public void setAiClass(String aiClass)
	{
		_aiClass = aiClass;
	}
	
	public String getAiClass()
	{
		return _aiClass;
	}
	
	public L2Character getLastBuffer()
	{
		return _lastBuffer;
	}
	
	public void setChampion(boolean champ)
	{
		_champion = champ;
	}
	
	public boolean isChampion()
	{
		return _champion;
	}
	
	public int getLastHealAmount()
	{
		return _lastHealAmount;
	}
	
	public void setLastBuffer(L2Character buffer)
	{
		_lastBuffer = buffer;
	}
	
	public void setLastHealAmount(int hp)
	{
		_lastHealAmount = hp;
	}
	
	public void sendDamageMessage(L2Character target, int damage, boolean mcrit, boolean pcrit, boolean miss)
	{
	}
	
	public ForceBuff getForceBuff()
	{
		return null;
	}
	
	public void disableCoreAI(boolean val)
	{
		_AIdisabled = val;
	}
	
	public boolean isCoreAIDisabled()
	{
		return _AIdisabled;
	}
	
	public boolean isRaidMinion()
	{
		return _isMinion;
	}
	
	public int getDeBuffCount()
	{
		L2Effect[] effects = getAllEffects();
		int numDeBuffs = 0;
		if (effects != null)
		{
			for (L2Effect e : effects)
			{
				if (e != null)
				{
					if (e.getSkill().getSkillType() == L2SkillType.DEBUFF)
					{ // 7s buffs
						numDeBuffs++;
					}
				}
			}
		}
		return numDeBuffs;
	}
	
	public void setIsRaidMinion(boolean val)
	{
		_isRaid = val;
		_isMinion = val;
	}

	public void setPremiumService(int PS)
	{
		_PremiumService = PS;
	}
	
	public int getPremiumService()
	{
		return _PremiumService;
	}
	
	public int getMaxBuffCount()
	{
		return Config.BUFFS_MAX_AMOUNT + Math.max(0, getSkillLevel(1405));
	}
	
	double _mobdmg = 0;
	
	public void setMobDamage(double d)
	{
		_mobdmg = d;
	}
	
	public double getMobDamage()
	{
		return _mobdmg;
	}
	
	L2Attackable _mob;
	
	public L2Attackable getLastMob()
	{
		return _mob;
	}
	
	public void setLastMob(L2Attackable m)
	{
		_mob = m;
	}

	public int getMoveSpeed()
	{
		if (isRunning())
			return getRunSpeed();
		
		return getWalkSpeed();
	}
	
	public void stopAllEffectsExceptThoseThatLastThroughDeath()
	{
		_effects.stopAllEffectsExceptThoseThatLastThroughDeath();
	}
	
	public boolean checkIfOkToUseStriderSiegeAssault(L2Character activeChar, Castle castle, boolean isCheckOnly)
	{
		if (activeChar == null || !(activeChar instanceof L2PcInstance))
			return false;
		
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_S2);
		L2PcInstance player = (L2PcInstance) activeChar;
		
		if (castle == null || castle.getCastleId() <= 0)
			sm.addString("You must be on castle ground to use strider siege assault");
		else if (!castle.getSiege().getIsInProgress())
			sm.addString("You can only use strider siege assault during a siege.");
		else if (!(player.getTarget() instanceof L2DoorInstance))
			sm.addString("You can only use strider siege assault on doors and walls.");
		else if (!activeChar.isRiding())
			sm.addString("You can only use strider siege assault when on strider.");
		else
			return true;
		
		if (!isCheckOnly)
		{
			player.sendPacket(sm);
		}
		return false;
	}
	
	public boolean checkIfOkToUseStriderSiegeAssault(L2Character activeChar, boolean isCheckOnly)
	{
		return checkIfOkToUseStriderSiegeAssault(activeChar, CastleManager.getInstance().getCastle(activeChar), isCheckOnly);
	}
	
	public boolean checkIfOkToPlaceFlag(L2Character activeChar, boolean isCheckOnly)
	{
		return checkIfOkToPlaceFlag(activeChar, CastleManager.getInstance().getCastle(activeChar), isCheckOnly);
	}
	
	public boolean checkIfOkToPlaceFlag(L2Character activeChar, Castle castle, boolean isCheckOnly)
	{
		if (activeChar == null || !(activeChar instanceof L2PcInstance))
			return false;
		
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_S2);
		L2PcInstance player = (L2PcInstance) activeChar;
		
		if (castle == null || castle.getCastleId() <= 0)
			sm.addString("You must be on castle ground to place a flag");
		else if (!castle.getSiege().getIsInProgress())
			sm.addString("You can only place a flag during a siege.");
		else if (castle.getSiege().getAttackerClan(player.getClan()) == null)
			sm.addString("You must be an attacker to place a flag");
		else if (player.getClan() == null || !player.isClanLeader())
			sm.addString("You must be a clan leader to place a flag");
		else if (castle.getSiege().getAttackerClan(player.getClan()).getNumFlags() >= SiegeManager.getInstance().getFlagMaxCount())
			sm.addString("You have already placed the maximum number of flags possible");
		else
			return true;
		
		if (!isCheckOnly)
		{
			player.sendPacket(sm);
		}
		return false;
	}
	
	
	public boolean isFacing(L2Object target, int maxAngle)
	{
		if (target == null)
			return false;
		
		double maxAngleDiff = maxAngle / 2;
		double angleTarget = MathUtil.calculateAngleFrom(this, target);
		double angleChar = MathUtil.convertHeadingToDegree(getHeading());
		double angleDiff = angleChar - angleTarget;
		
		if (angleDiff <= -360 + maxAngleDiff)
			angleDiff += 360;
		
		if (angleDiff >= 360 - maxAngleDiff)
			angleDiff -= 360;
		
		return Math.abs(angleDiff) <= maxAngleDiff;
	}
	
	public void deleteMe()
	{
		if (hasAI())
			getAI().stopAITask();
	}
	

	public final void stopEffectsOnDamage(boolean awake)
	{
		_effects.stopEffectsOnDamage(awake);
	}
	
	public void broadcastPacket(L2GameServerPacket mov)
	{
		Broadcast.toSelfAndKnownPlayers(this, mov);
	}
	
	public void broadcastPacket(L2GameServerPacket mov, int radius)
	{
		Broadcast.toSelfAndKnownPlayersInRadius(this, mov, radius);
	}
	
	public int getClanId()
	{
		return 0;
	}
	
	public int getAllyId()
	{
		return 0;
	}
	
	public void rechargeShots(boolean physical, boolean magic)
	{
		// Dummy method to be overriden.
	}
	
	@Override
	public void setXYZ(int x, int y, int z)
	{
		
		final ZoneRegion oldZoneRegion = ZoneManager.getInstance().getRegion(this);
		final ZoneRegion newZoneRegion = ZoneManager.getInstance().getRegion(x, y);
		
		if (oldZoneRegion != newZoneRegion)
		{
			oldZoneRegion.removeFromZones(this);
			newZoneRegion.revalidateZones(this);
		}
		
		super.setXYZ(x, y, z);
		
	}
	
	boolean _thinking;
	
	public boolean thinking()
	{
		return _thinking;
	}
	
	public void setThinking(boolean thinking)
	{
		_thinking = thinking;
	}
}