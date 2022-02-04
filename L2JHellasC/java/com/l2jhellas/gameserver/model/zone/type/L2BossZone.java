package com.l2jhellas.gameserver.model.zone.type;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jhellas.gameserver.datatables.xml.MapRegionTable;
import com.l2jhellas.gameserver.enums.ZoneId;
import com.l2jhellas.gameserver.instancemanager.GrandBossManager;
import com.l2jhellas.gameserver.model.actor.L2Attackable;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.L2Summon;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.zone.L2ZoneType;

public class L2BossZone extends L2ZoneType
{
	// Track the times that players got disconnected. Players are allowed to log back into the zone as long as their log-out was within _timeInvade time...
	private final Map<Integer, Long> _playerAllowEntry = new ConcurrentHashMap<>();
	
	// Track players admitted to the zone who should be allowed back in after reboot/server downtime, within 30min of server restart
	private final Set<Integer> _playerAllowed = ConcurrentHashMap.newKeySet();
	
	private int _timeInvade;
	private boolean _enabled = true;
	private final int[] _oustLoc = new int[3];
	
	public L2BossZone(int id)
	{
		super(id);
		
		GrandBossManager.getInstance().addZone(this);
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("InvadeTime"))
			_timeInvade = Integer.parseInt(value);
		else if (name.equals("EnabledByDefault"))
			_enabled = Boolean.parseBoolean(value);
		else if (name.equals("oustX"))
			_oustLoc[0] = Integer.parseInt(value);
		else if (name.equals("oustY"))
			_oustLoc[1] = Integer.parseInt(value);
		else if (name.equals("oustZ"))
			_oustLoc[2] = Integer.parseInt(value);
		else
			super.setParameter(name, value);
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if (_enabled)
		{
			if (character instanceof L2PcInstance)
			{
				// Get player and set zone info.
				final L2PcInstance player = (L2PcInstance) character;
				player.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
				
				// Skip other checks for GM.
				if (player.isGM())
					return;
				
				// Get player object id.
				final int id = player.getObjectId();
				
				if (_playerAllowed.contains(id))
				{
					// Get and remove the entry expiration time (once entered, can not enter enymore, unless specified).
					final long entryTime = _playerAllowEntry.remove(id);
					if (entryTime > System.currentTimeMillis())
						return;
					
					// Player trying to join after expiration, remove from allowed list.
					_playerAllowed.remove(Integer.valueOf(id));
				}
				
				// Teleport out player, who attempt "illegal" (re-)entry.
				if (_oustLoc[0] != 0 && _oustLoc[1] != 0 && _oustLoc[2] != 0)
					player.teleToLocation(_oustLoc[0], _oustLoc[1], _oustLoc[2], false);
				else
					player.teleToLocation(MapRegionTable.TeleportWhereType.TOWN);
			}
			else if (character instanceof L2Summon)
			{
				final L2PcInstance player = ((L2Summon) character).getOwner();
				if (player != null)
				{
					if (_playerAllowed.contains(player.getObjectId()) || player.isGM())
						return;
					
					// Teleport out owner who attempt "illegal" (re-)entry.
					if (_oustLoc[0] != 0 && _oustLoc[1] != 0 && _oustLoc[2] != 0)
						player.teleToLocation(_oustLoc[0], _oustLoc[1], _oustLoc[2], false);
					else
						player.teleToLocation(MapRegionTable.TeleportWhereType.TOWN);
				}
				
				// Remove summon.
				((L2Summon) character).unSummon(player);
			}
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (character instanceof L2Playable && _enabled)
		{
			if (character instanceof L2PcInstance)
			{
				// Get player and set zone info.
				final L2PcInstance player = (L2PcInstance) character;
				player.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
				
				// Skip other checks for GM.
				if (player.isGM())
					return;
				
				// Get player object id.
				final int id = player.getObjectId();
				
				if (_playerAllowed.contains(id))
				{
					if (!player.isOnline())
					{
						// Player disconnected.
						_playerAllowEntry.put(id, System.currentTimeMillis() + _timeInvade);
					}
					else
					{
						// Player has allowed entry, do not delete from allowed list.
						if (_playerAllowEntry.containsKey(id))
							return;
						
						// Remove player allowed list.
						_playerAllowed.remove(Integer.valueOf(id));
					}
				}
			}
			
			// If playables aren't found, force all bosses to return to spawnpoint.
			if (!_characterList.isEmpty())
			{
				if (!getKnownTypeInside(L2Playable.class).isEmpty())
					return;
				
				for (L2Attackable raid : getKnownTypeInside(L2Attackable.class))
				{
					if (raid.isRaid())
					{
						if (raid.getSpawn() == null || raid.isDead())
							continue;
						
						if (!raid.isInsideRadius(raid.getSpawn().getLocx(), raid.getSpawn().getLocy(), 150, false))
							raid.returnHome();
					}
				}
			}
		}
		else if (character instanceof L2Attackable && character.isRaid() && !character.isDead())
			((L2Attackable) character).returnHome();
	}
	
	public void allowPlayerEntry(L2PcInstance player, int duration)
	{
		// Get player object id.
		final int playerId = player.getObjectId();
		
		// Allow player entry.
		if (!_playerAllowed.contains(playerId))
			_playerAllowed.add(playerId);
		
		// For the given duration.
		_playerAllowEntry.put(playerId, System.currentTimeMillis() + duration * 1000);
	}
	
	public void allowPlayerEntry(int playerId)
	{
		// Allow player entry.
		if (!_playerAllowed.contains(playerId))
			_playerAllowed.add(playerId);
		
		// For the given duration.
		_playerAllowEntry.put(playerId, System.currentTimeMillis() + _timeInvade);
	}
	
	public void removePlayer(L2PcInstance player)
	{
		// Get player object id.
		final int id = player.getObjectId();
		
		// Remove player from allowed list.
		_playerAllowed.remove(Integer.valueOf(id));
		
		// Remove player permission.
		_playerAllowEntry.remove(id);
	}
	
	public Set<Integer> getAllowedPlayers()
	{
		return _playerAllowed;
	}
	
	public void movePlayersTo(int x, int y, int z)
	{
		if (_characterList.isEmpty())
			return;
		
		for (L2PcInstance player : getKnownTypeInside(L2PcInstance.class))
		{
			if (player != null && player.isOnline() )
				player.teleToLocation(x, y, z, false);
		}
	}
	
	public void oustAllPlayers()
	{
		if (_characterList.isEmpty())
			return;
		
		for (L2PcInstance player : getKnownTypeInside(L2PcInstance.class))
		{
			if (player.isOnline())
			{
				if (_oustLoc[0] != 0 && _oustLoc[1] != 0 && _oustLoc[2] != 0)
					player.teleToLocation(_oustLoc[0], _oustLoc[1], _oustLoc[2], false);
				else
					player.teleToLocation(MapRegionTable.TeleportWhereType.TOWN);
			}
		}
		_playerAllowEntry.clear();
		_playerAllowed.clear();
	}
	
	@Override
	public void onDieInside(L2Character character)
	{
	}
	
	@Override
	public void onReviveInside(L2Character character)
	{
	}
}