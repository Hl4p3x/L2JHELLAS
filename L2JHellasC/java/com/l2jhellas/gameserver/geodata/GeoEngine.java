package com.l2jhellas.gameserver.geodata;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.xml.FenceData;
import com.l2jhellas.gameserver.geodata.GeoOptimizer.BlockLink;
import com.l2jhellas.gameserver.geodata.loader.GeoFileInfo;
import com.l2jhellas.gameserver.geodata.loader.GeoLoader;
import com.l2jhellas.gameserver.geodata.loader.GeoLoaderFactory;
import com.l2jhellas.gameserver.geometry.Point3D;
import com.l2jhellas.gameserver.geometry.Polygon;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.position.Location;
import com.l2jhellas.gameserver.taskmanager.MemoryWatchOptimize;
import com.l2jhellas.util.MathUtil;
import com.l2jhellas.util.ParallelExecutor;
import com.l2jhellas.util.Rnd;
import com.l2jhellas.util.Util;

public class GeoEngine
{
	private static final Logger log = Logger.getLogger(GeoEngine.class.getName());
	
	public static final byte EAST = 1, WEST = 2, SOUTH = 4, NORTH = 8, NSWE_ALL = 15, NSWE_NONE = 0;
	public static final byte BLOCKTYPE_FLAT = 0;
	public static final byte BLOCKTYPE_COMPLEX = 1;
	public static final byte BLOCKTYPE_MULTILEVEL = 2;
	public static final int BlocksInMap = 256 * 256;
	public static int MAX_LAYERS = 1;
		
	private static final byte[][][][] geodata = new byte[L2World.WORLD_SIZE_X][L2World.WORLD_SIZE_Y][][];
	
	// If geo is off do simple check
	public static boolean canSeeTarget(L2Object cha, L2Object target)
	{		
		// Don't allow casting on players on different dungeon lvls etc
		return Math.abs(target.getZ() - cha.getZ()) < 1000;
	}
	
	// If geo is off do simple check
	public static boolean canSeeTarget(L2Object cha, Point3D worldPosition)
	{
		// Don't allow casting on players on different dungeon lvls etc
		return Math.abs(worldPosition.getZ() - cha.getZ()) < 1000;
	}
	
	// If geo is off do simple check
	public static boolean canSeeTarget(int z, int tz)
	{
		// Don't allow casting on players on different dungeon lvls etc
		return (Math.abs(z - tz) < 1000);
	}
	
	public static short getType(int x, int y)
	{
		return NgetType(x - L2World.WORLD_X_MIN >> 4, y - L2World.WORLD_Y_MIN >> 4);
	}
	
	public static final int getGeoX(int worldX)
	{
		return (MathUtil.limit(worldX, L2World.WORLD_X_MIN, L2World.WORLD_X_MAX) - L2World.WORLD_X_MIN) >> 4;
	}

	public static final int getGeoY(int worldY)
	{
		return (MathUtil.limit(worldY, L2World.WORLD_Y_MIN, L2World.WORLD_Y_MAX) - L2World.WORLD_Y_MIN) >> 4;
	}
	
	public static int getHeight(Location loc)
	{
		return getHeight(loc._x, loc._y, loc._z);
	}
	
	public static int getHeight(int x, int y, int z)
	{
		return NgetHeight(x - L2World.WORLD_X_MIN >> 4, y - L2World.WORLD_Y_MIN >> 4, z);
	}
	
	public static boolean canMoveToCoord(L2Character player, L2Character target)
	{
		return canMove(player.getX(), player.getY(), player.getZ(), target.getX(), target.getY(), target.getZ(), false) == 0;
	}
	
	public static boolean canMoveToCoord(int x, int y, int z, int tx, int ty, int tz)
	{
		return canMove(x, y, z, tx, ty, tz, false) == 0;
	}
	
	public static byte getNSWE(int x, int y, int z)
	{
		return NgetNSWE(x - L2World.WORLD_X_MIN >> 4, y - L2World.WORLD_Y_MIN >> 4, z);
	}
	
	public static Location moveCheck(int x, int y, int z, int tx, int ty)
	{
		return MoveCheck(x, y, z, tx, ty, false, false, false);
	}
	
	public static Location moveCheck(int x, int y, int z, int tx, int ty, boolean returnPrev)
	{
		return MoveCheck(x, y, z, tx, ty, false, false, returnPrev);
	}
	
	public static Location moveCheckWithCollision(int x, int y, int z, int tx, int ty)
	{
		return MoveCheck(x, y, z, tx, ty, true, false, false);
	}
	
	public static Location moveCheckWithCollision(int x, int y, int z, int tx, int ty, boolean returnPrev)
	{
		return MoveCheck(x, y, z, tx, ty, true, false, returnPrev);
	}
	
	public static Location moveCheckBackward(int x, int y, int z, int tx, int ty)
	{
		return MoveCheck(x, y, z, tx, ty, false, true, false);
	}
	
	public static Location moveCheckBackward(int x, int y, int z, int tx, int ty, boolean returnPrev)
	{
		return MoveCheck(x, y, z, tx, ty, false, true, returnPrev);
	}
	
	public static Location moveCheckBackwardWithCollision(int x, int y, int z, int tx, int ty)
	{
		return MoveCheck(x, y, z, tx, ty, true, true, false);
	}
	
	public static Location moveCheckBackwardWithCollision(int x, int y, int z, int tx, int ty, boolean returnPrev)
	{
		return MoveCheck(x, y, z, tx, ty, true, true, returnPrev);
	}
	
	public static Location moveInWaterCheck(int x, int y, int z, int tx, int ty, int tz)
	{
		return MoveInWaterCheck(x - L2World.WORLD_X_MIN >> 4, y - L2World.WORLD_Y_MIN >> 4, z, tx - L2World.WORLD_X_MIN >> 4, ty - L2World.WORLD_Y_MIN >> 4, tz);
	}
	
	public static Location moveCheckForAI(Location loc1, Location loc2)
	{
		return MoveCheckForAI(loc1._x - L2World.WORLD_X_MIN >> 4, loc1._y - L2World.WORLD_Y_MIN >> 4, loc1._z, loc2._x - L2World.WORLD_X_MIN >> 4, loc2._y - L2World.WORLD_Y_MIN >> 4);
	}
	
	public static Location moveCheckInAir(int x, int y, int z, int tx, int ty, int tz, float ColRadius)
	{
		int gx = x - L2World.WORLD_X_MIN >> 4;
		int gy = y - L2World.WORLD_Y_MIN >> 4;
		int tgx = tx - L2World.WORLD_X_MIN >> 4;
		int tgy = ty - L2World.WORLD_Y_MIN >> 4;
		
		int nz = NgetHeight(tgx, tgy, tz);
		
		if (tz <= nz + 32)
			tz = nz + 32;
		
		Location result = canSee(gx, gy, z, tgx, tgy, tz, true);
		if (result.equals(gx, gy, z))
			return null;
		
		return result.geo2world();
	}
	
	public static boolean canSeeTarget(L2Object actor, L2Object target, boolean air)
	{
		if (target == null)
			return false;

		if (FenceData.getInstance().checkIfFenceBetween(actor.getX(), actor.getY(), actor.getZ(), target.getX(), target.getY(), target.getZ()))
			return false;
		
		if (target instanceof GeoControl || actor.equals(target))
			return true;
		
		return canSeeCoord(actor, target.getX(), target.getY(), target.getZ() + (int) 0.0F + 64, air);
	}
	
	public static boolean canSeeCoord(L2Object actor, int tx, int ty, int tz, boolean air)
	{
		return actor != null && canSeeCoord(actor.getX(), actor.getY(), actor.getZ() + (int) 0.0F + 64, tx, ty, tz, air);
	}
	
	public static boolean canSeeCoord(int x, int y, int z, int tx, int ty, int tz, boolean air)
	{
		int mx = x - L2World.WORLD_X_MIN >> 4;
		int my = y - L2World.WORLD_Y_MIN >> 4;
		int tmx = tx - L2World.WORLD_X_MIN >> 4;
		int tmy = ty - L2World.WORLD_Y_MIN >> 4;
		return canSee(mx, my, z, tmx, tmy, tz, air).equals(tmx, tmy, tz) && canSee(tmx, tmy, tz, mx, my, z, air).equals(mx, my, z);
	}
	
	public static boolean canMoveWithCollision(int x, int y, int z, int tx, int ty, int tz)
	{
		return canMove(x, y, z, tx, ty, tz, true) == 0;
	}
	
