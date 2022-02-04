package com.l2jhellas.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.zone.L2ZoneType;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.scrips.siegable.ClanHallSiegeEngine;
import com.l2jhellas.gameserver.scrips.siegable.SiegableHall;
import com.l2jhellas.gameserver.templates.StatsSet;
import com.l2jhellas.util.database.L2DatabaseFactory;

public final class ClanHallSiegeManager
{	
	protected static final Logger _log = Logger.getLogger(ClanHallSiegeManager.class.getName());
	
	private static final String SQL_LOAD_HALLS = "SELECT * FROM siegable_clanhall";
	
	private final Map<Integer, SiegableHall> _siegableHalls = new HashMap<>();
	
	protected ClanHallSiegeManager() {
		loadClanHalls();
	}
	
	private void loadClanHalls()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
		Statement s = con.createStatement();
		ResultSet rs = s.executeQuery(SQL_LOAD_HALLS))
		{
			_siegableHalls.clear();
			
			while (rs.next())
			{
				final int id = rs.getInt("clanHallId");
				
				StatsSet set = new StatsSet();
				
				set.set("id", id);
				set.set("name", rs.getString("name"));
				set.set("ownerId", rs.getInt("ownerId"));
				set.set("desc", rs.getString("desc"));
				set.set("location", rs.getString("location"));
				set.set("nextSiege", rs.getLong("nextSiege"));
				set.set("siegeLength", rs.getLong("siege_length"));
				set.set("scheduleConfig", rs.getString("schedule_config"));
				SiegableHall hall = new SiegableHall(set);
				_siegableHalls.put(id, hall);
			}
			_log.info("ClanHallSiegeManager Loaded :"+_siegableHalls.size()+" conquerable clan halls." );
		} 
		catch (Exception ex)
		{
			_log.warning("ClanHallSiegeManager: Could not load siegable clan halls :" + ex);
		}
	}
	
	public Map<Integer, SiegableHall> getConquerableHalls() 
	{
		return _siegableHalls;
	}
	
	public SiegableHall getSiegableHall(int clanHall)
	{
		return getConquerableHalls().get(clanHall);
	}
	
	public SiegableHall getNearbyClanHall(L2Character activeChar) 
	{
		return getNearbyClanHall(activeChar.getX(), activeChar.getY(), 10000);
	}
	
	public SiegableHall getNearbyClanHall(int x, int y, int maxDist) 
	{
		for (Map.Entry<Integer, SiegableHall> ch : _siegableHalls.entrySet()) 
		{
			final L2ZoneType zone = ch.getValue().getZone();
			if ((zone != null) && (zone.getDistanceToZone(x, y) < maxDist))
				return ch.getValue();
		}
		return null;
	}
	
	public final SiegableHall getActiveSiege(L2Character creature)
	{
		for (Map.Entry<Integer, SiegableHall> ch : _siegableHalls.entrySet()) 
		{
			final SiegableHall sh = ch.getValue();

			if (sh.getSiegeZone().isActive() && sh.getSiegeZone().isInsideZone(creature))
				return ch.getValue();
		}
		return null;
	}
	
	public ClanHallSiegeEngine getSiege(L2Character character)
	{
		SiegableHall hall = getNearbyClanHall(character);
		if (hall == null)
			return null;
		return hall.getSiege();
	}
	
	public void registerClan(L2Clan clan, SiegableHall hall,L2PcInstance player)
	{
		int SiegeClanlvl = SiegeManager.getInstance().getSiegeClanMinLevel();
		if (clan.getLevel() < SiegeClanlvl)
			player.sendMessage("Only clans of level " + SiegeClanlvl + " or higher may register for a castle siege");
		else if (hall.isWaitingBattle())
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DEADLINE_FOR_SIEGE_S1_PASSED).addString(hall.getName()));
		else if (hall.isInSiege())
			player.sendPacket(SystemMessageId.NOT_SIEGE_REGISTRATION_TIME2);
		else if (hall.getOwnerId() == clan.getClanId())
			player.sendPacket(SystemMessageId.CLAN_THAT_OWNS_CASTLE_IS_AUTOMATICALLY_REGISTERED_DEFENDING);
		else if ((clan.hasCastle() != 0) || (clan.hasHideout() != 0))
			player.sendPacket(SystemMessageId.CLAN_THAT_OWNS_CASTLE_CANNOT_PARTICIPATE_OTHER_SIEGE);
		else if (hall.getSiege().checkIsAttacker(clan))
			player.sendPacket(SystemMessageId.ALREADY_REQUESTED_SIEGE_BATTLE);
		else if (isClanParticipating(clan))
			player.sendPacket(SystemMessageId.APPLICATION_DENIED_BECAUSE_ALREADY_SUBMITTED_A_REQUEST_FOR_ANOTHER_SIEGE_BATTLE);
		else if (hall.getSiege().getAttackers().size() >= SiegeManager.getInstance().getAttackerMaxClans())
			player.sendPacket(SystemMessageId.ATTACKER_SIDE_FULL);
		else
			hall.addAttacker(clan);
	}
	
	public void unRegisterClan(L2Clan clan, SiegableHall hall) 
	{
		if (!hall.isRegistering()) 
			return;
		hall.removeAttacker(clan);
	}
	
	public boolean isClanParticipating(L2Clan clan) 
	{
		for (SiegableHall hall : getConquerableHalls().values())
		{
			if ((hall.getSiege() != null) && hall.getSiege().checkIsAttacker(clan))
				return true;
		}
		return false;
	}
	
	public SiegableHall getClanHallByOwner(L2Clan clan) 
	{
		for (SiegableHall hall : getConquerableHalls().values())
		{
			if ((hall.getOwnerId() == clan.getClanId()))
				return hall;
		}
		return null;
	}
	
	public void onServerShutDown() 
	{
		for (SiegableHall hall : getConquerableHalls().values()) 
		{
			// Rainbow springs has his own attackers table
			if ((hall.getId() == 62) || (hall.getSiege() == null)) 
				continue;
			
			hall.getSiege().saveAttackers();
		}
	}
	
	public static ClanHallSiegeManager getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder {
		protected static final ClanHallSiegeManager INSTANCE = new ClanHallSiegeManager();
	}
}