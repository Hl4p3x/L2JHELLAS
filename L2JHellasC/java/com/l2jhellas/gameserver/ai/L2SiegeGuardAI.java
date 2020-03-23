package com.l2jhellas.gameserver.ai;

import java.util.logging.Logger;

import com.l2jhellas.gameserver.model.actor.L2Character;

public class L2SiegeGuardAI extends L2AttackableAI
{
	protected static final Logger _log = Logger.getLogger(L2SiegeGuardAI.class.getName());
					
	public L2SiegeGuardAI(L2Character character)
	{
		super(character);		
	}

	@Override
	protected void thinkActive()
	{
		super.thinkActive();
	}
	
	@Override
	protected void thinkAttack()
	{
        super.thinkAttack();
	}
		
	@Override
	protected void onEvtThink()
	{
		 super.onEvtThink();
	}
	
	@Override
	protected void onEvtDead()
	{
		stopAITask();
		super.onEvtDead();
	}
	
	@Override
	public void stopAITask()
	{
		_actor.detachAI();
	}
}