package com.l2jhellas.gameserver.model.actor.instance;

import com.l2jhellas.gameserver.enums.FenceState;
import com.l2jhellas.gameserver.idfactory.IdFactory;
import com.l2jhellas.gameserver.instancemanager.FenceManager;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.network.serverpackets.DeleteObject;
import com.l2jhellas.gameserver.network.serverpackets.ExColosseumFenceInfo;

public final class L2FenceInstance extends L2Object
{
	private String _name;
	
	private final int _xMin;
	private final int _xMax;
	private final int _yMin;
	private final int _yMax;
	
	private final int _width;
	private final int _length;
	
	private FenceState _state;
	private int[] _heightFences;
	
	public L2FenceInstance(String name, int x, int y, int width, int length, int height, FenceState state)
	{
		super(IdFactory.getInstance().getNextId());
		
		_name = name;
		_xMin = x - (width / 2);
		_xMax = x + (width / 2);
		_yMin = y - (length / 2);
		_yMax = y + (length / 2);
		
		_width = width;
		_length = length;
		
		_state = state;
		
		if (height > 1)
		{
			_heightFences = new int[height - 1];
			for (int i = 0; i < _heightFences.length; i++)
			{
				_heightFences[i] = IdFactory.getInstance().getNextId();
			}
		}
	}
	
	public void setFenceName(String name)
	{
		_name = name;
	}
	
	public String getFenceName()
	{
		return _name;
	}
	
	public int getId()
	{
		return getObjectId();
	}
	
	public boolean deleteMe()
	{
		decayMe();
		
		FenceManager.getInstance().removeFence(this);
		
		return false;
	}
	
	public static boolean deleteAllFence()
	{
		FenceManager.getInstance().deleteAllFence();
		return false;
	}
	
	public FenceState getState()
	{
		return _state;
	}
	
	public void setState(FenceState type)
	{
		_state = type;
		
		broadcastInfo();
	}
	
	public int getWidth()
	{
		return _width;
	}
	
	public int getLength()
	{
		return _length;
	}
	
	public int getXMin()
	{
		return _xMin;
	}
	
	public int getYMin()
	{
		return _yMin;
	}
	
	public int getXMax()
	{
		return _xMax;
	}
	
	public int getYMax()
	{
		return _yMax;
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}
	
	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		activeChar.sendPacket(new ExColosseumFenceInfo(getId(), this));
		
		if (_heightFences != null)
		{
			for (int objId : _heightFences)
			{
				activeChar.sendPacket(new ExColosseumFenceInfo(objId, this));
			}
		}
	}
	
	@Override
	public void decayMe()
	{
		if (_heightFences != null)
		{
			final DeleteObject[] deleteObjects = new DeleteObject[_heightFences.length];
			for (int i = 0; i < _heightFences.length; i++)
			{
				deleteObjects[i] = new DeleteObject(_heightFences[i]);
			}
			
			L2World.getInstance().forEachVisibleObject(this, L2PcInstance.class, player -> player.sendPacket(deleteObjects));
		}
		
		super.decayMe();
	}
	
}