	public static boolean checkNSWE(byte NSWE, int x, int y, int tx, int ty)
	{
		if (NSWE == NSWE_ALL)
			return true;
		if (NSWE == NSWE_NONE)
			return false;
		if (tx > x)
		{
			if ((NSWE & EAST) == 0)
				return false;
		}
		else if (tx < x)
			if ((NSWE & WEST) == 0)
				return false;
		if (ty > y)
		{
			if ((NSWE & SOUTH) == 0)
				return false;
		}
		else if (ty < y)
			if ((NSWE & NORTH) == 0)
				return false;
		return true;
	}
	
	public static String geoXYZ2Str(int _x, int _y, int _z)
	{
		return "(" + String.valueOf((_x << 4) + L2World.WORLD_X_MIN + 8) + " " + String.valueOf((_y << 4) + L2World.WORLD_Y_MIN + 8) + " " + _z + ")";
	}
	
	public static String NSWE2Str(byte nswe)
	{
		String result = "";
		if ((nswe & NORTH) == NORTH)
			result += "N";
		if ((nswe & SOUTH) == SOUTH)
			result += "S";
		if ((nswe & WEST) == WEST)
			result += "W";
		if ((nswe & EAST) == EAST)
			result += "E";
		return result.isEmpty() ? "X" : result;
	}
	
	private static boolean NLOS_WATER(int x, int y, int z, int next_x, int next_y, int next_z)
	{
		Layer[] layers1 = NGetLayers(x, y);
		Layer[] layers2 = NGetLayers(next_x, next_y);
		
		if (layers1.length == 0 || layers2.length == 0)
			return true;
		
		short z2 = Short.MIN_VALUE;
		for (Layer layer : layers2)
			if (Math.abs(next_z - z2) > Math.abs(next_z - layer.height))
				z2 = layer.height;
		
		if (next_z + 32 >= z2)
			return true;
		
		short z3 = Short.MIN_VALUE;
		
		for (Layer layer : layers2)
			if (layer.height < z2 + Config.MIN_LAYER_HEIGHT && Math.abs(next_z - z3) > Math.abs(next_z - layer.height))
				z3 = layer.height;
		
		if (z3 == Short.MIN_VALUE)
			return false;
		
		short z1 = Short.MIN_VALUE;
		byte NSWE1 = NSWE_ALL;
		
		for (Layer layer : layers1)
			if (layer.height < z + Config.MIN_LAYER_HEIGHT && Math.abs(z - z1) > Math.abs(z - layer.height))
			{
				z1 = layer.height;
				NSWE1 = layer.nswe;
			}
		
		if (z1 < -30000)
			return true;
		
		return checkNSWE(NSWE1, x, y, next_x, next_y);
	}
	
	private static int FindNearestLowerLayer(short[] layers, int z)
	{
		short h, nearest_layer_h = Short.MIN_VALUE;
		int nearest_layer = Integer.MIN_VALUE;
		for (int i = 1; i <= layers[0]; i++)
		{
			h = (short) ((short) (layers[i] & 0x0fff0) >> 1);
			if (h <= z && nearest_layer_h <= h)
			{
				nearest_layer_h = h;
				nearest_layer = layers[i];
			}
		}
		return nearest_layer;
	}
	
	private static short CheckNoOneLayerInRangeAndFindNearestLowerLayer(short[] layers, int z0, int z1)
	{
		int z_min, z_max;
		if (z0 > z1)
		{
			z_min = z1;
			z_max = z0;
		}
		else
		{
			z_min = z0;
			z_max = z1;
		}
		short h, nearest_layer = Short.MIN_VALUE, nearest_layer_h = Short.MIN_VALUE;
		for (int i = 1; i <= layers[0]; i++)
		{
			h = (short) ((short) (layers[i] & 0x0fff0) >> 1);
			if (z_min <= h && h <= z_max)
				return Short.MIN_VALUE;
			if (h < z_max && nearest_layer_h < h)
			{
				nearest_layer_h = h;
				nearest_layer = layers[i];
			}
		}
		return nearest_layer;
	}
	
	public static boolean canSeeWallCheck(Layer layer, Layer nearest_lower_neighbor, byte directionNSWE)
	{
		return (layer.nswe & directionNSWE) != 0 || layer.height <= nearest_lower_neighbor.height || Math.abs(layer.height - nearest_lower_neighbor.height) < Config.MAX_Z_DIFF;
	}
	
	public static boolean canSeeWallCheck(short layer, short nearest_lower_neighbor, byte directionNSWE, int curr_z, boolean air)
	{
		short nearest_lower_neighborh = (short) ((short) (nearest_lower_neighbor & 0x0fff0) >> 1);
		if (air)
			return nearest_lower_neighborh < curr_z;
		short layerh = (short) ((short) (layer & 0x0fff0) >> 1);
		int zdiff = nearest_lower_neighborh - layerh;
		return (layer & 0x0F & directionNSWE) != 0 || zdiff > -Config.MAX_Z_DIFF && zdiff != 0;
	}
	
	public static Location canSee(int _x, int _y, int _z, int _tx, int _ty, int _tz, boolean air)
	{
		int diff_x = _tx - _x, diff_y = _ty - _y, diff_z = _tz - _z;
		int dx = Math.abs(diff_x), dy = Math.abs(diff_y);
		
		float steps = Math.max(dx, dy);
		int curr_x = _x, curr_y = _y, curr_z = _z;
		short[] curr_layers = new short[MAX_LAYERS + 1];
		NGetLayers(curr_x, curr_y, curr_layers);
		
		Location result = new Location(_x, _y, _z, -1);
		
		if (steps == 0)
		{
			if (CheckNoOneLayerInRangeAndFindNearestLowerLayer(curr_layers, curr_z, curr_z + diff_z) != Short.MIN_VALUE)
				result.set(_tx, _ty, _tz, 1);
			return result;
		}
		
		float step_x = diff_x / steps, step_y = diff_y / steps, step_z = diff_z / steps;
		int half_step_z = (int) (step_z / 2);
		float next_x = curr_x, next_y = curr_y, next_z = curr_z;
		int i_next_x, i_next_y, i_next_z, middle_z;
		short[] tmp_layers = new short[MAX_LAYERS + 1];
		short src_nearest_lower_layer, dst_nearest_lower_layer, tmp_nearest_lower_layer;
		
		for (int i = 0; i < steps; i++)
		{
			if (curr_layers[0] == 0)
			{
				result.set(_tx, _ty, _tz, 0);
				return result;
			}
			
			next_x += step_x;
			next_y += step_y;
			next_z += step_z;
			i_next_x = (int) (next_x + 0.5f);
			i_next_y = (int) (next_y + 0.5f);
			i_next_z = (int) (next_z + 0.5f);
			middle_z = curr_z + half_step_z;
			
			if ((src_nearest_lower_layer = CheckNoOneLayerInRangeAndFindNearestLowerLayer(curr_layers, curr_z, middle_z)) == Short.MIN_VALUE)
				return result.setH(-10);
			
			NGetLayers(curr_x, curr_y, curr_layers);
			if (curr_layers[0] == 0)
			{
				result.set(_tx, _ty, _tz, 0);
				return result;
			}
			
			if ((dst_nearest_lower_layer = CheckNoOneLayerInRangeAndFindNearestLowerLayer(curr_layers, i_next_z, middle_z)) == Short.MIN_VALUE)
				return result.setH(-11);
			
			if (curr_x == i_next_x)
			{
				if (!canSeeWallCheck(src_nearest_lower_layer, dst_nearest_lower_layer, i_next_y > curr_y ? SOUTH : NORTH, curr_z, air))
					return result.setH(-20);
			}
			else if (curr_y == i_next_y)
			{
				if (!canSeeWallCheck(src_nearest_lower_layer, dst_nearest_lower_layer, i_next_x > curr_x ? EAST : WEST, curr_z, air))
					return result.setH(-21);
			}
			else
			{
				NGetLayers(curr_x, i_next_y, tmp_layers);
				if (tmp_layers[0] == 0)
				{
					result.set(_tx, _ty, _tz, 0);
					return result;
				}
				if ((tmp_nearest_lower_layer = CheckNoOneLayerInRangeAndFindNearestLowerLayer(tmp_layers, i_next_z, middle_z)) == Short.MIN_VALUE)
					return result.setH(-30);
				
				if (!(canSeeWallCheck(src_nearest_lower_layer, tmp_nearest_lower_layer, i_next_y > curr_y ? SOUTH : NORTH, curr_z, air) && canSeeWallCheck(tmp_nearest_lower_layer, dst_nearest_lower_layer, i_next_x > curr_x ? EAST : WEST, curr_z, air)))
				{
					NGetLayers(i_next_x, curr_y, tmp_layers);
					if (tmp_layers[0] == 0)
					{
						result.set(_tx, _ty, _tz, 0);
						return result;
					}
					if ((tmp_nearest_lower_layer = CheckNoOneLayerInRangeAndFindNearestLowerLayer(tmp_layers, i_next_z, middle_z)) == Short.MIN_VALUE)
						return result.setH(-31);
					if (!canSeeWallCheck(src_nearest_lower_layer, tmp_nearest_lower_layer, i_next_x > curr_x ? EAST : WEST, curr_z, air))
						return result.setH(-32);
					if (!canSeeWallCheck(tmp_nearest_lower_layer, dst_nearest_lower_layer, i_next_x > curr_x ? EAST : WEST, curr_z, air))
						return result.setH(-33);
				}
			}
			
			result.set(curr_x, curr_y, curr_z);
			curr_x = i_next_x;
			curr_y = i_next_y;
			curr_z = i_next_z;
		}
		
		result.set(_tx, _ty, _tz, 0xFF);
		return result;
	}
	
