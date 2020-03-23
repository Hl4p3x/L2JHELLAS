package com.l2jhellas.gameserver.instancemanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.model.entity.Castle;
import com.l2jhellas.gameserver.model.entity.Siege;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class SiegeManager
{
	private static final Logger _log = Logger.getLogger(SiegeManager.class.getName());
	
	private static SiegeManager _instance;
	
	public static final SiegeManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new SiegeManager();
			_instance.load();
		}
		return _instance;
	}
	
	private int _attackerMaxClans = 500; // Max number of clans
	private int _attackerRespawnDelay = 20000; // Time in ms. Changeable in siege.config
	private int _defenderMaxClans = 500; // Max number of clans
	private int _defenderRespawnDelay = 10000; // Time in ms. Changeable in siege.config
	
	// Siege settings
	private Map<Integer, List<SiegeSpawn>> _artefactSpawnList = new HashMap<>();
	private Map<Integer, List<SiegeSpawn>> _controlTowerSpawnList = new HashMap<>();
	
	private int _controlTowerLosePenalty = 20000; // Time in ms. Changeable in siege.config
	private int _flagMaxCount = 1; // Changeable in siege.config
	private int _siegeClanMinLevel = 4; // Changeable in siege.config
	private int _siegeLength = 120; // Time in minute. Changeable in siege.config
	public static int _daytosiege;
	private List<Siege> _sieges;
	
	private SiegeManager()
	{
	}
	
	public final static void addSiegeSkills(L2PcInstance character)
	{
		character.addSkill(SkillTable.getInstance().getInfo(246, 1), false);
		character.addSkill(SkillTable.getInstance().getInfo(247, 1), false);
	}
	
	public final static boolean checkIfOkToSummon(L2Character activeChar, boolean isCheckOnly)
	{
		if ((activeChar == null) || !(activeChar instanceof L2PcInstance))
			return false;
		
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_S2);
		L2PcInstance player = (L2PcInstance) activeChar;
		Castle castle = CastleManager.getInstance().getCastle(player);
		
		if ((castle == null) || castle.getCastleId() <= 0)
			sm.addString("You must be on castle ground to summon this.");
		else if (!castle.getSiege().getIsInProgress())
			sm.addString("You can only summon this during a siege.");
		else if (player.getClanId() != 0 && castle.getSiege().getAttackerClan(player.getClanId()) == null)
			sm.addString("You can only summon this as a registered attacker.");
		else
			return true;
		
		if (!isCheckOnly)
		{
			player.sendPacket(sm);
		}
		return false;
	}
	
	public final static boolean checkIsRegistered(L2Clan clan, int castleid)
	{
		if (clan == null)
			return false;
		
		if (clan.hasCastle() > 0)
			return true;
		
		boolean register = false;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT clan_id FROM siege_clans WHERE clan_id=? AND castle_id=?");
			statement.setInt(1, clan.getClanId());
			statement.setInt(2, castleid);
			ResultSet rs = statement.executeQuery();
			
			while (rs.next())
			{
				register = true;
				break;
			}
			
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning(SiegeManager.class.getName() + ": checkIsRegistered(): ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
		return register;
	}
	
	public final static void removeSiegeSkills(L2PcInstance character)
	{
		character.removeSkill(SkillTable.getInstance().getInfo(246, 1));
		character.removeSkill(SkillTable.getInstance().getInfo(247, 1));
	}
	
	private final void load()
	{
		try
		{
			InputStream is = new FileInputStream(new File(Config.SIEGE_CONFIGURATION_FILE));
			Properties siegeSettings = new Properties();
			siegeSettings.load(is);
			is.close();
			
			// Siege setting
			_attackerMaxClans = Integer.decode(siegeSettings.getProperty("AttackerMaxClans", "500"));
			_attackerRespawnDelay = Integer.decode(siegeSettings.getProperty("AttackerRespawn", "30000"));
			_controlTowerLosePenalty = Integer.decode(siegeSettings.getProperty("CTLossPenalty", "20000"));
			_defenderMaxClans = Integer.decode(siegeSettings.getProperty("DefenderMaxClans", "500"));
			_defenderRespawnDelay = Integer.decode(siegeSettings.getProperty("DefenderRespawn", "20000"));
			_flagMaxCount = Integer.decode(siegeSettings.getProperty("MaxFlags", "1"));
			_siegeClanMinLevel = Integer.decode(siegeSettings.getProperty("SiegeClanMinLevel", "4"));
			_siegeLength = Integer.decode(siegeSettings.getProperty("SiegeLength", "120"));
			_daytosiege = Integer.parseInt(siegeSettings.getProperty("DayToSiege", "14"));
			
			for (Castle castle : CastleManager.getInstance().getCastles())
			{
				List<SiegeSpawn> _controlTowersSpawns = new ArrayList<>();
				
				for (int i = 1; i < 0xFF; i++)
				{
					String _spawnParams = siegeSettings.getProperty(castle.getName() + "ControlTower" + Integer.toString(i), "");
					
					if (_spawnParams.length() == 0)
						break;
					
					StringTokenizer st = new StringTokenizer(_spawnParams.trim(), ",");
					
					try
					{
						int x = Integer.parseInt(st.nextToken());
						int y = Integer.parseInt(st.nextToken());
						int z = Integer.parseInt(st.nextToken());
						int npc_id = Integer.parseInt(st.nextToken());
						int hp = Integer.parseInt(st.nextToken());
						
						_controlTowersSpawns.add(new SiegeSpawn(castle.getCastleId(), x, y, z, 0, npc_id, hp));
					}
					catch (Exception e)
					{
						_log.warning(SiegeManager.class.getName() + ": Error while loading control tower(s) for " + castle.getName() + " castle.");
						if (Config.DEVELOPER)
							e.printStackTrace();
					}
				}
				
				List<SiegeSpawn> _artefactSpawns = new ArrayList<>();
				
				for (int i = 1; i < 0xFF; i++)
				{
					String _spawnParams = siegeSettings.getProperty(castle.getName() + "Artefact" + Integer.toString(i), "");
					
					if (_spawnParams.length() == 0)
						break;
					
					StringTokenizer st = new StringTokenizer(_spawnParams.trim(), ",");
					
					try
					{
						int x = Integer.parseInt(st.nextToken());
						int y = Integer.parseInt(st.nextToken());
						int z = Integer.parseInt(st.nextToken());
						int heading = Integer.parseInt(st.nextToken());
						int npc_id = Integer.parseInt(st.nextToken());
						
						_artefactSpawns.add(new SiegeSpawn(castle.getCastleId(), x, y, z, heading, npc_id));
					}
					catch (Exception e)
					{
						_log.warning(SiegeManager.class.getName() + ": Error while loading artefact(s) for " + castle.getName() + " castle.");
						if (Config.DEVELOPER)
							e.printStackTrace();
					}
				}
				
				_controlTowerSpawnList.put(castle.getCastleId(), _controlTowersSpawns);
				_artefactSpawnList.put(castle.getCastleId(), _artefactSpawns);
			}
			
		}
		catch (Exception e)
		{
			// _initialized = false;
			_log.warning(SiegeManager.class.getName() + ": Error while loading siege data.");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public final List<SiegeSpawn> getArtefactSpawnList(int _castleId)
	{
		if (_artefactSpawnList.containsKey(_castleId))
			return _artefactSpawnList.get(_castleId);
		return null;
	}
	
	public final List<SiegeSpawn> getControlTowerSpawnList(int _castleId)
	{
		if (_controlTowerSpawnList.containsKey(_castleId))
			return _controlTowerSpawnList.get(_castleId);
		return null;
	}
	
	public final int getAttackerMaxClans()
	{
		return _attackerMaxClans;
	}
	
	public final int getAttackerRespawnDelay()
	{
		return _attackerRespawnDelay;
	}
	
	public final int getControlTowerLosePenalty()
	{
		return _controlTowerLosePenalty;
	}
	
	public final int getDefenderMaxClans()
	{
		return _defenderMaxClans;
	}
	
	public final int getDefenderRespawnDelay()
	{
		return (_defenderRespawnDelay);
	}
	
	public final int getFlagMaxCount()
	{
		return _flagMaxCount;
	}
	
	public final static Siege getSiege(L2Object activeObject)
	{
		return getSiege(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}
	
	public final static Siege getSiege(int x, int y, int z)
	{
		for (Castle castle : CastleManager.getInstance().getCastles())
			if (castle.getSiege().checkIfInZone(x, y, z))
				return castle.getSiege();
		return null;
	}
	
	public final int getSiegeClanMinLevel()
	{
		return _siegeClanMinLevel;
	}
	
	public final int getSiegeLength()
	{
		return _siegeLength;
	}
	
	public final List<Siege> getSieges()
	{
		if (_sieges == null)
			_sieges = new ArrayList<>();
		return _sieges;
	}
	
	public class SiegeSpawn
	{
		Location _location;
		private final int _npcId;
		private final int _heading;
		private final int _castleId;
		private int _hp;
		
		public SiegeSpawn(int castle_id, int x, int y, int z, int heading, int npc_id)
		{
			_castleId = castle_id;
			_location = new Location(x, y, z, heading);
			_heading = heading;
			_npcId = npc_id;
		}
		
		public SiegeSpawn(int castle_id, int x, int y, int z, int heading, int npc_id, int hp)
		{
			_castleId = castle_id;
			_location = new Location(x, y, z, heading);
			_heading = heading;
			_npcId = npc_id;
			_hp = hp;
		}
		
		public int getCastleId()
		{
			return _castleId;
		}
		
		public int getNpcId()
		{
			return _npcId;
		}
		
		public int getHeading()
		{
			return _heading;
		}
		
		public int getHp()
		{
			return _hp;
		}
		
		public Location getLocation()
		{
			return _location;
		}
	}
}