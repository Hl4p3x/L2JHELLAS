package com.l2jhellas.gameserver.ai;

import static com.l2jhellas.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;
import static com.l2jhellas.gameserver.ai.CtrlIntention.AI_INTENTION_FOLLOW;
import static com.l2jhellas.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;

import java.util.concurrent.Future;
import java.util.logging.Logger;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.controllers.GameTimeController;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.AutoAttackStart;
import com.l2jhellas.gameserver.network.serverpackets.AutoAttackStop;
import com.l2jhellas.gameserver.network.serverpackets.Die;
import com.l2jhellas.gameserver.network.serverpackets.MoveToLocation;
import com.l2jhellas.gameserver.network.serverpackets.MoveToLocationInVehicle;
import com.l2jhellas.gameserver.network.serverpackets.MoveToPawn;
import com.l2jhellas.gameserver.network.serverpackets.StopMove;
import com.l2jhellas.gameserver.taskmanager.AttackStanceTaskManager;

public abstract class AbstractAI implements Ctrl
{
	protected static final Logger _log = Logger.getLogger(AbstractAI.class.getName());
	
	protected final L2Character _actor;
		
	protected CtrlIntention _intention = AI_INTENTION_IDLE;
	
	protected Object _intentionArg0 = null;
	
	protected Object _intentionArg1 = null;
	
	protected boolean _clientMoving;
	
	protected int _clientMovingToPawnOffset;
	
	private L2Object _target;
	protected L2Character _attackTarget;
	protected L2Character _followTarget;
	
	protected L2Skill _skill;
	
	private int _moveToPawnTimeout;
	
	protected Future<?> _followTask = null;
	protected Future<?> _attackfollowTask = null;
	
	private static final int FOLLOW_INTERVAL = 1000;
	private static final int ATTACK_FOLLOW_INTERVAL = 500;
	
	private NextAction _nextAction;
	
	protected AbstractAI(L2Character character)
	{		
		_actor = character;
	}
	
	@Override
	public L2Character getActor()
	{
		return _actor;
	}
	
	@Override
	public CtrlIntention getIntention()
	{
		return _intention;
	}
	
	protected void setAttackTarget(L2Character target)
	{
		_attackTarget = target;
	}
	
	@Override
	public L2Character getAttackTarget()
	{
		return _attackTarget;
	}
	
	synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		_intention = intention;
		_intentionArg0 = arg0;
		_intentionArg1 = arg1;
	}
	
	@Override
	public final void setIntention(CtrlIntention intention)
	{
		setIntention(intention, null, null);
	}
	
	@Override
	public final void setIntention(CtrlIntention intention, Object arg0)
	{
		setIntention(intention, arg0, null);
	}
	
	@Override
	public final void setIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		if (!_actor.isVisible())
			return;
		
		// Stop the follow mode if necessary
		if (intention != AI_INTENTION_FOLLOW && intention != AI_INTENTION_ATTACK)
			stopFollow();
		
		// Launch the onIntention method of the L2CharacterAI corresponding to the new Intention
		switch (intention)
		{
			case AI_INTENTION_IDLE:
				onIntentionIdle();
				break;
			case AI_INTENTION_ACTIVE:
				onIntentionActive();
				break;
			case AI_INTENTION_REST:
				onIntentionRest();
				break;
			case AI_INTENTION_ATTACK:
				onIntentionAttack((L2Character) arg0);
				break;
			case AI_INTENTION_CAST:
				onIntentionCast((L2Skill) arg0, (L2Object) arg1);
				break;
			case AI_INTENTION_MOVE_TO:
				onIntentionMoveTo((Location) arg0);
				break;
			case AI_INTENTION_MOVE_TO_IN_A_BOAT:
				onIntentionMoveToInABoat((Location) arg0, (Location) arg1);
				break;
			case AI_INTENTION_FOLLOW:
				onIntentionFollow((L2Character) arg0);
				break;
			case AI_INTENTION_PICK_UP:
				onIntentionPickUp((L2Object) arg0);
				break;
			case AI_INTENTION_INTERACT:
				onIntentionInteract((L2Object) arg0);
				break;
		}
		
		if (_nextAction != null && _nextAction.getIntention() == intention)
			_nextAction = null;
	}
	
	@Override
	public final void notifyEvent(CtrlEvent evt)
	{
		notifyEvent(evt, null, null);
	}
	
	@Override
	public final void notifyEvent(CtrlEvent evt, Object arg0)
	{
		notifyEvent(evt, arg0, null);
	}
	
	@SuppressWarnings("incomplete-switch")
	@Override
	public final void notifyEvent(CtrlEvent evt, Object arg0, Object arg1)
	{
		if ((!_actor.isVisible() && !_actor.isTeleporting()) || !_actor.hasAI())
			return;
		
		switch (evt)
		{
			case EVT_THINK:
				onEvtThink();
				break;
			case EVT_ATTACKED:
				onEvtAttacked((L2Character) arg0,(L2Skill)arg1);
				break;
			case EVT_AGGRESSION:
				onEvtAggression((L2Character) arg0, ((Number) arg1).intValue());
				break;
			case EVT_STUNNED:
				onEvtStunned((L2Character) arg0);
				break;
			case EVT_SLEEPING:
				onEvtSleeping((L2Character) arg0);
				break;
			case EVT_ROOTED:
				onEvtRooted((L2Character) arg0);
				break;
			case EVT_CONFUSED:
				onEvtConfused((L2Character) arg0);
				break;
			case EVT_MUTED:
				onEvtMuted((L2Character) arg0);
				break;
			case EVT_READY_TO_ACT:
				if (!_actor.isCastingNow())
					onEvtReadyToAct();
				break;
			case EVT_USER_CMD:
				onEvtUserCmd(arg0, arg1);
				break;
			case EVT_ARRIVED:
				if (!_actor.isCastingNow())
					onEvtArrived();
				break;
			case EVT_ARRIVED_BLOCKED:
				onEvtArrivedBlocked((Location) arg0);
				break;
			case EVT_FORGET_OBJECT:
				onEvtForgetObject((L2Object) arg0);
				break;
			case EVT_CANCEL:
				onEvtCancel();
				break;
			case EVT_DEAD:
				onEvtDead();
				break;
			case EVT_FAKE_DEATH:
				onEvtFakeDeath();
				break;
			case EVT_FINISH_CASTING:
				onEvtFinishCasting();
				break;
		}
		
		if (_nextAction != null && _nextAction.getEvent() == evt)
		{
			_nextAction.run();
			_nextAction = null;
		}
	}
	
	protected abstract void onIntentionIdle();
	
	protected abstract void onIntentionActive();
	
	protected abstract void onIntentionRest();
	
	protected abstract void onIntentionAttack(L2Character target);
	
	protected abstract void onIntentionCast(L2Skill skill, L2Object target);
	
	protected abstract void onIntentionMoveTo(Location destination);
	
	protected abstract void onIntentionMoveToInABoat(Location destination, Location origin);
	
	protected abstract void onIntentionFollow(L2Character target);
	
	protected abstract void onIntentionPickUp(L2Object item);
	
	protected abstract void onIntentionInteract(L2Object object);
	
	protected abstract void onEvtThink();
	
	protected abstract void onEvtAttacked(L2Character attacker,L2Skill skill);
	
	protected abstract void onEvtAggression(L2Character target, int aggro);
	
	protected abstract void onEvtStunned(L2Character attacker);
	
	protected abstract void onEvtSleeping(L2Character attacker);
	
	protected abstract void onEvtRooted(L2Character attacker);
	
	protected abstract void onEvtConfused(L2Character attacker);
	
	protected abstract void onEvtMuted(L2Character attacker);
	
	protected abstract void onEvtReadyToAct();
	
	protected abstract void onEvtUserCmd(Object arg0, Object arg1);
	
	protected abstract void onEvtArrived();
		
	protected abstract void onEvtArrivedBlocked(Location blocked_at_pos);
	
	protected abstract void onEvtForgetObject(L2Object object);
	
	protected abstract void onEvtCancel();
	
	protected abstract void onEvtDead();
	
	protected abstract void onEvtFakeDeath();
	
	protected abstract void onEvtFinishCasting();
	
	protected void clientActionFailed()
	{

	}
	
	public void moveToPawn(L2Object pawn, int offset)
	{		
		if (!_actor.isMovementDisabled())
		{
			if (offset < 10)
				offset = 10;
			
			if (_clientMoving && (_target == pawn))
			{
				if (_clientMovingToPawnOffset == offset)
				{
					if (GameTimeController.getInstance().getGameTicks() < _moveToPawnTimeout)
						return;
				}
				else if (_actor.isOnGeodataPath())
				{
					if (GameTimeController.getInstance().getGameTicks() < (_moveToPawnTimeout + 15))
						return;
				}
			}
			
			_clientMoving = true;
			_clientMovingToPawnOffset = offset;
			_target = pawn;
			_moveToPawnTimeout = GameTimeController.getInstance().getGameTicks();
			_moveToPawnTimeout += 1000 / GameTimeController.MILLIS_IN_TICK;		

			if (pawn == null)
			{
				clientActionFailed();
				return;
			}
			
			if (_actor.isInsideRadius(pawn, offset, true,true))
				return;
			
			_actor.moveToLocation(pawn.getX(), pawn.getY(), pawn.getZ(), offset);

			if (!_actor.isMoving())
			{
				clientActionFailed();
				return;
			}
			
			if (pawn instanceof L2Character)
			{
				if (_actor.isOnGeodataPath())
				{
					_actor.broadcastPacket(new MoveToLocation(_actor));
					_clientMovingToPawnOffset = 0;
				}
				else
					_actor.broadcastPacket(new MoveToPawn(_actor, pawn, offset <= 10 ? _actor.getPhysicalAttackRange() : offset));
			}
			else
				_actor.broadcastPacket(new MoveToLocation(_actor));
		}
		else
			clientActionFailed();
	}
	
	public boolean maybeStartAttackFollow(L2Character target, int weaponAttackRange)
	{
		if (weaponAttackRange < 0)
			return false;
		
		if (_actor.isInRadius2D(target.getLoc(), (int) (weaponAttackRange + _actor.getTemplate().getCollisionRadius() + target.getTemplate().getCollisionRadius())))
			return false;
		
		if (!_actor.isMovementDisabled())
			startAttackFollow(target, weaponAttackRange);
		
		return true;
	}
	
	public void startAttackFollow(L2Character pawn, int offset)
	{
		if (_attackfollowTask != null)
		{
			_attackfollowTask.cancel(false);
			_attackfollowTask = null;
		}
		
		if(pawn.isDead())
			return;
		
		_attackfollowTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() -> AttackFollowTask(pawn, offset), 5, ATTACK_FOLLOW_INTERVAL);
	}
	
	protected void AttackFollowTask(L2Character target, int offset)
	{
		if (_attackfollowTask == null || target.isDead())
			return;
		
		final Location destination = target.getLoc().clone();
		final int realOffset = (int) (offset + _actor.getTemplate().getCollisionRadius() + target.getTemplate().getCollisionRadius());
		
		if ((!_actor.isFlying()) ? _actor.isInRadius2D(destination, realOffset) : _actor.isInRadius3D(destination, realOffset))
			return;

		StartAttackmoveTo(destination , target , offset);
	}
	
	public void StartAttackmoveTo(Location loc , L2Character target , int offset)
	{
		if(target == null)
			return;
		
		if (!_actor.isMovementDisabled())
		{
			if(!_actor.isAttacking() && !_actor.isCastingNow())
			{
				_actor.moveToLocation(loc.getX() , loc.getY() , loc.getZ(), offset-10);
				_actor.broadcastPacket(_actor.isOnGeodataPath() || target.isMoving() ? new MoveToLocation(_actor) : new MoveToPawn(_actor, target, offset-10));
			}
		}
		else
		    clientActionFailed();
	}
	
	public void moveTo(int x, int y, int z)
	{
		// Chek if actor can move
		if (!_actor.isMovementDisabled())
		{
			// Set AI movement data
			_clientMoving = true;
			_clientMovingToPawnOffset = 0;
			
			// Calculate movement data for a move to location action and add the actor to movingObjects of GameTimeController
			_actor.moveToLocation(x, y, z, 0);
			
			if(_actor.isMoving())
			   _actor.broadcastPacket(new MoveToLocation(_actor));
		}
		else
			clientActionFailed();
	}
	
	protected void moveToInABoat(Location destination, Location origin)
	{
		if (!_actor.isMovementDisabled())
		{
			if (((L2PcInstance) _actor).getBoat() != null)
			{
				MoveToLocationInVehicle msg = new MoveToLocationInVehicle(_actor, destination, origin);
				_actor.broadcastPacket(msg);
			}		
		}
		else
			_actor.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	protected void clientStopMoving(Location pos)
	{
		if (_actor.isMoving())
			_actor.stopMove(pos);
		
		_clientMovingToPawnOffset = 0;
		_clientMoving = false;
	}
	
	// Client has already arrived to target, no need to force StopMove packet
	protected void clientStoppedMoving()
	{
		if (_clientMovingToPawnOffset > 0) // movetoPawn needs to be stopped
		{
			_clientMovingToPawnOffset = 0;
			_actor.broadcastPacket(new StopMove(_actor));
		}
		_clientMoving = false;
	}

	public void clientStartAutoAttack()
	{		
		if (!AttackStanceTaskManager.getInstance().isInAttackStance(_actor))
		{
			_actor.broadcastPacket(new AutoAttackStart(_actor.getObjectId()));
			
			if (_actor instanceof L2PcInstance && ((L2PcInstance) _actor).getPet() != null)
				((L2PcInstance) _actor).getPet().broadcastPacket(new AutoAttackStart(((L2PcInstance) _actor).getPet().getObjectId()));

		}	
		AttackStanceTaskManager.getInstance().add(_actor);		
	}
	
	public void clientStopAutoAttack()
	{		
		if (AttackStanceTaskManager.getInstance().remove(_actor))
		{
			_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
			
			if (_actor instanceof L2PcInstance && ((L2PcInstance) _actor).getPet() != null)
				((L2PcInstance) _actor).getPet().broadcastPacket(new AutoAttackStop(((L2PcInstance) _actor).getPet().getObjectId()));
		}
	}
	
	protected  void clientNotifyDead()
	{
		_actor.broadcastPacket(new Die(_actor));
		
		// Init AI
		_intention = AI_INTENTION_IDLE;
		_target = null;
		_attackTarget = null;
		_followTarget = null;
		
		// Cancel the follow task if necessary
		stopFollow();
	}
	
	public void describeStateToPlayer(L2PcInstance player)
	{		
		if (_clientMoving)
		{
			if ((_clientMovingToPawnOffset != 0) && isFollowing())
				player.sendPacket(new MoveToPawn(_actor, _followTarget, _clientMovingToPawnOffset));
			else
				player.sendPacket(new MoveToLocation(_actor));
		}
	}
	
	public void setNextAction(NextAction nextAction)
	{
		_nextAction = nextAction;
	}
	
	public boolean isFollowing()
	{
		return (getTarget() instanceof L2Character) && (getIntention() == CtrlIntention.AI_INTENTION_FOLLOW);
	}
	
	class FollowTask implements Runnable
	{
		protected int _range = 60;
		
		public FollowTask()
		{
		}
		
		public FollowTask(int range)
		{
			_range = range;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (_followTask == null || getIntention() != CtrlIntention.AI_INTENTION_FOLLOW)
				{
					stopFollow();
					return;
				}
				
				if (_followTarget == null || _followTarget.isTeleporting())
				{
					stopFollow();
					setIntention(AI_INTENTION_IDLE);
					return;
				}
				
				if (_actor instanceof L2Summon && !_actor.isInsideRadius(_followTarget, _range, true, false))
				{
					moveToPawn(_followTarget, _range);
					return;
				}

				if (!_actor.isAttacking() && !_actor.isCastingNow() && !_actor.isInsideRadius(_followTarget, _range, true, false))
					moveToPawn(_followTarget, _range);
			}
			catch (Throwable t)
			{

			}
		}
	}
	
	public synchronized void startFollow(L2Character target)
	{
		if (_followTask != null)
		{
			_followTask.cancel(false);
			_followTask = null;
		}
		
		// Create and Launch an AI Follow Task to execute every 1s
		_followTarget = target;
		_followTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new FollowTask(), 5, FOLLOW_INTERVAL);
	}
	
	public synchronized void startFollow(L2Character target, int range)
	{
		if (_followTask != null)
		{
			_followTask.cancel(false);
			_followTask = null;
		}
		
		_followTarget = target;
		_followTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new FollowTask(range), 5, ATTACK_FOLLOW_INTERVAL);
	}
	
	public synchronized void stopFollow()
	{
		if (_followTask != null)
		{
			// Stop the Follow Task
			_followTask.cancel(false);
			_followTask = null;
		}
		
		if (_attackfollowTask != null)
		{
			// Stop the Follow Task
			_attackfollowTask.cancel(false);
			_attackfollowTask = null;
		}
				
		_followTarget = null;
	}
	
	protected L2Character getFollowTarget()
	{
		return _followTarget;
	}
	
	public L2Object getTarget()
	{
		return _target;
	}
	
	protected void setTarget(L2Object target)
	{
		_target = target;
	}
}