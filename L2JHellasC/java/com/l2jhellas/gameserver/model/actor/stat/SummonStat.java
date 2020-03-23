package com.l2jhellas.gameserver.model.actor.stat;

import com.l2jhellas.gameserver.model.actor.L2Summon;

public class SummonStat extends PlayableStat
{
	public SummonStat(L2Summon activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public L2Summon getActiveChar()
	{
		return (L2Summon) super.getActiveChar();
	}
}