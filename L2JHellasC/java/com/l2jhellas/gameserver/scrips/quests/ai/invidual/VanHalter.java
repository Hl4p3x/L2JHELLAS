package com.l2jhellas.gameserver.scrips.quests.ai.invidual;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.datatables.sql.NpcData;
import com.l2jhellas.gameserver.datatables.sql.SpawnTable;
import com.l2jhellas.gameserver.datatables.xml.DoorData;
import com.l2jhellas.gameserver.instancemanager.GrandBossManager;
import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.L2Skill;
import com.l2jhellas.gameserver.model.L2Spawn;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2RaidBossInstance;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.model.quest.QuestEventType;
import com.l2jhellas.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jhellas.gameserver.network.serverpackets.SpecialCamera;
import com.l2jhellas.gameserver.scrips.quests.ai.AbstractNpcAI;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;
import com.l2jhellas.gameserver.templates.StatsSet;
import com.l2jhellas.util.Rnd;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class VanHalter extends AbstractNpcAI
{
	private static final Logger _log = Logger.getLogger(VanHalter.class.getName());
	
	protected Map<Integer, List<L2PcInstance>> _bleedingPlayers = new ConcurrentHashMap<>();
	protected Map<Integer, L2Spawn> _monsterSpawn = new ConcurrentHashMap<>();
	public Map<Integer, L2Spawn> _cameraMarkerSpawn = new ConcurrentHashMap<>();
	protected Map<Integer, L2Npc> _cameraMarker = new ConcurrentHashMap<>();
	
	protected List<L2Spawn> _royalGuardSpawn = new ArrayList<>();
	protected List<L2Spawn> _royalGuardCaptainSpawn = new ArrayList<>();
	protected List<L2Spawn> _royalGuardHelperSpawn = new ArrayList<>();
	protected List<L2Spawn> _triolRevelationSpawn = new ArrayList<>();
	protected List<L2Spawn> _triolRevelationAlive = new ArrayList<>();
	protected List<L2Spawn> _guardOfAltarSpawn = new ArrayList<>();
	protected List<L2Npc> _monsters = new ArrayList<>();
	protected List<L2Npc> _royalGuard = new ArrayList<>();
	protected List<L2Npc> _royalGuardCaptain = new ArrayList<>();
	protected List<L2Npc> _royalGuardHepler = new ArrayList<>();
	protected List<L2Npc> _triolRevelation = new ArrayList<>();
	protected List<L2Npc> _guardOfAltar = new ArrayList<>();
	public List<L2DoorInstance> _doorOfAltar = new ArrayList<>();
	public List<L2DoorInstance> _doorOfSacrifice = new ArrayList<>();
	
	protected L2Spawn _ritualOfferingSpawn = null;
	protected L2Spawn _ritualSacrificeSpawn = null;
	protected L2Spawn _vanHalterSpawn = null;
	protected L2Npc _ritualOffering = null;
	protected L2Npc _ritualSacrifice = null;
	protected L2RaidBossInstance _vanHalter = null;
	
	protected ScheduledFuture<?> _movieTask = null;
	protected ScheduledFuture<?> _closeDoorOfAltarTask = null;
	protected ScheduledFuture<?> _openDoorOfAltarTask = null;
	protected ScheduledFuture<?> _lockUpDoorOfAltarTask = null;
	protected ScheduledFuture<?> _callRoyalGuardHelperTask = null;
	public ScheduledFuture<?> _timeUpTask = null;
	protected ScheduledFuture<?> _intervalTask = null;
	protected ScheduledFuture<?> _halterEscapeTask = null;
	public ScheduledFuture<?> _setBleedTask = null;
	
	public boolean _isLocked = false;
	public boolean _isHalterSpawned = false;
	public boolean _isSacrificeSpawned = false;
	public boolean _isCaptainSpawned = false;
	public boolean _isHelperCalled = false;
	
	public VanHalter()
	{
		super("vanhalter", "ai");
		
		int[] mobs =
		{
			29062,
			22188,
			32058,
			32059,
			32060,
			32061,
			32062,
			32063,
			32064,
			32065,
			32066
		};
		
		addEventId(29062, QuestEventType.ON_ATTACK);
		
		registerMobs(mobs,QuestEventType.ON_ATTACK, QuestEventType.ON_KILL);
		
		_isLocked = false;
		_isCaptainSpawned = false;
		_isHelperCalled = false;
		_isHalterSpawned = false;
		
		_doorOfAltar.add(DoorData.getInstance().getDoor(Integer.valueOf(19160014)));
		_doorOfAltar.add(DoorData.getInstance().getDoor(Integer.valueOf(19160015)));
		openDoorOfAltar(true);
		_doorOfSacrifice.add(DoorData.getInstance().getDoor(Integer.valueOf(19160016)));
		_doorOfSacrifice.add(DoorData.getInstance().getDoor(Integer.valueOf(19160017)));
		closeDoorOfSacrifice();
		
		loadRoyalGuard();
		loadTriolRevelation();
		loadRoyalGuardCaptain();
		loadRoyalGuardHelper();
		loadGuardOfAltar();
		loadVanHalter();
		loadRitualOffering();
		loadRitualSacrifice();
		
		spawnRoyalGuard();
		spawnTriolRevelation();
		spawnVanHalter();
		spawnRitualOffering();
		
		_cameraMarkerSpawn.clear();
		try
		{
			L2NpcTemplate template1 = NpcData.getInstance().getTemplate(13014);
			
			L2Spawn tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(-16397);
			tempSpawn.setLocy(-55200);
			tempSpawn.setLocz(-10449);
			tempSpawn.setHeading(16384);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(60000);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_cameraMarkerSpawn.put(Integer.valueOf(1), tempSpawn);
			
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(-16397);
			tempSpawn.setLocy(-55200);
			tempSpawn.setLocz(-10051);
			tempSpawn.setHeading(16384);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(60000);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_cameraMarkerSpawn.put(Integer.valueOf(2), tempSpawn);
			
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(-16397);
			tempSpawn.setLocy(-55200);
			tempSpawn.setLocz(-9741);
			tempSpawn.setHeading(16384);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(60000);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_cameraMarkerSpawn.put(Integer.valueOf(3), tempSpawn);
			
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(-16397);
			tempSpawn.setLocy(-55200);
			tempSpawn.setLocz(-9394);
			tempSpawn.setHeading(16384);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(60000);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_cameraMarkerSpawn.put(Integer.valueOf(4), tempSpawn);
			
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(-16397);
			tempSpawn.setLocy(-55197);
			tempSpawn.setLocz(-8739);
			tempSpawn.setHeading(16384);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(60000);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_cameraMarkerSpawn.put(Integer.valueOf(5), tempSpawn);
		}
		catch (Exception e)
		{
			_log.warning("VanHalterManager : " + e.getMessage() + " :" + e);
		}
		
		if (_timeUpTask != null)
			_timeUpTask.cancel(false);
		_timeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new TimeUp(), 21600);
		
		if (_setBleedTask != null)
			_setBleedTask.cancel(false);
		_setBleedTask = ThreadPoolManager.getInstance().scheduleGeneral(new Bleeding(), 2000L);
		
		Integer status = GrandBossManager.getInstance().getBossStatus(29062);
		if (status.intValue() == 0)
			enterInterval();
		else
			GrandBossManager.setBossStatus(29062, 1);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (npc.getNpcId() == 29062)
			if (((int) (npc.getStatus().getCurrentHp() / npc.getMaxHp()) * 100) <= 20)
				callRoyalGuardHelper();
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if ((npcId == 32058) || (npcId == 32059) || (npcId == 32060) || (npcId == 32061) || (npcId == 32062) || (npcId == 32063) || (npcId == 32064) || (npcId == 32065) || (npcId == 32066))
			removeBleeding(npcId);
		checkTriolRevelationDestroy();
		if (npcId == 22188)
			checkRoyalGuardCaptainDestroy();
		if (npcId == 29062)
			enterInterval();
		return super.onKill(npc, killer, isPet);
	}
	
	protected void loadRoyalGuard()
	{
		_royalGuardSpawn.clear();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			try (PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid between ? and ? ORDER BY id"))
			{
				statement.setInt(1, 22175);
				statement.setInt(2, 22176);
				
				try (ResultSet rset = statement.executeQuery())
				{
					while (rset.next())
					{
						L2NpcTemplate template1 = NpcData.getInstance().getTemplate(rset.getInt("npc_templateid"));
						if (template1 != null)
						{
							L2Spawn spawnDat = new L2Spawn(template1);
							spawnDat.setAmount(rset.getInt("count"));
							spawnDat.setLocx(rset.getInt("locx"));
							spawnDat.setLocy(rset.getInt("locy"));
							spawnDat.setLocz(rset.getInt("locz"));
							spawnDat.setHeading(rset.getInt("heading"));
							spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
							SpawnTable.getInstance().addNewSpawn(spawnDat, false);
							_royalGuardSpawn.add(spawnDat);
							continue;
						}
						
						_log.warning("VanHalterManager.loadRoyalGuard: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
					}
				}
			}
			if (Config.DEBUG)
				_log.info("VanHalterManager.loadRoyalGuard: Loaded " + _royalGuardSpawn.size() + " Royal Guard spawn locations.");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			_log.warning("VanHalterManager.loadRoyalGuard: Spawn could not be initialized: " + e);
		}
	}
	
	protected void spawnRoyalGuard()
	{
		if (!_royalGuard.isEmpty())
			deleteRoyalGuard();
		
		for (L2Spawn rgs : _royalGuardSpawn)
		{
			rgs.startRespawn();
			_royalGuard.add(rgs.doSpawn());
		}
	}
	
	protected void deleteRoyalGuard()
	{
		for (L2Npc rg : _royalGuard)
		{
			rg.getSpawn().stopRespawn();
			rg.deleteMe();
		}
		
		_royalGuard.clear();
	}
	
	protected void loadTriolRevelation()
	{
		_triolRevelationSpawn.clear();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			try (PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid between ? and ? ORDER BY id"))
			{
				statement.setInt(1, 32058);
				statement.setInt(2, 32068);
				
				try (ResultSet rset = statement.executeQuery())
				{
					while (rset.next())
					{
						L2NpcTemplate template1 = NpcData.getInstance().getTemplate(rset.getInt("npc_templateid"));
						if (template1 != null)
						{
							L2Spawn spawnDat = new L2Spawn(template1);
							spawnDat.setAmount(rset.getInt("count"));
							spawnDat.setLocx(rset.getInt("locx"));
							spawnDat.setLocy(rset.getInt("locy"));
							spawnDat.setLocz(rset.getInt("locz"));
							spawnDat.setHeading(rset.getInt("heading"));
							spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
							SpawnTable.getInstance().addNewSpawn(spawnDat, false);
							_triolRevelationSpawn.add(spawnDat);
							continue;
						}
						
						_log.warning("VanHalterManager.loadTriolRevelation: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
					}
				}
			}
			
			if (Config.DEBUG)
				_log.info("VanHalterManager.loadTriolRevelation: Loaded " + _triolRevelationSpawn.size() + " Triol's Revelation spawn locations.");
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			_log.warning("VanHalterManager.loadTriolRevelation: Spawn could not be initialized: " + e);
		}
	}
	
	protected void spawnTriolRevelation()
	{
		if (!_triolRevelation.isEmpty())
			deleteTriolRevelation();
		
		for (L2Spawn trs : _triolRevelationSpawn)
		{
			trs.startRespawn();
			_triolRevelation.add(trs.doSpawn());
			if ((trs.getNpcid() != 32067) && (trs.getNpcid() != 32068))
				_triolRevelationAlive.add(trs);
		}
	}
	
	protected void deleteTriolRevelation()
	{
		for (L2Npc tr : _triolRevelation)
		{
			tr.getSpawn().stopRespawn();
			tr.deleteMe();
		}
		_triolRevelation.clear();
		_bleedingPlayers.clear();
	}
	
	protected void loadRoyalGuardCaptain()
	{
		_royalGuardCaptainSpawn.clear();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
			statement.setInt(1, 22188);
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				L2NpcTemplate template1 = NpcData.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					L2Spawn spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setLocx(rset.getInt("locx"));
					spawnDat.setLocy(rset.getInt("locy"));
					spawnDat.setLocz(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					_royalGuardCaptainSpawn.add(spawnDat);
					continue;
				}
				
				_log.warning("VanHalterManager.loadRoyalGuardCaptain: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
			}
			
			rset.close();
			statement.close();
			if (Config.DEBUG)
				_log.info("VanHalterManager.loadRoyalGuardCaptain: Loaded " + _royalGuardCaptainSpawn.size() + " Royal Guard Captain spawn locations.");
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			_log.warning("VanHalterManager.loadRoyalGuardCaptain: Spawn could not be initialized: " + e);
		}
	}
	
	protected void spawnRoyalGuardCaptain()
	{
		if (!_royalGuardCaptain.isEmpty())
			deleteRoyalGuardCaptain();
		
		for (L2Spawn trs : _royalGuardCaptainSpawn)
		{
			trs.startRespawn();
			_royalGuardCaptain.add(trs.doSpawn());
		}
		_isCaptainSpawned = true;
	}
	
	protected void deleteRoyalGuardCaptain()
	{
		for (L2Npc tr : _royalGuardCaptain)
		{
			tr.getSpawn().stopRespawn();
			tr.deleteMe();
		}
		
		_royalGuardCaptain.clear();
	}
	
	protected void loadRoyalGuardHelper()
	{
		_royalGuardHelperSpawn.clear();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
			statement.setInt(1, 22191);
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				L2NpcTemplate template1 = NpcData.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					L2Spawn spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setLocx(rset.getInt("locx"));
					spawnDat.setLocy(rset.getInt("locy"));
					spawnDat.setLocz(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					_royalGuardHelperSpawn.add(spawnDat);
					continue;
				}
				
				_log.warning("VanHalterManager.loadRoyalGuardHelper: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
			}
			
			rset.close();
			statement.close();
			if (Config.DEBUG)
				_log.info("VanHalterManager.loadRoyalGuardHelper: Loaded " + _royalGuardHelperSpawn.size() + " Royal Guard Helper spawn locations.");
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			_log.warning("VanHalterManager.loadRoyalGuardHelper: Spawn could not be initialized: " + e);
		}
	}
	
	protected void spawnRoyalGuardHepler()
	{
		for (L2Spawn trs : _royalGuardHelperSpawn)
		{
			trs.startRespawn();
			_royalGuardHepler.add(trs.doSpawn());
		}
	}
	
	protected void deleteRoyalGuardHepler()
	{
		for (L2Npc tr : _royalGuardHepler)
		{
			tr.getSpawn().stopRespawn();
			tr.deleteMe();
		}
		_royalGuardHepler.clear();
	}
	
	protected void loadGuardOfAltar()
	{
		_guardOfAltarSpawn.clear();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
			statement.setInt(1, 32051);
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				L2NpcTemplate template1 = NpcData.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					L2Spawn spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setLocx(rset.getInt("locx"));
					spawnDat.setLocy(rset.getInt("locy"));
					spawnDat.setLocz(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					_guardOfAltarSpawn.add(spawnDat);
					continue;
				}
				
				_log.warning("VanHalterManager.loadGuardOfAltar: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
			}
			
			rset.close();
			statement.close();
			if (Config.DEBUG)
				_log.info("VanHalterManager.loadGuardOfAltar: Loaded " + _guardOfAltarSpawn.size() + " Guard Of Altar spawn locations.");
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			_log.warning("VanHalterManager.loadGuardOfAltar: Spawn could not be initialized: " + e);
		}
	}
	
	protected void spawnGuardOfAltar()
	{
		if (!_guardOfAltar.isEmpty())
			deleteGuardOfAltar();
		
		for (L2Spawn trs : _guardOfAltarSpawn)
		{
			trs.startRespawn();
			_guardOfAltar.add(trs.doSpawn());
		}
	}
	
	protected void deleteGuardOfAltar()
	{
		for (L2Npc tr : _guardOfAltar)
		{
			tr.getSpawn().stopRespawn();
			tr.deleteMe();
		}
		
		_guardOfAltar.clear();
	}
	
	protected void loadVanHalter()
	{
		_vanHalterSpawn = null;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
			statement.setInt(1, 29062);
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				L2NpcTemplate template1 = NpcData.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					L2Spawn spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setLocx(rset.getInt("locx"));
					spawnDat.setLocy(rset.getInt("locy"));
					spawnDat.setLocz(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					_vanHalterSpawn = spawnDat;
					continue;
				}
				
				_log.warning("VanHalterManager.loadVanHalter: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
			}
			
			rset.close();
			statement.close();
			if (Config.DEBUG)
				_log.info("VanHalterManager.loadVanHalter: Loaded High Priestess van Halter spawn locations.");
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			_log.warning("VanHalterManager.loadVanHalter: Spawn could not be initialized: " + e);
		}
	}
	
	protected void spawnVanHalter()
	{
		_vanHalter = ((L2RaidBossInstance) _vanHalterSpawn.doSpawn());
		
		_vanHalter.setIsInvul(true);
		_isHalterSpawned = true;
	}
	
	protected void deleteVanHalter()
	{
		_vanHalter.setIsInvul(false);
		_vanHalter.getSpawn().stopRespawn();
		_vanHalter.deleteMe();
	}
	
	protected void loadRitualOffering()
	{
		_ritualOfferingSpawn = null;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
			statement.setInt(1, 32038);
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				L2NpcTemplate template1 = NpcData.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					L2Spawn spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setLocx(rset.getInt("locx"));
					spawnDat.setLocy(rset.getInt("locy"));
					spawnDat.setLocz(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					_ritualOfferingSpawn = spawnDat;
					continue;
				}
				
				_log.warning("VanHalterManager.loadRitualOffering: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
			}
			
			rset.close();
			statement.close();
			if (Config.DEBUG)
				_log.info("VanHalterManager.loadRitualOffering: Loaded Ritual Offering spawn locations.");
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			_log.warning("VanHalterManager.loadRitualOffering: Spawn could not be initialized: " + e);
		}
	}
	
	protected void spawnRitualOffering()
	{
		_ritualOffering = _ritualOfferingSpawn.doSpawn();
		
		_ritualOffering.setIsInvul(true);
		_ritualOffering.setIsParalyzed(true);
	}
	
	protected void deleteRitualOffering()
	{
		_ritualOffering.setIsInvul(false);
		_ritualOffering.setIsParalyzed(false);
		_ritualOffering.getSpawn().stopRespawn();
		_ritualOffering.deleteMe();
	}
	
	protected void loadRitualSacrifice()
	{
		_ritualSacrificeSpawn = null;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay FROM vanhalter_spawnlist Where npc_templateid = ? ORDER BY id");
			statement.setInt(1, 22195);
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				L2NpcTemplate template1 = NpcData.getInstance().getTemplate(rset.getInt("npc_templateid"));
				if (template1 != null)
				{
					L2Spawn spawnDat = new L2Spawn(template1);
					spawnDat.setAmount(rset.getInt("count"));
					spawnDat.setLocx(rset.getInt("locx"));
					spawnDat.setLocy(rset.getInt("locy"));
					spawnDat.setLocz(rset.getInt("locz"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					_ritualSacrificeSpawn = spawnDat;
					continue;
				}
				
				_log.warning("VanHalterManager.loadRitualSacrifice: Data missing in NPC table for ID: " + rset.getInt("npc_templateid") + ".");
			}
			
			rset.close();
			statement.close();
			if (Config.DEBUG)
				_log.info("VanHalterManager.loadRitualSacrifice: Loaded Ritual Sacrifice spawn locations.");
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			_log.warning("VanHalterManager.loadRitualSacrifice: Spawn could not be initialized: " + e);
		}
	}
	
	protected void spawnRitualSacrifice()
	{
		_ritualSacrifice = _ritualSacrificeSpawn.doSpawn();
		
		_ritualSacrifice.setIsInvul(true);
		_isSacrificeSpawned = true;
	}
	
	protected void deleteRitualSacrifice()
	{
		if (!_isSacrificeSpawned)
			return;
		_ritualSacrifice.getSpawn().stopRespawn();
		_ritualSacrifice.deleteMe();
		_isSacrificeSpawned = false;
	}
	
	protected void spawnCameraMarker()
	{
		_cameraMarker.clear();
		for (int i = 1; i <= _cameraMarkerSpawn.size(); i++)
		{
			_cameraMarker.put(Integer.valueOf(i), _cameraMarkerSpawn.get(Integer.valueOf(i)).doSpawn());
			_cameraMarker.get(Integer.valueOf(i)).getSpawn().stopRespawn();
		}
	}
	
	protected void deleteCameraMarker()
	{
		if (_cameraMarker.isEmpty())
			return;
		for (int i = 1; i <= _cameraMarker.size(); i++)
			_cameraMarker.get(Integer.valueOf(i)).deleteMe();
		_cameraMarker.clear();
	}
	
	public void intruderDetection(L2PcInstance intruder)
	{
		if ((_lockUpDoorOfAltarTask == null) && (!_isLocked) && (_isCaptainSpawned))
			_lockUpDoorOfAltarTask = ThreadPoolManager.getInstance().scheduleGeneral(new LockUpDoorOfAltar(), 180 * 6000);
	}
	
	protected void openDoorOfAltar(boolean loop)
	{
		for (L2DoorInstance door : _doorOfAltar)
			try
			{
				door.openMe();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				_log.warning(e.getMessage() + " :" + e);
			}
		
		if (loop)
		{
			_isLocked = false;
			
			if (_closeDoorOfAltarTask != null)
				_closeDoorOfAltarTask.cancel(false);
			_closeDoorOfAltarTask = null;
			_closeDoorOfAltarTask = ThreadPoolManager.getInstance().scheduleGeneral(new CloseDoorOfAltar(), 5400 * 6000);
		}
		else
		{
			if (_closeDoorOfAltarTask != null)
				_closeDoorOfAltarTask.cancel(false);
			_closeDoorOfAltarTask = null;
		}
	}
	
	protected void closeDoorOfAltar(boolean loop)
	{
		for (L2DoorInstance door : _doorOfAltar)
			door.closeMe();
		
		if (loop)
		{
			if (_openDoorOfAltarTask != null)
				_openDoorOfAltarTask.cancel(false);
			_openDoorOfAltarTask = null;
			_openDoorOfAltarTask = ThreadPoolManager.getInstance().scheduleGeneral(new OpenDoorOfAltar(), 5400 * 6000);
		}
		else
		{
			if (_openDoorOfAltarTask != null)
				_openDoorOfAltarTask.cancel(false);
			_openDoorOfAltarTask = null;
		}
	}
	
	protected void openDoorOfSacrifice()
	{
		for (L2DoorInstance door : _doorOfSacrifice)
			try
			{
				door.openMe();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				_log.warning(e.getMessage() + " :" + e);
			}
	}
	
	protected void closeDoorOfSacrifice()
	{
		for (L2DoorInstance door : _doorOfSacrifice)
			try
			{
				door.closeMe();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				_log.warning(e.getMessage() + " :" + e);
			}
	}
	
	public void checkTriolRevelationDestroy()
	{
		if (_isCaptainSpawned)
			return;
		boolean isTriolRevelationDestroyed = true;
		for (L2Spawn tra : _triolRevelationAlive)
			if (!tra.getLastSpawn().isDead())
				isTriolRevelationDestroyed = false;
		
		if (isTriolRevelationDestroyed)
			spawnRoyalGuardCaptain();
	}
	
	public void checkRoyalGuardCaptainDestroy()
	{
		if (!_isHalterSpawned)
			return;
		deleteRoyalGuard();
		deleteRoyalGuardCaptain();
		spawnGuardOfAltar();
		openDoorOfSacrifice();
		
		_vanHalter.setIsInvul(true);
		spawnCameraMarker();
		
		if (_timeUpTask != null)
			_timeUpTask.cancel(false);
		_timeUpTask = null;
		
		_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(1), 20 * 6000);
	}
	
	protected void combatBeginning()
	{
		if (_timeUpTask != null)
			_timeUpTask.cancel(false);
		_timeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new TimeUp(), 7200 * 6000);
		
		Map<Integer, L2PcInstance> _targets = new HashMap<>();
		int i = 0;
		
		for (L2PcInstance pc : L2World.getInstance().getVisibleObjects(_vanHalter, L2PcInstance.class))
		{
			i++;
			_targets.put(Integer.valueOf(i), pc);
		}
		
		_vanHalter.reduceCurrentHp(1.0D, _targets.get(Integer.valueOf(Rnd.get(1, i))));
	}
	
	public void callRoyalGuardHelper()
	{
		if (!_isHelperCalled)
		{
			_isHelperCalled = true;
			_halterEscapeTask = ThreadPoolManager.getInstance().scheduleGeneral(new HalterEscape(), 500L);
			_callRoyalGuardHelperTask = ThreadPoolManager.getInstance().scheduleGeneral(new CallRoyalGuardHelper(), 1000L);
		}
	}
	
	protected void addBleeding()
	{
		L2Skill bleed = SkillTable.getInstance().getInfo(4615, 12);
		
		for (L2Npc tr : _triolRevelation)
		{
			if (tr == null || tr.isDead())
				continue;
			
			List<L2PcInstance> bpc = new ArrayList<>();
			
			for (L2PcInstance pc : L2World.getInstance().getVisibleObjects(tr, L2PcInstance.class, tr.getAggroRange()))
			{
				if (pc.getFirstEffect(bleed) == null)
				{
					bleed.getEffects(tr, pc);
					tr.broadcastPacket(new MagicSkillUse(tr, pc, bleed.getId(), 12, 1, 1));
				}
				
				bpc.add(pc);
			}
			_bleedingPlayers.remove(Integer.valueOf(tr.getNpcId()));
			_bleedingPlayers.put(Integer.valueOf(tr.getNpcId()), bpc);
		}
	}
	
	public void removeBleeding(int npcId)
	{
		if (_bleedingPlayers.get(Integer.valueOf(npcId)) == null)
			return;
		for (L2PcInstance pc : _bleedingPlayers.get(Integer.valueOf(npcId)))
			if (pc.getFirstEffect(L2Effect.EffectType.DMG_OVER_TIME) != null)
				pc.stopEffects(L2Effect.EffectType.DMG_OVER_TIME);
		_bleedingPlayers.remove(Integer.valueOf(npcId));
	}
	
	public void enterInterval()
	{
		if (_callRoyalGuardHelperTask != null)
			_callRoyalGuardHelperTask.cancel(false);
		_callRoyalGuardHelperTask = null;
		
		if (_closeDoorOfAltarTask != null)
			_closeDoorOfAltarTask.cancel(false);
		_closeDoorOfAltarTask = null;
		
		if (_halterEscapeTask != null)
			_halterEscapeTask.cancel(false);
		_halterEscapeTask = null;
		
		if (_intervalTask != null)
			_intervalTask.cancel(false);
		_intervalTask = null;
		
		if (_lockUpDoorOfAltarTask != null)
			_lockUpDoorOfAltarTask.cancel(false);
		_lockUpDoorOfAltarTask = null;
		
		if (_movieTask != null)
			_movieTask.cancel(false);
		_movieTask = null;
		
		if (_openDoorOfAltarTask != null)
			_openDoorOfAltarTask.cancel(false);
		_openDoorOfAltarTask = null;
		
		if (_timeUpTask != null)
			_timeUpTask.cancel(false);
		_timeUpTask = null;
		
		if (_vanHalter.isDead())
			_vanHalter.getSpawn().stopRespawn();
		else
			deleteVanHalter();
		deleteRoyalGuardHepler();
		deleteRoyalGuardCaptain();
		deleteRoyalGuard();
		deleteRitualOffering();
		deleteRitualSacrifice();
		deleteGuardOfAltar();
		
		if (_intervalTask != null)
			_intervalTask.cancel(false);
		
		Integer status = GrandBossManager.getInstance().getBossStatus(29062);
		
		if (status.intValue() != 0)
		{
			long interval = Rnd.get((172800 * 6000), (172800 * 6000) + (86400 * 6000)) * 3600000;
			StatsSet info = GrandBossManager.getStatsSet(29062);
			info.set("respawn_time", System.currentTimeMillis() + interval);
			GrandBossManager.setStatsSet(29062, info);
			GrandBossManager.setBossStatus(29062, 0);
		}
		
		StatsSet info = GrandBossManager.getStatsSet(29062);
		long temp = info.getLong("respawn_time") - System.currentTimeMillis();
		_intervalTask = ThreadPoolManager.getInstance().scheduleGeneral(new Interval(), temp);
	}
	
	public void setupAltar()
	{
		if (_callRoyalGuardHelperTask != null)
			_callRoyalGuardHelperTask.cancel(false);
		_callRoyalGuardHelperTask = null;
		
		if (_closeDoorOfAltarTask != null)
			_closeDoorOfAltarTask.cancel(false);
		_closeDoorOfAltarTask = null;
		
		if (_halterEscapeTask != null)
			_halterEscapeTask.cancel(false);
		_halterEscapeTask = null;
		
		if (_intervalTask != null)
			_intervalTask.cancel(false);
		_intervalTask = null;
		
		if (_lockUpDoorOfAltarTask != null)
			_lockUpDoorOfAltarTask.cancel(false);
		_lockUpDoorOfAltarTask = null;
		
		if (_movieTask != null)
			_movieTask.cancel(false);
		_movieTask = null;
		
		if (_openDoorOfAltarTask != null)
			_openDoorOfAltarTask.cancel(false);
		_openDoorOfAltarTask = null;
		
		if (_timeUpTask != null)
			_timeUpTask.cancel(false);
		_timeUpTask = null;
		
		deleteVanHalter();
		deleteTriolRevelation();
		deleteRoyalGuardHepler();
		deleteRoyalGuardCaptain();
		deleteRoyalGuard();
		deleteRitualSacrifice();
		deleteRitualOffering();
		deleteGuardOfAltar();
		deleteCameraMarker();
		
		_isLocked = false;
		_isCaptainSpawned = false;
		_isHelperCalled = false;
		_isHalterSpawned = false;
		
		closeDoorOfSacrifice();
		openDoorOfAltar(true);
		
		spawnTriolRevelation();
		spawnRoyalGuard();
		spawnRitualOffering();
		spawnVanHalter();
		
		GrandBossManager.setBossStatus(29062, 1);
		
		if (_timeUpTask != null)
			_timeUpTask.cancel(false);
		_timeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new TimeUp(), 21600 * 1000);
	}
	
	private class Movie implements Runnable
	{
		private final int _taskId;
		
		public Movie(int taskId)
		{
			_taskId = taskId;
		}
		
		@Override
		public void run()
		{
			_vanHalter.setHeading(16384);
			_vanHalter.setTarget(_ritualOffering);
			
			switch (_taskId)
			{
				case 1:
					GrandBossManager.setBossStatus(29062, 2);
					
					for (L2PcInstance pc : L2World.getInstance().getVisibleObjects(_vanHalter, L2PcInstance.class))
						if (pc.getPlanDistanceSq(_vanHalter) <= 6502500.0D)
							_vanHalter.broadcastPacket(new SpecialCamera(_vanHalter.getObjectId(), 50, 90, 0, 0, 15000));
					
					if (_movieTask != null)
						_movieTask.cancel(false);
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(2), 16L);
					
					break;
				case 2:
					for (L2PcInstance pc : L2World.getInstance().getVisibleObjects(_vanHalter, L2PcInstance.class))
						if (pc.getPlanDistanceSq(_cameraMarker.get(Integer.valueOf(5))) <= 6502500.0D)
							_cameraMarker.get(Integer.valueOf(5)).broadcastPacket(new SpecialCamera(_cameraMarker.get(Integer.valueOf(5)).getObjectId(), 1842, 100, -3, 0, 15000));
					
					if (_movieTask != null)
						_movieTask.cancel(false);
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(3), 1L);
					
					break;
				case 3:
					for (L2PcInstance pc : L2World.getInstance().getVisibleObjects(_vanHalter, L2PcInstance.class))
						if (pc.getPlanDistanceSq(_cameraMarker.get(Integer.valueOf(5))) <= 6502500.0D)
							_cameraMarker.get(Integer.valueOf(5)).broadcastPacket(new SpecialCamera(_cameraMarker.get(Integer.valueOf(5)).getObjectId(), 1861, 97, -10, 1500, 15000));
					
					if (_movieTask != null)
						_movieTask.cancel(false);
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(4), 1500L);
					
					break;
				case 4:
					for (L2PcInstance pc : L2World.getInstance().getVisibleObjects(_vanHalter, L2PcInstance.class))
						if (pc.getPlanDistanceSq(_cameraMarker.get(Integer.valueOf(4))) <= 6502500.0D)
							_cameraMarker.get(Integer.valueOf(4)).broadcastPacket(new SpecialCamera(_cameraMarker.get(Integer.valueOf(4)).getObjectId(), 1876, 97, 12, 0, 15000));
					
					if (_movieTask != null)
						_movieTask.cancel(false);
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(5), 1L);
					
					break;
				case 5:
					for (L2PcInstance pc : L2World.getInstance().getVisibleObjects(_vanHalter, L2PcInstance.class))
						if (pc.getPlanDistanceSq(_cameraMarker.get(Integer.valueOf(4))) <= 6502500.0D)
							_cameraMarker.get(Integer.valueOf(4)).broadcastPacket(new SpecialCamera(_cameraMarker.get(Integer.valueOf(4)).getObjectId(), 1839, 94, 0, 1500, 15000));
					
					if (_movieTask != null)
						_movieTask.cancel(false);
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(6), 1500L);
					
					break;
				case 6:
					for (L2PcInstance pc : L2World.getInstance().getVisibleObjects(_vanHalter, L2PcInstance.class))
						if (pc.getPlanDistanceSq(_cameraMarker.get(Integer.valueOf(3))) <= 6502500.0D)
							_cameraMarker.get(Integer.valueOf(3)).broadcastPacket(new SpecialCamera(_cameraMarker.get(Integer.valueOf(3)).getObjectId(), 1872, 94, 15, 0, 15000));
					
					if (_movieTask != null)
						_movieTask.cancel(false);
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(7), 1L);
					
					break;
				case 7:
					for (L2PcInstance pc : L2World.getInstance().getVisibleObjects(_vanHalter, L2PcInstance.class))
						if (pc.getPlanDistanceSq(_cameraMarker.get(Integer.valueOf(3))) <= 6502500.0D)
							_cameraMarker.get(Integer.valueOf(3)).broadcastPacket(new SpecialCamera(_cameraMarker.get(Integer.valueOf(3)).getObjectId(), 1839, 92, 0, 1500, 15000));
					
					if (_movieTask != null)
						_movieTask.cancel(false);
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(8), 1500L);
					
					break;
				case 8:
					
					for (L2PcInstance pc : L2World.getInstance().getVisibleObjects(_vanHalter, L2PcInstance.class))
						if (pc.getPlanDistanceSq(_cameraMarker.get(Integer.valueOf(2))) <= 6502500.0D)
							_cameraMarker.get(Integer.valueOf(2)).broadcastPacket(new SpecialCamera(_cameraMarker.get(Integer.valueOf(2)).getObjectId(), 1872, 92, 15, 0, 15000));
					
					if (_movieTask != null)
						_movieTask.cancel(false);
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(9), 1L);
					
					break;
				case 9:
					for (L2PcInstance pc : L2World.getInstance().getVisibleObjects(_vanHalter, L2PcInstance.class))
						if (pc.getPlanDistanceSq(_cameraMarker.get(Integer.valueOf(2))) <= 6502500.0D)
							_cameraMarker.get(Integer.valueOf(2)).broadcastPacket(new SpecialCamera(_cameraMarker.get(Integer.valueOf(2)).getObjectId(), 1839, 90, 5, 1500, 15000));
					
					if (_movieTask != null)
						_movieTask.cancel(false);
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(10), 1500L);
					
					break;
				case 10:
					for (L2PcInstance pc : L2World.getInstance().getVisibleObjects(_vanHalter, L2PcInstance.class))
						if (pc.getPlanDistanceSq(_cameraMarker.get(Integer.valueOf(1))) <= 6502500.0D)
							_cameraMarker.get(Integer.valueOf(1)).broadcastPacket(new SpecialCamera(_cameraMarker.get(Integer.valueOf(1)).getObjectId(), 1872, 90, 5, 0, 15000));
					
					if (_movieTask != null)
						_movieTask.cancel(false);
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(11), 1L);
					break;
				case 11:
					for (L2PcInstance pc : L2World.getInstance().getVisibleObjects(_vanHalter, L2PcInstance.class))
						if (pc.getPlanDistanceSq(_cameraMarker.get(Integer.valueOf(1))) <= 6502500.0D)
							_cameraMarker.get(Integer.valueOf(1)).broadcastPacket(new SpecialCamera(_cameraMarker.get(Integer.valueOf(1)).getObjectId(), 2002, 90, 2, 1500, 15000));
					
					if (_movieTask != null)
						_movieTask.cancel(false);
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(12), 2000L);
					
					break;
				case 12:
					for (L2PcInstance pc : L2World.getInstance().getVisibleObjects(_vanHalter, L2PcInstance.class))
						if (pc.getPlanDistanceSq(_vanHalter) <= 6502500.0D)
							_vanHalter.broadcastPacket(new SpecialCamera(_vanHalter.getObjectId(), 50, 90, 10, 0, 15000));
					
					if (_movieTask != null)
						_movieTask.cancel(false);
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(13), 1000L);
					
					break;
				case 13:
					L2Skill skill = SkillTable.getInstance().getInfo(1168, 7);
					_ritualOffering.setIsInvul(false);
					_vanHalter.setTarget(_ritualOffering);
					
					_vanHalter.doCast(skill);
					
					if (_movieTask != null)
						_movieTask.cancel(false);
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(14), 4700L);
					
					break;
				case 14:
					_ritualOffering.setIsInvul(false);
					_ritualOffering.reduceCurrentHp(_ritualOffering.getMaxHp() + 1, _vanHalter);
					
					if (_movieTask != null)
						_movieTask.cancel(false);
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(15), 4300L);
					
					break;
				case 15:
					spawnRitualSacrifice();
					deleteRitualOffering();
					
					for (L2PcInstance pc : L2World.getInstance().getVisibleObjects(_vanHalter, L2PcInstance.class))
						if (pc.getPlanDistanceSq(_vanHalter) <= 6502500.0D)
							_vanHalter.broadcastPacket(new SpecialCamera(_vanHalter.getObjectId(), 100, 90, 15, 1500, 15000));
					
					if (_movieTask != null)
						_movieTask.cancel(false);
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(16), 2000L);
					
					break;
				case 16:
					for (L2PcInstance pc : L2World.getInstance().getVisibleObjects(_vanHalter, L2PcInstance.class))
						if (pc.getPlanDistanceSq(_vanHalter) <= 6502500.0D)
							_vanHalter.broadcastPacket(new SpecialCamera(_vanHalter.getObjectId(), 5200, 90, -10, 9500, 6000));
					
					if (_movieTask != null)
						_movieTask.cancel(false);
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(17), 6000L);
					
					break;
				case 17:
					deleteRitualSacrifice();
					deleteCameraMarker();
					
					_vanHalter.setIsInvul(false);
					
					if (_movieTask != null)
						_movieTask.cancel(false);
					_movieTask = null;
					_movieTask = ThreadPoolManager.getInstance().scheduleGeneral(new Movie(18), 1000L);
					
					break;
				case 18:
					combatBeginning();
					if (_movieTask != null)
						_movieTask.cancel(false);
					_movieTask = null;
			}
		}
	}
	
	private class TimeUp implements Runnable
	{
		TimeUp()
		{
		}
		
		@Override
		public void run()
		{
			enterInterval();
		}
	}
	
	private class Interval implements Runnable
	{
		Interval()
		{
		}
		
		@Override
		public void run()
		{
			setupAltar();
		}
	}
	
	private class Bleeding implements Runnable
	{
		Bleeding()
		{
		}
		
		@Override
		public void run()
		{
			addBleeding();
			
			if (_setBleedTask != null)
				_setBleedTask.cancel(false);
			_setBleedTask = ThreadPoolManager.getInstance().scheduleGeneral(new Bleeding(), 2000L);
		}
	}
	
	private class HalterEscape implements Runnable
	{
		HalterEscape()
		{
		}
		
		@Override
		public void run()
		{
			if ((_royalGuardHepler.size() <= 6) && (!_vanHalter.isDead()))
			{
				if (_vanHalter.isAfraid())
				{
					_vanHalter.stopEffects(L2Effect.EffectType.FEAR);
					_vanHalter.setIsAfraid(false);
					_vanHalter.updateAbnormalEffect();
				}
				else
				{
					_vanHalter.startFear();
					if (_vanHalter.getZ() >= -10476)
					{
						Location pos = new Location(-16397, -53308, -10448, 0);
						if ((_vanHalter.getX() == pos.getX()) && (_vanHalter.getY() == pos.getY()))
						{
							_vanHalter.stopEffects(L2Effect.EffectType.FEAR);
							_vanHalter.setIsAfraid(false);
							_vanHalter.updateAbnormalEffect();
						}
						else
							_vanHalter.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, pos);
					}
					else if (_vanHalter.getX() >= -16397)
					{
						Location pos = new Location(-15548, -54830, -10475, 0);
						_vanHalter.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, pos);
					}
					else
					{
						Location pos = new Location(-17248, -54830, -10475, 0);
						_vanHalter.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, pos);
					}
				}
				if (_halterEscapeTask != null)
					_halterEscapeTask.cancel(false);
				_halterEscapeTask = ThreadPoolManager.getInstance().scheduleGeneral(new HalterEscape(), 5000L);
			}
			else
			{
				_vanHalter.stopEffects(L2Effect.EffectType.FEAR);
				_vanHalter.setIsAfraid(false);
				_vanHalter.updateAbnormalEffect();
				if (_halterEscapeTask != null)
					_halterEscapeTask.cancel(false);
				_halterEscapeTask = null;
			}
		}
	}
	
	private class CallRoyalGuardHelper implements Runnable
	{
		CallRoyalGuardHelper()
		{
		}
		
		@Override
		public void run()
		{
			spawnRoyalGuardHepler();
			
			if ((_royalGuardHepler.size() <= 6) && (!_vanHalter.isDead()))
			{
				if (_callRoyalGuardHelperTask != null)
					_callRoyalGuardHelperTask.cancel(false);
				_callRoyalGuardHelperTask = ThreadPoolManager.getInstance().scheduleGeneral(new CallRoyalGuardHelper(), 10 * 6000);
			}
			else
			{
				if (_callRoyalGuardHelperTask != null)
					_callRoyalGuardHelperTask.cancel(false);
				_callRoyalGuardHelperTask = null;
			}
		}
	}
	
	private class CloseDoorOfAltar implements Runnable
	{
		CloseDoorOfAltar()
		{
		}
		
		@Override
		public void run()
		{
			closeDoorOfAltar(true);
		}
	}
	
	private class OpenDoorOfAltar implements Runnable
	{
		OpenDoorOfAltar()
		{
		}
		
		@Override
		public void run()
		{
			openDoorOfAltar(true);
		}
	}
	
	private class LockUpDoorOfAltar implements Runnable
	{
		LockUpDoorOfAltar()
		{
		}
		
		@Override
		public void run()
		{
			closeDoorOfAltar(false);
			_isLocked = true;
			_lockUpDoorOfAltarTask = null;
		}
	}
	
	public static void main(String[] args)
	{
		new VanHalter();
	}
}