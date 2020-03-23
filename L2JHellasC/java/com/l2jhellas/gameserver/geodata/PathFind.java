package com.l2jhellas.gameserver.geodata;

import java.util.ArrayList;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.geodata.PathFindBuffers.GeoNode;
import com.l2jhellas.gameserver.geodata.PathFindBuffers.PathFindBuffer;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.actor.L2Npc;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.position.Location;

public class PathFind
{
	private static final byte NSWE_NONE = 0, EAST = 1, WEST = 2, SOUTH = 4, NORTH = 8, NSWE_ALL = 15;
	
	private PathFindBuffer buff;
	
	private ArrayList<Location> path;
	
	public PathFind(int x, int y, int z, int destX, int destY, int destZ, L2Object obj)
	{
		Location startpoint = Config.PATHFIND_BOOST == 0 ? new Location(x, y, z) : GeoEngine.moveCheckWithCollision(x, y, z, destX, destY, true);
		Location native_endpoint = new Location(destX, destY, destZ);
		Location endpoint = Config.PATHFIND_BOOST != 2 || Math.abs(destZ - z) > 200 ? native_endpoint.clone() : GeoEngine.moveCheckBackwardWithCollision(destX, destY, destZ, startpoint._x, startpoint._y, true);
		
		startpoint.world2geo();
		native_endpoint.world2geo();
		endpoint.world2geo();
		
		startpoint._z = GeoEngine.NgetHeight(startpoint._x, startpoint._y, startpoint._z);
		endpoint._z = GeoEngine.NgetHeight(endpoint._x, endpoint._y, endpoint._z);
		
		int xdiff = Math.abs(endpoint._x - startpoint._x);
		int ydiff = Math.abs(endpoint._y - startpoint._y);
		
		if (xdiff == 0 && ydiff == 0)
		{
			if (Math.abs(endpoint._z - startpoint._z) < 32)
			{
				path = new ArrayList<>();
				path.add(0, startpoint);
			}
			return;
		}
		
		if ((buff = PathFindBuffers.alloc(64 + 2 * Math.max(xdiff, ydiff), obj instanceof L2Playable, startpoint, endpoint, native_endpoint)) != null)
		{
			path = findPath();
			
			buff.free();
			
			if (obj instanceof L2Npc)
			{
				L2Npc npc = (L2Npc) obj;
				npc.pathfindCount++;
				npc.pathfindTime += (System.nanoTime() - buff.useStartedNanos) / 1000000.0;
			}
		}
	}
	
	public ArrayList<Location> findPath()
	{
		buff.firstNode = GeoNode.initNode(buff, buff.startpoint._x - buff.offsetX, buff.startpoint._y - buff.offsetY, buff.startpoint);
		buff.firstNode.closed = true;
		
		GeoNode nextNode = buff.firstNode, finish = null;
		int i = buff.info.maxIterations;
		
		while (nextNode != null && i-- > 0)
		{
			if ((finish = handleNode(nextNode)) != null)
				return tracePath(finish);
			nextNode = getBestOpenNode();
		}
		
		return null;
	}
	
	private GeoNode getBestOpenNode()
	{
		GeoNode bestNodeLink = null;
		GeoNode oldNode = buff.firstNode;
		GeoNode nextNode = buff.firstNode.link;
		
		while (nextNode != null)
		{
			if (bestNodeLink == null || nextNode.score < bestNodeLink.link.score)
				bestNodeLink = oldNode;
			oldNode = nextNode;
			nextNode = oldNode.link;
		}
		
		if (bestNodeLink != null)
		{
			bestNodeLink.link.closed = true;
			GeoNode bestNode = bestNodeLink.link;
			bestNodeLink.link = bestNode.link;
			if (bestNode == buff.currentNode)
				buff.currentNode = bestNodeLink;
			return bestNode;
		}
		
		return null;
	}
	
	private static ArrayList<Location> tracePath(GeoNode f)
	{
		ArrayList<Location> locations = new ArrayList<>();
		do
		{
			locations.add(0, f.getLoc());
			f = f.parent;
		}
		while (f.parent != null);
		return locations;
	}
	
