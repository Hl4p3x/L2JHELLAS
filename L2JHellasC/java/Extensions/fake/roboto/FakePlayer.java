package Extensions.fake.roboto;

import Extensions.fake.roboto.ai.FakePlayerAI;
import Extensions.fake.roboto.helpers.FakeHelpers;

import com.l2jhellas.gameserver.enums.skills.L2SkillTargetType;
import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.geometry.Point3D;
import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.appearance.PcAppearance;
import com.l2jhellas.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.MoveToLocation;
import com.l2jhellas.gameserver.network.serverpackets.MoveToPawn;
import com.l2jhellas.gameserver.templates.L2PcTemplate;

public class FakePlayer extends L2PcInstance
{
	private FakePlayerAI _fakeAi;
	private boolean _underControl = false;
	
	public boolean isUnderControl()
	{
		return _underControl;
	}
	
	public void setUnderControl(boolean underControl)
	{
		_underControl = underControl;
	}
	
	protected FakePlayer(int objectId)
	{
		super(objectId);
	}
	
	public FakePlayer(int objectId, L2PcTemplate template, String accountName, PcAppearance app)
	{
		super(objectId, template, accountName, app);
	}
	
	public FakePlayerAI getFakeAi()
	{
		return _fakeAi;
	}
	
	public void setFakeAi(FakePlayerAI _fakeAi)
	{
		this._fakeAi = _fakeAi;
	}
	
