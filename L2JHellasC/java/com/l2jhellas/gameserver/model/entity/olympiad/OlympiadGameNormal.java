package com.l2jhellas.gameserver.model.entity.olympiad;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.model.Auction;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.model.zone.type.L2OlympiadStadiumZone;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ExOlympiadUserInfo;
import com.l2jhellas.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.util.Rnd;
import com.l2jhellas.util.database.L2DatabaseFactory;

abstract public class OlympiadGameNormal extends AbstractOlympiadGame
{
	protected static final String INSERT_OLYMPIAD_FIGHTS = "INSERT INTO olympiad_fights (charOneId, charTwoId, charOneClass, charTwoClass, winner, start, time, classed) values(?,?,?,?,?,?,?,?)";
	
	protected int _damageP1 = 0;
	protected int _damageP2 = 0;
	
	protected Participant _playerOne;
	protected Participant _playerTwo;
	
	protected OlympiadGameNormal(int id, Participant[] opponents)
	{
		super(id);
		
		_playerOne = opponents[0];
		_playerTwo = opponents[1];
		
		_playerOne.player.setOlympiadGameId(id);
		_playerTwo.player.setOlympiadGameId(id);
	}
	
	protected static final Participant[] createListOfParticipants(List<Integer> list)
	{
		if (list == null || list.isEmpty() || list.size() < 2)
			return null;
		
		int playerOneObjectId = 0;
		L2PcInstance playerOne = null;
		L2PcInstance playerTwo = null;
		
		while (list.size() > 1)
		{
			playerOneObjectId = list.remove(Rnd.get(list.size()));
			playerOne = L2World.getInstance().getPlayer(playerOneObjectId);
			if (playerOne == null || !playerOne.isOnline())
				continue;
			
			playerTwo = L2World.getInstance().getPlayer(list.remove(Rnd.get(list.size())));
			if (playerTwo == null || !playerTwo.isOnline())
			{
				list.add(playerOneObjectId);
				continue;
			}
			
			Participant[] result = new Participant[2];
			result[0] = new Participant(playerOne, 1);
			result[1] = new Participant(playerTwo, 2);
			
			return result;
		}
		return null;
	}
	
	@Override
	public final boolean containsParticipant(int playerId)
	{
		return _playerOne.objectId == playerId || _playerTwo.objectId == playerId;
	}
	
	@Override
	protected final boolean portPlayersToArena(Location location)
	{		
		if (_playerOne.player == null || _playerTwo.player == null || !_playerOne.player.isOnline() || !_playerTwo.player.isOnline())
			return false;
		
		boolean result = true;
		
		Location loc1 = new Location(location.getX() + 900, location.getY(), location.getZ());
		Location loc2 = new Location(location.getX() - 900, location.getY(), location.getZ());
		
		try
		{
			result &= portPlayerToArena(_playerOne, loc1, _stadiumID);
			result &= portPlayerToArena(_playerTwo, loc2, _stadiumID);
			hpsShow();
		}
		catch (Exception e)
		{
			return false;
		}
		return result;
	}
	
	@Override
	public final void sendOlympiadInfo(L2Character player)
	{
		player.sendPacket(new ExOlympiadUserInfo(_playerOne.player, 1));
		player.sendPacket(new ExOlympiadUserInfo(_playerTwo.player, 1));
	}
	
	@Override
	public final void broadcastOlympiadInfo(L2OlympiadStadiumZone stadium)
	{
		broadcastPacket(new ExOlympiadUserInfo(_playerOne.player, 2));
		broadcastPacket(new ExOlympiadUserInfo(_playerTwo.player, 2));
	}
	
	@Override
	protected final void broadcastPacket(L2GameServerPacket packet)
	{
		_playerOne.updatePlayer();
		if (_playerOne.player != null)
			_playerOne.player.sendPacket(packet);
		
		_playerTwo.updatePlayer();
		if (_playerTwo.player != null)
			_playerTwo.player.sendPacket(packet);
	}
	
	@Override
	public void hpsShow()
	{
		if (_playerTwo.player != null && _playerOne.player != null)
		{
			_playerTwo.player.sendPacket(new ExOlympiadUserInfo(_playerOne.player, 1));
			_playerOne.player.sendPacket(new ExOlympiadUserInfo(_playerTwo.player, 1));
		}
	}
	
	@Override
	protected final void removals()
	{
		if (_aborted)
			return;
		
		removals(_playerOne.player, true);
		removals(_playerTwo.player, true);
	}
	
	@Override
	protected final void buffAndHealPlayers()
	{
		if (_aborted)
			return;
		
		buffAndHealPlayer(_playerOne.player);
		buffAndHealPlayer(_playerTwo.player);
	}
	
