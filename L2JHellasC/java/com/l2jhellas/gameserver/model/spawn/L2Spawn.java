package com.l2jhellas.gameserver.model.spawn;

import java.lang.reflect.Constructor;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.geodata.GeoEngine;
import com.l2jhellas.gameserver.idfactory.IdFactory;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2RaidBossInstance;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;
import com.l2jhellas.util.Rnd;

public class L2Spawn
{
	protected static final Logger _log = Logger.getLogger(L2Spawn.class.getName());
	
	boolean _canSpawnByDefault;
	SpawnTerritory _territory;
	private L2NpcTemplate _template;
	
	private int _id;
	
	// private String _location = DEFAULT_LOCATION;
	
	private String _EventName;
	private String _LocationName;
		
	private int _maximumCount;
	
	private int _currentCount;
	
	protected int _scheduledCount;
	
	private int _locX;
	
	private int _locY;
	
	private int _locZ;
	
	private int _heading;
	
	private int _respawnDelay;
	
	private int _respawnMinDelay;
	
	private int _respawnMaxDelay;
	
	private Constructor<?> _constructor;
	
	private boolean _doRespawn;
	private int _instanceId = 0;
	
	private L2Npc _lastSpawn;
	
	class SpawnTask implements Runnable
	{
		// L2NpcInstance _instance;
		// int _objId;
		private final L2Npc _oldNpc;
		
		public SpawnTask(L2Npc pOldNpc)
		{
			// _objId= objid;
			_oldNpc = pOldNpc;
		}
		
		@Override
		public void run()
		{
			try
			{
				// doSpawn();
				respawnNpc(_oldNpc);
			}
			catch (Exception e)
			{
				_log.warning(L2Spawn.class.getSimpleName() + ": " + e);
			}
			
			_scheduledCount--;
		}
	}
	
	public L2Spawn(L2NpcTemplate mobTemplate) throws SecurityException, ClassNotFoundException, NoSuchMethodException
	{
		// Set the _template of the L2Spawn
		_template = mobTemplate;
		
		if (_template == null)
			return;
		
		// The Name of the L2NpcInstance type managed by this L2Spawn
		String implementationName = _template.type; // implementing class name
		
		if (mobTemplate.npcId == 30995)
			implementationName = "L2RaceManager";
		
		// if (mobTemplate.npcId == 8050)
		
		if ((mobTemplate.npcId >= 31046) && (mobTemplate.npcId <= 31053))
			implementationName = "L2SymbolMaker";
		
		// Create the generic constructor of L2NpcInstance managed by this L2Spawn
		Class<?>[] parameters =
		{
			int.class,
			Class.forName("com.l2jhellas.gameserver.templates.L2NpcTemplate")
		};
		_constructor = Class.forName("com.l2jhellas.gameserver.model.actor.instance." + implementationName + "Instance").getConstructor(parameters);
	}
	
	public int getAmount()
	{
		return _maximumCount;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public String getEventName()
	{
		return _EventName;
	}
	
	public boolean EvtNameIsNotBlank()
	{
		return !_EventName.isEmpty();
	}
	
	public boolean LocNameIsNotBlank()
	{
		return !_LocationName.isEmpty();
	}
	
	public String getLocationName()
	{
		return _LocationName;
	}
		
	public int getLocx()
	{
		return _locX;
	}
	
	public int getLocy()
	{
		return _locY;
	}
	
	public int getLocz()
	{
		return _locZ;
	}
	
	public int getNpcid()
	{
		return _template.npcId;
	}
	
	public int getHeading()
	{
		return _heading;
	}
	
	public int getRespawnDelay()
	{
		return _respawnDelay;
	}
	
	public int getRespawnMinDelay()
	{
		return _respawnMinDelay;
	}
	
	public int getRespawnMaxDelay()
	{
		return _respawnMaxDelay;
	}
	
	public void setAmount(int amount)
	{
		_maximumCount = amount;
	}
	
	public void setId(int id)
	{
		_id = id;
	}
	
	public void setEventName(String eventname)
	{
		_EventName = eventname;
	}
	
	public void setLocationName(String locname)
	{
		_LocationName = locname;
	}
	
	public void setRespawnMinDelay(int date)
	{
		_respawnMinDelay = date;
	}
	
	public void setRespawnMaxDelay(int date)
	{
		_respawnMaxDelay = date;
	}
	
	public void setLocx(int locx)
	{
		_locX = locx;
	}
	
	public void setLocy(int locy)
	{
		_locY = locy;
	}
	
	public void setLocz(int locz)
	{
		_locZ = locz;
	}
	
	public void setHeading(int heading)
	{
		_heading = heading;
	}
	
	public void decreaseCount(L2Npc oldNpc)
	{
		// Decrease the current number of L2NpcInstance of this L2Spawn
		_currentCount--;
		
		// Check if respawn is possible to prevent multiple respawning caused by lag
		if (_doRespawn && _scheduledCount + _currentCount < _maximumCount)
		{
			// Update the current number of SpawnTask in progress or stand by of this L2Spawn
			_scheduledCount++;
			
			// Create a new SpawnTask to launch after the respawn Delay
			// ClientScheduler.getInstance().scheduleLow(new SpawnTask(npcId), _respawnDelay);
			ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(oldNpc), _respawnDelay);
		}
	}
	
	public int init()
	{
		while (_currentCount < _maximumCount)
		{
			doSpawn();
		}
		_doRespawn = true;
		
		return _currentCount;
	}
	
