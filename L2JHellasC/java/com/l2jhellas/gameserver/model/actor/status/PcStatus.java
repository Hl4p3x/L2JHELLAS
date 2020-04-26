package com.l2jhellas.gameserver.model.actor.status;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.enums.player.DuelState;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2SiegeGuardInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jhellas.gameserver.model.actor.stat.PcStat;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.Formulas;
import com.l2jhellas.gameserver.skills.Stats;
import com.l2jhellas.util.Util;

public class PcStatus extends PlayableStatus
{
	public PcStatus(L2PcInstance activeChar)
	{
		super(activeChar);
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
		
		if (attacker instanceof L2PcInstance)
		{
			if (getActiveChar().isInDuel())
			{
				// the duel is finishing - players do not receive damage
				if (getActiveChar().getDuelState() == DuelState.DEAD)
					return;
				else if (getActiveChar().getDuelState() == DuelState.WINNER)
					return;
				
				// cancel duel if player got hit by another player, that is not part of the duel
				if (((L2PcInstance) attacker).getDuelId() != getActiveChar().getDuelId())
					getActiveChar().setDuelState(DuelState.INTERRUPTED);
			}
			
			if (getActiveChar().isDead() && !getActiveChar().isFakeDeath())
				return;
		}
		else
		{
			// if attacked by a non L2PcInstance & non L2SummonInstance the duel gets canceled
			if (getActiveChar().isInDuel() && !(attacker instanceof L2SummonInstance))
				getActiveChar().setDuelState(DuelState.INTERRUPTED);
			if (getActiveChar().isDead())
				return;
		}
		
		int fullValue = (int) value;
		
		if (attacker != null && attacker != getActiveChar())
		{
			// Check and calculate transfered damage
			L2Summon summon = getActiveChar().getPet();
			
			if (summon != null && summon instanceof L2SummonInstance && Util.checkIfInRange(900, getActiveChar(), summon, true))
			{
				int tDmg = (int) value * (int) getActiveChar().getStat().calcStat(Stats.TRANSFER_DAMAGE_PERCENT, 0, null, null) / 100;
				
				// Only transfer dmg up to current HP, it should not be killed
				if (summon.getCurrentHp() < tDmg)
					tDmg = (int) summon.getCurrentHp() - 1;
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
		}
		
		super.reduceHp(value, attacker, awake);
		
		if (!getActiveChar().isDead() && getActiveChar().isSitting())
			getActiveChar().standUp();
		
		if (getActiveChar().isFakeDeath())
			getActiveChar().stopFakeDeath(null);
		
		if (attacker != null && attacker != getActiveChar() && fullValue > 0)
		{
			// Send a System Message to the L2PcInstance
			SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.S1_GAVE_YOU_S2_DMG);
			
			if (Config.DEBUG)
				_log.fine("Attacker:" + attacker.getName());
			
			if (attacker instanceof L2Npc)
			{
				int mobId = ((L2Npc) attacker).getTemplate().idTemplate;
				
				if (Config.DEBUG)
					_log.fine("mob id:" + mobId);
				
				smsg.addNpcName(mobId);
			}
			else if (attacker instanceof L2Summon)
			{
				int mobId = ((L2Summon) attacker).getTemplate().idTemplate;
				
				smsg.addNpcName(mobId);
			}
			else
			{
				smsg.addString(attacker.getName());
			}
			
			smsg.addNumber(fullValue);
			getActiveChar().sendPacket(smsg);
		}
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