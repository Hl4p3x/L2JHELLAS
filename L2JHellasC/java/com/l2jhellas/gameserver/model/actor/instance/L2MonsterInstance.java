package com.l2jhellas.gameserver.model.actor.instance;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.model.MinionList;
import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public class L2MonsterInstance extends L2Attackable
{
	private L2MonsterInstance _master;
	protected MinionList _minionList;
		
	private static final int MONSTER_MAINTENANCE_INTERVAL = 1000;
	
	public L2MonsterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if (attacker instanceof L2MonsterInstance)
			return false;
		
		return !isEventMob;
	}
	
	@Override
	public boolean isMonster()
	{
		return true;
	}
	
	@Override
	public boolean isAggressive()
	{
		return (getTemplate().aggroRange > 0) && !isEventMob;
	}
	
	@Override
	public void onSpawn()
	{		
		if (!isTeleporting())
		{
			if(getLeader() != null)
			{
				setIsRaidMinion(getLeader().isRaid());
				getLeader().getMinionList().onMinionSpawn(this);
			}
			
			if (hasMinions())
				getMinionList().deleteSpawnedMinions();
			
			if(getTemplate().getMinionData() != null)
			  manageMinions();

		}
		
		// dynamic script-based minions spawned here, after all preparations.
		super.onSpawn();
	}

	@Override
	public void onTeleported()
	{
		super.onTeleported();
		
		if (hasMinions())
			getMinionList().onMasterTeleported();
	}
	
	@Override
	public boolean returnHome()
	{
		return super.returnHome();
	}
	
	protected void manageMinions()
	{
		ThreadPoolManager.getInstance().scheduleGeneral(() -> getMinionList().spawnMinions() , getMaintenanceInterval());
	}
	
	protected int getMaintenanceInterval()
	{
		return MONSTER_MAINTENANCE_INTERVAL;
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;
		
		if (_master != null)
			_master.getMinionList().onMinionDie(this, _master.getSpawn().getRespawnDelay() / 2);
		
		return true;
	}
	
	@Override
	public void deleteMe()
	{
		abortAllAttacks();
		
		if (hasMinions())
			getMinionList().onMasterDie(true);
		else if (_master != null)
			_master.getMinionList().onMinionDie(this, 0);
		
		super.deleteMe();
	}
	
	@Override
	public L2MonsterInstance getLeader()
	{
		return _master;
	}
	
	public void setLeader(L2MonsterInstance leader)
	{
		_master = leader;
	}
	
	public boolean hasMinions()
	{
		return _minionList != null;
	}

	public MinionList getMinionList()
	{
		if (_minionList == null)
			_minionList = new MinionList(this);
		
		return _minionList;
	}
}