	private static Location MoveInWaterCheck(int x, int y, int z, int tx, int ty, int tz)
	{
		int dx = tx - x;
		int dy = ty - y;
		int dz = tz - z;
		int inc_x = sign(dx);
		int inc_y = sign(dy);
		dx = Math.abs(dx);
		dy = Math.abs(dy);
		if (dx + dy == 0)
			return new Location(x, y, z).geo2world();
		float inc_z_for_x = dx == 0 ? 0 : dz / dx;
		float inc_z_for_y = dy == 0 ? 0 : dz / dy;
		int prev_x;
		int prev_y;
		int prev_z;
		int next_x = x;
		int next_y = y;
		int next_z = z;
		if (dx >= dy) // dy/dx <= 1
		{
			int delta_A = 2 * dy;
			int d = delta_A - dx;
			int delta_B = delta_A - 2 * dx;
			for (int i = 0; i < dx; i++)
			{
				prev_x = x;
				prev_y = y;
				prev_z = z;
				x = next_x;
				y = next_y;
				z = next_z;
				if (d > 0)
				{
					d += delta_B;
					next_x += inc_x;
					next_z += inc_z_for_x;
					next_y += inc_y;
					next_z += inc_z_for_y;
				}
				else
				{
					d += delta_A;
					next_x += inc_x;
					next_z += inc_z_for_x;
				}
				if (!NLOS_WATER(x, y, z, next_x, next_y, next_z))
					return new Location(prev_x, prev_y, prev_z).geo2world();
			}
		}
		else
		{
			int delta_A = 2 * dx;
			int d = delta_A - dy;
			int delta_B = delta_A - 2 * dy;
			for (int i = 0; i < dy; i++)
			{
				prev_x = x;
				prev_y = y;
				prev_z = z;
				x = next_x;
				y = next_y;
				z = next_z;
				if (d > 0)
				{
					d += delta_B;
					next_x += inc_x;
					next_z += inc_z_for_x;
					next_y += inc_y;
					next_z += inc_z_for_y;
				}
				else
				{
					d += delta_A;
					next_y += inc_y;
					next_z += inc_z_for_y;
				}
				if (!NLOS_WATER(x, y, z, next_x, next_y, next_z))
					return new Location(prev_x, prev_y, prev_z).geo2world();
			}
		}
		return new Location(next_x, next_y, next_z).geo2world();
	}
	
	public static int canMove(int __x, int __y, int _z, int __tx, int __ty, int _tz, boolean withCollision)
	{
		int _x = __x - L2World.WORLD_X_MIN >> 4;
		int _y = __y - L2World.WORLD_Y_MIN >> 4;
		int _tx = __tx - L2World.WORLD_X_MIN >> 4;
		int _ty = __ty - L2World.WORLD_Y_MIN >> 4;
		int diff_x = _tx - _x, diff_y = _ty - _y, diff_z = _tz - _z;
		int dx = Math.abs(diff_x), dy = Math.abs(diff_y), dz = Math.abs(diff_z);
		float steps = Math.max(dx, dy);
		if (steps == 0)
			return -5;
		
		int curr_x = _x, curr_y = _y, curr_z = _z;
		short[] curr_layers = new short[MAX_LAYERS + 1];
		NGetLayers(curr_x, curr_y, curr_layers);
		if (curr_layers[0] == 0)
			return 0;
		
		float step_x = diff_x / steps, step_y = diff_y / steps;
		float next_x = curr_x, next_y = curr_y;
		int i_next_x, i_next_y;
		
		short[] next_layers = new short[MAX_LAYERS + 1];
		short[] temp_layers = new short[MAX_LAYERS + 1];
		short[] curr_next_switcher;
		
		for (int i = 0; i < steps; i++)
		{
			next_x += step_x;
			next_y += step_y;
			i_next_x = (int) (next_x + 0.5f);
			i_next_y = (int) (next_y + 0.5f);
			NGetLayers(i_next_x, i_next_y, next_layers);
			if ((curr_z = NcanMoveNext(curr_x, curr_y, curr_z, curr_layers, i_next_x, i_next_y, next_layers, temp_layers, withCollision)) == Integer.MIN_VALUE)
				return 1;
			curr_next_switcher = curr_layers;
			curr_layers = next_layers;
			next_layers = curr_next_switcher;
			curr_x = i_next_x;
			curr_y = i_next_y;
		}
		diff_z = curr_z - _tz;
		dz = Math.abs(diff_z);
		if (Config.ALLOW_FALL_FROM_WALLS)
			return diff_z < Config.MAX_Z_DIFF ? 0 : diff_z * 10000;
		return dz > Config.MAX_Z_DIFF ? dz * 1000 : 0;
	}
	
	public static Location MoveCheck(int __x, int __y, int _z, int __tx, int __ty, boolean withCollision, boolean backwardMove, boolean returnPrev)
	{
		int _x = __x - L2World.WORLD_X_MIN >> 4;
		int _y = __y - L2World.WORLD_Y_MIN >> 4;
		int _tx = __tx - L2World.WORLD_X_MIN >> 4;
		int _ty = __ty - L2World.WORLD_Y_MIN >> 4;
		
		int diff_x = _tx - _x, diff_y = _ty - _y;
		int dx = Math.abs(diff_x), dy = Math.abs(diff_y);
		float steps = Math.max(dx, dy);
		if (steps == 0)
			return new Location(__x, __y, _z);
		
		float step_x = diff_x / steps, step_y = diff_y / steps;
		int curr_x = _x, curr_y = _y, curr_z = _z;
		float next_x = curr_x, next_y = curr_y;
		int i_next_x, i_next_y, i_next_z = curr_z;
		
		short[] next_layers = new short[MAX_LAYERS + 1];
		short[] temp_layers = new short[MAX_LAYERS + 1];
		short[] curr_layers = new short[MAX_LAYERS + 1];
		short[] curr_next_switcher;
		NGetLayers(curr_x, curr_y, curr_layers);
		int prev_x = curr_x, prev_y = curr_y, prev_z = curr_z;
		
		for (int i = 0; i < steps; i++)
		{
			next_x += step_x;
			next_y += step_y;
			i_next_x = (int) (next_x + 0.5f);
			i_next_y = (int) (next_y + 0.5f);
			NGetLayers(i_next_x, i_next_y, next_layers);
			if ((i_next_z = NcanMoveNext(curr_x, curr_y, curr_z, curr_layers, i_next_x, i_next_y, next_layers, temp_layers, withCollision)) == Integer.MIN_VALUE)
				break;
			if (backwardMove && NcanMoveNext(i_next_x, i_next_y, i_next_z, next_layers, curr_x, curr_y, curr_layers, temp_layers, withCollision) == Integer.MIN_VALUE)
				break;
			curr_next_switcher = curr_layers;
			curr_layers = next_layers;
			next_layers = curr_next_switcher;
			if (returnPrev)
			{
				prev_x = curr_x;
				prev_y = curr_y;
				prev_z = curr_z;
			}
			curr_x = i_next_x;
			curr_y = i_next_y;
			curr_z = i_next_z;
		}
		
		if (returnPrev)
		{
			curr_x = prev_x;
			curr_y = prev_y;
			curr_z = prev_z;
		}
		
		// if(curr_x == _x && curr_y == _y)
		// return new Location(__x, __y, _z);
		
		// log.info("move" + (backwardMove ? " back" : "") + (withCollision ? " +collision" : "") + ": " + curr_x + " " + curr_y + " " + curr_z + " / xyz: " + __x + " " + __y + " " + _z + " / to xy: " + __tx + " " + __ty + " / geo xy: " + _x + " " + _y + " / geo to xy: " + _tx + " " + _ty);
		return new Location(curr_x, curr_y, curr_z).geo2world();
	}
	
