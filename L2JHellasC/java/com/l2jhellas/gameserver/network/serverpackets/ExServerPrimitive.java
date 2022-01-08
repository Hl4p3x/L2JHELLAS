package com.l2jhellas.gameserver.network.serverpackets;

import java.awt.Color;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.position.Location;

public class ExServerPrimitive extends L2GameServerPacket
{
	private static final int MAX_SIZE = 16000;
	
	private final Set<Point> _points = ConcurrentHashMap.newKeySet();
	private final Set<Line> _lines = ConcurrentHashMap.newKeySet();
	
	private final String _name;
	
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _index;
	
	private int _size = 0;
	private ExServerPrimitive _next;

	public ExServerPrimitive(String name, Location location)
	{
		_name = name;
		_x = location.getX();
		_y = location.getY();
		_z = location.getZ();
		_index = 0;
	}

	public ExServerPrimitive()
	{
		_name = null;
		_x = 0;
		_y = 0;
		_z = 0;
		_index = -1;
	}

	public ExServerPrimitive(ExServerPrimitive parent)
	{
		_name = parent._name;
		_x = parent._x;
		_y = parent._y;
		_z = parent._z;
		_index = parent._index + 1;
	}

	private void addPoint(Point point)
	{
		if (_size < MAX_SIZE || _index < 0)
		{
			_size += point.size();
			_points.add(point);
			return;
		}
		
		if (_next == null)
			_next = new ExServerPrimitive(this);
		
		_next.addPoint(point);
	}

	public void addPoint(String name, Color color, boolean isNameColored, int x, int y, int z)
	{
		addPoint(new Point(name, color, isNameColored, x, y, z));
	}

	public void addPoint(String name, Color color, boolean isNameColored, Location loc)
	{
		addPoint(name, color, isNameColored, loc.getX(), loc.getY(), loc.getZ());
	}

	public void addPoint(Color color, int x, int y, int z)
	{
		addPoint("", color, false, x, y, z);
	}

	public void addPoint(Color color, Location loc)
	{
		addPoint("", color, false, loc.getX(), loc.getY(), loc.getZ());
	}

	private void addLine(Line line)
	{
		if (_size < MAX_SIZE || _index < 0)
		{
			_size += line.size();
			_lines.add(line);
			return;
		}
		
		if (_next == null)
			_next = new ExServerPrimitive(this);
		
		_next.addLine(line);
	}

	public void addLine(String name, Color color, boolean isNameColored, int x, int y, int z, int x2, int y2, int z2)
	{
		addLine(new Line(name, color, isNameColored, x, y, z, x2, y2, z2));
	}

	public void addLine(String name, Color color, boolean isNameColored, Location loc, int x2, int y2, int z2)
	{
		addLine(name, color, isNameColored, loc.getX(), loc.getY(), loc.getZ(), x2, y2, z2);
	}

	public void addLine(String name, Color color, boolean isNameColored, int x, int y, int z, Location loc)
	{
		addLine(name, color, isNameColored, x, y, z, loc.getX(), loc.getY(), loc.getZ());
	}

	public void addLine(String name, Color color, boolean isNameColored, Location loc, Location loc2)
	{
		addLine(name, color, isNameColored, loc.getX(), loc.getY(), loc.getZ(), loc2.getX(), loc2.getY(), loc2.getZ());
	}

	public void addLine(Color color, int x, int y, int z, int x2, int y2, int z2)
	{
		addLine("", color, false, x, y, z, x2, y2, z2);
	}

	public void addLine(Color color, Location loc, int x2, int y2, int z2)
	{
		addLine("", color, false, loc.getX(), loc.getY(), loc.getZ(), x2, y2, z2);
	}

	public void addLine(Color color, int x, int y, int z, Location loc)
	{
		addLine("", color, false, x, y, z, loc.getX(), loc.getY(), loc.getZ());
	}

	public void addLine(Color color, Location loc, Location loc2)
	{
		addLine("", color, false, loc.getX(), loc.getY(), loc.getZ(), loc2.getX(), loc2.getY(), loc2.getZ());
	}

	public void addRectangle(String name, Color color, boolean isNameColored, int x, int y, int x2, int y2, int z)
	{
		addLine(name, color, isNameColored, x, y, z, x, y2, z);
		addLine(name, color, isNameColored, x2, y2, z, x, y2, z);
		addLine(name, color, isNameColored, x2, y2, z, x2, y, z);
		addLine(name, color, isNameColored, x, y, z, x2, y, z);
	}

	public void addRectangle(String name, Color color, boolean isNameColored, Location loc, Location loc2)
	{
		addRectangle(name, color, isNameColored, loc.getX(), loc.getY(), loc2.getX(), loc2.getY(), loc.getZ());
	}

