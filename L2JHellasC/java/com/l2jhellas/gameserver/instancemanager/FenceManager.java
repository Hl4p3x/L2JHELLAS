package com.l2jhellas.gameserver.instancemanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import com.l2jhellas.gameserver.datatables.xml.MapRegionTable;
import com.l2jhellas.gameserver.enums.FenceState;
import com.l2jhellas.gameserver.model.actor.instance.L2FenceInstance;

public final class FenceManager
{
	
	private final int MAX_Z_D = 100;
	
	private final Map<Integer, List<L2FenceInstance>> _regions = new ConcurrentHashMap<>();
	private final Map<Integer, L2FenceInstance> _fences = new ConcurrentHashMap<>();
	
	protected FenceManager()
	{
	}
	
	public void spawnFence(String name, int x, int y, int z, int width, int length, int height, FenceState state)
	{
		L2FenceInstance fence = new L2FenceInstance(name, x, y, width, length, height, state);
		fence.spawnMe(x, y, z);
		addFence(fence);
	}
	
	private void addFence(L2FenceInstance fence)
	{
		_fences.put(fence.getObjectId(), fence);
		_regions.computeIfAbsent(MapRegionTable.getMapRegion(fence.getX(), fence.getY()), key -> new ArrayList<>()).add(fence);
	}
	
	public void removeFence(L2FenceInstance fence)
	{
		_fences.remove(fence.getObjectId());
		
		final List<L2FenceInstance> fencesInRegion = _regions.get(MapRegionTable.getMapRegion(fence.getX(), fence.getY()));
		if (fencesInRegion != null)
			fencesInRegion.remove(fence);
	}
	
	public Map<Integer, L2FenceInstance> getFences()
	{
		return _fences;
	}
	
	public L2FenceInstance getFence(int objectId)
	{
		return _fences.get(objectId);
	}
	
	public int getFenceCount()
	{
		return _fences.size();
	}
	
	public boolean checkIfFenceBetween(double x, double y, double z, double tx, double ty, double tz)
	{
		final Predicate<L2FenceInstance> filter = fence ->
		{
			if (!fence.getState().isGeodataEnabled())
				return false;
			
			final double xMin = fence.getXMin();
			final double xMax = fence.getXMax();
			final double yMin = fence.getYMin();
			final double yMax = fence.getYMax();
			
			if ((x < xMin) && (tx < xMin))
				return false;
			
			if ((x > xMax) && (tx > xMax))
				return false;
			
			if ((y < yMin) && (ty < yMin))
				return false;
			
			if ((y > yMax) && (ty > yMax))
				return false;
			
			if ((x > xMin) && (tx > xMin) && (x < xMax) && (tx < xMax))
			{
				if ((y > yMin) && (ty > yMin) && (y < yMax) && (ty < yMax))
					return false;
			}
			
			if (crossLinePart(xMin, yMin, xMax, yMin, x, y, tx, ty, xMin, yMin, xMax, yMax) || crossLinePart(xMax, yMin, xMax, yMax, x, y, tx, ty, xMin, yMin, xMax, yMax) || crossLinePart(xMax, yMax, xMin, yMax, x, y, tx, ty, xMin, yMin, xMax, yMax) || crossLinePart(xMin, yMax, xMin, yMin, x, y, tx, ty, xMin, yMin, xMax, yMax))
			{
				if ((z > (fence.getZ() - MAX_Z_D)) && (z < (fence.getZ() + MAX_Z_D)))
					return true;
			}
			
			return false;
		};
		
		return _regions.getOrDefault(MapRegionTable.getMapRegion((int) x, (int) y), Collections.emptyList()).stream().anyMatch(filter);
	}
	
	private static boolean crossLinePart(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4, double xMin, double yMin, double xMax, double yMax)
	{
		final double[] result = intersection(x1, y1, x2, y2, x3, y3, x4, y4);
		
		if (result == null)
			return false;
		
		final double xCross = result[0];
		final double yCross = result[1];
		
		if ((xCross <= xMax) && (xCross >= xMin))
			return true;
		if ((yCross <= yMax) && (yCross >= yMin))
			return true;
		
		return false;
	}
	
	private static double[] intersection(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4)
	{
		final double d = ((x1 - x2) * (y3 - y4)) - ((y1 - y2) * (x3 - x4));
		
		if (d == 0)
			return null;
		
		final double xi = (((x3 - x4) * ((x1 * y2) - (y1 * x2))) - ((x1 - x2) * ((x3 * y4) - (y3 * x4)))) / d;
		final double yi = (((y3 - y4) * ((x1 * y2) - (y1 * x2))) - ((y1 - y2) * ((x3 * y4) - (y3 * x4)))) / d;
		
		return new double[]
		{
			xi,
			yi
		};
	}
	
	public void deleteAllFence()
	{
		if (!_fences.isEmpty())
			_fences.values().forEach(this::removeFence);
		
		if (!_regions.isEmpty())
			_regions.clear();
	}
	
	public static FenceManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final FenceManager INSTANCE = new FenceManager();
	}
}