package com.l2jhellas.gameserver.model.actor.status;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.enums.ZoneId;
import com.l2jhellas.gameserver.enums.player.DuelState;
import com.l2jhellas.gameserver.instancemanager.DuelManager;
import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jhellas.gameserver.model.actor.stat.CharStat;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.skills.Formulas;
import com.l2jhellas.util.Rnd;

public class CharStatus
{
	protected static final Logger _log = Logger.getLogger(CharStatus.class.getName());
	
	private final L2Character _activeChar;
	private double _currentCp = 0; // Current CP of the L2Character
	private double _currentHp = 0; // Current HP of the L2Character
	private double _currentMp = 0; // Current MP of the L2Character
	
	private final Set<L2Character> _StatusListener = ConcurrentHashMap.newKeySet();
	
	private Future<?> _regTask;
	private byte _flagsRegenActive = 0;
	private static final byte REGEN_FLAG_CP = 4;
	private static final byte REGEN_FLAG_HP = 1;
	private static final byte REGEN_FLAG_MP = 2;
	
	public CharStatus(L2Character activeChar)
	{
		_activeChar = activeChar;
	}
	
	public final void addStatusListener(L2Character object)
	{
		if (object == getActiveChar())
			return;
		
		_StatusListener.add(object);
	}
	
	public final void reduceCp(int value)
	{
		if (getCurrentCp() > value)
			setCurrentCp(getCurrentCp() - value);
		else
			setCurrentCp(0);
	}
	
	public void reduceHp(double value, L2Character attacker)
	{
		reduceHp(value, attacker, true);
	}
	
	public void reduceHp(double value, L2Character attacker, boolean awake)
	{
		if (getActiveChar().isInvul())
			return;
		
		if (getActiveChar() instanceof L2PcInstance)
		{
			if (((L2PcInstance) getActiveChar()).isInDuel())
			{
				// the duel is finishing - players do not receive damage
				if (((L2PcInstance) getActiveChar()).getDuelState() == DuelState.DEAD)
					return;
				else if (((L2PcInstance) getActiveChar()).getDuelState() == DuelState.WINNER)
					return;
				
				// cancel duel if player got hit by another player, that is not part of the duel or a monster
				if (!(attacker instanceof L2SummonInstance) && !(attacker instanceof L2PcInstance && ((L2PcInstance) attacker).getDuelId() == ((L2PcInstance) getActiveChar()).getDuelId()))
				{
					((L2PcInstance) getActiveChar()).setDuelState(DuelState.INTERRUPTED);
				}
			}
			if (getActiveChar().isDead() && !getActiveChar().isFakeDeath())
				return; // Disabled == null check so skills like Body to Mind work again until another solution is found
		}
		else
		{
			if (getActiveChar().isDead())
				return; // Disabled == null check so skills like Body to Mind work again until another solution is found
				
			if (attacker instanceof L2PcInstance && ((L2PcInstance) attacker).isInDuel() && !(getActiveChar() instanceof L2SummonInstance && ((L2SummonInstance) getActiveChar()).getOwner().getDuelId() == ((L2PcInstance) attacker).getDuelId()))
			// Duelling player attacks mob
			
			{
				((L2PcInstance) attacker).setDuelState(DuelState.INTERRUPTED);
			}
		}
		if (awake && getActiveChar().isSleeping())
			getActiveChar().stopSleeping(null);
		if (getActiveChar().isStunned() && Rnd.get(10) == 0)
			getActiveChar().stopStunning(null);
		if (!getActiveChar().isImmobileUntilAttacked())
			getActiveChar().stopImmobileUntilAttacked(null);
		
		if (getActiveChar().isImmobileUntilAttacked())
			getActiveChar().stopImmobileUntilAttacked(null);
		
		if (getActiveChar().isAfraid())
			getActiveChar().stopFear(null);
		
		// Add attackers to npc's attacker list
		if (getActiveChar() instanceof L2Npc)
			getActiveChar().addAttackerToAttackByList(attacker);
		
		if (value > 0) // Reduce Hp if any
		{
			// If we're dealing with an L2Attackable Instance and the attacker hit it with an over-hit enabled skill, set the over-hit values.
			// Anything else, clear the over-hit flag
			if (getActiveChar() instanceof L2Attackable)
			{
				if (((L2Attackable) getActiveChar()).isOverhit())
					((L2Attackable) getActiveChar()).setOverhitValues(attacker, value);
				else
					((L2Attackable) getActiveChar()).overhitEnabled(false);
			}
			value = getCurrentHp() - value; // Get diff of Hp vs value
			if (value <= 0)
			{
				if (getActiveChar().isPlayer())
				{
					// is the dyeing one a duelist? if so change his duel state to dead
					if(((L2PcInstance) getActiveChar()).isInDuel())
					{
					   getActiveChar().disableAllSkills();
					   stopHpMpRegeneration();
					   attacker.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
					   attacker.sendPacket(ActionFailed.STATIC_PACKET);
					
					   // let the DuelManager know of his defeat
					   DuelManager.getInstance().onPlayerDefeat((L2PcInstance) getActiveChar());
					   value = 1;
					}
					else
					{
					  boolean isInside = (getActiveChar().isInsideZone(ZoneId.PEACE) && attacker != null && !attacker.isInsideZone(ZoneId.PEACE));			
					  value = isInside ? 1 : 0;
					}
				}
				else
					value = 0; // Set value to 0 if Hp < 0
				
			}
			setCurrentHp(value); // Set Hp
		}
		else
		{
			// If we're dealing with an L2Attackable Instance and the attacker's hit didn't kill the mob, clear the over-hit flag
			if (getActiveChar() instanceof L2Attackable)
			{
				((L2Attackable) getActiveChar()).overhitEnabled(false);
			}
		}
		
		if (getActiveChar().isDead())
		{
			getActiveChar().abortAttack();
			getActiveChar().abortCast();
			
			if (getActiveChar() instanceof L2PcInstance)
			{
				if (((L2PcInstance) getActiveChar()).isInOlympiadMode())
				{
					stopHpMpRegeneration();
					return;
				}
			}
			
			// first die (and calculate rewards), if currentHp < 0,
			// then overhit may be calculated
			
			// Start the doDie process
			getActiveChar().doDie(attacker);
			
			// now reset currentHp to zero
			setCurrentHp(0);
		}
		else
		{
			// If we're dealing with an L2Attackable Instance and the attacker's hit didn't kill the mob, clear the over-hit flag
			if (getActiveChar() instanceof L2Attackable)
			{
				((L2Attackable) getActiveChar()).overhitEnabled(false);
			}
		}
	}
	
