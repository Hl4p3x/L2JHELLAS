package com.l2jhellas.gameserver.model.actor.status;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.stat.CharStat;
import com.l2jhellas.gameserver.skills.Formulas;
import com.l2jhellas.util.Rnd;

public class CharStatus
{
	protected static final Logger _log = Logger.getLogger(CharStatus.class.getName());
	
	private final L2Character _activeChar;
	private double _currentHp = 0; // Current HP of the L2Character
	private double _currentMp = 0; // Current MP of the L2Character
	
	private final Set<L2Character> _StatusListener = ConcurrentHashMap.newKeySet();
	
	private Future<?> _regTask;
	protected byte _flagsRegenActive = 0;
	protected static final byte REGEN_FLAG_CP = 4;
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
	
	public void reduceHp(double value, L2Character attacker)
	{
		reduceHp(value, attacker, true);
	}
	
	public void reduceHp(double value, L2Character attacker, boolean awake)
	{
		if (getActiveChar().isInvul() || getActiveChar().isDead())
			return;

		if (awake && getActiveChar().isSleeping())
			getActiveChar().stopSleeping(null);
		if (getActiveChar().isStunned() && Rnd.get(10) == 0)
			getActiveChar().stopStunning(null);
		if (!getActiveChar().isImmobileUntilAttacked())
			getActiveChar().stopImmobileUntilAttacked(null);
		
		if (getActiveChar().isImmobileUntilAttacked())
			getActiveChar().stopImmobileUntilAttacked(null);
		
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

			setCurrentHp(Math.max(getCurrentHp() - value, 0));
		}
		else
		{
			// If we're dealing with an L2Attackable Instance and the attacker's hit didn't kill the mob, clear the over-hit flag
			if (getActiveChar() instanceof L2Attackable)
				((L2Attackable) getActiveChar()).overhitEnabled(false);
		}
		
		if (getActiveChar().isDead())
		{
			getActiveChar().abortAllAttacks();
			getActiveChar().doDie(attacker);
		}
		else
		{
			// If we're dealing with an L2Attackable Instance and the attacker's hit didn't kill the mob, clear the over-hit flag
			if (getActiveChar() instanceof L2Attackable)
				((L2Attackable) getActiveChar()).overhitEnabled(false);
		}
	}
	
	public final void reduceMp(double value)
	{
		setCurrentMp(Math.max(getCurrentMp() - value, 0));
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
			_regTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(() -> startRegen(), period, period);
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
	
	public double getCurrentCp()
	{
		return 0;
	}
	
	public void setCurrentCp(double newCp)
	{
		
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
				
				if (!getActiveChar().isDead())
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
	
	
	public void reduceCp(int value)
	{
	}
	
	protected void startRegen()
	{
		final CharStat charstat = getActiveChar().getStat();
		
		if (getCurrentHp() < charstat.getMaxHp())
			setCurrentHp(getCurrentHp() + Formulas.calcHpRegen(getActiveChar()), false);
		
		if (getCurrentMp() < charstat.getMaxMp())
			setCurrentMp(getCurrentMp() + Formulas.calcMpRegen(getActiveChar()), false);
		
		if (!getActiveChar().isInActiveRegion())
		{
			if ((getCurrentHp() == charstat.getMaxHp()) && (getCurrentMp() == charstat.getMaxMp()))
				stopHpMpRegeneration();
		}
		else
			getActiveChar().broadcastStatusUpdate(); 
	}
}