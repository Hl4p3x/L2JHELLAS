package com.l2jhellas.gameserver.model;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ai.CtrlEvent;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.ai.L2CharacterAI;
import com.l2jhellas.gameserver.datatables.sql.CharNameTable;
import com.l2jhellas.gameserver.geometry.Point3D;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PetInstance;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.network.serverpackets.DeleteObject;
import com.l2jhellas.util.Util;

public final class L2World
{
	// Geodata min/max tiles
	public static final int TILE_X_MIN = 15;
	public static final int TILE_X_MAX = 26;
	public static final int TILE_Y_MIN = 10;
	public static final int TILE_Y_MAX = 26;
	
	// Map dimensions
	public static final int TILE_SIZE = 32768;
	public static final int WORLD_X_MIN = (TILE_X_MIN - 20) * TILE_SIZE;
	public static final int WORLD_X_MAX = (TILE_X_MAX - 19) * TILE_SIZE;
	public static final int WORLD_Y_MIN = (TILE_Y_MIN - 18) * TILE_SIZE;
	public static final int WORLD_Y_MAX = (TILE_Y_MAX - 17) * TILE_SIZE;
	public static final int WORLD_Z_MIN = -32768;
	public static final int WORLD_Z_MAX = 32767;
	
	// Regions and offsets
	public static final int REGION_SIZE = 4096;
	public static final int REGIONS_X = (WORLD_X_MAX - WORLD_X_MIN) / REGION_SIZE;
	public static final int REGIONS_Y = (WORLD_Y_MAX - WORLD_Y_MIN) / REGION_SIZE;
	public static final int REGIONS_Z = (WORLD_Z_MAX - WORLD_Z_MIN) / REGION_SIZE;
	
	private static final int REGION_X_OFFSET = Math.abs(WORLD_X_MIN / REGION_SIZE);
	private static final int REGION_Y_OFFSET = Math.abs(WORLD_Y_MIN / REGION_SIZE);
	public static final int REGION_Z_OFFSET = Math.abs(WORLD_Z_MIN / REGION_SIZE);
	
	private static Logger _log = Logger.getLogger(L2World.class.getName());
	
	public static final int SHIFT_BY = 11;
	public static final int SHIFT_BY_Z = 10;
	
	public static final int WORLD_SIZE_X = Config.GEO_X_LAST - Config.GEO_X_FIRST + 1;
	public static final int WORLD_SIZE_Y = Config.GEO_Y_LAST - Config.GEO_Y_FIRST + 1;
	
	public static final int REGION_MIN_DIMENSION = Math.min(32768 / (32768 >> SHIFT_BY), 32768 / (32768 >> SHIFT_BY));
	
	private final Map<Integer, L2PcInstance> _allPlayers = new ConcurrentHashMap<>();
	private final Map<Integer, L2Object> _allObjects = new ConcurrentHashMap<>();
	private final Map<Integer, L2PetInstance> _petsInstance = new ConcurrentHashMap<>();
	
	private final L2WorldRegion[][][] _worldRegions = new L2WorldRegion[REGIONS_X + 1][REGIONS_Y + 1][REGIONS_Z + 1];
	
	protected L2World()
	{	
		initRegions();
	}
	
	public static L2World getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public L2WorldRegion getRegion(Point3D point)
	{
		return getRegion(point.getX(), point.getY(), point.getZ());
	}
	
	public L2WorldRegion getRegion(L2Object point)
	{
		return getRegion(point.getX(), point.getY(), point.getZ());
	}
	
	public L2WorldRegion getRegion(int x, int y, int z)
	{
		return _worldRegions[(x - WORLD_X_MIN) / REGION_SIZE][(y - WORLD_Y_MIN) / REGION_SIZE][(z - WORLD_Z_MIN) / REGION_SIZE];
	}
	
	public static int getRegionX(int regionX)
	{
		return (regionX - REGION_X_OFFSET) * REGION_SIZE;
	}
	
	public static int getRegionY(int regionY)
	{
		return (regionY - REGION_Y_OFFSET) * REGION_SIZE;
	}
	
	public static int getRegionZ(int regionZ)
	{
		return (regionZ - REGION_Z_OFFSET) * REGION_SIZE;
	}
	
	public void storeObject(L2Object object)
	{
		_allObjects.putIfAbsent(object.getObjectId(), object);
		
		if (object instanceof L2PcInstance)
		{
			L2PcInstance player = object.getActingPlayer();
			if (!player.isTeleporting())
			{
				final L2PcInstance old = getPlayer(player.getObjectId());
				if (old != null)
				{
					player.closeNetConnection(false);
					old.closeNetConnection(false);
					return;
				}
				addToAllPlayers(player);
			}
		}
		
	}
	
