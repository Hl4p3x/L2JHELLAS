package com.l2jhellas.gameserver.model.zone.type;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.enums.ZoneId;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.zone.L2SpawnZone;

public class L2TownZone extends L2SpawnZone
{
	private int _townId;
	private int _castleId;
	private boolean _isPeaceZone;
	
	public L2TownZone(int id)
	{
		super(id);
		
		// Default peace zone
		_isPeaceZone = true;
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("townId"))
			_townId = Integer.parseInt(value);
		else if (name.equals("castleId"))
			_castleId = Integer.parseInt(value);
		else if (name.equals("isPeaceZone"))
			_isPeaceZone = Boolean.parseBoolean(value);
		else
			super.setParameter(name, value);
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		// PVP possible during siege, now for siege participants only
		// Could also check if this town is in siege, or if any siege is going on
		if (character instanceof L2PcInstance && ((L2PcInstance) character).getSiegeState() != 0 && Config.ZONE_TOWN == 1)
			return;
		
		if (_isPeaceZone && Config.ZONE_TOWN != 2)
			character.setInsideZone(ZoneId.PEACE, true);
		
		character.setInsideZone(ZoneId.TOWN, true);
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		
		if (_isPeaceZone)
			character.setInsideZone(ZoneId.PEACE, false);
		
		character.setInsideZone(ZoneId.TOWN, false);
	}
	
	@Override
	public void onDieInside(L2Character character)
	{
	}
	
	@Override
	public void onReviveInside(L2Character character)
	{
	}
	
	public int getTownId()
	{
		return _townId;
	}
	
	public final int getTaxById()
	{
		return _castleId;
	}
	
	public final boolean isPeaceZone()
	{
		return _isPeaceZone;
	}
}