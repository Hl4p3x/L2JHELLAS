package com.l2jhellas.gameserver.ai;

import static com.l2jhellas.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;
import static com.l2jhellas.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;
import static com.l2jhellas.gameserver.ai.CtrlIntention.AI_INTENTION_CAST;
import static com.l2jhellas.gameserver.ai.CtrlIntention.AI_INTENTION_FOLLOW;
import static com.l2jhellas.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;
import static com.l2jhellas.gameserver.ai.CtrlIntention.AI_INTENTION_INTERACT;
import static com.l2jhellas.gameserver.ai.CtrlIntention.AI_INTENTION_PICK_UP;
import static com.l2jhellas.gameserver.ai.CtrlIntention.AI_INTENTION_REST;

import java.util.logging.Logger;

import com.l2jhellas.gameserver.enums.items.ItemLocation;
import com.l2jhellas.gameserver.enums.player.DuelState;
import com.l2jhellas.gameserver.enums.skills.L2SkillTargetType;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2CubicInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2StaticObjectInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.MoveToLocation;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public class L2PlayerAI extends L2CharacterAI
{
	protected static final Logger _log = Logger.getLogger(L2PlayerAI.class.getName());
	
	private boolean _thinking; // to prevent recursive thinking
	
	private IntentionCommand _nextIntention = null;
	
	void saveNextIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		_nextIntention = new IntentionCommand(intention, arg0, arg1);
	}
	
	public L2PlayerAI(L2PcInstance accessor)
	{
		super(accessor);
	}
	
	@Override
	synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		if(_intention == AI_INTENTION_ATTACK || _intention == AI_INTENTION_FOLLOW)
			_actor.sendPacket(ActionFailed.STATIC_PACKET);
		
		if (intention != AI_INTENTION_CAST || (arg0 != null && _skill.isOffensive()))
		{
			_nextIntention = null;
			super.changeIntention(intention, arg0, arg1);
			return;
		}
		if ((intention == _intention) && (arg0 == _intentionArg0) && (arg1 == _intentionArg1))
		{
			super.changeIntention(intention, arg0, arg1);
			return;
		}
		if ((_intention != AI_INTENTION_ACTIVE) && (_intention != AI_INTENTION_IDLE))
			saveNextIntention(_intention, _intentionArg0, _intentionArg1);
		
		super.changeIntention(intention, arg0, arg1);
	}

	@Override
	protected void onEvtAttacked(L2Character target,L2Skill skill)
	{
		if (target == null || _actor.isDead())
			return;
		
		if (_actor.getActingPlayer().getPet() != null)
			_actor.getActingPlayer().getPet().getAI().clientStartAutoAttack();
		
		if (!_actor.getActingPlayer().getCubics().isEmpty())
		{
			for (L2CubicInstance cubic : _actor.getActingPlayer().getCubics().values())
				if (cubic != null && cubic.getId() != L2CubicInstance.LIFE_CUBIC)
					cubic.doAction(target);
		}		
	}
	
	@Override
	protected void onEvtFinishCasting() 
	{
		if (_skill != null)
		{
			if (_skill.useSoulShot())
				_actor.rechargeShots(true, false);
			else if (_skill.useSpiritShot())
				_actor.rechargeShots(false, true);
		}
		
		if (getIntention() == AI_INTENTION_CAST) 
		{			
			IntentionCommand nextIntention = _nextIntention;
			if (nextIntention != null) 
			{
				if (nextIntention._crtlIntention != AI_INTENTION_CAST)
					setIntention(nextIntention._crtlIntention, nextIntention._arg0, nextIntention._arg1);
				else 
					setIntention(AI_INTENTION_IDLE);
			} 
			else 
				setIntention(AI_INTENTION_IDLE);
		}
	}

	@Override
	protected void onIntentionIdle()
	{
		changeIntention(AI_INTENTION_IDLE, null, null);	
		setTarget(null);
		setAttackTarget(null);	
		clientStopMoving(null);	
	}
	
	@Override
	protected void onEvtReadyToAct()
	{
		if (_nextIntention != null)
		{
			setIntention(_nextIntention._crtlIntention, _nextIntention._arg0, _nextIntention._arg1);
			_nextIntention = null;
		}	
		
		_actor.rechargeShots(true, false);
			
		super.onEvtReadyToAct();
	}
	
	@Override
	protected void onEvtCancel()
	{
		_actor.sendPacket(ActionFailed.STATIC_PACKET);
		_nextIntention = null;
		super.onEvtCancel();
	}
	
	@Override
	protected void onIntentionRest()
	{
		if (getIntention() != AI_INTENTION_REST)
		{
			changeIntention(AI_INTENTION_REST, null, null);
			setTarget(null);
			
			if (getAttackTarget() != null)
				setAttackTarget(null);
			
			clientStopMoving(null);			
		}
	}
	
	@Override
	protected void onIntentionActive()
	{
		setIntention(AI_INTENTION_IDLE);
	}

	@Override
	protected void clientNotifyDead()
	{
		_clientMovingToPawnOffset = 0;
		_clientMoving = false;

		super.clientNotifyDead();
	}
		
	private void thinkAttack()
	{
		L2Character target = getAttackTarget();

		if(checkTargetLostOrDead(target))
			return;

		if(maybeStartAttackFollow(target, _actor.getPhysicalAttackRange()))
		{
			if(target != null && target.isMoving() && _actor.isInRadius2D(target.getLoc(),_actor.getPhysicalAttackRange()+60))
			{
				_actor.getAI().stopFollow();
				_actor.getAI().clientStopMoving(null);
				_actor.doAttack(target);	
			}
			
			return;
		}
			
		_actor.doAttack(target);
	}
		
	private void thinkCast()
	{
		L2Character target = (L2Character) getTarget();
		
		if (_skill.getTargetType() == L2SkillTargetType.TARGET_SIGNET_GROUND && _actor instanceof L2PcInstance)
		{
			if (maybeMoveToPosition(((L2PcInstance) _actor).getCurrentSkillWorldPosition(), _actor.getMagicalAttackRange(_skill)))
				return;
		}
		else
		{
			if (checkTargetLost(target))
			{
				boolean isBad = _skill.isOffensive() || _skill.isDebuff();
				
				if (isBad && target != null)
					setTarget(null);
				
				_actor.sendPacket(ActionFailed.STATIC_PACKET);		
				return;
			}

		}

		if (target == null)
			return;
		
		if(maybeStartAttackFollow(target, _actor.getMagicalAttackRange(_skill)))
		{
			if(	target != null && target.isMoving() && _actor.isInRadius2D(target.getLoc(),_actor.getMagicalAttackRange(_skill)+95))
			{
				if (_skill.getHitTime() > 50)
					clientStopMoving(null);

				_actor.doCast(_skill);
			}		
		}
		else
		{
			if(	target != null)
			{
				if (_skill.getHitTime() > 50)
					clientStopMoving(null);

				_actor.doCast(_skill);
			}
		}
	}
	
	@Override
	protected void onIntentionMoveTo(Location loc)
	{
		if (getIntention() == AI_INTENTION_REST)
		{
			_actor.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (_actor.getActingPlayer().getDuelState() == DuelState.DEAD)
		{
			_actor.getActingPlayer().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_MOVE_FROZEN));
			_actor.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (_actor.MovementIsDisabled())
		{
			_actor.sendPacket(ActionFailed.STATIC_PACKET);
			saveNextIntention(CtrlIntention.AI_INTENTION_MOVE_TO, loc, null);
			return;
		}
		
		changeIntention(CtrlIntention.AI_INTENTION_MOVE_TO, loc, null);
	
		moveTo(loc.getX(), loc.getY(), loc.getZ());
	}
	
	@Override
	protected void onIntentionPickUp(L2Object object)
	{
		if (getIntention() == AI_INTENTION_REST)
		{
			clientActionFailed();
			return;
		}
		
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow() || _actor.isAttacking())
		{
			clientActionFailed();
			return;
		}
		
		if (object instanceof L2ItemInstance && (((L2ItemInstance) object).getLocation() != ItemLocation.VOID))
			return;
		
		clientStopAutoAttack();
		
		changeIntention(AI_INTENTION_PICK_UP, object, null);
		
		setTarget(object);
		if (object.getX() == 0 && object.getY() == 0)
		{
			_log.warning(L2CharacterAI.class.getName() + ": Object in coords 0,0 - using a temporary fix");
			object.setXYZ(getActor().getX(), getActor().getY(), getActor().getZ() + 5);
		}
		
		StartPickUpMove(object.getLoc().clone() , object , 0);		
	}
	
	private void thinkPickUp()
	{
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow() || _actor.isAttacking() || _actor.isAlikeDead() || getActor().isFakeDeath())
		{
			_actor.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		L2Object target = getTarget();

		if (checkTargetLost(target))
			return;

		if (tryPickUpMove(target))
			return;

		_actor.getActingPlayer().doPickupItem(target);
	}
	
	protected boolean tryPickUpMove(L2Object target)
	{
		int offset = 36;
		final Location destination = target.getLoc().clone();
		if (_actor.isInRadius3D(destination, offset))
			return false;
		
		StartPickUpMove(destination , target , offset);			
		return true;
	}

	public void StartPickUpMove(Location loc , L2Object target , int offset)
	{
		if(target == null)
			return;
		
		// Chek if actor can move
		if (!_actor.isMovementDisabled())
		{
			if(!_actor.isAttacking() && !_actor.isCastingNow())
			{
				// Calculate movement data for a move to location action and add the actor to movingObjects of GameTimeController
				_actor.moveToLocation(loc.getX() , loc.getY() , loc.getZ(), offset);

				_actor.broadcastPacket(new MoveToLocation(_actor));
			}
		}
		else
		    clientActionFailed();
	}
	
	@Override
	protected void onIntentionInteract(L2Object object)
	{
		if (getIntention() == CtrlIntention.AI_INTENTION_REST)
		{
			_actor.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			_actor.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		changeIntention(CtrlIntention.AI_INTENTION_INTERACT, object, null);
		
		setTarget(object);
		
		moveToPawn(object, 60);		
		
		_actor.sendPacket(ActionFailed.STATIC_PACKET);	
	}
	
	@Override
	protected void onIntentionAttack(L2Character target)
	{
		super.onIntentionAttack(target);
	}
	
	private void thinkInteract()
	{
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			_actor.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
			
		L2Object target = getTarget();
		
		if (checkTargetLost(target))
			return;
		
		if (maybeMoveToPawn(target, 40))
			return;
		
		if (!(target instanceof L2StaticObjectInstance))
			_actor.getActingPlayer().doInteract((L2Character) target);
			
		setIntention(AI_INTENTION_IDLE);
		
		_actor.sendPacket(ActionFailed.STATIC_PACKET);	
	}
	
	@Override
	protected void onEvtThink()
	{		
		if (_thinking || _actor.isCastingNow() || _actor.isAllSkillsDisabled())
			return;

		_thinking = true;
		
		try
		{
			if (getIntention() == AI_INTENTION_ATTACK)
				thinkAttack();
			else if (getIntention() == AI_INTENTION_CAST)
				thinkCast();
			else if (getIntention() == AI_INTENTION_PICK_UP)
				thinkPickUp();
			else if (getIntention() == AI_INTENTION_INTERACT)
				thinkInteract();
		}
		finally
		{
			_thinking = false;
		}
	}
	
	@Override
	protected void clientActionFailed()
	{
		_actor.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	protected void onEvtForgetObject(L2Object object)
	{
		super.onEvtForgetObject(object);
	}
	
	@Override
	protected void onEvtArrived()
	{
		if (_nextIntention != null)
		{
			setIntention(_nextIntention._crtlIntention, _nextIntention._arg0, _nextIntention._arg1);
			_nextIntention = null;
		}
		
		super.onEvtArrived();	
		_actor.sendPacket(ActionFailed.STATIC_PACKET);
	}
}