	@Override
	protected final boolean makeCompetitionStart()
	{
		if (!super.makeCompetitionStart())
			return false;
		
		if (_playerOne.player == null || _playerTwo.player == null)
			return false;
		
		_playerOne.player.setIsOlympiadStart(true);
		_playerOne.player.updateEffectIcons();
		_playerTwo.player.setIsOlympiadStart(true);
		_playerTwo.player.updateEffectIcons();
		
		_playerOne.player.broadcastRelationChanged();
	    _playerTwo.player.broadcastRelationChanged();
	    
		return true;
	}
	
	@Override
	protected final void cleanEffects()
	{
		if (_playerOne.player != null && !_playerOne.defaulted && !_playerOne.disconnected && _playerOne.player.getOlympiadGameId() == _stadiumID)
			cleanEffects(_playerOne.player);
		
		if (_playerTwo.player != null && !_playerTwo.defaulted && !_playerTwo.disconnected && _playerTwo.player.getOlympiadGameId() == _stadiumID)
			cleanEffects(_playerTwo.player);
	}
	
	@Override
	protected final void portPlayersBack()
	{
		if (_playerOne.player != null && !_playerOne.defaulted && !_playerOne.disconnected)
			portPlayerBack(_playerOne.player);
		if (_playerTwo.player != null && !_playerTwo.defaulted && !_playerTwo.disconnected)
			portPlayerBack(_playerTwo.player);
	}
	
	@Override
	protected final void playersStatusBack()
	{
		if (_playerOne.player != null && !_playerOne.defaulted && !_playerOne.disconnected && _playerOne.player.getOlympiadGameId() == _stadiumID)
			playerStatusBack(_playerOne.player);
		
		if (_playerTwo.player != null && !_playerTwo.defaulted && !_playerTwo.disconnected && _playerTwo.player.getOlympiadGameId() == _stadiumID)
			playerStatusBack(_playerTwo.player);
	}
	
	@Override
	protected final void clearPlayers()
	{
		_playerOne.player = null;
		_playerOne = null;
		_playerTwo.player = null;
		_playerTwo = null;
	}
	
	@Override
	protected final void handleDisconnect(L2PcInstance player)
	{
		if (player.getObjectId() == _playerOne.objectId)
			_playerOne.disconnected = true;
		else if (player.getObjectId() == _playerTwo.objectId)
			_playerTwo.disconnected = true;
	}
	
	@Override
	protected final boolean checkBattleStatus()
	{
		if (_aborted)
			return false;
		
		if (_playerOne.player == null || _playerOne.disconnected)
			return false;
		
		if (_playerTwo.player == null || _playerTwo.disconnected)
			return false;
		
		return true;
	}
	
	@Override
	protected final boolean haveWinner()
	{
		if (!checkBattleStatus())
			return true;
		
		boolean playerOneLost = true;
		try
		{
			if (_playerOne.player.getOlympiadGameId() == _stadiumID)
				playerOneLost = _playerOne.player.isDead();
		}
		catch (Exception e)
		{
			playerOneLost = true;
		}
		
		boolean playerTwoLost = true;
		try
		{
			if (_playerTwo.player.getOlympiadGameId() == _stadiumID)
				playerTwoLost = _playerTwo.player.isDead();
		}
		catch (Exception e)
		{
			playerTwoLost = true;
		}
		
		return playerOneLost || playerTwoLost;
	}
	
	@Override
	protected void validateWinner(L2OlympiadStadiumZone stadium)
	{
		if (_aborted)
			return;
		
		final String ploneIp = _playerOne.player.getClient().getConnection().getInetAddress().getHostAddress();
		final String pltowIp = _playerTwo.player.getClient().getConnection().getInetAddress().getHostAddress();
		
		if (ploneIp.equals(pltowIp) && !Config.OLY_SAME_IP)
		{
			SystemMessage _sm;
			_log.config("Match from same ip " + _playerOne.player.getName() + "(IP : " + _playerOne.player.getClient().getConnection().getInetAddress() + " )" + " vs " + _playerTwo.player.getName() + "(IP : " + _playerOne.player.getClient().getConnection().getInetAddress() + " )");
			
			_sm = SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_ENDED_IN_A_TIE);
			stadium.broadcastPacket(_sm);
			_playerOne.player.sendMessage("Match suspected of Illegal Violation: GM informed!");
			_playerTwo.player.sendMessage("Match suspected of Illegal Violation: GM informed!");
		}
		final boolean _pOneCrash = (_playerOne.player == null || _playerOne.disconnected);
		final boolean _pTwoCrash = (_playerTwo.player == null || _playerTwo.disconnected);
		
