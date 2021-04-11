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
	protected void onEvtAttacked(L2Character attacker,L2Skill skill)
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
		changeIntention(AI_INTENTION_IDLE, null, null);	
		setTarget(null);
		setAttackTarget(null);	
		clientStopMoving(null);	
		clientStopAutoAttack();		
	}
	
	@Override
	protected void onIntentionActive()
	{
		if (getIntention() != AI_INTENTION_ACTIVE)
		{
			changeIntention(AI_INTENTION_ACTIVE, null, null);
			
			setTarget(null);
			setAttackTarget(null);
			
			clientStopMoving(null);
			
			clientStopAutoAttack();
			
			if (_actor instanceof L2Attackable)
				((L2Npc) _actor).startRandomAnimationTimer();
			
			onEvtThink();
		}
	}
	
	@Override
	protected void onIntentionRest()
	{
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
			clientActionFailed();
			return;
		}
		
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow() || _actor.isAfraid())
		{
			clientActionFailed();
			return;
		}
		
		if (getIntention() == AI_INTENTION_ATTACK)
		{
			if (getAttackTarget() != target)
			{
				setAttackTarget(target);
				setTarget(target);

				stopFollow();
				
				notifyEvent(CtrlEvent.EVT_THINK, null);
				
			}
			else
				clientActionFailed(); 			
		}
		else
		{
			setTarget(target);
			setAttackTarget(target);
			
			stopFollow();

			changeIntention(AI_INTENTION_ATTACK, target, null);		
				
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
			clientActionFailed();
			return;
		}
		
		if (_actor.isMuted() && skill.isMagic())
		{
			clientActionFailed();
			return;
		}
		
		setTarget((L2Character) target);
		
		_skill = skill;
		
		changeIntention(AI_INTENTION_CAST, skill, target);
		
		notifyEvent(CtrlEvent.EVT_THINK, null);
	}
	
	@Override
	protected void onIntentionMoveTo(Location pos)
	{
		if (getIntention() == AI_INTENTION_REST)
		{
			clientActionFailed();
			return;
		}
		
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			clientActionFailed();
			return;
		}
		
		changeIntention(AI_INTENTION_MOVE_TO, pos, null);
		
		clientStopAutoAttack();
		
		_actor.abortAttack();
		
		moveTo(pos.getX(), pos.getY(), pos.getZ());
	}
	
	@Override
	protected void onIntentionFollow(L2Character target)
	{
		if (getIntention() == AI_INTENTION_REST)
		{
			clientActionFailed();
			return;
		}
		
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			clientActionFailed();
			return;
		}
		
		if (_actor.isMovementDisabled() || (_actor.getMoveSpeed() <= 0))
		{
			clientActionFailed();
			return;
		}
		
		if (_actor.isDead())
		{
			clientActionFailed();
			return;
		}
		
		if (_actor == target)
		{
			clientActionFailed();
			return;
		}
		
		clientStopAutoAttack();
		
		changeIntention(AI_INTENTION_FOLLOW, target, null);
		
		startFollow(target);
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
		
		moveToPawn(object, 20);
	}
	
	@Override
	protected void onIntentionInteract(L2Object object)
	{
		if (getIntention() == AI_INTENTION_REST)
		{
			clientActionFailed();
			return;
		}
		
		if (_actor.isAllSkillsDisabled() || _actor.isCastingNow())
		{
			clientActionFailed();
			return;
		}
		
		clientStopAutoAttack();
		
		if (getIntention() != AI_INTENTION_INTERACT)
		{
			changeIntention(AI_INTENTION_INTERACT, object, null);
			
			setTarget(object);
			
			moveToPawn(object, 60);
		}
	}
	
	@Override
	protected void onEvtThink()
	{
		
	}
	
	@Override
	protected void onEvtAggression(L2Character target, int aggro)
	{
		
	}
	
	@Override
	protected void onEvtStunned(L2Character attacker)
	{
		_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		
		AttackStanceTaskManager.getInstance().remove(_actor);

		clientStopMoving(null);
		
		onEvtAttacked(attacker,null);
	}
	
	@Override
	protected void onEvtSleeping(L2Character attacker)
	{
		_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		
		AttackStanceTaskManager.getInstance().remove(_actor);
		
		clientStopMoving(null);
	}
	
	@Override
	protected void onEvtRooted(L2Character attacker)
	{
		clientStopMoving(null);	
		onEvtAttacked(attacker,null);		
	}
	
	@Override
	protected void onEvtConfused(L2Character attacker)
	{
		clientStopMoving(null);		
		onEvtAttacked(attacker,null);
	}
	
	@Override
	protected void onEvtMuted(L2Character attacker)
	{
		onEvtAttacked(attacker,null);
	}
	
	@Override
	protected void onEvtReadyToAct()
	{
		onEvtThink();
	}
	
	@Override
	protected void onEvtUserCmd(Object arg0, Object arg1)
	{
	}
	
	@Override
	protected void onEvtArrived()
	{
		getActor().revalidateZone(true);
		
		if (getActor().moveToNextRoutePoint())
			return;
		
		clientStoppedMoving();
		
		if (getIntention() == AI_INTENTION_MOVE_TO)
			setIntention(AI_INTENTION_ACTIVE);
		
		onEvtThink();
	}
	
	@Override
	protected void onEvtArrivedRevalidate()
	{
		onEvtThink();
	}
	
	@Override
	protected void onEvtArrivedBlocked(Location blocked_at_pos)
	{
		if ((getIntention() == AI_INTENTION_MOVE_TO) || (getIntention() == AI_INTENTION_CAST))
			setIntention(AI_INTENTION_ACTIVE);
		
		clientStopMoving(blocked_at_pos);
		
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
		
		stopFollow();
		
		if (!AttackStanceTaskManager.getInstance().isInAttackStance(_actor))
			_actor.broadcastPacket(new AutoAttackStop(_actor.getObjectId()));
		
		onEvtThink();
	}
	
	@Override
	protected void onEvtDead()
	{
		stopFollow();
		
		clientNotifyDead();
		
		if (!(_actor instanceof L2PcInstance))
			_actor.setWalking();
	}
	
	@Override
	protected void onEvtFakeDeath()
	{
		stopFollow();
		
		clientStopMoving(null);
		
		_intention = AI_INTENTION_IDLE;
		setTarget(null);
		setAttackTarget(null);
	}
	
	@Override
	protected void onEvtFinishCasting()
	{
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
			
		if (!_actor.isInRadius2D(worldPosition.getX(), worldPosition.getY(), offset + _actor.getTemplate().collisionRadius))
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
			if (target != null && target.getActingPlayer() !=null && target.isFakeDeath())
			{
				target.getActingPlayer().stopFakeDeath(null);
				return false;
			}
			
			setIntention(AI_INTENTION_ACTIVE);
			clientActionFailed();
			return true;
		}
		return false;
	}
	
	protected boolean checkTargetLost(L2Object target)
	{
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
			clientActionFailed();
			return;
		}
		
		if (_actor.isAllSkillsDisabled())
		{
			clientActionFailed();
			return;
		}
		clientStopAutoAttack();
		
		_actor.abortAttack();
		
		moveToInABoat(destination, origin);
	}

	public void setGlobalAggro(int i)
	{
	}
	
	public void stopAITask()
	{
		stopFollow();
	}
}