	public void removeObject(L2Object object)
	{
		_allObjects.remove(object.getObjectId());
		
		if (object instanceof L2PcInstance)
		{
			final L2PcInstance player = (L2PcInstance) object;
			if (player.isTeleporting()) // TODO: drop when we stop removing player from the world while teleportingq.
			{
				return;
			}
			_allPlayers.remove(object.getObjectId());
		}
	}
	
	public L2Object findObject(int objectId)
	{
		return _allObjects.get(objectId);
	}
	
	public final Map<Integer, L2Object> getAllVisibleObjects()
	{
		return _allObjects;
	}
	
	public final int getAllVisibleObjectsCount()
	{
		return _allObjects.size();
	}
	
	public Map<Integer, L2PcInstance> getAllPlayers()
	{
		return _allPlayers;
	}
	
	public int getAllPlayersCount()
	{
		return _allPlayers.size();
	}
	
	public L2PcInstance getPlayer(String name)
	{
		return getPlayer(CharNameTable.getInstance().getIdByName(name));
	}
	
	public L2PcInstance getPlayer(int objectId)
	{
		return _allPlayers.get(objectId);
	}
	
	public L2PetInstance getPet(int ownerId)
	{
		return _petsInstance.get(ownerId);
	}
	
	public L2PetInstance addPet(int ownerId, L2PetInstance pet)
	{
		return _petsInstance.putIfAbsent(ownerId, pet);
	}
	
	public void removePet(int ownerId)
	{
		_petsInstance.remove(ownerId);
	}
	
	public void addToAllPlayers(L2PcInstance cha)
	{
		_allPlayers.putIfAbsent(cha.getObjectId(), cha);
	}
	
	public void removeFromAllPlayers(L2PcInstance cha)
	{
		_allPlayers.remove(cha.getObjectId());
	}
	
	public L2WorldRegion getRegion(Location point)
	{
		return getRegion(point.getX(), point.getY(), point.getZ());
	}
	
	public L2WorldRegion[][][] getAllWorldRegions()
	{
		return _worldRegions;
	}
	
	public static boolean validRegion(int x, int y, int z)
	{
		return ((x >= 0) && (x <= REGIONS_X) && (y >= 0) && (y <= REGIONS_Y)) && (z >= 0) && (z <= REGIONS_Z);
	}
	
	private void initRegions()
	{
		for (int x = 0; x <= REGIONS_X; x++)
		{
			for (int y = 0; y <= REGIONS_Y; y++)
			{
				for (int z = 0; z <= REGIONS_Z; z++)
				{
					_worldRegions[x][y][z] = new L2WorldRegion(x, y, z);
				}
			}
		}
		
		_log.info("L2World: WorldRegion grid (" + REGIONS_X + " by " + REGIONS_Y + " by " + REGIONS_Z + ") is now setted up.");
	}
	
	public void deleteVisibleNpcSpawns()
	{
		_log.info("Deleting all visible NPC's.");
		
		for (int x = 0; x <= REGIONS_X; x++)
		{
			for (int y = 0; y <= REGIONS_Y; y++)
			{
				for (int z = 0; z <= REGIONS_Z; z++)
				{
					_worldRegions[x][y][z].deleteVisibleNpcSpawns();
				}
			}
		}
		
		_log.info("All visible NPC's deleted.");
	}
	
	public void addVisibleObject(L2Object object, L2WorldRegion newRegion)
	{
		if (!newRegion.isActive())
			return;
		
		forEachVisibleObject(object, L2Object.class, 1, wo ->
		{
			if (object instanceof L2PcInstance && wo.isVisible())
			{
				wo.sendInfo((L2PcInstance) object);
				
				if (wo instanceof L2Character)
				{
					final L2CharacterAI ai = ((L2Character) wo).getAI();
					
					if (ai != null)
					{
						ai.describeStateToPlayer((L2PcInstance) object);
						
						if (wo instanceof L2MonsterInstance)
						{
							if (ai.getIntention() == CtrlIntention.AI_INTENTION_IDLE)
								ai.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
						}
					}
				}
			}
			
			if (wo instanceof L2PcInstance && object.isVisible())
			{
				object.sendInfo((L2PcInstance) wo);
				
				if (object instanceof L2Character)
				{
					final L2CharacterAI ai = ((L2Character) object).getAI();
					
					if (ai != null)
					{
						ai.describeStateToPlayer((L2PcInstance) wo);
						
						if (object instanceof L2MonsterInstance)
						{
							if (ai.getIntention() == CtrlIntention.AI_INTENTION_IDLE)
								ai.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
						}
					}
				}
			}
		});
	}
	