	public static ArrayList<Location> MoveList(int __x, int __y, int _z, int __tx, int __ty, boolean onlyFullPath)
	{
		int _x = __x - L2World.WORLD_X_MIN >> 4;
		int _y = __y - L2World.WORLD_Y_MIN >> 4;
		int _tx = __tx - L2World.WORLD_X_MIN >> 4;
		int _ty = __ty - L2World.WORLD_Y_MIN >> 4;
		
		int diff_x = _tx - _x, diff_y = _ty - _y;
		int dx = Math.abs(diff_x), dy = Math.abs(diff_y);
		float steps = Math.max(dx, dy);
		if (steps == 0)
			return new ArrayList<>(0);
		
		float step_x = diff_x / steps, step_y = diff_y / steps;
		int curr_x = _x, curr_y = _y, curr_z = _z;
		float next_x = curr_x, next_y = curr_y;
		int i_next_x, i_next_y, i_next_z = curr_z;
		
		short[] next_layers = new short[MAX_LAYERS + 1];
		short[] temp_layers = new short[MAX_LAYERS + 1];
		short[] curr_layers = new short[MAX_LAYERS + 1];
		short[] curr_next_switcher;
		
		NGetLayers(curr_x, curr_y, curr_layers);
		if (curr_layers[0] == 0)
			return null;
		
		ArrayList<Location> result = new ArrayList<>();
		
		result.add(new Location(curr_x, curr_y, curr_z));
		
		for (int i = 0; i < steps; i++)
		{
			next_x += step_x;
			next_y += step_y;
			i_next_x = (int) (next_x + 0.5f);
			i_next_y = (int) (next_y + 0.5f);
			
			NGetLayers(i_next_x, i_next_y, next_layers);
			if ((i_next_z = NcanMoveNext(curr_x, curr_y, curr_z, curr_layers, i_next_x, i_next_y, next_layers, temp_layers, false)) == Integer.MIN_VALUE)
			{
				if (onlyFullPath)
					return null;
				break;
			}
			
			curr_next_switcher = curr_layers;
			curr_layers = next_layers;
			next_layers = curr_next_switcher;
			
			curr_x = i_next_x;
			curr_y = i_next_y;
			curr_z = i_next_z;
			
			result.add(new Location(curr_x, curr_y, curr_z));
		}
		
		return result;
	}
	
	private static Location MoveCheckForAI(int x, int y, int z, int tx, int ty)
	{
		int dx = tx - x;
		int dy = ty - y;
		int inc_x = sign(dx);
		int inc_y = sign(dy);
		dx = Math.abs(dx);
		dy = Math.abs(dy);
		if (dx + dy < 2 || dx == 2 && dy == 0 || dx == 0 && dy == 2)
			return new Location(x, y, z).geo2world();
		int prev_x = x;
		int prev_y = y;
		int prev_z = z;
		int next_x = x;
		int next_y = y;
		int next_z = z;
		if (dx >= dy) // dy/dx <= 1
		{
			int delta_A = 2 * dy;
			int d = delta_A - dx;
			int delta_B = delta_A - 2 * dx;
			for (int i = 0; i < dx; i++)
			{
				prev_x = x;
				prev_y = y;
				prev_z = z;
				x = next_x;
				y = next_y;
				z = next_z;
				if (d > 0)
				{
					d += delta_B;
					next_x += inc_x;
					next_y += inc_y;
				}
				else
				{
					d += delta_A;
					next_x += inc_x;
				}
				next_z = NcanMoveNextForAI(x, y, z, next_x, next_y);
				if (next_z == 0)
					return new Location(prev_x, prev_y, prev_z).geo2world();
			}
		}
		else
		{
			int delta_A = 2 * dx;
			int d = delta_A - dy;
			int delta_B = delta_A - 2 * dy;
			for (int i = 0; i < dy; i++)
			{
				prev_x = x;
				prev_y = y;
				prev_z = z;
				x = next_x;
				y = next_y;
				z = next_z;
				if (d > 0)
				{
					d += delta_B;
					next_x += inc_x;
					next_y += inc_y;
				}
				else
				{
					d += delta_A;
					next_y += inc_y;
				}
				next_z = NcanMoveNextForAI(x, y, z, next_x, next_y);
				if (next_z == 0)
					return new Location(prev_x, prev_y, prev_z).geo2world();
			}
		}
		return new Location(next_x, next_y, next_z).geo2world();
	}
	
	private static boolean NcanMoveNextExCheck(int x, int y, int h, int nextx, int nexty, int hexth, short[] temp_layers)
	{
		NGetLayers(x, y, temp_layers);
		if (temp_layers[0] == 0)
			return true;
		
		int temp_layer;
		if ((temp_layer = FindNearestLowerLayer(temp_layers, h + Config.MIN_LAYER_HEIGHT)) == Integer.MIN_VALUE)
			return false;
		short temp_layer_h = (short) ((short) (temp_layer & 0x0fff0) >> 1);
		if (Math.abs(temp_layer_h - hexth) >= Config.MAX_Z_DIFF || Math.abs(temp_layer_h - h) >= Config.MAX_Z_DIFF)
			return false;
		return checkNSWE((byte) (temp_layer & 0x0F), x, y, nextx, nexty);
	}
	
	public static int NcanMoveNext(int x, int y, int z, short[] layers, int next_x, int next_y, short[] next_layers, short[] temp_layers, boolean withCollision)
	{
		if (layers[0] == 0 || next_layers[0] == 0)
			return z;
		
		int layer, next_layer;
		if ((layer = FindNearestLowerLayer(layers, z + Config.MIN_LAYER_HEIGHT)) == Integer.MIN_VALUE)
			return Integer.MIN_VALUE;
		
		byte layer_nswe = (byte) (layer & 0x0F);
		if (!checkNSWE(layer_nswe, x, y, next_x, next_y))
			return Integer.MIN_VALUE;
		
		short layer_h = (short) ((short) (layer & 0x0fff0) >> 1);
		if ((next_layer = FindNearestLowerLayer(next_layers, layer_h + Config.MIN_LAYER_HEIGHT)) == Integer.MIN_VALUE)
			return Integer.MIN_VALUE;
		
		short next_layer_h = (short) ((short) (next_layer & 0x0fff0) >> 1);
		
		if (x == next_x || y == next_y)
		{
			if (withCollision)
			{
				// short[] heightNSWE = temp_layers;
				if (x == next_x)
				{
					NgetHeightAndNSWE(x - 1, y, layer_h, temp_layers);
					if (Math.abs(temp_layers[0] - layer_h) > 15 || !checkNSWE(layer_nswe, x - 1, y, x, y) || !checkNSWE((byte) temp_layers[1], x - 1, y, x - 1, next_y))
						return Integer.MIN_VALUE;
					
					NgetHeightAndNSWE(x + 1, y, layer_h, temp_layers);
					if (Math.abs(temp_layers[0] - layer_h) > 15 || !checkNSWE(layer_nswe, x + 1, y, x, y) || !checkNSWE((byte) temp_layers[1], x + 1, y, x + 1, next_y))
						return Integer.MIN_VALUE;
					
					return next_layer_h;
				}
				
				NgetHeightAndNSWE(x, y - 1, layer_h, temp_layers);
				if (Math.abs(temp_layers[0] - layer_h) >= Config.MAX_Z_DIFF || !checkNSWE(layer_nswe, x, y - 1, x, y) || !checkNSWE((byte) temp_layers[1], x, y - 1, next_x, y - 1))
					return Integer.MIN_VALUE;
				
				NgetHeightAndNSWE(x, y + 1, layer_h, temp_layers);
				if (Math.abs(temp_layers[0] - layer_h) >= Config.MAX_Z_DIFF || !checkNSWE(layer_nswe, x, y + 1, x, y) || !checkNSWE((byte) temp_layers[1], x, y + 1, next_x, y + 1))
					return Integer.MIN_VALUE;
			}
			
			return next_layer_h;
		}
		
		if (!NcanMoveNextExCheck(x, next_y, layer_h, next_x, next_y, next_layer_h, temp_layers))
			return Integer.MIN_VALUE;
		if (!NcanMoveNextExCheck(next_x, y, layer_h, next_x, next_y, next_layer_h, temp_layers))
			return Integer.MIN_VALUE;
		
		// FIXME if(withCollision)
		
		return next_layer_h;
	}
	
