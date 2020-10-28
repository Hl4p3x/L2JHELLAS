package com.l2jhellas.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.communitybbs.BB.Forum;
import com.l2jhellas.gameserver.communitybbs.Manager.ForumsBBSManager;
import com.l2jhellas.gameserver.datatables.sql.ClanTable;
import com.l2jhellas.gameserver.enums.ZoneId;
import com.l2jhellas.gameserver.instancemanager.CastleManager;
import com.l2jhellas.gameserver.instancemanager.SiegeManager;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.ItemContainer;
import com.l2jhellas.gameserver.model.entity.Siege;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.CreatureSay;
import com.l2jhellas.gameserver.network.serverpackets.ItemList;
import com.l2jhellas.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jhellas.gameserver.network.serverpackets.PledgeReceiveSubPledgeCreated;
import com.l2jhellas.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import com.l2jhellas.gameserver.network.serverpackets.PledgeShowMemberListAll;
import com.l2jhellas.gameserver.network.serverpackets.PledgeShowMemberListDeleteAll;
import com.l2jhellas.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import com.l2jhellas.gameserver.network.serverpackets.PledgeSkillListAdd;
import com.l2jhellas.gameserver.network.serverpackets.StatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.gameserver.network.serverpackets.UserInfo;
import com.l2jhellas.gameserver.skills.SkillTable;
import com.l2jhellas.util.Util;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class L2Clan
{
	private static final Logger _log = Logger.getLogger(L2Clan.class.getName());
	
	private String _name;
	private int _clanId;
	private L2ClanMember _leader;
	private final Map<String, L2ClanMember> _members = new HashMap<>();
	
	private String _allyName;
	private int _allyId;
	private int _level;
	private int _hasCastle;
	private int _hasHideout;
	private boolean _hasCrest;
	private int _hiredGuards;
	private int _crestId;
	private int _crestLargeId;
	private int _allyCrestId;
	private int _auctionBiddedAt = 0;
	private long _allyPenaltyExpiryTime;
	private int _allyPenaltyType;
	private long _charPenaltyExpiryTime;
	private long _dissolvingExpiryTime;
	// Ally Penalty Types
	
	public static final int PENALTY_TYPE_CLAN_LEAVED = 1;
	
	public static final int PENALTY_TYPE_CLAN_DISMISSED = 2;
	
	public static final int PENALTY_TYPE_DISMISS_CLAN = 3;
	
	public static final int PENALTY_TYPE_DISSOLVE_ALLY = 4;
	
	private final ItemContainer _warehouse = new ClanWarehouse(this);
	private final List<Integer> _atWarWith = new ArrayList<>();
	private final List<Integer> _atWarAttackers = new ArrayList<>();
	
	private boolean _hasCrestLarge;
	
	private Forum _forum;
	
	private final List<L2Skill> _skillList = new ArrayList<>();
	
	// Clan Privileges
	
	public static final int CP_NOTHING = 0;
	
	public static final int CP_CL_JOIN_CLAN = 2;
	
	public static final int CP_CL_GIVE_TITLE = 4;
	
	public static final int CP_CL_VIEW_WAREHOUSE = 8;
	
	public static final int CP_CL_MANAGE_RANKS = 16;
	public static final int CP_CL_PLEDGE_WAR = 32;
	public static final int CP_CL_DISMISS = 64;
	
	public static final int CP_CL_REGISTER_CREST = 128;
	public static final int CP_CL_MASTER_RIGHTS = 256;
	public static final int CP_CL_MANAGE_LEVELS = 512;
	
	public static final int CP_CH_OPEN_DOOR = 1024;
	public static final int CP_CH_OTHER_RIGHTS = 2048;
	public static final int CP_CH_AUCTION = 4096;
	public static final int CP_CH_DISMISS = 8192;
	public static final int CP_CH_SET_FUNCTIONS = 16384;
	public static final int CP_CS_OPEN_DOOR = 32768;
	public static final int CP_CS_MANOR_ADMIN = 65536;
	public static final int CP_CS_MANAGE_SIEGE = 131072;
	public static final int CP_CS_USE_FUNCTIONS = 262144;
	public static final int CP_CS_DISMISS = 524288;
	public static final int CP_CS_TAXES = 1048576;
	public static final int CP_CS_MERCENARIES = 2097152;
	public static final int CP_CS_SET_FUNCTIONS = 4194304;
	
	public static final int CP_ALL = 8388606;
	
	// Sub-unit types
	
	public static final int SUBUNIT_ACADEMY = -1;
	
	public static final int SUBUNIT_ROYAL1 = 100;
	
	public static final int SUBUNIT_ROYAL2 = 200;
	
	public static final int SUBUNIT_KNIGHT1 = 1001;
	
	public static final int SUBUNIT_KNIGHT2 = 1002;
	
	public static final int SUBUNIT_KNIGHT3 = 2001;
	
	public static final int SUBUNIT_KNIGHT4 = 2002;
	
	protected final Map<Integer, L2Skill> _skills = new HashMap<>();
	protected final Map<Integer, RankPrivs> _privs = new HashMap<>();
	protected final Map<Integer, SubPledge> _subPledges = new HashMap<>();
	
	private int _reputationScore = 0;
	private int _rank = 0;
	
	private String _notice;
	private boolean _noticeEnabled = false;
	
	private AtomicInteger _siegeKills;
	private AtomicInteger _siegeDeaths;
	
	public L2Clan(int clanId)
	{
		_clanId = clanId;
		initializePrivs();
		restore();
		getWarehouse().restore();
	}
	
	public L2Clan(int clanId, String clanName)
	{
		_clanId = clanId;
		_name = clanName;
		initializePrivs();
	}
	
	public int getClanId()
	{
		return _clanId;
	}
	
	public void setClanId(int clanId)
	{
		_clanId = clanId;
	}
	
	public int getLeaderId()
	{
		return (_leader != null ? _leader.getObjectId() : 0);
	}
	
	public L2ClanMember getLeader()
	{
		return _leader;
	}
	
	public void setLeader(L2ClanMember leader)
	{
		_leader = leader;
		_members.put(leader.getName(), leader);
	}
	
	public void setNewLeader(L2ClanMember member, L2PcInstance activeChar, boolean changeName)
	{
		if (activeChar.isRiding() || activeChar.isFlying())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (!getLeader().isOnline() && !changeName)
			return;
		if (member == null)
			return;
		if (!member.isOnline() && !changeName)
			return;
		
		L2PcInstance exLeader = getLeader().getPlayerInstance();
		SiegeManager.removeSiegeSkills(exLeader);
		exLeader.setClan(this);
		exLeader.setClanPrivileges(L2Clan.CP_NOTHING);
		exLeader.broadcastUserInfo();
		
		setLeader(member);
		updateClanInDB();
		
		exLeader.setPledgeClass(exLeader.getClan().getClanMember(exLeader.getObjectId()).calculatePledgeClass(exLeader));
		exLeader.broadcastUserInfo();
		L2PcInstance newLeader = member.getPlayerInstance();
		newLeader.setClan(this);
		newLeader.setPledgeClass(member.calculatePledgeClass(newLeader));
		newLeader.setClanPrivileges(L2Clan.CP_ALL);
		
		if (getLevel() >= 4)
		{
			SiegeManager.addSiegeSkills(newLeader);
		}
		newLeader.broadcastUserInfo();
		
		broadcastClanStatus();
		broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_LEADER_PRIVILEGES_HAVE_BEEN_TRANSFERRED_TO_S1).addString(member.getName()));
		
	}
	
	public String getLeaderName()
	{
		return (_leader != null ? _leader.getName() : "");
	}
	
	public String getName()
	{
		return _name;
	}
	
	public void setName(String name)
	{
		_name = name;
	}
	
	private void addClanMember(L2ClanMember member)
	{
		_members.put(member.getName(), member);
	}
	
	public void addClanMember(L2PcInstance player)
	{
		L2ClanMember member = new L2ClanMember(this, player.getName(), player.getLevel(), player.getClassId().getId(), player.getObjectId(), player.getPledgeType(), player.getPowerGrade(), player.getTitle());
		// store in memory
		addClanMember(member);
		member.setPlayerInstance(player);
		player.setClan(this);
		player.setPledgeClass(member.calculatePledgeClass(player));
		
		for (Siege siege : SiegeManager.getInstance().getSieges())
		{
			if (!siege.getIsInProgress())
				continue;
			if (siege.checkIsAttacker(player.getClan()))
				player.setSiegeState((byte) 1);
			else if (siege.checkIsDefender(player.getClan()))
				player.setSiegeState((byte) 2);
		}
		
		player.rewardSkills();
		
		player.sendPacket(new PledgeShowMemberListUpdate(player));
		player.sendPacket(new UserInfo(player));
	}
	
	public void updateClanMember(L2PcInstance player)
	{
		L2ClanMember member = new L2ClanMember(player);
		
		addClanMember(member);
	}
	
	public L2ClanMember getClanMember(String name)
	{
		return _members.get(name);
	}
	
	public L2ClanMember getClanMember(int objectID)
	{
		for (L2ClanMember temp : _members.values())
		{
			if (temp.getObjectId() == objectID)
				return temp;
		}
		return null;
	}
	
	public void removeClanMember(String name, long clanJoinExpiryTime)
	{
		L2ClanMember exMember = _members.remove(name);
		if (exMember == null)
		{
			_log.warning(L2Clan.class.getClass().getCanonicalName() + ": Member " + name + " not found in clan while trying to remove.");
			return;
		}
		int leadssubpledge = getLeaderSubPledge(name);
		if (leadssubpledge != 0)
		{
			// Sub-unit leader withdraws, position becomes vacant and leader
			// should appoint new via NPC
			getSubPledge(leadssubpledge).setLeaderName("");
			updateSubPledgeInDB(leadssubpledge);
		}
		
		if (exMember.getApprentice() != 0)
		{
			L2ClanMember apprentice = getClanMember(exMember.getApprentice());
			if (apprentice != null)
			{
				if (apprentice.getPlayerInstance() != null)
				{
					apprentice.getPlayerInstance().setSponsor(0);
				}
				else
				{
					apprentice.initApprenticeAndSponsor(0, 0);
				}
				
				apprentice.saveApprenticeAndSponsor(0, 0);
			}
		}
		if (exMember.getSponsor() != 0)
		{
			L2ClanMember sponsor = getClanMember(exMember.getSponsor());
			if (sponsor != null)
			{
				if (sponsor.getPlayerInstance() != null)
				{
					sponsor.getPlayerInstance().setApprentice(0);
				}
				else
				{
					sponsor.initApprenticeAndSponsor(0, 0);
				}
				
				sponsor.saveApprenticeAndSponsor(0, 0);
			}
		}
		exMember.saveApprenticeAndSponsor(0, 0);
		if (Config.REMOVE_CASTLE_CIRCLETS)
		{
			CastleManager.getInstance().removeCirclet(exMember, hasCastle());
		}
		if (exMember.isOnline())
		{
			L2PcInstance player = exMember.getPlayerInstance();
			player.setApprentice(0);
			player.setSponsor(0);
			
			if (player.isClanLeader())
			{
				SiegeManager.removeSiegeSkills(player);
				player.setClanCreateExpiryTime(System.currentTimeMillis() + Config.ALT_CLAN_CREATE_DAYS * 86400000L); // 24*60*60*1000 = 86400000
			}
			// remove Clanskills from Player
			for (L2Skill skill : player.getClan().getAllSkills())
			{
				player.removeSkill(skill, false);
			}
			player.setClan(null);
			player.setClanJoinExpiryTime(clanJoinExpiryTime);
			player.setPledgeClass(exMember.calculatePledgeClass(player));
			player.broadcastUserInfo();
			// disable clan tab
			player.sendPacket(new PledgeShowMemberListDeleteAll());
		}
		else
		{
			removeMemberInDatabase(exMember, clanJoinExpiryTime, getLeaderName().equalsIgnoreCase(name) ? System.currentTimeMillis() + Config.ALT_CLAN_CREATE_DAYS * 86400000L : 0);
		}
	}
	
	public L2ClanMember[] getMembers()
	{
		return _members.values().toArray(new L2ClanMember[_members.size()]);
	}
	
	public int getMembersCount()
	{
		return _members.size();
	}
	
	public int getOnlineMembersCount()
	{
		return (int) _members.values().stream().filter(m -> m.isOnline()).count();
	}
	
	public int getSubPledgeMembersCount(int subpl)
	{
		int result = 0;
		for (L2ClanMember temp : _members.values())
		{
			if (temp.getPledgeType() == subpl)
			{
				result++;
			}
		}
		return result;
	}
	
	public int getMaxNrOfMembers(int pledgetype)
	{
		int limit = 0;
		
		switch (pledgetype)
		{
			case 0:
				switch (getLevel())
				{
					case 4:
						limit = 40;
						break;
					case 3:
						limit = 30;
						break;
					case 2:
						limit = 20;
						break;
					case 1:
						limit = 15;
						break;
					case 0:
						limit = 10;
						break;
					default:
						limit = 40;
						break;
				}
				break;
			case -1:
			case 100:
			case 200:
				limit = 20;
				break;
			case 1001:
			case 1002:
			case 2001:
			case 2002:
				limit = 10;
				break;
			default:
				break;
		}
		
		return limit;
	}
	
	public L2PcInstance[] getOnlineMembers()
	{
		List<L2PcInstance> result = new ArrayList<>();
		
		for (L2ClanMember temp : _members.values())
		{
			if (temp != null && temp.isOnline())
				result.add(temp.getPlayerInstance());
		}
		
		return result.toArray(new L2PcInstance[result.size()]);
	}
	
	public List<L2PcInstance> getOnlineMembers(int exclude)
	{
		return _members.values().stream()
			.filter(member -> member.getObjectId() != exclude)
			.filter(L2ClanMember::isOnline)
			.map(L2ClanMember::getPlayerInstance)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());		
	}
	
	public Integer[] getOfflineMembersIds()
	{
		List<Integer> list = new ArrayList<>();
		
		for (L2ClanMember temp : _members.values())
		{
			if ((temp != null) && !temp.isOnline())
				list.add(temp.getObjectId());
		}
		
		return list.toArray(new Integer[list.size()]);
	}
	
	public int getAllyId()
	{
		return _allyId;
	}
		
	public void setAllyCrestId(int allyCrestId)
	{
		_allyCrestId = allyCrestId;
	}
	
	public int getAllyCrestId()
	{
		return _allyCrestId;
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public int hasCastle()
	{
		return _hasCastle;
	}
	
	public int hasHideout()
	{
		return _hasHideout;
	}
	
	public void setCrestId(int crestId)
	{
		_crestId = crestId;
	}
	
	public int getCrestId()
	{
		return _crestId;
	}
	
	public void setCrestLargeId(int crestLargeId)
	{
		_crestLargeId = crestLargeId;
	}
	
	public int getCrestLargeId()
	{
		return _crestLargeId;
	}
	
	public void setAllyId(int allyId)
	{
		_allyId = allyId;
	}
	
	public void setAllyName(String allyName)
	{
		_allyName = allyName;
	}
	public String getAllyName()
	{
		return _allyName;
	}

	public void setHasCastle(int hasCastle)
	{
		_hasCastle = hasCastle;
	}
	
	public void setHasHideout(int hasHideout)
	{
		_hasHideout = hasHideout;
	}
	
	public void setLevel(int level)
	{
		_level = level;
		if (_forum == null)
		{
			if (_level >= 2)
			{
				_forum = ForumsBBSManager.getInstance().getForumByName("ClanRoot").getChildByName(_name);
				if (_forum == null)
				{
					_forum = ForumsBBSManager.getInstance().createNewForum(_name, ForumsBBSManager.getInstance().getForumByName("ClanRoot"), Forum.CLAN, Forum.CLANMEMBERONLY, getClanId());
				}
			}
		}
	}
	
	public boolean isMember(String name)
	{
		return _members.containsKey(name);
	}
	
	public void updateClanInDB()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET leader_id=?,ally_id=?,ally_name=?,reputation_score=?,ally_penalty_expiry_time=?,ally_penalty_type=?,char_penalty_expiry_time=?,dissolving_expiry_time=? WHERE clan_id=?");
			statement.setInt(1, getLeaderId());
			statement.setInt(2, getAllyId());
			statement.setString(3, getAllyName());
			statement.setInt(4, getReputationScore());
			statement.setLong(5, getAllyPenaltyExpiryTime());
			statement.setInt(6, getAllyPenaltyType());
			statement.setLong(7, getCharPenaltyExpiryTime());
			statement.setLong(8, getDissolvingExpiryTime());
			statement.setInt(9, getClanId());
			statement.execute();
			statement.close();
			
			if (Config.DEBUG)
			{
				_log.config(L2Clan.class.getName() + ": New clan leader saved in db: " + getClanId());
			}
		}
		catch (SQLException e)
		{
			_log.warning(L2Clan.class.getName() + ": error while saving new clan leader to db ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public void store()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("INSERT INTO clan_data (clan_id,clan_name,clan_level,hasCastle,ally_id,ally_name,leader_id,crest_id,crest_large_id,ally_crest_id) values (?,?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, getClanId());
			statement.setString(2, getName());
			statement.setInt(3, getLevel());
			statement.setInt(4, hasCastle());
			statement.setInt(5, getAllyId());
			statement.setString(6, getAllyName());
			statement.setInt(7, getLeaderId());
			statement.setInt(8, getCrestId());
			statement.setInt(9, getCrestLargeId());
			statement.setInt(10, getAllyCrestId());
			statement.execute();
			statement.close();
			
			if (Config.DEBUG)
			{
				_log.config(L2Clan.class.getName() + ": New clan saved in db: " + getClanId());
			}
		}
		catch (SQLException e)
		{
			_log.warning(L2Clan.class.getName() + ": error while saving new clan to db ");
			e.printStackTrace();
		}
	}
	
	private void removeMemberInDatabase(L2ClanMember member, long clanJoinExpiryTime, long clanCreateExpiryTime)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET clanid=0, title=?, clan_join_expiry_time=?, clan_create_expiry_time=?, clan_privs=0, wantspeace=0, subpledge=0, lvl_joined_academy=0, apprentice=0, sponsor=0 WHERE obj_Id=?");
			statement.setString(1, "");
			statement.setLong(2, clanJoinExpiryTime);
			statement.setLong(3, clanCreateExpiryTime);
			statement.setInt(4, member.getObjectId());
			statement.execute();
			statement.close();
			if (Config.DEBUG)
			{
				_log.log(Level.FINE, getClass().getName() + ": clan member removed in db: " + getClanId());
			}
			
			statement = con.prepareStatement("UPDATE characters SET apprentice=0 WHERE apprentice=?");
			statement.setInt(1, member.getObjectId());
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("UPDATE characters SET sponsor=0 WHERE sponsor=?");
			statement.setInt(1, member.getObjectId());
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning(L2Clan.class.getName() + ": error while removing clan member in db ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	private void restore()
	{
		// restorewars();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT clan_name,clan_level,hasCastle,ally_id,ally_name,leader_id,crest_id,crest_large_id,ally_crest_id,reputation_score,auction_bid_at,ally_penalty_expiry_time,ally_penalty_type,char_penalty_expiry_time,dissolving_expiry_time FROM clan_data WHERE clan_id=?");
			statement.setInt(1, getClanId());
			ResultSet clanData = statement.executeQuery();
			
			if (clanData.next())
			{
				setName(clanData.getString("clan_name"));
				setLevel(clanData.getInt("clan_level"));
				setHasCastle(clanData.getInt("hasCastle"));
				setAllyId(clanData.getInt("ally_id"));
				setAllyName(clanData.getString("ally_name"));
				setAllyPenaltyExpiryTime(clanData.getLong("ally_penalty_expiry_time"), clanData.getInt("ally_penalty_type"));
				if (getAllyPenaltyExpiryTime() < System.currentTimeMillis())
				{
					setAllyPenaltyExpiryTime(0, 0);
				}
				setCharPenaltyExpiryTime(clanData.getLong("char_penalty_expiry_time"));
				if ((getCharPenaltyExpiryTime() + (Config.ALT_CLAN_JOIN_DAYS * 86400000L)) < System.currentTimeMillis()) // 24*60*60*1000 = 86400000
				{
					setCharPenaltyExpiryTime(0);
				}
				setDissolvingExpiryTime(clanData.getLong("dissolving_expiry_time"));
				
				setCrestId(clanData.getInt("crest_id"));
				if (getCrestId() != 0)
				{
					setHasCrest(true);
				}
				
				setCrestLargeId(clanData.getInt("crest_large_id"));
				if (getCrestLargeId() != 0)
				{
					setHasCrestLarge(true);
				}
				
				setAllyCrestId(clanData.getInt("ally_crest_id"));
				setReputationScore(clanData.getInt("reputation_score"), false);
				setAuctionBiddedAt(clanData.getInt("auction_bid_at"), false);
				
				final int leaderId = (clanData.getInt("leader_id"));
				
				statement.clearParameters();
				
				try (PreparedStatement statement2 = con.prepareStatement("SELECT char_name,level,classid,obj_Id,title,power_grade,subpledge,apprentice,sponsor,sex,race FROM characters WHERE clanid=?"))
				{
					statement2.setInt(1, getClanId());
					try (ResultSet clanMembers = statement2.executeQuery())
					{
						while (clanMembers.next())
						{
							L2ClanMember member = null;
							member = new L2ClanMember(this, clanMembers.getString("char_name"), clanMembers.getInt("level"), clanMembers.getInt("classid"), clanMembers.getInt("obj_id"), clanMembers.getInt("subpledge"), clanMembers.getInt("power_grade"), clanMembers.getString("title"));
							if (member.getObjectId() == leaderId)
							{
								setLeader(member);
							}
							else
							{
								addClanMember(member);
							}
							member.initApprenticeAndSponsor(clanMembers.getInt("apprentice"), clanMembers.getInt("sponsor"));
						}
						clanMembers.close();
					}
					statement2.close();
				}
				catch (Exception e)
				{
					if (Config.DEVELOPER)
						e.printStackTrace();
				}
			}
			
			if (Config.DEBUG && getName() != null)
			{
				_log.config(L2Clan.class.getSimpleName() + ": Restored clan data for: " + getName() + " from database.");
			}
			
			clanData.close();
			statement.close();
			
			restoreSubPledges();
			restoreRankPrivs();
			restoreSkills();
			restoreNotice();
		}
		catch (SQLException e)
		{
			_log.warning(L2Clan.class.getName() + ": error while restoring clan ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	private void restoreSkills()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			// Retrieve all skills of this L2PcInstance from the database
			PreparedStatement statement = con.prepareStatement("SELECT skill_id,skill_level FROM clan_skills WHERE clan_id=?");
			statement.setInt(1, getClanId());
			ResultSet rset = statement.executeQuery();
			
			// Go though the recordset of this SQL query
			while (rset.next())
			{
				int id = rset.getInt("skill_id");
				int level = rset.getInt("skill_level");
				// Create a L2Skill object for each record
				L2Skill skill = SkillTable.getInstance().getInfo(id, level);
				// Add the L2Skill object to the L2Clan _skills
				_skills.put(skill.getId(), skill);
			}
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning(L2Clan.class.getName() + ": Could not restore clan skills: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	private void restoreNotice()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT enabled,notice FROM clan_notices WHERE clan_id=?");
			statement.setInt(1, getClanId());
			try (ResultSet noticeData = statement.executeQuery())
			{
				while (noticeData.next())
				{
					_noticeEnabled = noticeData.getBoolean("enabled");
					_notice = noticeData.getString("notice");
				}
				noticeData.close();
			}
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning(L2Clan.class.getName() + ": Error restoring clan notice.");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	private void storeNotice(String notice, boolean enabled)
	{
		if (notice == null)
		{
			notice = "";
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO clan_notices (clan_id,notice,enabled) values (?,?,?) ON DUPLICATE KEY UPDATE notice=?,enabled=?"))
		{
			statement.setInt(1, getClanId());
			statement.setString(2, notice);
			if (enabled)
			{
				statement.setString(3, "true");
			}
			else
			{
				statement.setString(3, "false");
			}
			statement.setString(4, notice);
			if (enabled)
			{
				statement.setString(5, "true");
			}
			else
			{
				statement.setString(5, "false");
			}
			statement.execute();
		}
		catch (SQLException e)
		{
			_log.warning(L2Clan.class.getName() + ": Error could not store clan notice: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
		_notice = notice;
		_noticeEnabled = enabled;
	}
	
	public void setNoticeEnabled(boolean noticeEnabled)
	{
		storeNotice(_notice, noticeEnabled);
	}
	
	public void setNotice(String notice)
	{
		storeNotice(notice, _noticeEnabled);
	}
	
	public boolean isNoticeEnabled()
	{
		return _noticeEnabled;
	}
	
	public String getNotice()
	{
		if (_notice == null)
			return "";
		return _notice;
	}
	
	public final L2Skill[] getAllSkills()
	{
		if (_skills == null)
			return new L2Skill[0];
		
		return _skills.values().toArray(new L2Skill[_skills.values().size()]);
	}
	
	public L2Skill addSkill(L2Skill newSkill)
	{
		L2Skill oldSkill = null;
		
		if (newSkill != null)
		{
			// Replace oldSkill by newSkill or Add the newSkill
			oldSkill = _skills.put(newSkill.getId(), newSkill);
		}
		
		return oldSkill;
	}
	
	public L2Skill addNewSkill(L2Skill newSkill)
	{
		L2Skill oldSkill = null;
		
		if (newSkill != null)
		{
			
			// Replace oldSkill by newSkill or Add the newSkill
			oldSkill = _skills.put(newSkill.getId(), newSkill);
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection())
			{
				if (oldSkill != null)
				{
					try (PreparedStatement statement = con.prepareStatement("UPDATE clan_skills SET skill_level=? WHERE skill_id=? AND clan_id=?"))
					{
						statement.setInt(1, newSkill.getLevel());
						statement.setInt(2, oldSkill.getId());
						statement.setInt(3, getClanId());
						statement.execute();
					}
				}
				else
				{
					try (PreparedStatement statement = con.prepareStatement("INSERT INTO clan_skills (clan_id,skill_id,skill_level,skill_name) VALUES (?,?,?,?)"))
					{
						statement.setInt(1, getClanId());
						statement.setInt(2, newSkill.getId());
						statement.setInt(3, newSkill.getLevel());
						statement.setString(4, newSkill.getName());
						statement.execute();
					}
				}
			}
			catch (SQLException e)
			{
				_log.warning(L2Clan.class.getName() + ": Error could not store char skills: ");
				if (Config.DEVELOPER)
					e.printStackTrace();
			}
			
			for (L2ClanMember temp : _members.values())
			{
				try
				{
					if (temp.isOnline())
					{
						if (newSkill.getMinPledgeClass() <= temp.getPlayerInstance().getPledgeClass())
						{
							temp.getPlayerInstance().addSkill(newSkill, false); // Skill is not saved to player DB
							temp.getPlayerInstance().sendPacket(new PledgeSkillListAdd(newSkill.getId(), newSkill.getLevel()));
						}
					}
				}
				catch (NullPointerException e)
				{
				}
			}
		}
		
		return oldSkill;
	}
	
	public void addSkillEffects()
	{
		for (L2Skill skill : _skills.values())
		{
			for (L2ClanMember temp : _members.values())
			{
				try
				{
					if (temp.isOnline())
					{
						if (skill.getMinPledgeClass() <= temp.getPlayerInstance().getPledgeClass())
						{
							temp.getPlayerInstance().addSkill(skill, false); // Skill is not saved to player DB
						}
					}
				}
				catch (NullPointerException e)
				{
				}
			}
		}
	}
	
	public void addSkillEffects(L2PcInstance cm)
	{
		if (cm == null)
			return;
		
		for (L2Skill skill : _skills.values())
		{
			// TODO add skills according to members class( in ex. don't add Clan Agillity skill's effect to lower class then Baron)
			if (skill.getMinPledgeClass() <= cm.getPledgeClass())
			{
				cm.addSkill(skill, false); // Skill is not saved to player DB
			}
		}
	}
	
	public void broadcastToOnlineAllyMembers(L2GameServerPacket packet)
	{
		if (getAllyId() == 0)
			return;
		for (L2Clan clan : ClanTable.getInstance().getClans())
		{
			if (clan == null)
				continue;
			
			if (clan.getAllyId() == getAllyId())
			{
				clan.broadcastToOnlineMembers(packet);
			}
		}
	}
	
	public void broadcastCSToOnlineMembers(CreatureSay packet, L2PcInstance broadcaster)
	{
		for (L2ClanMember member : _members.values())
		{
			if (member == null)
				continue;
			
			if (member.isOnline() && !BlockList.isBlocked(member.getPlayerInstance(), broadcaster))
			{
				member.getPlayerInstance().sendPacket(packet);
			}
		}
	}
	
	public void broadcastToOnlineMembers(L2GameServerPacket packet)
	{
		for (L2ClanMember member : _members.values())
		{
			if (member == null)
				continue;
			
			if (member.isOnline())
				member.getPlayerInstance().sendPacket(packet);
		}
	}
	
	public void broadcastToOtherOnlineMembers(L2GameServerPacket packet, L2PcInstance player)
	{
		for (L2ClanMember member : _members.values())
		{
			if (member == null)
				continue;
			
			if (member.isOnline() && member.getPlayerInstance() != player)
			{
				member.getPlayerInstance().sendPacket(packet);
			}
		}
	}
	
	@Override
	public String toString()
	{
		return getName();
	}
	
	public boolean hasCrest()
	{
		return _hasCrest;
	}
	
	public boolean hasCrestLarge()
	{
		return _hasCrestLarge;
	}
	
	public void setHasCrest(boolean flag)
	{
		_hasCrest = flag;
	}
	
	public void setHasCrestLarge(boolean flag)
	{
		_hasCrestLarge = flag;
	}
	
	public ItemContainer getWarehouse()
	{
		return _warehouse;
	}
	
	public boolean isAtWarWith(Integer id)
	{
		if ((_atWarWith != null) && (_atWarWith.size() > 0))
			if (_atWarWith.contains(id))
				return true;
		return false;
	}
	
	public boolean isAtWarAttacker(Integer id)
	{
		if ((_atWarAttackers != null) && (_atWarAttackers.size() > 0))
			if (_atWarAttackers.contains(id))
				return true;
		return false;
	}
	
	public void setEnemyClan(L2Clan clan)
	{
		Integer id = clan.getClanId();
		_atWarWith.add(id);
	}
	
	public void setEnemyClan(Integer clan)
	{
		_atWarWith.add(clan);
	}
	
	public void setAttackerClan(L2Clan clan)
	{
		Integer id = clan.getClanId();
		_atWarAttackers.add(id);
	}
	
	public void setAttackerClan(Integer clan)
	{
		_atWarAttackers.add(clan);
	}
	
	public void deleteEnemyClan(L2Clan clan)
	{
		Integer id = clan.getClanId();
		_atWarWith.remove(id);
	}
	
	public void deleteAttackerClan(L2Clan clan)
	{
		Integer id = clan.getClanId();
		_atWarAttackers.remove(id);
	}
	
	public int getHiredGuards()
	{
		return _hiredGuards;
	}
	
	public void incrementHiredGuards()
	{
		_hiredGuards++;
	}
	
	public boolean isAtWar()
	{
		if ((_atWarWith != null) && (_atWarWith.size() > 0))
			return true;
		return false;
	}
	
	public List<Integer> getWarList()
	{
		return _atWarWith;
	}
	
	public List<Integer> getAttackerList()
	{
		return _atWarAttackers;
	}
	
	public void broadcastClanStatus()
	{
		for (L2PcInstance member : getOnlineMembers())
		{
			if (member == null)
				continue;
			
			member.sendPacket(new PledgeShowMemberListDeleteAll());
			member.sendPacket(new PledgeShowMemberListAll(this, 0));
			
			for (SubPledge sp : getAllSubPledges())
			{
				if (sp == null)
					continue;
				
				member.sendPacket(new PledgeShowMemberListAll(this, sp.getId()));
			}
			
			member.sendPacket(new UserInfo(member));
		}
	}
	
	public void removeSkill(int id)
	{
		L2Skill deleteSkill = null;
		for (L2Skill sk : _skillList)
		{
			if (sk.getId() == id)
			{
				deleteSkill = sk;
				return;
			}
		}
		_skillList.remove(deleteSkill);
	}
	
	public void removeSkill(L2Skill deleteSkill)
	{
		_skillList.remove(deleteSkill);
	}
	
	public List<L2Skill> getSkills()
	{
		return _skillList;
	}
	
	public class SubPledge
	{
		private final int _id;
		private final String _subPledgeName;
		private String _leaderName;
		
		public SubPledge(int id, String name, String leaderName)
		{
			_id = id;
			_subPledgeName = name;
			_leaderName = leaderName;
		}
		
		public int getId()
		{
			return _id;
		}
		
		public String getName()
		{
			return _subPledgeName;
		}
		
		public String getLeaderName()
		{
			return _leaderName;
		}
		
		public void setLeaderName(String leaderName)
		{
			_leaderName = leaderName;
		}
	}
	
	public class RankPrivs
	{
		private final int _rankId;
		private final int _party;// TODO find out what this stuff means and implement it
		private int _rankPrivs;
		
		public RankPrivs(int rank, int party, int privs)
		{
			_rankId = rank;
			_party = party;
			_rankPrivs = privs;
		}
		
		public int getRank()
		{
			return _rankId;
		}
		
		public int getParty()
		{
			return _party;
		}
		
		public int getPrivs()
		{
			return _rankPrivs;
		}
		
		public void setPrivs(int privs)
		{
			_rankPrivs = privs;
		}
	}
	
	private void restoreSubPledges()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			// Retrieve all subpledges of this clan from the database
			PreparedStatement statement = con.prepareStatement("SELECT sub_pledge_id,name,leader_name FROM clan_subpledges WHERE clan_id=?");
			statement.setInt(1, getClanId());
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					int id = rset.getInt("sub_pledge_id");
					String name = rset.getString("name");
					String leaderName = rset.getString("leader_name");
					// Create a SubPledge object for each record
					SubPledge pledge = new SubPledge(id, name, leaderName);
					_subPledges.put(id, pledge);
				}
				rset.close();
			}
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning(L2Clan.class.getName() + ": Could not restore clan sub-units: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public final SubPledge getSubPledge(int pledgeType)
	{
		if (_subPledges == null)
			return null;
		
		return _subPledges.get(pledgeType);
	}
	
	public final SubPledge getSubPledge(String pledgeName)
	{
		if (_subPledges == null)
			return null;
		
		for (SubPledge sp : _subPledges.values())
		{
			if (sp.getName().equalsIgnoreCase(pledgeName))
				return sp;
		}
		return null;
	}
	
	public final SubPledge[] getAllSubPledges()
	{
		if (_subPledges == null)
			return new SubPledge[0];
		
		return _subPledges.values().toArray(new SubPledge[_subPledges.values().size()]);
	}
	
	public SubPledge createSubPledge(L2PcInstance player, int pledgeType, String leaderName, String subPledgeName)
	{
		SubPledge subPledge = null;
		pledgeType = getAvailablePledgeTypes(pledgeType);
		if (pledgeType == 0)
		{
			if (pledgeType == L2Clan.SUBUNIT_ACADEMY)
				player.sendPacket(SystemMessageId.CLAN_HAS_ALREADY_ESTABLISHED_A_CLAN_ACADEMY);
			else
				player.sendMessage("You can't create any more sub-units of this type");
			return null;
		}
		if (_leader.getName().equals(leaderName))
		{
			player.sendMessage("Leader is not correct");
			return null;
		}
		
		// Royal Guard 5000 points per each
		// Order of Knights 10000 points per each
		if (pledgeType != -1 && ((getReputationScore() < 5000 && pledgeType < L2Clan.SUBUNIT_KNIGHT1) || (getReputationScore() < 10000 && pledgeType > L2Clan.SUBUNIT_ROYAL2)))
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW));
			return null;
		}
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO clan_subpledges (clan_id,sub_pledge_id,name,leader_name) VALUES (?,?,?,?)"))
		{
			statement.setInt(1, getClanId());
			statement.setInt(2, pledgeType);
			statement.setString(3, subPledgeName);
			if (pledgeType != -1)
			{
				statement.setString(4, leaderName);
			}
			else
			{
				statement.setString(4, "");
			}
			statement.execute();
			
			subPledge = new SubPledge(pledgeType, subPledgeName, leaderName);
			_subPledges.put(pledgeType, subPledge);
			
			if (pledgeType != -1)
			{
				// Royal Guard 5000 points per each
				// Order of Knights 10000 points per each
				if (pledgeType < L2Clan.SUBUNIT_KNIGHT1)
					setReputationScore(getReputationScore() - 2500, true);
				else
					setReputationScore(getReputationScore() - 2500, true);
			}
			
			if (Config.DEBUG)
			{
				_log.config(L2Clan.class.getName() + ": New sub_clan saved in db: " + getClanId() + "; " + pledgeType);
			}
		}
		catch (SQLException e)
		{
			_log.warning(L2Clan.class.getName() + ": error while saving new sub_clan to db ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
		broadcastToOnlineMembers(new PledgeShowInfoUpdate(_leader.getClan()));
		broadcastToOnlineMembers(new PledgeReceiveSubPledgeCreated(subPledge));
		return subPledge;
	}
	
	public int getAvailablePledgeTypes(int pledgeType)
	{
		if (_subPledges.get(pledgeType) != null)
		{
			// _log.warning(L2Clan.class.getName() + ": found sub-unit with id: "+pledgeType);
			switch (pledgeType)
			{
				case SUBUNIT_ACADEMY:
					return 0;
				case SUBUNIT_ROYAL1:
					pledgeType = getAvailablePledgeTypes(SUBUNIT_ROYAL2);
					break;
				case SUBUNIT_ROYAL2:
					return 0;
				case SUBUNIT_KNIGHT1:
					pledgeType = getAvailablePledgeTypes(SUBUNIT_KNIGHT2);
					break;
				case SUBUNIT_KNIGHT2:
					pledgeType = getAvailablePledgeTypes(SUBUNIT_KNIGHT3);
					break;
				case SUBUNIT_KNIGHT3:
					pledgeType = getAvailablePledgeTypes(SUBUNIT_KNIGHT4);
					break;
				case SUBUNIT_KNIGHT4:
					return 0;
			}
		}
		return pledgeType;
	}
	
	public void updateSubPledgeInDB(int pledgeType)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE clan_subpledges SET leader_name=? WHERE clan_id=? AND sub_pledge_id=?"))
		{
			statement.setString(1, getSubPledge(pledgeType).getLeaderName());
			statement.setInt(2, getClanId());
			statement.setInt(3, pledgeType);
			statement.execute();
			
			if (Config.DEBUG)
			{
				_log.config(L2Clan.class.getName() + ": New subpledge leader saved in db: " + getClanId());
			}
		}
		catch (SQLException e)
		{
			_log.warning(L2Clan.class.getName() + ": error while saving new clan leader to db ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	private void restoreRankPrivs()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			// Retrieve all skills of this L2PcInstance from the database
			PreparedStatement statement = con.prepareStatement("SELECT * FROM clan_privs WHERE clan_id=?");
			statement.setInt(1, getClanId());
			// _log.warning(L2Clan.class.getName() + ": clanPrivs restore for ClanId : "+getClanId());
			try (ResultSet rset = statement.executeQuery())
			{
				// Go though the recordset of this SQL query
				while (rset.next())
				{
					int rank = rset.getInt("rank");
					// int party = rset.getInt("party");
					int privileges = rset.getInt("privs");
					// Create a SubPledge object for each record
					if (rank == -1)
					{
						continue;
					}
					_privs.get(rank).setPrivs(privileges);
				}
				rset.close();
			}
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning(L2Clan.class.getName() + ": Could not restore clan privs by rank: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public void initializePrivs()
	{
		RankPrivs privs;
		for (int i = 1; i < 10; i++)
		{
			privs = new RankPrivs(i, 0, CP_NOTHING);
			_privs.put(i, privs);
		}
		
	}
	
	public int getRankPrivs(int rank)
	{
		return _privs.get(rank) != null ? _privs.get(rank).getPrivs() : CP_NOTHING;
	}
	
	public void setRankPrivs(int rank, int privs)
	{
		if (_privs.get(rank) != null)
		{
			_privs.get(rank).setPrivs(privs);
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("INSERT INTO clan_privs (clan_id,rank,party,privs) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE privs=?"))
			{
				// Retrieve all skills of this L2PcInstance from the database
				
				statement.setInt(1, getClanId());
				statement.setInt(2, rank);
				statement.setInt(3, 0);
				statement.setInt(4, privs);
				statement.setInt(5, privs);
				statement.execute();
			}
			catch (SQLException e)
			{
				_log.warning(L2Clan.class.getName() + ": Could not store clan privs for rank: ");
				if (Config.DEVELOPER)
					e.printStackTrace();
			}
			for (L2ClanMember cm : getMembers())
			{
				if (cm.isOnline())
					if (cm.getPowerGrade() == rank)
						if (cm.getPlayerInstance() != null)
						{
							cm.getPlayerInstance().setClanPrivileges(privs);
							cm.getPlayerInstance().sendPacket(new UserInfo(cm.getPlayerInstance()));
						}
			}
			broadcastClanStatus();
		}
		else
		{
			_privs.put(rank, new RankPrivs(rank, 0, privs));
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("INSERT INTO clan_privs (clan_id,rank,party,privs) VALUES (?,?,?,?)"))
			{
				// Retrieve all skills of this L2PcInstance from the database
				statement.setInt(1, getClanId());
				statement.setInt(2, rank);
				statement.setInt(3, 0);
				statement.setInt(4, privs);
				statement.execute();
			}
			catch (SQLException e)
			{
				_log.warning(L2Clan.class.getName() + ": Could not create new rank and store clan privs for rank: ");
				if (Config.DEVELOPER)
					e.printStackTrace();
			}
		}
	}
	
	public final RankPrivs[] getAllRankPrivs()
	{
		if (_privs == null)
			return new RankPrivs[0];
		
		return _privs.values().toArray(new RankPrivs[_privs.values().size()]);
	}
	
	public int getLeaderSubPledge(String name)
	{
		int id = 0;
		for (SubPledge sp : _subPledges.values())
		{
			if (sp.getLeaderName() == null)
			{
				continue;
			}
			if (sp.getLeaderName().equals(name))
			{
				id = sp.getId();
			}
		}
		return id;
	}
	
	public void setReputationScore(int value, boolean save)
	{
		if (_reputationScore >= 0 && value < 0)
		{
			broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.REPUTATION_POINTS_0_OR_LOWER_CLAN_SKILLS_DEACTIVATED));
			L2Skill[] skills = getAllSkills();
			for (L2ClanMember member : _members.values())
			{
				if (member.isOnline() && member.getPlayerInstance() != null)
				{
					for (L2Skill sk : skills)
					{
						member.getPlayerInstance().removeSkill(sk, false);
					}
				}
			}
		}
		else if (_reputationScore < 0 && value >= 0)
		{
			broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_SKILLS_WILL_BE_ACTIVATED_SINCE_REPUTATION_IS_0_OR_HIGHER));
			L2Skill[] skills = getAllSkills();
			for (L2ClanMember member : _members.values())
			{
				if (member.isOnline() && member.getPlayerInstance() != null)
				{
					for (L2Skill sk : skills)
					{
						if (sk.getMinPledgeClass() <= member.getPlayerInstance().getPledgeClass())
						{
							member.getPlayerInstance().addSkill(sk, false);
						}
					}
				}
			}
		}
		
		_reputationScore = value;
		if (_reputationScore > 100000000)
		{
			_reputationScore = 100000000;
		}
		if (_reputationScore < -100000000)
		{
			_reputationScore = -100000000;
		}
		if (save)
		{
			updateClanInDB();
		}
	}
	
	public int getReputationScore()
	{
		return _reputationScore;
	}
	
	public void setRank(int rank)
	{
		_rank = rank;
	}
	
	public int getRank()
	{
		return _rank;
	}
	
	public int getAuctionBiddedAt()
	{
		return _auctionBiddedAt;
	}
	
	public void setAuctionBiddedAt(int id, boolean storeInDb)
	{
		_auctionBiddedAt = id;
		
		if (storeInDb)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET auction_bid_at=? WHERE clan_id=?"))
			{
				statement.setInt(1, id);
				statement.setInt(2, getClanId());
				statement.execute();
			}
			catch (SQLException e)
			{
				_log.warning(L2Clan.class.getName() + ": Could not store auction for clan: ");
				if (Config.DEVELOPER)
					e.printStackTrace();
			}
		}
	}
	
	public boolean checkClanJoinCondition(L2PcInstance activeChar, L2PcInstance target, int pledgeType)
	{
		if (activeChar == null)
			return false;
		if ((activeChar.getClanPrivileges() & L2Clan.CP_CL_JOIN_CLAN) != L2Clan.CP_CL_JOIN_CLAN)
		{
			activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return false;
		}
		if (target == null)
		{
			activeChar.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
			return false;
		}
		
		if (activeChar.getObjectId() == target.getObjectId())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_INVITE_YOURSELF);
			return false;
		}
		if (getCharPenaltyExpiryTime() > System.currentTimeMillis())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_MUST_WAIT_BEFORE_ACCEPTING_A_NEW_MEMBER);
			sm.addString(target.getName());
			activeChar.sendPacket(sm);
			sm = null;
			return false;
		}
		if (target.getClanId() != 0)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_WORKING_WITH_ANOTHER_CLAN);
			sm.addString(target.getName());
			activeChar.sendPacket(sm);
			sm = null;
			return false;
		}
		if (target.getClanId() != 0)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_WORKING_WITH_ANOTHER_CLAN);
			sm.addString(target.getName());
			activeChar.sendPacket(sm);
			sm = null;
			return false;
		}
		if (target.getClanJoinExpiryTime() > System.currentTimeMillis())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MUST_WAIT_BEFORE_JOINING_ANOTHER_CLAN);
			sm.addString(target.getName());
			activeChar.sendPacket(sm);
			sm = null;
			return false;
		}
		if ((target.getLevel() > 40 || target.getClassId().level() >= 2) && pledgeType == -1)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DOESNOT_MEET_REQUIREMENTS_TO_JOIN_ACADEMY);
			sm.addString(target.getName());
			activeChar.sendPacket(sm);
			sm = null;
			activeChar.sendPacket(SystemMessageId.ACADEMY_REQUIREMENTS);
			return false;
		}
		if (getSubPledgeMembersCount(pledgeType) >= getMaxNrOfMembers(pledgeType))
		{
			if (pledgeType == 0)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CLAN_IS_FULL);
				sm.addString(getName());
				activeChar.sendPacket(sm);
				sm = null;
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.SUBCLAN_IS_FULL);
			}
			return false;
		}
		return true;
	}
	
	public boolean checkAllyJoinCondition(L2PcInstance activeChar, L2PcInstance target)
	{
		if (activeChar == null)
			return false;
		if (activeChar.getAllyId() == 0 || !activeChar.isClanLeader() || activeChar.getClanId() != activeChar.getAllyId())
		{
			activeChar.sendPacket(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER);
			return false;
		}
		L2Clan leaderClan = activeChar.getClan();
		if (leaderClan.getAllyPenaltyExpiryTime() > System.currentTimeMillis())
		{
			if (leaderClan.getAllyPenaltyType() == PENALTY_TYPE_DISMISS_CLAN)
			{
				activeChar.sendPacket(SystemMessageId.CANT_INVITE_CLAN_WITHIN_1_DAY);
				return false;
			}
		}
		if (target == null)
		{
			activeChar.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
			return false;
		}
		if (activeChar.getObjectId() == target.getObjectId())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_INVITE_YOURSELF);
			return false;
		}
		if (target.getClan() == null)
		{
			activeChar.sendPacket(SystemMessageId.TARGET_MUST_BE_IN_CLAN);
			return false;
		}
		if (!target.isClanLeader())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_IS_NOT_A_CLAN_LEADER);
			sm.addString(target.getName());
			activeChar.sendPacket(sm);
			sm = null;
			return false;
		}
		L2Clan targetClan = target.getClan();
		if (target.getAllyId() != 0)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CLAN_ALREADY_MEMBER_OF_S2_ALLIANCE);
			sm.addString(targetClan.getName());
			sm.addString(targetClan.getAllyName());
			activeChar.sendPacket(sm);
			sm = null;
			return false;
		}
		if (targetClan.getAllyPenaltyExpiryTime() > System.currentTimeMillis())
		{
			if (targetClan.getAllyPenaltyType() == PENALTY_TYPE_CLAN_LEAVED)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANT_ENTER_ALLIANCE_WITHIN_1_DAY);
				sm.addString(target.getClan().getName());
				sm.addString(target.getClan().getAllyName());
				activeChar.sendPacket(sm);
				sm = null;
				return false;
			}
			if (targetClan.getAllyPenaltyType() == PENALTY_TYPE_CLAN_DISMISSED)
			{
				activeChar.sendPacket(SystemMessageId.CANT_ENTER_ALLIANCE_WITHIN_1_DAY);
				return false;
			}
		}
		if (activeChar.isInsideZone(ZoneId.SIEGE) && target.isInsideZone(ZoneId.SIEGE))
		{
			activeChar.sendPacket(SystemMessageId.OPPOSING_CLAN_IS_PARTICIPATING_IN_SIEGE);
			return false;
		}
		if (leaderClan.isAtWarWith(targetClan.getClanId()))
		{
			activeChar.sendPacket(SystemMessageId.MAY_NOT_ALLY_CLAN_BATTLE);
			return false;
		}
		
		int numOfClansInAlly = 0;
		for (L2Clan clan : ClanTable.getInstance().getClans())
		{
			if (clan.getAllyId() == activeChar.getAllyId())
			{
				++numOfClansInAlly;
			}
		}
		if (numOfClansInAlly >= Config.ALT_MAX_NUM_OF_CLANS_IN_ALLY)
		{
			activeChar.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_LIMIT);
			return false;
		}
		
		return true;
	}
	
	public long getAllyPenaltyExpiryTime()
	{
		return _allyPenaltyExpiryTime;
	}
	
	public int getAllyPenaltyType()
	{
		return _allyPenaltyType;
	}
	
	public void setAllyPenaltyExpiryTime(long expiryTime, int penaltyType)
	{
		_allyPenaltyExpiryTime = expiryTime;
		_allyPenaltyType = penaltyType;
	}
	
	public long getCharPenaltyExpiryTime()
	{
		return _charPenaltyExpiryTime;
	}
	
	public void setCharPenaltyExpiryTime(long time)
	{
		_charPenaltyExpiryTime = time;
	}
	
	public long getDissolvingExpiryTime()
	{
		return _dissolvingExpiryTime;
	}
	
	public void setDissolvingExpiryTime(long time)
	{
		_dissolvingExpiryTime = time;
	}
	
	public void createAlly(L2PcInstance player, String allyName)
	{
		if (null == player)
			return;
		
		if (Config.DEBUG)
		{
			_log.config(L2Clan.class.getName() + ": " + player.getObjectId() + "(" + player.getName() + ") requested ally creation from ");
		}
		
		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.ONLY_CLAN_LEADER_CREATE_ALLIANCE);
			return;
		}
		if (getAllyId() != 0)
		{
			player.sendPacket(SystemMessageId.ALREADY_JOINED_ALLIANCE);
			return;
		}
		if (getLevel() < 5)
		{
			player.sendPacket(SystemMessageId.TO_CREATE_AN_ALLY_YOU_CLAN_MUST_BE_LEVEL_5_OR_HIGHER);
			return;
		}
		if (getAllyPenaltyExpiryTime() > System.currentTimeMillis())
		{
			if (getAllyPenaltyType() == L2Clan.PENALTY_TYPE_DISSOLVE_ALLY)
			{
				player.sendPacket(SystemMessageId.CANT_CREATE_ALLIANCE_10_DAYS_DISOLUTION);
				return;
			}
		}
		if (getDissolvingExpiryTime() > System.currentTimeMillis())
		{
			player.sendPacket(SystemMessageId.YOU_MAY_NOT_CREATE_ALLY_WHILE_DISSOLVING);
			return;
		}
		if (!Util.isAlphaNumeric(allyName))
		{
			player.sendPacket(SystemMessageId.INCORRECT_ALLIANCE_NAME);
			return;
		}
		if (allyName.length() > 16 || allyName.length() < 2)
		{
			player.sendPacket(SystemMessageId.INCORRECT_ALLIANCE_NAME_LENGTH);
			return;
		}
		if (ClanTable.getInstance().isAllyExists(allyName))
		{
			player.sendPacket(SystemMessageId.ALLIANCE_ALREADY_EXISTS);
			return;
		}
		
		setAllyId(getClanId());
		setAllyName(allyName.trim());
		setAllyPenaltyExpiryTime(0, 0);
		updateClanInDB();
		
		player.sendPacket(new UserInfo(player));
		player.sendMessage("Alliance " + allyName + " has been created.");
	}
	
	public void dissolveAlly(L2PcInstance player)
	{
		if (getAllyId() == 0)
		{
			player.sendPacket(SystemMessageId.NO_CURRENT_ALLIANCES);
			return;
		}
		if (!player.isClanLeader() || getClanId() != getAllyId())
		{
			player.sendPacket(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER);
			return;
		}
		if (player.isInsideZone(ZoneId.SIEGE))
		{
			player.sendPacket(SystemMessageId.CANNOT_DISSOLVE_ALLY_WHILE_IN_SIEGE);
			return;
		}
		
		broadcastToOnlineAllyMembers(SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_DISOLVED));
		
		long currentTime = System.currentTimeMillis();
		for (L2Clan clan : ClanTable.getInstance().getClans())
		{
			if (clan.getAllyId() == getAllyId() && clan.getClanId() != getClanId())
			{
				clan.setAllyId(0);
				clan.setAllyName(null);
				clan.setAllyPenaltyExpiryTime(0, 0);
				clan.updateClanInDB();
			}
		}
		
		setAllyId(0);
		setAllyName(null);
		setAllyPenaltyExpiryTime(currentTime + Config.ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED * 86400000L, L2Clan.PENALTY_TYPE_DISSOLVE_ALLY); // 24*60*60*1000 = 86400000
		updateClanInDB();
		
		// The clan leader should take the XP penalty of a full death.
		player.deathPenalty(false, false, false);
	}
	
	public void levelUpClan(L2PcInstance player)
	{
		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		if (System.currentTimeMillis() < getDissolvingExpiryTime())
		{
			player.sendPacket(SystemMessageId.CANNOT_RISE_LEVEL_WHILE_DISSOLUTION_IN_PROGRESS);
			return;
		}
		
		boolean increaseClanLevel = false;
		
		switch (getLevel())
		{
			case 0:
			{
				// upgrade to 1
				if (player.getSp() >= 30000 && player.getAdena() >= 650000)
				{
					if (player.reduceAdena("ClanLvl", 650000, player.getTarget(), true))
					{
						player.setSp(player.getSp() - 30000);
						SystemMessage sp = SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1);
						sp.addNumber(30000);
						player.sendPacket(sp);
						sp = null;
						increaseClanLevel = true;
					}
				}
				break;
			}
			case 1:
			{
				// upgrade to 2
				if (player.getSp() >= 150000 && player.getAdena() >= 2500000)
				{
					if (player.reduceAdena("ClanLvl", 2500000, player.getTarget(), true))
					{
						player.setSp(player.getSp() - 150000);
						SystemMessage sp = SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1);
						sp.addNumber(150000);
						player.sendPacket(sp);
						sp = null;
						increaseClanLevel = true;
					}
				}
				break;
			}
			case 2:
			{
				// upgrade to 3
				if (player.getSp() >= 500000 && player.getInventory().getItemByItemId(1419) != null)
				{
					// itemid 1419 == proof of blood
					if (player.destroyItemByItemId("ClanLvl", 1419, 1, player.getTarget(), false))
					{
						player.setSp(player.getSp() - 500000);
						SystemMessage sp = SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1);
						sp.addNumber(500000);
						player.sendPacket(sp);
						sp = null;
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
						sm.addItemName(1419);
						sm.addNumber(1);
						player.sendPacket(sm);
						sm = null;
						increaseClanLevel = true;
					}
				}
				break;
			}
			case 3:
			{
				// upgrade to 4
				if (player.getSp() >= 1400000 && player.getInventory().getItemByItemId(3874) != null)
				{
					// itemid 3874 == proof of alliance
					if (player.destroyItemByItemId("ClanLvl", 3874, 1, player.getTarget(), false))
					{
						player.setSp(player.getSp() - 1400000);
						SystemMessage sp = SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1);
						sp.addNumber(1400000);
						player.sendPacket(sp);
						sp = null;
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
						sm.addItemName(3874);
						sm.addNumber(1);
						player.sendPacket(sm);
						sm = null;
						increaseClanLevel = true;
					}
				}
				break;
			}
			case 4:
			{
				// upgrade to 5
				if (player.getSp() >= 3500000 && player.getInventory().getItemByItemId(3870) != null)
				{
					// itemid 3870 == proof of aspiration
					if (player.destroyItemByItemId("ClanLvl", 3870, 1, player.getTarget(), false))
					{
						player.setSp(player.getSp() - 3500000);
						SystemMessage sp = SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1);
						sp.addNumber(3500000);
						player.sendPacket(sp);
						sp = null;
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
						sm.addItemName(3870);
						sm.addNumber(1);
						player.sendPacket(sm);
						sm = null;
						increaseClanLevel = true;
					}
				}
				break;
			}
			case 5:
				if (getReputationScore() >= 10000 && getMembersCount() >= 30)
				{
					setReputationScore(getReputationScore() - 10000, true);
					SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
					cr.addNumber(10000);
					player.sendPacket(cr);
					cr = null;
					increaseClanLevel = true;
				}
				break;
			
			case 6:
				if (getReputationScore() >= 20000 && getMembersCount() >= 80)
				{
					setReputationScore(getReputationScore() - 20000, true);
					SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
					cr.addNumber(20000);
					player.sendPacket(cr);
					cr = null;
					increaseClanLevel = true;
				}
				break;
			case 7:
				if (getReputationScore() >= 40000 && getMembersCount() >= 120)
				{
					setReputationScore(getReputationScore() - 40000, true);
					SystemMessage cr = SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
					cr.addNumber(40000);
					player.sendPacket(cr);
					cr = null;
					increaseClanLevel = true;
				}
				break;
			default:
				return;
		}
		
		if (!increaseClanLevel)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_INCREASE_CLAN_LEVEL);
			player.sendPacket(sm);
			return;
		}
		
		// the player should know that he has less sp now :p
		StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.SP, player.getSp());
		player.sendPacket(su);
		
		ItemList il = new ItemList(player, false);
		player.sendPacket(il);
		
		changeLevel(getLevel() + 1);
	}
	
	public void changeLevel(int level)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET clan_level=? WHERE clan_id=?"))
		{
			statement.setInt(1, level);
			statement.setInt(2, getClanId());
			statement.execute();
		}
		catch (SQLException e)
		{
			_log.warning(L2Clan.class.getSimpleName() + ": could not increase clan level:");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
		
		setLevel(level);
		
		if (getLeader().isOnline())
		{
			L2PcInstance leader = getLeader().getPlayerInstance();
			if (3 < level)
				SiegeManager.addSiegeSkills(leader);
			else if (4 > level)
				SiegeManager.removeSiegeSkills(leader);
			if (4 < level)
				leader.sendPacket(SystemMessageId.CLAN_CAN_ACCUMULATE_CLAN_REPUTATION_POINTS);
		}
		
		broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_LEVEL_INCREASED));
		broadcastToOnlineMembers(new PledgeShowInfoUpdate(this));
	}
	
	public int getSiegeKills()
	{
		return _siegeKills != null ? _siegeKills.get() : 0;
	}
	
	public int getSiegeDeaths()
	{
		return _siegeDeaths != null ? _siegeDeaths.get() : 0;
	}
	
	public int addSiegeKill()
	{
		if (_siegeKills == null)
		{
			synchronized (this)
			{
				if (_siegeKills == null)
				{
					_siegeKills = new AtomicInteger();
				}
			}
		}
		return _siegeKills.incrementAndGet();
	}
	
	public int addSiegeDeath()
	{
		if (_siegeDeaths == null)
		{
			synchronized (this)
			{
				if (_siegeDeaths == null)
				{
					_siegeDeaths = new AtomicInteger();
				}
			}
		}
		return _siegeDeaths.incrementAndGet();
	}
	
	public void clearSiegeKills()
	{
		if (_siegeKills != null)
		{
			_siegeKills.set(0);
		}
	}
	
	public void clearSiegeDeaths()
	{
		if (_siegeDeaths != null)
		{
			_siegeDeaths.set(0);
		}
	}
	
	private void setReputationScore(int value)
	{
		// That check is used to see if it needs a refresh.
		final boolean needRefresh = (_reputationScore > 0 && value <= 0) || (value > 0 && _reputationScore <= 0);
		
		// Store the online members (used in 2 positions, can't merge)
		final L2PcInstance[] members = getOnlineMembers();
		
		_reputationScore = Math.min(100000000, Math.max(-100000000, value));
		
		// Refresh clan windows of all clan members, and reward/remove skills.
		if (needRefresh)
		{
			final L2Skill[] skills = getAllSkills();
			
			if (_reputationScore <= 0)
			{
				for (L2PcInstance member : members)
				{
					member.sendPacket(SystemMessageId.REPUTATION_POINTS_0_OR_LOWER_CLAN_SKILLS_DEACTIVATED);
					
					for (L2Skill sk : skills)
						member.removeSkill(sk, false);
					
					member.sendSkillList();
				}
			}
			else
			{
				for (L2PcInstance member : members)
				{
					member.sendPacket(SystemMessageId.CLAN_SKILLS_WILL_BE_ACTIVATED_SINCE_REPUTATION_IS_0_OR_HIGHER);
					
					for (L2Skill sk : skills)
					{
						if (sk.getMinPledgeClass() <= member.getPledgeClass())
							member.addSkill(sk, false);
					}
					
					member.sendSkillList();
				}
			}
		}
		
		// Points reputation update for all.
		final PledgeShowInfoUpdate infoRefresh = new PledgeShowInfoUpdate(this);
		for (L2PcInstance member : members)
			member.sendPacket(infoRefresh);
		
		// Save the amount on the database.
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET reputation_score=? WHERE clan_id=?");
			statement.setInt(1, _reputationScore);
			statement.setInt(2, _clanId);
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning(L2Clan.class.getSimpleName() + ": Exception on updateClanScoreInDb(): ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public synchronized void addReputationScore(int value)
	{
		setReputationScore(_reputationScore + value);
	}
	
	public synchronized void takeReputationScore(int value)
	{
		setReputationScore(_reputationScore - value);
	}
	
	public boolean isSubPledgeLeader(String name)
	{
		for (SubPledge sp : getAllSubPledges())
		{
			if (sp.getLeaderName() == name)
				return true;
		}
		
		return false;
	}
}