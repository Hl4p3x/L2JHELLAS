package com.l2jhellas.gameserver.instancemanager;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jhellas.gameserver.engines.DocumentParser;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.model.zone.L2SpawnZone;
import com.l2jhellas.gameserver.model.zone.L2ZoneType;
import com.l2jhellas.gameserver.model.zone.ZoneRegion;
import com.l2jhellas.gameserver.model.zone.form.ZoneCuboid;
import com.l2jhellas.gameserver.model.zone.form.ZoneCylinder;
import com.l2jhellas.gameserver.model.zone.form.ZoneNPoly;
import com.l2jhellas.gameserver.model.zone.type.L2ArenaZone;
import com.l2jhellas.gameserver.model.zone.type.L2BossZone;
import com.l2jhellas.gameserver.model.zone.type.L2OlympiadStadiumZone;

public class ZoneManager implements DocumentParser
{
	private static final Logger _log = Logger.getLogger(ZoneManager.class.getName());
	
	private final Map<Class<? extends L2ZoneType>, Map<Integer, ? extends L2ZoneType>> _classZones = new HashMap<>();
	private int _lastDynamicId = 0;
	private final List<L2ItemInstance> _debugItems = new ArrayList<>();
	boolean reload = false;
	
	private final ZoneRegion[][] _zoneRegions = new ZoneRegion[L2World.REGIONS_X + 1][L2World.REGIONS_Y + 1];
	
	public static final ZoneManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected ZoneManager()
	{
		
		for (int x = 0; x < _zoneRegions.length; x++)
		{
			for (int y = 0; y < _zoneRegions[x].length; y++)
			{
				_zoneRegions[x][y] = new ZoneRegion(x, y);
			}
		}
		
		_lastDynamicId = 0;
		
		load();
	}
	
	public void reload()
	{
		// Get the world regions
		int count = 0;
		for (ZoneRegion[] zoneRegions : _zoneRegions)
		{
			for (ZoneRegion zoneRegion : zoneRegions)
			{
				zoneRegion.getZones().clear();
				count++;
			}
		}
		
		OlympiadStadiaManager.getInstance().clearStadium();
		GrandBossManager.getInstance().getZones().clear();
		
		_log.info("Removed zones in " + count + " regions.");
		
		reload = true;
		
		_lastDynamicId = 0;
		
		// Load the zones
		load();
		
		for (L2Object o : L2World.getInstance().getAllVisibleObjects().values())
		{
			if (o instanceof L2Character)
				((L2Character) o).revalidateZone(true);
		}
		
	}
	
	@Override
	public void load()
	{
		_classZones.clear();
		_log.info("Loading zones...");
		parseDatapackDirectory("data/xml/zones", false);
		_log.info("ZoneManager: loaded " + _classZones.size() + " zones classes and " + getSize() + " zones.");		
	}
	
