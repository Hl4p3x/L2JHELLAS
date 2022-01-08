package com.l2jhellas.gameserver.model.actor.position;

import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.templates.StatsSet;
import com.l2jhellas.util.Util;

public class Location
{
	public volatile int _x, _y, _z;
	private int _heading;
	
	public Location(int x, int y, int z)
	{
		_x = x;
		_y = y;
		_z = z;
	}
	
	public Location(L2Character character)
	{
		_x = character.getX();
		_y = character.getY();
		_z = character.getZ();
	}
	
	public Location()
	{

	}
	
	public Location(int x, int y, int z, int heading)
	{
		_x = x;
		_y = y;
		_z = z;
		_heading = heading;
	}
	
	public Location(StatsSet loc)
	{
		_x = loc.getInteger("x");
		_y = loc.getInteger("y");
		_z = loc.getInteger("z");
	}
	
	public int getX()
	{
		return _x;
	}
	
	public int getY()
	{
		return _y;
	}
	
	public int getZ()
	{
		return _z;
	}
	
	public int getHeading()
	{
		return _heading;
	}
	
	public void setHeading(int heading)
	{
		_heading = heading;
	}
	
	public void setXYZ(int x, int y, int z)
	{
		_x = x;
		_y = y;
		_z = z;
	}
	
	public Location world2geo()
	{
		_x = _x - L2World.WORLD_X_MIN >> 4;
		_y = _y - L2World.WORLD_Y_MIN >> 4;
		return this;
	}
	
	public Location geo2world()
	{
		_x = (_x << 4) + L2World.WORLD_X_MIN + 8;
		_y = (_y << 4) + L2World.WORLD_Y_MIN + 8;
		return this;
	}
	
	@Override
	public String toString()
	{
		return "(" + _x + ", " + _y + ", " + _z + ")";
	}
	
	@Override
	public int hashCode()
	{
		return _x ^ _y ^ _z;
	}
	
	public Location setH(int h)
	{
		_heading = h;
		return this;
	}
	
	public void set(int x, int y, int z, int h)
	{
		_x = x;
		_y = y;
		_z = z;
		_heading = h;
	}
	
	public void set(int x, int y, int z)
	{
		_x = x;
		_y = y;
		_z = z;
	}
	
	public void setZ(int z)
	{
		_z = z;
	}
	
	public void set(Location loc)
	{
		_x = loc.getX();
		_y = loc.getY();
		_z = loc.getZ();
		_heading = loc.getHeading();
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o instanceof Location)
		{
			Location point3D = (Location) o;
			return (point3D._x == _x && point3D._y == _y && point3D._z == _z);
		}
		
		return false;
	}
	
	public boolean equals(int x, int y, int z)
	{
		return _x == x && _y == y && _z == z;
	}
	
	@Override
	public Location clone()
	{
		return new Location(_x, _y, _z, _heading);
	}
	
	public double getDistance(Location location)
	{
		double dx = location.getX() - getX();
		double dy = location.getY() - getY();
		double dz = location.getZ() - getZ();
		
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}
	
	public void clean()
	{
		_x = 0;
		_y = 0;
		_z = 0;
	}

	public int calculateHeadingTo(L2Character activeChar)
	{
		return Util.calculateHeadingFrom(getX(), getY(), activeChar.getX(), activeChar.getY());
	}
	
	public int calculateHeadingTo(L2Object activeChar)
	{
		return Util.calculateHeadingFrom(getX(), getY(), activeChar.getX(), activeChar.getY());
	}
}