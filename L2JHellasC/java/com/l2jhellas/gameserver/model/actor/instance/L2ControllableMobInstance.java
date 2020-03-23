package com.l2jhellas.gameserver.model.actor.instance;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.ai.L2CharacterAI;
import com.l2jhellas.gameserver.ai.L2ControllableMobAI;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public class L2ControllableMobInstance extends L2MonsterInstance
{
	private boolean _isInvul;
	private L2ControllableMobAI _aiBackup; // to save ai, avoiding being detached
	

	@Override
	public void detachAI()
	{
			// do nothing, AI of controllable mobs can't be detached automatically
	}

	
	@Override
	public boolean isAggressive()
	{
		return true;
	}
	
	@Override
	public int getAggroRange()
	{
		// force mobs to be aggressive
		return 500;
	}
	
	public L2ControllableMobInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public L2CharacterAI getAI()
	{
		if (_ai == null)
		{
			synchronized (this)
			{
				if ((_ai == null) && (_aiBackup == null))
				{
					_ai = new L2ControllableMobAI(this);
					_aiBackup = (L2ControllableMobAI) _ai;
				}
				else
				{
					_ai = _aiBackup;
				}
			}
		}
		return _ai;
	}
	
	@Override
	public boolean isInvul()
	{
		return _isInvul;
	}
	
	public void setInvul(boolean isInvul)
	{
		_isInvul = isInvul;
	}
	
	@Override
	public void reduceCurrentHp(double i, L2Character attacker, boolean awake)
	{
		if (isInvul() || isDead())
			return;
		
		if (awake)
			stopSleeping(null);
		
		if (!isImmobileUntilAttacked())
			stopImmobileUntilAttacked(null);
		
		i = getCurrentHp() - i;
		
		if (i < 0)
			i = 0;
		
		setCurrentHp(i);
		
		if (isDead())
		{
			// first die (and calculate rewards), if currentHp < 0,
			// then overhit may be calculated
			if (Config.DEBUG)
				_log.fine("char is dead.");
			
			stopMove(null);
			
			// Start the doDie process
			doDie(attacker);
			
			// now reset currentHp to zero
			setCurrentHp(0);
		}
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;
		
		removeAI();
		return true;
	}
	
	@Override
	public void deleteMe()
	{
		removeAI();
		super.deleteMe();
	}
	
	protected void removeAI()
	{
		synchronized (this)
		{
			if (_aiBackup != null)
			{
				_aiBackup.setIntention(CtrlIntention.AI_INTENTION_IDLE);
				_aiBackup = null;
				_ai = null;
			}
		}
	}
}