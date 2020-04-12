package com.l2jhellas.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.Announcements;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.datatables.sql.ClanTable;
import com.l2jhellas.gameserver.datatables.sql.NpcData;
import com.l2jhellas.gameserver.datatables.xml.MapRegionTable;
import com.l2jhellas.gameserver.enums.sound.Sound;
import com.l2jhellas.gameserver.idfactory.IdFactory;
import com.l2jhellas.gameserver.instancemanager.MercTicketManager;
import com.l2jhellas.gameserver.instancemanager.SiegeGuardManager;
import com.l2jhellas.gameserver.instancemanager.SiegeManager;
import com.l2jhellas.gameserver.instancemanager.SiegeManager.SiegeSpawn;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.L2ClanMember;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2SiegeClan;
import com.l2jhellas.gameserver.model.L2SiegeClan.SiegeClanType;
import com.l2jhellas.gameserver.model.L2Spawn;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2ArtefactInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2ControlTowerInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SiegeInfo;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.network.serverpackets.UserInfo;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;
import com.l2jhellas.util.Broadcast;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class Siege
{
	private final SimpleDateFormat fmt = new SimpleDateFormat("H:mm.");
	protected static final Logger _log = Logger.getLogger(Siege.class.getName());
	
	public static enum TeleportWhoType
	{
		All,
		Attacker,
		DefenderNotOwner,
		Owner,
		Spectator
	}
		
	public class ScheduleEndSiegeTask implements Runnable
	{
		private final Castle _castleInst;
		
		public ScheduleEndSiegeTask(Castle pCastle)
		{
			_castleInst = pCastle;
		}
		
		@Override
		public void run()
		{
			if (!getIsInProgress())
				return;
			
			try
			{
				long timeRemaining = _siegeEndDate.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
				if (timeRemaining > 3600000)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(_castleInst), timeRemaining - 3600000); // Prepare task for 1 hr left.
				}
				else if ((timeRemaining <= 3600000) && (timeRemaining > 600000))
				{
					announceToPlayer(Math.round(timeRemaining / 60000) + " minute(s) until " + getCastle().getName() + " siege conclusion.", true);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(_castleInst), timeRemaining - 600000); // Prepare task for 10 minute left.
				}
				else if ((timeRemaining <= 600000) && (timeRemaining > 300000))
				{
					announceToPlayer(Math.round(timeRemaining / 60000) + " minute(s) until " + getCastle().getName() + " siege conclusion.", true);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(_castleInst), timeRemaining - 300000); // Prepare task for 5 minute left.
				}
				else if ((timeRemaining <= 300000) && (timeRemaining > 10000))
				{
					announceToPlayer(Math.round(timeRemaining / 60000) + " minute(s) until " + getCastle().getName() + " siege conclusion.", true);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(_castleInst), timeRemaining - 10000); // Prepare task for 10 seconds count down
				}
				else if ((timeRemaining <= 10000) && (timeRemaining > 0))
				{
					announceToPlayer(getCastle().getName() + " siege " + Math.round(timeRemaining / 1000) + " second(s) left!", true);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(_castleInst), timeRemaining); // Prepare task for second count down
				}
				else
				{
					_castleInst.getSiege().endSiege();
				}
			}
			catch (Throwable t)
			{
				
			}
		}
	}
	
	public class ScheduleStartSiegeTask implements Runnable
	{
		private final Castle _castleInst;
		
		public ScheduleStartSiegeTask(Castle pCastle)
		{
			_castleInst = pCastle;
		}
		
		@Override
		public void run()
		{
			if (getIsInProgress())
				return;
			
			try
			{
				long timeRemaining = getSiegeDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
				if (timeRemaining > 86400000)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_castleInst), timeRemaining - 86400000); // Prepare task for 24 before siege start to
					// end registration
				}
				else if ((timeRemaining <= 86400000) && (timeRemaining > 13600000))
				{
					announceToPlayer("The registration term for " + getCastle().getName() + " has ended.", false);
					_isRegistrationOver = true;
					clearSiegeWaitingClan();
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_castleInst), timeRemaining - 13600000); // Prepare task for 1 hr left before siege
					// start.
				}
				else if ((timeRemaining <= 13600000) && (timeRemaining > 600000))
				{
					announceToPlayer(Math.round(timeRemaining / 60000) + " minute(s) until " + getCastle().getName() + " siege begin.", false);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_castleInst), timeRemaining - 600000); // Prepare task for 10 minute left.
				}
				else if ((timeRemaining <= 600000) && (timeRemaining > 300000))
				{
					announceToPlayer(Math.round(timeRemaining / 60000) + " minute(s) until " + getCastle().getName() + " siege begin.", false);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_castleInst), timeRemaining - 300000); // Prepare task for 5 minute left.
				}
				else if ((timeRemaining <= 300000) && (timeRemaining > 10000))
				{
					announceToPlayer(Math.round(timeRemaining / 60000) + " minute(s) until " + getCastle().getName() + " siege begin.", false);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_castleInst), timeRemaining - 10000); // Prepare task for 10 seconds count down
				}
				else if ((timeRemaining <= 10000) && (timeRemaining > 0))
				{
					announceToPlayer(getCastle().getName() + " siege " + Math.round(timeRemaining / 1000) + " second(s) to start!", false);
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartSiegeTask(_castleInst), timeRemaining); // Prepare task for second count down
				}
				else
				{
					_castleInst.getSiege().startSiege();
				}
			}
			catch (Throwable t)
			{
				
			}
		}
	}
	
	// Attacker and Defender
	private final List<L2SiegeClan> _attackerClans = new ArrayList<>(); // L2SiegeClan
	
	private final List<L2SiegeClan> _defenderClans = new ArrayList<>(); // L2SiegeClan
	private final List<L2SiegeClan> _defenderWaitingClans = new ArrayList<>(); // L2SiegeClan
	private int _defenderRespawnDelayPenalty;
	
	// Castle setting
	private List<L2ArtefactInstance> _artifacts = new ArrayList<>();
	private List<L2ControlTowerInstance> _controlTowers = new ArrayList<>();
	private final Castle _castle;
	private boolean _isInProgress = false;
	private boolean _isNormalSide = true; // true = Atk is Atk, false = Atk is Def
	protected boolean _isRegistrationOver = false;
	protected Calendar _siegeEndDate;
	protected Calendar _siegeRegistrationEndDate;
	
	public Siege(Castle castle)
	{
		_castle = castle;		
		startAutoTask();
	}
	
	public void endSiege()
	{
		if (getIsInProgress())
		{
            Broadcast.toAllOnlinePlayers(Sound.SIEGE_SOUND_END.getPacket());
			announceToPlayer("The siege of " + getCastle().getName() + " has finished!", false);
			_log.info("[SIEGE] The siege of " + getCastle().getName() + " has finished! " + fmt.format(new Date(System.currentTimeMillis())));
			
			if (getCastle().getOwnerId() <= 0)
			{
				announceToPlayer("The siege of " + getCastle().getName() + " has ended in a draw.", false);
				_log.info("[SIEGE] The siege of " + getCastle().getName() + " has ended in a draw. " + fmt.format(new Date(System.currentTimeMillis())));
			}
			// Cleanup clans kills/deaths counters.
			for (L2SiegeClan attackerClan : getAttackerClans())
			{
				final L2Clan clan = ClanTable.getInstance().getClan(attackerClan.getClanId());
				if (clan == null)
				{
					continue;
				}
				
				clan.clearSiegeKills();
				clan.clearSiegeDeaths();
			}
			
			for (L2SiegeClan defenderClan : getDefenderClans())
			{
				final L2Clan clan = ClanTable.getInstance().getClan(defenderClan.getClanId());
				if (clan == null)
				{
					continue;
				}
				
				clan.clearSiegeKills();
				clan.clearSiegeDeaths();
			}
			removeFlags(); // Removes all flags. Note: Remove flag before teleporting players
			teleportPlayer(Siege.TeleportWhoType.Attacker, MapRegionTable.TeleportWhereType.TOWN); // Teleport to the second closest town
			teleportPlayer(Siege.TeleportWhoType.DefenderNotOwner, MapRegionTable.TeleportWhereType.TOWN); // Teleport to the second closest town
			teleportPlayer(Siege.TeleportWhoType.Spectator, MapRegionTable.TeleportWhereType.TOWN); // Teleport to the second closest town
			_isInProgress = false; // Flag so that siege instance can be started
			updatePlayerSiegeStateFlags(true);
			saveCastleSiege(true); // Save castle specific data
			clearSiegeClan(); // Clear siege clan from db
			removeArtifact(); // Remove artifact from this castle
			removeControlTower(); // Remove all control tower from this castle
			SiegeGuardManager.getInstance().unspawnSiegeGuard(getCastle());
			if (getCastle().getOwnerId() > 0)
			    SiegeGuardManager.getInstance().removeMercs(getCastle());

			getCastle().spawnDoor(); // Respawn door to castle
			getCastle().getZone().updateZoneStatusForCharactersInside();
			L2Clan clan = ClanTable.getInstance().getClan(getCastle().getOwnerId());
			if (clan != null)
				for (L2ClanMember member : clan.getMembers())
				{
					if (member != null)
					{
						L2PcInstance player = member.getPlayerInstance();
						if (player != null && player.isNoble())
							Hero.getInstance().setCastleTaken(player.getObjectId(), getCastle().getCastleId());
					}
				}			
		}
	}
	
	private void removeDefender(L2SiegeClan sc)
	{
		if (sc != null)
			getDefenderClans().remove(sc);
	}
	
	private void removeAttacker(L2SiegeClan sc)
	{
		if (sc != null)
			getAttackerClans().remove(sc);
	}
	
	private void addDefender(L2SiegeClan sc, SiegeClanType type)
	{
		if (sc == null)
			return;
		sc.setType(type);
		getDefenderClans().add(sc);
	}
	
	private void addAttacker(L2SiegeClan sc)
	{
		if (sc == null)
			return;
		sc.setType(SiegeClanType.ATTACKER);
		getAttackerClans().add(sc);
	}
	
	public void midVictory()
	{
		if (getIsInProgress()) // Siege still in progress
		{
			if (getCastle().getOwnerId() > 0)
			 SiegeGuardManager.getInstance().removeMercs(getCastle());
			
			if (getDefenderClans().size() == 0 && // If defender doesn't exist (Pc vs Npc)
			getAttackerClans().size() == 1 // Only 1 attacker
			)
			{
				L2SiegeClan sc_newowner = getAttackerClan(getCastle().getOwnerId());
				removeAttacker(sc_newowner);
				addDefender(sc_newowner, SiegeClanType.OWNER);
				endSiege();
				return;
			}
			if (getCastle().getOwnerId() > 0)
			{
				
				int allyId = ClanTable.getInstance().getClan(getCastle().getOwnerId()).getAllyId();
				if (getDefenderClans().size() == 0) // If defender doesn't exist (Pc vs Npc)
				// and only an alliance attacks
				{
					// The player's clan is in an alliance
					if (allyId != 0)
					{
						boolean allinsamealliance = true;
						for (L2SiegeClan sc : getAttackerClans())
						{
							if (sc != null)
							{
								if (ClanTable.getInstance().getClan(sc.getClanId()).getAllyId() != allyId)
									allinsamealliance = false;
							}
						}
						if (allinsamealliance)
						{
							L2SiegeClan sc_newowner = getAttackerClan(getCastle().getOwnerId());
							removeAttacker(sc_newowner);
							addDefender(sc_newowner, SiegeClanType.OWNER);
							endSiege();
							return;
						}
					}
				}
				
				for (L2SiegeClan sc : getDefenderClans())
				{
					if (sc != null)
					{
						removeDefender(sc);
						addAttacker(sc);
					}
				}
				
				L2SiegeClan sc_newowner = getAttackerClan(getCastle().getOwnerId());
				removeAttacker(sc_newowner);
				addDefender(sc_newowner, SiegeClanType.OWNER);
				
				// The player's clan is in an alliance
				if (allyId != 0)
				{
					Collection<L2Clan> clanList = ClanTable.getInstance().getClans();
					
					for (L2Clan clan : clanList)
					{
						if (clan.getAllyId() == allyId)
						{
							L2SiegeClan sc = getAttackerClan(clan.getClanId());
							if (sc != null)
							{
								removeAttacker(sc);
								addDefender(sc, SiegeClanType.DEFENDER);
							}
						}
					}
				}
				teleportPlayer(Siege.TeleportWhoType.Attacker, MapRegionTable.TeleportWhereType.SIEGE_FLAG); // Teleport to the second closest town
				teleportPlayer(Siege.TeleportWhoType.Spectator, MapRegionTable.TeleportWhereType.TOWN); // Teleport to the second closest town
				
				removeDefenderFlags(); // Removes defenders' flags
				getCastle().removeUpgrade(); // Remove all castle upgrade
				getCastle().spawnDoor(true); // Respawn door to castle but make them weaker (50% hp)
				updatePlayerSiegeStateFlags(false);
			}
		}
	}
	
	public void startSiege()
	{
		if (!getIsInProgress())
		{
			if (getAttackerClans().size() <= 0)
			{
				final SystemMessage sm = SystemMessage.getSystemMessage((getCastle().getOwnerId() <= 0) ? SystemMessageId.SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST : SystemMessageId.S1_SIEGE_WAS_CANCELED_BECAUSE_NO_CLANS_PARTICIPATED);
				sm.addString(_castle.getName());			
				Announcements.getInstance().announceToAll(sm);
				saveCastleSiege(true);
				return;
			}
			
			_isNormalSide = true; // Atk is now atk
			_isInProgress = true; // Flag so that same siege instance cannot be started again
			
			loadSiegeClan(); // Load siege clan from db
			updatePlayerSiegeStateFlags(false);
			teleportPlayer(Siege.TeleportWhoType.Attacker, MapRegionTable.TeleportWhereType.TOWN); // Teleport to the closest town
			// teleportPlayer(Siege.TeleportWhoType.Spectator, MapRegionTable.TeleportWhereType.Town); // Teleport to the second closest town
			spawnArtifact(getCastle().getCastleId()); // Spawn artifact
			spawnControlTower(getCastle().getCastleId()); // Spawn control tower
			getCastle().spawnDoor(); // Spawn door
			spawnSiegeGuard(); // Spawn siege guard
			MercTicketManager.getInstance().deleteTickets(getCastle().getCastleId()); // remove the tickets from the ground
			_defenderRespawnDelayPenalty = 0; // Reset respawn delay
			
			getCastle().getZone().updateZoneStatusForCharactersInside();
			
			// Schedule a task to prepare auto siege end
			_siegeEndDate = Calendar.getInstance();
			_siegeEndDate.add(Calendar.MINUTE, SiegeManager.getInstance().getSiegeLength());
			ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndSiegeTask(getCastle()), 1000); // Prepare auto end task
			
            Broadcast.toAllOnlinePlayers(Sound.SIEGE_SOUND_START.getPacket());

			announceToPlayer("The siege of " + getCastle().getName() + " has started!", false);
			_log.info("[SIEGE] The siege of " + getCastle().getName() + " has started! " + fmt.format(new Date(System.currentTimeMillis())));
		}
	}
	
	public void announceToPlayer(String message, boolean inAreaOnly)
	{
		if (inAreaOnly)
		{
			getCastle().getZone().announceToPlayers(message);
			return;
		}
		
		// Get all players
		for (L2PcInstance player : L2World.getInstance().getAllPlayers().values())
		{
			if(player != null)
			   player.sendMessage(message);
		}
	}
	
	public void updatePlayerSiegeStateFlags(boolean clear)
	{
		for (L2SiegeClan siegeclan : getAttackerClans())
		{
			L2Clan clan = ClanTable.getInstance().getClan(siegeclan.getClanId());

			if(clan == null)
				continue;
			
			for (L2PcInstance member : clan.getOnlineMembers())
			{
				if (clear)
				{
					member.setSiegeState((byte) 0);
					member.setIsInSiege(false);
				}
				else
				{
					member.setSiegeState((byte) 1);
					if (checkIfInZone(member))
						member.setIsInSiege(true);
				}
				member.sendPacket(new UserInfo(member));
				member.broadcastRelationChanged();
			}
		}
		
		for (L2SiegeClan siegeclan : getDefenderClans())
		{
			L2Clan clan = ClanTable.getInstance().getClan(siegeclan.getClanId());

			if(clan == null)
				continue;
			
			for (L2PcInstance member : clan.getOnlineMembers())
			{
				if (clear)
				{
					member.setSiegeState((byte) 0);
					member.setIsInSiege(false);
				}
				else
				{
					member.setSiegeState((byte) 2);
					if (checkIfInZone(member))
						member.setIsInSiege(true);
				}
				member.sendPacket(new UserInfo(member));
				member.broadcastRelationChanged();
			}
		}
	}
	
	public void approveSiegeDefenderClan(int clanId)
	{
		if (clanId <= 0)
			return;
		saveSiegeClan(ClanTable.getInstance().getClan(clanId), 0, true);
		loadSiegeClan();
	}
	
	public boolean checkIfInZone(L2Object object)
	{
		return checkIfInZone(object.getX(), object.getY(), object.getZ());
	}
	
	public boolean checkIfInZone(int x, int y, int z)
	{
		return (getIsInProgress() && (getCastle().checkIfInZone(x, y, z))); // Castle zone during siege
	}
	
	public boolean checkIsAttacker(L2Clan clan)
	{
		return (getAttackerClan(clan) != null);
	}
	
	public boolean checkIsDefender(L2Clan clan)
	{
		return (getDefenderClan(clan) != null);
	}
	
	public boolean checkIsDefenderWaiting(L2Clan clan)
	{
		return (getDefenderWaitingClan(clan) != null);
	}
	
	public void clearSiegeClan()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("DELETE FROM siege_clans WHERE castle_id=?");
			statement.setInt(1, getCastle().getCastleId());
			statement.execute();
			statement.close();
			
			if (getCastle().getOwnerId() > 0)
			{
				PreparedStatement statement2 = con.prepareStatement("DELETE FROM siege_clans WHERE clan_id=?");
				statement2.setInt(1, getCastle().getOwnerId());
				statement2.execute();
				statement2.close();
			}
			
			getAttackerClans().clear();
			getDefenderClans().clear();
			getDefenderWaitingClans().clear();
		}
		catch (SQLException e)
		{
			_log.warning(Siege.class.getName() + ": Exception: clearSiegeClan(): ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public void clearSiegeWaitingClan()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("DELETE FROM siege_clans WHERE castle_id=? and type = 2");
			statement.setInt(1, getCastle().getCastleId());
			statement.execute();
			statement.close();
			
			getDefenderWaitingClans().clear();
		}
		catch (SQLException e)
		{
			_log.warning(Siege.class.getName() + ": clearSiegeWaitingClan(): ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}

	public List<L2PcInstance> getAttackersInZone()
	{
		return getAttackerClans().stream().filter(cl -> cl.getType().equals(SiegeClanType.ATTACKER))
		.map(siegeclan -> ClanTable.getInstance().getClan(siegeclan.getClanId()))
		.filter(Objects::nonNull)
		.flatMap(clan -> clan.getOnlineMembers(0).stream())
		.filter(L2PcInstance::isInSiege)
		.collect(Collectors.toList());		
	}

	public List<L2PcInstance> getPlayersInZone()
	{
		return getCastle().getZone().getPlayersInside();
	}

	public List<L2PcInstance> getOwnersInZone()
	{
		return getDefenderClans().stream()
			.filter(siegeclan -> siegeclan.getClanId() == _castle.getOwnerId())
			.map(siegeclan -> ClanTable.getInstance().getClan(siegeclan.getClanId()))
			.filter(Objects::nonNull)
			.flatMap(clan -> clan.getOnlineMembers(0).stream())
			.filter(L2PcInstance::isInSiege)
			.collect(Collectors.toList());
	}
	
	public List<L2PcInstance> getSpectatorsInZone()
	{
		return getCastle().getZone().getPlayersInside().stream().filter(p -> !p.isInSiege()).collect(Collectors.toList());
	}
	
	public void killedCT(L2Npc ct)
	{
		_defenderRespawnDelayPenalty += SiegeManager.getInstance().getControlTowerLosePenalty(); // Add respawn penalty to defenders for each control tower lose
	}
	
	public void killedFlag(L2Npc flag)
	{
		if (flag == null)
			return;
		for (int i = 0; i < getAttackerClans().size(); i++)
		{
			if (getAttackerClan(i).removeFlag(flag))
				return;
		}
	}
	
	public void listRegisterClan(L2PcInstance player)
	{
		player.sendPacket(new SiegeInfo(getCastle()));
	}
	
	public void registerAttacker(L2PcInstance player)
	{
		registerAttacker(player, false);
	}
	
	public void registerAttacker(L2PcInstance player, boolean force)
	{
		
		if (player.getClan() == null)
			return;
		int allyId = 0;
		if (getCastle().getOwnerId() != 0)
			allyId = ClanTable.getInstance().getClan(getCastle().getOwnerId()).getAllyId();
		if (allyId != 0)
		{
			if (player.getClan().getAllyId() == allyId && !force)
			{
				player.sendMessage("You cannot register as an attacker because your alliance owns the castle");
				return;
			}
		}
		if (force || checkIfCanRegister(player))
			saveSiegeClan(player.getClan(), 1, false); // Save to database
	}
	
	public void registerDefender(L2PcInstance player)
	{
		registerDefender(player, false);
	}
	
	public void registerDefender(L2PcInstance player, boolean force)
	{
		if (getCastle().getOwnerId() <= 0)
			player.sendMessage("You cannot register as a defender because " + getCastle().getName() + " is owned by NPC.");
		else if (force || checkIfCanRegister(player))
			saveSiegeClan(player.getClan(), 2, false); // Save to database
	}
	
	public void removeSiegeClan(int clanId)
	{
		if (clanId <= 0)
			return;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("DELETE FROM siege_clans WHERE castle_id=? AND clan_id=?");
			statement.setInt(1, getCastle().getCastleId());
			statement.setInt(2, clanId);
			statement.execute();
			statement.close();
			
			loadSiegeClan();
		}
		catch (Exception e)
		{
			_log.warning(Siege.class.getName() + ": Could not remove from siege clan" + clanId);
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public void removeSiegeClan(L2Clan clan)
	{
		if ((clan == null) || clan.hasCastle() == getCastle().getCastleId() || !SiegeManager.checkIsRegistered(clan, getCastle().getCastleId()))
			return;
		removeSiegeClan(clan.getClanId());
	}
	
	public void removeSiegeClan(L2PcInstance player)
	{
		removeSiegeClan(player.getClan());
	}
	
	public void startAutoTask()
	{
		if (getCastle().getSiegeDate().getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
			saveCastleSiege(false);
		else
		{
			loadSiegeClan();

			// Schedule registration end
			_siegeRegistrationEndDate = Calendar.getInstance();
			_siegeRegistrationEndDate.setTimeInMillis(getCastle().getSiegeDate().getTimeInMillis());
			_siegeRegistrationEndDate.add(Calendar.DAY_OF_MONTH, -1);

			_log.info(Siege.class.getSimpleName() + ": Siege of " + getCastle().getName() + ": " + getCastle().getSiegeDate().getTime());

			// Schedule siege auto start
			ThreadPoolManager.getInstance().scheduleGeneral(new Siege.ScheduleStartSiegeTask(getCastle()), 1000);
		}
	}
	
	public void teleportPlayer(TeleportWhoType teleportWho, MapRegionTable.TeleportWhereType teleportWhere)
	{
		List<L2PcInstance> players;
		switch (teleportWho)
		{
			case Owner:
				players = getOwnersInZone();
				break;
			case Attacker:
				players = getAttackersInZone();
				break;
			case DefenderNotOwner:				
				players = getPlayersInZone();
				final Iterator<L2PcInstance> it = players.iterator();
				while (it.hasNext())
				{
					final L2PcInstance player = it.next();
					if ((player == null) || player.inObserverMode() || ((player.getClanId() > 0) && (player.getClanId() == getCastle().getOwnerId())))
						it.remove();
				}
				break;
			case Spectator:
				players = getSpectatorsInZone();
				break;
			default:
				players = getPlayersInZone();
		}
		
		for (L2Character player : players)
		{
			if (player == null || player.getActingPlayer().isGM() || player.getActingPlayer().isInJail())
				continue;
			player.teleToLocation(teleportWhere);
		}
	}
	
	private void addAttacker(int clanId)
	{
		getAttackerClans().add(new L2SiegeClan(clanId, SiegeClanType.ATTACKER)); // Add registered attacker to attacker list
	}
	
	private void addDefender(int clanId)
	{
		getDefenderClans().add(new L2SiegeClan(clanId, SiegeClanType.DEFENDER)); // Add registered defender to defender list
	}
	
	private void addDefender(int clanId, SiegeClanType type)
	{
		getDefenderClans().add(new L2SiegeClan(clanId, type));
	}
	
	private void addDefenderWaiting(int clanId)
	{
		getDefenderWaitingClans().add(new L2SiegeClan(clanId, SiegeClanType.DEFENDER_PENDING)); // Add registered defender to defender list
	}
	
	private boolean checkIfCanRegister(L2PcInstance player)
	{
		if (getIsRegistrationOver())
			player.sendMessage("The deadline to register for the siege of " + getCastle().getName() + " has passed.");
		else if (getIsInProgress())
			player.sendMessage("This is not the time for siege registration and so registration and cancellation cannot be done.");
		else if ((player.getClan() == null) || (player.getClan().getLevel() < SiegeManager.getInstance().getSiegeClanMinLevel()))
			player.sendMessage("Only clans with Level " + SiegeManager.getInstance().getSiegeClanMinLevel() + " and higher may register for a castle siege.");
		else if (player.getClan().hasCastle() > 0)
			player.sendMessage("You cannot register because your clan already own a castle.");
		else if (player.getClan().getClanId() == getCastle().getOwnerId())
			player.sendPacket(SystemMessageId.CLAN_THAT_OWNS_CASTLE_IS_AUTOMATICALLY_REGISTERED_DEFENDING);
		else if (SiegeManager.checkIsRegistered(player.getClan(), getCastle().getCastleId()))
			player.sendMessage("You are already registered in a Siege.");
		else
			return true;
		
		return false;
	}

	private void loadSiegeClan()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			getAttackerClans().clear();
			getDefenderClans().clear();
			getDefenderWaitingClans().clear();
			
			// Add castle owner as defender (add owner first so that they are on the top of the defender list)
			if (getCastle().getOwnerId() > 0)
				addDefender(getCastle().getOwnerId(), SiegeClanType.OWNER);
			
			PreparedStatement statement = null;
			ResultSet rs = null;
			
			statement = con.prepareStatement("SELECT clan_id,type FROM siege_clans WHERE castle_id=?");
			statement.setInt(1, getCastle().getCastleId());
			rs = statement.executeQuery();
			
			int typeId;
			while (rs.next())
			{
				typeId = rs.getInt("type");
				if (typeId == 0)
					addDefender(rs.getInt("clan_id"));
				else if (typeId == 1)
					addAttacker(rs.getInt("clan_id"));
				else if (typeId == 2)
					addDefenderWaiting(rs.getInt("clan_id"));
			}
			
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning(Siege.class.getName() + ": loadSiegeClan(): ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	private void removeArtifact()
	{
		if (_artifacts != null)
		{
			// Remove all instance of artifact for this castle
			for (L2ArtefactInstance art : _artifacts)
			{
				if (art != null)
					art.decayMe();
			}
			_artifacts = null;
		}
	}
	
	private void removeControlTower()
	{
		if (_controlTowers != null)
		{
			// Remove all instance of control tower for this castle
			for (L2ControlTowerInstance ct : _controlTowers)
			{
				if (ct != null)
					ct.decayMe();
			}
			
			_controlTowers = null;
		}
	}
	
	private void removeFlags()
	{
		for (L2SiegeClan sc : getAttackerClans())
		{
			if (sc != null)
				sc.removeFlags();
		}
		for (L2SiegeClan sc : getDefenderClans())
		{
			if (sc != null)
				sc.removeFlags();
		}
	}
	
	private void removeDefenderFlags()
	{
		for (L2SiegeClan sc : getDefenderClans())
		{
			if (sc != null)
				sc.removeFlags();
		}
	}
	
	private void saveCastleSiege(boolean launch)
	{
		setNextSiegeDate(); // Set the next set date for 2 weeks from now
		saveSiegeDate(); // Save the new date
		
		if (launch)
		    startAutoTask(); // Prepare auto start siege and end registration
	}
	
	public void saveSiegeDate()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE castle SET siegeDate=? WHERE id=?");
			statement.setLong(1, getSiegeDate().getTimeInMillis());
			statement.setInt(2, getCastle().getCastleId());
			statement.execute();
			
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning(Siege.class.getName() + ": saveSiegeDate(): ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	private void saveSiegeClan(L2Clan clan, int typeId, boolean isUpdateRegistration)
	{
		if (clan.hasCastle() > 0)
			return;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			if ((typeId == 0) || (typeId == 2) || (typeId == -1))
				if (getDefenderClans().size() + getDefenderWaitingClans().size() >= SiegeManager.getInstance().getDefenderMaxClans())
					return;
				else if (getAttackerClans().size() >= SiegeManager.getInstance().getAttackerMaxClans())
					return;
			
			PreparedStatement statement;
			if (!isUpdateRegistration)
			{
				statement = con.prepareStatement("INSERT INTO siege_clans (clan_id,castle_id,type,castle_owner) VALUES (?,?,?,0)");
				statement.setInt(1, clan.getClanId());
				statement.setInt(2, getCastle().getCastleId());
				statement.setInt(3, typeId);
				statement.execute();
				statement.close();
			}
			else
			{
				statement = con.prepareStatement("UPDATE siege_clans SET type=? WHERE castle_id=? AND clan_id=?");
				statement.setInt(1, typeId);
				statement.setInt(2, getCastle().getCastleId());
				statement.setInt(3, clan.getClanId());
				statement.execute();
				statement.close();
			}
			
			if (typeId == 0 || typeId == -1)
			{
				addDefender(clan.getClanId());
				announceToPlayer(clan.getName() + " has been registered to defend " + getCastle().getName(), false);
			}
			else if (typeId == 1)
			{
				addAttacker(clan.getClanId());
				announceToPlayer(clan.getName() + " has been registered to attack " + getCastle().getName(), false);
			}
			else if (typeId == 2)
			{
				addDefenderWaiting(clan.getClanId());
				announceToPlayer(clan.getName() + " has requested to defend " + getCastle().getName(), false);
			}
		}
		catch (Exception e)
		{
			_log.warning(Siege.class.getName() + ": saveSiegeClan(L2Clan clan, int typeId, boolean isUpdateRegistration): " + e.getMessage());
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	private void setNextSiegeDate()
	{
		final Calendar siegeDate = _castle.getSiegeDate();
		
		if (siegeDate.getTimeInMillis() < System.currentTimeMillis())
			siegeDate.setTimeInMillis(System.currentTimeMillis());
		
		switch (_castle.getCastleId())
		{
			case 3:
			case 4:
			case 6:
			case 7:
				siegeDate.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
				break;
				
			default:
				siegeDate.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
				break;
		}
		
		siegeDate.add(Calendar.WEEK_OF_YEAR, 2);
		
		siegeDate.set(Calendar.HOUR_OF_DAY, 18);
		siegeDate.set(Calendar.MINUTE, 0);
		siegeDate.set(Calendar.SECOND, 0);
		siegeDate.set(Calendar.MILLISECOND, 0);
		
		_isRegistrationOver = false; // Allow registration for next siege
	}
	
	private void spawnArtifact(int Id)
	{
		// Set artifact array size if one does not exist
		if (_artifacts == null)
			_artifacts = new ArrayList<>();
		
		for (SiegeSpawn _sp : SiegeManager.getInstance().getArtefactSpawnList(Id))
		{
			L2ArtefactInstance art;
			
			art = new L2ArtefactInstance(IdFactory.getInstance().getNextId(), NpcData.getInstance().getTemplate(_sp.getNpcId()));
			art.setCurrentHpMp(art.getMaxHp(), art.getMaxMp());
			art.setHeading(_sp.getLocation().getHeading());
			art.spawnMe(_sp.getLocation().getX(), _sp.getLocation().getY(), _sp.getLocation().getZ() + 50);
			
			_artifacts.add(art);
		}
	}
	
	private void spawnControlTower(int Id)
	{
		// Set control tower array size if one does not exist
		if (_controlTowers == null)
			_controlTowers = new ArrayList<>();
		
		for (SiegeSpawn _sp : SiegeManager.getInstance().getControlTowerSpawnList(Id))
		{
			L2ControlTowerInstance ct;
			
			L2NpcTemplate template = NpcData.getInstance().getTemplate(_sp.getNpcId());
			
			template.getStatsSet().set("baseHpMax", _sp.getHp());
			// TODO: Check/confirm if control towers have any special weapon resistances/vulnerabilities
			// template.addVulnerability(Stats.BOW_WPN_VULN,0);
			// template.addVulnerability(Stats.BLUNT_WPN_VULN,0);
			// template.addVulnerability(Stats.DAGGER_WPN_VULN,0);
			
			ct = new L2ControlTowerInstance(IdFactory.getInstance().getNextId(), template);
			
			ct.setCurrentHpMp(ct.getMaxHp(), ct.getMaxMp());
			ct.spawnMe(_sp.getLocation().getX(), _sp.getLocation().getY(), _sp.getLocation().getZ() + 20);
			ct.broadcastInfo();
			_controlTowers.add(ct);
		}
	}
	
	private void spawnSiegeGuard()
	{
		SiegeGuardManager.getInstance().spawnSiegeGuard(getCastle());

		final Set<L2Spawn> spawned = SiegeGuardManager.getInstance().getSpawnedGuards(getCastle().getCastleId());
		if (!spawned.isEmpty() && _controlTowers.size() > 0)
		{
			L2ControlTowerInstance closestCt;
			double distance, x, y, z;
			double distanceClosest = 0;
			for (L2Spawn spawn : spawned)
			{
				if (spawn == null)
					continue;
				closestCt = null;
				distanceClosest = 0;
				for (L2ControlTowerInstance ct : _controlTowers)
				{
					if (ct == null)
						continue;
					x = (spawn.getLocx() - ct.getX());
					y = (spawn.getLocy() - ct.getY());
					z = (spawn.getLocz() - ct.getZ());
					
					distance = (x * x) + (y * y) + (z * z);
					
					if (closestCt == null || distance < distanceClosest)
					{
						closestCt = ct;
						distanceClosest = distance;
					}
				}
				
				if (closestCt != null)
					closestCt.registerGuard(spawn);
			}
		}
	}
	
	public final L2SiegeClan getAttackerClan(L2Clan clan)
	{
		if (clan == null)
			return null;
		return getAttackerClan(clan.getClanId());
	}
	
	public final L2SiegeClan getAttackerClan(int clanId)
	{
		for (L2SiegeClan sc : getAttackerClans())
			if (sc != null && sc.getClanId() == clanId)
				return sc;
		return null;
	}
	
	public final List<L2SiegeClan> getAttackerClans()
	{
		if (_isNormalSide)
			return _attackerClans;
		return _defenderClans;
	}
	
	@SuppressWarnings("static-method")
	public final int getAttackerRespawnDelay()
	{
		return (SiegeManager.getInstance().getAttackerRespawnDelay());
	}
	
	public final Castle getCastle()
	{
		return _castle;
	}
	
	public final L2SiegeClan getDefenderClan(L2Clan clan)
	{
		if (clan == null)
			return null;
		return getDefenderClan(clan.getClanId());
	}
	
	public final L2SiegeClan getDefenderClan(int clanId)
	{
		for (L2SiegeClan sc : getDefenderClans())
			if ((sc != null) && (sc.getClanId() == clanId))
				return sc;
		return null;
	}
	
	public final List<L2SiegeClan> getDefenderClans()
	{
		if (_isNormalSide)
			return _defenderClans;
		return _attackerClans;
	}
	
	public final L2SiegeClan getDefenderWaitingClan(L2Clan clan)
	{
		if (clan == null)
			return null;
		return getDefenderWaitingClan(clan.getClanId());
	}
	
	public final L2SiegeClan getDefenderWaitingClan(int clanId)
	{
		for (L2SiegeClan sc : getDefenderWaitingClans())
			if ((sc != null) && (sc.getClanId() == clanId))
				return sc;
		return null;
	}
	
	public final List<L2SiegeClan> getDefenderWaitingClans()
	{
		return _defenderWaitingClans;
	}
	
	public final int getDefenderRespawnDelay()
	{
		return (SiegeManager.getInstance().getDefenderRespawnDelay() + _defenderRespawnDelayPenalty);
	}
	
	public final boolean getIsInProgress()
	{
		return _isInProgress;
	}
	
	public final boolean getIsRegistrationOver()
	{
		return _isRegistrationOver;
	}
	
	public final Calendar getSiegeDate()
	{
		return getCastle().getSiegeDate();
	}
	
	public List<L2Npc> getFlag(L2Clan clan)
	{
		if (clan != null)
		{
			L2SiegeClan sc = getAttackerClan(clan);
			if (sc != null)
				return sc.getFlag();
		}
		return null;
	}
}