package com.l2jhellas.gameserver.model.zone.form;

import com.l2jhellas.gameserver.geodata.GeoEngine;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.model.zone.L2ZoneForm;
import com.l2jhellas.util.Rnd;

public class ZoneCylinder extends L2ZoneForm
{
	private final int _x, _y, _z1, _z2, _rad, _radS;
	
	public ZoneCylinder(int x, int y, int z1, int z2, int rad)
	{
		_x = x;
		_y = y;
		_z1 = z1;
		_z2 = z2;
		_rad = rad;
		_radS = rad * rad;
	}
	
	@Override
	public boolean isInsideZone(double x, double y, double z)
	{
		if (((Math.pow(_x - x, 2) + Math.pow(_y - y, 2)) > _radS) || (z < _z1) || (z > _z2))
		{
			return false;
		}
		return true;
	}
	
	@Override
	public boolean intersectsRectangle(double ax1, double ax2, double ay1, double ay2)
	{
		// Circles point inside the rectangle?
		if ((_x > ax1) && (_x < ax2) && (_y > ay1) && (_y < ay2))
		{
			return true;
		}
		
		// Any point of the rectangle intersecting the Circle?
		if ((Math.pow(ax1 - _x, 2) + Math.pow(ay1 - _y, 2)) < _radS)
		{
			return true;
		}
		if ((Math.pow(ax1 - _x, 2) + Math.pow(ay2 - _y, 2)) < _radS)
		{
			return true;
		}
		if ((Math.pow(ax2 - _x, 2) + Math.pow(ay1 - _y, 2)) < _radS)
		{
			return true;
		}
		if ((Math.pow(ax2 - _x, 2) + Math.pow(ay2 - _y, 2)) < _radS)
		{
			return true;
		}
		
		// Collision on any side of the rectangle?
		if ((_x > ax1) && (_x < ax2))
		{
			if (Math.abs(_y - ay2) < _rad)
			{
				return true;
			}
			if (Math.abs(_y - ay1) < _rad)
			{
				return true;
			}
		}
		if ((_y > ay1) && (_y < ay2))
		{
			if (Math.abs(_x - ax2) < _rad)
			{
				return true;
			}
			if (Math.abs(_x - ax1) < _rad)
			{
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public double getDistanceToZone(double x, double y)
	{
		return (Math.sqrt((Math.pow(_x - x, 2) + Math.pow(_y - y, 2))) - _rad);
	}
	
	// getLowZ() / getHighZ() - These two functions were added to cope with the demand of the new fishing algorithms, wich are now able to correctly place the hook in the water, thanks to getHighZ(). getLowZ() was added, considering potential future modifications.
	@Override
	public int getLowZ()
	{
		return _z1;
	}
	
	@Override
	public int getHighZ()
	{
		return _z2;
	}
	
	@Override
	public void visualizeZone(double z)
	{
		int count = (int) ((2 * Math.PI * _rad) / STEP);
		double angle = (2 * Math.PI) / count;
		for (int i = 0; i < count; i++)
		{
			int x = (int) (Math.cos(angle * i) * _rad);
			int y = (int) (Math.sin(angle * i) * _rad);
			dropDebugItem(57, 1, _x + x, _y + y, z);
		}
	}
	
	@Override
	public Location getRandomPoint()
	{
		int x, y, q, r;
		
		q = (int) (Rnd.nextInt() * 2 * Math.PI);
		r = (int) Math.sqrt(Rnd.nextInt());
		x = (int) ((_rad * r * Math.cos(q)) + _x);
		y = (int) ((_rad * r * Math.sin(q)) + _y);
		
		return new Location(x, y, GeoEngine.getHeight(x, y, _z1));
	}
}