	public final void reduceMp(double value)
	{
		value = getCurrentMp() - value;
		if (value < 0)
			value = 0;
		setCurrentMp(value);
	}
	
	public final void removeStatusListener(L2Character object)
	{
		_StatusListener.remove(object);
	}
	
	public synchronized final void startHpMpRegeneration()
	{
		if (_regTask == null && !getActiveChar().isDead())
		{
			// Get the Regeneration periode
			int period = Formulas.getRegeneratePeriod(getActiveChar());
			
			// Create the HP/MP/CP Regeneration task
			_regTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new RegenTask(), period, period);
		}
	}
	
	public synchronized final void stopHpMpRegeneration()
	{
		if (_regTask != null)
		{
			// Stop the HP/MP/CP Regeneration task
			_regTask.cancel(false);
			_regTask = null;
			
			// Set the RegenActive flag to false
			_flagsRegenActive = 0;
		}
	}
	
	public L2Character getActiveChar()
	{
		return _activeChar;
	}
	
	public final double getCurrentCp()
	{
		return _currentCp;
	}
	
	public final void setCurrentCp(double newCp)
	{
		setCurrentCp(newCp, true);
	}
	
	public final void setCurrentCp(double newCp, boolean broadcastPacket)
	{
		synchronized (this)
		{
			// Get the Max CP of the L2Character
			int maxCp = getActiveChar().getStat().getMaxCp();
			
			if (newCp < 0)
				newCp = 0;
			
			if (newCp >= maxCp)
			{
				// Set the RegenActive flag to false
				_currentCp = maxCp;
				_flagsRegenActive &= ~REGEN_FLAG_CP;
				
				// Stop the HP/MP/CP Regeneration task
				if (_flagsRegenActive == 0)
					stopHpMpRegeneration();
			}
			else
			{
				// Set the RegenActive flag to true
				_currentCp = newCp;
				_flagsRegenActive |= REGEN_FLAG_CP;
				
				// Start the HP/MP/CP Regeneration task with Medium priority
				startHpMpRegeneration();
			}
		}
		
		// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		if (broadcastPacket)
			getActiveChar().broadcastStatusUpdate();
	}
	
	public final double getCurrentHp()
	{
		return _currentHp;
	}
	
	public final void setCurrentHp(double newHp)
	{
		setCurrentHp(newHp, true);
	}
	
	public final void setCurrentHp(double newHp, boolean broadcastPacket)
	{
		synchronized (this)
		{
			// Get the Max HP of the L2Character
			double maxHp = getActiveChar().getStat().getMaxHp();
			
			if (newHp >= maxHp)
			{
				// Set the RegenActive flag to false
				_currentHp = maxHp;
				_flagsRegenActive &= ~REGEN_FLAG_HP;
				getActiveChar().setIsKilledAlready(false);
				
				// Stop the HP/MP/CP Regeneration task
				if (_flagsRegenActive == 0)
					stopHpMpRegeneration();
			}
			else
			{
				// Set the RegenActive flag to true
				_currentHp = newHp;
				_flagsRegenActive |= REGEN_FLAG_HP;
				if (!getActiveChar().isDead())
					getActiveChar().setIsKilledAlready(false);
				
				// Start the HP/MP/CP Regeneration task with Medium priority
				startHpMpRegeneration();
			}
		}
		
		// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		if (broadcastPacket)
			getActiveChar().broadcastStatusUpdate();
	}
	
	public final void setCurrentHpMp(double newHp, double newMp)
	{
		setCurrentHp(newHp, false);
		setCurrentMp(newMp, true); // send the StatusUpdate only once
	}
	
	public final double getCurrentMp()
	{
		return _currentMp;
	}
	
	public final void setCurrentMp(double newMp)
	{
		setCurrentMp(newMp, true);
	}
	
	public final void setCurrentMp(double newMp, boolean broadcastPacket)
	{
		synchronized (this)
		{
			// Get the Max MP of the L2Character
			int maxMp = getActiveChar().getStat().getMaxMp();
			
			if (newMp >= maxMp)
			{
				// Set the RegenActive flag to false
				_currentMp = maxMp;
				_flagsRegenActive &= ~REGEN_FLAG_MP;
				
				// Stop the HP/MP/CP Regeneration task
				if (_flagsRegenActive == 0)
					stopHpMpRegeneration();
			}
			else
			{
				// Set the RegenActive flag to true
				_currentMp = newMp;
				_flagsRegenActive |= REGEN_FLAG_MP;
				
				// Start the HP/MP/CP Regeneration task with Medium priority
				startHpMpRegeneration();
			}
		}
		
		// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		if (broadcastPacket)
			getActiveChar().broadcastStatusUpdate();
	}
	
	public final Set<L2Character> getStatusListener()
	{
		return _StatusListener;
	}
	
	class RegenTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				CharStat charstat = getActiveChar().getStat();
				
				// Modify the current CP of the L2Character and broadcast Server->Client packet StatusUpdate
				if (getCurrentCp() < charstat.getMaxCp())
					setCurrentCp(getCurrentCp() + Formulas.calcCpRegen((L2PcInstance) getActiveChar()), false);
				
				// Modify the current HP of the L2Character and broadcast Server->Client packet StatusUpdate
				if (getCurrentHp() < charstat.getMaxHp())
					setCurrentHp(getCurrentHp() + Formulas.calcHpRegen(getActiveChar()), false);
				
				// Modify the current MP of the L2Character and broadcast Server->Client packet StatusUpdate
				if (getCurrentMp() < charstat.getMaxMp())
					setCurrentMp(getCurrentMp() + Formulas.calcMpRegen(getActiveChar()), false);
				
				if (!getActiveChar().isInActiveRegion())
				{
					// no broadcast necessary for characters that are in inactive regions.
					// stop regeneration for characters who are filled up and in an inactive region.
					if ((getCurrentCp() == charstat.getMaxCp()) && (getCurrentHp() == charstat.getMaxHp()) && (getCurrentMp() == charstat.getMaxMp()))
						stopHpMpRegeneration();
				}
				else
					getActiveChar().broadcastStatusUpdate(); // send the StatusUpdate packet
			}
			catch (Throwable e)
			{
				_log.severe(RegenTask.class.getName() + ": Throwable: run");
				if (Config.DEVELOPER)
					e.printStackTrace();
			}
		}
	}
}