	public void addRectangle(Color color, int x, int y, int x2, int y2, int z)
	{
		addRectangle("", color, false, x, y, x, y2, z);
	}

	public void addRectangle(Color color, Location loc, Location loc2)
	{
		addRectangle("", color, false, loc.getX(), loc.getY(), loc2.getX(), loc2.getY(), loc.getZ());
	}

	public void addSquare(String name, Color color, boolean isNameColored, int x, int y, int z, int size)
	{
		int x2 = x + size;
		int y2 = y + size;
		addLine(name, color, isNameColored, x, y, z, x, y2, z);
		addLine(name, color, isNameColored, x2, y2, z, x, y2, z);
		addLine(name, color, isNameColored, x2, y2, z, x2, y, z);
		addLine(name, color, isNameColored, x, y, z, x2, y, z);
	}

	public void addSquare(String name, Color color, boolean isNameColored, Location loc, int size)
	{
		addSquare(name, color, isNameColored, loc.getX(), loc.getY(), loc.getZ(), size);
	}

	public void addSquare(Color color, int x, int y, int z, int size)
	{
		addSquare("", color, false, x, y, z, size);
	}

	public void addSquare(Color color, Location loc, int size)
	{
		addSquare("", color, false, loc.getX(), loc.getY(), loc.getZ(), size);
	}

	public void addAll(ExServerPrimitive esp)
	{
		for (Point p : esp._points)
			addPoint(p);
		for (Line l : esp._lines)
			addLine(l);
	}

	public void reset()
	{
		_lines.clear();
		_points.clear();
		_size = 0;
		
		if (_next != null)
			_next.reset();
	}

	public void sendTo(L2PcInstance player)
	{
		if (_size == 0)
			addPoint(Color.WHITE, _x, _y, 16384);
		
		player.sendPacket(this);
		
		if (_next == null)
			return;
		
		if (_next._size == 0)
		{
			_next.addPoint(Color.WHITE, _x, _y, 16384);
			
			_next.sendTo(player);
			_next = null;
		}
		else
			_next.sendTo(player);
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x24);
		if (_index == 0)
			writeS(_name);
		else
			writeS(_name == null ? "null" + _index : _name + _index);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(Integer.MAX_VALUE); 
		writeD(Integer.MAX_VALUE); 
		
		writeD(_points.size() + _lines.size());
		
		for (Point point : _points)
		{
			writeC(1); 
			writeS(point.getName());
			int color = point.getColor();
			writeD((color >> 16) & 0xFF); // R
			writeD((color >> 8) & 0xFF); // G
			writeD(color & 0xFF); // B
			writeD(point.isNameColored() ? 1 : 0);
			writeD(point.getX());
			writeD(point.getY());
			writeD(point.getZ());
		}
		
		for (Line line : _lines)
		{
			writeC(2); // Its the type in this case Line
			writeS(line.getName());
			int color = line.getColor();
			writeD((color >> 16) & 0xFF); // R
			writeD((color >> 8) & 0xFF); // G
			writeD(color & 0xFF); // B
			writeD(line.isNameColored() ? 1 : 0);
			writeD(line.getX());
			writeD(line.getY());
			writeD(line.getZ());
			writeD(line.getX2());
			writeD(line.getY2());
			writeD(line.getZ2());
		}
	}
	
	private static class Point
	{
		protected final String _name;
		private final int _color;
		private final boolean _isNameColored;
		private final int _x;
		private final int _y;
		private final int _z;
		
		public Point(String name, Color color, boolean isNameColored, int x, int y, int z)
		{
			_name = name;
			_color = color.getRGB();
			_isNameColored = isNameColored;
			_x = x;
			_y = y;
			_z = z;
		}
		
		public String getName()
		{
			return _name;
		}
		
		public int getColor()
		{
			return _color;
		}
		
		public boolean isNameColored()
		{
			return _isNameColored;
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
		
		public int size()
		{
			return _name == null ? 31 : 31 + 2 * _name.length();
		}
	}
	
	private static class Line extends Point
	{
		private final int _x2;
		private final int _y2;
		private final int _z2;
		
		public Line(String name, Color color, boolean isNameColored, int x, int y, int z, int x2, int y2, int z2)
		{
			super(name, color, isNameColored, x, y, z);
			_x2 = x2;
			_y2 = y2;
			_z2 = z2;
		}
		
		public int getX2()
		{
			return _x2;
		}
		
		public int getY2()
		{
			return _y2;
		}
		
		public int getZ2()
		{
			return _z2;
		}
		
		@Override
		public int size()
		{
			return _name == null ? 43 : 43 + 2 * _name.length();
		}
	}
}