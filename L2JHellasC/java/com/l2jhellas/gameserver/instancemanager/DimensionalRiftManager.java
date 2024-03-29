package com.l2jhellas.gameserver.instancemanager;

import java.awt.Polygon;
import java.awt.Shape;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.PackRoot;
import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.sql.NpcData;
import com.l2jhellas.gameserver.engines.DocumentParser;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.group.party.L2Party;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.model.entity.DimensionalRift;
import com.l2jhellas.gameserver.model.spawn.L2Spawn;
import com.l2jhellas.gameserver.model.spawn.SpawnData;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;
import com.l2jhellas.util.Rnd;
import com.l2jhellas.util.Util;

public class DimensionalRiftManager implements DocumentParser
{
	private static Logger _log = Logger.getLogger(DimensionalRiftManager.class.getName());
	
	private static final Map<Byte, HashMap<Byte, DimensionalRiftRoom>> _rooms = new HashMap<>(7);
	private static final int DIMENSIONAL_FRAGMENT_ITEM_ID = 7079;
	
	public static DimensionalRiftManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected DimensionalRiftManager()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseFile(new File(PackRoot.DATAPACK_ROOT, "data/xml/dimensional_rift.xml"));
		_log.info("DimensionalRiftManager: Loaded rooms.");
	}
	
	@Override
	public void parseDocument(Document doc)
	{
		try
		{
		for (Node rift = doc.getFirstChild(); rift != null; rift = rift.getNextSibling())
		{
			if ("rift".equalsIgnoreCase(rift.getNodeName()))
			{
				for (Node area = rift.getFirstChild(); area != null; area = area.getNextSibling())
				{
					if ("area".equalsIgnoreCase(area.getNodeName()))
					{
						NamedNodeMap attrs = area.getAttributes();
						byte type = Byte.parseByte(attrs.getNamedItem("type").getNodeValue());
						
						for (Node room = area.getFirstChild(); room != null; room = room.getNextSibling())
						{
							if ("room".equalsIgnoreCase(room.getNodeName()))
							{
								attrs = room.getAttributes();
								byte roomId = Byte.parseByte(attrs.getNamedItem("id").getNodeValue());
								
								int xMin = Integer.parseInt(attrs.getNamedItem("xMin").getNodeValue());
								int xMax = Integer.parseInt(attrs.getNamedItem("xMax").getNodeValue());
								int yMin = Integer.parseInt(attrs.getNamedItem("yMin").getNodeValue());
								int yMax = Integer.parseInt(attrs.getNamedItem("yMax").getNodeValue());
								int xT = Integer.parseInt(attrs.getNamedItem("xT").getNodeValue());
								int yT = Integer.parseInt(attrs.getNamedItem("yT").getNodeValue());
								
								if (!_rooms.containsKey(type))
									_rooms.put(type, new HashMap<Byte, DimensionalRiftRoom>(9));
								
								_rooms.get(type).put(roomId, new DimensionalRiftRoom(type, roomId, xMin, xMax, yMin, yMax, xT, yT));
								
								for (Node spawn = room.getFirstChild(); spawn != null; spawn = spawn.getNextSibling())
								{
									if ("spawn".equalsIgnoreCase(spawn.getNodeName()))
									{
										attrs = spawn.getAttributes();
										int mobId = Integer.parseInt(attrs.getNamedItem("mobId").getNodeValue());
										int delay = Integer.parseInt(attrs.getNamedItem("delay").getNodeValue());
										int count = Integer.parseInt(attrs.getNamedItem("count").getNodeValue());
										
										L2NpcTemplate template = NpcData.getInstance().getTemplate(mobId);
										if (template == null)
											_log.warning(DimensionalRiftManager.class.getSimpleName() + ": Template " + mobId + " not found!");
										if (!_rooms.containsKey(type))
											_log.warning(DimensionalRiftManager.class.getSimpleName() + ": Type " + type + " not found!");
										else if (!_rooms.get(type).containsKey(roomId))
											_log.warning(DimensionalRiftManager.class.getSimpleName() + ": Room " + roomId + " in Type " + type + " not found!");
										
										for (int i = 0; i < count; i++)
										{
											DimensionalRiftRoom riftRoom = _rooms.get(type).get(roomId);
											int x = riftRoom.getRandomX();
											int y = riftRoom.getRandomY();
											int z = riftRoom.getTeleportCoords()[2];
											
											if (template != null && _rooms.containsKey(type) && _rooms.get(type).containsKey(roomId))
											{
												L2Spawn spawnDat = new L2Spawn(template);
												spawnDat.setLocx(x);
												spawnDat.setLocy(y);
												spawnDat.setLocz(z);
												spawnDat.setHeading(-1);
												spawnDat.setRespawnDelay(delay);
												SpawnData.getInstance().addNewSpawn(spawnDat, false);
												_rooms.get(type).get(roomId).getSpawns().add(spawnDat);
											}
										}
									}
								}
							}
						}
					}
				}
			 }
		  }
		}
		catch (Exception e)
		{
			_log.warning(DimensionalRiftManager.class.getSimpleName() + ": Error on loading dimensional rift spawns: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public DimensionalRiftRoom getRoom(byte type, byte room)
	{
		return _rooms.get(type) == null ? null : _rooms.get(type).get(room);
	}
	
	public void reload()
	{
		for (byte b : _rooms.keySet())
		{
			for (byte i : _rooms.get(b).keySet())
				_rooms.get(b).get(i).getSpawns().clear();
			
			_rooms.get(b).clear();
		}
		_rooms.clear();
		load();
	}
	
	public boolean checkIfInRiftZone(int x, int y, int z, boolean ignorePeaceZone)
	{
		if (ignorePeaceZone)
			return _rooms.get((byte) 0).get((byte) 1).checkIfInZone(x, y, z);
		
		return _rooms.get((byte) 0).get((byte) 1).checkIfInZone(x, y, z) && !_rooms.get((byte) 0).get((byte) 0).checkIfInZone(x, y, z);
	}
	
	public boolean checkIfInPeaceZone(int x, int y, int z)
	{
		return _rooms.get((byte) 0).get((byte) 0).checkIfInZone(x, y, z);
	}
	
	public void teleportToWaitingRoom(L2PcInstance player)
	{
		int[] coords = getRoom((byte) 0, (byte) 0).getTeleportCoords();
		player.teleToLocation(coords[0], coords[1], coords[2], false);
	}
	
	public synchronized void start(L2PcInstance player, byte type, L2Npc npc)
	{
		final L2Party party = player.getParty();
		
		// No party.
		if (party == null)
		{
			showHtmlFile(player, "data/html/seven_signs/rift/NoParty.htm", npc);
			return;
		}
		
		// Player isn't the party leader.
		if (!party.isLeader(player))
		{
			showHtmlFile(player, "data/html/seven_signs/rift/NotPartyLeader.htm", npc);
			return;
		}
		
		// Party is already in rift.
		if (party.isInDimensionalRift())
			return;
		
		// Party members' count is lower than config.
		if (party.getMemberCount() < Config.RIFT_MIN_PARTY_SIZE)
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
			html.setFile("data/html/seven_signs/rift/SmallParty.htm");
			html.replace("%npc_name%", npc.getName());
			html.replace("%count%", Integer.toString(Config.RIFT_MIN_PARTY_SIZE));
			player.sendPacket(html);
			return;
		}
		
		// Rift is full.
		if (!isAllowedEnter(type))
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
			html.setFile("data/html/seven_signs/rift/Full.htm");
			html.replace("%npc_name%", npc.getName());
			player.sendPacket(html);
			return;
		}
		
		// One of teammates isn't on peace zone or hasn't required amount of items.
		for (L2PcInstance p : party.getPartyMembers())
		{
			if (!checkIfInPeaceZone(p.getX(), p.getY(), p.getZ()))
			{
				showHtmlFile(player, "data/html/seven_signs/rift/NotInWaitingRoom.htm", npc);
				return;
			}
		}
		
		L2ItemInstance i;
		final int count = getNeededItems(type);
		
		for (L2PcInstance p : party.getPartyMembers())
		{
			i = p.getInventory().getItemByItemId(DIMENSIONAL_FRAGMENT_ITEM_ID);
			
			if (i == null || i.getCount() < getNeededItems(type))
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
				html.setFile("data/html/seven_signs/rift/NoFragments.htm");
				html.replace("%npc_name%", npc.getName());
				html.replace("%count%", Integer.toString(count));
				player.sendPacket(html);
				return;
			}
		}
		
		for (L2PcInstance p : party.getPartyMembers())
		{
			i = p.getInventory().getItemByItemId(DIMENSIONAL_FRAGMENT_ITEM_ID);
			if (!p.destroyItem("RiftEntrance", i.getObjectId(), count, null, true))
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
				html.setFile("data/html/seven_signs/rift/NoFragments.htm");
				html.replace("%npc_name%", npc.getName());
				html.replace("%count%", Integer.toString(count));
				player.sendPacket(html);
				return;
			}
		}
		
		byte room;
		List<Byte> emptyRooms;
		do
		{
			emptyRooms = getFreeRooms(type);
			room = emptyRooms.get(Rnd.get(1, emptyRooms.size()) - 1);
			
			// Relaunch random number until another room than room boss popups.
			while (room == 9)
				room = emptyRooms.get(Rnd.get(1, emptyRooms.size()) - 1);
		}
		// Find empty room
		while (_rooms.get(type).get(room).isPartyInside());
		
		// Creates an instance of the rift.
		new DimensionalRift(party, type, room);
	}
	
	public void killRift(DimensionalRift d)
	{
		if (d.getTeleportTimerTask() != null)
			d.getTeleportTimerTask().cancel();
		d.setTeleportTimerTask(null);
		
		if (d.getTeleportTimer() != null)
			d.getTeleportTimer().cancel();
		d.setTeleportTimer(null);
		
		if (d.getSpawnTimerTask() != null)
			d.getSpawnTimerTask().cancel();
		d.setSpawnTimerTask(null);
		
		if (d.getSpawnTimer() != null)
			d.getSpawnTimer().cancel();
		d.setSpawnTimer(null);
	}
	
	public static class DimensionalRiftRoom
	{
		protected final byte _type;
		protected final byte _room;
		private final int _xMin;
		private final int _xMax;
		private final int _yMin;
		private final int _yMax;
		private final int[] _teleportCoords;
		private final Shape _s;
		private final boolean _isBossRoom;
		private final List<L2Spawn> _roomSpawns;
		protected final List<L2Npc> _roomMobs;
		private boolean _partyInside = false;
		
		public DimensionalRiftRoom(byte type, byte room, int xMin, int xMax, int yMin, int yMax, int xT, int yT)
		{
			_type = type;
			_room = room;
			_xMin = (xMin + 128);
			_xMax = (xMax - 128);
			_yMin = (yMin + 128);
			_yMax = (yMax - 128);
			
			_teleportCoords = new int[]
			{
				xT,
				yT,
				-6752
			};
			
			_isBossRoom = (room == 9);
			_roomSpawns = new ArrayList<>();
			_roomMobs = new ArrayList<>();
			
			_s = new Polygon(new int[]
			{
				xMin,
				xMax,
				xMax,
				xMin
			}, new int[]
			{
				yMin,
				yMin,
				yMax,
				yMax
			}, 4);
		}
		
		public int getRandomX()
		{
			return Rnd.get(_xMin, _xMax);
		}
		
		public int getRandomY()
		{
			return Rnd.get(_yMin, _yMax);
		}
		
		public int[] getTeleportCoords()
		{
			return _teleportCoords;
		}
		
		public boolean checkIfInZone(int x, int y, int z)
		{
			return _s.contains(x, y) && z >= -6816 && z <= -6240;
		}
		
		public boolean isBossRoom()
		{
			return _isBossRoom;
		}
		
		public List<L2Spawn> getSpawns()
		{
			return _roomSpawns;
		}
		
		public void spawn()
		{
			for (L2Spawn spawn : _roomSpawns)
			{
				spawn.doSpawn();
				spawn.startRespawn();
			}
		}
		
		public DimensionalRiftRoom unspawn()
		{
			for (L2Spawn spawn : _roomSpawns)
			{
				spawn.stopRespawn();
				if (spawn.getLastSpawn() != null)
					spawn.getLastSpawn().deleteMe();
			}
			return this;
		}
		
		public boolean isPartyInside()
		{
			return _partyInside;
		}
		
		public void setPartyInside(boolean partyInside)
		{
			_partyInside = partyInside;
		}
	}
	
	private static int getNeededItems(byte type)
	{
		switch (type)
		{
			case 1:
				return Config.RIFT_ENTER_COST_RECRUIT;
			case 2:
				return Config.RIFT_ENTER_COST_SOLDIER;
			case 3:
				return Config.RIFT_ENTER_COST_OFFICER;
			case 4:
				return Config.RIFT_ENTER_COST_CAPTAIN;
			case 5:
				return Config.RIFT_ENTER_COST_COMMANDER;
			case 6:
				return Config.RIFT_ENTER_COST_HERO;
			default:
				throw new IndexOutOfBoundsException();
		}
	}
	
	public void showHtmlFile(L2PcInstance player, String file, L2Npc npc)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
		html.setFile(file);
		html.replace("%npc_name%", npc.getName());
		player.sendPacket(html);
	}
	
	public boolean isAllowedEnter(byte type)
	{
		int count = 0;
		for (DimensionalRiftRoom room : _rooms.get(type).values())
		{
			if (room.isPartyInside())
				count++;
		}
		return count < (_rooms.get(type).size() - 1);
	}
	
	public void handleCheat(L2PcInstance player, L2Npc npc)
	{
		showHtmlFile(player, "data/html/seven_signs/rift/Cheater.htm", npc);
		if (!player.isGM())
		{
			_log.warning(DimensionalRiftManager.class.getName() + ": Player " + player.getName() + "(" + player.getObjectId() + ") was cheating in dimension rift area!");
			Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " tried to cheat in dimensional rift.", Config.DEFAULT_PUNISH);
		}
	}
	
	public List<Byte> getFreeRooms(byte type)
	{
		List<Byte> list = new ArrayList<>();
		for (DimensionalRiftRoom room : _rooms.get(type).values())
		{
			if (!room.isPartyInside())
				list.add(room._room);
		}
		return list;
	}
	
	private static class SingletonHolder
	{
		protected static final DimensionalRiftManager _instance = new DimensionalRiftManager();
	}
}