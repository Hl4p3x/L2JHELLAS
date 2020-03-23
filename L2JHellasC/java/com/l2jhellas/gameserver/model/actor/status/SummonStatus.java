package com.l2jhellas.gameserver.model.actor.status;

import com.l2jhellas.gameserver.model.actor.L2Summon;

public class SummonStatus extends PlayableStatus
{
	public SummonStatus(L2Summon activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public L2Summon getActiveChar()
	{
		return (L2Summon) super.getActiveChar();
	}
}