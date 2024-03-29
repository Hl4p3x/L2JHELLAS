package com.l2jhellas.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.instancemanager.SiegeManager;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class L2ClanMember
{
	protected static final Logger _log = Logger.getLogger(L2ClanMember.class.getName());
	
	private final L2Clan _clan;
	private int _objectId;
	private String _name;
	private String _title;
	private int _powerGrade;
	private int _level;
	private int _classId;
	private L2PcInstance _player;
	private int _pledgeType;
	private int _apprentice;
	private int _sponsor;
	
	public L2ClanMember(L2Clan clan, String name, int level, int classId, int objectId, int pledgeType, int powerGrade, String title)
	{
		if (clan == null)
			throw new IllegalArgumentException("Can not create a ClanMember with a null clan.");
		_clan = clan;
		_name = name;
		_level = level;
		_classId = classId;
		_objectId = objectId;
		_powerGrade = powerGrade;
		_title = title;
		_pledgeType = pledgeType;
		_apprentice = 0;
		_sponsor = 0;
		
	}
	
	public L2ClanMember(L2PcInstance player)
	{
		if (player.getClan() == null)
			throw new IllegalArgumentException("Can not create a ClanMember if player has a null clan.");
		_clan = player.getClan();
		_player = player;
		_name = _player.getName();
		_level = _player.getLevel();
		_classId = _player.getClassId().getId();
		_objectId = _player.getObjectId();
		_powerGrade = _player.getPowerGrade();
		_pledgeType = _player.getPledgeType();
		_title = _player.getTitle();
		_apprentice = 0;
		_sponsor = 0;
	}
	
	public void setPlayerInstance(L2PcInstance player)
	{
		if (player == null && _player != null)
		{
			// this is here to keep the data when the player logs off
			_name = _player.getName();
			_level = _player.getLevel();
			_classId = _player.getClassId().getId();
			_objectId = _player.getObjectId();
			_powerGrade = _player.getPowerGrade();
			_pledgeType = _player.getPledgeType();
			_title = _player.getTitle();
			_apprentice = _player.getApprentice();
			_sponsor = _player.getSponsor();
		}
		
		if (player != null)
		{
			if (_clan.getLevel() > 3 && player.isClanLeader())
				SiegeManager.addSiegeSkills(player);
			
			if (_clan.getReputationScore() >= 0)
			{
				Collection<L2Skill> skills = _clan.getClanSkills().values();

				for (L2Skill sk : skills)
				{
					if (sk.getMinPledgeClass() <= player.getPledgeClass())
						player.addSkill(sk, false);
				}
			}
		}
		
		_player = player;
	}
	
	public L2PcInstance getPlayerInstance()
	{
		return _player;
	}
	
	public boolean isOnline()
	{
		if (_player == null)
			return false;
		if (_player.getClient() == null)
			return false;
		return true;
	}
	
	public int getClassId()
	{
		if (_player != null)
		{
			return _player.getClassId().getId();
		}
		return _classId;
	}
	
	public int getLevel()
	{
		if (_player != null)
		{
			return _player.getLevel();
		}
		return _level;
	}
	
	public String getName()
	{
		if (_player != null)
		{
			return _player.getName();
		}
		return _name;
	}
	
	public int getObjectId()
	{
		if (_player != null)
		{
			return _player.getObjectId();
		}
		return _objectId;
	}
	
	public String getTitle()
	{
		if (_player != null)
		{
			return _player.getTitle();
		}
		return _title;
	}
	
	public int getPledgeType()
	{
		if (_player != null)
		{
			return _player.getPledgeType();
		}
		return _pledgeType;
	}
	
	public void setPledgeType(int pledgeType)
	{
		_pledgeType = pledgeType;
		if (_player != null)
		{
			_player.setPledgeType(pledgeType);
		}
		else
		{
			// db save if char not logged in
			updatePledgeType();
		}
	}
	
	public void updatePledgeType()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET subpledge=? WHERE obj_Id=?");
			statement.setLong(1, _pledgeType);
			statement.setInt(2, getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning(L2ClanMember.class.getName() + ": could not set char power_grade:");
		}
	}
	
	public int getPowerGrade()
	{
		if (_player != null)
			return _player.getPowerGrade();
		return _powerGrade;
	}
	
	public void setPowerGrade(int powerGrade)
	{
		_powerGrade = powerGrade;
		if (_player != null)
		{
			_player.setPowerGrade(powerGrade);
		}
		else
		{
			// db save if char not logged in
			updatePowerGrade();
		}
	}
	
	public void updatePowerGrade()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET power_grade=? WHERE obj_Id=?");
			statement.setLong(1, _powerGrade);
			statement.setInt(2, getObjectId());
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning(L2ClanMember.class.getName() + " Could not set char power_grade:");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public void initApprenticeAndSponsor(int apprenticeID, int sponsorID)
	{
		_apprentice = apprenticeID;
		_sponsor = sponsorID;
	}
	
	public int getSponsor()
	{
		return _player != null ? _player.getSponsor():_sponsor;
	}
	
	public int getApprentice()
	{

		return _player != null ? _player.getApprentice() : _apprentice;
	}
	
	public String getApprenticeOrSponsorName()
	{
		if (_player != null)
		{
			_apprentice = _player.getApprentice();
			_sponsor = _player.getSponsor();
		}
		
		if (_apprentice != 0)
		{
			L2ClanMember apprentice = _clan.getClanMember(_apprentice);
			if (apprentice != null)
				return apprentice.getName();
			
			return "Error";
		}
		if (_sponsor != 0)
		{
			L2ClanMember sponsor = _clan.getClanMember(_sponsor);
			if (sponsor != null)
				return sponsor.getName();
			
			return "Error";
		}
		return "";
	}
	
	public L2Clan getClan()
	{
		return _clan;
	}
	
	public int calculatePledgeClass(L2PcInstance player)
	{
		int pledgeClass = 0;
		
		if (player !=null && player.getClan() != null)
		{
			switch (player.getClan().getLevel())
			{
				case 4:
					if (player.isClanLeader())
						pledgeClass = 3;
					break;
				case 5:
					if (player.isClanLeader())
						pledgeClass = 4;
					else
						pledgeClass = 2;
					break;
				case 6:
					switch (player.getPledgeType())
					{
						case -1:
							pledgeClass = 1;
							break;
						case 100:
						case 200:
							pledgeClass = 2;
							break;
						case 0:
							if (player.isClanLeader())
								pledgeClass = 5;
							else
								switch (player.getClan().getLeaderSubPledge(player.getName()))
								{
									case 100:
									case 200:
										pledgeClass = 4;
										break;
									case -1:
									default:
										pledgeClass = 3;
										break;
								}
							break;
					}
					break;
				case 7:
					switch (player.getPledgeType())
					{
						case -1:
							pledgeClass = 1;
							break;
						case 100:
						case 200:
							pledgeClass = 3;
							break;
						case 1001:
						case 1002:
						case 2001:
						case 2002:
							pledgeClass = 2;
							break;
						case 0:
							if (player.isClanLeader())
								pledgeClass = 7;
							else
								switch (player.getClan().getLeaderSubPledge(player.getName()))
								{
									case 100:
									case 200:
										pledgeClass = 6;
										break;
									case 1001:
									case 1002:
									case 2001:
									case 2002:
										pledgeClass = 5;
										break;
									case -1:
									default:
										pledgeClass = 4;
										break;
								}
							break;
					}
					break;
				case 8:
					switch (player.getPledgeType())
					{
						case -1:
							pledgeClass = 1;
							break;
						case 100:
						case 200:
							pledgeClass = 4;
							break;
						case 1001:
						case 1002:
						case 2001:
						case 2002:
							pledgeClass = 3;
							break;
						case 0:
							if (player.isClanLeader())
								pledgeClass = 8;
							else
								switch (player.getClan().getLeaderSubPledge(player.getName()))
								{
									case 100:
									case 200:
										pledgeClass = 7;
										break;
									case 1001:
									case 1002:
									case 2001:
									case 2002:
										pledgeClass = 6;
										break;
									case -1:
									default:
										pledgeClass = 5;
										break;
								}
							break;
					}
					break;
				default:
					pledgeClass = 1;
					break;
			}
		}
		return pledgeClass;
	}
	
	public void saveApprenticeAndSponsor(int apprentice, int sponsor)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET apprentice=?,sponsor=? WHERE obj_Id=?");
			statement.setInt(1, apprentice);
			statement.setInt(2, sponsor);
			statement.setInt(3, getObjectId());
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning(L2ClanMember.class.getName() + " could not set apprentice/sponsor:");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
}