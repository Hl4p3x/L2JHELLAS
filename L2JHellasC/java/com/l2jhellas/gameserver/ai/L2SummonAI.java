package com.l2jhellas.gameserver.ai;

import static com.l2jhellas.gameserver.ai.CtrlIntention.AI_INTENTION_FOLLOW;
import static com.l2jhellas.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.geodata.GeoEngine;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.util.Rnd;

public class L2SummonAI extends L2CharacterAI
{
	private boolean _thinking; // to prevent recursive thinking
	
	public L2SummonAI(L2Summon accessor)
	{
		super(accessor);
	}
	
	@Override
	protected void onIntentionIdle()
	{
		stopFollow();
		onIntentionActive();
	}
	
	@Override
	protected void onIntentionActive()
	{
		final L2Summon summon = (L2Summon) _actor;
		if (summon.getFollowStatus())
			setIntention(AI_INTENTION_FOLLOW, summon.getOwner());
		else
			super.onIntentionActive();
	}
	
	private void thinkAttack()
	{
		final L2Summon summon = (L2Summon) _actor;
		final L2Character target = (L2Character) summon.getTarget();
		
		// L2OFF if the target is dead the summon must go back to his owner
		if (target != null  && target.isDead())
		{
			summon.setFollowStatus(true);
			return;
		}
		
		if (!checkTargetLostOrDead(target) && !maybeMoveToPawn(target, summon.getPhysicalAttackRange()))
			summon.doAttack(target,true);
	}
	
	private void thinkCast()
	{
		final L2Summon summon = (L2Summon) _actor;
		final L2Character target = (L2Character) summon.getTarget();

		// L2OFF if the target is dead the summon must go back to his owner
		if (target != null  && target.isDead())
		{
			summon.setFollowStatus(true);
			return;
		}
		
		if (!checkTargetLost(target) &&!maybeMoveToPawn(target, summon.getMagicalAttackRange(_skill)))
		{
		    summon.setFollowStatus(false);
		    summon.getAI().setIntention(AI_INTENTION_IDLE);
		    summon.doCast(_skill);
		}
	}
	
	private void thinkPickUp()
	{
		final L2Summon summon = (L2Summon) _actor;

		if (summon.isAllSkillsDisabled())
			return;
		if (checkTargetLost(getTarget()))
			return;
		if (maybeMoveToPawn(getTarget(), 36))
			return;
		setIntention(AI_INTENTION_IDLE);
		summon.doPickupItem(getTarget());
		return;
	}
	
	private void thinkInteract()
	{
		if (checkTargetLost(getTarget()))
			return;
		
		if (maybeMoveToPawn(getTarget(), 36))
			return;
		
		setIntention(AI_INTENTION_IDLE);
	}
	
	@Override
	protected void onEvtAttacked(L2Character attacker)
	{
		super.onEvtAttacked(attacker);
		
		final L2Summon summon = (L2Summon) _actor;
		
		if (summon != null)
			summon.getOwner().getAI().clientStartAutoAttack();
		
		avoidAttack();
	}
	
	@Override
	protected void onEvtFinishCasting()
	{
		final L2Summon summon = (L2Summon) _actor;
		final L2Character target = (L2Character) summon.getTarget();

		if (target == null)
		{
			summon.setFollowStatus(((L2Summon) _actor).getFollowStatus());
			return;
		}
		
		if(target.isDead())
			summon.setFollowStatus(true);
		
		if (_skill.isOffensive() && !(_skill.getSkillType() == L2SkillType.UNLOCK) && !(_skill.getSkillType() == L2SkillType.DELUXE_KEY_UNLOCK))
		{
			summon.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			summon.getOwner().getAI().clientStartAutoAttack();
		}
		else
			summon.setFollowStatus(((L2Summon) _actor).getFollowStatus());
		
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
		super.onEvtReadyToAct();
	}
	
	@Override
	protected void onIntentionCast(L2Skill skill, L2Object target)
	{
		super.onIntentionCast(skill, target);
	}
	
	private void avoidAttack()
	{		
		if (Config.GEODATA && ((L2Summon) _actor).getOwner() !=null && ((L2Summon) _actor).getOwner().isInsideRadius(_actor,150,false,false) && !_actor.isAttackingNow() && !_actor.isCastingNow() && !_clientMoving && !_actor.isDead() && !_actor.isMovementDisabled() && (_actor.getMoveSpeed() > 0))
		{
			final int ownerX = ((L2Summon) _actor).getOwner().getX();
			final int ownerY = ((L2Summon) _actor).getOwner().getY();
			final double angle = Math.toRadians(Rnd.get(-90, 90)) + Math.atan2(ownerY - _actor.getY(), ownerX - _actor.getX());
			
			final int targetX = ownerX + (int) (70 * Math.cos(angle));
			final int targetY = ownerY + (int) (70 * Math.sin(angle));
			
			if (GeoEngine.canMoveToCoord(_actor.getX(), _actor.getY(), _actor.getZ(), targetX, targetY, _actor.getZ()))
				moveTo(targetX, targetY, _actor.getZ());
		}
	}
	
	@Override
	protected void onEvtThink()
	{
		if (_thinking || _actor.isCastingNow() || _actor.isAllSkillsDisabled())
			return;
		
		_thinking = true;
		
		try
		{
			switch (getIntention())
			{
				case AI_INTENTION_ATTACK:
					thinkAttack();
					break;
				case AI_INTENTION_CAST:
					thinkCast();
					break;
				case AI_INTENTION_PICK_UP:
					thinkPickUp();
					break;
				case AI_INTENTION_INTERACT:
					thinkInteract();
					break;
				default :
					break;
			}
		}
		finally
		{
			_thinking = false;
		}
	}
}