package com.l2jhellas.gameserver.ai;

import static com.l2jhellas.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;
import static com.l2jhellas.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;
import static com.l2jhellas.gameserver.ai.CtrlIntention.AI_INTENTION_CAST;
import static com.l2jhellas.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;
import static com.l2jhellas.gameserver.ai.CtrlIntention.AI_INTENTION_INTERACT;
import static com.l2jhellas.gameserver.ai.CtrlIntention.AI_INTENTION_PICK_UP;
import static com.l2jhellas.gameserver.ai.CtrlIntention.AI_INTENTION_REST;

import java.util.logging.Logger;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.enums.player.DuelState;
import com.l2jhellas.gameserver.enums.skills.L2SkillTargetType;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2StaticObjectInstance;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
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
		if (intention != AI_INTENTION_CAST || (arg0 != null && _skill.isOffensive()))
		{
			_nextIntention = null;
			super.changeIntention(intention, arg0, arg1);
			return;
		}
		// do nothing if next intention is same as current one.
		if ((intention == _intention) && (arg0 == _intentionArg0) && (arg1 == _intentionArg1))
		{
			super.changeIntention(intention, arg0, arg1);
			return;
		}
		// save current intention so it can be used after cast
		if ((_intention != AI_INTENTION_ACTIVE) && (_intention != AI_INTENTION_IDLE))
			saveNextIntention(_intention, _intentionArg0, _intentionArg1);
		
		super.changeIntention(intention, arg0, arg1);
	}

	@Override
	protected void onEvtAttacked(L2Character target)
	{
		if (target == null || _actor.isDead())
			return;
		
		if (_actor.getActingPlayer().getPet() != null)
			_actor.getActingPlayer().getPet().getAI().clientStartAutoAttack();
	}
	
	@Override
	protected void onEvtFinishCasting()
	{
		if (getIntention() == CtrlIntention.AI_INTENTION_CAST)
		{
			if (_nextIntention != null && _nextIntention.getCtrlIntention() != CtrlIntention.AI_INTENTION_CAST)
				setIntention(_nextIntention.getCtrlIntention(), _nextIntention._arg0, _nextIntention._arg1);
			else
				setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
		
		if (_skill != null)
		{
			if (_skill.useSoulShot())
				_actor.rechargeShots(true, false);
			else if (_skill.useSpiritShot())
				_actor.rechargeShots(false, true);
		}
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
		_nextIntention = null;
		super.onEvtCancel();
		_actor.sendPacket(ActionFailed.STATIC_PACKET);
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
		if (getIntention() != AI_INTENTION_ACTIVE)
		{
			final IntentionCommand nextIntention = _nextIntention;
			if (nextIntention != null)
			{
				_nextIntention = null;
				setIntention(nextIntention._crtlIntention, nextIntention._arg0, nextIntention._arg1);
			}
			else
				changeIntention(AI_INTENTION_ACTIVE, null, null);
		}
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

		if(!maybeMoveToPawn(target, _actor.getPhysicalAttackRange()))
			_actor.doAttack(target,false);
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

			if (target != null && maybeMoveToPawn(target, _actor.getMagicalAttackRange(_skill)))
				return;
		}

		if (_skill.getHitTime() > 50)
			clientStopMoving(null);

		_actor.doCast(_skill);

		return;
	}
	
	@Override
	protected void onIntentionMoveTo(Location loc)
	{
		if (getIntention() == AI_INTENTION_REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			_actor.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (_actor.getActingPlayer().getDuelState() == DuelState.DEAD)
		{
			_actor.getActingPlayer().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_MOVE_FROZEN));
			_actor.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow() || _actor.isAttackingNow())
		{
			saveNextIntention(CtrlIntention.AI_INTENTION_MOVE_TO, loc, null);
			_actor.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Set the Intention of this AbstractAI to AI_INTENTION_MOVE_TO
		changeIntention(CtrlIntention.AI_INTENTION_MOVE_TO, loc, null);
	
		// Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast)
		moveTo(loc.getX(), loc.getY(), loc.getZ());
	}
	
	private void thinkPickUp()
	{
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow() || _actor.isAttackingNow())
		{
			_actor.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		L2Object target = getTarget();
		
		if (checkTargetLost(target))
			return;
		
		if (maybeMoveToPawn(target, 36))
			return;
		
		if (_actor.isAlikeDead() || getActor().isFakeDeath())
			return;
		
		setIntention(AI_INTENTION_ACTIVE);
		_actor.getActingPlayer().doPickupItem(target);
		_actor.setIsParalyzed(true);
		ThreadPoolManager.getInstance().scheduleGeneral(() -> _actor.setIsParalyzed(false), (int) (660 / _actor.getStat().getMovementSpeedMultiplier()));		
		return;
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
			
		setIntention(AI_INTENTION_ACTIVE);	
		
		_actor.sendPacket(ActionFailed.STATIC_PACKET);	
	}
	
	@Override
	protected void onEvtThink()
	{
		
		if (_thinking || _actor.isCastingNow() || _actor.isAllSkillsDisabled())
		{
			_actor.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

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
	protected void onEvtArrivedRevalidate()
	{
		super.onEvtArrivedRevalidate();
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
	}
}