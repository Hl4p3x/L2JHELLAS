package com.l2jhellas.gameserver.model.actor.instance;

import java.util.concurrent.ScheduledFuture;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.model.MinionList;
import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public class L2MonsterInstance extends L2Attackable
{
	private L2MonsterInstance _master;
	protected MinionList _minionList;
	
	protected ScheduledFuture<?> _minionMaintainTask = null;
	
	private static final int MONSTER_MAINTENANCE_INTERVAL = 1000;
	
	public L2MonsterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		_minionList = new MinionList(this);
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if (attacker instanceof L2MonsterInstance)
			return false;
		
		return !isEventMob;
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
			if (_master != null)
			{
				setIsRaidMinion(_master.isRaid());
				_master.getMinionList().onMinionSpawn(this);
			}
			// delete spawned minions before dynamic minions spawned by script
			else if (_minionList != null)
				getMinionList().deleteSpawnedMinions();
			
			manageMinions();
		}
		
		// dynamic script-based minions spawned here, after all preparations.
		super.onSpawn();
	}
	
	@Override
	public boolean returnHome()
	{
		return super.returnHome();
	}
	
	protected void manageMinions()
	{
		_minionMaintainTask = ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				_minionList.spawnMinions();
			}
		}, getMaintenanceInterval());
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
		
		if (_minionList != null)
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