package com.l2jhellas.gameserver.datatables.xml;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.PackRoot;
import com.l2jhellas.gameserver.engines.DocumentParser;
import com.l2jhellas.gameserver.idfactory.IdFactory;
import com.l2jhellas.gameserver.instancemanager.ClanHallManager;
import com.l2jhellas.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jhellas.gameserver.model.entity.ClanHall;
import com.l2jhellas.gameserver.templates.L2CharTemplate;
import com.l2jhellas.gameserver.templates.StatsSet;

public class DoorData implements DocumentParser
{
	protected static Logger _log = Logger.getLogger(DoorData.class.getName());
	
	private final Map<Integer, L2DoorInstance> _doors = new HashMap<>();
	
	public DoorData()
	{
		load();
		onStart();
	}
	
	@Override
	public void load()
	{
		parseFile(new File(PackRoot.DATAPACK_ROOT, "data/xml/doors.xml"));
		_log.info("DoorTable: Loaded " + _doors.size() + " door templates.");
	}
	
	@Override
	public void parseDocument(Document doc)
	{
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if (d.getNodeName().equalsIgnoreCase("door"))
					{
						String name = String.valueOf(d.getAttributes().getNamedItem("name").getNodeValue());
						int id = Integer.valueOf(d.getAttributes().getNamedItem("id").getNodeValue());
						int x = Integer.valueOf(d.getAttributes().getNamedItem("x").getNodeValue());
						int y = Integer.valueOf(d.getAttributes().getNamedItem("y").getNodeValue());
						int z = Integer.valueOf(d.getAttributes().getNamedItem("z").getNodeValue());
						int rangeXMin = Integer.valueOf(d.getAttributes().getNamedItem("XMin").getNodeValue());
						int rangeYMin = Integer.valueOf(d.getAttributes().getNamedItem("YMin").getNodeValue());
						int rangeZMin = Integer.valueOf(d.getAttributes().getNamedItem("ZMin").getNodeValue());
						int rangeXMax = Integer.valueOf(d.getAttributes().getNamedItem("XMax").getNodeValue());
						int rangeYMax = Integer.valueOf(d.getAttributes().getNamedItem("YMax").getNodeValue());
						int rangeZMax = Integer.valueOf(d.getAttributes().getNamedItem("ZMax").getNodeValue());
						int hp = Integer.valueOf(d.getAttributes().getNamedItem("hp").getNodeValue());
						int pdef = Integer.valueOf(d.getAttributes().getNamedItem("pdef").getNodeValue());
						int mdef = Integer.valueOf(d.getAttributes().getNamedItem("mdef").getNodeValue());
						boolean unlockable = Boolean.valueOf(d.getAttributes().getNamedItem("unlockable").getNodeValue());
						boolean autoOpen = Boolean.valueOf(d.getAttributes().getNamedItem("autoOpen").getNodeValue());
						if (rangeXMin > rangeXMax)
							_log.warning(DoorData.class.getName() + ": DoorTable: Error on rangeX min/max, ID:" + id);
						if (rangeYMin > rangeYMax)
							_log.warning(DoorData.class.getName() + ": DoorTable: Error on rangeY min/max, ID:" + id);
						if (rangeZMin > rangeZMax)
							_log.warning(DoorData.class.getName() + ": DoorTable: Error on rangeZ min/max, ID:" + id);
						
						StatsSet npcDat = new StatsSet();
						npcDat.set("npcId", id);
						npcDat.set("level", 0);
						npcDat.set("jClass", "door");
						npcDat.set("baseSTR", 0);
						npcDat.set("baseCON", 0);
						npcDat.set("baseDEX", 0);
						npcDat.set("baseINT", 0);
						npcDat.set("baseWIT", 0);
						npcDat.set("baseMEN", 0);
						npcDat.set("baseShldDef", 0);
						npcDat.set("baseShldRate", 0);
						npcDat.set("baseAccCombat", 38);
						npcDat.set("baseEvasRate", 38);
						npcDat.set("baseCritRate", 38);
						npcDat.set("collision_radius", Math.max(50, Math.min(rangeXMax - rangeXMin, rangeYMax - rangeYMin)));
						npcDat.set("collision_height", rangeZMax - rangeZMin & 0xfff0);
						npcDat.set("sex", "male");
						npcDat.set("type", "");
						npcDat.set("baseAtkRange", 0);
						npcDat.set("baseMpMax", 0);
						npcDat.set("baseCpMax", 0);
						npcDat.set("rewardExp", 0);
						npcDat.set("rewardSp", 0);
						npcDat.set("basePAtk", 0);
						npcDat.set("baseMAtk", 0);
						npcDat.set("basePAtkSpd", 0);
						npcDat.set("aggroRange", 0);
						npcDat.set("baseMAtkSpd", 0);
						npcDat.set("rhand", 0);
						npcDat.set("lhand", 0);
						npcDat.set("armor", 0);
						npcDat.set("baseWalkSpd", 0);
						npcDat.set("baseRunSpd", 0);
						npcDat.set("name", name);
						npcDat.set("baseHpMax", hp);
						npcDat.set("baseHpReg", 3.e-3f);
						npcDat.set("baseMpReg", 3.e-3f);
						npcDat.set("basePDef", pdef);
						npcDat.set("baseMDef", mdef);
						
						L2CharTemplate template = new L2CharTemplate(npcDat);
						L2DoorInstance door = new L2DoorInstance(IdFactory.getInstance().getNextId(), template, id, name, unlockable);
						door.setRange(rangeXMin, rangeYMin, rangeZMin, rangeXMax, rangeYMax, rangeZMax);

						try
						{
							door.setMapRegion(MapRegionTable.getMapRegion(x, y));
						}
						catch (Exception e)
						{
							_log.warning(DoorData.class.getName() + ": Error in door data, ID:" + id);
						}
						door.setCurrentHpMp(door.getMaxHp(), door.getMaxMp());
						door.setOpen(autoOpen);
						door.setXYZInvisible(x, y, z);
						putDoor(door);

						door.spawnMe(door.getX(), door.getY(), door.getZ());
						ClanHall clanhall = ClanHallManager.getInstance().getNearbyClanHall(door.getX(), door.getY(), 500);
						if (clanhall != null)
						{
							clanhall.getDoors().add(door);
							door.setClanHall(clanhall);
						}
					}
				}
			}
		}
	}
	
	public void reloadAll()
	{
		_doors.clear();
		load();
		onStart();
	}
	
	public static L2DoorInstance parseList(String line)
	{
		StringTokenizer st = new StringTokenizer(line, ";");
		
		String name = st.nextToken();
		int id = Integer.parseInt(st.nextToken());
		int x = Integer.parseInt(st.nextToken());
		int y = Integer.parseInt(st.nextToken());
		int z = Integer.parseInt(st.nextToken());
		int rangeXMin = Integer.parseInt(st.nextToken());
		int rangeYMin = Integer.parseInt(st.nextToken());
		int rangeZMin = Integer.parseInt(st.nextToken());
		int rangeXMax = Integer.parseInt(st.nextToken());
		int rangeYMax = Integer.parseInt(st.nextToken());
		int rangeZMax = Integer.parseInt(st.nextToken());
		int hp = Integer.parseInt(st.nextToken());
		int pdef = Integer.parseInt(st.nextToken());
		int mdef = Integer.parseInt(st.nextToken());
		
		boolean unlockable = false;
		
		if (st.hasMoreTokens())
			unlockable = Boolean.parseBoolean(st.nextToken());
		
		boolean autoOpen = false;
		
		if (st.hasMoreTokens())
			autoOpen = Boolean.parseBoolean(st.nextToken());
		
		st = null;
		
		if (rangeXMin > rangeXMax)
			_log.warning(DoorData.class.getSimpleName() + ": Error in door data, ID:" + id);
		
		if (rangeYMin > rangeYMax)
			_log.warning(DoorData.class.getSimpleName() + ": Error in door data, ID:" + id);
		
		if (rangeZMin > rangeZMax)
			_log.warning(DoorData.class.getSimpleName() + ": Error in door data, ID:" + id);
		
		int collisionRadius;
		
		if (rangeXMax - rangeXMin > rangeYMax - rangeYMin)
			collisionRadius = rangeYMax - rangeYMin;
		else
			collisionRadius = rangeXMax - rangeXMin;
		
		StatsSet npcDat = new StatsSet();
		npcDat.set("npcId", id);
		npcDat.set("level", 0);
		npcDat.set("jClass", "door");
		npcDat.set("baseSTR", 0);
		npcDat.set("baseCON", 0);
		npcDat.set("baseDEX", 0);
		npcDat.set("baseINT", 0);
		npcDat.set("baseWIT", 0);
		npcDat.set("baseMEN", 0);
		npcDat.set("baseShldDef", 0);
		npcDat.set("baseShldRate", 0);
		npcDat.set("baseAccCombat", 38);
		npcDat.set("baseEvasRate", 38);
		npcDat.set("baseCritRate", 38);
		npcDat.set("collision_radius", collisionRadius);
		npcDat.set("collision_height", rangeZMax - rangeZMin);
		npcDat.set("sex", "male");
		npcDat.set("type", "");
		npcDat.set("baseAtkRange", 0);
		npcDat.set("baseMpMax", 0);
		npcDat.set("baseCpMax", 0);
		npcDat.set("rewardExp", 0);
		npcDat.set("rewardSp", 0);
		npcDat.set("basePAtk", 0);
		npcDat.set("baseMAtk", 0);
		npcDat.set("basePAtkSpd", 0);
		npcDat.set("aggroRange", 0);
		npcDat.set("baseMAtkSpd", 0);
		npcDat.set("rhand", 0);
		npcDat.set("lhand", 0);
		npcDat.set("armor", 0);
		npcDat.set("baseWalkSpd", 0);
		npcDat.set("baseRunSpd", 0);
		npcDat.set("name", name);
		npcDat.set("baseHpMax", hp);
		npcDat.set("baseHpReg", 3.e-3f);
		npcDat.set("baseMpReg", 3.e-3f);
		npcDat.set("basePDef", pdef);
		npcDat.set("baseMDef", mdef);
		
		L2CharTemplate template = new L2CharTemplate(npcDat);
		L2DoorInstance door = new L2DoorInstance(IdFactory.getInstance().getNextId(), template, id, name, unlockable);
		door.setRange(rangeXMin, rangeYMin, rangeZMin, rangeXMax, rangeYMax, rangeZMax);
		name = null;
		npcDat = null;
		template = null;
		try
		{
			door.setMapRegion(MapRegionTable.getMapRegion(x, y));
		}
		catch (Exception e)
		{
			_log.warning(DoorData.class.getSimpleName() + ": Error in door data, ID:" + id);
		}
		door.setCurrentHpMp(door.getMaxHp(), door.getMaxMp());
		door.setOpen(autoOpen);
		door.setXYZInvisible(x, y, z);
		return door;
	}
	
	public boolean isInitialized()
	{
		return _initialized;
	}
	
	private final boolean _initialized = true;
	
	public L2DoorInstance getDoor(Integer id)
	{
		return _doors.get(id);
	}
	
	public void putDoor(L2DoorInstance door)
	{
		_doors.put(door.getDoorId(), door);
	}
	
	public Collection<L2DoorInstance> getDoors()
	{
		return _doors.values();
	}
	
	public void checkAutoOpen()
	{
		for (L2DoorInstance doorInst : getDoors())
		{
			if (doorInst.getDoorName().startsWith("goe"))
				doorInst.setAutoActionDelay(420000);
			else if (doorInst.getDoorName().startsWith("aden_tower"))
				doorInst.setAutoActionDelay(300000);
			else if (doorInst.getDoorName().startsWith("cruma"))
				doorInst.setAutoActionDelay(1200000);
		}
	}
	
	private void onStart()
	{
		try
		{
			getDoor(24190001).openMe();
			getDoor(24190002).openMe();
			getDoor(24190003).openMe();
			getDoor(24190004).openMe();
			getDoor(23180001).openMe();
			getDoor(23180002).openMe();
			getDoor(23180003).openMe();
			getDoor(23180004).openMe();
			getDoor(23180005).openMe();
			getDoor(23180006).openMe();
			
			checkAutoOpen();
		}
		catch (NullPointerException e)
		{
			_log.warning(DoorData.class.getSimpleName() + ": There are errors in your Doors.xml file.");
		}
	}
	
	public static DoorData getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final DoorData INSTANCE = new DoorData();
	}
}