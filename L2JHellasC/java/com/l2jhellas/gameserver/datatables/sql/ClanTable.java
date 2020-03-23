package com.l2jhellas.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.idfactory.IdFactory;
import com.l2jhellas.gameserver.instancemanager.SiegeManager;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.L2ClanMember;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.entity.Siege;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import com.l2jhellas.gameserver.network.serverpackets.PledgeShowMemberListAll;
import com.l2jhellas.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.network.serverpackets.UserInfo;
import com.l2jhellas.util.Util;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class ClanTable
{
	private static Logger _log = Logger.getLogger(ClanTable.class.getName());
		
	private final Map<Integer, L2Clan> _clans = new ConcurrentHashMap<>();

	protected ClanTable()
	{
		L2Clan clan;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT clan_id FROM clan_data");
			ResultSet result = statement.executeQuery();
			
			// Count the clans
			int clanCount = 0;
			
			while (result.next())
			{
				_clans.put(Integer.parseInt(result.getString("clan_id")), new L2Clan(Integer.parseInt(result.getString("clan_id"))));
				clan = getClan(Integer.parseInt(result.getString("clan_id")));
				if (clan.getDissolvingExpiryTime() != 0)
				{
					if (clan.getDissolvingExpiryTime() < System.currentTimeMillis())
					{
						destroyClan(clan.getClanId());
					}
					else
					{
						scheduleRemoveClan(clan.getClanId());
					}
				}
				clanCount++;
			}
			result.close();
			statement.close();
			
			_log.info(ClanTable.class.getSimpleName() + ": Restored " + clanCount + " clans from the database.");
		}
		catch (Exception e)
		{
			_log.warning(ClanTable.class.getName() + ": data error on ClanTable: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
		restorewars();
	}
	
	public Collection<L2Clan> getClans()
	{
		return _clans.values();
	}

	public L2Clan getClan(int clanId)
	{
		return _clans.get(clanId);
	}
	
	public L2Clan getClanByName(String clanName)
	{
		return _clans.values().stream().filter(c -> c.getName().equalsIgnoreCase(clanName)).findAny().orElse(null);
	}
	
	public L2Clan createClan(L2PcInstance player, String clanName)
	{
		if (player == null)
		{
			return null;
		}
		
		if (player.getLevel() < 10)
		{
			player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_CLAN);
			return null;
		}
		
		if (player.getClanId() != 0)
		{
			player.sendPacket(SystemMessageId.FAILED_TO_CREATE_CLAN);
			return null;
		}
		
		if (System.currentTimeMillis() < player.getClanCreateExpiryTime())
		{
			player.sendPacket(SystemMessageId.YOU_MUST_WAIT_XX_DAYS_BEFORE_CREATING_A_NEW_CLAN);
			return null;
		}
		if (!Util.isAlphaNumeric(clanName))
		{
			player.sendPacket(SystemMessageId.CLAN_NAME_INVALID);
			return null;
		}
		
		if (clanName.length() < 2 || clanName.length() > 16)
		{
			player.sendPacket(SystemMessageId.CLAN_NAME_LENGTH_INCORRECT);
			return null;
		}
		
		if (getClanByName(clanName) != null)
		{
			// clan name is already taken
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_EXISTS).addString(clanName));
			return null;
		}
		
		L2Clan clan = new L2Clan(IdFactory.getInstance().getNextId(), clanName);
		L2ClanMember leader = new L2ClanMember(clan, player.getName(), player.getLevel(), player.getClassId().getId(), player.getObjectId(), player.getPledgeType(), player.getPowerGrade(), player.getTitle());
		clan.setLeader(leader);
		leader.setPlayerInstance(player);
		clan.store();
		player.setClan(clan);
		player.setPledgeClass(leader.calculatePledgeClass(player));
		player.setClanPrivileges(L2Clan.CP_ALL);
		
		if (Config.DEBUG)
			_log.config(ClanTable.class.getName() + ": New clan created: " + clan.getClanId() + " " + clan.getName());

		_clans.put(clan.getClanId(), clan);
		
		// should be update packet only
		player.sendPacket(new PledgeShowInfoUpdate(clan));
		player.sendPacket(new PledgeShowMemberListAll(clan, 0));
		player.sendPacket(new UserInfo(player));
		player.sendPacket(new PledgeShowMemberListUpdate(player));
		player.sendPacket(SystemMessageId.CLAN_CREATED);
		return clan;
	}
	
	public synchronized void destroyClan(int clanId)
	{
		L2Clan clan = getClan(clanId);
		if (clan == null)
		{
			return;
		}
		
		clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_HAS_DISPERSED));
		int castleId = clan.hasCastle();
		if (castleId == 0)
		{
			for (Siege siege : SiegeManager.getInstance().getSieges())
			{
				siege.removeSiegeClan(clanId);
			}
		}
		
		L2ClanMember leaderMember = clan.getLeader();
		if (leaderMember == null)
			clan.getWarehouse().destroyAllItems("ClanRemove", null, null);
		else
			clan.getWarehouse().destroyAllItems("ClanRemove", clan.getLeader().getPlayerInstance(), null);
		
		for (L2ClanMember member : clan.getMembers())
		{
			clan.removeClanMember(member.getName(), 0);
		}
		
		_clans.remove(clanId);
		IdFactory.getInstance().releaseId(clanId);
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("DELETE FROM clan_data WHERE clan_id=?");
			statement.setInt(1, clanId);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM clan_privs WHERE clan_id=?");
			statement.setInt(1, clanId);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM clan_skills WHERE clan_id=?");
			statement.setInt(1, clanId);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM clan_subpledges WHERE clan_id=?");
			statement.setInt(1, clanId);
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? OR clan2=?");
			statement.setInt(1, clanId);
			statement.setInt(2, clanId);
			statement.execute();
			statement.close();
			
			if (castleId != 0)
			{
				statement = con.prepareStatement("UPDATE castle SET taxPercent=0 WHERE id=?");
				statement.setInt(2, castleId);
				statement.execute();
				statement.close();
			}
			
			if (Config.DEBUG)
				_log.config(ClanTable.class.getName() + ": clan removed in db: " + clanId);
		}
		catch (Exception e)
		{
			_log.warning(ClanTable.class.getName() + ": error while removing clan in db ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public void scheduleRemoveClan(final int clanId)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				if (getClan(clanId) == null)
				{
					return;
				}
				if (getClan(clanId).getDissolvingExpiryTime() != 0)
				{
					destroyClan(clanId);
				}
			}
		}, getClan(clanId).getDissolvingExpiryTime() - System.currentTimeMillis());
	}
	
	public boolean isAllyExists(String allyName)
	{
		for (L2Clan clan : getClans())
		{
			if (clan.getAllyName() != null && clan.getAllyName().equalsIgnoreCase(allyName))
			{
				return true;
			}
		}
		return false;
	}
	
	public void storeclanswars(int clanId1, int clanId2)
	{
		L2Clan clan1 = ClanTable.getInstance().getClan(clanId1);
		L2Clan clan2 = ClanTable.getInstance().getClan(clanId2);
		clan1.setEnemyClan(clan2);
		clan2.setAttackerClan(clan1);
		clan1.broadcastClanStatus();
		clan2.broadcastClanStatus();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement;
			statement = con.prepareStatement("REPLACE INTO clan_wars (clan1, clan2, wantspeace1, wantspeace2) VALUES (?,?,?,?)");
			statement.setInt(1, clanId1);
			statement.setInt(2, clanId2);
			statement.setInt(3, 0);
			statement.setInt(4, 0);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning(ClanTable.class.getName() + ": could not store clans wars data:");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
		// SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.WAR_WITH_THE_S1_CLAN_HAS_BEGUN);
		//
		SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_WAR_DECLARED_AGAINST_S1_IF_KILLED_LOSE_LOW_EXP);
		msg.addString(clan2.getName());
		clan1.broadcastToOnlineMembers(msg);
		// msg = SystemMessage.getSystemMessage(SystemMessageId.WAR_WITH_THE_S1_CLAN_HAS_BEGUN);
		// msg.addString(clan1.getName());
		// clan2.broadcastToOnlineMembers(msg);
		// clan1 declared clan war.
		msg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_DECLARED_WAR);
		msg.addString(clan1.getName());
		clan2.broadcastToOnlineMembers(msg);
	}
	
	public void deleteclanswars(int clanId1, int clanId2)
	{
		L2Clan clan1 = ClanTable.getInstance().getClan(clanId1);
		L2Clan clan2 = ClanTable.getInstance().getClan(clanId2);
		clan1.deleteEnemyClan(clan2);
		clan2.deleteAttackerClan(clan1);
		clan1.broadcastClanStatus();
		clan2.broadcastClanStatus();
		// for(L2ClanMember player: clan1.getMembers())
		// {
		// if(player.getPlayerInstance()!=null)
		// player.getPlayerInstance().setWantsPeace(0);
		// }
		// for(L2ClanMember player: clan2.getMembers())
		// {
		// if(player.getPlayerInstance()!=null)
		// player.getPlayerInstance().setWantsPeace(0);
		// }
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement;
			statement = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? AND clan2=?");
			statement.setInt(1, clanId1);
			statement.setInt(2, clanId2);
			statement.execute();
			// statement = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? AND clan2=?");
			// statement.setInt(1,clanId2);
			// statement.setInt(2,clanId1);
			// statement.execute();
			
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning(ClanTable.class.getName() + ": could not restore clans wars data:");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
		// SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.WAR_WITH_THE_S1_CLAN_HAS_ENDED);
		SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.WAR_AGAINST_S1_HAS_STOPPED);
		msg.addString(clan2.getName());
		clan1.broadcastToOnlineMembers(msg);
		msg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_HAS_DECIDED_TO_STOP);
		msg.addString(clan1.getName());
		clan2.broadcastToOnlineMembers(msg);
		// msg = SystemMessage.getSystemMessage(SystemMessageId.WAR_WITH_THE_S1_CLAN_HAS_ENDED);
		// msg.addString(clan1.getName());
		// clan2.broadcastToOnlineMembers(msg);
	}
	
	public void checkSurrender(L2Clan clan1, L2Clan clan2)
	{
		int count = 0;
		for (L2ClanMember player : clan1.getMembers())
		{
			if (player != null && player.getPlayerInstance().getWantsPeace() == 1)
				count++;
		}
		if (count == clan1.getMembers().length - 1)
		{
			clan1.deleteEnemyClan(clan2);
			clan2.deleteEnemyClan(clan1);
			deleteclanswars(clan1.getClanId(), clan2.getClanId());
		}
	}
	
	private void restorewars()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement;
			statement = con.prepareStatement("SELECT clan1, clan2, wantspeace1, wantspeace2 FROM clan_wars");
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				getClan(rset.getInt("clan1")).setEnemyClan(rset.getInt("clan2"));
				getClan(rset.getInt("clan2")).setAttackerClan(rset.getInt("clan1"));
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning(ClanTable.class.getName() + ": could not restore clan wars data:");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public static ClanTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ClanTable _instance = new ClanTable();
	}
}