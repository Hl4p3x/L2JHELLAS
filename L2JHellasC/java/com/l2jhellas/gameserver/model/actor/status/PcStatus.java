package com.l2jhellas.gameserver.model.actor.status;

import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.enums.ZoneId;
import com.l2jhellas.gameserver.enums.player.DuelState;
import com.l2jhellas.gameserver.instancemanager.DuelManager;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2SiegeGuardInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jhellas.gameserver.model.actor.stat.PcStat;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.Formulas;
import com.l2jhellas.gameserver.skills.Stats;
import com.l2jhellas.util.Rnd;
import com.l2jhellas.util.Util;

public class PcStatus extends PlayableStatus
{
	private double _currentCp = 0;

	public PcStatus(L2PcInstance activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public final void reduceCp(int value)
	{
		if (getCurrentCp() > value)
			setCurrentCp(getCurrentCp() - value);
		else
			setCurrentCp(0);
	}
	
	@Override
	public final void reduceHp(double value, L2Character attacker)
	{
		reduceHp(value, attacker, true);
	}
	
	@Override
	public final void reduceHp(double value, L2Character attacker, boolean awake)
	{
		if (getActiveChar().isInvul() || getActiveChar().isDead())
			return;
		
		if (getActiveChar().isSitting())
			getActiveChar().standUp();
		
		if (getActiveChar().isFakeDeath())
			getActiveChar().stopFakeDeath(null);
					
		if (awake && getActiveChar().isSleeping())
			getActiveChar().stopSleeping(null);
		if (getActiveChar().isStunned() && Rnd.get(10) == 0)
			getActiveChar().stopStunning(null);
		if (!getActiveChar().isImmobileUntilAttacked())
			getActiveChar().stopImmobileUntilAttacked(null);
		
		if (getActiveChar().isImmobileUntilAttacked())
			getActiveChar().stopImmobileUntilAttacked(null);
		
		int fullValue = (int) value;
		
		if (attacker != null && attacker != getActiveChar())
		{		
			final L2PcInstance attackerPlayer = attacker.getActingPlayer();

			if (getActiveChar().isInDuel())
			{
				if (getActiveChar().getDuelState() == DuelState.DEAD || getActiveChar().getDuelState() == DuelState.WINNER)
					return;

				if (attackerPlayer == null || attackerPlayer.getDuelId() != getActiveChar().getDuelId() || getActiveChar().getDuelState() != DuelState.DUELLING)
					getActiveChar().setDuelState(DuelState.INTERRUPTED);
			}
			
			// Check and calculate transfered damage
			L2Summon summon = getActiveChar().getPet();
			int tDmg = 0;
			if (summon != null && summon instanceof L2SummonInstance && Util.checkIfInRange(900, getActiveChar(), summon, true))
			{
				tDmg = (int) value * (int) getActiveChar().getStat().calcStat(Stats.TRANSFER_DAMAGE_PERCENT, 0, null, null) / 100;
				
				tDmg = Math.min((int) summon.getCurrentHp() - 1, tDmg);
				if (tDmg > 0)
				{
					summon.reduceCurrentHp(tDmg, attacker);
					value -= tDmg;
					fullValue = (int) value; // reduce the announced value here as player will get a message about summon damage
				}
			}
			
			if (attacker instanceof L2Playable || attacker instanceof L2SiegeGuardInstance)
			{
				if (getCurrentCp() >= value)
				{
					setCurrentCp(getCurrentCp() - value); // Set Cp to diff of Cp vs value
					value = 0; // No need to subtract anything from Hp
				}
				else
				{
					value -= getCurrentCp(); // Get diff from value vs Cp; will apply diff to Hp
					setCurrentCp(0); // Set Cp to 0
				}
			}
			
			if (fullValue > 0)
			{
				getActiveChar().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_GAVE_YOU_S2_DMG).addCharName(attacker).addNumber(fullValue));

				if (tDmg > 0 && attackerPlayer != null)
					attackerPlayer.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.GIVEN_S1_DAMAGE_TO_YOUR_TARGET_AND_S2_DAMAGE_TO_SERVITOR).addNumber(fullValue).addNumber(tDmg));
			}
		}
				
		if (value > 0)
		{
			value = getCurrentHp() - value;
			if (value <= 0)
			{
				if (getActiveChar().isInDuel())
				{
					if (getActiveChar().getDuelState() == DuelState.DUELLING)
					{
						getActiveChar().disableAllSkills();
						stopHpMpRegeneration();
						
						getActiveChar().setIsDead(true);

						if (attacker != null)
						{
							attacker.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
							attacker.sendPacket(ActionFailed.STATIC_PACKET);
						}
						
						DuelManager.getInstance().onPlayerDefeat(getActiveChar());
						
					}
					value = 1;
				}
				else
				{
				  boolean isInside = (getActiveChar().isInsideZone(ZoneId.PEACE) && attacker != null && !attacker.isInsideZone(ZoneId.PEACE));			
				  value = isInside ? 1 : 0;
				}
			}
			setCurrentHp(value);
		}
				
		if (getActiveChar().getCurrentHp() < 0.5)
		{
			if (getActiveChar().isInOlympiadMode())
			{
				getActiveChar().abortAllAttacks();
				
				stopHpMpRegeneration();
				
				final L2Summon summon = getActiveChar().getPet();
				
				if (summon != null)
				    summon.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			
				return;
			}
			
			getActiveChar().doDie(attacker);
		}
	}
	
	@Override
	public final double getCurrentCp()
	{
		return _currentCp;
	}
	
	@Override
	public final void setCurrentCp(double newCp)
	{
		setCurrentCp(newCp, true);
	}
	
	public final void setCurrentCp(double newCp, boolean broadcastPacket)
	{
		int currentCp = (int) getCurrentCp();
		int maxCp = getActiveChar().getStat().getMaxCp();
		
		synchronized (this)
		{	
			if (getActiveChar().isDead())
				return;
			
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
		if (currentCp != _currentCp && broadcastPacket)
			getActiveChar().broadcastStatusUpdate();
	}
	
	@Override
	protected void startRegen()
	{
		final PcStat pcStat = getActiveChar().getStat();
		
		if (getCurrentCp() < pcStat.getMaxCp())
			setCurrentCp(getCurrentCp() + Formulas.calcCpRegen(getActiveChar()), false);
		
		if (getCurrentHp() < pcStat.getMaxHp())
			setCurrentHp(getCurrentHp() + Formulas.calcHpRegen(getActiveChar()), false);
		
		if (getCurrentMp() < pcStat.getMaxMp())
			setCurrentMp(getCurrentMp() + Formulas.calcMpRegen(getActiveChar()), false);
		
		getActiveChar().broadcastStatusUpdate();
	}
	
	@Override
	public L2PcInstance getActiveChar()
	{
		return (L2PcInstance) super.getActiveChar();
	}
}