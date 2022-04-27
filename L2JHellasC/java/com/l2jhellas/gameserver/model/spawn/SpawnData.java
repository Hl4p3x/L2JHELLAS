package com.l2jhellas.gameserver.model.spawn;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.sql.NpcData;
import com.l2jhellas.gameserver.engines.DocumentParser;
import com.l2jhellas.gameserver.enums.PeriodOfDay;
import com.l2jhellas.gameserver.instancemanager.DayNightSpawnManager;
import com.l2jhellas.gameserver.instancemanager.CustomSpawnManager;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.model.zone.form.ZoneNPoly;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;
import com.l2jhellas.util.Rnd;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class SpawnData implements DocumentParser
{
	private static final Logger LOGGER = Logger.getLogger(SpawnData.class.getName());
	
	private static final String SELECT_ALL_SPAWNS = "SELECT * FROM spawnlist";
	private static final String ADD_NEW_SPAWN = "INSERT INTO spawnlist (id,count,npc_templateid,locx,locy,locz,heading,respawn_delay) VALUES (?,?,?,?,?,?,?,?)";
	private static final String DELETE_SPAWN = "DELETE FROM spawnlist WHERE locx=? AND locy=? AND locz=? AND npc_templateid=? AND heading=?";

	private final Set<L2Spawn> _spawns = ConcurrentHashMap.newKeySet();

	protected SpawnData()
	{
		if (!Config.ALT_DEV_NO_SPAWNS)
			load();
	
		loadCustomSpawn();
	}
	
	@Override
	public void load()
	{
		parseDatapackDirectory("data/xml/spawn", true);
		LOGGER.info("Spawns loaded: " + getSpawns().size());
	}

	@Override
	public void parseDocument(Document doc, File path)
	{	
		forEach(doc, "list", listNode -> forEach(listNode, "spawn", spawnNode ->
		{
			boolean SpawnByDefault = Boolean.valueOf(String.valueOf(spawnNode.getAttributes().getNamedItem("spawn_bydefault").getNodeValue()));
			String SpawnName = String.valueOf(spawnNode.getAttributes().getNamedItem("name").getNodeValue());
			String EventName =  String.valueOf(parseEventName(spawnNode));
			
			forEach(spawnNode, "npc", npc ->
			{
				final NamedNodeMap npcattrs = npc.getAttributes();

				int NpcId = parseInteger(npcattrs, "id",0);
				final L2NpcTemplate template = NpcData.getInstance().getTemplate(NpcId);

				if(template != null)
				{
					// Don't spawn
					if (template.isType("L2SiegeGuard") || template.isType("L2RaidBoss"))
						return;
					
					int count = parseInteger(npcattrs, "count",0);
					
					if(count == 0)
						return;
					
					int respawn = parseInteger(npcattrs, "respawn",70);
					int randomRespawn = parseInteger(npcattrs, "respawn_rand" ,0);				

					String pos = parseString(npcattrs, "pos" ,"");

					Location loc = null;
					SpawnTerritory _territory = null;

					if(!pos.isEmpty())
						loc = parseLoc(pos);
					else
					{			
						final List<Integer> xNodes = new ArrayList<>();
						final List<Integer> yNodes = new ArrayList<>();
						
						final List<Integer> zMin = new ArrayList<>();
						final List<Integer> zMax = new ArrayList<>();
						    		
						forEach(spawnNode, "territory", coordinatesNode -> forEach(coordinatesNode, "location", locNode ->
						{
							final NamedNodeMap terrattrs = locNode.getAttributes();
							xNodes.add(parseInteger(terrattrs, "x"));
							yNodes.add(parseInteger(terrattrs, "y"));					
							zMin.add(parseInteger(terrattrs, "minz" , 32767));
							zMax.add(parseInteger(terrattrs, "maxz", -32768));						
						}));
						
						final int[] x = xNodes.stream().mapToInt(Integer::valueOf).toArray();
						final int[] y = yNodes.stream().mapToInt(Integer::valueOf).toArray();
						
						_territory = new SpawnTerritory(SpawnName, new ZoneNPoly(x, y,zMin.get(0), zMax.get(0)));
					}
					
					try
					{
						L2Spawn spawnDat = new L2Spawn(template);
						spawnDat.setId(NpcId);
						spawnDat.setAmount(count);
						spawnDat.setTerritory(_territory);
						spawnDat.setLocationName(SpawnName);
						spawnDat.setIsSpawningByDefault(SpawnByDefault);
						
						PeriodOfDay pod = PeriodOfDay.ALL;
												
						if(!EventName.isEmpty())
						{
							spawnDat.setEventName(EventName);
							
							if((EventName.startsWith("DAY") || EventName.startsWith("NIGHT")))
								pod = PeriodOfDay.valueOf(EventName);							
						}
						
						if(loc != null)
						{
							spawnDat.setLocx(loc.getX());
							spawnDat.setLocy(loc.getY());
							spawnDat.setLocz(loc.getZ());
							spawnDat.setHeading(loc.getHeading() <= 0 ? Rnd.get(65536) : loc.getHeading());
						}

						spawnDat.setRespawnDelay(respawn , randomRespawn);
						
						switch (pod.ordinal())
						{
							case 0: // default
								if(spawnDat.isSpawningByDefault())
									spawnDat.init();
								else
									CustomSpawnManager.getInstance().addSpawn(spawnDat);
								break;
							case 1: // Day
								DayNightSpawnManager.getInstance().addDayCreature(spawnDat);
								break;
							case 2: // Night
								DayNightSpawnManager.getInstance().addNightCreature(spawnDat);
								break;
						}	
						
						addSpawn(spawnDat);

					}
					catch (Exception e)
					{
						LOGGER.info("SpawnData Error: " + e);
					}

				}
				else
					LOGGER.info("SpawnData: data missing in npc table for id: " + NpcId);
			});
		}));
	}

	private void loadCustomSpawn()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
		PreparedStatement statement = con.prepareStatement(SELECT_ALL_SPAWNS))
		{	
			try(ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					final L2NpcTemplate template1 = NpcData.getInstance().getTemplate(rset.getInt("npc_templateid"));
					if (template1 != null)
					{
						final L2Spawn spawnDat = new L2Spawn(template1);
						spawnDat.setId(rset.getInt("id"));
						spawnDat.setAmount(rset.getInt("count"));
						spawnDat.setLocx(rset.getInt("locx"));
						spawnDat.setLocy(rset.getInt("locy"));
						spawnDat.setLocz(rset.getInt("locz"));
						spawnDat.setHeading(rset.getInt("heading"));
						spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));		
						spawnDat.init();						
						addSpawn(spawnDat);
					}
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("SpawnData: Sql Spawn could not be initialized: " + e);
		}		
	}
	
	public void addNewSpawn(L2Spawn spawn, boolean storeInDb)
	{
		addSpawn(spawn);
		
		if (storeInDb)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(ADD_NEW_SPAWN))
			{
				statement.setInt(1, spawn.getId());
				statement.setInt(2, spawn.getAmount());
				statement.setInt(3, spawn.getNpcid());
				statement.setInt(4, spawn.getLocx());
				statement.setInt(5, spawn.getLocy());
				statement.setInt(6, spawn.getLocz());
				statement.setInt(7, spawn.getHeading());
				statement.setInt(8, spawn.getRespawnDelay() / 1000);
				statement.execute();
			}
			catch (Exception e)
			{
				LOGGER.warning("SpawnData: Could not store spawn in the DB:" + e);
			}
		}
	}
	
	public void deleteSpawn(L2Spawn spawn, boolean updateDb)
	{
		if (!getSpawns().remove(spawn))
			return;
		
		if (updateDb)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(DELETE_SPAWN))
			{
				ps.setInt(1, spawn.getLocx());
				ps.setInt(2, spawn.getLocy());
				ps.setInt(3, spawn.getLocz());
				ps.setInt(4, spawn.getNpcid());
				ps.setInt(5, spawn.getHeading());
				ps.execute();
			}
			catch (Exception e)
			{
				LOGGER.warning("SpawnData: Spawn Id " + spawn.getNpcid() + " could not be removed from DB: " + e);
			}
		}
	}

	private String parseEventName(Node d)
	{
		if(d.getAttributes().getNamedItem("event_name") != null)
			return String.valueOf(d.getAttributes().getNamedItem("event_name").getNodeValue());	
		return "";
	}
	
	private void addSpawn(L2Spawn spawn)
	{
		getSpawns().add(spawn);
	}
	
	public L2Spawn getSpawn(int npcId)
	{
		return getSpawns().stream().filter(Objects::nonNull).filter(s -> s.getNpcid() == npcId).findFirst().orElse(null);
	}
	
	public  List<L2Spawn> getSpawnsByEventName(String Evtname)
	{
		return getSpawns().stream().filter(Objects::nonNull).filter(L2Spawn :: EvtNameIsNotBlank).filter(s -> String.valueOf(s.getEventName()).equalsIgnoreCase(Evtname)).collect(Collectors.toList());
	}
	
	public  List<L2Spawn> getSpawnsByLocationName(String Locname)
	{
		return getSpawns().stream().filter(Objects::nonNull).filter(L2Spawn :: LocNameIsNotBlank).filter(s -> String.valueOf(s.getLocationName()).equalsIgnoreCase(Locname)).collect(Collectors.toList());
	}
	
	public  List<L2Spawn> getSpawnsByTerritoryName(String Terrname)
	{
		return getSpawns().stream().filter(Objects::nonNull).filter(L2Spawn :: hasTerritory).filter(s -> String.valueOf(s.getTerritory().getName()).equalsIgnoreCase(Terrname)).collect(Collectors.toList());
	}
	
	public Set<L2Spawn> getSpawns()
	{
		return _spawns;
	}
	
	public boolean forEachSpawn(Function<L2Spawn, Boolean> function)
	{
		for (L2Spawn spawn : getSpawns())
			if (!function.apply(spawn))
				return false;
		return true;
	}

	public void findNPCInstances(L2PcInstance activeChar, int npcId, int teleportIndex)
	{
	    AtomicInteger _index = new AtomicInteger(0);

	    getSpawns().stream().filter(Objects::nonNull).filter(s -> s.getNpcid() ==  npcId && s.getLastSpawn() != null).forEach(ls ->
		{
			_index.incrementAndGet();

			if (teleportIndex > -1 && teleportIndex == _index.get())
				activeChar.teleToLocation(ls.getLocx(), ls.getLocy(), ls.getLocz(), true);
			else
				activeChar.sendMessage(_index.get() + " - " + ls.getTemplate().name + " (" + ls.getId() + "): " + ls.getLocx() + " " + ls.getLocy() + " " + ls.getLocz());

		});

		if (_index.get() == 0)
			activeChar.sendMessage("No current spawns found.");
		
		_index.set(0);
	}
	
	public void reloadAll()
	{
		if (!Config.ALT_DEV_NO_SPAWNS)
		{
			getSpawns().clear();
			CustomSpawnManager.getInstance().cleanUp();
			load();
		}
		
		loadCustomSpawn();
	}
	
	public static SpawnData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SpawnData _instance = new SpawnData();
	}
}