	public void assignDefaultAI()
	{
		try
		{
			setFakeAi(FakeHelpers.getAIbyClassId(getClassId()).getConstructor(FakePlayer.class).newInstance(this));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean checkUseMagicConditions(L2Skill skill, boolean forceUse, boolean dontMove)
	{
		if (skill == null)
			return false;
		
		if (isDead() || isOutOfControl())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		L2SkillType sklType = skill.getSkillType();
		
		if (isSitting())
		{
			if (skill.isToggle())
			{
				L2Effect effect = getFirstEffect(skill.getId());
				if (effect != null)
				{
					effect.exit();
					return false;
				}
			}
			return false;
		}
		
		if (skill.isToggle())
		{
			L2Effect effect = getFirstEffect(skill.getId());
			
			if (effect != null)
			{
				if (skill.getId() != 60)
					effect.exit();
				
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		}
		
		if (isFakeDeath())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		L2Object target = null;
		L2SkillTargetType sklTargetType = skill.getTargetType();
		Point3D worldPosition = getCurrentSkillWorldPosition();
		
		if (sklTargetType == L2SkillTargetType.TARGET_GROUND && worldPosition == null)
		{
			_log.info("WorldPosition is null for skill: " + skill.getName() + ", player: " + getName() + ".");
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		switch (sklTargetType)
		{
			case TARGET_AURA:
			case TARGET_FRONT_AURA:
			case TARGET_BEHIND_AURA:
			case TARGET_AURA_UNDEAD:
			case TARGET_PARTY:
			case TARGET_ALLY:
			case TARGET_CLAN:
			case TARGET_GROUND:
			case TARGET_SELF:
			case TARGET_CORPSE_ALLY:
			case TARGET_AREA_SUMMON:
				target = this;
				break;
			case TARGET_PET:
			case TARGET_SUMMON:
				target = getPet();
				break;
			default:
				target = getTarget();
				break;
		}
		
		if (target == null)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (target instanceof L2DoorInstance)
		{
			if (!((L2DoorInstance) target).isAutoAttackable(this) // Siege doors only hittable during siege
				|| (((L2DoorInstance) target).isUnlockable() && skill.getSkillType() != L2SkillType.UNLOCK)) // unlockable doors
			{
				sendPacket(SystemMessageId.INCORRECT_TARGET);
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		}
		
		if (isInDuel())
		{
			if (target instanceof L2Playable)
			{
				L2PcInstance cha = target.getActingPlayer();
				if (cha.getDuelId() != getDuelId())
				{
					sendPacket(SystemMessageId.INCORRECT_TARGET);
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
		}
		
		if (!skill.checkCondition(this, target, false))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (skill.isOffensive())
		{
			if (isInsidePeaceZone(this, target))
			{
				sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			
			if (!target.isAutoAttackable(this) && !forceUse)
			{
				switch (sklTargetType)
				{
					case TARGET_AURA:
					case TARGET_FRONT_AURA:
					case TARGET_BEHIND_AURA:
					case TARGET_AURA_UNDEAD:
					case TARGET_CLAN:
					case TARGET_ALLY:
					case TARGET_PARTY:
					case TARGET_SELF:
					case TARGET_GROUND:
					case TARGET_CORPSE_ALLY:
					case TARGET_AREA_SUMMON:
						break;
					default:
						sendPacket(ActionFailed.STATIC_PACKET);
						return false;
				}
			}
			
			if (dontMove)
			{
				if (sklTargetType == L2SkillTargetType.TARGET_GROUND)
				{
					if (!isInsideRadius(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), skill.getCastRange() + getTemplate().getCollisionRadius(), false, false))
					{
						sendPacket(SystemMessageId.TARGET_TOO_FAR);
						sendPacket(ActionFailed.STATIC_PACKET);
						return false;
					}
				}
				else if (skill.getCastRange() > 0 && !isInsideRadius(target, skill.getCastRange() + getTemplate().getCollisionRadius(), false, false))
				{
					sendPacket(SystemMessageId.TARGET_TOO_FAR);
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
		}
		
		if (!skill.isOffensive() && target instanceof L2MonsterInstance && !forceUse)
		{
			switch (sklTargetType)
			{
				case TARGET_PET:
				case TARGET_SUMMON:
				case TARGET_AURA:
				case TARGET_FRONT_AURA:
				case TARGET_BEHIND_AURA:
				case TARGET_AURA_UNDEAD:
				case TARGET_CLAN:
				case TARGET_SELF:
				case TARGET_CORPSE_ALLY:
				case TARGET_PARTY:
				case TARGET_ALLY:
				case TARGET_CORPSE_MOB:
				case TARGET_AREA_CORPSE_MOB:
				case TARGET_GROUND:
					break;
				default:
				{
					switch (sklType)
					{
						case BEAST_FEED:
						case DELUXE_KEY_UNLOCK:
						case UNLOCK:
							break;
						default:
							sendPacket(ActionFailed.STATIC_PACKET);
							return false;
					}
					break;
				}
			}
		}
		if (sklType == L2SkillType.DRAIN_SOUL)
		{
			if (!(target instanceof L2MonsterInstance))
			{
				sendPacket(SystemMessageId.INCORRECT_TARGET);
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		}
		
		switch (sklTargetType)
		{
			case TARGET_PARTY:
			case TARGET_ALLY:
			case TARGET_CLAN:
			case TARGET_AURA:
			case TARGET_FRONT_AURA:
			case TARGET_BEHIND_AURA:
			case TARGET_AURA_UNDEAD:
			case TARGET_GROUND:
			case TARGET_SELF:
			case TARGET_CORPSE_ALLY:
			case TARGET_AREA_SUMMON:
				break;
			default:
				if (!checkPvpSkill(target, skill) && !getAccessLevel().allowPeaceAttack())
				{
					sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
		}
		
		return true;
	}
	
	protected boolean maybeMoveToPawn(L2Object target, int offset)
	{
		if (target == null)
			return false;
		
		if (offset < 10)
			offset = 10;
		
		offset += getTemplate().collisionRadius;
		
		if (target instanceof L2Character)
			offset += ((L2Character) target).getTemplate().collisionRadius;
		
		if (!isInsideRadius(target, offset, false, false))
		{
			moveToPawn(target, offset);
			return true;
		}
		
		return false;
	}
	
	protected void moveToPawn(L2Object pawn, int offset)
	{
		if (!isMovementDisabled())
		{
			if (offset < 10)
				offset = 10;
			
			if (pawn == null)
				return;
			
			moveToLocation(pawn.getX(), pawn.getY(), pawn.getZ(), offset);
			
			if (!isMoving())
				return;
			
			if (pawn instanceof L2Character)
			{
				if (isOnGeodataPath())
					broadcastPacket(new MoveToLocation(this));
				else
					broadcastPacket(new MoveToPawn(this, pawn, offset));
			}
			else
				broadcastPacket(new MoveToLocation(this));
		}
	}
	
	public void forceAutoAttack(L2Character target)
	{
		if (getTarget() == null)
			return;
		
		if (maybeMoveToPawn(target, getPhysicalAttackRange()))
			return;
		
		doAttack(target,true);
	}
	
	public synchronized void despawnPlayer()
	{
		deleteMe();
	}
	
	public void heal()
	{
		setCurrentCp(getMaxCp());
		setCurrentHp(getMaxHp());
		setCurrentMp(getMaxMp());
	}
}