	@Override
	public void parseDocument(Document doc)
	{
		_lastDynamicId = (_lastDynamicId / 1000) * 1000 + 1000;

		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				NamedNodeMap attrs = n.getAttributes();
				Node attribute = attrs.getNamedItem("enabled");
				if (attribute != null && !Boolean.parseBoolean(attribute.getNodeValue()))
					continue;
				
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("zone".equalsIgnoreCase(d.getNodeName()))
					{
						final NamedNodeMap nnmd = d.getAttributes();
						
						attribute = nnmd.getNamedItem("id");
						final int zoneId = attribute == null ? _lastDynamicId++ : Integer.parseInt(attribute.getNodeValue());

						final String zoneType = nnmd.getNamedItem("type").getNodeValue();
						final String zoneShape = nnmd.getNamedItem("shape").getNodeValue();
						final int minZ = Integer.parseInt(nnmd.getNamedItem("minZ").getNodeValue());
						final int maxZ = Integer.parseInt(nnmd.getNamedItem("maxZ").getNodeValue());
						
						// Create the zone
						Class<?> newZone;
						Constructor<?> zoneConstructor;
						L2ZoneType temp;
						try
						{
							newZone = Class.forName("com.l2jhellas.gameserver.model.zone.type.L2" + zoneType);
							zoneConstructor = newZone.getConstructor(int.class);
							temp = (L2ZoneType) zoneConstructor.newInstance(zoneId);
						}
						catch (Exception e)
						{
							_log.warning(ZoneManager.class.getName() + ": No such zone type: " + zoneType + " in file: " + doc.getClass().getSimpleName());
							continue;
						}
											
						try
						{						
							List<int[]> rs = new ArrayList<>();
							
							// loading from XML first
							for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
							{
								if ("node".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									int[] point = new int[2];
									point[0] = Integer.parseInt(attrs.getNamedItem("X").getNodeValue());
									point[1] = Integer.parseInt(attrs.getNamedItem("Y").getNodeValue());
									rs.add(point);
								}
							}
							
							int[][] coords = rs.toArray(new int[rs.size()][]);
							
							if (coords == null || coords.length == 0)
							{
								_log.warning(ZoneManager.class.getName() + ": missing data for zone: " + zoneId + " on file: " + doc.getClass().getSimpleName());
								continue;
							}
							
							// Create this zone. Parsing for cuboids is a
							// bit different than for other polygons
							// cuboids need exactly 2 points to be defined.
							// Other polygons need at least 3 (one per
							// vertex)
							if (zoneShape.equalsIgnoreCase("Cuboid"))
							{
								if (coords.length == 2)
									temp.setZone(new ZoneCuboid(coords[0][0], coords[1][0], coords[0][1], coords[1][1], minZ, maxZ));
								else
								{
									_log.warning(ZoneManager.class.getName() + ": Missing cuboid vertex in sql data for zone: " + zoneId + " in file: " + doc.getClass().getSimpleName());
									continue;
								}
							}
							else if (zoneShape.equalsIgnoreCase("NPoly"))
							{
								// nPoly needs to have at least 3 vertices
								if (coords.length > 2)
								{
									final int[] aX = new int[coords.length];
									final int[] aY = new int[coords.length];
									for (int i = 0; i < coords.length; i++)
									{
										aX[i] = coords[i][0];
										aY[i] = coords[i][1];
									}
									temp.setZone(new ZoneNPoly(aX, aY, minZ, maxZ));
								}
								else
								{
									_log.warning(ZoneManager.class.getName() + ": Bad data for zone: " + zoneId + " in file: " + doc.getClass().getSimpleName());
									continue;
								}
							}
							else if (zoneShape.equalsIgnoreCase("Cylinder"))
							{
								// A Cylinder zone requires a center point
								// at x,y and a radius
								attrs = d.getAttributes();
								final int zoneRad = Integer.parseInt(attrs.getNamedItem("rad").getNodeValue());
								if (coords.length == 1 && zoneRad > 0)
									temp.setZone(new ZoneCylinder(coords[0][0], coords[0][1], minZ, maxZ, zoneRad));
								else
								{
									_log.warning(ZoneManager.class.getName() + ": Bad data for zone: " + zoneId + " in file: " + doc.getClass().getSimpleName());
									continue;
								}
							}
							else
							{
								_log.warning(ZoneManager.class.getName() + ": Unknown shape: " + zoneShape + " in file: " + doc.getClass().getSimpleName());
								continue;
							}
						}
						catch (Exception e)
						{
							_log.warning(ZoneManager.class.getSimpleName() + ": ZoneData: Failed to load zone " + zoneId + " coordinates: " + e.getMessage());
						}
						
						// Check for additional parameters
						for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
						{
							if ("stat".equalsIgnoreCase(cd.getNodeName()))
							{
								attrs = cd.getAttributes();
								String name = attrs.getNamedItem("name").getNodeValue();
								String val = attrs.getNamedItem("val").getNodeValue();
								
								temp.setParameter(name, val);
							}
							else if ("spawn".equalsIgnoreCase(cd.getNodeName()) && temp instanceof L2SpawnZone)
							{
								attrs = cd.getAttributes();
								int spawnX = Integer.parseInt(attrs.getNamedItem("X").getNodeValue());
								int spawnY = Integer.parseInt(attrs.getNamedItem("Y").getNodeValue());
								int spawnZ = Integer.parseInt(attrs.getNamedItem("Z").getNodeValue());
								
								Node val = attrs.getNamedItem("isChaotic");
								
								if (reload)
									((L2SpawnZone) temp).clearSpawnZone();
								
								if (val != null && Boolean.parseBoolean(val.getNodeValue()))
									((L2SpawnZone) temp).addChaoticSpawn(spawnX, spawnY, spawnZ);
								else
									((L2SpawnZone) temp).addSpawn(spawnX, spawnY, spawnZ);
							}
						}

						addZone(zoneId, temp);
						
						// Register the zone into any world region it intersects with...
						for (int x = 0; x < _zoneRegions.length; x++)
						{
							for (int y = 0; y < _zoneRegions[x].length; y++)
							{
								if (temp.getZone().intersectsRectangle(L2World.getRegionX(x), L2World.getRegionX(x + 1), L2World.getRegionY(y), L2World.getRegionY(y + 1)))
									_zoneRegions[x][y].addZone(temp);
							}
						}
						
						if (temp instanceof L2OlympiadStadiumZone)
							OlympiadStadiaManager.getInstance().addStadium((L2OlympiadStadiumZone) temp);
						else if (temp instanceof L2BossZone)
							GrandBossManager.getInstance().addZone((L2BossZone) temp);
					}
				}
			}
		}
	}

	public int getSize()
	{
		int i = 0;
		for (Map<Integer, ? extends L2ZoneType> map : _classZones.values())
		{
			i += map.size();
		}
		return i;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends L2ZoneType> void addZone(Integer id, T zone)
	{
		// _zones.put(id, zone);
		Map<Integer, T> map = (Map<Integer, T>) _classZones.get(zone.getClass());
		if (map == null)
		{
			map = new HashMap<>();
			map.put(id, zone);
			_classZones.put(zone.getClass(), map);
		}
		else
			map.put(id, zone);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends L2ZoneType> Collection<T> getAllZones(Class<T> zoneType)
	{
		return (Collection<T>) _classZones.get(zoneType).values();
	}
	
	public L2ZoneType getZoneById(int id)
	{
		for (Map<Integer, ? extends L2ZoneType> map : _classZones.values())
		{
			if (map.containsKey(id))
				return map.get(id);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends L2ZoneType> T getZoneById(int id, Class<T> zoneType)
	{
		return (T) _classZones.get(zoneType).get(id);
	}
	
	public List<L2ZoneType> getZones(L2Object object)
	{
		return getZones(object.getX(), object.getY(), object.getZ());
	}
	
	public <T extends L2ZoneType> T getZone(L2Object object, Class<T> type)
	{
		if (object == null)
			return null;
		return getZone(object.getX(), object.getY(), object.getZ(), type);
	}
	
	public List<L2ZoneType> getZones(int x, int y, int z)
	{
		final List<L2ZoneType> temp = new ArrayList<>();
		for (L2ZoneType zone : getRegion(x, y).getZones())
		{
			if (zone.isInsideZone(x, y, z))
			{
				temp.add(zone);
			}
		}
		return temp;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends L2ZoneType> T getZone(int x, int y, int z, Class<T> type)
	{
		for (L2ZoneType zone : getRegion(x, y).getZones())
		{
			if (zone.isInsideZone(x, y, z) && type.isInstance(zone))
			{
				return (T) zone;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends L2ZoneType> T getZone(int x, int y, Class<T> type)
	{
		for (L2ZoneType zone : getRegion(x, y).getZones())
		{
			if (zone.isInsideZone(x, y) && type.isInstance(zone))
				return (T) zone;
		}
		return null;
	}
	
	public final static L2ArenaZone getArena(L2Character character)
	{
		if (character == null)
			return null;
		
		for (L2ZoneType temp : ZoneManager.getInstance().getZones(character.getX(), character.getY(), character.getZ()))
		{
			if (temp instanceof L2ArenaZone && temp.isCharacterInZone(character))
				return ((L2ArenaZone) temp);
		}
		
		return null;
	}
	
	public final static L2OlympiadStadiumZone getOlympiadStadium(L2Character character)
	{
		if (character == null)
			return null;
		
		for (L2ZoneType temp : ZoneManager.getInstance().getZones(character.getX(), character.getY(), character.getZ()))
		{
			if (temp instanceof L2OlympiadStadiumZone && temp.isCharacterInZone(character))
				return ((L2OlympiadStadiumZone) temp);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends L2ZoneType> T getClosestZone(L2Object obj, Class<T> type)
	{
		T zone = getZone(obj, type);
		if (zone == null)
		{
			double closestdis = Double.MAX_VALUE;
			for (T temp : (Collection<T>) _classZones.get(type).values())
			{
				double distance = temp.getDistanceToZone(obj);
				if (distance < closestdis)
				{
					closestdis = distance;
					zone = temp;
				}
			}
		}
		return zone;
	}
	
	public List<L2ItemInstance> getDebugItems()
	{
		return _debugItems;
	}
	
	public void clearDebugItems()
	{
		for (L2ItemInstance item : _debugItems)
			item.decayMe();
		
		_debugItems.clear();
	}
	
	// public ZoneRegion getRegion(int x, int y)
	// {
	// try
	// {
	// return _zoneRegions[(x >> SHIFT_BY) + OFFSET_X][(y >> SHIFT_BY) + OFFSET_Y];
	// }
	// catch (ArrayIndexOutOfBoundsException e)
	// {
	// return null;
	// }
	// }
	
	public ZoneRegion getRegion(int x, int y)
	{
		return _zoneRegions[(x - L2World.WORLD_X_MIN) / L2World.REGION_SIZE][(y - L2World.WORLD_Y_MIN) / L2World.REGION_SIZE];
	}
	
	public ZoneRegion getRegion(L2Object point)
	{
		return getRegion(point.getX(), point.getY());
	}
	
	private static class SingletonHolder
	{
		protected static final ZoneManager _instance = new ZoneManager();
	}
}