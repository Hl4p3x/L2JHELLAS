package com.l2jhellas.gameserver.model.entity.olympiad;

import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.model.zone.type.L2OlympiadStadiumZone;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public final class OlympiadGameTask implements Runnable
{
	protected static final Logger _log = Logger.getLogger(OlympiadGameTask.class.getName());
	protected static final long BATTLE_PERIOD = Config.ALT_OLY_BATTLE; // 6 mins
	
	public static final int[] TELEPORT_TO_ARENA =
	{
		120,
		60,
		30,
		15,
		10,
		5,
		4,
		3,
		2,
		1,
		0
	};
	public static final int[] BATTLE_START_TIME_FIRST =
	{
		60,
		50,
		40,
		30,
		20,
		10,
		0
	};
	public static final int[] BATTLE_START_TIME_SECOND =
	{
		10,
		5,
		4,
		3,
		2,
		1,
		0
	};
	public static final int[] TELEPORT_TO_TOWN =
	{
		40,
		30,
		20,
		10,
		5,
		4,
		3,
		2,
		1,
		0
	};
	
	private final L2OlympiadStadiumZone _zone;
	private AbstractOlympiadGame _game;
	public GameState _state = GameState.IDLE;
	private boolean _needAnnounce = false;
	private int _countDown = 0;
	
	public static enum GameState
	{
		BEGIN,
		TELE_TO_ARENA,
		GAME_STARTED,
		BATTLE_COUNTDOWN_FIRST,
		BATTLE_COUNTDOWN_SECOND,
		BATTLE_STARTED,
		BATTLE_IN_PROGRESS,
		GAME_STOPPED,
		TELE_TO_TOWN,
		CLEANUP,
		IDLE
	}
	
	public OlympiadGameTask(L2OlympiadStadiumZone zone)
	{
		_zone = zone;
	}
	
	public final boolean isRunning()
	{
		return _state != GameState.IDLE;
	}
	
	public final boolean isGameStarted()
	{
		return _state.ordinal() >= GameState.GAME_STARTED.ordinal() && _state.ordinal() <= GameState.CLEANUP.ordinal();
	}
	
	public final boolean isInTimerTime()
	{
		return _state == GameState.BATTLE_COUNTDOWN_FIRST || _state == GameState.BATTLE_COUNTDOWN_SECOND;
	}
	
	public final boolean isBattleStarted()
	{
		return _state == GameState.BATTLE_IN_PROGRESS;
	}
	
	public final boolean isBattleFinished()
	{
		return _state == GameState.TELE_TO_TOWN;
	}
	
	public final boolean needAnnounce()
	{
		if (_needAnnounce)
		{
			_needAnnounce = false;
			return true;
		}
		return false;
	}
	
	public final L2OlympiadStadiumZone getZone()
	{
		return _zone;
	}
	
	public final AbstractOlympiadGame getGame()
	{
		return _game;
	}
	
	public final void attachGame(AbstractOlympiadGame game)
	{
		if (game != null && _state != GameState.IDLE)
		{
			_log.warning(OlympiadGameTask.class.getSimpleName() + ": Attempt to overwrite non-finished game in state " + _state);
			return;
		}
		
		_game = game;
		_state = GameState.BEGIN;
		_needAnnounce = false;
		ThreadPoolManager.getInstance().executeTask(this);
	}
	
	@Override
	public final void run()
	{
		try
		{
			int delay = 1; // schedule next call after 1s
			switch (_state)
			{
			// Game created
				case BEGIN:
				{
					_state = GameState.TELE_TO_ARENA;
					_countDown = Config.ALT_OLY_WAIT_TIME;
					break;
				}
				// Teleport to arena countdown
				case TELE_TO_ARENA:
				{
					if (_countDown > 0)
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_WILL_ENTER_THE_OLYMPIAD_STADIUM_IN_S1_SECOND_S);
						sm.addNumber(_countDown);
						_game.broadcastPacket(sm);
					}
					
					delay = getDelay(TELEPORT_TO_ARENA);
					if (_countDown <= 0)
						_state = GameState.GAME_STARTED;
					break;
				}
				// Game start, port players to arena
				case GAME_STARTED:
				{
					if (!startGame())
					{
						_state = GameState.GAME_STOPPED;
						break;
					}
					
					_state = GameState.BATTLE_COUNTDOWN_FIRST;
					_countDown = BATTLE_START_TIME_FIRST[0];
					delay = 5;
					break;
				}
				// Battle start countdown, first part (60-10)
				case BATTLE_COUNTDOWN_FIRST:
				{
					if (_countDown > 0)
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_WILL_START_IN_S1_SECOND_S);
						sm.addNumber(_countDown);
						_game.broadcastPacket(sm);
						
						if (_countDown == 59)
							_game.buffAndHealPlayers();
					}
					
					delay = getDelay(BATTLE_START_TIME_FIRST);
					if (_countDown <= 0)
					{
						_game.resetDamage();
						
						_state = GameState.BATTLE_COUNTDOWN_SECOND;
						_countDown = BATTLE_START_TIME_SECOND[0];
						delay = getDelay(BATTLE_START_TIME_SECOND);
					}
					
					break;
				}
				// Battle start countdown, second part (10-0)
				case BATTLE_COUNTDOWN_SECOND:
				{
					if (_countDown > 0)
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_WILL_START_IN_S1_SECOND_S);
						sm.addNumber(_countDown);
						_game.broadcastPacket(sm);
					}
					
					delay = getDelay(BATTLE_START_TIME_SECOND);
					if (_countDown <= 0)
						_state = GameState.BATTLE_STARTED;
					
					break;
				}
				// Beginning of the battle
				case BATTLE_STARTED:
				{
					_countDown = 0;
					_state = GameState.BATTLE_IN_PROGRESS; // set state first, used in zone update
					if (!startBattle())
						_state = GameState.GAME_STOPPED;
					
					break;
				}
				// Checks during battle
				case BATTLE_IN_PROGRESS:
				{
					_countDown += 1000;
					if (checkBattle() || _countDown > Config.ALT_OLY_BATTLE)
						_state = GameState.GAME_STOPPED;
					
					break;
				}
				// End of the battle
				case GAME_STOPPED:
				{
					_state = GameState.TELE_TO_TOWN;
					_countDown = TELEPORT_TO_TOWN[0];
					stopGame();
					delay = getDelay(TELEPORT_TO_TOWN);
					break;
				}
				// Teleport to town countdown
				case TELE_TO_TOWN:
				{
					if (_countDown > 0)
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_WILL_BE_MOVED_TO_TOWN_IN_S1_SECONDS);
						sm.addNumber(_countDown);
						_game.broadcastPacket(sm);
					}
					
					delay = getDelay(TELEPORT_TO_TOWN);
					if (_countDown <= 0)
						_state = GameState.CLEANUP;
					
					break;
				}
				// Removals
				case CLEANUP:
				{
					cleanupGame();
					_state = GameState.IDLE;
					_game = null;
					return;
				}
				default:
					break;
			}
			ThreadPoolManager.getInstance().scheduleGeneral(this, delay * 1000);
		}
		catch (Exception e)
		{
			switch (_state)
			{
				case GAME_STOPPED:
				case TELE_TO_TOWN:
				case CLEANUP:
				case IDLE:
				{
					_log.warning(OlympiadGameTask.class.getSimpleName() + ": Unable to return players back in town, exception");
					_state = GameState.IDLE;
					_game = null;
					return;
				}
				default:
					break;
			}
			
			_log.warning(OlympiadGameTask.class.getSimpleName() + ": Exception in " + _state + ", trying to port players back");
			_state = GameState.GAME_STOPPED;
			ThreadPoolManager.getInstance().scheduleGeneral(this, 1000);
		}
	}
	
	private final int getDelay(int[] times)
	{
		int time;
		for (int i = 0; i < times.length - 1; i++)
		{
			time = times[i];
			if (time >= _countDown)
				continue;
			
			final int delay = _countDown - time;
			_countDown = time;
			return delay;
		}
		// should not happens
		_countDown = -1;
		return 1;
	}
	
	private final boolean startGame()
	{
		// Checking for opponents and teleporting to arena
		if (_game.checkDefaulted())
			return false;
			
		if (!_game.portPlayersToArena(_zone.getCoordinates(_game._stadiumID)))
			return false;
			
		_game.removals();
		_needAnnounce = true;
		OlympiadGameManager.getInstance().startBattle(); // inform manager
		return true;
	}
	
	private final boolean startBattle()
	{
		if (_game.checkBattleStatus() && _game.makeCompetitionStart())
		{
			// game successfully started
			_game.broadcastOlympiadInfo(_zone);
			_game.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.STARTS_THE_GAME));
			_zone.updateZoneStatusForCharactersInside();
			return true;
		}
		return false;
	}
	
	private final boolean checkBattle()
	{
		return _game.haveWinner();
	}
	
	private final void stopGame()
	{
		_game.validateWinner(_zone);
		_zone.updateZoneStatusForCharactersInside();
		_game.cleanEffects();
	}
	
	private final void cleanupGame()
	{
		_game.playersStatusBack();
		_game.portPlayersBack();
		_game.clearPlayers();
	}
}