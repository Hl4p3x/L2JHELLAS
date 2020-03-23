package com.l2jhellas.gameserver.geodata;

import java.util.ArrayList;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.actor.position.Location;

public class GeoMove
{
	private static final ArrayList<Location> emptyTargetRecorder = new ArrayList<>(0);
	private static final ArrayList<ArrayList<Location>> emptyMovePath = new ArrayList<>(0);
	
	public static ArrayList<Location> findPath(int x, int y, int z, Location target, L2Object obj)
	{
		if (Math.abs(z - target._z) > 256)
			return emptyTargetRecorder;
		
		z = GeoEngine.getHeight(x, y, z);
		target._z = GeoEngine.getHeight(target);
		
		PathFind n = new PathFind(x, y, z, target._x, target._y, target._z, obj);
		
		if (n.getPath() == null || n.getPath().isEmpty())
			return emptyTargetRecorder;
		
		ArrayList<Location> targetRecorder = new ArrayList<>(n.getPath().size() + 2);
		
		targetRecorder.add(new Location(x, y, z));
		
		for (Location p : n.getPath())
			targetRecorder.add(p.geo2world());
		
		targetRecorder.add(target);
		
		if (Config.PATH_CLEAN)
			pathClean(targetRecorder);
		
		return targetRecorder;
	}
	
	public static ArrayList<ArrayList<Location>> findMovePath(int x, int y, int z, Location target, L2Object obj)
	{
		return getNodePath(findPath(x, y, z, target, obj));
	}
	
	public static ArrayList<ArrayList<Location>> getNodePath(ArrayList<Location> path)
	{
		int size = path.size();
		if (size <= 1)
			return emptyMovePath;
		ArrayList<ArrayList<Location>> result = new ArrayList<>();
		for (int i = 1; i < size; i++)
		{
			Location p2 = path.get(i);
			Location p1 = path.get(i - 1);
			ArrayList<Location> moveList = GeoEngine.MoveList(p1._x, p1._y, p1._z, p2._x, p2._y, true);
			if (moveList == null)
				return emptyMovePath;
			if (!moveList.isEmpty())
				result.add(moveList);
		}
		return result;
	}
	
	public static ArrayList<Location> constructMoveList(Location begin, Location end)
	{
		begin.world2geo();
		end.world2geo();
		
		ArrayList<Location> result = new ArrayList<>();
		
		int diff_x = end._x - begin._x, diff_y = end._y - begin._y, diff_z = end._z - begin._z;
		int dx = Math.abs(diff_x), dy = Math.abs(diff_y), dz = Math.abs(diff_z);
		float steps = Math.max(Math.max(dx, dy), dz);
		if (steps == 0)
			return result;
		
		float step_x = diff_x / steps, step_y = diff_y / steps, step_z = diff_z / steps;
		float next_x = begin._x, next_y = begin._y, next_z = begin._z;
		
		result.add(new Location(begin._x, begin._y, begin._z));
		
		for (int i = 0; i < steps; i++)
		{
			next_x += step_x;
			next_y += step_y;
			next_z += step_z;
			
			result.add(new Location((int) (next_x + 0.5f), (int) (next_y + 0.5f), (int) (next_z + 0.5f)));
		}
		
		return result;
	}
	
	private static void pathClean(ArrayList<Location> path)
	{
		int size = path.size();
		if (size > 2)
			for (int i = 2; i < size; i++)
			{
				Location p3 = path.get(i);
				Location p2 = path.get(i - 1);
				Location p1 = path.get(i - 2);
				if (p1.equals(p2) || p3.equals(p2) || IsPointInLine(p1, p2, p3))
				{
					path.remove(i - 1);
					size--;
					i = Math.max(2, i - 2);
				}
			}
		
		int current = 0;
		int sub;
		while (current < path.size() - 2)
		{
			sub = current + 2;
			while (sub < path.size())
			{
				Location one = path.get(current);
				Location two = path.get(sub);
				if (one.equals(two) || GeoEngine.canMoveWithCollision(one._x, one._y, one._z, two._x, two._y, two._z)) // canMoveWithCollision / canMoveToCoord
					while (current + 1 < sub)
					{
						path.remove(current + 1);
						sub--;
					}
				sub++;
			}
			current++;
		}
	}
	
	private static boolean IsPointInLine(Location p1, Location p2, Location p3)
	{
		if (p1._x == p3._x && p3._x == p2._x || p1._y == p3._y && p3._y == p2._y)
			return true;
		if ((p1._x - p2._x) * (p1._y - p2._y) == (p2._x - p3._x) * (p2._y - p3._y))
			return true;
		return false;
	}
}
