package com.l2jhellas.gameserver.datatables.xml;

import java.util.List;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jhellas.gameserver.SevenSigns;
import com.l2jhellas.gameserver.engines.DocumentParser;
import com.l2jhellas.gameserver.enums.ZoneId;
import com.l2jhellas.gameserver.enums.player.ClassRace;
import com.l2jhellas.gameserver.instancemanager.CastleManager;
import com.l2jhellas.gameserver.instancemanager.ClanHallManager;
import com.l2jhellas.gameserver.instancemanager.ZoneManager;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.model.entity.Castle;
import com.l2jhellas.gameserver.model.entity.ClanHall;
import com.l2jhellas.gameserver.model.zone.type.L2ArenaZone;
import com.l2jhellas.gameserver.model.zone.type.L2ClanHallZone;
import com.l2jhellas.gameserver.model.zone.type.L2TownZone;

public class MapRegionTable implements DocumentParser
{
	private static Logger _log = Logger.getLogger(MapRegionTable.class.getName());
	
	private static final int REGIONS_X = 11;
	private static final int REGIONS_Y = 16;
	
	private static final int[][] _regions = new int[REGIONS_X][REGIONS_Y];
	
	private static final int[] _castleIdArray =
	{
		0,
		0,
		0,
		0,
		0,
		1,
		0,
		2,
		3,
		4,
		5,
		0,
		0,
		6,
		8,
		7,
		9,
		0,
		0
	};
	