		final int playerOnePoints = _playerOne.stats.getInteger(POINTS);
		final int playerTwoPoints = _playerTwo.stats.getInteger(POINTS);
		
		int pointDiff = Math.min(playerOnePoints, playerTwoPoints) / getDivider();
		if (pointDiff <= 0)
			pointDiff = 1;
		else if (pointDiff > Config.ALT_OLY_MAX_POINTS)
			pointDiff = Config.ALT_OLY_MAX_POINTS;
		
		int points;
		SystemMessage sm;
		
		// Check for if a player defaulted before battle started
		if (_playerOne.defaulted || _playerTwo.defaulted)
		{
			try
			{
				if (_playerOne.defaulted)
				{
					try
					{
						points = Math.min(playerOnePoints / 3, Config.ALT_OLY_MAX_POINTS);
						removePointsFromParticipant(_playerOne, points);
					}
					catch (Exception e)
					{
						_log.warning(OlympiadGameNormal.class.getSimpleName() + ": Exception on validateWinner(): " + e.getMessage());
					}
				}
				
				if (_playerTwo.defaulted)
				{
					try
					{
						points = Math.min(playerTwoPoints / 3, Config.ALT_OLY_MAX_POINTS);
						removePointsFromParticipant(_playerTwo, points);
					}
					catch (Exception e)
					{
						_log.warning(OlympiadGameNormal.class.getSimpleName() + ": Exception on validateWinner(): " + e.getMessage());
					}
				}
				return;
			}
			catch (Exception e)
			{
				_log.warning(OlympiadGameNormal.class.getSimpleName() + ": Exception on validateWinner(): " + e.getMessage());
				return;
			}
		}
		
