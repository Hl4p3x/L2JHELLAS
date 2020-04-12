package com.l2jhellas.gameserver.model.entity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Future;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.ai.CtrlIntention;
import com.l2jhellas.gameserver.enums.Team;
import com.l2jhellas.gameserver.enums.ZoneId;
import com.l2jhellas.gameserver.enums.player.DuelResult;
import com.l2jhellas.gameserver.enums.player.DuelState;
import com.l2jhellas.gameserver.enums.sound.Music;
import com.l2jhellas.gameserver.instancemanager.DuelManager;
import com.l2jhellas.gameserver.instancemanager.ZoneManager;
import com.l2jhellas.gameserver.model.L2Effect;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.model.zone.type.L2OlympiadStadiumZone;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.ExDuelEnd;
import com.l2jhellas.gameserver.network.serverpackets.ExDuelReady;
import com.l2jhellas.gameserver.network.serverpackets.ExDuelStart;
import com.l2jhellas.gameserver.network.serverpackets.ExDuelUpdateUserInfo;
import com.l2jhellas.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jhellas.gameserver.network.serverpackets.PlaySound;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.util.Rnd;

public class Duel
{
	private final List<PlayerCondition> _playerConditions = new ArrayList<>();
	
	private static final int PARTY_DUEL_DURATION = 300;
	private static final int PLAYER_DUEL_DURATION = 120;
	
	private final int _duelId;
	private final boolean _partyDuel;
	
	private final Calendar _duelEndTime;
	
	private final L2PcInstance _playerA;
	private final L2PcInstance _playerB;
	
	private int _surrenderRequest;
	
	protected Future<?> _startTask = null;
	protected Future<?> _checkTask = null;
	
	protected int _countdown = 4;
	
	final PlaySound DUELSOUND = Music.B04_S01.getPacket();
	
	public Duel(L2PcInstance playerA, L2PcInstance playerB, boolean isPartyDuel, int duelId)
	{
		_duelId = duelId;
		_playerA = playerA;
		_playerB = playerB;
		_partyDuel = isPartyDuel;
		
		_duelEndTime = Calendar.getInstance();
		_duelEndTime.add(Calendar.SECOND, _partyDuel ? PARTY_DUEL_DURATION : PLAYER_DUEL_DURATION);

		
		if (_partyDuel)
		{
			_countdown++;
			
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.IN_A_MOMENT_YOU_WILL_BE_TRANSPORTED_TO_THE_SITE_WHERE_THE_DUEL_WILL_TAKE_PLACE);
			broadcastToTeam1(sm);
			broadcastToTeam2(sm);
			
			for (L2PcInstance partyPlayerA : _playerA.getParty().getPartyMembers())
				partyPlayerA.setIsInDuel(_duelId);
			
			for (L2PcInstance partyPlayerB : _playerB.getParty().getPartyMembers())
				partyPlayerB.setIsInDuel(_duelId);
		}
		else
		{
			_playerA.setIsInDuel(_duelId);		
			_playerB.setIsInDuel(_duelId);
		}
		
		savePlayerConditions();
		
