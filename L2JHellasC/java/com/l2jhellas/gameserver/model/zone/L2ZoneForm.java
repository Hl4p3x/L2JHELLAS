package com.l2jhellas.gameserver.model.zone;

import com.l2jhellas.gameserver.idfactory.IdFactory;
import com.l2jhellas.gameserver.instancemanager.ZoneManager;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.model.actor.position.Location;

public abstract class L2ZoneForm
{
	protected static final int STEP = 20;
	
	public abstract boolean isInsideZone(double x, double y, double z);
	
	public abstract boolean intersectsRectangle(double x1, double x2, double y1, double y2);
	
	public abstract double getDistanceToZone(double x, double y);
	
	public abstract int getLowZ(); // Support for the ability to extract the z coordinates of zones.
	
	public abstract int getHighZ(); // New fishing patch makes use of that to get the Z for the hook
	
	public abstract void visualizeZone(double z);
	
	// public abstract boolean isInsideZone(double x, double y, double z);
	
	// public abstract boolean isInsideZone(int x, int y, int z);
	
	// public abstract boolean intersectsRectangle(int x1, int x2, int y1, int y2);
	
	// public abstract double getDistanceToZone(int x, int y);
	
	// public abstract int getLowZ(); //Support for the ability to extract the z coordinates of zones.
	//
	// public abstract int getHighZ(); //New fishing patch makes use of that to get the Z for the hook
	
	// landing coordinates.
	protected boolean lineSegmentsIntersect(int ax1, int ay1, int ax2, int ay2, int bx1, int by1, int bx2, int by2)
	{
		return java.awt.geom.Line2D.linesIntersect(ax1, ay1, ax2, ay2, bx1, by1, bx2, by2);
	}
	
	protected final static void dropDebugItem(int itemId, int num, int x, int y, double z)
	{
		L2ItemInstance item = new L2ItemInstance(IdFactory.getInstance().getNextId(), itemId);
		item.setCount(num);
		item.spawnMe(x, y, (int) z + 5);
		ZoneManager.getInstance().getDebugItems().add(item);
	}
	
	public abstract Location getRandomPoint();
}