	public L2Npc spawnOne()
	{
		return doSpawn();
	}
	
	public void stopRespawn()
	{
		_doRespawn = false;
	}
	
	public void startRespawn()
	{
		_doRespawn = true;
	}
	
	public L2Npc doSpawn()
	{
		L2Npc mob = null;
		try
		{
			// Check if the L2Spawn is not a L2Pet or L2Minion spawn
			if (_template.type.equalsIgnoreCase("L2Pet") || _template.type.equalsIgnoreCase("L2Minion"))
			{
				_currentCount++;
				
				return mob;
			}
			
			// Get L2NpcInstance Init parameters and its generate an Identifier
			Object[] parameters =
			{
				IdFactory.getInstance().getNextId(),
				_template
			};
			
			// Call the constructor of the L2NpcInstance
			// (can be a L2ArtefactInstance, L2FriendlyMobInstance, L2GuardInstance, L2MonsterInstance, L2SiegeGuardInstance, L2BoxInstance,
			// L2FeedableBeastInstance, L2TamedBeastInstance, L2FolkInstance or L2TvTEventNpcInstance)
			Object tmp = _constructor.newInstance(parameters);
			
			// Must be done before object is spawned into visible world
			((L2Object) tmp).setInstanceId(_instanceId);
			
			// Check if the Instance is a L2NpcInstance
			if (!(tmp instanceof L2Npc))
				return mob;
			mob = (L2Npc) tmp;
			
			return intializeNpcInstance(mob);
		}
		catch (Exception e)
		{
			_log.warning(L2Spawn.class.getSimpleName() + ": NPC " + _template.npcId + " class not found");
		}
		return mob;
	}
	
	private L2Npc intializeNpcInstance(L2Npc mob)
	{
		int newlocx = getLocx();
		int newlocy = getLocy();
		int newlocz = getLocz();
		
		SpawnTerritory territory = getTerritory();
		
		if(territory != null)
		{
			Location terloc = territory.getRandomPoint();		
			newlocx = terloc.getX();
			newlocy = terloc.getY();
			newlocz = terloc.getZ();
			
			setLocx(newlocx);
			setLocy(newlocy);
			setLocz(newlocz);
		}
			
		if (Config.GEODATA && !mob.isFlying())
			newlocz = GeoEngine.getHeight(newlocx, newlocy, newlocz);
		
		mob.stopAllEffects();
		mob.setHeading(getHeading() < 0 ? Rnd.get(65536) : getHeading());
		
		// Reset decay info
		mob.setDecayed(false);

		mob.setIsDead(false);

		// Set the HP and MP of the L2NpcInstance to the max
		mob.setCurrentHpMp(mob.getMaxHp(), mob.getMaxMp());
		
		// setting up champion mobs
		if (((mob instanceof L2MonsterInstance && !(mob instanceof L2RaidBossInstance)) || (mob instanceof L2RaidBossInstance && Config.CHAMPION_BOSS)) && Config.CHAMPION_FREQUENCY > 0 && !mob.getTemplate().isQuestMonster() && mob.getLevel() >= Config.CHAMPION_MIN_LEVEL && mob.getLevel() <= Config.CHAMPION_MAX_LEVEL)
		{
			if (Rnd.get(100000) <= Config.CHAMPION_FREQUENCY)
				mob.setChampion(true);
		}
		else
			mob.setChampion(false);
		
		// Link the L2NpcInstance to this L2Spawn
		mob.setSpawn(this);
		
		// Init other values of the L2NpcInstance (ex : from its L2CharTemplate for INT, STR, DEX...) and add it in the world as a visible object
		mob.spawnMe(newlocx, newlocy, newlocz);
		
		_lastSpawn = mob;
		
		// Increase the current number of L2NpcInstance managed by this L2Spawn
		_currentCount++;
		return mob;
	}
	
	public void setRespawnDelay(int delay, int randomInterval)
	{
		if (delay < 10)
			delay = 10;
		
		_respawnDelay = (randomInterval > 0 ? Rnd.get(delay - randomInterval, delay) : delay) * 1000;
	}	
	
	public void setRespawnDelay(int i)
	{
		if (i < 10)
			i = 10;
		
		_respawnDelay = i * 1000;
	}
	
	public L2Npc getLastSpawn()
	{
		return _lastSpawn;
	}
	
	public void respawnNpc(L2Npc oldNpc)
	{
		oldNpc.refreshID();
		intializeNpcInstance(oldNpc);
	}
	
	public L2NpcTemplate getTemplate()
	{
		return _template;
	}
	
	public int getInstanceId()
	{
		return _instanceId;
	}
	
	public void setInstanceId(int instanceId)
	{
		_instanceId = instanceId;
	}
	
	public void setTerritory(SpawnTerritory territory)
	{
		_territory = territory;
	}
	
	public SpawnTerritory getTerritory()
	{
		return _territory;
	}
	
	public boolean hasTerritory()
	{
		return _territory != null;
	}
	
	public void setIsSpawningByDefault(boolean val)
	{
		_canSpawnByDefault = val;
	}
	
	public boolean isSpawningByDefault()
	{
		return _canSpawnByDefault;
	}
	
	public void setLoc(Location loc)
	{
		setLocx(loc.getX());
		setLocy(loc.getY());
		setLocz(loc.getZ());
	}
	
	public void setLoc(int x, int y, int z, int heading)
	{
		setLocx(x);
		setLocy(y);
		setLocz(z);
		setHeading(heading);
	}
}