		// Create results for players if a player crashed
		if (_pOneCrash || _pTwoCrash)
		{
			try
			{
				if (_pTwoCrash && !_pOneCrash)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_WON_THE_GAME);
					sm.addString(_playerOne.name);
					_playerOne.player.sendPacket(sm);
					
					_playerOne.updateStat(COMP_WON, 1);
					addPointsToParticipant(_playerOne, pointDiff);
					
					_playerTwo.updateStat(COMP_LOST, 1);
					removePointsFromParticipant(_playerTwo, pointDiff);
					
					rewardParticipant(_playerOne.player, getReward());
				}
				else if (_pOneCrash && !_pTwoCrash)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_WON_THE_GAME);
					sm.addString(_playerTwo.name);
					_playerTwo.player.sendPacket(sm);
					
					_playerTwo.updateStat(COMP_WON, 1);
					addPointsToParticipant(_playerTwo, pointDiff);
					
					_playerOne.updateStat(COMP_LOST, 1);
					removePointsFromParticipant(_playerOne, pointDiff);
					
					rewardParticipant(_playerTwo.player, getReward());
				}
				else if (_pOneCrash && _pTwoCrash)
				{
					stadium.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_ENDED_IN_A_TIE));
					
					_playerOne.updateStat(COMP_LOST, 1);
					removePointsFromParticipant(_playerOne, pointDiff);
					
					_playerTwo.updateStat(COMP_LOST, 1);
					removePointsFromParticipant(_playerTwo, pointDiff);
				}
				
				_playerOne.updateStat(COMP_DONE, 1);
				_playerTwo.updateStat(COMP_DONE, 1);
				
				return;
			}
			catch (Exception e)
			{
				_log.warning(OlympiadGameNormal.class.getSimpleName() + ": Exception on validateWinner(): " + e.getMessage());
				return;
			}
		}
		
		try
		{
			// Calculate Fight time
			long _fightTime = (System.currentTimeMillis() - _startTime);
			
			double playerOneHp = 0;
			if (_playerOne.player != null && !_playerOne.player.isDead())
			{
				playerOneHp = _playerOne.player.getCurrentHp() + _playerOne.player.getCurrentCp();
				if (playerOneHp < 0.5)
					playerOneHp = 0;
			}
			
			double playerTwoHp = 0;
			if (_playerTwo.player != null && !_playerTwo.player.isDead())
			{
				playerTwoHp = _playerTwo.player.getCurrentHp() + _playerTwo.player.getCurrentCp();
				if (playerTwoHp < 0.5)
					playerTwoHp = 0;
			}
			
			// if players crashed, search if they've relogged
			_playerOne.updatePlayer();
			_playerTwo.updatePlayer();
			
			if ((_playerOne.player == null || !_playerOne.player.isOnline()) && (_playerTwo.player == null || !_playerTwo.player.isOnline()))
			{
				_playerOne.updateStat(COMP_DRAWN, 1);
				_playerTwo.updateStat(COMP_DRAWN, 1);
				sm = SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_ENDED_IN_A_TIE);
				stadium.broadcastPacket(sm);
			}
			else if (_playerTwo.player == null || !_playerTwo.player.isOnline()|| (playerTwoHp == 0 && playerOneHp != 0) || (_damageP1 > _damageP2 && playerTwoHp != 0 && playerOneHp != 0))
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_WON_THE_GAME);
				sm.addString(_playerOne.name);
				_playerOne.player.sendPacket(sm);
				
				_playerOne.updateStat(COMP_WON, 1);
				_playerTwo.updateStat(COMP_LOST, 1);
				
				addPointsToParticipant(_playerOne, pointDiff);
				removePointsFromParticipant(_playerTwo, pointDiff);
				
				// Save Fight Result
				saveResults(_playerOne, _playerTwo, 1, _startTime, _fightTime, getType());
				rewardParticipant(_playerOne.player, getReward());
			}
			else if (_playerOne.player == null || !_playerOne.player.isOnline() || (playerOneHp == 0 && playerTwoHp != 0) || (_damageP2 > _damageP1 && playerOneHp != 0 && playerTwoHp != 0))
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_WON_THE_GAME);
				sm.addString(_playerTwo.name);
				_playerTwo.player.sendPacket(sm);
				
				_playerTwo.updateStat(COMP_WON, 1);
				_playerOne.updateStat(COMP_LOST, 1);
				
				addPointsToParticipant(_playerTwo, pointDiff);
				removePointsFromParticipant(_playerOne, pointDiff);
				
				// Save Fight Result
				saveResults(_playerOne, _playerTwo, 2, _startTime, _fightTime, getType());
				rewardParticipant(_playerTwo.player, getReward());
			}
			else
			{
				// Save Fight Result
				saveResults(_playerOne, _playerTwo, 0, _startTime, _fightTime, getType());
				
				sm = SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_ENDED_IN_A_TIE);
				stadium.broadcastPacket(sm);
				
				removePointsFromParticipant(_playerOne, Math.min(playerOnePoints / getDivider(), Config.ALT_OLY_MAX_POINTS));
				removePointsFromParticipant(_playerTwo, Math.min(playerTwoPoints / getDivider(), Config.ALT_OLY_MAX_POINTS));
			}
			
			_playerOne.updateStat(COMP_DONE, 1);
			_playerTwo.updateStat(COMP_DONE, 1);
		}
		catch (Exception e)
		{
			_log.warning(OlympiadGameNormal.class.getSimpleName() + ": Exception on validateWinner(): " + e.getMessage());
		}
	}
	
	@Override
	protected final void addDamage(L2PcInstance player, int damage)
	{
		if (_playerOne.player == null || _playerTwo.player == null)
			return;
		if (player == _playerOne.player)
			_damageP1 += damage;
		else if (player == _playerTwo.player)
			_damageP2 += damage;
	}
	
	@Override
	public final String[] getPlayerNames()
	{
		return new String[]
		{
			_playerOne.name,
			_playerTwo.name
		};
	}
	
	@Override
	public boolean checkDefaulted()
	{
		SystemMessage reason;
		_playerOne.updatePlayer();
		_playerTwo.updatePlayer();
		
		reason = checkDefaulted(_playerOne.player);
		if (reason != null)
		{
			_playerOne.defaulted = true;
			if (_playerTwo.player != null)
				_playerTwo.player.sendPacket(reason);
		}
		
		reason = checkDefaulted(_playerTwo.player);
		if (reason != null)
		{
			_playerTwo.defaulted = true;
			if (_playerOne.player != null)
				_playerOne.player.sendPacket(reason);
		}
		
		return _playerOne.defaulted || _playerTwo.defaulted;
	}
	
	@Override
	public final void resetDamage()
	{
		_damageP1 = 0;
		_damageP2 = 0;
	}
	
	protected static final void saveResults(Participant one, Participant two, int _winner, long _startTime, long _fightTime, CompetitionType type)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(INSERT_OLYMPIAD_FIGHTS);
			statement.setInt(1, one.objectId);
			statement.setInt(2, two.objectId);
			statement.setInt(3, one.baseClass);
			statement.setInt(4, two.baseClass);
			statement.setInt(5, _winner);
			statement.setLong(6, _startTime);
			statement.setLong(7, _fightTime);
			statement.setInt(8, (type == CompetitionType.CLASSED ? 1 : 0));
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.severe(Auction.class.getName() + ": SQL exception while saving olympiad fight");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
}