	public static enum TeleportWhereType
	{
		CASTLE,
		CLAN_HALL,
		SIEGE_FLAG,
		TOWN
	}
	
	
	protected MapRegionTable()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseDatapackFile("data/xml/map_region.xml");
		_log.info("MapRegionTable: Loaded regions.");
	}
	
	@Override
	public void parseDocument(Document doc)
	{
		Node n = doc.getFirstChild();
		for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
		{
			if (d.getNodeName().equalsIgnoreCase("map"))
			{
				NamedNodeMap attrs = d.getAttributes();
				int rY = Integer.valueOf(attrs.getNamedItem("geoY").getNodeValue()) - 10;
				for (int rX = 0; rX < REGIONS_X; rX++)
					_regions[rX][rY] = Integer.valueOf(attrs.getNamedItem("geoX_" + (rX + 16)).getNodeValue());
			}
		}
	}

	
	public static final int getMapRegion(int posX, int posY)
	{
		return _regions[getMapRegionX(posX)][getMapRegionY(posY)];
	}
	
	public static final int getMapRegion(L2Object ob)
	{
		return _regions[getMapRegionX(ob.getX())][getMapRegionY(ob.getY())];
	}
	
	public static final int getMapRegionX(int posX)
	{
		// +4 to shift coords center
		return (posX >> 15) + 4;
	}
	
	public static final int getMapRegionY(int posY)
	{
		// +8 to shift coords center
		return (posY >> 15) + 8;
	}
	
	public static final int getAreaCastle(int x, int y)
	{
		switch (getMapRegion(x, y))
		{
			case 0: // Talking Island Village
			case 5: // Town of Gludio
			case 6: // Gludin Village
				return 1;
				
			case 7: // Town of Dion
				return 2;
				
			case 8: // Town of Giran
			case 12: // Giran Harbor
				return 3;
				
			case 1: // Elven Village
			case 2: // Dark Elven Village
			case 9: // Town of Oren
			case 17: // Floran Village
				return 4;
				
			case 10: // Town of Aden
			case 11: // Hunters Village
			default: // Town of Aden
				return 5;
				
			case 13: // Heine
				return 6;
				
			case 15: // Town of Goddard
				return 7;
				
			case 14: // Rune Township
			case 18: // Primeval Isle Wharf
				return 8;
				
			case 3: // Orc Village
			case 4: // Dwarven Village
			case 16: // Town of Schuttgart
				return 9;
		}
	}
	
	public String getClosestTownName(int x, int y)
	{
		return getClosestTownName(getMapRegion(x, y));
	}
	
	public String getClosestTownName(int townId)
	{
		switch (townId)
		{
			case 0:
				return "Talking Island Village";
				
			case 1:
				return "Elven Village";
				
			case 2:
				return "Dark Elven Village";
				
			case 3:
				return "Orc Village";
				
			case 4:
				return "Dwarven Village";
				
			case 5:
				return "Town of Gludio";
				
			case 6:
				return "Gludin Village";
				
			case 7:
				return "Town of Dion";
				
			case 8:
				return "Town of Giran";
				
			case 9:
				return "Town of Oren";
				
			case 10:
				return "Town of Aden";
				
			case 11:
				return "Hunters Village";
				
			case 12:
				return "Giran Harbor";
				
			case 13:
				return "Heine";
				
			case 14:
				return "Rune Township";
				
			case 15:
				return "Town of Goddard";
				
			case 16:
				return "Town of Schuttgart";
				
			case 17:
				return "Floran Village";
				
			case 18:
				return "Primeval Isle";
				
			default:
				return "Town of Aden";
		}
	}
	
	public Location getTeleToLocation(L2Character activeChar, TeleportWhereType teleportWhere)
	{
		if (activeChar instanceof L2PcInstance)
		{
			L2PcInstance player = ((L2PcInstance) activeChar);
			
			// If in Monster Derby Track
			if (player.isInsideZone(ZoneId.MONSTER_TRACK))
				return new Location(12661, 181687, -3560);
			
			Castle castle = null;
			ClanHall clanhall = null;
			
			if (player.getClan() != null)
			{
				// If teleport to clan hall
				if (teleportWhere == TeleportWhereType.CLAN_HALL)
				{
					clanhall = ClanHallManager.getInstance().getClanHallByOwner(player.getClan());
					if (clanhall != null)
					{
						L2ClanHallZone zone = clanhall.getZone();
						if (zone != null)
							return zone.getSpawnLoc();
					}
				}
				
				// If teleport to castle
				if (teleportWhere == TeleportWhereType.CASTLE)
				{
					castle = CastleManager.getInstance().getCastleByOwner(player.getClan());
					
					// check if player is on castle and player's clan is defender
					if (castle == null)
					{
						castle = CastleManager.getInstance().getCastle(player);
						if (!(castle != null && castle.getSiege().getIsInProgress() && castle.getSiege().getDefenderClan(player.getClan()) != null))
							castle = null;
					}
					
					if (castle != null && castle.getCastleId() > 0)
						return castle.getZone().getSpawnLoc();
				}
				
				// If teleport to SiegeHQ
				if (teleportWhere == TeleportWhereType.SIEGE_FLAG)
				{
					castle = CastleManager.getInstance().getCastle(player);
					
					if (castle != null && castle.getSiege().getIsInProgress())
					{
						// Check if player's clan is attacker
						List<L2Npc> flags = castle.getSiege().getFlag(player.getClan());
						if (flags != null && !flags.isEmpty())
						{
							// Spawn to flag - Need more work to get player to the nearest flag
							L2Npc flag = flags.get(0);
							return new Location(flag.getX(), flag.getY(), flag.getZ());
						}
					}
				}
			}
			
			// Karma player land out of city
			if (player.getKarma() > 0)
				return getClosestTown(player.getTemplate().getRace(), activeChar.getX(), activeChar.getY()).getChaoticSpawnLoc();
			
			// Checking if in arena
			L2ArenaZone arena = ZoneManager.getArena(player);
			if (arena != null)
				return arena.getSpawnLoc();
			
			// Checking if needed to be respawned in "far" town from the castle;
			castle = CastleManager.getInstance().getCastle(player);
			if (castle != null)
			{
				if (castle.getSiege().getIsInProgress())
				{
					// Check if player's clan is participating
					if ((castle.getSiege().checkIsDefender(player.getClan()) || castle.getSiege().checkIsAttacker(player.getClan())) && SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE) == SevenSigns.CABAL_DAWN)
						return getSecondClosestTown(activeChar.getX(), activeChar.getY()).getSpawnLoc();
				}
			}
			
			// Get the nearest town
			return getClosestTown(player.getTemplate().getRace(), activeChar.getX(), activeChar.getY()).getSpawnLoc();
		}
		
		return getClosestTown(activeChar.getX(), activeChar.getY()).getSpawnLoc();
	}
	
	private static final L2TownZone getClosestTown(ClassRace race, int x, int y)
	{
		switch (getMapRegion(x, y))
		{
			case 0: // TI
				return getTown(2);
				
			case 1:// Elven
				return getTown((race == ClassRace.DARK_ELF) ? 1 : 3);
				
			case 2:// DE
				return getTown((race == ClassRace.ELF) ? 3 : 1);
				
			case 3: // Orc
				return getTown(4);
				
			case 4:// Dwarven
				return getTown(6);
				
			case 5:// Gludio
				return getTown(7);
				
			case 6:// Gludin
				return getTown(5);
				
			case 7: // Dion
				return getTown(8);
				
			case 8: // Giran
			case 12: // Giran Harbor
				return getTown(9);
				
			case 9: // Oren
				return getTown(10);
				
			case 10: // Aden
				return getTown(12);
				
			case 11: // HV
				return getTown(11);
				
			case 13: // Heine
				return getTown(15);
				
			case 14: // Rune
				return getTown(14);
				
			case 15: // Goddard
				return getTown(13);
				
			case 16: // Schuttgart
				return getTown(17);
				
			case 17:// Floran
				return getTown(16);
				
			case 18:// Primeval Isle
				return getTown(19);
		}
		return getTown(16); // Default to floran
	}
	
	private static final L2TownZone getClosestTown(int x, int y)
	{
		switch (getMapRegion(x, y))
		{
			case 0: // TI
				return getTown(2);
				
			case 1:// Elven
				return getTown(3);
				
			case 2:// DE
				return getTown(1);
				
			case 3: // Orc
				return getTown(4);
				
			case 4:// Dwarven
				return getTown(6);
				
			case 5:// Gludio
				return getTown(7);
				
			case 6:// Gludin
				return getTown(5);
				
			case 7: // Dion
				return getTown(8);
				
			case 8: // Giran
			case 12: // Giran Harbor
				return getTown(9);
				
			case 9: // Oren
				return getTown(10);
				
			case 10: // Aden
				return getTown(12);
				
			case 11: // HV
				return getTown(11);
				
			case 13: // Heine
				return getTown(15);
				
			case 14: // Rune
				return getTown(14);
				
			case 15: // Goddard
				return getTown(13);
				
			case 16: // Schuttgart
				return getTown(17);
				
			case 17:// Floran
				return getTown(16);
				
			case 18:// Primeval Isle
				return getTown(19);
		}
		return getTown(16); // Default to floran
	}
	
	private static final L2TownZone getSecondClosestTown(int x, int y)
	{
		switch (getMapRegion(x, y))
		{
			case 0: // TI
			case 1: // Elven
			case 2: // DE
			case 5: // Gludio
			case 6: // Gludin
				return getTown(5);
				
			case 3: // Orc
				return getTown(4);
				
			case 4: // Dwarven
			case 16: // Schuttgart
				return getTown(6);
				
			case 7: // Dion
				return getTown(7);
				
			case 8: // Giran
			case 9: // Oren
			case 10:// Aden
			case 11: // HV
				return getTown(11);
				
			case 12: // Giran Harbour
			case 13: // Heine
			case 17:// Floran
				return getTown(16);
				
			case 14: // Rune
				return getTown(13);
				
			case 15: // Goddard
				return getTown(12);
				
			case 18: // Primeval Isle
				return getTown(19);
		}
		return getTown(16); // Default to floran
	}
	
	public static final int getClosestLocation(int x, int y)
	{
		switch (getMapRegion(x, y))
		{
			case 0: // TI
				return 1;
				
			case 1: // Elven
				return 4;
				
			case 2: // DE
				return 3;
				
			case 3: // Orc
			case 4: // Dwarven
			case 16:// Schuttgart
				return 9;
				
			case 5: // Gludio
			case 6: // Gludin
				return 2;
				
			case 7: // Dion
				return 5;
				
			case 8: // Giran
			case 12: // Giran Harbor
				return 6;
				
			case 9: // Oren
				return 10;
				
			case 10: // Aden
				return 13;
				
			case 11: // HV
				return 11;
				
			case 13: // Heine
				return 12;
				
			case 14: // Rune
				return 14;
				
			case 15: // Goddard
				return 15;
		}
		return 0;
	}
	
	public static final boolean townHasCastleInSiege(int x, int y)
	{
		final int castleIndex = _castleIdArray[getMapRegion(x, y)];
		if (castleIndex > 0)
		{
			final Castle castle = CastleManager.getInstance().getCastles().get(CastleManager.getInstance().getCastleIndex(castleIndex));
			if (castle != null)
				return castle.getSiege().getIsInProgress();
		}
		return false;
	}
	
	public static final L2TownZone getTown(int townId)
	{
		for (L2TownZone temp : ZoneManager.getInstance().getAllZones(L2TownZone.class))
		{
			if (temp.getTownId() == townId)
				return temp;
		}
		return null;
	}
	
	public static final L2TownZone getTown(int x, int y, int z)
	{
		return ZoneManager.getInstance().getZone(x, y, z, L2TownZone.class);
	}
	
	public static MapRegionTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final MapRegionTable _instance = new MapRegionTable();
	}
}