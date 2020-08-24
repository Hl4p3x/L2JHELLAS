package com.l2jhellas.gameserver.ai;

import static com.l2jhellas.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;
import static com.l2jhellas.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;
import static com.l2jhellas.gameserver.ai.CtrlIntention.AI_INTENTION_CAST;
import static com.l2jhellas.gameserver.ai.CtrlIntention.AI_INTENTION_FOLLOW;
import static com.l2jhellas.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;
import static com.l2jhellas.gameserver.ai.CtrlIntention.AI_INTENTION_INTERACT;
import static com.l2jhellas.gameserver.ai.CtrlIntention.AI_INTENTION_MOVE_TO;
import static com.l2jhellas.gameserver.ai.CtrlIntention.AI_INTENTION_PICK_UP;
import static com.l2jhellas.gameserver.ai.CtrlIntention.AI_INTENTION_REST;

import com.l2jhellas.gameserver.enums.items.ItemLocation;
import com.l2jhellas.gameserver.geometry.Point3D;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.network.serverpackets.AutoAttackStop;
import com.l2jhellas.gameserver.taskmanager.AttackStanceTaskManager;

public class L2CharacterAI extends AbstractAI
{
	@Override
	protected void onEvtAttacked(L2Character attacker)
	{
		clientStartAutoAttack();
	}
	
	public L2CharacterAI(L2Character character)
	{
		super(character);
	}
	
	public static class IntentionCommand
	{
		protected final CtrlIntention _crtlIntention;
		protected final Object _arg0, _arg1;
		
		protected IntentionCommand(CtrlIntention pIntention, Object pArg0, Object pArg1)
		{
			_crtlIntention = pIntention;
			_arg0 = pArg0;
			_arg1 = pArg1;
		}
		
		public CtrlIntention getCtrlIntention()
		{
			return _crtlIntention;
		}
	}
	
