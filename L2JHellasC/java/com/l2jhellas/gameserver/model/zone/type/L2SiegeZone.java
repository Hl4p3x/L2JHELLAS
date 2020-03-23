package com.l2jhellas.gameserver.model.zone.type;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.xml.MapRegionTable;
import com.l2jhellas.gameserver.enums.ZoneId;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2SiegeSummonInstance;
import com.l2jhellas.gameserver.model.zone.L2ZoneType;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.taskmanager.PvpFlagTaskManager;

public class L2SiegeZone extends L2ZoneType
{
	private int _siegableId = -1;
	private boolean _isActiveSiege = false;
	private static final int DISMOUNT_DELAY = 5;
	
	public L2SiegeZone(int id)
	{
		super(id);
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("castleId"))
		{
			if (_siegableId != -1)
				throw new IllegalArgumentException("Siege object already defined!");
			_siegableId = Integer.parseInt(value);
		}
		else if (name.equals("clanHallId"))
		{
			if (_siegableId != -1)
				throw new IllegalArgumentException("Siege object already defined!");
			_siegableId = Integer.parseInt(value);
		}
		else
			super.setParameter(name, value);
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if (_isActiveSiege)
		{
			character.setInsideZone(ZoneId.PVP, true);
			character.setInsideZone(ZoneId.SIEGE, true);
			character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
			
			if (character instanceof L2PcInstance)
			{
				L2PcInstance activeChar = (L2PcInstance) character;
				
				activeChar.setIsInSiege(true); // in siege
				
				activeChar.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
				
				if (activeChar.getMountType() == 2)
				{
					activeChar.sendPacket(SystemMessageId.AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_WYVERN);
					activeChar.enteredNoLanding(DISMOUNT_DELAY);
				}
			}
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(ZoneId.PVP, false);
		character.setInsideZone(ZoneId.SIEGE, false);
		character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
		
		if (character instanceof L2PcInstance)
		{
			final L2PcInstance activeChar = (L2PcInstance) character;
			
			if (_isActiveSiege)
			{
				activeChar.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
				
				if (activeChar.getMountType() == 2)
					activeChar.exitedNoLanding();
				
				PvpFlagTaskManager.getInstance().add(activeChar, Config.PVP_NORMAL_TIME);
				
				// Set pvp flag
				if (activeChar.getPvpFlag() == 0)
					activeChar.updatePvPFlag(1);
			}
			
			activeChar.setIsInSiege(false);
		}
		else if (character instanceof L2SiegeSummonInstance)
			((L2SiegeSummonInstance) character).unSummon(((L2SiegeSummonInstance) character).getOwner());
	}
	
	@Override
	public void onDieInside(L2Character character)
	{
	}
	
	@Override
	public void onReviveInside(L2Character character)
	{
	}
	
	public void updateZoneStatusForCharactersInside()
	{
		if (_isActiveSiege)
		{
			for (L2Character character : _characterList.values())
			{
				if (character != null)
					onEnter(character);
			}
		}
		else
		{
			for (L2Character character : _characterList.values())
			{
				if (character == null)
					continue;
				
				character.setInsideZone(ZoneId.PVP, false);
				character.setInsideZone(ZoneId.SIEGE, false);
				character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
				
				if (character instanceof L2PcInstance)
				{
					final L2PcInstance player = ((L2PcInstance) character);
					
					player.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
					
					if (player.getMountType() == 2)
						player.exitedNoLanding();
				}
				else if (character instanceof L2SiegeSummonInstance)
					((L2SiegeSummonInstance) character).unSummon(((L2SiegeSummonInstance) character).getOwner());
			}
		}
	}
	
	public void announceToPlayers(String message)
	{
		for (L2PcInstance player : getKnownTypeInside(L2PcInstance.class))
			player.sendMessage(message);
	}
	
	public int getSiegeObjectId()
	{
		return _siegableId;
	}
	
	public boolean isActive()
	{
		return _isActiveSiege;
	}
	
	public void setIsActive(boolean val)
	{
		_isActiveSiege = val;
	}
	
	public void banishForeigners(L2Clan owningClan)
	{
		for (L2PcInstance player : getKnownTypeInside(L2PcInstance.class))
		{
			if (player.getClan() == owningClan || player.isGM())
				continue;
			
			player.teleToLocation(MapRegionTable.TeleportWhereType.TOWN);
		}
	}
}