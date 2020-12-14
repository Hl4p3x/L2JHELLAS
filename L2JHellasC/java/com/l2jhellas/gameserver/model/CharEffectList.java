package com.l2jhellas.gameserver.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.enums.skills.L2SkillType;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.actor.group.party.L2Party;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.entity.olympiad.OlympiadGameManager;
import com.l2jhellas.gameserver.model.entity.olympiad.OlympiadGameTask;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ExOlympiadSpelledInfo;
import com.l2jhellas.gameserver.network.serverpackets.AbnormalStatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.PartySpelled;
import com.l2jhellas.gameserver.network.serverpackets.ShortBuffStatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public class CharEffectList
{
	protected static final Logger _log = Logger.getLogger(CharEffectList.class.getName());
	private static final L2Effect[] EMPTY_EFFECTS = new L2Effect[0];

	private List<L2Effect> _buffs;
	private List<L2Effect> _debuffs;
	
	// The table containing the List of all stacked effect in progress for each Stack group Identifier
	private Map<String, List<L2Effect>> _stackedEffects;
	
	private volatile boolean _hasBuffsRemovedOnAnyAction = false;
	private volatile boolean _hasBuffsRemovedOnDamage = false;
	private volatile boolean _hasDebuffsRemovedOnDamage = false;
	
	private boolean _queuesInitialized = false;
	private LinkedBlockingQueue<L2Effect> _addQueue;
	private LinkedBlockingQueue<L2Effect> _removeQueue;
	private final AtomicBoolean queueLock = new AtomicBoolean();
	private int _effectFlags;
	
	// only party icons need to be updated
	private boolean _partyOnly = false;
	
	// Owner of this list
	private final L2Character _owner;
	
	private L2Effect _shortBuff = null;

	private L2Effect[] _effectCache;
	private volatile boolean _rebuildCache = true;
	private final Object _buildEffectLock = new Object();
	
	public CharEffectList(L2Character owner)
	{
		_owner = owner;
	}
		
	public final L2Effect getFirstEffect(L2Effect.EffectType tp)
	{
		L2Effect effect = null;	

		if (_buffs != null && !_buffs.isEmpty())
		     effect = _buffs.stream().filter(Objects :: nonNull).filter(e -> e.getInUse() && e.getEffectType() != null && e.getEffectType().equals(tp)).findFirst().orElse(null);
	
		if (effect == null && _debuffs != null && !_debuffs.isEmpty())
			effect = _debuffs.stream().filter(Objects :: nonNull).filter(e -> e.getInUse() && e.getEffectType() != null && e.getEffectType().equals(tp)).findFirst().orElse(null);

		return effect;
	}

	public final L2Effect getFirstEffect(L2Skill skill)
	{
		L2Effect effect = null;	
		
		if (skill.isDebuff() && _debuffs != null && !_debuffs.isEmpty())
			effect = _debuffs.stream().filter(Objects :: nonNull).filter(e -> e.getInUse() && e.getSkill().equals(skill)).findFirst().orElse(null);
		else if (_buffs != null && !_buffs.isEmpty())
			effect = _buffs.stream().filter(Objects :: nonNull).filter(e -> e.getInUse() && e.getSkill().equals(skill)).findFirst().orElse(null);

		return effect;
	}

	public final L2Effect getFirstEffect(int skillId)
	{
		L2Effect effect = null;	

		if (_buffs != null && !_buffs.isEmpty())
		    effect = _buffs.stream().filter(Objects :: nonNull).filter(e -> e.getInUse() && e.getSkill().getId() == skillId).findFirst().orElse(null);
	
		if (effect == null && _debuffs != null && !_debuffs.isEmpty())
			effect = _debuffs.stream().filter(Objects :: nonNull).filter(e -> e.getInUse() && e.getSkill().getId() == skillId).findFirst().orElse(null);

		return effect;
	}
	
	private boolean doesStack(L2Skill checkSkill)
	{
		if ((_buffs == null || _buffs.isEmpty()) || checkSkill._effectTemplates == null || checkSkill._effectTemplates.length < 1 || checkSkill._effectTemplates[0].stackType == null || "none".equals(checkSkill._effectTemplates[0].stackType))
			return false;
		
		String stackType = checkSkill._effectTemplates[0].stackType;
		
		if (stackType == null || "none".equals(stackType))
			return false;

		return _buffs.stream().filter(Objects :: nonNull).filter(e -> !e.getStackType().isEmpty()).anyMatch(b -> b.getStackType().equals(stackType));
	}
	
	public int getBuffCount()
	{
		if (_buffs == null || _buffs.isEmpty())
			return 0;
		
		return (int) _buffs.stream().filter(Objects :: nonNull).filter(b -> b.getInUse() && !b.getSkill().is7Signs() && b.getSkill().isBuffSkill()).count();

	}

	public int getDanceCount()
	{
		if (_buffs == null || _buffs.isEmpty())
			return 0;
		
		return (int) _buffs.stream().filter(Objects :: nonNull).filter(b -> b.getInUse()  && b.getSkill().isDance()).count();
	}

	public final void stopAllEffects()
	{
		// Get all active skills effects from this list
		L2Effect[] effects = getAllEffects();
		
		// Exit them
		for (L2Effect e : effects)
		{
			if (e != null)
				e.exit(true);
		}
	}
	
	public final void stopAllEffectsExceptThoseThatLastThroughDeath()
	{
		// Get all active skills effects from this list
		final L2Effect[] effects = getAllEffects();
		
		// Exit them
		for (L2Effect e : effects)
		{
			if (e != null && !(e.getSkill().getSkillType() == L2SkillType.BUFF))
				e.exit(true);
		}
	}

	public void stopAllToggles()
	{
		if (_buffs != null && !_buffs.isEmpty())
		{
			_buffs.stream().filter(Objects :: nonNull).filter(e -> e.getSkill().isToggle()).forEach(t ->
			{
				if (t != null)
					t.exit();
			});
		}
	}
	
	public final void stopEffects(L2Effect type)
	{
		if (_buffs != null && !_buffs.isEmpty())
		{
			_buffs.stream().filter(Objects :: nonNull).filter(e -> e.getEffectType() != null && e.getEffectType().equals(type.getEffectType())).forEach(t ->
			{
				if (t != null)
	                t.exit();
			});
		}
		
		if (_debuffs != null && !_debuffs.isEmpty())
		{
			_debuffs.stream().filter(Objects :: nonNull).filter(e -> e.getEffectType() != null && e.getEffectType().equals(type.getEffectType())).forEach(t ->
			{
				if (t != null)
					t.exit();
			});	
		}
	}
	
	public final void stopSkillEffects(int skillId)
	{
		if (_buffs != null && !_buffs.isEmpty())
		{
			_buffs.stream().filter(Objects :: nonNull).filter(e -> e.getSkill().getId() == skillId).forEach(t ->
			{
				if (t != null)
	                t.exit();
			});
		}
			
		if (_debuffs != null && !_debuffs.isEmpty())
		{
			_debuffs.stream().filter(Objects :: nonNull).filter(e -> e.getSkill().getId() == skillId).forEach(t ->
			{
				if (t != null)
	                t.exit();
			});
		}
	}
	
	public final void stopSkillEffects(L2SkillType skillType, int negateLvl)
	{
		if (_buffs != null && !_buffs.isEmpty())
		{
			_buffs.stream().filter(Objects :: nonNull).filter(e -> e.getSkill().canBeRemoved(skillType, negateLvl)).forEach(t ->
			{
				if (t != null)
	                t.exit();
			});
		}
		
		if (_debuffs != null && !_debuffs.isEmpty())
		{
			_debuffs.stream().filter(Objects :: nonNull).filter(e -> e.getSkill().canBeRemoved(skillType, negateLvl)).forEach(t ->
			{
				if (t != null)
	                t.exit();
			});
		}
	}
	
	public void stopEffectsOnAction()
	{
		if (_hasBuffsRemovedOnAnyAction)
		{
			if (_buffs != null && !_buffs.isEmpty())
			{
				_buffs.stream().filter(Objects :: nonNull).forEach(t ->
				{
					if (t != null)
		                t.exit(true);
				});
			}			
		}
	}
	
	public void stopEffectsOnDamage(boolean awake)
	{
		if (_hasBuffsRemovedOnDamage)
		{
			if (_buffs != null && !_buffs.isEmpty())
			{
				_buffs.stream().filter(Objects :: nonNull).filter(e -> e.getSkill().getSkillType() == L2SkillType.SLEEP && (awake || e.getSkill().getSkillType() != L2SkillType.SLEEP)).forEach(t ->
				{
					if (t != null)
			            t.exit(true);
				});
			}
		}
		
		if (_hasDebuffsRemovedOnDamage)
		{
			if (_debuffs != null && !_debuffs.isEmpty())
			{
				_debuffs.stream().filter(Objects :: nonNull).filter(e -> e.getSkill().getSkillType() == L2SkillType.SLEEP && (awake || e.getSkill().getSkillType() != L2SkillType.SLEEP)).forEach(t ->
				{
					if (t != null)
		                t.exit(true);
				});
			}
		}
	}
	
	public void updateEffectIcons(boolean partyOnly)
	{
		if (_buffs == null && _debuffs == null)
			return;
		
		if (partyOnly)
			_partyOnly = true;
		
		queueRunner();
	}
	
	public void queueEffect(L2Effect effect, boolean remove)
	{
		if (effect == null)
			return;
		
		if (!_queuesInitialized)
			init();
		
		if (remove)
			_removeQueue.offer(effect);
		else
			_addQueue.offer(effect);
		
		queueRunner();
	}
	
	private synchronized void init()
	{
		if (_queuesInitialized)
			return;
		
		_addQueue = new LinkedBlockingQueue<>();
		_removeQueue = new LinkedBlockingQueue<>();
		_queuesInitialized = true;
	}
	
	private void queueRunner()
	{
		if (!queueLock.compareAndSet(false, true))
			return;
		
		try
		{
			L2Effect effect;
			do
			{
				// remove has more priority than add so removing all effects from queue first
				while ((effect = _removeQueue.poll()) != null)
				{
					removeEffectFromQueue(effect);
					_partyOnly = false;
				}
				
				if ((effect = _addQueue.poll()) != null)
				{
					addEffectFromQueue(effect);
					_partyOnly = false;
				}
			}
			while (!_addQueue.isEmpty() || !_removeQueue.isEmpty());
			
			computeEffectFlags();
			updateEffectIcons();
		}
		finally
		{
			queueLock.set(false);
		}
	}
	
	protected void removeEffectFromQueue(L2Effect effect)
	{
		if (effect == null)
			return;
		
		List<L2Effect> effectList;
		
		_rebuildCache = true;
		
		if (effect.getSkill().isDebuff())
		{
			if (_debuffs == null)
				return;
			
			effectList = _debuffs;
		}
		else
		{
			if (_buffs == null)
				return;
			
			effectList = _buffs;
		}
		
		if (effect == _shortBuff)
			shortBuffStatusUpdate(null);
		
		final String stackType = effect.getStackType();
		
		if ("none".equals(stackType))
			_owner.removeStatsOwner(effect);
		else
		{
			if (_stackedEffects == null)
				return;
			
			// Get the list of all stacked effects corresponding to the stack type to add.
			final List<L2Effect> stackQueue = _stackedEffects.get(stackType);
			if (stackQueue == null || stackQueue.isEmpty())
				return;
			
			final int index = stackQueue.indexOf(effect);
			if (index >= 0)
			{
				stackQueue.remove(effect);
				
				if (index == 0)
				{
					_owner.removeStatsOwner(effect);
					
					if (!stackQueue.isEmpty())
					{
						L2Effect newStackedEffect = listsContains(stackQueue.get(0));
						if (newStackedEffect != null)
						{
							// Set the effect to In Use
							if (newStackedEffect.setInUse(true))
								_owner.addStatFuncs(newStackedEffect.getStatFuncs());
						}
					}
				}
				
				// Update the Stack Group table _stackedEffects
				if (stackQueue.isEmpty())
					_stackedEffects.remove(stackType);
				else
					_stackedEffects.put(stackType, stackQueue);
			}
		}
		
		// Remove the active skill L2effect from _effects
		if (effectList.remove(effect) && _owner.isPlayer())
		{
			SystemMessage sm = effect.getSkill().isToggle() ? SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_ABORTED).addSkillName(effect) 
			: SystemMessage.getSystemMessage(SystemMessageId.EFFECT_S1_DISAPPEARED).addSkillName(effect);					
			_owner.sendPacket(sm);			
		}
	}
	
	protected void addEffectFromQueue(L2Effect newEffect)
	{
		if (newEffect == null)
			return;
		
		L2Skill newSkill = newEffect.getSkill();
		
		_rebuildCache = true;
		
		if (isAffected(newEffect.getEffectType().ordinal()) && !newEffect.onSameEffect(null))
		{
			newEffect.stopEffectTask();
			return;
		}
		
		if (newSkill.isDebuff())
		{
			if (_debuffs == null)
				_debuffs = new CopyOnWriteArrayList<>();
			
			for (L2Effect e : _debuffs)
			{
				if (e != null && e.getSkill().getId() == newEffect.getSkill().getId() && e.getEffectType() == newEffect.getEffectType() && e.getStackOrder() == newEffect.getStackOrder() && e.getStackType().equals(newEffect.getStackType()))
				{
					newEffect.stopEffectTask();
					return;
				}
			}
			_debuffs.add(newEffect);
		}
		else
		{
			if (_buffs == null)
				_buffs = new CopyOnWriteArrayList<>();
			
			// Started scheduled timer needs to be canceled.
			for (L2Effect e : _buffs)
			{
				if (e != null && e.getSkill().getId() == newEffect.getSkill().getId() && e.getEffectType() == newEffect.getEffectType() && e.getStackOrder() == newEffect.getStackOrder() && e.getStackType().equals(newEffect.getStackType()))
					e.exit();
			}
			
			// if max buffs, no herb effects are used, even if they would replace one old
			if (newEffect.isHerbEffect() && getBuffCount() >= _owner.getMaxBuffCount())
			{
				newEffect.stopEffectTask();
				return;
			}
			
			if (!doesStack(newSkill) && !newSkill.is7Signs())
			{
				int effectsToRemove = getBuffCount() - _owner.getMaxBuffCount();
				if (effectsToRemove >= 0)
				{
					switch (newSkill.getSkillType())
					{
						case BUFF:
						case REFLECT:
						case HEAL_PERCENT:
						case HEAL_STATIC:
						case MANAHEAL_PERCENT:
						case COMBATPOINTHEAL:
							for (L2Effect e : _buffs)
							{
								if (e == null)
									continue;
								
								switch (e.getSkill().getSkillType())
								{
									case BUFF:
									case REFLECT:
									case HEAL_PERCENT:
									case HEAL_STATIC:
									case MANAHEAL_PERCENT:
									case COMBATPOINTHEAL:
										e.exit();
										effectsToRemove--;
										break; // break switch()
									default:
										continue; // continue for()
								}
								if (effectsToRemove < 0)
									break; // break for()
							}
						default :
							break;
					}
				}
			}
			
			if (newSkill.isToggle())
				_buffs.add(newEffect);
			else
			{
				int pos = 0;
				for (L2Effect e : _buffs)
				{
					if (e == null || e.getSkill().isToggle() || e.getSkill().is7Signs())
						continue;
					
					pos++;
				}
				_buffs.add(pos, newEffect);
			}
		}
		
		final String stackType = newEffect.getStackType();
		
		if ("none".equals(stackType))
		{
			if (newEffect.setInUse(true))
				_owner.addStatFuncs(newEffect.getStatFuncs());
			
			return;
		}
		
		L2Effect effectToAdd = null;
		L2Effect effectToRemove = null;
		
		if (_stackedEffects == null)
			_stackedEffects = new HashMap<>();
		
		// Get the list of all stacked effects corresponding to the stack type to add.
		List<L2Effect> stackQueue = _stackedEffects.get(stackType);
		if (stackQueue != null)
		{
			int pos = 0;
			if (!stackQueue.isEmpty())
			{
				// Get the first stacked effect of the Stack group selected
				effectToRemove = listsContains(stackQueue.get(0));
				
				// Create an Iterator to go through the list of stacked effects in progress.
				Iterator<L2Effect> queueIterator = stackQueue.iterator();
				
				while (queueIterator.hasNext())
				{
					if (newEffect.getStackOrder() < queueIterator.next().getStackOrder())
						pos++;
					else
						break;
				}

				// Add the new effect to the Stack list in function of its position in the Stack group
				stackQueue.add(pos, newEffect);
				
				// skill.exit() could be used, if the users don't wish to see "effect
				// removed" always when a timer goes off, even if the buff isn't active
				// any more (has been replaced). but then check e.g. npc hold and raid petrification.
				if (Config.EFFECT_CANCELING && !newEffect.isHerbEffect() && stackQueue.size() > 1)
				{
					if (newSkill.isDebuff())
						_debuffs.remove(stackQueue.remove(1));
					else
						_buffs.remove(stackQueue.remove(1));
				}
			}
			else
				stackQueue.add(0, newEffect);
		}
		else
		{
			stackQueue = new ArrayList<>();
			stackQueue.add(0, newEffect);
		}
		
		_stackedEffects.put(stackType, stackQueue);
		
		// Get the first stacked effect of the Stack group selected
		if (!stackQueue.isEmpty())
			effectToAdd = listsContains(stackQueue.get(0));
		
		if (effectToRemove != effectToAdd)
		{
			if (effectToRemove != null)
			{
				// Remove all Func objects corresponding to this stacked effect from the Calculator set.
				_owner.removeStatsOwner(effectToRemove);				
				effectToRemove.setInUse(false);
			}
			
			if (effectToAdd != null)
			{
				if (effectToAdd.setInUse(true))
					_owner.addStatFuncs(effectToAdd.getStatFuncs());
			}
		}
	}
	
	public void updateEffectIcons()
	{
		final L2PcInstance player = _owner.getActingPlayer();
		if (player != null)
		{
			final L2Party party = player.getParty();
			final Optional<AbnormalStatusUpdate> asu = (_owner.isPlayer() && !_partyOnly) ? Optional.of(new AbnormalStatusUpdate()) : Optional.empty();
			final Optional<PartySpelled> ps = ((party != null) || _owner instanceof L2Summon) ? Optional.of(new PartySpelled(_owner)) : Optional.empty();
			final Optional<ExOlympiadSpelledInfo> os = (player.isInOlympiadMode() && player.isOlympiadStart()) ? Optional.of(new ExOlympiadSpelledInfo(player)) : Optional.empty();
			
			if (_buffs != null && !_buffs.isEmpty())
			{
				_buffs.stream()
					.filter(Objects::nonNull)
					.filter(L2Effect::getInUse)
					.forEach(info ->
					{
						if (info.getSkill().isPotion())
							shortBuffStatusUpdate(info);
						else
						{
							asu.ifPresent(a -> a.addEffect(info.getSkill(),info.getTotalCount() > 1 || info.getPeriod() == -1 ? -1 : info.getFuture() != null ? (int) info.getFuture().getDelay(TimeUnit.MILLISECONDS) : -1));
							ps.filter(p -> !info.getSkill().isToggle()).ifPresent(p -> p.addPartySpelledEffect(info.getSkill().getId(),info.getSkill().getLevel(),info.getFuture() != null ? (int) info.getFuture().getDelay(TimeUnit.MILLISECONDS) : -1));
							os.ifPresent(o -> o.addEffect(info.getSkill().getId(),info.getSkill().getLevel(),info.getFuture() != null ? (int) info.getFuture().getDelay(TimeUnit.MILLISECONDS) : -1));
						}
					});
			}
			
			if (_debuffs != null && !_debuffs.isEmpty())
			{
				_debuffs.stream()
					.filter(Objects::nonNull)
					.filter(L2Effect::getInUse)
					.forEach(info ->
					{
						if (info.getSkill().isPotion())
							shortBuffStatusUpdate(info);
						else
						{
							asu.ifPresent(a -> a.addEffect(info.getSkill(),info.getTotalCount() > 1 || info.getPeriod() == -1 ? -1 : info.getFuture() != null ? (int) info.getFuture().getDelay(TimeUnit.MILLISECONDS) : -1));
							ps.filter(p -> !info.getSkill().isToggle()).ifPresent(p -> p.addPartySpelledEffect(info.getSkill().getId(),info.getSkill().getLevel(),info.getFuture() != null ? (int) info.getFuture().getDelay(TimeUnit.MILLISECONDS) : -1));
							os.ifPresent(o -> o.addEffect(info.getSkill().getId(),info.getSkill().getLevel(),info.getFuture() != null ? (int) info.getFuture().getDelay(TimeUnit.MILLISECONDS) : -1));
						}
					});
			}
			
			asu.ifPresent(_owner::sendPacket);
			
			if (party != null)
				ps.ifPresent(party::broadcastToPartyMembers);
			else
				ps.ifPresent(player::sendPacket);
			
			if (os.isPresent())
			{
				final OlympiadGameTask game = OlympiadGameManager.getInstance().getOlympiadTask(player.getOlympiadGameId());
				if ((game != null) && game.isBattleStarted())
					os.ifPresent(game.getZone()::broadcastPacketToObservers);
			}
		}
	}
	
	public void shortBuffStatusUpdate(L2Effect info)
	{
		if (_owner.isPlayer())
		{
			_shortBuff = info;

			if (info == null)
				_owner.sendPacket(ShortBuffStatusUpdate.RESET_SHORT_BUFF);
			else
				_owner.sendPacket(new ShortBuffStatusUpdate(info.getSkill().getId(), info.getSkill().getLevel(), (info.getSkill().getBuffDuration() - (info.getTaskTime() * 1000)) / 1000));
		}
	}
	
	protected void updateEffectFlags()
	{
		boolean foundRemovedOnAction = false;
		boolean foundRemovedOnDamage = false;
		
		if (_buffs != null && !_buffs.isEmpty())
		{
			for (L2Effect e : _buffs)
			{
				if (e == null)
					continue;
				
				if (e.getSkill().getSkillType() == L2SkillType.SLEEP)
					foundRemovedOnDamage = true;
			}
		}
		_hasBuffsRemovedOnAnyAction = foundRemovedOnAction;
		_hasBuffsRemovedOnDamage = foundRemovedOnDamage;
		foundRemovedOnDamage = false;
		
		if (_debuffs != null && !_debuffs.isEmpty())
		{
			for (L2Effect e : _debuffs)
			{
				if (e == null)
					continue;
				
				if (e.getSkill().getSkillType() == L2SkillType.SLEEP)
					foundRemovedOnDamage = true;
			}
		}
		_hasDebuffsRemovedOnDamage = foundRemovedOnDamage;
	}
	
	private L2Effect listsContains(L2Effect effect)
	{
		if (_buffs != null && !_buffs.isEmpty() && _buffs.contains(effect))
			return effect;
		if (_debuffs != null && !_debuffs.isEmpty() && _debuffs.contains(effect))
			return effect;
		
		return null;
	}
	
	private final void computeEffectFlags()
	{
		int flags = 0;
		
		if (_buffs != null)
		{
			for (L2Effect e : _buffs)
			{
				if (e == null)
					continue;
				
				flags |= 0;
			}
		}
		
		if (_debuffs != null)
		{
			for (L2Effect e : _debuffs)
			{
				if (e == null)
					continue;
				
				flags |= 0;
			}
		}
		
		_effectFlags = flags;
	}
	
	public boolean isAffected(int bitFlag)
	{
		return (_effectFlags & bitFlag) != 0;
	}
	
	public final L2Effect[] getAllEffects()
	{
		// If no effect is active, return EMPTY_EFFECTS
		if ((_buffs == null || _buffs.isEmpty()) && (_debuffs == null || _debuffs.isEmpty()))
			return EMPTY_EFFECTS;
		
		synchronized (_buildEffectLock)
		{
			// If we dont need to rebuild the cache, just return the current one.
			if (!_rebuildCache)
				return _effectCache;
			
			_rebuildCache = false;
			
			// Create a copy of the effects
			List<L2Effect> temp = new ArrayList<>();
			
			// Add all buffs and all debuffs
			if (_buffs != null && !_buffs.isEmpty())
				temp.addAll(_buffs);
			if (_debuffs != null && !_debuffs.isEmpty())
				temp.addAll(_debuffs);
			
			// Return all effects in an array
			L2Effect[] tempArray = new L2Effect[temp.size()];
			temp.toArray(tempArray);
			return (_effectCache = tempArray);
		}
	}
	
	public void clear()
	{
		_addQueue = null;
		_removeQueue = null;
		_buffs = null;
		_debuffs = null;
		_stackedEffects = null;
		_queuesInitialized = false;
	}
}