	public static void removeVisibleObject(L2Object object, L2WorldRegion oldRegion)
	{
		if (object == null)
			return;
		
		if (oldRegion != null)
		{
			oldRegion.removeVisibleObject(object);
			
			// Go through all surrounding WorldRegion Creatures
			oldRegion.forEachSurroundingRegion(w ->
			{
				for (L2Object wo : w.getVisibleObjects().values())
				{
					if (wo == object)
						continue;
					
					if (object instanceof L2Character)
					{
						final L2Character objectCreature = (L2Character) object;
						final L2CharacterAI ai = objectCreature.getAI();
						
						if (ai != null)
							ai.notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, wo);
						
						if (objectCreature.getTarget() == wo)
							objectCreature.setTarget(null);
						
						if (object instanceof L2PcInstance)
							((L2PcInstance) object).sendPacket(new DeleteObject(wo,(wo instanceof L2PcInstance) && ((L2PcInstance) wo).isSeated()));
					}
					
					if (wo instanceof L2Character)
					{
						final L2Character woCreature = (L2Character) wo;
						final L2CharacterAI ai = woCreature.getAI();
						
						if (ai != null)
							ai.notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, object);
						
						if (woCreature.getTarget() == object)
							woCreature.setTarget(null);
						
						if (wo instanceof L2PcInstance)
							((L2PcInstance) wo).sendPacket(new DeleteObject(object,(object instanceof L2PcInstance) && ((L2PcInstance) object).isSeated()));
					}
				}
				return true;
			});
		}
	}
	
	public static void switchRegion(L2Object object, L2WorldRegion newRegion)
	{
		final L2WorldRegion oldRegion = object.getWorldRegion();
		
		if ((oldRegion == null) || (oldRegion == newRegion))
			return;
		
		oldRegion.forEachSurroundingRegion(w ->
		{
			if (!newRegion.isSurroundingRegion(w))
			{
				for (L2Object wo : w.getVisibleObjects().values())
				{
					if (wo == object)
						continue;
					
					if (object instanceof L2Character)
					{
						final L2Character objectCreature = (L2Character) object;
						final L2CharacterAI ai = objectCreature.getAI();
						
						if (ai != null)
							ai.notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, wo);
						
						if (objectCreature.getTarget() == wo)
							objectCreature.setTarget(null);
						
						if (object instanceof L2PcInstance)
							((L2PcInstance) object).sendPacket(new DeleteObject(wo,(wo instanceof L2PcInstance) && ((L2PcInstance) wo).isSeated()));
					}
					
					if (wo instanceof L2Character)
					{
						final L2Character woCreature = (L2Character) wo;
						final L2CharacterAI ai = woCreature.getAI();
						
						if (ai != null)
							ai.notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, object);
						
						if (woCreature.getTarget() == object)
							woCreature.setTarget(null);
						
						if (wo instanceof L2PcInstance)
							((L2PcInstance) wo).sendPacket(new DeleteObject(object,(object instanceof L2PcInstance) && ((L2PcInstance) object).isSeated()));
					}
				}
			}
			return true;
		});
		
		newRegion.forEachSurroundingRegion(w ->
		{
			if (!oldRegion.isSurroundingRegion(w))
			{
				for (L2Object wo : w.getVisibleObjects().values())
				{
					if ((wo == object) || (wo.getInstanceId() != object.getInstanceId()))
						continue;
					
					if (object instanceof L2PcInstance && wo.isVisible())
					{
						wo.sendInfo((L2PcInstance) object);
						
						if (wo instanceof L2Character)
						{
							final L2CharacterAI ai = ((L2Character) wo).getAI();
							
							if (ai != null)
							{
								ai.describeStateToPlayer((L2PcInstance) object);
								
								if (wo instanceof L2MonsterInstance)
								{
									if (ai.getIntention() == CtrlIntention.AI_INTENTION_IDLE)
										ai.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
								}
							}
						}
					}
					
					if (wo instanceof L2PcInstance && object.isVisible())
					{
						object.sendInfo((L2PcInstance) wo);
						
						if (object instanceof L2Character)
						{
							final L2CharacterAI ai = ((L2Character) object).getAI();
							if (ai != null)
							{
								ai.describeStateToPlayer((L2PcInstance) wo);
								
								if (object instanceof L2MonsterInstance)
								{
									if (ai.getIntention() == CtrlIntention.AI_INTENTION_IDLE)
										ai.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
								}
							}
						}
					}
				}
			}
			return true;
		});
	}
	
	private <T extends L2Object> void forEachVisibleObject(L2Object object, Class<T> clazz, int depth, Consumer<T> c)
	{
		if (object == null)
			return;
		
		final L2WorldRegion centerWorldRegion = getRegion(object);
		
		if (centerWorldRegion == null)
			return;
		
		for (int x = Math.max(centerWorldRegion.getRegionX() - depth, 0); x <= Math.min(centerWorldRegion.getRegionX() + depth, REGIONS_X); x++)
		{
			for (int y = Math.max(centerWorldRegion.getRegionY() - depth, 0); y <= Math.min(centerWorldRegion.getRegionY() + depth, REGIONS_Y); y++)
			{
				for (int z = Math.max(centerWorldRegion.getRegionZ() - depth, 0); z <= Math.min(centerWorldRegion.getRegionZ() + depth, REGIONS_Z); z++)
				{
					for (L2Object visibleObject : _worldRegions[x][y][z].getVisibleObjects().values())
					{
						if ((visibleObject == null) || (visibleObject == object) || !clazz.isInstance(visibleObject))
							continue;
						
						if (visibleObject.getInstanceId() != object.getInstanceId())
							continue;
						
						c.accept(clazz.cast(visibleObject));
					}
				}
			}
		}
	}
	
	public <T extends L2Object> void forEachVisibleObject(L2Object object, Class<T> clazz, Consumer<T> c)
	{
		forEachVisibleObject(object, clazz, 1, c);
	}
	
	public <T extends L2Object> void forEachVisibleObjectInRange(L2Object object, Class<T> clazz, int range, Consumer<T> c)
	{
		if (object == null)
			return;
		
		final L2WorldRegion centerWorldRegion = getRegion(object);
		if (centerWorldRegion == null)
			return;
		
		final int depth = (range / REGION_MIN_DIMENSION) + 1;
		for (int x = Math.max(centerWorldRegion.getRegionX() - depth, 0); x <= Math.min(centerWorldRegion.getRegionX() + depth, REGIONS_X); x++)
		{
			for (int y = Math.max(centerWorldRegion.getRegionY() - depth, 0); y <= Math.min(centerWorldRegion.getRegionY() + depth, REGIONS_Y); y++)
			{
				for (int z = Math.max(centerWorldRegion.getRegionZ() - depth, 0); z <= Math.min(centerWorldRegion.getRegionZ() + depth, REGIONS_Z); z++)
				{
					
					final int x1 = (getRegionX(x));
					final int y1 = (getRegionY(y));
					final int z1 = (getRegionZ(z));
					
					final int x2 = (getRegionX(x + 1));
					final int y2 = (getRegionY(y + 1));
					final int z2 = (getRegionZ(z + 1));
					
					if (Util.cubeIntersectsSphere(x1, y1, z1, x2, y2, z2, object.getX(), object.getY(), object.getZ(), range))
					{
						for (L2Object visibleObject : _worldRegions[x][y][z].getVisibleObjects().values())
						{
							if ((visibleObject == null) || (visibleObject == object) || !clazz.isInstance(visibleObject))
								continue;
							
							if (visibleObject.getInstanceId() != object.getInstanceId())
								continue;
							
							if (visibleObject.calculateDistance(object, true, false) <= range)
								c.accept(clazz.cast(visibleObject));
						}
					}
				}
			}
		}
	}
	
	public <T extends L2Object> List<T> getVisibleObjects(L2Object object, Class<T> clazz)
	{
		final List<T> result = new LinkedList<>();
		forEachVisibleObject(object, clazz, result::add);
		return result;
	}
	
	public <T extends L2Object> List<T> getVisibleObjects(L2Object object, Class<T> clazz, Predicate<T> predicate)
	{
		final List<T> result = new LinkedList<>();
		forEachVisibleObject(object, clazz, o ->
		{
			if (predicate.test(o))
				result.add(o);
		});
		return result;
	}
	
	public <T extends L2Object> List<T> getVisibleObjects(L2Object object, Class<T> clazz, int range)
	{
		final List<T> result = new LinkedList<>();
		forEachVisibleObjectInRange(object, clazz, range, result::add);
		return result;
	}
	
	public <T extends L2Object> List<T> getVisibleObjects(L2Object object, Class<T> clazz, int range, Predicate<T> predicate)
	{
		final List<T> result = new LinkedList<>();
		forEachVisibleObjectInRange(object, clazz, range, o ->
		{
			if (predicate.test(o))
				result.add(o);
		});
		return result;
	}
	
	public L2PcInstance[] getPlayersSortedBy(Comparator<L2PcInstance> comparator)
	{
		final L2PcInstance[] players = _allPlayers.values().toArray(new L2PcInstance[_allPlayers.values().size()]);
		Arrays.sort(players, comparator);
		return players;
	}
	
	private static class SingletonHolder
	{
		protected static final L2World _instance = new L2World();
	}
}