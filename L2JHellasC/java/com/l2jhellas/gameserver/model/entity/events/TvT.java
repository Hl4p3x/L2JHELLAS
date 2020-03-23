package com.l2jhellas.gameserver.model.entity.events;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.Announcements;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.datatables.sql.ItemTable;
import com.l2jhellas.gameserver.datatables.sql.NpcData;
import com.l2jhellas.gameserver.datatables.sql.SpawnTable;
import com.l2jhellas.gameserver.datatables.xml.DoorData;
import com.l2jhellas.gameserver.enums.Team;
import com.l2jhellas.gameserver.enums.player.ChatType;
import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.L2Spawn;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.actor.group.party.L2Party;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PetInstance;
import com.l2jhellas.gameserver.model.entity.olympiad.OlympiadManager;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.CreatureSay;
import com.l2jhellas.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;
import com.l2jhellas.util.Rnd;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class TvT
{
	protected static final Logger _log = Logger.getLogger(TvT.class.getName());
	public static String _eventName = "";
	public static String _eventDesc = "";
	public static String _topTeam = "";
	public static String _joiningLocationName = "";
	public static List<String> _teams = new ArrayList<>();
	public static List<String> _savePlayers = new ArrayList<>();
	public static List<String> _savePlayerTeams = new ArrayList<>();
	
	public static List<L2PcInstance> _players = new ArrayList<>();
	public static List<L2PcInstance> _playersShuffle = new ArrayList<>();
	public static List<Integer> _teamPlayersCount = new ArrayList<>();
	public static List<Integer> _teamKillsCount = new ArrayList<>();
	public static List<Integer> _teamColors = new ArrayList<>();
	public static List<Integer> _teamsX = new ArrayList<>();
	public static List<Integer> _teamsY = new ArrayList<>();
	public static List<Integer> _teamsZ = new ArrayList<>();
	public static boolean _joining = false;
	public static boolean _teleport = false;
	public static boolean _started = false;
	public static boolean _sitForced = false;
	public static L2Spawn _npcSpawn;
	
	public static int _npcId = 0;
	public static int _npcX = 0;
	public static int _npcY = 0;
	public static int _npcZ = 0;
	public static int _npcHeading = 0;
	
	public static int _rewardId = 0;
	public static int _rewardAmount = 0;
	
	public static int _topKills = 0;
	public static int _minlvl = 0;
	public static int _maxlvl = 0;
	
	public static int _joinTime = 0;
	public static int _eventTime = 0;
	
	public static int _minPlayers = 0;
	public static int _maxPlayers = 0;
	
	public static int _playerWon = 0;
	
	public static void AnnounceToPlayers(Boolean toall, String announce)
	{
		if (toall)
			Announcements.getInstance().gameAnnounceToAll(announce);
		else
		{
			CreatureSay cs = new CreatureSay(0, ChatType.ANNOUNCEMENT.getClientId(), "", announce);
			if (_players != null && !_players.isEmpty())
			{
				for (L2PcInstance player : _players)
				{
					if (player != null && player.isOnline() != 0)
						player.sendPacket(cs);
				}
			}
		}
	}
	
	public static void kickPlayerFromTvt(L2PcInstance playerToKick)
	{
		if (playerToKick == null)
			return;
		
		if (_joining)
		{
			_playersShuffle.remove(playerToKick);
			_players.remove(playerToKick);
			playerToKick._inEventTvT = false;
			playerToKick._teamNameTvT = "";
			playerToKick._countTvTkills = 0;
		}
		if (_started || _teleport)
		{
			_playersShuffle.remove(playerToKick);
			playerToKick._inEventTvT = false;
			removePlayer(playerToKick);
			if (playerToKick.isOnline() != 0)
			{
				playerToKick.getAppearance().setNameColor(playerToKick._originalNameColorTvT);
				playerToKick.setKarma(playerToKick._originalKarmaTvT);
				playerToKick.setTitle(playerToKick._originalTitleTvT);
				playerToKick.broadcastUserInfo();
				playerToKick.sendMessage("You have been kicked from the TvT.");
				playerToKick.teleToLocation(_npcX, _npcY, _npcZ, false);
			}
		}
	}
	
	public static void setNpcPos(L2PcInstance activeChar)
	{
		_npcX = activeChar.getX();
		_npcY = activeChar.getY();
		_npcZ = activeChar.getZ();
		_npcHeading = activeChar.getHeading();
	}
	
	public static void setNpcPos(int x, int y, int z)
	{
		_npcX = x;
		_npcY = y;
		_npcZ = z;
	}
	
	public static void addTeam(String teamName)
	{
		if (!checkTeamOk())
		{
			if (Config.DEBUG)
				_log.log(Level.FINER, "TvT Engine[addTeam(" + teamName + ")]: checkTeamOk() = false");
			return;
		}
		
		if (teamName.equals(" "))
			return;
		
		_teams.add(teamName);
		_teamPlayersCount.add(0);
		_teamKillsCount.add(0);
		_teamColors.add(0);
		_teamsX.add(0);
		_teamsY.add(0);
		_teamsZ.add(0);
	}
	
	public static boolean checkMaxLevel(int maxlvl)
	{
		return _minlvl < maxlvl;
	}
	
	public static boolean checkMinLevel(int minlvl)
	{
		return _maxlvl > minlvl;
	}
	
	public static boolean checkMinPlayers(int players)
	{
		return _minPlayers <= players;
	}
	
	public static boolean checkMaxPlayers(int players)
	{
		return _maxPlayers > players;
	}
	
	public static void removeTeam(String teamName)
	{
		if (!checkTeamOk() || _teams.isEmpty())
		{
			if (Config.DEBUG)
				_log.log(Level.FINER, "TvT Engine[removeTeam(" + teamName + ")]: checkTeamOk() = false");
			return;
		}
		
		if (teamPlayersCount(teamName) > 0)
		{
			if (Config.DEBUG)
				_log.log(Level.FINER, "TvT Engine[removeTeam(" + teamName + ")]: teamPlayersCount(teamName) > 0");
			return;
		}
		
		int index = _teams.indexOf(teamName);
		
		if (index == -1)
			return;
		
		_teamsZ.remove(index);
		_teamsY.remove(index);
		_teamsX.remove(index);
		_teamColors.remove(index);
		_teamKillsCount.remove(index);
		_teamPlayersCount.remove(index);
		_teams.remove(index);
	}
	
	public static void setTeamPos(String teamName, L2PcInstance activeChar)
	{
		int index = _teams.indexOf(teamName);
		
		if (index == -1)
			return;
		
		_teamsX.set(index, activeChar.getX());
		_teamsY.set(index, activeChar.getY());
		_teamsZ.set(index, activeChar.getZ());
	}
	
	public static void setTeamPos(String teamName, int x, int y, int z)
	{
		int index = _teams.indexOf(teamName);
		
		if (index == -1)
			return;
		
		_teamsX.set(index, x);
		_teamsY.set(index, y);
		_teamsZ.set(index, z);
	}
	
	public static void setTeamColor(String teamName, int color)
	{
		if (!checkTeamOk())
			return;
		
		int index = _teams.indexOf(teamName);
		
		if (index == -1)
			return;
		
		_teamColors.set(index, color);
	}
	
	public static boolean checkTeamOk()
	{
		return !(_started || _teleport || _joining);
	}
	
	public static void startJoin(L2PcInstance activeChar)
	{
		if (!startJoinOk())
		{
			activeChar.sendMessage("Event not setted propertly.");
			if (Config.DEBUG)
				_log.log(Level.FINER, "TvT Engine[startJoin(" + activeChar.getName() + ")]: startJoinOk() = false");
			return;
		}
		_joining = true;
		spawnEventNpc(activeChar);
		AnnounceToPlayers(true, _eventName);
		if (Config.TVT_ANNOUNCE_REWARD)
			AnnounceToPlayers(true, "Reward: " + _rewardAmount + " " + ItemTable.getInstance().getTemplate(_rewardId).getItemName());
		AnnounceToPlayers(true, "Recruiting levels " + _minlvl + " to " + _maxlvl);
		AnnounceToPlayers(true, "Join in " + _joiningLocationName + "!");
	}
	
	public static void startJoin()
	{
		if (!startJoinOk())
		{
			_log.warning(TvT.class.getSimpleName() + ":  Event not setted propertly.");
			if (Config.DEBUG)
				_log.log(Level.FINER, "TvT Engine[startJoin(startJoinOk() = false");
			return;
		}
		_joining = true;
		spawnEventNpc();
		AnnounceToPlayers(true, _eventName);
		if (Config.TVT_ANNOUNCE_REWARD)
			AnnounceToPlayers(true, "Reward: " + _rewardAmount + " " + ItemTable.getInstance().getTemplate(_rewardId).getItemName());
		AnnounceToPlayers(true, "Recruiting levels " + _minlvl + " to " + _maxlvl);
		AnnounceToPlayers(true, "Join in " + _joiningLocationName + "!");
	}
	
	public static boolean startAutoJoin()
	{
		if (!startJoinOk())
		{
			if (Config.DEBUG)
				_log.log(Level.FINER, "TvT Engine[startJoin]: startJoinOk() = false");
			return false;
		}
		
		_joining = true;
		spawnEventNpc();
		AnnounceToPlayers(true, _eventName);
		if (Config.TVT_ANNOUNCE_REWARD)
			AnnounceToPlayers(true, "Reward: " + _rewardAmount + " " + ItemTable.getInstance().getTemplate(_rewardId).getItemName());
		AnnounceToPlayers(true, "Recruiting levels " + _minlvl + " to " + _maxlvl);
		AnnounceToPlayers(true, "Join in " + _joiningLocationName + "!");
		return true;
	}
	
	public static boolean startJoinOk()
	{
		return !(_started || _teleport || _joining || _teams.size() < 2 || _eventName.isEmpty() || _joiningLocationName.isEmpty() || _eventDesc.isEmpty() || _npcId == 0 || _npcX == 0 || _npcY == 0 || _npcZ == 0 || _rewardId == 0 || _rewardAmount == 0 || _teamsX.contains(0) || _teamsY.contains(0) || _teamsZ.contains(0));
	}
	
	private static void spawnEventNpc(L2PcInstance activeChar)
	{
		L2NpcTemplate tmpl = NpcData.getInstance().getTemplate(_npcId);
		
		try
		{
			_npcSpawn = new L2Spawn(tmpl);
			
			_npcSpawn.setLocx(_npcX);
			_npcSpawn.setLocy(_npcY);
			_npcSpawn.setLocz(_npcZ);
			_npcSpawn.setAmount(1);
			_npcSpawn.setHeading(_npcHeading);
			_npcSpawn.setRespawnDelay(1);
			
			SpawnTable.getInstance().addNewSpawn(_npcSpawn, false);
			
			_npcSpawn.init();
			_npcSpawn.getLastSpawn().getStatus().setCurrentHp(999999999);
			_npcSpawn.getLastSpawn().setTitle(_eventName);
			_npcSpawn.getLastSpawn()._isEventMobTvT = true;
			_npcSpawn.getLastSpawn().isAggressive();
			_npcSpawn.getLastSpawn().decayMe();
			_npcSpawn.getLastSpawn().spawnMe(_npcSpawn.getLastSpawn().getX(), _npcSpawn.getLastSpawn().getY(), _npcSpawn.getLastSpawn().getZ());
			
			_npcSpawn.getLastSpawn().broadcastPacket(new MagicSkillUse(_npcSpawn.getLastSpawn(), _npcSpawn.getLastSpawn(), 1034, 1, 1, 1));
		}
		catch (Exception e)
		{
			_log.warning(TvT.class.getSimpleName() + ":  TvT Engine[spawnEventNpc(" + activeChar.getName() + ")]: exception: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	private static void spawnEventNpc()
	{
		L2NpcTemplate tmpl = NpcData.getInstance().getTemplate(_npcId);
		
		try
		{
			_npcSpawn = new L2Spawn(tmpl);
			
			_npcSpawn.setLocx(_npcX);
			_npcSpawn.setLocy(_npcY);
			_npcSpawn.setLocz(_npcZ);
			_npcSpawn.setAmount(1);
			_npcSpawn.setHeading(_npcHeading);
			_npcSpawn.setRespawnDelay(1);
			
			SpawnTable.getInstance().addNewSpawn(_npcSpawn, false);
			
			_npcSpawn.init();
			_npcSpawn.getLastSpawn().getStatus().setCurrentHp(999999999);
			_npcSpawn.getLastSpawn().setTitle(_eventName);
			_npcSpawn.getLastSpawn()._isEventMobTvT = true;
			_npcSpawn.getLastSpawn().isAggressive();
			_npcSpawn.getLastSpawn().decayMe();
			_npcSpawn.getLastSpawn().spawnMe(_npcSpawn.getLastSpawn().getX(), _npcSpawn.getLastSpawn().getY(), _npcSpawn.getLastSpawn().getZ());
			
			_npcSpawn.getLastSpawn().broadcastPacket(new MagicSkillUse(_npcSpawn.getLastSpawn(), _npcSpawn.getLastSpawn(), 1034, 1, 1, 1));
		}
		catch (Exception e)
		{
			_log.warning(TvT.class.getSimpleName() + ":  TvT Engine[spawnEventNpc(exception: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public static void teleportStart()
	{
		if (!_joining || _started || _teleport)
			return;
		
		if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE") && checkMinPlayers(_playersShuffle.size()))
		{
			removeOfflinePlayers();
			shuffleTeams();
		}
		else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE") && !checkMinPlayers(_playersShuffle.size()))
		{
			AnnounceToPlayers(true, "Not enough players for event. Min Requested : " + _minPlayers + ", Participating : " + _playersShuffle.size());
			return;
		}
		
		_joining = false;
		AnnounceToPlayers(false, _eventName + "Teleport to team spot in 20 seconds!");
		
		setUserData();
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				TvT.sit();
				
				for (L2PcInstance player : _players)
				{
					if (player != null)
					{
						if (Config.TVT_ON_START_UNSUMMON_PET)
						{
							// Remove Summon's buffs
							if (player.getPet() != null)
							{
								L2Summon summon = player.getPet();
								for (L2Effect e : summon.getAllEffects())
								{
									if (e != null)
										e.exit();
								}
								
								if (summon instanceof L2PetInstance)
									summon.unSummon(player);
							}
						}
						
						if (Config.TVT_ON_START_REMOVE_ALL_EFFECTS)
						{
							for (L2Effect e : player.getAllEffects())
							{
								if (e != null)
									e.exit();
							}
						}
						
						// Remove player from his party
						if (player.getParty() != null)
						{
							L2Party party = player.getParty();
							party.removePartyMember(player);
						}
						
						player.setTitle("Kills: " + player._countTvTkills);
						player.teleToLocation(_teamsX.get(_teams.indexOf(player._teamNameTvT)), _teamsY.get(_teams.indexOf(player._teamNameTvT)), _teamsZ.get(_teams.indexOf(player._teamNameTvT)));
					}
				}
			}
		}, 20000);
		_teleport = true;
	}
	
	public static boolean teleportAutoStart()
	{
		if (!_joining || _started || _teleport)
			return false;
		
		if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE") && checkMinPlayers(_playersShuffle.size()))
		{
			removeOfflinePlayers();
			shuffleTeams();
		}
		else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE") && !checkMinPlayers(_playersShuffle.size()))
		{
			AnnounceToPlayers(true, "Not enough players for event. Min Requested : " + _minPlayers + ", Participating : " + _playersShuffle.size());
			return false;
		}
		
		_joining = false;
		AnnounceToPlayers(false, _eventName + "Teleport to team spot in 20 seconds!");
		
		setUserData();
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				TvT.sit();
				
				for (L2PcInstance player : _players)
				{
					if (player != null)
					{
						if (Config.TVT_ON_START_UNSUMMON_PET)
						{
							// Remove Summon's buffs
							if (player.getPet() != null)
							{
								L2Summon summon = player.getPet();
								for (L2Effect e : summon.getAllEffects())
								{
									if (e != null)
										e.exit();
								}
								
								if (summon instanceof L2PetInstance)
									summon.unSummon(player);
							}
						}
						
						if (Config.TVT_ON_START_REMOVE_ALL_EFFECTS)
						{
							for (L2Effect e : player.getAllEffects())
							{
								if (e != null)
									e.exit();
							}
						}
						
						// Remove player from his party
						if (player.getParty() != null)
						{
							L2Party party = player.getParty();
							party.removePartyMember(player);
						}
						player.setTitle("Kills: " + player._countTvTkills);
						player.teleToLocation(_teamsX.get(_teams.indexOf(player._teamNameTvT)), _teamsY.get(_teams.indexOf(player._teamNameTvT)), _teamsZ.get(_teams.indexOf(player._teamNameTvT)));
					}
				}
			}
		}, 20000);
		_teleport = true;
		return true;
	}
	
	public static void startEvent(L2PcInstance activeChar)
	{
		if (_inProgress)
		{
			activeChar.sendMessage("A TvT event is already in progress, try abort.");
			return;
		}
		
		if (!startEventOk())
		{
			if (Config.DEBUG)
				_log.log(Level.FINER, "TvT Engine[startEvent(" + activeChar.getName() + ")]: startEventOk() = false");
			return;
		}
		
		_teleport = false;
		sit();
		
		if (Config.TVT_CLOSE_COLISEUM_DOORS)
			closeColiseumDoors();
		
		AnnounceToPlayers(false, _eventName + "Started. Go to kill your enemies!");
		_started = true;
		_inProgress = true;
	}
	
	public static void setJoinTime(int time)
	{
		_joinTime = time;
	}
	
	public static void setEventTime(int time)
	{
		_eventTime = time;
	}
	
	public static boolean startAutoEvent()
	{
		if (!startEventOk())
		{
			if (Config.DEBUG)
				_log.log(Level.FINER, "TvT Engine[startEvent]: startEventOk() = false");
			return false;
		}
		
		_teleport = false;
		sit();
		
		if (Config.TVT_CLOSE_COLISEUM_DOORS)
			closeColiseumDoors();
		
		AnnounceToPlayers(false, _eventName + "Started. Go to kill your enemies!");
		_started = true;
		return true;
	}
	
	public static synchronized void autoEvent()
	{
		_log.info("Starting TvT!");
		if (startAutoJoin())
		{
			_eventType = 2;
			
			if (_joinTime > 0)
				waiter(_joinTime * 60 * 1000); // minutes for join event
			else if (_joinTime <= 0)
			{
				_log.info("TvT: join time <=0 aborting event.");
				abortEvent();
				return;
			}
			if (teleportAutoStart())
			{
				waiter(30 * 1000); // 30 sec wait time untill start fight after teleported
				if (startAutoEvent())
				{
					_log.log(Level.FINER, "TvT: waiting.....minutes for event time " + TvT._eventTime);
					
					waiter(_eventTime * 60 * 1000); // minutes for event time
					finishEvent();
					
					_log.info("TvT: waiting... delay for final messages ");
					waiter(60000);// just a give a delay delay for final messages
					sendFinalMessages();
				}
			}
			else if (!teleportAutoStart())
			{
				abortEvent();
			}
		}
	}
	
	private synchronized static void waiter(long interval)
	{
		long startWaiterTime = System.currentTimeMillis();
		int seconds = (int) (interval / 1000);
		
		String npcManager = NpcData.getInstance().getTemplate(_npcId).getName();
		
		while (startWaiterTime + interval > System.currentTimeMillis())
		{
			seconds--; // here because we don't want to see two time announce at the same time
			
			if (_joining || _started || _teleport)
			{
				switch (seconds)
				{
					case 3600: // 1 hour left
						if (_joining)
						{
							AnnounceToPlayers(true, "TvT: Joinable in " + _joiningLocationName + "!");
							AnnounceToPlayers(true, "TvT: " + seconds / 60 / 60 + " hour(s) till registration ends!");
							if (Config.TVT_ANNOUNCE_REGISTRATION_LOC_NPC)
								AnnounceToPlayers(true, "TvT: Registration Period is Active in " + _joiningLocationName + " ! at Npc " + npcManager);
						}
						else if (_started)
							AnnounceToPlayers(false, "TvT: " + seconds / 60 / 60 + " hour(s) till event ends!");
						
						break;
					case 1800: // 30 minutes left
					case 900: // 15 minutes left
					case 600: // 10 minutes left
					case 300: // 5 minutes left
					case 60: // 1 minute left
						if (_joining)
						{
							removeOfflinePlayers();
							AnnounceToPlayers(true, "TvT: Joinable in " + _joiningLocationName + "!");
							AnnounceToPlayers(true, "TvT: " + seconds / 60 + " minute(s) till registration ends!");
							if (Config.TVT_ANNOUNCE_REGISTRATION_LOC_NPC)
								Announcements.getInstance().gameAnnounceToAll("TvT: Registration Period is Active in " + _joiningLocationName + " ! at Npc " + npcManager);
						}
						else if (_started)
							AnnounceToPlayers(false, "TvT: " + seconds / 60 + " minute(s) till event ends!");
						
						break;
					case 30: // 30 seconds left
					case 10: // 10 seconds left
					case 3: // 3 seconds left
					case 2: // 2 seconds left
					case 1: // 1 seconds left
						if (_joining)
							AnnounceToPlayers(true, "TvT: " + seconds + " second(s) till registration ends!");
						else if (_teleport)
							AnnounceToPlayers(false, "TvT: " + seconds + " seconds(s) till fight starts!");
						else if (_started)
							AnnounceToPlayers(false, "TvT: " + seconds + " second(s) till event ends!");
						
						break;
				}
			}
			
			long startOneSecondWaiterStartTime = System.currentTimeMillis();
			
			// only the try catch with Thread.sleep(1000) give bad countdown on high wait times
			while (startOneSecondWaiterStartTime + 1000 > System.currentTimeMillis())
			{
				try
				{
					Thread.sleep(1);
				}
				catch (InterruptedException ie)
				{
					
				}
			}
		}
	}
	
	private static boolean startEventOk()
	{
		if (_joining || !_teleport || _started)
			return false;
		
		if (Config.TVT_EVEN_TEAMS.equals("NO") || Config.TVT_EVEN_TEAMS.equals("BALANCE"))
		{
			if (_teamPlayersCount.contains(0))
				return false;
		}
		else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE"))
		{
			List<L2PcInstance> playersShuffleTemp = new ArrayList<>();
			int loopCount = 0;
			
			loopCount = _playersShuffle.size();
			
			for (int i = 0; i < loopCount; i++)
			{
				if (_playersShuffle != null)
					playersShuffleTemp.add(_playersShuffle.get(i));
			}
			
			_playersShuffle = playersShuffleTemp;
			playersShuffleTemp.clear();
		}
		
		return true;
	}
	
	public static void shuffleTeams()
	{
		int teamCount = 0, playersCount = 0;
		
		for (;;)
		{
			if (_playersShuffle.isEmpty())
				break;
			
			int playerToAddIndex = Rnd.nextInt(_playersShuffle.size());
			L2PcInstance player = null;
			player = _playersShuffle.get(playerToAddIndex);
			player._originalNameColorTvT = player.getAppearance().getNameColor();
			player._originalTitleTvT = player.getTitle();
			player._originalKarmaTvT = player.getKarma();
			
			_players.add(player);
			_players.get(playersCount)._teamNameTvT = _teams.get(teamCount);
			_savePlayers.add(_players.get(playersCount).getName());
			_savePlayerTeams.add(_teams.get(teamCount));
			playersCount++;
			
			if (teamCount == _teams.size() - 1)
				teamCount = 0;
			else
				teamCount++;
			
			_playersShuffle.remove(playerToAddIndex);
		}
	}
	
	public static void setUserData()
	{
		for (L2PcInstance player : _players)
		{
			player.getAppearance().setNameColor(_teamColors.get(_teams.indexOf(player._teamNameTvT)));
			player.setKarma(0);
			
			if (Config.TVT_AURA)
			{
				if (_teams.size() >= 2)
				{
					final int teamId = _teams.indexOf(player._teamNameTvT) + 1;
					switch (teamId)
					{
						case 0:
						{
							player.setTeam(Team.NONE);
							break;
						}
						case 1:
						{
							player.setTeam(Team.BLUE);
							break;
						}
						case 2:
						{
							player.setTeam(Team.RED);
							break;
						}
					}
				}
			}
			else
				player.setTeam(Team.NONE);
			
			player.broadcastUserInfo();
		}
	}
	
	public static void finishEvent()
	{
		if (!finishEventOk())
		{
			if (Config.DEBUG)
				_log.log(Level.FINER, "TvT Engine[finishEvent]: finishEventOk() = false");
			return;
		}
		
		_started = false;
		unspawnEventNpc();
		processTopTeam();
		
		if (_topKills == 0)
			AnnounceToPlayers(true, _eventName + "No team wins the match(nobody killed).");
		else
		{
			AnnounceToPlayers(true, _eventName + " " + _topTeam + "'s win the match! " + _topKills + " kills.");
			rewardTeam(_topTeam);
			playKneelAnimation(_topTeam);
		}
		
		if (Config.TVT_ANNOUNCE_TEAM_STATS)
		{
			AnnounceToPlayers(true, _eventName + " Team Statistics:");
			for (String team : _teams)
			{
				int _kills = teamKillsCount(team);
				AnnounceToPlayers(true, "Team: " + team + " - Kills: " + _kills);
			}
		}
		
		teleportFinish();
	}
	
	// show losers and winners animations
	public static void playKneelAnimation(String teamName)
	{
		for (L2PcInstance player : _players)
		{
			if (player != null)
			{
				if (!player._teamNameTvT.equals(teamName))
					player.broadcastSocialActionInRadius(7);
				else if (player._teamNameTvT.equals(teamName))
					player.broadcastSocialActionInRadius(3);
			}
		}
	}
	
	private static boolean finishEventOk()
	{
		if (!_started)
			return false;
		
		_inProgress = false;
		if (Config.TVT_CLOSE_COLISEUM_DOORS)
			openColiseumDoors();
		
		return true;
	}
	
	public static void processTopTeam()
	{
		for (String team : _teams)
		{
			if (teamKillsCount(team) > _topKills)
			{
				_topTeam = team;
				_topKills = teamKillsCount(team);
			}
		}
	}
	
	public static void rewardTeam(String teamName)
	{
		for (L2PcInstance player : _players)
		{
			if (player != null && player.isOnline() != 0 && player._inEventTvT)
			{
				if (player._teamNameTvT.equals(teamName) && (player._countTvTkills > 0 || Config.TVT_PRICE_NO_KILLS))
				{
					player.addItem("TvT: " + _eventName, _rewardId, _rewardAmount, player, true);
					
					_playerWon = 1;
					
					NpcHtmlMessage nhm = new NpcHtmlMessage(5);
					StringBuilder replyMSG = new StringBuilder("");
					
					replyMSG.append("<html><body>Your team wins the event. Look in your inventory for the reward.</body></html>");
					
					nhm.setHtml(replyMSG.toString());
					player.sendPacket(nhm);
					
					// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
			}
		}
	}
	
	public static void abortEvent()
	{
		if (!_joining && !_teleport && !_started)
			return;
		if (_joining && !_teleport && !_started)
		{
			unspawnEventNpc();
			cleanTvT();
			_joining = false;
			AnnounceToPlayers(true, _eventName + "Match aborted!");
			return;
		}
		_joining = false;
		_teleport = false;
		_started = false;
		_inProgress = false;
		unspawnEventNpc();
		AnnounceToPlayers(true, _eventName + "Match aborted!");
		teleportFinish();
	}
	
	public static void sit()
	{
		_sitForced = !_sitForced;
		
		for (L2PcInstance player : _players)
		{
			if (player != null)
			{
				if (_sitForced)
				{
					player.stopMove(null);
					player.abortAttack();
					player.abortCast();
					
					if (!player.isSitting())
						player.sitDown();
				}
				else
				{
					if (player.isSitting())
						player.standUp();
				}
			}
		}
	}
	
	public static void dumpData()
	{
		_log.info("");
		_log.info("");
		
		if (!_joining && !_teleport && !_started)
		{
			_log.info("<<---------------------------------->>");
			_log.info(">> TvT Engine infos dump (INACTIVE) <<");
			_log.info("<<--^----^^-----^----^^------^^----->>");
		}
		else if (_joining && !_teleport && !_started)
		{
			_log.info("<<--------------------------------->>");
			_log.info(">> TvT Engine infos dump (JOINING) <<");
			_log.info("<<--^----^^-----^----^^------^----->>");
		}
		else if (!_joining && _teleport && !_started)
		{
			_log.info("<<---------------------------------->>");
			_log.info(">> TvT Engine infos dump (TELEPORT) <<");
			_log.info("<<--^----^^-----^----^^------^^----->>");
		}
		else if (!_joining && !_teleport && _started)
		{
			_log.info("<<--------------------------------->>");
			_log.info(">> TvT Engine infos dump (STARTED) <<");
			_log.info("<<--^----^^-----^----^^------^----->>");
		}
		
		_log.info("Name: " + _eventName);
		_log.info("Desc: " + _eventDesc);
		_log.info("Join location: " + _joiningLocationName);
		_log.info("Min lvl: " + _minlvl);
		_log.info("Max lvl: " + _maxlvl);
		_log.info("");
		_log.info("##########################");
		_log.info("# _teams(Vector<String>) #");
		_log.info("##########################");
		
		for (String team : _teams)
			_log.info(team + " Kills Done :" + _teamKillsCount.get(_teams.indexOf(team)));
		
		if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE"))
		{
			_log.info("");
			_log.info("#########################################");
			_log.info("# _playersShuffle(Vector<L2PcInstance>) #");
			_log.info("#########################################");
			
			for (L2PcInstance player : _playersShuffle)
			{
				if (player != null)
					_log.info("Name: " + player.getName());
			}
		}
		
		_log.info("");
		_log.info("##################################");
		_log.info("# _players(Vector<L2PcInstance>) #");
		_log.info("##################################");
		
		for (L2PcInstance player : _players)
		{
			if (player != null)
				_log.info("Name: " + player.getName() + "   Team: " + player._teamNameTvT + "  Kills Done:" + player._countTvTkills);
		}
		
		_log.info("");
		_log.info("#####################################################################");
		_log.info("# _savePlayers(Vector<String>) and _savePlayerTeams(Vector<String>) #");
		_log.info("#####################################################################");
		
		for (String player : _savePlayers)
			_log.info("Name: " + player + "	Team: " + _savePlayerTeams.get(_savePlayers.indexOf(player)));
		
		_log.info("");
		_log.info("");
	}
	
	public static void loadData()
	{
		_eventName = "";
		_eventDesc = "";
		_topTeam = "";
		_joiningLocationName = "";
		_teams = new ArrayList<>();
		_savePlayers = new ArrayList<>();
		_savePlayerTeams = new ArrayList<>();
		_players = new ArrayList<>();
		_playersShuffle = new ArrayList<>();
		_teamPlayersCount = new ArrayList<>();
		_teamKillsCount = new ArrayList<>();
		_teamColors = new ArrayList<>();
		_teamsX = new ArrayList<>();
		_teamsY = new ArrayList<>();
		_teamsZ = new ArrayList<>();
		_joining = false;
		_teleport = false;
		_started = false;
		_sitForced = false;
		_npcId = 0;
		_npcX = 0;
		_npcY = 0;
		_npcZ = 0;
		_npcHeading = 0;
		_rewardId = 0;
		_rewardAmount = 0;
		_topKills = 0;
		_minlvl = 0;
		_maxlvl = 0;
		_joinTime = 0;
		_eventTime = 0;
		_minPlayers = 0;
		_maxPlayers = 0;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement;
			ResultSet rs;
			statement = con.prepareStatement("SELECT * FROM tvt");
			rs = statement.executeQuery();
			
			int teams = 0;
			
			while (rs.next())
			{
				_eventName = rs.getString("eventName");
				_eventDesc = rs.getString("eventDesc");
				_joiningLocationName = rs.getString("joiningLocation");
				_minlvl = rs.getInt("minlvl");
				_maxlvl = rs.getInt("maxlvl");
				_npcId = rs.getInt("npcId");
				_npcX = rs.getInt("npcX");
				_npcY = rs.getInt("npcY");
				_npcZ = rs.getInt("npcZ");
				_npcHeading = rs.getInt("npcHeading");
				_rewardId = rs.getInt("rewardId");
				_rewardAmount = rs.getInt("rewardAmount");
				teams = rs.getInt("teamsCount");
				_joinTime = rs.getInt("joinTime");
				_eventTime = rs.getInt("eventTime");
				_minPlayers = rs.getInt("minPlayers");
				_maxPlayers = rs.getInt("maxPlayers");
			}
			rs.close();
			statement.close();
			
			int index = -1;
			if (teams > 0)
				index = 0;
			while (index < teams && index > -1)
			{
				statement = con.prepareStatement("SELECT * FROM tvt_teams WHERE teamId=?");
				statement.setInt(1, index);
				rs = statement.executeQuery();
				while (rs.next())
				{
					_teams.add(rs.getString("teamName"));
					_teamPlayersCount.add(0);
					_teamKillsCount.add(0);
					_teamColors.add(0);
					_teamsX.add(0);
					_teamsY.add(0);
					_teamsZ.add(0);
					_teamsX.set(index, rs.getInt("teamX"));
					_teamsY.set(index, rs.getInt("teamY"));
					_teamsZ.set(index, rs.getInt("teamZ"));
					_teamColors.set(index, rs.getInt("teamColor"));
				}
				index++;
				rs.close();
				statement.close();
			}
		}
		catch (Exception e)
		{
			_log.warning(TvT.class.getSimpleName() + ":  Exception: TvT.loadData(): ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public static void saveData()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement;
			
			statement = con.prepareStatement("DELETE FROM tvt");
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("INSERT INTO tvt (eventName, eventDesc, joiningLocation, minlvl, maxlvl, npcId, npcX, npcY, npcZ, npcHeading, rewardId, rewardAmount, teamsCount, joinTime, eventTime, minPlayers, maxPlayers) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			statement.setString(1, _eventName);
			statement.setString(2, _eventDesc);
			statement.setString(3, _joiningLocationName);
			statement.setInt(4, _minlvl);
			statement.setInt(5, _maxlvl);
			statement.setInt(6, _npcId);
			statement.setInt(7, _npcX);
			statement.setInt(8, _npcY);
			statement.setInt(9, _npcZ);
			statement.setInt(10, _npcHeading);
			statement.setInt(11, _rewardId);
			statement.setInt(12, _rewardAmount);
			statement.setInt(13, _teams.size());
			statement.setInt(14, _joinTime);
			statement.setInt(15, _eventTime);
			statement.setInt(16, _minPlayers);
			statement.setInt(17, _maxPlayers);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM tvt_teams");
			statement.execute();
			statement.close();
			
			for (String teamName : _teams)
			{
				int index = _teams.indexOf(teamName);
				
				if (index == -1)
					return;
				statement = con.prepareStatement("INSERT INTO tvt_teams (teamId ,teamName, teamX, teamY, teamZ, teamColor) VALUES (?, ?, ?, ?, ?, ?)");
				statement.setInt(1, index);
				statement.setString(2, teamName);
				statement.setInt(3, _teamsX.get(index));
				statement.setInt(4, _teamsY.get(index));
				statement.setInt(5, _teamsZ.get(index));
				statement.setInt(6, _teamColors.get(index));
				statement.execute();
				statement.close();
			}
		}
		catch (Exception e)
		{
			_log.warning(TvT.class.getSimpleName() + ":  Exception: TvT.saveData(): ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public static void showEventHtml(L2PcInstance eventPlayer, String objectId)
	{
		try
		{
			NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			
			StringBuilder replyMSG = new StringBuilder("<html><body>");
			replyMSG.append("TvT Match<br><br><br>");
			replyMSG.append("Current event...<br1>");
			replyMSG.append("    ... name:&nbsp;<font color=\"00FF00\">" + _eventName + "</font><br1>");
			replyMSG.append("    ... description:&nbsp;<font color=\"00FF00\">" + _eventDesc + "</font><br>");
			if (Config.TVT_ANNOUNCE_REWARD)
				replyMSG.append("    ... reward: (" + _rewardAmount + ") " + ItemTable.getInstance().getTemplate(_rewardId).getItemName() + "<br>");
			
			if (!_started && !_joining)
				replyMSG.append("<center>Wait till the admin/gm start the participation.</center>");
			else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE") && !checkMaxPlayers(_playersShuffle.size()))
			{
				if (!TvT._started)
					replyMSG.append("<font color=\"FFFF00\">The event has reached its maximum capacity.</font><br>Keep checking, someone may crit and you can steal their spot.");
			}
			else if (eventPlayer.isCursedWeaponEquiped() && !Config.TVT_JOIN_CURSED)
				replyMSG.append("<font color=\"FFFF00\">You can't participate in this event with a cursed Weapon.</font><br>");
			else if (!_started && _joining && eventPlayer.getLevel() >= _minlvl && eventPlayer.getLevel() <= _maxlvl)
			{
				if (_players.contains(eventPlayer) || checkShufflePlayers(eventPlayer))
				{
					if (Config.TVT_EVEN_TEAMS.equals("NO") || Config.TVT_EVEN_TEAMS.equals("BALANCE"))
						replyMSG.append("You are already participating in team <font color=\"LEVEL\">" + eventPlayer._teamNameTvT + "</font><br><br>");
					else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE"))
						replyMSG.append("You are already participating!<br><br>");
					
					replyMSG.append("<table border=\"0\"><tr>");
					replyMSG.append("<td width=\"200\">Wait till event start or</td>");
					replyMSG.append("<td width=\"60\"><center><button value=\"remove\" action=\"bypass -h npc_" + objectId + "_tvt_player_leave\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></td>");
					replyMSG.append("<td width=\"100\">your participation!</td>");
					replyMSG.append("</tr></table>");
				}
				else
				{
					replyMSG.append("You want to participate in the event?<br><br>");
					replyMSG.append("<td width=\"200\">Your level : <font color=\"00FF00\">" + eventPlayer.getLevel() + "</font></td><br>");
					replyMSG.append("<td width=\"200\">Min level : <font color=\"00FF00\">" + _minlvl + "</font></td><br>");
					replyMSG.append("<td width=\"200\">Max level : <font color=\"00FF00\">" + _maxlvl + "</font></td><br><br>");
					
					if (Config.TVT_EVEN_TEAMS.equals("NO") || Config.TVT_EVEN_TEAMS.equals("BALANCE"))
					{
						replyMSG.append("<center><table border=\"0\">");
						
						for (String team : _teams)
						{
							replyMSG.append("<tr><td width=\"100\"><font color=\"LEVEL\">" + team + "</font>&nbsp;(" + teamPlayersCount(team) + " joined)</td>");
							replyMSG.append("<td width=\"60\"><button value=\"Join\" action=\"bypass -h npc_" + objectId + "_tvt_player_join " + team + "\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
						}
						
						replyMSG.append("</table></center>");
					}
					else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE"))
					{
						replyMSG.append("<center><table border=\"0\">");
						
						for (String team : _teams)
							replyMSG.append("<tr><td width=\"100\"><font color=\"LEVEL\">" + team + "</font></td>");
						
						replyMSG.append("</table></center><br>");
						
						replyMSG.append("<button value=\"Join\" action=\"bypass -h npc_" + objectId + "_tvt_player_join eventShuffle\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
						replyMSG.append("Teams will be randomly generated!");
					}
				}
			}
			else if (_started && !_joining)
				replyMSG.append("<center>TvT match is in progress.</center>");
			else if (eventPlayer.getLevel() < _minlvl || eventPlayer.getLevel() >= _maxlvl)
			{
				replyMSG.append("Your level : <font color=\"00FF00\">" + eventPlayer.getLevel() + "</font><br>");
				replyMSG.append("Min level : <font color=\"00FF00\">" + _minlvl + "</font><br>");
				replyMSG.append("Max level : <font color=\"00FF00\">" + _maxlvl + "</font><br><br>");
				replyMSG.append("<font color=\"FFFF00\">You can't participate in this event.</font><br>");
			}
			// Show how many players joined & how many are still needed to join
			replyMSG.append("<br>There are " + _playersShuffle.size() + " player(s) participating in this event.<br>");
			if (_joining)
			{
				if (_playersShuffle.size() < _minPlayers)
				{
					int playersNeeded = _minPlayers - _playersShuffle.size();
					replyMSG.append("The event will not start unless " + playersNeeded + " more player(s) joins!");
				}
			}
			replyMSG.append("</body></html>");
			adminReply.setHtml(replyMSG.toString());
			eventPlayer.sendPacket(adminReply);
			
			// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
			eventPlayer.sendPacket(ActionFailed.STATIC_PACKET);
		}
		catch (Exception e)
		{
			_log.warning(TvT.class.getSimpleName() + ":  TvT Engine[showEventHtlm(" + eventPlayer.getName() + ", " + objectId + ")]: exception");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public static void addPlayer(L2PcInstance player, String teamName)
	{
		if (!addPlayerOk(teamName, player))
			return;
		
		if (Config.TVT_EVEN_TEAMS.equals("NO") || Config.TVT_EVEN_TEAMS.equals("BALANCE"))
		{
			player._teamNameTvT = teamName;
			_players.add(player);
			setTeamPlayersCount(teamName, teamPlayersCount(teamName) + 1);
		}
		else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE"))
			_playersShuffle.add(player);
		
		player._inEventTvT = true;
		player._countTvTkills = 0;
		
		// notify player that he signed up in event
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile("data/html/mods/Participation.htm");
		player.sendPacket(html);
		
		if (Config.TVT_ANNOUNCE_SIGNUPS)
			Announcements.getInstance().gameAnnounceToAll("Player " + player.getName() + " has Signed Up in TvT!");
	}
	
	public synchronized static void removeOfflinePlayers()
	{
		try
		{
			if (_playersShuffle == null || _playersShuffle.isEmpty())
				return;
			for (L2PcInstance player : _playersShuffle)
			{
				if (player == null)
					_playersShuffle.remove(player);
				else if (player.isOnline() == 0 || player.isInJail())
					removePlayer(player);
				if (_playersShuffle.size() == 0 || _playersShuffle.isEmpty())
					break;
			}
		}
		catch (Exception e)
		{
			_log.warning(TvT.class.getSimpleName() + ": TVT unknown error");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public static boolean checkShufflePlayers(L2PcInstance eventPlayer)
	{
		try
		{
			for (L2PcInstance player : _playersShuffle)
			{
				if (player == null || player.isOnline() == 0)
				{
					_playersShuffle.remove(player);
					eventPlayer._inEventTvT = false;
					continue;
				}
				else if (player.getObjectId() == eventPlayer.getObjectId())
				{
					eventPlayer._inEventTvT = true;
					eventPlayer._countTvTkills = 0;
					return true;
				}
				// this 1 is incase player got new objectid after DC or reconnect
				else if (player.getName().equals(eventPlayer.getName()))
				{
					_playersShuffle.remove(player);
					_playersShuffle.add(eventPlayer);
					eventPlayer._inEventTvT = true;
					eventPlayer._countTvTkills = 0;
					return true;
				}
			}
		}
		catch (Exception e)
		{
		}
		return false;
	}
	
	public static boolean addPlayerOk(String teamName, L2PcInstance eventPlayer)
	{
		try
		{
			if (OlympiadManager.getInstance().isRegisteredInComp(eventPlayer) || eventPlayer.isInOlympiadMode() || eventPlayer.getOlympiadGameId() > 0)
			{
				eventPlayer.sendMessage("You can't register while you are in olympiad!");
				return false;
			}
			if (checkShufflePlayers(eventPlayer) || eventPlayer._inEventTvT)
			{
				eventPlayer.sendMessage("You are already participating in the event!");
				return false;
			}
			if (eventPlayer._inEventCTF || eventPlayer._inEventDM || eventPlayer._inEventVIP)
			{
				eventPlayer.sendMessage("You are already participating in another event!");
				return false;
			}
			
			for (L2PcInstance player : _players)
			{
				if (player.getObjectId() == eventPlayer.getObjectId())
				{
					eventPlayer.sendMessage("You are already participating in the event!");
					return false;
				}
				else if (player.getName() == eventPlayer.getName())
				{
					eventPlayer.sendMessage("You are already participating in the event!");
					return false;
				}
			}
			if (_players.contains(eventPlayer))
			{
				eventPlayer.sendMessage("You are already participating in the event!");
				return false;
			}
			if (CTF._savePlayers.contains(eventPlayer.getName()))
			{
				eventPlayer.sendMessage("You are already participating in another event!");
				return false;
			}
		}
		catch (Exception e)
		{
			_log.warning(TvT.class.getSimpleName() + ":  TvT Engine exception: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
		
		if (Config.TVT_EVEN_TEAMS.equals("NO"))
			return true;
		else if (Config.TVT_EVEN_TEAMS.equals("BALANCE"))
		{
			boolean allTeamsEqual = true;
			int countBefore = -1;
			
			for (int playersCount : _teamPlayersCount)
			{
				if (countBefore == -1)
					countBefore = playersCount;
				
				if (countBefore != playersCount)
				{
					allTeamsEqual = false;
					break;
				}
				
				countBefore = playersCount;
			}
			
			if (allTeamsEqual)
				return true;
			
			countBefore = Integer.MAX_VALUE;
			
			for (int teamPlayerCount : _teamPlayersCount)
			{
				if (teamPlayerCount < countBefore)
					countBefore = teamPlayerCount;
			}
			
			List<String> joinableTeams = new ArrayList<>();
			
			for (String team : _teams)
			{
				if (teamPlayersCount(team) == countBefore)
					joinableTeams.add(team);
			}
			
			if (joinableTeams.contains(teamName))
				return true;
		}
		else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE"))
			return true;
		
		eventPlayer.sendMessage("Too many players in team \"" + teamName + "\"");
		return false;
	}
	
	public static synchronized void addDisconnectedPlayer(L2PcInstance player)
	{
		if ((Config.TVT_EVEN_TEAMS.equals("SHUFFLE") && (_teleport || _started)) || (Config.TVT_EVEN_TEAMS.equals("NO") || Config.TVT_EVEN_TEAMS.equals("BALANCE") && (_teleport || _started)))
		{
			if (Config.TVT_ON_START_REMOVE_ALL_EFFECTS)
			{
				for (L2Effect e : player.getAllEffects())
				{
					if (e != null)
						e.exit();
				}
			}
			
			player._teamNameTvT = _savePlayerTeams.get(_savePlayers.indexOf(player.getName()));
			for (L2PcInstance p : _players)
			{
				if (p == null)
					continue;
				// check by name incase player got new objectId
				else if (p.getName().equals(player.getName()))
				{
					player._originalNameColorTvT = player.getAppearance().getNameColor();
					player._originalTitleTvT = player.getTitle();
					player._originalKarmaTvT = player.getKarma();
					player._inEventTvT = true;
					player._countTvTkills = p._countTvTkills;
					_players.remove(p); // removing old object id from vector
					_players.add(player); // adding new objectId to vector
					break;
				}
			}
			
			player.getAppearance().setNameColor(_teamColors.get(_teams.indexOf(player._teamNameTvT)));
			player.setKarma(0);
			if (Config.TVT_AURA)
			{
				if (_teams.size() >= 2)
				{
					final int teamId = _teams.indexOf(player._teamNameTvT) + 1;
					switch (teamId)
					{
						case 0:
						{
							player.setTeam(Team.NONE);
							break;
						}
						case 1:
						{
							player.setTeam(Team.BLUE);
							break;
						}
						case 2:
						{
							player.setTeam(Team.RED);
							break;
						}
					}
				}
			}
			else
				player.setTeam(Team.NONE);
			
			player.broadcastUserInfo();
			player.teleToLocation(_teamsX.get(_teams.indexOf(player._teamNameTvT)), _teamsY.get(_teams.indexOf(player._teamNameTvT)), _teamsZ.get(_teams.indexOf(player._teamNameTvT)));
		}
	}
	
	public static void removePlayer(L2PcInstance player)
	{
		if (player._inEventTvT)
		{
			if (!_joining)
			{
				player.getAppearance().setNameColor(player._originalNameColorTvT);
				player.setTitle(player._originalTitleTvT);
				player.setKarma(player._originalKarmaTvT);
				if (Config.TVT_AURA)
				{
					if (_teams.size() >= 2)
						player.setTeam(Team.NONE);// clear aura :P
				}
				player.broadcastUserInfo();
			}
			player._teamNameTvT = "";
			player._countTvTkills = 0;
			player._inEventTvT = false;
			
			if ((Config.TVT_EVEN_TEAMS.equals("NO") || Config.TVT_EVEN_TEAMS.equals("BALANCE")) && _players.contains(player))
			{
				setTeamPlayersCount(player._teamNameTvT, teamPlayersCount(player._teamNameTvT) - 1);
				_players.remove(player);
			}
			else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE") && (!_playersShuffle.isEmpty() && _playersShuffle.contains(player)))
				_playersShuffle.remove(player);
		}
	}
	
	public static void cleanTvT()
	{
		_log.info("TvT: Cleaning players.");
		for (L2PcInstance player : _players)
		{
			if (player != null)
			{
				removePlayer(player);
				if (_savePlayers.contains(player.getName()))
					_savePlayers.remove(player.getName());
				player._inEventTvT = false;
			}
		}
		if (_playersShuffle != null && !_playersShuffle.isEmpty())
		{
			for (L2PcInstance player : _playersShuffle)
			{
				if (player != null)
					player._inEventTvT = false;
			}
		}
		_log.info("TvT: Cleaning teams.");
		for (String team : _teams)
		{
			int index = _teams.indexOf(team);
			
			_teamPlayersCount.set(index, 0);
			_teamKillsCount.set(index, 0);
		}
		
		_topKills = 0;
		_topTeam = "";
		_players.clear();
		_playersShuffle.clear();
		_savePlayers.clear();
		_savePlayerTeams.clear();
		_log.info("Cleaning TvT done.");
	}
	
	public static void unspawnEventNpc()
	{
		if (_npcSpawn == null)
			return;
		
		_npcSpawn.getLastSpawn().deleteMe();
		_npcSpawn.stopRespawn();
		SpawnTable.getInstance().deleteSpawn(_npcSpawn, true);
	}
	
	public static void teleportFinish()
	{
		AnnounceToPlayers(false, _eventName + "Teleport back to participation NPC in 20 seconds!");
		
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				for (L2PcInstance player : _players)
				{
					if (player != null)
					{
						if (player.isOnline() != 0)
							player.teleToLocation(_npcX, _npcY, _npcZ, false);
						else
						{
							try (Connection con = L2DatabaseFactory.getInstance().getConnection())
							{
								PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=? WHERE char_name=?");
								statement.setInt(1, _npcX);
								statement.setInt(2, _npcY);
								statement.setInt(3, _npcZ);
								statement.setString(4, player.getName());
								statement.execute();
								statement.close();
							}
							catch (SQLException se)
							{
								_log.warning(TvT.class.getSimpleName() + ": " + se);
							}
						}
					}
				}
				_log.info("TvT: Teleport done.");
				cleanTvT();
			}
		}, 20000);
	}
	
	public static int teamKillsCount(String teamName)
	{
		int index = _teams.indexOf(teamName);
		
		if (index == -1)
			return -1;
		
		return _teamKillsCount.get(index);
	}
	
	public static void setTeamKillsCount(String teamName, int teamKillsCount)
	{
		int index = _teams.indexOf(teamName);
		
		if (index == -1)
			return;
		
		_teamKillsCount.set(index, teamKillsCount);
	}
	
	public static int teamPlayersCount(String teamName)
	{
		int index = _teams.indexOf(teamName);
		
		if (index == -1)
			return -1;
		
		return _teamPlayersCount.get(index);
	}
	
	public static void setTeamPlayersCount(String teamName, int teamPlayersCount)
	{
		int index = _teams.indexOf(teamName);
		
		if (index == -1)
			return;
		
		_teamPlayersCount.set(index, teamPlayersCount);
	}
	
	public static long _intervalBetweenMatchs = 0;
	
	public static int _eventType = 0;
	
	private static boolean _inProgress = false;
	private static boolean _finished = false;
	private static boolean _aborted = false;
	
	private static void closeColiseumDoors()
	{
		Announcements.getInstance().gameAnnounceToAll("Closing Coliseum Doors, TvT event has just started !");
		DoorData.getInstance().getDoor(24190001).closeMe();// west gate out
		DoorData.getInstance().getDoor(24190002).closeMe();// west gate in
		DoorData.getInstance().getDoor(24190003).closeMe();// east gate out
		DoorData.getInstance().getDoor(24190004).closeMe();// east gate in
		
		try
		{
			// just to give a lil delay :P
			Thread.sleep(20);
		}
		catch (InterruptedException ie)
		{
			_log.warning(TvT.class.getSimpleName() + ": Error, " + ie.getMessage());
			if (Config.DEVELOPER)
			{
				ie.printStackTrace();
			}
		}
	}
	
	private static void openColiseumDoors()
	{
		Announcements.getInstance().gameAnnounceToAll("Opening Coliseum Doors, TvT event has finished!");
		DoorData.getInstance().getDoor(24190001).openMe();
		DoorData.getInstance().getDoor(24190002).openMe();
		DoorData.getInstance().getDoor(24190003).openMe();
		DoorData.getInstance().getDoor(24190004).openMe();
		
	}
	
	public static String getEventTypeByName(int value)
	{
		String type = String.valueOf(value);
		
		switch (value)
		{
			case 0:
				type = ("None");
				break;
			
			case 1:
				type = ("Manual");
				break;
			
			case 2:
				type = ("Automatic");
				break;
		}
		return type;
	}
	
	public static void sendFinalMessages()
	{
		if (_finished && !_aborted)
			Announcements.getInstance().gameAnnounceToAll("TvT: Thank you For Participating At, " + "TVT Event.");
	}
}