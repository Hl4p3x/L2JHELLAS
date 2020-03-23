package com.l2jhellas.gameserver.instancemanager;

import java.util.HashMap;
import java.util.Map;

import com.l2jhellas.gameserver.idfactory.IdFactory;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.VehiclePathPoint;
import com.l2jhellas.gameserver.model.actor.L2Vehicle;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jhellas.gameserver.templates.L2CharTemplate;
import com.l2jhellas.gameserver.templates.StatsSet;

public class BoatManager
{
	public final Map<Integer, L2Vehicle> _boats = new HashMap<>();
	
	public static final int TALKING_ISLAND = 0;
	public static final int GLUDIN_HARBOR = 1;
	public static final int RUNE_HARBOR = 2;
	
	public static final int BOAT_BROADCAST_RADIUS = 20000;
	
	private final boolean[] _docksBusy = new boolean[3];
	
	public static final BoatManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public boolean _initialized;
	
	public BoatManager()
	{
		for (int i = 0; i < _docksBusy.length; i++)
		{
			_docksBusy[i] = false;
		}
	}
	
	public L2Vehicle getNewBoat(int boatId, int x, int y, int z, int heading)
	{
		StatsSet npcDat = new StatsSet();
		npcDat.set("npcId", boatId);
		npcDat.set("level", 0);
		npcDat.set("jClass", "boat");
		
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
		
		// npcDat.set("name", "");
		npcDat.set("collision_radius", 0);
		npcDat.set("collision_height", 0);
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
		npcDat.set("baseHpMax", 50000);
		npcDat.set("baseHpReg", 3.e-3f);
		npcDat.set("baseMpReg", 3.e-3f);
		npcDat.set("basePDef", 100);
		npcDat.set("baseMDef", 100);
		
		L2CharTemplate template = new L2CharTemplate(npcDat);
		
		final L2Vehicle boat = new L2Vehicle(IdFactory.getInstance().getNextId(),template);
		
		_boats.put(boat.getObjectId(), boat);
		
		boat.setHeading(heading);
		boat.setXYZInvisible(x, y, z);
		boat.spawnMe();
		return boat;
	}
	
	public L2Vehicle getBoat(int boatId)
	{
		return _boats.get(boatId);
	}
	
	public void dockShip(int h, boolean value)
	{
		_docksBusy[h] = value;
	}
	
	public boolean dockBusy(int h)
	{
		return _docksBusy[h];
	}
	
	public void broadcastPacket(VehiclePathPoint point1, VehiclePathPoint point2, L2GameServerPacket packet)
	{
		for (L2PcInstance player : L2World.getInstance().getAllPlayers().values())
		{
			if (Math.hypot(player.getX() - point1.getX(), player.getY() - point1.getY()) < BOAT_BROADCAST_RADIUS)
				player.sendPacket(packet);
			else
			{
				if (Math.hypot(player.getX() - point2.getX(), player.getY() - point2.getY()) < BOAT_BROADCAST_RADIUS) 
					player.sendPacket(packet);
			}
		}
	}
	
	public void broadcastPackets(VehiclePathPoint point1, VehiclePathPoint point2, L2GameServerPacket... packets)
	{
		for (L2PcInstance player : L2World.getInstance().getAllPlayers().values())
		{
			if (Math.hypot(player.getX() - point1.getX(), player.getY() - point1.getY()) < BOAT_BROADCAST_RADIUS)
			{
				for (L2GameServerPacket p : packets)
					player.sendPacket(p);
			}
			else
			{
				if (Math.hypot(player.getX() - point2.getX(), player.getY() - point2.getY()) < BOAT_BROADCAST_RADIUS) 
					for (L2GameServerPacket p : packets)
						player.sendPacket(p);
			}
		}
	}
	
	private static class SingletonHolder
	{
		protected static final BoatManager _instance = new BoatManager();
	}
}