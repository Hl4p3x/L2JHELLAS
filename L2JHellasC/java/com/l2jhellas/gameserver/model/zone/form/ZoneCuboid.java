package com.l2jhellas.gameserver.model.zone.form;

import java.awt.Rectangle;

import com.l2jhellas.gameserver.geodata.GeoEngine;
import com.l2jhellas.gameserver.model.actor.item.Inventory;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.model.zone.L2ZoneForm;
import com.l2jhellas.util.Rnd;

public class ZoneCuboid extends L2ZoneForm
{
	private final int _z1, _z2;
	Rectangle _r;
	
	public ZoneCuboid(int x1, int x2, int y1, int y2, int z1, int z2)
	{
		int _x1 = Math.min(x1, x2);
		int _x2 = Math.max(x1, x2);
		int _y1 = Math.min(y1, y2);
		int _y2 = Math.max(y1, y2);
		
		_r = new Rectangle(_x1, _y1, _x2 - _x1, _y2 - _y1);
		
		_z1 = Math.min(z1, z2);
		_z2 = Math.max(z1, z2);
	}
	
	@Override
	public boolean isInsideZone(double x, double y, double z)
	{
		return (_r.contains(x, y) && (z >= _z1) && (z <= _z2));
	}
	
	@Override
	public boolean intersectsRectangle(double ax1, double ax2, double ay1, double ay2)
	{
		return (_r.intersects(Math.min(ax1, ax2), Math.min(ay1, ay2), Math.abs(ax2 - ax1), Math.abs(ay2 - ay1)));
	}
	
	@Override
	public double getDistanceToZone(double x, double y)
	{
		int _x1 = _r.x;
		int _x2 = _r.x + _r.width;
		int _y1 = _r.y;
		int _y2 = _r.y + _r.height;
		double test, shortestDist = Math.pow(_x1 - x, 2) + Math.pow(_y1 - y, 2);
		
		test = Math.pow(_x1 - x, 2) + Math.pow(_y2 - y, 2);
		if (test < shortestDist)
		{
			shortestDist = test;
		}
		
		test = Math.pow(_x2 - x, 2) + Math.pow(_y1 - y, 2);
		if (test < shortestDist)
		{
			shortestDist = test;
		}
		
		test = Math.pow(_x2 - x, 2) + Math.pow(_y2 - y, 2);
		if (test < shortestDist)
		{
			shortestDist = test;
		}
		
		return Math.sqrt(shortestDist);
	}
	
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
		int _x1 = _r.x;
		int _x2 = _r.x + _r.width;
		int _y1 = _r.y;
		int _y2 = _r.y + _r.height;
		
		// x1->x2
		for (int x = _x1; x < _x2; x = x + STEP)
		{
			dropDebugItem(Inventory.ADENA_ID, 1, x, _y1, z);
			dropDebugItem(Inventory.ADENA_ID, 1, x, _y2, z);
		}
		// y1->y2
		for (int y = _y1; y < _y2; y = y + STEP)
		{
			dropDebugItem(Inventory.ADENA_ID, 1, _x1, y, z);
			dropDebugItem(Inventory.ADENA_ID, 1, _x2, y, z);
		}
	}
	
	@Override
	public Location getRandomPoint()
	{
		int x = Rnd.get(_r.x, _r.x + _r.width);
		int y = Rnd.get(_r.y, _r.y + _r.height);
		
		return new Location(x, y, GeoEngine.getHeight(x, y, _z1));
	}
}
