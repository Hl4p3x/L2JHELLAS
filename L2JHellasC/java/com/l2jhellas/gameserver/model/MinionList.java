package com.l2jhellas.gameserver.model;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.datatables.sql.NpcData;
import com.l2jhellas.gameserver.idfactory.IdFactory;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;
import com.l2jhellas.util.Rnd;

public class MinionList
{	
	protected final L2MonsterInstance _master;
	private final Set<L2MonsterInstance> _minionReferences = ConcurrentHashMap.newKeySet();

	public MinionList(L2MonsterInstance master)
	{
		_master = master;
	}
	
	public Set<L2MonsterInstance> getSpawnedMinions()
	{
		return _minionReferences;
	}
	
	public final void spawnMinions()
	{
		if (_master.isAlikeDead())
			return;
		
		List<L2MinionData> minions = _master.getTemplate().getMinionData();
		if (minions == null)
			return;
		
		int minionCount, minionId, minionsToSpawn;
		for (L2MinionData minion : minions)
		{
			minionCount = minion.getAmount();
			minionId = minion.getMinionId();
			
			minionsToSpawn = minionCount - countSpawnedMinionsById(minionId);
			if (minionsToSpawn > 0)
			{
				for (int i = 0; i < minionsToSpawn; i++)
					spawnMinion(_master, minionId);
			}
		}
	}
	
	public void deleteSpawnedMinions()
	{
		if (!_minionReferences.isEmpty())
		{
			for (L2MonsterInstance minion : _minionReferences)
			{
				if (minion != null)
				{
					minion.setLeader(null);
					minion.deleteMe();
				}
			}
			_minionReferences.clear();
		}
	}

	public void onMinionSpawn(L2MonsterInstance minion)
	{
		_minionReferences.add(minion);
	}
	
	public void onMasterDie(boolean force)
	{
		if (_master.isRaid() || force)
			deleteSpawnedMinions();
	}
	
	public void onMinionDie(L2MonsterInstance minion, int respawnTime)
	{
		minion.setLeader(null);
		_minionReferences.remove(minion);
		
		final int time = _master.isRaid() ? (int) Config.RAID_MINION_RESPAWN_TIMER : respawnTime;
		if (time > 0 && !_master.isAlikeDead())
			ThreadPoolManager.getInstance().scheduleGeneral(new MinionRespawnTask(minion), time);
	}
	
	public void onAssist(L2Character caller, L2Character attacker)
	{
		if (attacker == null)
			return;
		
		if (!_master.isAlikeDead() && !_master.isInCombat())
			_master.addDamageHate(attacker, 0, 1);
		
		final boolean callerIsMaster = caller == _master;
		int aggro = callerIsMaster ? 10 : 1;
		if (_master.isRaid())
			aggro *= 10;
		
		for (L2MonsterInstance minion : _minionReferences)
		{
			if (minion != null && !minion.isDead() && (callerIsMaster || !minion.isInCombat()))
				minion.addDamageHate(attacker, 0, aggro);
		}
	}
	
	public void onMasterTeleported()
	{
		final int offset = 200;
		final int minRadius = _master.getCollisionRadius() + 30;
		
		for (L2MonsterInstance minion : _minionReferences)
		{
			if (minion != null && !minion.isDead() && !minion.isMovementDisabled())
			{
				int newX = Rnd.get(minRadius * 2, offset * 2); // x
				int newY = Rnd.get(newX, offset * 2); // distance
				newY = (int) Math.sqrt(newY * newY - newX * newX); // y
				if (newX > offset + minRadius)
					newX = _master.getX() + newX - offset;
				else
					newX = _master.getX() - newX + minRadius;
				if (newY > offset + minRadius)
					newY = _master.getY() + newY - offset;
				else
					newY = _master.getY() - newY + minRadius;
				
				minion.teleToLocation(newX, newY, _master.getZ(), false);
			}
		}
	}
	
	private final class MinionRespawnTask implements Runnable
	{
		private final L2MonsterInstance _minion;
		
		public MinionRespawnTask(L2MonsterInstance minion)
		{
			_minion = minion;
		}
		
		@Override
		public void run()
		{
			if (!_master.isAlikeDead() && _master.isVisible())
			{
				// minion can be already spawned or deleted
				if (!_minion.isVisible())
				{
					_minion.refreshID();
					initializeNpcInstance(_master, _minion);
				}
			}
		}
	}
	
	public static final L2MonsterInstance spawnMinion(L2MonsterInstance master, int minionId)
	{
		// Get the template of the Minion to spawn
		L2NpcTemplate minionTemplate = NpcData.getInstance().getTemplate(minionId);
		if (minionTemplate == null)
			return null;
		
		// Create and Init the Minion and generate its Identifier
		L2MonsterInstance minion = new L2MonsterInstance(IdFactory.getInstance().getNextId(), minionTemplate);
		return initializeNpcInstance(master, minion);
	}
	
	protected static final L2MonsterInstance initializeNpcInstance(L2MonsterInstance master, L2MonsterInstance minion)
	{
		minion.stopAllEffects();
		minion.setDecayed(false);
		minion.setIsDead(false);

		// Set the Minion HP, MP and Heading
		minion.setCurrentHpMp(minion.getMaxHp(), minion.getMaxMp());
		minion.setHeading(master.getHeading());
		
		// Set the Minion leader to this RaidBoss
		minion.setLeader(master);
		
		// Init the position of the Minion and add it in the world as a visible object
		final int offset = 100 + minion.getCollisionRadius() + master.getCollisionRadius();
		final int minRadius = master.getCollisionRadius() + 30;
		
		int newX = Rnd.get(minRadius * 2, offset * 2); // x
		int newY = Rnd.get(newX, offset * 2); // distance
		newY = (int) Math.sqrt(newY * newY - newX * newX); // y
		if (newX > offset + minRadius)
			newX = master.getX() + newX - offset;
		else
			newX = master.getX() - newX + minRadius;
		if (newY > offset + minRadius)
			newY = master.getY() + newY - offset;
		else
			newY = master.getY() - newY + minRadius;
		
		minion.spawnMe(newX, newY, master.getZ());

		return minion;
	}
	
	private final int countSpawnedMinionsById(int minionId)
	{
		return (int) _minionReferences.stream().filter(minion -> minion.getNpcId() == minionId).count();
	}
}