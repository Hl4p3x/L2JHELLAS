package com.l2jhellas.gameserver.model.zone;

import com.l2jhellas.gameserver.instancemanager.CastleManager;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.entity.Castle;

public abstract class L2CastleZoneType extends L2ZoneType
{
	private int _castleId;
	private Castle _castle;
	
	protected boolean _enabled;
	
	protected L2CastleZoneType(int id)
	{
		super(id);
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("castleId"))
			_castleId = Integer.parseInt(value);
		else
			super.setParameter(name, value);
	}
	
	@Override
	public void onDieInside(L2Character character)
	{
	}
	
	@Override
	public void onReviveInside(L2Character character)
	{
	}
	
	public Castle getCastle()
	{
		if (_castleId > 0 && _castle == null)
			_castle = CastleManager.getInstance().getCastleById(_castleId);
		
		return _castle;
	}
	
	public boolean isEnabled()
	{
		return _enabled;
	}
	
	public void setEnabled(boolean val)
	{
		_enabled = val;
	}
}