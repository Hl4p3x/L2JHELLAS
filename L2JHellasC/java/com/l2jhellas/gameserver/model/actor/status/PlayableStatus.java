package com.l2jhellas.gameserver.model.actor.status;

import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Playable;

public class PlayableStatus extends CharStatus
{
	public PlayableStatus(L2Playable activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public void reduceHp(double value, L2Character attacker)
	{
		reduceHp(value, attacker, true);
	}
	
	@Override
	public void reduceHp(double value, L2Character attacker, boolean awake)
	{
		if (getActiveChar().isDead())
			return;
		
		super.reduceHp(value, attacker, awake);
		
	}
	
	@Override
	public L2Playable getActiveChar()
	{
		return (L2Playable) super.getActiveChar();
	}
}