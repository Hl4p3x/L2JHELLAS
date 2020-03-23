package com.l2jhellas.gameserver.ai;

import static com.l2jhellas.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;
import static com.l2jhellas.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;
import static com.l2jhellas.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.controllers.GameTimeController;
import com.l2jhellas.gameserver.enums.ZoneId;
import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.geodata.GeoEngine;
import com.l2jhellas.gameserver.instancemanager.DimensionalRiftManager;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Attackable.AggroInfo;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2FestivalMonsterInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2FriendlyMobInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2GuardInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2RaidBossInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2RiftInvaderInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2SiegeGuardInstance;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.model.quest.QuestEventType;
import com.l2jhellas.util.Rnd;

public class L2AttackableAI extends L2CharacterAI
{	
	private static final int RANDOM_WALK_RATE = 30; 
	private static final int MAX_ATTACK_TIMEOUT = 1200;

	private Future<?> _aiTask;

	private int _attackTimeout;

	private int _globalAggro;
	
	private int chaostime = 0;
	int lastBuffTick;
	
	public L2AttackableAI(L2Character accessor)
	{
		super(accessor);
		_attackTimeout = Integer.MAX_VALUE;
		_globalAggro = -10;
	}

	private boolean autoAttackCondition(L2Character target)
	{
		if ((target == null) || (getActiveChar() == null))
			return false;
				
		if (target.isInvul())
			return false;
		
		if (target instanceof L2DoorInstance)
			return false;
		
		final L2Attackable me = getActiveChar();
		
		if (target.isAlikeDead())
			return false;

		final L2PcInstance player = target.getActingPlayer();
		
		if (player != null)
		{			
			if (player.isGM() && !player.getAccessLevel().canTakeAggro())
				return false;

			if (player.isRecentFakeDeath())
				return false;
			
			if (!(me.isRaid()) && !(me.canSeeThroughSilentMove()) && player.isSilentMoving())
				return false;

			if(me instanceof L2SiegeGuardInstance)
			{			
				final L2SiegeGuardInstance sGuard = (L2SiegeGuardInstance) me;

				// Check if the target isn't another guard, folk or a door
				if (target instanceof L2SiegeGuardInstance || target instanceof L2NpcInstance || target instanceof L2DoorInstance || target.isAlikeDead() || target.isInvul())
					return false;
				
				// Get the owner if the target is a summon
				if (target instanceof L2Summon)
				{
					L2PcInstance owner = ((L2Summon) target).getOwner();
					if (sGuard.isInsideRadius(owner, 1000, true, false))
						target = owner;
				}

				if (sGuard.getCastle().getSiege().checkIsDefender(player.getClan()))
				{
					sGuard.stopHating(player);
					return false;
				}
					
				if (player.isSilentMoving() && !sGuard.isInsideRadius(player, 250, false, false))
					return false;						
			}
			
			if (me instanceof L2GuardInstance)
			{
				L2World.getInstance().forEachVisibleObjectInRange(me, L2GuardInstance.class, 600, guard ->
				{
					if (guard.isAttackingNow() && (guard.getTarget() == player))
					{
						if(player.getKarma() > 0)
						{
						   me.getAI().startFollow(player);
						   me.addDamageHate(player, 0, 10);
						}
					}
				});
				
				if (player.getKarma() > 0)
					return ((Config.GEODATA) ? GeoEngine.canSeeTarget(me, target, me.isFlying()) : GeoEngine.canSeeTarget(me, target));
			}
			
			if ("varka".equals(me.getFactionId()) && player.isAlliedWithVarka())
				return false;
			if ("ketra".equals(me.getFactionId()) && player.isAlliedWithKetra())
				return false;
			
			if (player.isInParty() && player.getParty().isInDimensionalRift())
			{
				byte riftType = player.getParty().getDimensionalRift().getType();
				byte riftRoom = player.getParty().getDimensionalRift().getCurrentRoom();
				
				if (me instanceof L2RiftInvaderInstance && !DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(me.getX(), me.getY(), me.getZ()))
					return false;
			}
		}	
		
		if (me instanceof L2MonsterInstance)
		{
			if (!Config.ALT_MOB_AGRO_IN_PEACEZONE && target.isInsideZone(ZoneId.PEACE))
				return false;
			
			if (me.isChampion() && Config.CHAMPION_PASSIVE)
				return false;
			
			if (!me.isAggressive())
				return false;
		}
		
		if (me instanceof L2FriendlyMobInstance)
		{ 	
			if (target instanceof L2Npc)
				return false;
			
			if (target instanceof L2PcInstance && ((L2PcInstance) target).getKarma() > 0)
				return ((Config.GEODATA) ? GeoEngine.canSeeTarget(me, target, me.isFlying()) : GeoEngine.canSeeTarget(me, target)); // Los Check
				
			return false;
		}

		if (target instanceof L2Summon)
		{
			L2PcInstance owner = ((L2Summon) target).getOwner();
			if (owner != null)
			{
				// Don't take the aggro if the GM has the access level below or equal to GM_DONT_TAKE_AGGRO
				if (owner.isGM() && (owner.isInvul() || !owner.getAccessLevel().canTakeAggro()))
					return false;
				// Check if player is an ally (comparing mem addr)
				if ("varka".equals(me.getFactionId()) && owner.isAlliedWithVarka())
					return false;
				if ("ketra".equals(me.getFactionId()) && owner.isAlliedWithKetra())
					return false;
			}
		}
		
		if (target instanceof L2Attackable)
		{
			if (!Config.ALLOW_GUARDS && me instanceof L2GuardInstance)
				return false;
			
			if (!target.isAutoAttackable(me))
				return false;
			
			if (!Config.ALT_MOB_AGRO_IN_PEACEZONE && target.isInsidePeaceZone(_actor.getActingPlayer()))
				return false;		
		}
		
		return target.isAutoAttackable(me) && ((Config.GEODATA) ? GeoEngine.canSeeTarget(me, target, me.isFlying()) : GeoEngine.canSeeTarget(me, target));
	}
	
