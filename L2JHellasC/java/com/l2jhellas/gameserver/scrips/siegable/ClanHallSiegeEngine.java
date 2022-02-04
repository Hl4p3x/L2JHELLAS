package com.l2jhellas.gameserver.scrips.siegable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.datatables.sql.ClanTable;
import com.l2jhellas.gameserver.instancemanager.ClanHallSiegeManager;
import com.l2jhellas.gameserver.instancemanager.CustomSpawnManager;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.L2SiegeClan;
import com.l2jhellas.gameserver.model.L2SiegeClan.SiegeClanType;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.quest.Quest;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.NpcSay;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.util.Broadcast;
import com.l2jhellas.util.database.L2DatabaseFactory;

public abstract class ClanHallSiegeEngine extends Quest implements Siegable
{	
	protected static Logger _log = Logger.getLogger(ClanHallSiegeEngine.class.getName());
	
	private static final String SQL_LOAD_ATTACKERS = "SELECT attacker_id FROM clanhall_siege_attackers WHERE clanhall_id = ?";	
	private static final String SQL_SAVE_ATTACKERS = "INSERT INTO clanhall_siege_attackers VALUES (?,?)";	

	private String DEVASTATED_CASTLE_GUARDS = "devastated_castle_guards";
	private String DEVASTATED_CASTLE_BOSS = "devastated_castle_boss";
	
	private String FORTRESS_OF_RESSISTANCE_GUARDS_NO_OWNER = "fortress_of_resistance_siege_guards_no_owner";
	private String FORTRESS_OF_RESSISTANCE_GUARDS_OWNER = "fortress_of_resistance_siege_guards_owner";	
	private String FORTRESS_OF_RESSISTANCE_NURKA_NO_OWNER = "fortress_of_resistance_siege_nurka_no_owner";
	private String FORTRESS_OF_RESSISTANCE_NURKA_OWNER = "fortress_of_resistance_siege_nurka_owner";

	private String FORTRESS_OF_DEAD_GUARDS = "fortress_of_dead_guards";
	private String FORTRESS_OF_DEAD_BOSS = "fortress_of_dead_boss";
	
	private String BANDIT_STRONGHOLD_NPCS = "Bandit_Stronghold_Npcs";
	private String BEAST_FARM_NPCS = "Beast_Farm_Npcs";
	
	public static final int FORTRESS_RESSISTANCE = 21;	
	public static final int DEVASTATED_CASTLE = 34;	
	public static final int BANDIT_STRONGHOLD = 35;	
	public static final int RAINBOW_SPRINGS = 62;	
	public static final int BEAST_FARM = 63;	
	public static final int FORTRESS_OF_DEAD = 64;
	
	private final Map<Integer, L2SiegeClan> _attackers = new ConcurrentHashMap<>();
		
	public SiegableHall _hall;
	
	public ScheduledFuture<?> _siegeTask;
	
	public boolean _missionAccomplished = false;
	