	public static int NcanMoveNextForAI(int x, int y, int z, int next_x, int next_y)
	{
		Layer[] layers1 = NGetLayers(x, y);
		Layer[] layers2 = NGetLayers(next_x, next_y);
		
		if (layers1.length == 0 || layers2.length == 0)
			return z == 0 ? 1 : z;
		
		short z1 = Short.MIN_VALUE;
		short z2 = Short.MIN_VALUE;
		byte NSWE1 = NSWE_ALL;
		byte NSWE2 = NSWE_ALL;
		
		for (Layer layer : layers1)
			if (layer.height < z + Config.MIN_LAYER_HEIGHT && Math.abs(z - z1) > Math.abs(z - layer.height))
			{
				z1 = layer.height;
				NSWE1 = layer.nswe;
			}
		
		if (z1 < -30000)
			for (Layer layer : layers1)
				if (Math.abs(z - z1) > Math.abs(z - layer.height))
				{
					z1 = layer.height;
					NSWE1 = layer.nswe;
				}
		
		if (z1 < -30000)
			return 0;
		
		for (Layer layer : layers2)
			if (layer.height < z1 + Config.MIN_LAYER_HEIGHT && Math.abs(z1 - z2) > Math.abs(z1 - layer.height))
			{
				z2 = layer.height;
				NSWE2 = layer.nswe;
			}
		
		if (z2 < -30000)
			for (Layer layer : layers2)
				if (Math.abs(z1 - z2) > Math.abs(z1 - layer.height))
				{
					z2 = layer.height;
					NSWE2 = layer.nswe;
				}
		
		if (z2 < -30000)
			return 0;
		
		if (z1 > z2 && z1 - z2 > Config.MAX_Z_DIFF)
			return 0;
		
		if (!checkNSWE(NSWE1, x, y, next_x, next_y) || !checkNSWE(NSWE2, next_x, next_y, x, y))
			return 0;
		
		return z2 == 0 ? 1 : z2;
	}
	
	public static void NGetLayers(int geoX, int geoY, short[] result)
	{
		result[0] = 0;
		byte[] block = getGeoBlockFromGeoCoords(geoX, geoY);
		if (block == null)
			return;
		
		int cellX, cellY;
		int index = 0;
		// Read current block type: 0 - flat, 1 - complex, 2 - multilevel
		byte type = block[index];
		index++;
		
		switch (type)
		{
			case BLOCKTYPE_FLAT:
				short height = makeShort(block[index + 1], block[index]);
				height = (short) (height & 0x0fff0);
				result[0]++;
				result[1] = (short) ((short) (height << 1) | NSWE_ALL);
				return;
			case BLOCKTYPE_COMPLEX:
				cellX = getCell(geoX);
				cellY = getCell(geoY);
				index += (cellX << 3) + cellY << 1;
				height = makeShort(block[index + 1], block[index]);
				result[0]++;
				result[1] = height;
				return;
			case BLOCKTYPE_MULTILEVEL:
				cellX = getCell(geoX);
				cellY = getCell(geoY);
				int offset = (cellX << 3) + cellY;
				while (offset > 0)
				{
					byte lc = block[index];
					index += (lc << 1) + 1;
					offset--;
				}
				byte layer_count = block[index];
				index++;
				if (layer_count <= 0 || layer_count > MAX_LAYERS)
					return;
				result[0] = layer_count;
				while (layer_count > 0)
				{
					result[layer_count] = makeShort(block[index + 1], block[index]);
					layer_count--;
					index += 2;
				}
				return;
			default:
				log.severe("GeoEngine: Unknown block type");
				return;
		}
	}
	
	public static Layer[] NGetLayers(int geoX, int geoY)
	{
		byte[] block = getGeoBlockFromGeoCoords(geoX, geoY);
		
		if (block == null)
			return new Layer[0];
		
		int cellX, cellY;
		int index = 0;
		// Read current block type: 0 - flat, 1 - complex, 2 - multilevel
		byte type = block[index];
		index++;
		
		switch (type)
		{
			case BLOCKTYPE_FLAT:
				short height = makeShort(block[index + 1], block[index]);
				height = (short) (height & 0x0fff0);
				return new Layer[]
				{
					new Layer(height, NSWE_ALL)
				};
			case BLOCKTYPE_COMPLEX:
				cellX = getCell(geoX);
				cellY = getCell(geoY);
				index += (cellX << 3) + cellY << 1;
				height = makeShort(block[index + 1], block[index]);
				return new Layer[]
				{
					new Layer((short) ((short) (height & 0x0fff0) >> 1), (byte) (height & 0x0F))
				};
			case BLOCKTYPE_MULTILEVEL:
				cellX = getCell(geoX);
				cellY = getCell(geoY);
				int offset = (cellX << 3) + cellY;
				while (offset > 0)
				{
					byte lc = block[index];
					index += (lc << 1) + 1;
					offset--;
				}
				byte layer_count = block[index];
				index++;
				if (layer_count <= 0 || layer_count > MAX_LAYERS)
					return new Layer[0];
				Layer[] layers = new Layer[layer_count];
				while (layer_count > 0)
				{
					height = makeShort(block[index + 1], block[index]);
					layer_count--;
					layers[layer_count] = new Layer((short) ((short) (height & 0x0fff0) >> 1), (byte) (height & 0x0F));
					index += 2;
				}
				return layers;
			default:
				log.severe("GeoEngine: Unknown block type");
				return new Layer[0];
		}
	}
	
	private static short NgetType(int geoX, int geoY)
	{
		byte[] block = getGeoBlockFromGeoCoords(geoX, geoY);
		
		if (block == null)
			return 0;
		
		return block[0];
	}
	
	public static int NgetHeight(int geoX, int geoY, int z)
	{
		byte[] block = getGeoBlockFromGeoCoords(geoX, geoY);
		
		if (block == null)
			return z;
		
		int cellX, cellY, index = 0;
		
		// Read current block type: 0 - flat, 1 - complex, 2 - multilevel
		byte type = block[index];
		index++;
		
		short height;
		switch (type)
		{
			case BLOCKTYPE_FLAT:
				height = makeShort(block[index + 1], block[index]);
				return (short) (height & 0x0fff0);
			case BLOCKTYPE_COMPLEX:
				cellX = getCell(geoX);
				cellY = getCell(geoY);
				index += (cellX << 3) + cellY << 1;
				height = makeShort(block[index + 1], block[index]);
				return (short) ((short) (height & 0x0fff0) >> 1); // height / 2
			case BLOCKTYPE_MULTILEVEL:
				cellX = getCell(geoX);
				cellY = getCell(geoY);
				int offset = (cellX << 3) + cellY;
				while (offset > 0)
				{
					byte lc = block[index];
					index += (lc << 1) + 1;
					offset--;
				}
				byte layers = block[index];
				index++;
				if (layers <= 0 || layers > MAX_LAYERS)
					return (short) z;
				
				int z_nearest_lower_limit = z + Config.MIN_LAYER_HEIGHT;
				int z_nearest_lower = Integer.MIN_VALUE;
				int z_nearest = Integer.MIN_VALUE;
				
				while (layers > 0)
				{
					height = (short) ((short) (makeShort(block[index + 1], block[index]) & 0x0fff0) >> 1);
					if (height < z_nearest_lower_limit)
						z_nearest_lower = Math.max(z_nearest_lower, height);
					else if (Math.abs(z - height) < Math.abs(z - z_nearest))
						z_nearest = height;
					layers--;
					index += 2;
				}
				
				return z_nearest_lower != Integer.MIN_VALUE ? z_nearest_lower : z_nearest;
			default:
				log.warning("GeoEngine: Unknown blockType");
				return z;
		}
	}
	
