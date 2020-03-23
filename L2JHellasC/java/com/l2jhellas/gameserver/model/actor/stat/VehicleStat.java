package com.l2jhellas.gameserver.model.actor.stat;

import com.l2jhellas.gameserver.model.actor.L2Vehicle;

public class VehicleStat extends CharStat
{
	private int _moveSpeed = 0;
	
	private int _rotationSpeed = 0;
	
	public VehicleStat(L2Vehicle activeChar)
	{
		super(activeChar);
	}
	
	public final int getRotationSpeed()
	{
		return _rotationSpeed;
	}
	
	public final void setRotationSpeed(int speed)
	{
		_rotationSpeed = speed;
	}
	
	public final void setMoveSpeed(int speed)
	{
		_moveSpeed = speed;
	}
		
	@Override
	public float getMoveSpeed()
	{
		return _moveSpeed;
	}
}