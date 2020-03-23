package com.l2jhellas.gameserver.model.zone.type;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.datatables.xml.MapRegionTable;
import com.l2jhellas.gameserver.enums.ZoneId;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.entity.olympiad.OlympiadGameTask;
import com.l2jhellas.gameserver.model.zone.L2SpawnZone;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ExOlympiadMatchEnd;
import com.l2jhellas.gameserver.network.serverpackets.ExOlympiadUserInfo;
import com.l2jhellas.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public class L2OlympiadStadiumZone extends L2SpawnZone
{
	OlympiadGameTask _task = null;
	private int _stadiumId;
	
	public L2OlympiadStadiumZone(int id)
	{
		super(id);
	}
	
	public final void registerTask(OlympiadGameTask task)
	{
		_task = task;
	}
	
	public final void broadcastStatusUpdate(L2PcInstance player)
	{
		final ExOlympiadUserInfo packet = new ExOlympiadUserInfo(player, 2);
		for (L2PcInstance plyr : getKnownTypeInside(L2PcInstance.class))
		{
			if (plyr.inObserverMode() || plyr.getOlympiadSide() != player.getOlympiadSide())
				plyr.sendPacket(packet);
		}
	}
	
	public final void broadcastPacketToObservers(L2GameServerPacket packet)
	{
		for (L2PcInstance player : getKnownTypeInside(L2PcInstance.class))
		{
			if (player.inObserverMode())
				player.sendPacket(packet);
		}
	}
	
	@Override
	protected final void onEnter(L2Character character)
	{
		character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
		character.setInsideZone(ZoneId.NO_RESTART, true);
		
		if (_task != null)
		{
			if (_task.isBattleStarted())
			{
				character.setInsideZone(ZoneId.PVP, true);
				if (character instanceof L2PcInstance)
				{
					character.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ENTERED_COMBAT_ZONE));
					_task.getGame().sendOlympiadInfo(character);
				}
			}
		}
		
		if (character instanceof L2Playable)
		{
			final L2PcInstance player = character.getActingPlayer();
			if (player != null)
			{
				// only participants, observers and GMs allowed
				if (!player.isGM() && !player.isInOlympiadMode() && !player.inObserverMode() && !player.isInDuel())
					ThreadPoolManager.getInstance().executeTask(new KickPlayer(player));
			}
		}
	}
	
	@Override
	protected final void onExit(L2Character character)
	{
		character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
		character.setInsideZone(ZoneId.NO_RESTART, false);
		
		if (_task != null)
		{
			if (_task.isBattleStarted())
			{
				character.setInsideZone(ZoneId.PVP, false);
				if (character instanceof L2PcInstance)
				{
					character.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));
					character.sendPacket(ExOlympiadMatchEnd.STATIC_PACKET);
				}
			}
		}
	}
	
	public final void updateZoneStatusForCharactersInside()
	{
		if (_task == null)
			return;
		
		final boolean battleStarted = _task.isBattleStarted();
		final SystemMessage sm;
		if (battleStarted)
			sm = SystemMessage.getSystemMessage(SystemMessageId.ENTERED_COMBAT_ZONE);
		else
			sm = SystemMessage.getSystemMessage(SystemMessageId.LEFT_COMBAT_ZONE);
		
		for (L2Character character : _characterList.values())
		{
			if (character == null)
				continue;
			
			if (battleStarted)
			{
				character.setInsideZone(ZoneId.PVP, true);
				if (character instanceof L2PcInstance)
					character.sendPacket(sm);
			}
			else
			{
				character.setInsideZone(ZoneId.PVP, false);
				if (character instanceof L2PcInstance)
				{
					character.sendPacket(sm);
					character.sendPacket(ExOlympiadMatchEnd.STATIC_PACKET);
				}
			}
		}
	}
	
	@Override
	public void onDieInside(L2Character character)
	{
	}
	
	@Override
	public void onReviveInside(L2Character character)
	{
	}
	
	private static final class KickPlayer implements Runnable
	{
		private L2PcInstance _player;
		
		public KickPlayer(L2PcInstance player)
		{
			_player = player;
		}
		
		@Override
		public void run()
		{
			if (_player != null)
			{
				final L2Summon summon = _player.getPet();
				if (summon != null)
					summon.unSummon(_player);
				
				_player.teleToLocation(MapRegionTable.TeleportWhereType.TOWN);
				_player = null;
			}
		}
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("stadiumId"))
		{
			_stadiumId = Integer.parseInt(value);
		}
		else
			super.setParameter(name, value);
	}
	
	public int getStadiumId()
	{
		return _stadiumId;
	}
}