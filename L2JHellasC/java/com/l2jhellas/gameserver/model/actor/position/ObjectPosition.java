package com.l2jhellas.gameserver.model.actor.position;

import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.L2WorldRegion;

public class ObjectPosition
{
	
	private final L2Object _activeObject;
	private int _heading = 0;
	private Location _worldPosition;
	private L2WorldRegion _worldRegion; // Object localization : Used for items/chars that are seen in the world
	
	public ObjectPosition(L2Object activeObject)
	{
		_activeObject = activeObject;
		setWorldRegion(L2World.getInstance().getRegion(getWorldPosition()));
	}
	
	public final void setXYZ(int x, int y, int z)
	{
		setWorldPosition(x, y, z);

		final L2WorldRegion oldRegion = getWorldRegion();
		final L2WorldRegion newRegion = L2World.getInstance().getRegion(getWorldPosition());

		if (newRegion != oldRegion)
		{
			if (oldRegion != null)
				oldRegion.removeVisibleObject(getActiveObject());

			if (getActiveObject().isVisible())			
			    newRegion.addVisibleObject(getActiveObject());
			
			   L2World.switchRegion(getActiveObject(), newRegion);
			   setWorldRegion(newRegion);
		}
	}
	
	public final void setXYZInvisible(int x, int y, int z)
	{
		if (x > L2World.WORLD_X_MAX)
			x = L2World.WORLD_X_MAX - 5000;
		
		if (x < L2World.WORLD_X_MIN)
			x = L2World.WORLD_X_MIN + 5000;
		
		if (y > L2World.WORLD_Y_MAX)
			y = L2World.WORLD_Y_MAX - 5000;
		
		if (y < L2World.WORLD_Y_MIN)
			y = L2World.WORLD_Y_MIN + 5000;
		
		getActiveObject().setXYZ(x, y, z);
		getActiveObject().setIsVisible(false);
	}
	
	public L2Object getActiveObject()
	{
		return _activeObject;
	}
	
	public final int getX()
	{
		return getWorldPosition().getX();
	}
	
	public final int getY()
	{
		return getWorldPosition().getY();
	}
	
	public final int getZ()
	{
		return getWorldPosition().getZ();
	}
	
	public final Location getWorldPosition()
	{
		if (_worldPosition == null)
			_worldPosition = new Location(0, 0, 0);
		
		return _worldPosition;
	}
	
	public final void setWorldPosition(int x, int y, int z)
	{
		getWorldPosition().setXYZ(x, y, z);
	}
	
	public final void setWorldPosition(Location newPosition)
	{
		setWorldPosition(newPosition.getX(), newPosition.getY(), newPosition.getZ());
	}
	
	public final L2WorldRegion getWorldRegion()
	{
		return _worldRegion;
	}
	
	public void setWorldRegion(L2WorldRegion newRegion)
	{
		_worldRegion = newRegion;
	}
	
	public final int getHeading()
	{
		return _heading;
	}
	
	public final void setHeading(int value)
	{
		_heading = value;
	}
}