	public void startAITask()
	{
		if (_aiTask == null)
			_aiTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this::onEvtThink, 1100, 1100);
	}
	
	@Override
	public void stopAITask()
	{
		if (_aiTask != null)
		{
			_aiTask.cancel(false);
			_aiTask = null;
		}
		super.stopAITask();
	}

	@Override
	synchronized void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{				
		if (intention == AI_INTENTION_IDLE)
		{
			if (!getActiveChar().isAlikeDead() && getActiveChar().isInActiveRegion())			
				intention = AI_INTENTION_ACTIVE;
			
			if (intention == AI_INTENTION_IDLE)
			{
				super.changeIntention(AI_INTENTION_IDLE, null, null);			
				stopAITask();				
				_actor.detachAI();				
				return;
			}
		}
		
		super.changeIntention(intention, arg0, arg1);
		
		startAITask();	
	}

	@Override
	protected void onIntentionAttack(L2Character target)
	{
		_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getInstance().getGameTicks();
		
		if (getActiveChar().getTemplate().hasBuffSkill() && (lastBuffTick + 30) < GameTimeController.getInstance().getGameTicks())
		{
			for (L2Skill buff : getActiveChar().getTemplate()._buffskills)
			{
				if(buff==null || getActiveChar().getFirstEffect(buff) != null)
			       continue;

				getActiveChar().setTarget(getActiveChar());
				getActiveChar().doCast(buff);
				getActiveChar().setTarget(target);
				break;				
			}
			lastBuffTick = GameTimeController.getInstance().getGameTicks();
		}
		
		super.onIntentionAttack(target);
	}

	protected void thinkActive()
	{
		if (getActiveChar() == null)
			return;
		
		final L2Attackable npc = getActiveChar();
		L2Object target = getTarget();

		if (npc.getObjectId() == 36006)
			return;
		
		if (_globalAggro != 0)
		{
			if (_globalAggro < 0)
				_globalAggro++;
			else
				_globalAggro--;
		}
				
		if (_globalAggro >= 0)
		{
			if (!npc.isCastingNow() && !npc.isAttackingNow() && npc.isAggressive() || (npc instanceof L2GuardInstance))
			{
			    final int range = npc instanceof L2GuardInstance ? 600 : npc.getAggroRange();
			    L2World.getInstance().forEachVisibleObjectInRange(npc, L2Character.class, range, t ->
			    {
				     if ((npc instanceof L2FestivalMonsterInstance) && t instanceof L2PcInstance)
				     {
					     L2PcInstance targetPlayer = (L2PcInstance) t;						
					     if (!(targetPlayer.isFestivalParticipant()))
						       return;
				     }
					
				     if (autoAttackCondition(t)) 
				     {
					    int hating = npc.getHating(t);
						
					     if (hating == 0)
						     npc.addDamageHate(t, 0, 1);
				     }
			 });
		  }
					
			L2Character hated;
			
			if (npc.isConfused() && (target != null) && target instanceof L2Character)
				hated = (L2Character) target; 
			else
				hated = npc.getMostHated();
		
			if ((hated != null) && !npc.isCoreAIDisabled())
			{
				int aggro = npc.getHating(hated);
				
				if ((aggro + _globalAggro) > 0)
				{
					if (!npc.isRunning())
						 npc.setRunning();
					
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, hated);
				}
				
				return;
			}
		}

		if (!npc.canReturnToSpawnPoint())
			return;
		
		if ((npc instanceof L2GuardInstance) && !npc.isWalker()  || npc instanceof L2SiegeGuardInstance)
			npc.returnHome();

		final L2Character leader = npc.getLeader();
		if ((leader != null) && !leader.isAlikeDead())
		{
			final int offset;
			final int minRadius = 30;
			
			if (npc.isRaidMinion())
				offset = 500;
			else
				offset = 200;
			
			if (leader.isRunning())
				npc.setRunning();
			else
				npc.setWalking();
			
			if (npc.distance2d(leader) > offset)
			{
				double x1, y1, z1;
				x1 = Rnd.get(minRadius * 2, offset * 2); 
				y1 = Rnd.get((int) x1, offset * 2);
				y1 = (int) Math.sqrt((y1 * y1) - (x1 * x1)); 
				
				if (x1 > (offset + minRadius))
					x1 = (leader.getX() + x1) - offset;
				else
					x1 = (leader.getX() - x1) + minRadius;
				if (y1 > (offset + minRadius))
					y1 = (leader.getY() + y1) - offset;
				else
					y1 = (leader.getY() - y1) + minRadius;
				
				z1 = leader.getZ();
				
				moveTo((int)x1,(int) y1, (int)z1);
				return;
			}
			else if (Rnd.nextInt(RANDOM_WALK_RATE) == 0)
			{
				for (L2Skill sk : (npc.getTemplate()._buffskills))
				{
					if(sk==null || npc.getFirstEffect(sk) != null)
					   continue;

					npc.setTarget(npc);
					npc.doCast(sk);
					npc.setTarget(target);
					break;
				}
			}
		}
		else if ((npc.getSpawn() != null) && (Rnd.nextInt(RANDOM_WALK_RATE) == 0))
		{
			double x1 = 0;
			double y1 = 0;
			double z1 = 0;
			final int range = 300;
			
			for (L2Skill sk : (npc.getTemplate()._buffskills))
			{
				if(sk==null || npc.getFirstEffect(sk) != null)
					 continue;

				npc.setTarget(npc);
				npc.doCast(sk);
				npc.setTarget(target);
				break;
			}
			
			x1 = npc.getSpawn().getLocx();
			y1 = npc.getSpawn().getLocy();
			z1 = npc.getSpawn().getLocz();
			
			if (!npc.isInRadius2d(npc, range))
				npc.setIsReturningToSpawnPoint(true);
			else
			{
				int deltaX = Rnd.nextInt(range * 2); 
				int deltaY = Rnd.get(deltaX, range * 2); 
				deltaY = (int) Math.sqrt((deltaY * deltaY) - (deltaX * deltaX)); 
				x1 = (deltaX + x1) - range;
				y1 = (deltaY + y1) - range;
				z1 = npc.getZ();
			}
			
			if (npc.getNpcId() == 29014 && npc instanceof L2GrandBossInstance && ((L2GrandBossInstance) npc).getTeleported())
				return;
			
			final Location moveLoc = ((Config.GEODATA) ? GeoEngine.moveCheck(npc.getX(), npc.getY(), npc.getZ(),(int) x1, (int) y1, npc.isFlying()) : new Location((int) x1, (int) y1, (int) z1));
			
			moveTo(moveLoc.getX(), moveLoc.getY(), moveLoc.getZ());
		}
		
	}
	
	protected void thinkAttack()
	{
		final L2Attackable npc = getActiveChar();

		if ((npc == null) || npc.isCastingNow())
			return;
		
		L2Character target = npc.getMostHated();
		
		if ((target == null) || target.isAlikeDead())
		{
			npc.stopHating(target);		
			npc.getAI().setIntention(AI_INTENTION_ACTIVE);		
			npc.setWalking();
			npc.returnHome();
			return;
		}
		
		if (npc.getTarget() != target)
			npc.setTarget(target);	
		
		if(!npc.isInsideRadius(target.getX(), target.getY(), target.getZ(),3000, true, false))
		{
			npc.stopHating(target);		
			npc.getAI().setIntention(AI_INTENTION_ACTIVE);		
			npc.setWalking();
			npc.returnHome();
			return;	
		}

		if (_attackTimeout < GameTimeController.getInstance().getGameTicks())
		{
			npc.getAI().setIntention(AI_INTENTION_ACTIVE);
			
			if (npc.isRunning())
				npc.setWalking();	
		}

		if(target.isFlying() && Math.abs(target.getZ() - npc.getZ()) > 600)
		{
			npc.stopHating(target);		
			npc.getAI().setIntention(AI_INTENTION_ACTIVE);		
			npc.setWalking();
			npc.returnHome();
			return;
		}
			
		final int collision = npc.getTemplate().getCollisionRadius();
				
		if (npc.getFactionId() != null)
		{
			final int factionRange = npc.getFactionRange() + collision;
			try
			{
				final L2Character finalTarget = target;
				L2World.getInstance().forEachVisibleObjectInRange(npc, L2Npc.class, factionRange, called ->
				{
					if (getActiveChar().getFactionId() != called.getFactionId())
						return;
					
					if (called.hasAI() && !called.isDead())
					{
						boolean canSee  = ((Config.GEODATA) ? GeoEngine.canSeeTarget(called, finalTarget, called.isFlying()) : GeoEngine.canSeeTarget(called, finalTarget));
						
						if (canSee && (Math.abs(finalTarget.getZ() - called.getZ()) < 600) && npc.getAttackByList().stream().anyMatch(o -> o == finalTarget) && (called.getAI()._intention == CtrlIntention.AI_INTENTION_IDLE) || (called.getAI()._intention == CtrlIntention.AI_INTENTION_ACTIVE) || (called.getAI()._intention == CtrlIntention.AI_INTENTION_MOVE_TO && !called.isRunning()))
						{
							if (finalTarget.isPlayable())
							{
								final List<Quest> factionCallScript = called.getTemplate().getEventQuests(QuestEventType.ON_FACTION_CALL);
								if (factionCallScript != null)
								{
									final L2PcInstance player = finalTarget.getActingPlayer();
									final boolean isSummon = finalTarget instanceof L2Summon;
									
									for (Quest quest : factionCallScript)
										quest.notifyFactionCall(called, npc, player, isSummon);
								}
								
								called.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, finalTarget, 1);
							}
							else if (called.isAttackable() && (called.getAI()._intention != CtrlIntention.AI_INTENTION_ATTACK))
							{
								((L2Attackable) called).addDamageHate(finalTarget, 0, npc.getHating(finalTarget));
								called.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, finalTarget);
							}
						}
					}
				});
			}
			catch (NullPointerException e)
			{
				_log.info("L2AttackableAI: thinkAttack() faction call failed.");
				if (Config.DEBUG)
					e.printStackTrace();
			}
		}
		
		if (npc.isCoreAIDisabled())
			return;

		final int combinedCollision = collision + target.getTemplate().getCollisionRadius();

		if (Rnd.get(10) <= 4 && !npc.isMovementDisabled() && npc.getAttackByList().contains(target))
		{
			for (L2Attackable nearby : L2World.getInstance().getVisibleObjects(npc, L2Attackable.class,combinedCollision))
			{
				if(!nearby.getAttackByList().contains(target))
				    continue;
				
				if (npc.isInRadius2d(nearby, collision) && (nearby != target))
				{
					double newX = combinedCollision + Rnd.get(40);
					
					if (Rnd.nextBoolean())
						newX = target.getX() + newX;
					else
						newX = target.getX() - newX;
					
					double newY = combinedCollision + Rnd.get(40);
					
					if (Rnd.nextBoolean())
						newY = target.getY() + newY;
					else
						newY = target.getY() - newY;
					
					if (!npc.isInRadius2d(newX, newY, collision))
					{
						double newZ = npc.getZ() + 30;
						moveTo((int)newX, (int)newY, (int)newZ);
					}
					return;
				}
			}
		}

		if (!npc.isMovementDisabled() && (npc.getTemplate().getDodge() > 0))
		{
			if (Rnd.get(100) <= npc.getTemplate().getDodge())
			{
				if (npc.distance2d(target) <= (60 + combinedCollision))
				{
					double posX = npc.getX();
					double posY = npc.getY();
					double posZ = npc.getZ() + 30;
					
					if (target.getX() < posX)
						posX = posX + 300;
					else
						posX = posX - 300;
					
					if (target.getY() < posY)
						posY = posY + 300;
					else
						posY = posY - 300;

					moveTo((int)posX, (int)posY, (int)posZ);
					return;
				}
			}
		}

		if (npc.isRaid() || npc.isRaidMinion())
		{
			chaostime++;
			boolean changeTarget = false;
			if ((npc instanceof L2RaidBossInstance) && (chaostime > 10))
			{
				double multiplier = ((L2MonsterInstance) npc).hasMinions() ? 200 : 100;
				changeTarget = Rnd.get(100) <= (100 - ((npc.getCurrentHp() * multiplier) / npc.getMaxHp()));
			}
			else if ((npc instanceof L2GrandBossInstance) && (chaostime > 10))
			{
				double chaosRate = 100 - ((npc.getCurrentHp() * 300) / npc.getMaxHp());
				changeTarget = ((chaosRate <= 10) && (Rnd.get(100) <= 10)) || ((chaosRate > 10) && (Rnd.get(100) <= chaosRate));
			}
			else if (chaostime > 10)
				changeTarget = Rnd.get(100) <= (100 - ((npc.getCurrentHp() * 200) / npc.getMaxHp()));
			
			if (changeTarget)
			{
				target = targetReconsider(true);
				if (target != null)
				{
					setTarget(target);
					chaostime = 0;
					return;
				}
			}
		}
		
		if (target == null)
		{
			target = targetReconsider(false);
			
			if (target == null)
			{
				npc.returnHome();
				return;
			}
			
			npc.setTarget(target);
		}
		
		if(npc.getTemplate().hasSkill() && Rnd.get(100) < 10)
		{
			if (npc.getTemplate().hasHealSkill() && !npc.getTemplate()._healskills.isEmpty())
			{
				final L2Skill healSkill = npc.getTemplate()._healskills.get(Rnd.get(npc.getTemplate()._healskills.size()));
				if (checkUseConditions(npc, healSkill))
				{
					final L2Character healTarget = skillTargetReconsider(healSkill, false);
					if (healTarget != null)
					{
						double healChance = (100 - healTarget.getCurrentHpPercent()) * 1.5; 
						if ((Rnd.get(100) < healChance) && checkSkillTarget(healSkill, healTarget))
						{
							npc.setTarget(healTarget);
							npc.doCast(healSkill);
							npc.setTarget(target);
							return;
						}
					}
				}
			}
			
			if (npc.getTemplate().hasBuffSkill() && !npc.getTemplate()._buffskills.isEmpty())
			{
				final L2Skill buffSkill = npc.getTemplate()._buffskills.get(Rnd.get(npc.getTemplate()._buffskills.size()));
				if (checkUseConditions(npc, buffSkill))
				{
					if(buffSkill==null || npc.getFirstEffect(buffSkill) != null)
						 return;

					npc.setTarget(npc);
					npc.doCast(buffSkill);
					npc.setTarget(target);
					return;
				}
			}

			if (target.isMoving() && npc.getTemplate().hasImmobiliseSkill() && !npc.getTemplate()._immobiliseskills.isEmpty())
			{
				final L2Skill immobolizeSkill = npc.getTemplate()._immobiliseskills.get(Rnd.get(npc.getTemplate()._immobiliseskills.size()));
				if (checkUseConditions(npc, immobolizeSkill) && checkSkillTarget(immobolizeSkill, target))
				{
					npc.setTarget(target);
					npc.doCast(immobolizeSkill);
					return;
				}
			}
			
			if (target.isCastingNow() && npc.getTemplate().hasCOTSkill() && !npc.getTemplate()._cotskills.isEmpty())
			{
				final L2Skill muteSkill = npc.getTemplate()._cotskills.get(Rnd.get(npc.getTemplate()._cotskills.size()));
				if (checkUseConditions(npc, muteSkill) && checkSkillTarget(muteSkill, target))
				{
					npc.setTarget(target);
					npc.doCast(muteSkill);
					return;
				}
			}
			
			if (npc.getTemplate().hasAutoSrangeSkill() && !npc.getTemplate()._Srangeskills.isEmpty())
			{
				final L2Skill shortRangeSkill = npc.getTemplate()._Srangeskills.get(Rnd.get(npc.getTemplate()._Srangeskills.size()));
				if (checkUseConditions(npc, shortRangeSkill) && checkSkillTarget(shortRangeSkill, target))
				{
					npc.setTarget(target);
					npc.doCast(shortRangeSkill);
					return;
				}
			}

			if (npc.getTemplate().hasAutoLrangeSkill() && !npc.getTemplate()._Lrangeskills.isEmpty())
			{
				final L2Skill longRangeSkill = npc.getTemplate()._Lrangeskills.get(Rnd.get(npc.getTemplate()._Lrangeskills.size()));
				if (checkUseConditions(npc, longRangeSkill) && checkSkillTarget(longRangeSkill, target))
				{
					npc.setTarget(target);
					npc.doCast(longRangeSkill);
					return;
				}
			}
			
			if (!npc.getTemplate()._generalskills.isEmpty())
			{
				final L2Skill generalSkill = npc.getTemplate()._generalskills.get(Rnd.get(npc.getTemplate()._generalskills.size()));

				if (checkUseConditions(npc, generalSkill) && checkSkillTarget(generalSkill, target))
				{
					npc.setTarget(target);
					npc.doCast(generalSkill);
					return;
				}
			}
		}
		
		int range = npc.getPhysicalAttackRange() + combinedCollision;
					
        if(target.isDead())
        {
			target = targetReconsider(true);
			
			if (target == null)
			{
				npc.returnHome();
				return;
			}
			
			setTarget(target);
        }
		
		final double dista = Math.sqrt(npc.getPlanDistanceSq(target.getX(), target.getY()));
		int dist2 = (int) dista - npc.getTemplate().collisionRadius;
		
		if (dist2 > range)
		{		
			if (target.isMoving())
				range -= 30;
			
			if (range < 5)
				range = 5;
			
			moveToPawn(target, range);
			return;
		}
		
		npc.doAttack(target,true);
	}
	
	public static boolean checkUseConditions(L2Character caster, L2Skill skill)
	{
		if (caster == null)
			return false;
		
		if (skill == null || caster.isSkillDisabled(skill.getId()))
			return false;

		if (caster.isCastingNow())
			return false;

		if (caster.isMuted() || caster.isPsychicalMuted())
			return false;
		
		return true;
	}
	
	private boolean checkSkillTarget(L2Skill skill, L2Character target)
	{
		if (target == null || target.isDead())
			return false;
		
		if(!getActiveChar().isInsideRadius(target.getX(), target.getY(), target.getZ(), skill.getCastRange(), true, false))
			return false;

		if (skill.getSkillType()==L2SkillType.BUFF && target.isAutoAttackable(getActiveChar()))
			return false;
		
		if (skill.getSkillType()==L2SkillType.HEAL && target.getCurrentHp() == target.getMaxHp())
			return false;
		
		return true;
	}
	
	public boolean checkTarget(L2Object target)
	{
		if (target == null)
			return false;
		
		final L2Attackable npc = getActiveChar();
		
		boolean canSee  = ((Config.GEODATA) ? GeoEngine.canSeeTarget(npc, target, npc.isFlying()) : GeoEngine.canSeeTarget(npc, target));

		if (target instanceof L2Character)
		{
			if (((L2Character) target).isDead())
				return false;		
			
			if (npc.isMovementDisabled())
			{
				if (!npc.isInRadius2d(target, npc.getPhysicalAttackRange() + npc.getTemplate().getCollisionRadius() + ((L2Character) target).getTemplate().getCollisionRadius()))
					return false;
				
				if (!canSee)
					return false;
			}
			
			if (!target.isAutoAttackable(npc))
				return false;
		}		
		return canSee;
	}
	
	public boolean checkCondition(L2Character caster, L2Skill skill)
	{
		if (caster == null || skill == null || caster.isSkillDisabled(skill.getId()) || caster.isCastingNow())
			return false;
		
		if (caster.getCurrentHp() <= skill.getHpConsume())
			return false;
		
		if (caster.isMuted())
			return false;
		
		return true;
	}
	
	private L2Character skillTargetReconsider(L2Skill skill, boolean insideCastRange)
	{
		final L2Attackable npc = getActiveChar();
		
		if (!checkCondition(npc, skill))
			return null;
		
		final boolean isBad =  skill.isDebuff() || skill.isOffensive();
		
		final int range = insideCastRange ? skill.getCastRange() + getActiveChar().getTemplate().getCollisionRadius() : 2000;
		
		Stream<L2Character> stream;
		if (isBad)
		{
			stream = npc.getAggroList().values().stream()
					.map(AggroInfo::getAttacker)
					.filter(c -> checkSkillTarget(skill, c))
					.sorted(Comparator.<L2Character>comparingInt(npc::getHating).reversed());
		}
		else
		{
			stream = L2World.getInstance().getVisibleObjects(npc, L2Character.class, range, c -> checkSkillTarget(skill, c)).stream();
			
			if (checkSkillTarget(skill, npc))
				stream = Stream.concat(stream, Stream.of(npc));
			
			if (skill.getSkillType()==(L2SkillType.HEAL))
				stream = stream.sorted(Comparator.comparingInt(L2Character::getCurrentHpPercent));
		}
		
		return stream.findFirst().orElse(null);
		
	}

	private L2Character targetReconsider(boolean randomTarget)
	{
		final L2Attackable npc = getActiveChar();
		
		if (randomTarget)
		{
			Stream<L2Character> stream = npc.getAggroList().values().stream().map(AggroInfo::getAttacker).filter(this::checkTarget);
			
			if (npc.isAggressive())
				stream = Stream.concat(stream, L2World.getInstance().getVisibleObjects(npc, L2Character.class, npc.getAggroRange(), this::checkTarget).stream());
			
			return stream.findAny().orElse(null);
		}
		
		return npc.getAggroList().values().stream()
			.filter(a -> checkTarget(a.getAttacker()))
			.sorted(Comparator.comparingInt(AggroInfo::getHate))
			.map(AggroInfo::getAttacker)
			.findFirst()
			.orElse(npc.isAggressive() || npc instanceof L2GuardInstance ? L2World.getInstance().getVisibleObjects(npc, L2Character.class, npc.getAggroRange(), this::checkTarget).stream().findAny().orElse(null) : null);
	}

	@Override
	protected void onEvtThink()
	{
		if (getActiveChar().thinking() || !getActiveChar().isInActiveRegion() || getActiveChar().isCastingNow() || getActiveChar().isAllSkillsDisabled() || getActiveChar().isAfraid())
			return;
		
		getActiveChar().setThinking(true);
		
		try
		{
			switch (getIntention())
			{
				case AI_INTENTION_ACTIVE:
				{
					thinkActive();
					break;
				}
				case AI_INTENTION_ATTACK:
				{
					thinkAttack();
					break;
				}
			}
		}
		catch (Exception e)
		{
		}
		finally
		{
			getActiveChar().setThinking(false);
		}
	}
	
	@Override
	protected void onEvtAttacked(L2Character attacker)
	{
		final L2Attackable me = getActiveChar();
		final L2Object target = getTarget();
		
		_attackTimeout = MAX_ATTACK_TIMEOUT + GameTimeController.getInstance().getGameTicks();
		
		if (_globalAggro < 0)
			_globalAggro = 0;

		me.addDamageHate(attacker, 0, 1);		

		if (!me.isCoreAIDisabled() && (me.getAI().getIntention() != AI_INTENTION_ATTACK || me.getMostHated() != target))
		{
			if (!me.isRunning())
				me.setRunning();
			me.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);		
		}
		
		if (me instanceof L2MonsterInstance)
		{
			L2MonsterInstance master = (L2MonsterInstance) me;
			
			if (master.hasMinions())
				master.getMinionList().onAssist(me, attacker);
			
			master = master.getLeader();
			
			if ((master != null) && master.hasMinions())
				master.getMinionList().onAssist(me, attacker);
		}
		
		super.onEvtAttacked(attacker);
	}

	@Override
	protected void onEvtAggression(L2Character target, int aggro)
	{
		final L2Attackable me = getActiveChar();
		
		if (me.isDead())
			return;
		
		if (target != null)
		{
			me.addDamageHate(target, 0, aggro);
			
			if (!me.isCoreAIDisabled() && getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
			{
				if (!me.isRunning())
					 me.setRunning();
				me.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
			
			if (me instanceof L2MonsterInstance)
			{
				L2MonsterInstance master = (L2MonsterInstance) me;
				
				if (master.hasMinions())
					master.getMinionList().onAssist(me, target);
				
				master = master.getLeader();
				
				if ((master != null) && master.hasMinions())
					master.getMinionList().onAssist(me, target);
			}
		}
	}
	
	@Override
	protected void onIntentionActive()
	{
		_attackTimeout = Integer.MAX_VALUE;
		super.onIntentionActive();
	}
	
	@Override
	public void setGlobalAggro(int value)
	{
		_globalAggro = value;
	}
	
	@Override
	public void setTarget(L2Object target)
	{
		_actor.setTarget(target);
	}
	
	@Override
	public L2Object getTarget()
	{
		return _actor.getTarget();
	}

	public L2Attackable getActiveChar()
	{
		return (L2Attackable) _actor;
	}
}
