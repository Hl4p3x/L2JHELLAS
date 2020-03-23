package com.l2jhellas.gameserver.model;

import com.l2jhellas.gameserver.model.actor.position.Location;

public class VehiclePathPoint extends Location
{
	private final int _moveSpeed;
	private final int _rotationSpeed;
	
	public VehiclePathPoint(Location loc)
	{
		this(loc.getX(), loc.getY(), loc.getZ());
	}
	
	public VehiclePathPoint(int x, int y, int z)
	{
		super(x, y, z);
		_moveSpeed = 350;
		_rotationSpeed = 4000;
	}
	
	public VehiclePathPoint(int x, int y, int z, int moveSpeed, int rotationSpeed)
	{
		super(x, y, z);
		_moveSpeed = moveSpeed;
		_rotationSpeed = rotationSpeed;
	}
	
	public int getMoveSpeed()
	{
		return _moveSpeed;
	}
	
	public int getRotationSpeed()
	{
		return _rotationSpeed;
	}
}