	@Override
	protected void onIntentionIdle()
	{
		// Set the AI Intention to AI_INTENTION_IDLE
		changeIntention(AI_INTENTION_IDLE, null, null);
		
		// Init cast and attack target
		setTarget(null);
		setAttackTarget(null);
		
		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);
		
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();
		
	}
	
	@Override
	protected void onIntentionActive()
	{
		// Check if the Intention is not already Active
		if (getIntention() != AI_INTENTION_ACTIVE)
		{
			// Set the AI Intention to AI_INTENTION_ACTIVE
			changeIntention(AI_INTENTION_ACTIVE, null, null);
			
			// Init cast and attack target
			setTarget(null);
			setAttackTarget(null);
			
			// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
			clientStopMoving(null);
			
			// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
			clientStopAutoAttack();
			
			// Also enable random animations for this L2Character if allowed
			// This is only for mobs - town npcs are handled in their constructor
			if (_actor instanceof L2Attackable)
				((L2Npc) _actor).startRandomAnimationTimer();
			
			// Launch the Think Event
			onEvtThink();
		}
	}
	
	@Override
	protected void onIntentionRest()
	{
		// Set the AI Intention to AI_INTENTION_IDLE
		setIntention(AI_INTENTION_IDLE);
	}
	
	@Override
	protected void onIntentionAttack(L2Character target)
	{
		if (target == null || !target.isVisible())
		{
			clientActionFailed();
			return;
		}
		
		if (getIntention() == AI_INTENTION_REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}
		
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow() || _actor.isAfraid())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}
		
		// Check if the Intention is already AI_INTENTION_ATTACK
		if (getIntention() == AI_INTENTION_ATTACK)
		{
			// Check if the AI already targets the L2Character
			if (getAttackTarget() != target)
			{
				// Set the AI attack target (change target)
				setAttackTarget(target);
				setTarget(target);

				stopFollow();
				
				// Launch the Think Event
				notifyEvent(CtrlEvent.EVT_THINK, null);
				
			}
			else
				clientActionFailed(); // else client freezes until cancel target			
		}
		else
		{
			setTarget(target);
			// Set the AI attack target
			setAttackTarget(target);
			
			stopFollow();

			// Set the Intention of this AbstractAI to AI_INTENTION_ATTACK
			changeIntention(AI_INTENTION_ATTACK, target, null);		
				
			// Launch the Think Event
			notifyEvent(CtrlEvent.EVT_THINK, null);
		}		
	}
	
	@Override
	protected void onIntentionCast(L2Skill skill, L2Object target)
	{
		if(skill==null || target ==null)
			return;
		
		if (getIntention() == AI_INTENTION_REST && skill.isMagic())
		{
			clientActionFailed();
			return;
		}
		
		if (_actor.isAllSkillsDisabled())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}
		
		// can't cast if muted
		if (_actor.isMuted() && skill.isMagic())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}
		
		// Set the AI cast target
		setTarget((L2Character) target);
		
		// Set the AI skill used by INTENTION_CAST
		_skill = skill;
		
		// Change the Intention of this AbstractAI to AI_INTENTION_CAST
		changeIntention(AI_INTENTION_CAST, skill, target);
		
		// Launch the Think Event
		notifyEvent(CtrlEvent.EVT_THINK, null);
	}
	
	@Override
	protected void onIntentionMoveTo(Location pos)
	{
		if (getIntention() == AI_INTENTION_REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}
		
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}
		
		// Set the Intention of this AbstractAI to AI_INTENTION_MOVE_TO
		changeIntention(AI_INTENTION_MOVE_TO, pos, null);
		
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();
		
		// Abort the attack of the L2Character and send Server->Client ActionFailed packet
		_actor.abortAttack();
		
		// Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast)
		moveTo(pos.getX(), pos.getY(), pos.getZ());
	}
	
	@Override
	protected void onIntentionFollow(L2Character target)
	{
		if (getIntention() == AI_INTENTION_REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}
		
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}
		
		if (_actor.isMovementDisabled() || (_actor.getMoveSpeed() <= 0))
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}
		
		// Dead actors can`t follow
		if (_actor.isDead())
		{
			clientActionFailed();
			return;
		}
		
		// do not follow yourself
		if (_actor == target)
		{
			clientActionFailed();
			return;
		}
		
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();
		
		// Set the Intention of this AbstractAI to AI_INTENTION_FOLLOW
		changeIntention(AI_INTENTION_FOLLOW, target, null);
		
		// Create and Launch an AI Follow Task to execute every 1s
		startFollow(target);
	}
	
	@Override
	protected void onIntentionPickUp(L2Object object)
	{
		if (getIntention() == AI_INTENTION_REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}
		
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow() || _actor.isAttackingNow())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}
		
		if (object instanceof L2ItemInstance && (((L2ItemInstance) object).getLocation() != ItemLocation.VOID))
			return;
		
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();
		
		// Set the Intention of this AbstractAI to AI_INTENTION_PICK_UP
		changeIntention(AI_INTENTION_PICK_UP, object, null);
		
		// Set the AI pick up target
		setTarget(object);
		if (object.getX() == 0 && object.getY() == 0)
		{
			_log.warning(L2CharacterAI.class.getName() + ": Object in coords 0,0 - using a temporary fix");
			object.setXYZ(getActor().getX(), getActor().getY(), getActor().getZ() + 5);
		}
		
		// Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)
		moveToPawn(object, 20);
	}
	
	@Override
	protected void onIntentionInteract(L2Object object)
	{
		if (getIntention() == AI_INTENTION_REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}
		
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}
		
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		clientStopAutoAttack();
		
		if (getIntention() != AI_INTENTION_INTERACT)
		{
			// Set the Intention of this AbstractAI to AI_INTENTION_INTERACT
			changeIntention(AI_INTENTION_INTERACT, object, null);
			
			// Set the AI interact target
			setTarget(object);
			
			// Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)
			moveToPawn(object, 60);
		}
	}
	
	@Override
	protected void onEvtThink()
	{
		// do nothing
	}
	
	@Override
	protected void onEvtAggression(L2Character target, int aggro)
	{
		// do nothing
	}
	
	@Override
	protected void onEvtStunned(L2Character attacker)
	{
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		
		AttackStanceTaskManager.getInstance().remove(_actor);

		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);
		
		// Launch actions corresponding to the Event onAttacked (only for L2AttackableAI after the stunning periode)
		onEvtAttacked(attacker);
	}
	
	@Override
	protected void onEvtSleeping(L2Character attacker)
	{
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		
		AttackStanceTaskManager.getInstance().remove(_actor);
		
		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);
	}
	
	@Override
	protected void onEvtRooted(L2Character attacker)
	{
		// Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
		// _actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		// if (AttackStanceTaskManager.getInstance().getAttackStanceTask(_actor))
		// AttackStanceTaskManager.getInstance().removeAttackStanceTask(_actor);
		
		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);
		
		// Launch actions corresponding to the Event onAttacked
		onEvtAttacked(attacker);
		
	}
	
	@Override
	protected void onEvtConfused(L2Character attacker)
	{
		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);
		
		// Launch actions corresponding to the Event onAttacked
		onEvtAttacked(attacker);
	}
	
	@Override
	protected void onEvtMuted(L2Character attacker)
	{
		// Break a cast and send Server->Client ActionFailed packet and a System Message to the L2Character
		onEvtAttacked(attacker);
	}
	
	@Override
	protected void onEvtReadyToAct()
	{
		// Launch actions corresponding to the Event Think
		onEvtThink();
	}
	
	@Override
	protected void onEvtUserCmd(Object arg0, Object arg1)
	{
		// do nothing
	}
	
	@Override
	protected void onEvtArrived()
	{
		getActor().revalidateZone(true);
		
		if (getActor().moveToNextRoutePoint())
			return;
		
		clientStoppedMoving();
		
		// If the Intention was AI_INTENTION_MOVE_TO, set the Intention to AI_INTENTION_ACTIVE
		if (getIntention() == AI_INTENTION_MOVE_TO)
			setIntention(AI_INTENTION_ACTIVE);
		
		// Launch actions corresponding to the Event Think
		onEvtThink();
	}
	
	@Override
	protected void onEvtArrivedRevalidate()
	{
		// Launch actions corresponding to the Event Think
		onEvtThink();
	}
	
	@Override
	protected void onEvtArrivedBlocked(Location blocked_at_pos)
	{
		// If the Intention was AI_INTENTION_MOVE_TO, let the Intention to AI_INTENTION_ACTIVE
		if ((getIntention() == AI_INTENTION_MOVE_TO) || (getIntention() == AI_INTENTION_CAST))
			setIntention(AI_INTENTION_ACTIVE);
		
		// Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(blocked_at_pos);
		
		// Launch actions corresponding to the Event Think
		onEvtThink();
	}
	
	@Override
	protected void onEvtForgetObject(L2Object object)
	{
		final L2Object target = getTarget();

		if (target == object)
		{
			setTarget(null);
			
			if (isFollowing())
			{
				clientStopMoving(null);
				stopFollow();
			}
			
			if (getIntention() != AI_INTENTION_MOVE_TO)
				setIntention(AI_INTENTION_ACTIVE);			
		}
		
		if (_actor == object)
		{
			setTarget(null);
			stopFollow();
			clientStopMoving(null);
			setIntention(AI_INTENTION_IDLE);
		}
	}
	
	@Override
	protected void onEvtCancel()
	{
		_actor.abortCast();
		
		// Stop an AI Follow Task
		stopFollow();
		
		if (!AttackStanceTaskManager.getInstance().isInAttackStance(_actor))
			_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		
		// Launch actions corresponding to the Event Think
		onEvtThink();
	}
	
	@Override
	protected void onEvtDead()
	{
		// Stop an AI Follow Task
		stopFollow();
		
		// Kill the actor client side by sending Server->Client packet AutoAttackStop, StopMove/StopRotation, Die (broadcast)
		clientNotifyDead();
		
		if (!(_actor instanceof L2PcInstance))
			_actor.setWalking();
	}
	
	@Override
	protected void onEvtFakeDeath()
	{
		// Stop an AI Follow Task
		stopFollow();
		
		// Stop the actor movement and send Server->Client packet StopMove/StopRotation (broadcast)
		clientStopMoving(null);
		
		// Init AI
		_intention = AI_INTENTION_IDLE;
		setTarget(null);
		setAttackTarget(null);
	}
	
	@Override
	protected void onEvtFinishCasting()
	{
		// do nothing
	}
	
	protected boolean maybeMoveToPosition(Point3D worldPosition, int offset)
	{
		if (worldPosition == null)
		{
			_log.warning(L2CharacterAI.class.getName() + ": maybeMoveToPosition: worldPosition == NULL!");
			return false;
		}
		
		if (offset < 0)
			return false; // skill radius -1
			
		if (!_actor.isInRadius2d(worldPosition.getX(), worldPosition.getY(), offset + _actor.getTemplate().collisionRadius))
		{
			if (_actor.isMovementDisabled() || (_actor.getMoveSpeed() <= 0))
				return true;
			
			if (!_actor.isRunning() && !(this instanceof L2PlayerAI) && !(this instanceof L2SummonAI))
				_actor.setRunning();
			
			stopFollow();
			
			int x = _actor.getX();
			int y = _actor.getY();
			
			double dx = worldPosition.getX() - x;
			double dy = worldPosition.getY() - y;
			
			double dist = Math.sqrt(dx * dx + dy * dy);
			
			double sin = dy / dist;
			double cos = dx / dist;
			
			dist -= offset - 5;
			
			x += (int) (dist * cos);
			y += (int) (dist * sin);
			
			moveTo(x, y, worldPosition.getZ());
			return true;
		}
		
		if (isFollowing())
			stopFollow();
		
		return false;
	}
	
	protected boolean maybeMoveToPawn(L2Object target, int offset)
	{
		if (target == null || offset < 0)
			return false;

		offset += _actor.getTemplate().getCollisionRadius();
		
		if (target instanceof L2Character)
			offset += ((L2Character) target).getTemplate().getCollisionRadius();
		
		if (!_actor.isInsideRadius(target, offset, false, false))
		{
			if (isFollowing())
			{
				if (!_actor.isInsideRadius(target, offset + 100, false, false))
					return true;
				
				stopFollow();
				
				return false;
			}

			if (_actor.isMovementDisabled() || (_actor.getMoveSpeed() <= 0))
			{
				if (_actor.getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
					_actor.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				
				return true;
			}
			
			if (!_actor.isRunning() && !(this instanceof L2PlayerAI))
				_actor.setRunning();
						
			stopFollow();
			
			if (target instanceof L2Character && !(target instanceof L2DoorInstance))
			{			
				if (isFollowing() || ((L2Character) target).isMoving())
					offset -= 100;
								
				if (offset < 5)
					offset = 5;	

				if (((L2Character) target).isMoving() && _actor.getStat().getMoveSpeed() <= 174 && _actor.isInsideRadius(target,_actor.getPhysicalAttackRange()+40, false, true))
				{
					if (isFollowing())
						stopFollow();
					
					return false;
				}
						
			    startFollow((L2Character) target, offset);
			}
			else
				moveToPawn(target, offset);				
			
			return true;
		}		
	
		if (isFollowing())
			stopFollow();
		
		return false;
	}
	
	protected boolean checkTargetLostOrDead(L2Character target)
	{
		if (target == null || target.isAlikeDead())
		{
			// check if player is fakedeath
			if (target != null && target.getActingPlayer() !=null && target.isFakeDeath())
			{
				target.getActingPlayer().stopFakeDeath(null);
				return false;
			}
			
			// Set the Intention of this AbstractAI to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE);
			clientActionFailed();
			return true;
		}
		return false;
	}
	
	protected boolean checkTargetLost(L2Object target)
	{
		// check if player is fakedeath
		if (target instanceof L2PcInstance)
		{
			L2PcInstance target2 = (L2PcInstance) target; // convert object to chara
			
			if (target2.isFakeDeath())
			{
				target2.stopFakeDeath(null);
				return false;
			}
		}
		if (target == null)
		{
			// Set the Intention of this AbstractAI to AI_INTENTION_ACTIVE
			setIntention(AI_INTENTION_ACTIVE);
			clientActionFailed();
			return true;
		}
		return false;
	}

	@Override
	protected void onIntentionMoveToInABoat(Location destination, Location origin)
	{
		if (getIntention() == AI_INTENTION_REST)
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}
		
		if (_actor.isAllSkillsDisabled())
		{
			// Cancel action client side by sending Server->Client packet ActionFailed to the L2PcInstance actor
			clientActionFailed();
			return;
		}
		clientStopAutoAttack();
		
		// Abort the attack of the L2Character and send Server->Client ActionFailed packet
		_actor.abortAttack();
		
		// Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast)
		moveToInABoat(destination, origin);
	}

	public void setGlobalAggro(int i)
	{
		// Gets overriten
	}
	
	public void stopAITask()
	{
		stopFollow();
	}
}