	public GeoNode handleNode(GeoNode node)
	{
		GeoNode result = null;
		
		int clX = node._x;
		int clY = node._y;
		short clZ = node._z;
		
		getHeightAndNSWE(clX, clY, clZ);
		short NSWE = buff.hNSWE[1];
		
		if (Config.PATHFIND_DIAGONAL)
		{
			if ((NSWE & SOUTH) == SOUTH && (NSWE & EAST) == EAST)
			{
				getHeightAndNSWE(clX + 1, clY, clZ);
				if ((buff.hNSWE[1] & SOUTH) == SOUTH)
				{
					getHeightAndNSWE(clX, clY + 1, clZ);
					if ((buff.hNSWE[1] & EAST) == EAST)
					{
						result = getNeighbour(clX + 1, clY + 1, node, true);
						if (result != null)
							return result;
					}
				}
			}
			
			if ((NSWE & SOUTH) == SOUTH && (NSWE & WEST) == WEST)
			{
				getHeightAndNSWE(clX - 1, clY, clZ);
				if ((buff.hNSWE[1] & SOUTH) == SOUTH)
				{
					getHeightAndNSWE(clX, clY + 1, clZ);
					if ((buff.hNSWE[1] & WEST) == WEST)
					{
						result = getNeighbour(clX - 1, clY + 1, node, true);
						if (result != null)
							return result;
					}
				}
			}
			
			if ((NSWE & NORTH) == NORTH && (NSWE & EAST) == EAST)
			{
				getHeightAndNSWE(clX + 1, clY, clZ);
				if ((buff.hNSWE[1] & NORTH) == NORTH)
				{
					getHeightAndNSWE(clX, clY - 1, clZ);
					if ((buff.hNSWE[1] & EAST) == EAST)
					{
						result = getNeighbour(clX + 1, clY - 1, node, true);
						if (result != null)
							return result;
					}
				}
			}
			
			if ((NSWE & NORTH) == NORTH && (NSWE & WEST) == WEST)
			{
				getHeightAndNSWE(clX - 1, clY, clZ);
				if ((buff.hNSWE[1] & NORTH) == NORTH)
				{
					getHeightAndNSWE(clX, clY - 1, clZ);
					if ((buff.hNSWE[1] & WEST) == WEST)
					{
						result = getNeighbour(clX - 1, clY - 1, node, true);
						if (result != null)
							return result;
					}
				}
			}
		}
		
		if ((NSWE & EAST) == EAST)
		{
			result = getNeighbour(clX + 1, clY, node, false);
			if (result != null)
				return result;
		}
		
		if ((NSWE & WEST) == WEST)
		{
			result = getNeighbour(clX - 1, clY, node, false);
			if (result != null)
				return result;
		}
		
		if ((NSWE & SOUTH) == SOUTH)
		{
			result = getNeighbour(clX, clY + 1, node, false);
			if (result != null)
				return result;
		}
		
		if ((NSWE & NORTH) == NORTH)
			result = getNeighbour(clX, clY - 1, node, false);
		
		return result;
	}
	
	public GeoNode getNeighbour(int x, int y, GeoNode from, boolean d)
	{
		int nX = x - buff.offsetX, nY = y - buff.offsetY;
		if (nX >= buff.info.MapSize || nX < 0 || nY >= buff.info.MapSize || nY < 0)
			return null;
		
		boolean isOldNull = GeoNode.isNull(buff.nodes[nX][nY]);
		if (!isOldNull && buff.nodes[nX][nY].closed)
			return null;
		
		GeoNode n = isOldNull ? GeoNode.initNode(buff, nX, nY, x, y, from._z, from) : buff.tempNode.reuse(buff.nodes[nX][nY], from);
		
		int height = Math.abs(n._z - from._z);
		
		if (height > Config.PATHFIND_MAX_Z_DIFF || n._nswe == NSWE_NONE)
			return null;
		
		double weight = d ? 1.414213562373095 * Config.WEIGHT0 : Config.WEIGHT0;
		
		if (n._nswe != NSWE_ALL || height > 16)
			weight = Config.WEIGHT1;
		else
			while (buff.isPlayer || Config.SIMPLE_PATHFIND_FOR_MOBS)
			{
				getHeightAndNSWE(x + 1, y, n._z);
				if (buff.hNSWE[1] != NSWE_ALL || Math.abs(n._z - buff.hNSWE[0]) > 16)
				{
					weight = Config.WEIGHT2;
					break;
				}
				
				getHeightAndNSWE(x - 1, y, n._z);
				if (buff.hNSWE[1] != NSWE_ALL || Math.abs(n._z - buff.hNSWE[0]) > 16)
				{
					weight = Config.WEIGHT2;
					break;
				}
				
				getHeightAndNSWE(x, y + 1, n._z);
				if (buff.hNSWE[1] != NSWE_ALL || Math.abs(n._z - buff.hNSWE[0]) > 16)
				{
					weight = Config.WEIGHT2;
					break;
				}
				
				getHeightAndNSWE(x, y - 1, n._z);
				if (buff.hNSWE[1] != NSWE_ALL || Math.abs(n._z - buff.hNSWE[0]) > 16)
				{
					weight = Config.WEIGHT2;
					break;
				}
				
				break;
			}
		
		int diffx = buff.endpoint._x - x;
		int diffy = buff.endpoint._y - y;
		// int diffx = Math.abs(buff.endpoint.x - x);
		// int diffy = Math.abs(buff.endpoint.y - y);
		int dz = Math.abs(buff.endpoint._z - n._z);
		
		n.moveCost += from.moveCost + weight;
		n.score = n.moveCost + (Config.PATHFIND_DIAGONAL ? Math.sqrt(diffx * diffx + diffy * diffy + dz * dz / 256) : Math.abs(diffx) + Math.abs(diffy) + dz / 16); // 256 = 16*16
		// n.score = n.moveCost + diffx + diffy + dz / 16;
		
		if (x == buff.endpoint._x && y == buff.endpoint._y && dz < 64)
			return n;
		
		if (isOldNull)
		{
			if (buff.currentNode == null)
				buff.firstNode.link = n;
			else
				buff.currentNode.link = n;
			buff.currentNode = n;
			
		}
		else if (n.moveCost < buff.nodes[nX][nY].moveCost)
			buff.nodes[nX][nY].copy(n);
		
		return null;
	}
	
	private void getHeightAndNSWE(int x, int y, short z)
	{
		int nX = x - buff.offsetX, nY = y - buff.offsetY;
		if (nX >= buff.info.MapSize || nX < 0 || nY >= buff.info.MapSize || nY < 0)
		{
			buff.hNSWE[1] = NSWE_NONE;
			return;
		}
		GeoNode n = buff.nodes[nX][nY];
		if (n == null)
			n = GeoNode.initNodeGeo(buff, nX, nY, x, y, z);
		buff.hNSWE[0] = n._z;
		buff.hNSWE[1] = n._nswe;
	}
	
	public ArrayList<Location> getPath()
	{
		return path;
	}
}
