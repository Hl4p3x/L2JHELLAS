package com.l2jhellas.gameserver.model;

import java.util.ArrayList;
import java.util.List;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.RadarControl;

public final class L2Radar
{
	private final L2PcInstance _player;
	private final List<RadarMarker> _markers;
	
	public L2Radar(L2PcInstance player)
	{
		_player = player;
		_markers = new ArrayList<>();
	}
	
	// Add a marker to player's radar
	public void addMarker(int x, int y, int z)
	{
		RadarMarker newMarker = new RadarMarker(x, y, z);
		
		_markers.add(newMarker);
		_player.sendPacket(new RadarControl(2, 2, x, y, z));
		_player.sendPacket(new RadarControl(0, 1, x, y, z));
	}
	
	// Remove a marker from player's radar
	public void removeMarker(int x, int y, int z)
	{
		RadarMarker newMarker = new RadarMarker(x, y, z);
		
		_markers.remove(newMarker);
		_player.sendPacket(new RadarControl(1, 1, x, y, z));
	}
	
	public void removeAllMarkers()
	{
		// TODO: Need method to remove all markers from radar at once
		for (RadarMarker tempMarker : _markers)
			_player.sendPacket(new RadarControl(1, tempMarker._type, tempMarker._x, tempMarker._y, tempMarker._z));
		
		_markers.clear();
	}
	
	public void loadMarkers()
	{
		_player.sendPacket(new RadarControl(2, 2, _player.getX(), _player.getY(), _player.getZ()));
		for (RadarMarker tempMarker : _markers)
			_player.sendPacket(new RadarControl(0, 1, tempMarker._x, tempMarker._y, tempMarker._z));
	}
	
	public class RadarMarker
	{
		// Simple class to model radar points.
		public int _type, _x, _y, _z;
		
		public RadarMarker(int type, int x, int y, int z)
		{
			_type = type;
			_x = x;
			_y = y;
			_z = z;
		}
		
		public RadarMarker(int x, int y, int z)
		{
			_type = 1;
			_x = x;
			_y = y;
			_z = z;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			try
			{
				RadarMarker temp = (RadarMarker) obj;
				
				if ((temp._x == _x) && (temp._y == _y) && (temp._z == _z) && (temp._type == _type))
					return true;
				
				return false;
			}
			catch (Exception e)
			{
				return false;
			}
		}
	}
	
	public class RadarOnPlayer implements Runnable
	{
		private final L2PcInstance _myTarget, _me;
		
		public RadarOnPlayer(L2PcInstance target, L2PcInstance me)
		{
			_me = me;
			_myTarget = target;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (_me == null || !_me.isOnline())
					return;
				
				_me.sendPacket(new RadarControl(1, 1, _me.getX(), _me.getY(), _me.getZ()));
				if (_myTarget == null || !_myTarget.isOnline())
					return;
				
				_me.sendPacket(new RadarControl(0, 1, _myTarget.getX(), _myTarget.getY(), _myTarget.getZ()));
				ThreadPoolManager.getInstance().scheduleGeneral(new RadarOnPlayer(_myTarget, _me), 15000);
			}
			catch (Throwable t)
			{
			}
		}
	}
}