	public static byte NgetNSWE(int geoX, int geoY, int z)
	{
		byte[] block = getGeoBlockFromGeoCoords(geoX, geoY);
		
		if (block == null)
			return NSWE_ALL;
		
		int cellX, cellY;
		int index = 0;
		
		// Read current block type: 0 - flat, 1 - complex, 2 - multilevel
		byte type = block[index];
		index++;
		
		switch (type)
		{
			case BLOCKTYPE_FLAT:
				return NSWE_ALL;
			case BLOCKTYPE_COMPLEX:
				cellX = getCell(geoX);
				cellY = getCell(geoY);
				index += (cellX << 3) + cellY << 1;
				short height = makeShort(block[index + 1], block[index]);
				return (byte) (height & 0x0F);
			case BLOCKTYPE_MULTILEVEL:
				cellX = getCell(geoX);
				cellY = getCell(geoY);
				int offset = (cellX << 3) + cellY;
				while (offset > 0)
				{
					byte lc = block[index];
					index += (lc << 1) + 1;
					offset--;
				}
				byte layers = block[index];
				index++;
				if (layers <= 0 || layers > MAX_LAYERS)
					return NSWE_ALL;
				
				short tempz1 = Short.MIN_VALUE;
				short tempz2 = Short.MIN_VALUE;
				int index_nswe1 = NSWE_NONE;
				int index_nswe2 = NSWE_NONE;
				int z_nearest_lower_limit = z + Config.MIN_LAYER_HEIGHT;
				
				while (layers > 0)
				{
					height = (short) ((short) (makeShort(block[index + 1], block[index]) & 0x0fff0) >> 1); // height / 2
					
					if (height < z_nearest_lower_limit)
					{
						if (height > tempz1)
						{
							tempz1 = height;
							index_nswe1 = index;
						}
					}
					else if (Math.abs(z - height) < Math.abs(z - tempz2))
					{
						tempz2 = height;
						index_nswe2 = index;
					}
					
					layers--;
					index += 2;
				}
				
				if (index_nswe1 > 0)
					return (byte) (makeShort(block[index_nswe1 + 1], block[index_nswe1]) & 0x0F);
				if (index_nswe2 > 0)
					return (byte) (makeShort(block[index_nswe2 + 1], block[index_nswe2]) & 0x0F);
				
				return NSWE_ALL;
			default:
				log.severe("GeoEngine: Unknown block type.");
				return NSWE_ALL;
		}
	}
	
	public static void NgetHeightAndNSWE(int geoX, int geoY, short z, short[] result)
	{
		byte[] block = getGeoBlockFromGeoCoords(geoX, geoY);
		
		if (block == null)
		{
			result[0] = z;
			result[1] = NSWE_ALL;
			return;
		}
		
		int cellX, cellY, index = 0;
		short height, NSWE = NSWE_ALL;
		
		// Read current block type: 0 - flat, 1 - complex, 2 - multilevel
		byte type = block[index];
		index++;
		
		switch (type)
		{
			case BLOCKTYPE_FLAT:
				height = makeShort(block[index + 1], block[index]);
				result[0] = (short) (height & 0x0fff0);
				result[1] = NSWE_ALL;
				return;
			case BLOCKTYPE_COMPLEX:
				cellX = getCell(geoX);
				cellY = getCell(geoY);
				index += (cellX << 3) + cellY << 1;
				height = makeShort(block[index + 1], block[index]);
				result[0] = (short) ((short) (height & 0x0fff0) >> 1); // height / 2
				result[1] = (short) (height & 0x0F);
				return;
			case BLOCKTYPE_MULTILEVEL:
				cellX = getCell(geoX);
				cellY = getCell(geoY);
				int offset = (cellX << 3) + cellY;
				while (offset > 0)
				{
					byte lc = block[index];
					index += (lc << 1) + 1;
					offset--;
				}
				byte layers = block[index];
				index++;
				if (layers <= 0 || layers > MAX_LAYERS)
				{
					result[0] = z;
					result[1] = NSWE_ALL;
					return;
				}
				
				short tempz1 = Short.MIN_VALUE;
				short tempz2 = Short.MIN_VALUE;
				int index_nswe1 = 0;
				int index_nswe2 = 0;
				int z_nearest_lower_limit = z + Config.MIN_LAYER_HEIGHT;
				
				while (layers > 0)
				{
					height = (short) ((short) (makeShort(block[index + 1], block[index]) & 0x0fff0) >> 1); // height / 2
					
					if (height < z_nearest_lower_limit)
					{
						if (height > tempz1)
						{
							tempz1 = height;
							index_nswe1 = index;
						}
					}
					else if (Math.abs(z - height) < Math.abs(z - tempz2))
					{
						tempz2 = height;
						index_nswe2 = index;
					}
					
					layers--;
					index += 2;
				}
				
				if (index_nswe1 > 0)
				{
					NSWE = makeShort(block[index_nswe1 + 1], block[index_nswe1]);
					NSWE = (short) (NSWE & 0x0F);
				}
				else if (index_nswe2 > 0)
				{
					NSWE = makeShort(block[index_nswe2 + 1], block[index_nswe2]);
					NSWE = (short) (NSWE & 0x0F);
				}
				result[0] = tempz1 > Short.MIN_VALUE ? tempz1 : tempz2;
				result[1] = NSWE;
				return;
			default:
				log.severe("GeoEngine: Unknown block type.");
				result[0] = z;
				result[1] = NSWE_ALL;
				return;
		}
	}
	
	protected static short makeShort(byte b1, byte b0)
	{
		return (short) (b1 << 8 | b0 & 0xff);
	}
	
	protected static int getBlock(int geoPos)
	{
		return (geoPos >> 3) % 256;
	}
	
	protected static int getCell(int geoPos)
	{
		return geoPos % 8;
	}
	
	protected static int getBlockIndex(int blockX, int blockY)
	{
		return (blockX << 8) + blockY;
	}
	
	private static byte sign(int x)
	{
		if (x >= 0)
			return +1;
		return -1;
	}
	
	private static byte[] getGeoBlockFromGeoCoords(int geoX, int geoY)
	{
		int ix = geoX >> 11;
		int iy = geoY >> 11;
		
		if (ix < 0 || ix >= L2World.WORLD_SIZE_X || iy < 0 || iy >= L2World.WORLD_SIZE_Y)
			return null;
		
		byte[][] region = geodata[ix][iy];
		
		if (region == null)
			return null;
		
		int blockX = getBlock(geoX);
		int blockY = getBlock(geoY);
		
		return region[getBlockIndex(blockX, blockY)];
	}
	
	public static void loadGeo()
	{
		log.info("GeoEngine: - Loading Geodata...");
		
		File f = new File("./data/geodata");
		
		if (!f.exists() || !f.isDirectory())
		{
			log.info("Geo Engine: Files missing, loading aborted.");
			return;
		}
		
		for (File q : f.listFiles())
		{
			if (q.isHidden() || q.isDirectory())
				continue;
			
			GeoLoader geoLoader = GeoLoaderFactory.getInstance().getGeoLoader(q);
			
			if (geoLoader != null)
			{
				GeoFileInfo geoFileInfo = geoLoader.readFile(q);
				if (geoFileInfo != null)
				{
					
					int x = geoFileInfo.getX() - Config.GEO_X_FIRST;
					int y = geoFileInfo.getY() - Config.GEO_Y_FIRST;
					
					if (geodata[x][y] != null && geodata[x][y].length > 0)
					{
						log.warning("Geodata in region " + geoFileInfo.getX() + "_" + geoFileInfo.getY() + " was replased by " + geoLoader.getClass().getSimpleName());
					}
					geodata[x][y] = geoFileInfo.getData();
				}
			}
		}
		log.info(GeoEngine.class.getSimpleName() + " Geodata Loaded!");
		if (Config.COMPACT_GEO)
			compact(true);
	}
	
	public static void DumpGeodata(String dir)
	{
		new File(dir).mkdirs();
		for (int mapX = 0; mapX < L2World.WORLD_SIZE_X; mapX++)
			for (int mapY = 0; mapY < L2World.WORLD_SIZE_Y; mapY++)
			{
				if (geodata[mapX][mapY] == null)
					continue;
				int rx = mapX + Config.GEO_X_FIRST;
				int ry = mapY + Config.GEO_Y_FIRST;
				String fName = dir + "/" + rx + "_" + ry + ".l2j";
				log.info("Dumping geo: " + fName);
				DumpGeodataFile(fName, (byte) rx, (byte) ry);
			}
	}
	
	public static boolean DumpGeodataFile(int cx, int cy)
	{
		return DumpGeodataFileMap((byte) (Math.floor((float) cx / (float) 32768) + 20), (byte) (Math.floor((float) cy / (float) 32768) + 18));
	}
	
	public static boolean DumpGeodataFileMap(byte rx, byte ry)
	{
		String name = "./log/" + rx + "_" + ry + ".l2j";
		return DumpGeodataFile(name, rx, ry);
	}
	