	public ClanHallSiegeEngine(String name, String descr, final int hallId)
	{
		super(-1, name, descr);
		
		_hall = ClanHallSiegeManager.getInstance().getSiegableHall(hallId);
		_hall.setSiege(this);
		
		_siegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new PrepareOwner(), _hall.getNextSiegeTime() - System.currentTimeMillis() - 3600000);
		_log.info(_hall.getName() + " siege scheduled for " + getSiegeDate().getTime());
		loadAttackers();
	}
	
	public void loadAttackers()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement(SQL_LOAD_ATTACKERS))
		{
			ps.setInt(1, _hall.getId());
			try (ResultSet rset = ps.executeQuery())
			{
				while (rset.next())
				{
					final int id = rset.getInt("attacker_id");
					L2SiegeClan clan = new L2SiegeClan(id,SiegeClanType.ATTACKER);
					_attackers.put(id, clan);
				}
			}
		}
		catch (Exception e)
		{
			_log.warning("Could not load siege attackers : " + e);
		}
	}
	
	public final void saveAttackers() 
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement ps = con.prepareStatement("DELETE FROM clanhall_siege_attackers WHERE clanhall_id = ?")) {
			ps.setInt(1, _hall.getId());
			ps.execute();
			
			if (_attackers.size() > 0) 
			{
				try (PreparedStatement insert = con.prepareStatement(SQL_SAVE_ATTACKERS))
				{
					for (L2SiegeClan clan : _attackers.values())
					{
						insert.setInt(1, _hall.getId());
						insert.setInt(2, clan.getClanId());
						insert.execute();
						insert.clearParameters();
					}
				}
			}
			_log.info("Successfully saved attackers to database.");
		} catch (Exception e) {
			_log.warning("Couldn't save attacker list: " + e);
		}
	}
	
	private void spawnSiegeGuards(int id)
	{
		switch (id)
		{
			case FORTRESS_RESSISTANCE :
				if(_hall.getOwnerId() > 0)
				{
					spawnByEventName(FORTRESS_OF_RESSISTANCE_GUARDS_OWNER);
					spawnByEventName(FORTRESS_OF_RESSISTANCE_NURKA_OWNER);
				}
				else
				{
					spawnByEventName(FORTRESS_OF_RESSISTANCE_GUARDS_NO_OWNER);
					spawnByEventName(FORTRESS_OF_RESSISTANCE_NURKA_NO_OWNER);
				}
				break;
			case DEVASTATED_CASTLE :
				spawnByEventName(DEVASTATED_CASTLE_GUARDS);
				spawnByEventName(DEVASTATED_CASTLE_BOSS);
				break;
			case BANDIT_STRONGHOLD:
				spawnByEventName(BANDIT_STRONGHOLD_NPCS);
				break;
			case BEAST_FARM:
				spawnByEventName(BEAST_FARM_NPCS);
				break;
			case FORTRESS_OF_DEAD :
				spawnByEventName(FORTRESS_OF_DEAD_GUARDS);
				spawnByEventName(FORTRESS_OF_DEAD_BOSS);
				break;
		}

	}

	private void unSpawnSiegeGuards(int id)
	{
		switch (id)
		{
			case FORTRESS_RESSISTANCE :
				if(_hall.getOwnerId() > 0)
				{
					despawnByEventName(FORTRESS_OF_RESSISTANCE_GUARDS_OWNER);
					despawnByEventName(FORTRESS_OF_RESSISTANCE_NURKA_OWNER);
				}
				else
				{
					despawnByEventName(FORTRESS_OF_RESSISTANCE_GUARDS_NO_OWNER);
					despawnByEventName(FORTRESS_OF_RESSISTANCE_NURKA_NO_OWNER);
				}
				break;
			case DEVASTATED_CASTLE :
				despawnByEventName(DEVASTATED_CASTLE_GUARDS);
				despawnByEventName(DEVASTATED_CASTLE_BOSS);
				break;
			case BANDIT_STRONGHOLD:
				despawnByEventName(BANDIT_STRONGHOLD_NPCS);
				break;
			case BEAST_FARM:
				despawnByEventName(BEAST_FARM_NPCS);
				break;
			case FORTRESS_OF_DEAD :
				despawnByEventName(FORTRESS_OF_DEAD_GUARDS);
				despawnByEventName(FORTRESS_OF_DEAD_BOSS);
				break;
		}
		

	}
	
	@Override
	public List<L2Npc> getFlag(L2Clan clan) 
	{
		List<L2Npc> result = null;
		L2SiegeClan sClan = getAttackerClan(clan);
		if (sClan != null) 
			result = sClan.getFlag();
		return result;
	}
	
	public final Map<Integer, L2SiegeClan> getAttackers()
	{
		return _attackers;
	}

	@Override
	public boolean checkIsAttacker(L2Clan clan)
	{
		if (clan == null)
			return false;

		return _attackers.containsKey(clan.getClanId());
	}

	@Override
	public boolean checkIsDefender(L2Clan clan)
	{
		return false;
	}

	@Override
	public L2SiegeClan getAttackerClan(int clanId)
	{
		return _attackers.get(clanId);
	}

	@Override
	public L2SiegeClan getAttackerClan(L2Clan clan)
	{
		return getAttackerClan(clan.getClanId());
	}

	@Override
	public List<L2SiegeClan> getAttackerClans()
	{
		return new ArrayList<>(_attackers.values());
	}
	
	@Override
	public List<L2PcInstance> getAttackersInZone() 
	{
		final List<L2PcInstance> attackers = new ArrayList<>();
		for (L2PcInstance pc : _hall.getSiegeZone().getPlayersInside())
		{
			final L2Clan clan = pc.getClan();
			if ((clan != null) && _attackers.containsKey(clan.getClanId())) 
				attackers.add(pc);
		}
		return attackers;
	}
	
	@Override
	public L2SiegeClan getDefenderClan(int clanId)
	{
		return null;
	}

	@Override
	public L2SiegeClan getDefenderClan(L2Clan clan)
	{
		return null;
	}

	@Override
	public List<L2SiegeClan> getDefenderClans()
	{
		return null;
	}
	
	public void prepareOwner() 
	{
		if (_hall.getOwnerId() > 0)
		{
			final L2SiegeClan clan = new L2SiegeClan(_hall.getOwnerId(), SiegeClanType.ATTACKER);
			_attackers.put(clan.getClanId(), new L2SiegeClan(clan.getClanId(), SiegeClanType.ATTACKER));
		}
		
		_hall.free();
		_hall.banishForeigners();
		SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.REGISTRATION_TERM_FOR_S1_ENDED);
		msg.addString(getName());
		Broadcast.toAllOnlinePlayers(msg);
		_hall.updateSiegeStatus(SiegeStatus.WAITING_BATTLE);
		
		_siegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new SiegeStarts(), 3600000);
	}
	
	@Override
	public void startSiege() 
	{
		if ((_attackers.size() < 1) && (_hall.getId() != 21)) // Fortress of resistance don't have attacker list
		{
			onSiegeEnds();
			_attackers.clear();
			_hall.updateNextSiege();
			_siegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new PrepareOwner(), _hall.getSiegeDate().getTimeInMillis());
			_hall.updateSiegeStatus(SiegeStatus.WAITING_BATTLE);
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST);
			sm.addString(_hall.getName());
			Broadcast.toAllOnlinePlayers(sm);
			return;
		}
		
		_hall.spawnDoor();
		
		spawnSiegeGuards(_hall.getId());
		_hall.updateSiegeZone(true);
		
		final byte state = 1;
		for (L2SiegeClan sClan : _attackers.values())
		{
			final L2Clan clan = ClanTable.getInstance().getClan(sClan.getClanId());
			if (clan == null)
				continue;

			for (L2PcInstance pc : clan.getOnlineMembers(0))
			{
				pc.setSiegeState(state);
				pc.broadcastUserInfo();
			}
		}
		
		_hall.updateSiegeStatus(SiegeStatus.RUNNING);
		onSiegeStarts();
		_siegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new SiegeEnds(), _hall.getSiegeLength());
	}

	@Override
	public void endSiege()
	{
		SystemMessage end = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_ENDED);
		end.addString(_hall.getName());
		Broadcast.toAllOnlinePlayers(end);

		L2Clan winner = getWinner();
		if (_missionAccomplished && (winner != null))
		{
			_hall.setOwner(winner);
			winner.setHasHideout(_hall.getId());
			SystemMessage finalMsg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_VICTORIOUS_OVER_S2_S_SIEGE);
			finalMsg.addString(winner.getName());
			finalMsg.addString(_hall.getName());
			Broadcast.toAllOnlinePlayers(finalMsg);
		}
		else
		{
			SystemMessage finalMsg = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_S1_DRAW);
			finalMsg.addString(_hall.getName());
			Broadcast.toAllOnlinePlayers(finalMsg);
		}
		_missionAccomplished = false;

		_hall.updateSiegeZone(false);
		_hall.updateNextSiege();
		_hall.spawnDoor(false);
		_hall.banishForeignerss();

		final byte state = 0;
		for (L2SiegeClan sClan : _attackers.values())
		{
			final L2Clan clan = ClanTable.getInstance().getClan(sClan.getClanId());
			if (clan == null)
				continue;

			for (L2PcInstance player : clan.getOnlineMembers(0))
			{
				player.setSiegeState(state);
				player.broadcastUserInfo();
			}
		}
		
		// Update pvp flag for winners when siege zone becomes inactive
		for (L2Character chr : _hall.getSiegeZone().getCharactersInside())
		{
			if ((chr != null) && chr.isPlayer())
				chr.getActingPlayer().updatePvPStatus();
		}
		
		_attackers.clear();
		
		onSiegeEnds();
		
		_siegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new PrepareOwner(), _hall.getNextSiegeTime() - System.currentTimeMillis() - 3600000);
		_log.info("Siege of " + _hall.getName() + " scheduled for " + _hall.getSiegeDate().getTime());
		
		_hall.updateSiegeStatus(SiegeStatus.REGISTERING);
		unSpawnSiegeGuards(_hall.getId());
	}
	
	@Override
	public void updateSiege()
	{
		cancelSiegeTask();
		_siegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new PrepareOwner(), _hall.getNextSiegeTime() - 3600000);
		_log.info(_hall.getName() + " siege scheduled for " + _hall.getSiegeDate().getTime());
	}
	
	public void cancelSiegeTask()
	{
		if (_siegeTask != null)
			_siegeTask.cancel(false);
	}
	
	@Override
	public Calendar getSiegeDate()
	{
		return _hall.getSiegeDate();
	}

	public void spawnByEventName(String name)
	{
		CustomSpawnManager.getInstance().spawnByEventName(name);
	}
	
	public void despawnByEventName(String name)
	{
		CustomSpawnManager.getInstance().despawnByEventName(name);
	}
	
	public final void broadcastNpcSay(final L2Npc npc, final int type, final String messageId) 
	{		
		Broadcast.toAllPlayersInRegion(npc.getWorldRegion(), new NpcSay(npc.getObjectId(), type, npc.getNpcId(), messageId));
	}

	public boolean canPlantFlag()
	{
		return true;
	}

	public boolean doorIsAutoAttackable()
	{
		return true;
	}

	public void onSiegeStarts()
	{
	}

	public void onSiegeEnds()
	{
	}

	public abstract L2Clan getWinner();
	
	public class PrepareOwner implements Runnable
	{
		@Override
		public void run()
		{
			prepareOwner();
		}
	}

	public class SiegeStarts implements Runnable
	{
		@Override
		public void run()
		{
			startSiege();
		}
	}

	public class SiegeEnds implements Runnable
	{
		@Override
		public void run()
		{
			endSiege();
		}
	}
}