		_startTask = ThreadPoolManager.getInstance().scheduleGeneral(new StartTask(this), 2500);				
	}
	
	private static class PlayerCondition
	{
		private L2PcInstance _player;
		
		private double _hp;
		private double _mp;
		private double _cp;
		
		private int _x;
		private int _y;
		private int _z;
		
		private List<L2Effect> _debuffs;
		
		public PlayerCondition(L2PcInstance player, boolean partyDuel)
		{
			if (player == null)
				return;
			
			_player = player;
			_hp = _player.getCurrentHp();
			_mp = _player.getCurrentMp();
			_cp = _player.getCurrentCp();
			
			if (partyDuel)
			{
				_x = _player.getX();
				_y = _player.getY();
				_z = _player.getZ();
			}
		}
		
		public void restoreCondition(boolean abnormalEnd)
		{
			teleportBack();
			
			if (abnormalEnd)
				return;
			
			_player.setCurrentHp(_hp);
			_player.setCurrentMp(_mp);
			_player.setCurrentCp(_cp);
			
			if (_debuffs != null)
			{
				for (L2Effect skill : _debuffs)
					if (skill != null)
						skill.exit();
			}
		}
		
		public void registerDebuff(L2Effect debuff)
		{
			if (_debuffs == null)
				_debuffs = new ArrayList<>();
			
			_debuffs.add(debuff);
		}
		
		public void teleportBack()
		{
			if (_x != 0 && _y != 0)
				_player.teleToLocation(_x, _y, _z, false);
		}
		
		public L2PcInstance getPlayer()
		{
			return _player;
		}
	}
	
	public int countdown()
	{
		_countdown--;
		
		if (_countdown > 3)
			return _countdown;
		
		SystemMessage sm = null;
		
		if (_countdown > 0)
			sm = SystemMessage.getSystemMessage(SystemMessageId.THE_DUEL_WILL_BEGIN_IN_S1_SECONDS).addNumber(_countdown);
		else
			sm = SystemMessage.getSystemMessage(SystemMessageId.LET_THE_DUEL_BEGIN);
		
		broadcastToTeam1(sm);
		broadcastToTeam2(sm);
		
		return _countdown;
	}
	
	private class StartTask implements Runnable
	{
		private final Duel _duel;
		
		public StartTask(Duel duel)
		{
			_duel = duel;
		}
		
		@Override
		public void run()
		{
			int count = _duel.countdown();
			
			if (count == 4)
			{	
				_duel.teleportPlayers();				
				ThreadPoolManager.getInstance().scheduleGeneral(this, 20000);
			}
			else if (count > 0)
				ThreadPoolManager.getInstance().scheduleGeneral(this, 1000);
			else
				_duel.startDuel();	
		}
	}
	
	private class CheckTask implements Runnable
	{
		private final Duel _duel;
		
		public CheckTask(Duel duel)
		{
			_duel = duel;
		}
		
		@Override
		public void run()
		{
			final DuelResult status = _duel.checkEndDuelCondition();
			
			if (status != DuelResult.CONTINUE)
			{
				if (_startTask != null)
				{
					_startTask.cancel(true);
					_startTask = null;
				}
				
				if (_checkTask != null)
				{
					_checkTask.cancel(false);
					_checkTask = null;
				}
				
				_duel.stopFighting();
				
				if (status != DuelResult.CANCELED)
					_duel.playAnimations();
				
				ThreadPoolManager.getInstance().scheduleGeneral(new EndDuel(_duel,status), 4000);
			}
		}
	}
	
	
	private class EndDuel implements Runnable
	{
		private final Duel _duel;
		private final DuelResult _result;
		
		public EndDuel(Duel duel,DuelResult result)
		{
			_duel = duel;
			_result = result;
		}
		
		@Override
		public void run()
		{
			_duel.endDuel(_result);
		}
	}
	
	protected void stopFighting()
	{
		if (_partyDuel)
		{
			for (L2PcInstance partyPlayer : _playerA.getParty().getPartyMembers())
			{
				partyPlayer.abortAllAttacks();
				partyPlayer.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				partyPlayer.setTarget(null);
				partyPlayer.sendPacket(ActionFailed.STATIC_PACKET);
			}
			
			for (L2PcInstance partyPlayer : _playerB.getParty().getPartyMembers())
			{
				partyPlayer.abortAllAttacks();
				partyPlayer.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				partyPlayer.setTarget(null);
				partyPlayer.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
		else
		{
			_playerA.abortAllAttacks();
			_playerB.abortAllAttacks();
			
			_playerA.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			_playerA.setTarget(null);
			
			_playerB.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			_playerB.setTarget(null);
			
			_playerA.sendPacket(ActionFailed.STATIC_PACKET);
			_playerB.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	protected void startDuel()
	{
		if (_partyDuel)
		{
			for (L2PcInstance partyPlayer : _playerA.getParty().getPartyMembers())
			{
				partyPlayer.cancelActiveTrade();
				partyPlayer.setDuelState(DuelState.DUELLING);
				partyPlayer.setTeam(Team.BLUE);
				partyPlayer.broadcastUserInfo();
				
				final L2Summon summon = partyPlayer.getPet();
				if (summon != null)
					summon.updateAbnormalEffect();
				
				broadcastToTeam2(new ExDuelUpdateUserInfo(partyPlayer));
			}
			
			for (L2PcInstance partyPlayer : _playerB.getParty().getPartyMembers())
			{
				partyPlayer.cancelActiveTrade();
				partyPlayer.setDuelState(DuelState.DUELLING);
				partyPlayer.setTeam(Team.RED);
				partyPlayer.broadcastUserInfo();
				
				final L2Summon summon = partyPlayer.getPet();
				if (summon != null)
					summon.updateAbnormalEffect();
				
				broadcastToTeam1(new ExDuelUpdateUserInfo(partyPlayer));
			}
			
			ExDuelReady ready = new ExDuelReady(1);
			ExDuelStart start = new ExDuelStart(1);
			
			broadcastToTeam1(ready);
			broadcastToTeam2(ready);
			broadcastToTeam1(start);
			broadcastToTeam2(start);
		}
		else
		{
			_playerA.setDuelState(DuelState.DUELLING);
			_playerA.setTeam(Team.BLUE);
			_playerB.setDuelState(DuelState.DUELLING);
			_playerB.setTeam(Team.RED);
			
			ExDuelReady ready = new ExDuelReady(0);
			ExDuelStart start = new ExDuelStart(0);
			
			broadcastToTeam1(ready);
			broadcastToTeam2(ready);
			broadcastToTeam1(start);
			broadcastToTeam2(start);
			
			broadcastToTeam1(new ExDuelUpdateUserInfo(_playerB));
			broadcastToTeam2(new ExDuelUpdateUserInfo(_playerA));
			
			_playerA.broadcastUserInfo();
			
			L2Summon summon = _playerA.getPet();
			
			if (summon != null)
				summon.updateAbnormalEffect();
			
			_playerB.broadcastUserInfo();
			
			summon = _playerB.getPet();
			
			if (summon != null)
				summon.updateAbnormalEffect();
		}
		
		broadcastToTeam1(DUELSOUND);
		broadcastToTeam2(DUELSOUND);
		
		_checkTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckTask(this), 1000, 1000);

	}
	
	private void savePlayerConditions()
	{
		if (_partyDuel)
		{
			for (L2PcInstance partyPlayer : _playerA.getParty().getPartyMembers())
				_playerConditions.add(new PlayerCondition(partyPlayer, _partyDuel));
			
			for (L2PcInstance partyPlayer : _playerB.getParty().getPartyMembers())
				_playerConditions.add(new PlayerCondition(partyPlayer, _partyDuel));
		}
		else
		{
			_playerConditions.add(new PlayerCondition(_playerA, _partyDuel));
			_playerConditions.add(new PlayerCondition(_playerB, _partyDuel));
		}
	}
	
	private void restorePlayerConditions(boolean abnormalEnd)
	{
		if (_partyDuel)
		{
			for (L2PcInstance partyPlayer : _playerA.getParty().getPartyMembers())
			{
				partyPlayer.setIsInDuel(0);
				partyPlayer.setTeam(Team.NONE);
				partyPlayer.broadcastUserInfo();
				
				final L2Summon summon = partyPlayer.getPet();
				if (summon != null)
					summon.updateAbnormalEffect();
			}
			
			for (L2PcInstance partyPlayer : _playerB.getParty().getPartyMembers())
			{
				partyPlayer.setIsInDuel(0);
				partyPlayer.setTeam(Team.NONE);
				partyPlayer.broadcastUserInfo();
				
				final L2Summon summon = partyPlayer.getPet();
				if (summon != null)
					summon.updateAbnormalEffect();
			}
		}
		else
		{
			_playerA.setIsInDuel(0);
			_playerA.setTeam(Team.NONE);
			_playerA.broadcastUserInfo();
			
			L2Summon summon = _playerA.getPet();
			
			if (summon != null)
				summon.updateAbnormalEffect();
			
			_playerB.setIsInDuel(0);
			_playerB.setTeam(Team.NONE);
			_playerB.broadcastUserInfo();
			
			summon = _playerB.getPet();
			
			if (summon != null)
				summon.updateAbnormalEffect();
		}
		
		if ((!_partyDuel && !abnormalEnd) || _partyDuel)
		{
			for (PlayerCondition cond : _playerConditions)
				cond.restoreCondition(abnormalEnd);
		}
	}
	
	public int getId()
	{
		return _duelId;
	}
	
	public int getRemainingTime()
	{
		return (int) (_duelEndTime.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
	}
	
	public L2PcInstance getPlayerA()
	{
		return _playerA;
	}
	
	public L2PcInstance getPlayerB()
	{
		return _playerB;
	}
	
	public boolean isPartyDuel()
	{
		return _partyDuel;
	}
	
	protected void teleportPlayers()
	{
		if (!_partyDuel)
			return;
		
		final L2OlympiadStadiumZone zone = ZoneManager.getInstance().getAllZones(L2OlympiadStadiumZone.class).stream().filter(stad -> stad.getCharactersInside().size() == 0).findFirst().orElse(null);
		
		if (zone == null)
		{
			SystemMessage sm = SystemMessage.sendString("The arena is full, you will fight here.");
			broadcastToTeam1(sm);
			broadcastToTeam2(sm);
			return;
		}
		
		final List<Location> spawns = zone.getSpawns();
		
		final Location spawn1 = spawns.get(Rnd.get(spawns.size() / 2));
		for (L2PcInstance partyPlayerA : _playerA.getParty().getPartyMembers())
		{
			partyPlayerA.teleToLocation(spawn1.getX(), spawn1.getY(), spawn1.getZ());
		}
		
		final Location spawn2 = spawns.get(Rnd.get(spawns.size() / 2, spawns.size()));
		for (L2PcInstance partyPlayerB : _playerB.getParty().getPartyMembers())
		{
			partyPlayerB.teleToLocation(spawn2.getX(), spawn2.getY(), spawn2.getZ());
		}
	}
	
	public void broadcastToTeam1(L2GameServerPacket packet)
	{
		if (_partyDuel && _playerA.getParty() != null)
		{
			for (L2PcInstance partyPlayer : _playerA.getParty().getPartyMembers())
				partyPlayer.sendPacket(packet);
		}
		else
			_playerA.sendPacket(packet);
	}
	
	public void broadcastToTeam2(L2GameServerPacket packet)
	{
		if (_partyDuel && _playerB.getParty() != null)
		{
			for (L2PcInstance partyPlayer : _playerB.getParty().getPartyMembers())
				partyPlayer.sendPacket(packet);
		}
		else
			_playerB.sendPacket(packet);
	}
	
	protected void playAnimations()
	{
		if (_playerA != null)
		{
			if (_playerA.getDuelState() == DuelState.WINNER)
			{
				if (_partyDuel && _playerA.getParty() != null)
				{
					for (L2PcInstance partyPlayer : _playerA.getParty().getPartyMembers())
						partyPlayer.broadcastSocialActionInRadius(3);
				}
				else
					_playerA.broadcastSocialActionInRadius(3);
			}
			else if (_playerA.getDuelState() == DuelState.DEAD)
			{
				if (_partyDuel && _playerA.getParty() != null)
				{
					for (L2PcInstance partyPlayer : _playerA.getParty().getPartyMembers())
						partyPlayer.broadcastSocialActionInRadius(7);
				}
				else
					_playerA.broadcastSocialActionInRadius(7);
			}
		}
		
		if (_playerB != null)
		{
			if (_playerB.getDuelState() == DuelState.WINNER)
			{
				if (_partyDuel && _playerB.getParty() != null)
				{
					for (L2PcInstance partyPlayer : _playerB.getParty().getPartyMembers())
						partyPlayer.broadcastSocialActionInRadius(3);
				}
				else
					_playerB.broadcastSocialActionInRadius(3);
			}
			else if (_playerB.getDuelState() == DuelState.DEAD)
			{
				if (_partyDuel && _playerB.getParty() != null)
				{
					for (L2PcInstance partyPlayer : _playerB.getParty().getPartyMembers())
						partyPlayer.broadcastSocialActionInRadius(7);
				}
				else
					_playerB.broadcastSocialActionInRadius(7);
			}
		}
	}
	
	protected void endDuel(DuelResult result)
	{
		SystemMessage sm = null;
		switch (result)
		{
			case TEAM_2_SURRENDER:
				sm = SystemMessage.getSystemMessage((_partyDuel) ? SystemMessageId.SINCE_S1_PARTY_WITHDREW_FROM_THE_DUEL_S2_PARTY_HAS_WON : SystemMessageId.SINCE_S1_WITHDREW_FROM_THE_DUEL_S2_HAS_WON).addString(_playerB.getName()).addString(_playerA.getName());
				broadcastToTeam1(sm);
				broadcastToTeam2(sm);
			case TEAM_1_WIN:
				sm = SystemMessage.getSystemMessage((_partyDuel) ? SystemMessageId.S1_PARTY_HAS_WON_THE_DUEL : SystemMessageId.S1_HAS_WON_THE_DUEL).addString(_playerA.getName());
				break;
			
			case TEAM_1_SURRENDER:
				sm = SystemMessage.getSystemMessage((_partyDuel) ? SystemMessageId.SINCE_S1_PARTY_WITHDREW_FROM_THE_DUEL_S2_PARTY_HAS_WON : SystemMessageId.SINCE_S1_WITHDREW_FROM_THE_DUEL_S2_HAS_WON).addString(_playerA.getName()).addString(_playerB.getName());
				broadcastToTeam1(sm);
				broadcastToTeam2(sm);
			case TEAM_2_WIN:
				sm = SystemMessage.getSystemMessage((_partyDuel) ? SystemMessageId.S1_PARTY_HAS_WON_THE_DUEL : SystemMessageId.S1_HAS_WON_THE_DUEL).addString(_playerB.getName());
				break;
			
			case CANCELED:
			case TIMEOUT:
				sm = SystemMessage.getSystemMessage(SystemMessageId.THE_DUEL_HAS_ENDED_IN_A_TIE);
				break;
			default :
				break;
		}
		
		broadcastToTeam1(sm);
		broadcastToTeam2(sm);
		
		restorePlayerConditions(result == DuelResult.CANCELED);
		
		ExDuelEnd duelEnd = new ExDuelEnd((_partyDuel) ? 1 : 0);
		
		broadcastToTeam1(duelEnd);
		broadcastToTeam2(duelEnd);
		
		_playerConditions.clear();
		DuelManager.getInstance().removeDuel(_duelId);
	}
	
	protected DuelResult checkEndDuelCondition()
	{
		if (_playerA.isOnline() == 0 && _playerB.isOnline() == 0)
			return DuelResult.CANCELED;
		
		if (_playerA.isOnline() == 0)
		{
			onPlayerDefeat(_playerA);
			return DuelResult.TEAM_1_SURRENDER;
		}
		
		if (_playerB.isOnline() == 0)
		{
			onPlayerDefeat(_playerB);
			return DuelResult.TEAM_2_SURRENDER;
		}
		
		if (_surrenderRequest != 0)
			return (_surrenderRequest == 1) ? DuelResult.TEAM_1_SURRENDER : DuelResult.TEAM_2_SURRENDER;
		
		if (getRemainingTime() <= 0)
			return DuelResult.TIMEOUT;
		
		if (_playerA.getDuelState() == DuelState.WINNER)
			return DuelResult.TEAM_1_WIN;
		
		if (_playerB.getDuelState() == DuelState.WINNER)
			return DuelResult.TEAM_2_WIN;
		
		if (!_partyDuel)
		{
			if (_playerA.getDuelState() == DuelState.INTERRUPTED || _playerB.getDuelState() == DuelState.INTERRUPTED)
				return DuelResult.CANCELED;
			
			if (!_playerA.isInsideRadius(_playerB, 2000, false, false))
				return DuelResult.CANCELED;
			
			if (_playerA.getPvpFlag() != 0 || _playerB.getPvpFlag() != 0)
				return DuelResult.CANCELED;
			
			if (_playerA.isInsideZone(ZoneId.PEACE) || _playerB.isInsideZone(ZoneId.PEACE) || _playerA.isInsideZone(ZoneId.SIEGE) || _playerB.isInsideZone(ZoneId.SIEGE) || _playerA.isInsideZone(ZoneId.PVP) || _playerB.isInsideZone(ZoneId.PVP))
				return DuelResult.CANCELED;
		}
		else
		{
			if (_playerA.getParty() != null)
			{
				for (L2PcInstance partyMember : _playerA.getParty().getPartyMembers())
				{
					if (partyMember.getDuelState() == DuelState.INTERRUPTED)
						return DuelResult.CANCELED;
					
					if (!partyMember.isInsideRadius(_playerB, 2000, false, false))
						return DuelResult.CANCELED;
					
					if (partyMember.getPvpFlag() != 0)
						return DuelResult.CANCELED;
					
					if (partyMember.isInsideZone(ZoneId.PEACE) || partyMember.isInsideZone(ZoneId.PEACE) || partyMember.isInsideZone(ZoneId.SIEGE))
						return DuelResult.CANCELED;
				}
			}
			
			if (_playerB.getParty() != null)
			{
				for (L2PcInstance partyMember : _playerB.getParty().getPartyMembers())
				{
					if (partyMember.getDuelState() == DuelState.INTERRUPTED)
						return DuelResult.CANCELED;
					
					if (!partyMember.isInsideRadius(_playerA, 2000, false, false))
						return DuelResult.CANCELED;
					
					if (partyMember.getPvpFlag() != 0)
						return DuelResult.CANCELED;
					
					if (partyMember.isInsideZone(ZoneId.PEACE) || partyMember.isInsideZone(ZoneId.PEACE) || partyMember.isInsideZone(ZoneId.SIEGE))
						return DuelResult.CANCELED;
				}
			}
		}
		
		return DuelResult.CONTINUE;
	}
	
	public void doSurrender(L2PcInstance player)
	{
		if (_surrenderRequest != 0)
			return;
		
		// TODO: Can every party member cancel a party duel? or only the party leaders?
		if (_partyDuel)
		{
			if (_playerA.getParty().containsPlayer(player))
			{
				_surrenderRequest = 1;
				
				for (L2PcInstance partyPlayer : _playerA.getParty().getPartyMembers())
					partyPlayer.setDuelState(DuelState.DEAD);
				
				for (L2PcInstance partyPlayer : _playerB.getParty().getPartyMembers())
					partyPlayer.setDuelState(DuelState.WINNER);
			}
			else if (_playerB.getParty().containsPlayer(player))
			{
				_surrenderRequest = 2;
				
				for (L2PcInstance partyPlayer : _playerB.getParty().getPartyMembers())
					partyPlayer.setDuelState(DuelState.DEAD);
				
				for (L2PcInstance partyPlayer : _playerA.getParty().getPartyMembers())
					partyPlayer.setDuelState(DuelState.WINNER);
			}
		}
		else
		{
			if (player == _playerA)
			{
				_surrenderRequest = 1;
				
				_playerA.setDuelState(DuelState.DEAD);
				_playerB.setDuelState(DuelState.WINNER);
			}
			else if (player == _playerB)
			{
				_surrenderRequest = 2;
				
				_playerB.setDuelState(DuelState.DEAD);
				_playerA.setDuelState(DuelState.WINNER);
			}
		}
	}
	
	public void onPlayerDefeat(L2PcInstance player)
	{
		player.setDuelState(DuelState.DEAD);
		
		if (_partyDuel)
		{
			boolean teamDefeated = true;
			for (L2PcInstance partyPlayer : player.getParty().getPartyMembers())
			{
				if (partyPlayer.getDuelState() == DuelState.DUELLING)
				{
					teamDefeated = false;
					break;
				}
			}
			
			if (teamDefeated)
			{
				L2PcInstance winner = _playerA;
				if (_playerA.getParty().containsPlayer(player))
					winner = _playerB;
				
				for (L2PcInstance partyPlayer : winner.getParty().getPartyMembers())
					partyPlayer.setDuelState(DuelState.WINNER);
			}
		}
		else
		{
			if (_playerA == player)
				_playerB.setDuelState(DuelState.WINNER);
			else
				_playerA.setDuelState(DuelState.WINNER);
		}
	}
	
	public void onPartyEdit()
	{
		if (!_partyDuel)
			return;
		
		for (PlayerCondition cond : _playerConditions)
		{
			cond.teleportBack();
			cond.getPlayer().setIsInDuel(0);
		}
		
		endDuel(DuelResult.CANCELED);
	}
	
	public void onBuff(L2PcInstance player, L2Effect effect)
	{
		for (PlayerCondition cond : _playerConditions)
		{
			if (cond.getPlayer() == player)
			{
				cond.registerDebuff(effect);
				return;
			}
		}
	}
	
}