	public static boolean DumpGeodataFile(String _name, byte rx, byte ry)
	{
		int ix = rx - Config.GEO_X_FIRST;
		int iy = ry - Config.GEO_Y_FIRST;
		
		byte[][] geoblocks = geodata[ix][iy];
		if (geoblocks == null)
			return false;
		
		try
		{
			File f = new File(_name);
			if (f.exists())
				f.delete();
			@SuppressWarnings("resource")
			FileChannel wChannel = new RandomAccessFile(f, "rw").getChannel();
			
			for (byte[] geoblock : geoblocks)
			{
				ByteBuffer buffer = ByteBuffer.wrap(geoblock);
				wChannel.write(buffer);
			}
			wChannel.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public static boolean LoadGeodataFile(byte rx, byte ry)
	{
		String fname = "./geodata/" + rx + "_" + ry + ".l2j";
		int ix = rx - Config.GEO_X_FIRST;
		int iy = ry - Config.GEO_Y_FIRST;
		
		if (ix < 0 || iy < 0 || ix > (L2World.WORLD_X_MAX >> 15) + Math.abs(L2World.WORLD_X_MIN >> 15) || iy > (L2World.WORLD_Y_MAX >> 15) + Math.abs(L2World.WORLD_Y_MIN >> 15))
		{
			log.info("Geo Engine: File " + fname + " was not loaded!!! ");
			return false;
		}
		
		File Geo = new File(fname);
		int size, index = 0, block = 0, flor = 0;
		try
		{
			byte[] geo;
			synchronized (geodata)
			{
				// Create a read-only memory-mapped file
				FileChannel roChannel = new RandomAccessFile(Geo, "r").getChannel();
				size = (int) roChannel.size();
				ByteBuffer buffer = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, size);
				roChannel.close();
				buffer.order(ByteOrder.LITTLE_ENDIAN);
				geo = new byte[buffer.remaining()];
				buffer.get(geo, 0, geo.length);
			}
			if (size >= BlocksInMap * 3)
			{
				byte[][] blocks = new byte[BlocksInMap][]; // 256 * 256
				
				// Indexing geo files, so we will know where each block starts
				for (block = 0; block < blocks.length; block++)
				{
					byte type = geo[index];
					index++;
					
					byte[] geoBlock;
					switch (type)
					{
						case BLOCKTYPE_FLAT:
							
							geoBlock = new byte[2 + 1];
							
							geoBlock[0] = type;
							geoBlock[1] = geo[index];
							geoBlock[2] = geo[index + 1];
							
							index += 2;
							
							blocks[block] = geoBlock;
							break;
						
						case BLOCKTYPE_COMPLEX:
							
							geoBlock = new byte[128 + 1];
							
							geoBlock[0] = type;
							System.arraycopy(geo, index, geoBlock, 1, 128);
							
							index += 128;
							
							blocks[block] = geoBlock;
							break;
						
						case BLOCKTYPE_MULTILEVEL:
							
							int orgIndex = index;
							
							for (int b = 0; b < 64; b++)
							{
								byte layers = geo[index];
								MAX_LAYERS = Math.max(MAX_LAYERS, layers);
								index += (layers << 1) + 1;
								if (layers > flor)
									flor = layers;
							}
							
							int diff = index - orgIndex;
							
							geoBlock = new byte[diff + 1];
							
							geoBlock[0] = type;
							System.arraycopy(geo, orgIndex, geoBlock, 1, diff);
							
							blocks[block] = geoBlock;
							break;
						default:
							log.severe("GeoEngine: invalid block type: " + type);
					}
				}
				
				synchronized (geodata)
				{
					geodata[ix][iy] = blocks;
				}
				return true;
			}
			return false;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			log.warning("Failed to Load GeoFile at block: " + block + "\n");
			return false;
		}
	}
	
	public static void returnGeoAtControl(GeoControl collision)
	{
		Polygon shape = collision.getGeoPos();

		byte[][] around = collision.getGeoAround();
		if (around == null)
			throw new RuntimeException("Warning: trying to remove unitialized collision: " + collision);

		int minX = shape.getXmin() - L2World.WORLD_X_MIN - 16 >> 4;
		int minY = shape.getYmin() - L2World.WORLD_Y_MIN - 16 >> 4;
		int minZ = shape.getZmin();
		int maxZ = shape.getZmax();

		short height;
		byte old_nswe;

		for (int gX = 0; gX < around.length; gX++)
			for (int gY = 0; gY < around[gX].length; gY++)
			{
				int geoX = minX + gX;
				int geoY = minY + gY;

				byte[] block = getGeoBlockFromGeoCoords(geoX, geoY);
				if (block == null)
					continue;

				int cellX = getCell(geoX);
				int cellY = getCell(geoY);

				int index = 0;
				byte blockType = block[index];
				index++;

				switch (blockType)
				{
					case BLOCKTYPE_COMPLEX:
						index += (cellX << 3) + cellY << 1;

						height = makeShort(block[index + 1], block[index]);
						old_nswe = (byte) (height & 0x0F);
						height &= 0xfff0;
						height >>= 1;

						if (height < minZ || height > maxZ)
							break;

						height <<= 1;
						height &= 0xfff0;
						height |= old_nswe;
						if (collision.isGeoCloser())
							height |= around[gX][gY];
						else
							height &= ~around[gX][gY];

						block[index + 1] = (byte) (height >> 8);
						block[index] = (byte) (height & 0x00ff);
						break;
					case BLOCKTYPE_MULTILEVEL:
						int neededIndex = -1;

						int offset = (cellX << 3) + cellY;
						while (offset > 0)
						{
							byte lc = block[index];
							index += (lc << 1) + 1;
							offset--;
						}
						byte layers = block[index];
						index++;
						if (layers <= 0 || layers > MAX_LAYERS)
							break;
						short temph = Short.MIN_VALUE;
						old_nswe = NSWE_ALL;
						while (layers > 0)
						{
							height = makeShort(block[index + 1], block[index]);
							byte tmp_nswe = (byte) (height & 0x0F);
							height &= 0xfff0;
							height >>= 1;
							int z_diff_last = Math.abs(minZ - temph);
							int z_diff_curr = Math.abs(maxZ - height);
							if (z_diff_last > z_diff_curr)
							{
								old_nswe = tmp_nswe;
								temph = height;
								neededIndex = index;
							}
							layers--;
							index += 2;
						}

						if (temph == Short.MIN_VALUE || (temph < minZ || temph > maxZ))
							break;

						temph <<= 1;
						temph &= 0xfff0;
						temph |= old_nswe;
						
						if (collision.isGeoCloser())
							temph |= around[gX][gY];
						else
							temph &= ~around[gX][gY];

						block[neededIndex + 1] = (byte) (temph >> 8);
						block[neededIndex] = (byte) (temph & 0x00ff);
						break;
				}
			}
	}

	public static void applyControl(GeoControl collision)
	{
		Polygon shape = collision.getGeoPos();
		if (shape.getXmax() == shape.getYmax() && shape.getXmax() == 0)
			throw new RuntimeException("Attempt to add incorrect collision: " + collision);

		boolean isFirstTime = false;

		int minX = shape.getXmin() - L2World.WORLD_X_MIN - 16 >> 4;
		int maxX = shape.getXmax() - L2World.WORLD_X_MIN + 16 >> 4;
		int minY = shape.getYmin() - L2World.WORLD_Y_MIN - 16 >> 4;
		int maxY = shape.getYmax() - L2World.WORLD_Y_MIN + 16 >> 4;
		int minZ = shape.getZmin();
		int maxZ = shape.getZmax();

		byte[][] around = collision.getGeoAround();
		if (around == null)
		{
			isFirstTime = true;

			byte[][] cells = new byte[maxX - minX + 1][maxY - minY + 1];
			for (int gX = minX; gX <= maxX; gX++)
				for (int gY = minY; gY <= maxY; gY++)
				{
					int x = (gX << 4) + L2World.WORLD_X_MIN;
					int y = (gY << 4) + L2World.WORLD_Y_MIN;

					loop: for (int ax = x; ax < x + 16; ax++)
						for (int ay = y; ay < y + 16; ay++)
							if (shape.isInside(ax, ay))
							{
								cells[gX - minX][gY - minY] = 1;
								break loop;
							}
				}

			around = new byte[maxX - minX + 1][maxY - minY + 1];
			for (int gX = 0; gX < cells.length; gX++)
				for (int gY = 0; gY < cells[gX].length; gY++)
				{
					if (cells[gX][gY] == 1)
					{
						around[gX][gY] = NSWE_ALL;

						byte _nswe;
						
						if (gY > 0 && cells[gX][gY - 1] == 0)
						{
							_nswe = around[gX][gY - 1];
							_nswe |= SOUTH;
							around[gX][gY - 1] = _nswe;
						}
						if (gY + 1 < cells[gX].length && cells[gX][gY + 1] == 0)
						{
							_nswe = around[gX][gY + 1];
							_nswe |= NORTH;
							around[gX][gY + 1] = _nswe;
						}
						if (gX > 0 && cells[gX - 1][gY] == 0)
						{
							_nswe = around[gX - 1][gY];
							_nswe |= EAST;
							around[gX - 1][gY] = _nswe;
						}
						if (gX + 1 < cells.length && cells[gX + 1][gY] == 0)
						{
							_nswe = around[gX + 1][gY];
							_nswe |= WEST;
							around[gX + 1][gY] = _nswe;
						}
					}
				}

			collision.setGeoAround(around);
		}

		short height;
		byte old_nswe, close_nswe;

		for (int gX = 0; gX < around.length; gX++)
			for (int gY = 0; gY < around[gX].length; gY++)
			{
				int geoX = minX + gX;
				int geoY = minY + gY;

				byte[] block = getGeoBlockFromGeoCoords(geoX, geoY);
				if (block == null)
					continue;

				int cellX = getCell(geoX);
				int cellY = getCell(geoY);

				int index = 0;
				byte blockType = block[index];
				index++;

				switch (blockType)
				{
					case BLOCKTYPE_COMPLEX:
						index += (cellX << 3) + cellY << 1;

						height = makeShort(block[index + 1], block[index]);
						old_nswe = (byte) (height & 0x0F);
						height &= 0xfff0;
						height >>= 1;

						if (height < minZ || height > maxZ)
							break;

						close_nswe = around[gX][gY];

						if (isFirstTime)
						{
							if (collision.isGeoCloser())
								close_nswe &= old_nswe;
							else
								close_nswe &= ~old_nswe;
							around[gX][gY] = close_nswe;
						}

						height <<= 1;
						height &= 0xfff0;
						height |= old_nswe;

						if (collision.isGeoCloser())
							height &= ~close_nswe;
						else
							height |= close_nswe;

						block[index + 1] = (byte) (height >> 8);
						block[index] = (byte) (height & 0x00ff);
						break;
					case BLOCKTYPE_MULTILEVEL:
						int neededIndex = -1;

						int offset = (cellX << 3) + cellY;
						while (offset > 0)
						{
							byte lc = block[index];
							index += (lc << 1) + 1;
							offset--;
						}
						byte layers = block[index];
						index++;
						if (layers <= 0 || layers > MAX_LAYERS)
							break;
						short temph = Short.MIN_VALUE;
						old_nswe = NSWE_ALL;
						while (layers > 0)
						{
							height = makeShort(block[index + 1], block[index]);
							byte tmp_nswe = (byte) (height & 0x0F);
							height &= 0xfff0;
							height >>= 1;
							int z_diff_last = Math.abs(minZ - temph);
							int z_diff_curr = Math.abs(maxZ - height);
							if (z_diff_last > z_diff_curr)
							{
								old_nswe = tmp_nswe;
								temph = height;
								neededIndex = index;
							}
							layers--;
							index += 2;
						}

						if (temph == Short.MIN_VALUE || (temph < minZ || temph > maxZ))
							break;

						close_nswe = around[gX][gY];

						if (isFirstTime)
						{
							if (collision.isGeoCloser())
								close_nswe &= old_nswe;
							else
								close_nswe &= ~old_nswe;
							around[gX][gY] = close_nswe;
						}

						temph <<= 1;
						temph &= 0xfff0;
						temph |= old_nswe;
						if (collision.isGeoCloser())
							temph &= ~close_nswe;
						else
							temph |= close_nswe;

						block[neededIndex + 1] = (byte) (temph >> 8);
						block[neededIndex] = (byte) (temph & 0x00ff);
						break;
				}
			}
	}
	
	public static long makeLong(int nLo, int nHi)
	{
		return (long) nHi << 32 | nLo & 0x00000000ffffffffL;
	}
	
	public static Location findPointToStay(int x, int y, int z, int j, int k)
	{
		Location pos;
		for (int i = 0; i < 100; i++)
		{
			pos = Rnd.coordsRandomize(x, y, z, 0, j, k);
			if (canMoveToCoord(x, y, z, pos._x, pos._y, pos._z) && canMoveToCoord(pos._x, pos._y, pos._z, x, y, z))
				return pos;
		}
		return new Location(x, y, z);
	}
	
	public static void compact(boolean andClean)
	{
		long freeMemBefore = 0;
		long total = 0, optimized = 0;
		BlockLink[] links;
		byte[][] link_region;
		
		if (andClean)
		{
			Util.gc(2, 100);
			freeMemBefore = MemoryWatchOptimize.getMemFree();
		}
		
		for (int mapX = 0; mapX < L2World.WORLD_SIZE_X; mapX++)
			for (int mapY = 0; mapY < L2World.WORLD_SIZE_Y; mapY++)
			{
				if (geodata[mapX][mapY] == null)
					continue;
				total += BlocksInMap;
				links = GeoOptimizer.loadBlockMatches("./geodata/matches/" + (mapX + Config.GEO_X_FIRST) + "_" + (mapY + Config.GEO_Y_FIRST) + ".matches");
				if (links == null)
					continue;
				for (int i = 0; i < links.length; i++)
				{
					link_region = geodata[links[i].linkMapX][links[i].linkMapY];
					if (link_region == null)
						continue;
					link_region[links[i].linkBlockIndex][0] = geodata[mapX][mapY][links[i].blockIndex][0];
					optimized++;
				}
			}
		
		String logStr = String.format("Geo Engine: - Compacted %d of %d blocks...", optimized, total);
		if (andClean)
		{
			Util.gc(2, 100);
			logStr = String.format("%s Optimized ~%d Mb of memory", logStr, (MemoryWatchOptimize.getMemFree() - freeMemBefore) / 0x100000);
		}
		log.info(logStr);
	}
	
	public static boolean equalsData(byte[] a1, byte[] a2)
	{
		if (a1.length != a2.length)
			return false;
		for (int i = 0; i < a1.length; i++)
			if (a1[i] != a2[i])
				return false;
		return true;
	}
	
	public static boolean compareGeoBlocks(int mapX1, int mapY1, int blockIndex1, int mapX2, int mapY2, int blockIndex2)
	{
		return equalsData(geodata[mapX1][mapY1][blockIndex1], geodata[mapX2][mapY2][blockIndex2]);
	}
	
	private static void initChecksums()
	{
		log.info("Geo Engine: - Generating Checksums...");
		new File("./geodata/checksum").mkdirs();
		ParallelExecutor executor = new ParallelExecutor("initChecksums", Thread.MIN_PRIORITY);
		GeoOptimizer.checkSums = new int[L2World.WORLD_SIZE_X][L2World.WORLD_SIZE_Y][];
		for (int mapX = 0; mapX < L2World.WORLD_SIZE_X; mapX++)
			for (int mapY = 0; mapY < L2World.WORLD_SIZE_Y; mapY++)
				if (geodata[mapX][mapY] != null)
					executor.execute(new GeoOptimizer.CheckSumLoader(mapX, mapY, geodata[mapX][mapY]));
		try
		{
			executor.waitForFinishAndDestroy();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	private static void initBlockMatches(int maxScanRegions)
	{
		log.info("Geo Engine: - Generating Block Matches...");
		new File("./geodata/matches").mkdirs();
		ParallelExecutor executor = new ParallelExecutor("initBlockMatches", Thread.NORM_PRIORITY - 1);
		for (int mapX = 0; mapX < L2World.WORLD_SIZE_X; mapX++)
			for (int mapY = 0; mapY < L2World.WORLD_SIZE_Y; mapY++)
				if (geodata[mapX][mapY] != null && GeoOptimizer.checkSums != null && GeoOptimizer.checkSums[mapX][mapY] != null)
					executor.execute(new GeoOptimizer.GeoBlocksMatchFinder(mapX, mapY, maxScanRegions));
		try
		{
			executor.waitForFinishAndDestroy();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void deleteChecksumFiles()
	{
		for (int mapX = 0; mapX < L2World.WORLD_SIZE_X; mapX++)
			for (int mapY = 0; mapY < L2World.WORLD_SIZE_Y; mapY++)
			{
				if (geodata[mapX][mapY] == null)
					continue;
				new File("./geodata/checksum/" + (mapX + Config.GEO_X_FIRST) + "_" + (mapY + Config.GEO_Y_FIRST) + ".crc").delete();
			}
	}
	
	public static void genBlockMatches(int maxScanRegions)
	{
		initChecksums();
		initBlockMatches(maxScanRegions);
	}
	
	public static void unload()
	{
		for (int mapX = 0; mapX < L2World.WORLD_SIZE_X; mapX++)
			for (int mapY = 0; mapY < L2World.WORLD_SIZE_Y; mapY++)
				geodata[mapX][mapY] = null;
	}
}