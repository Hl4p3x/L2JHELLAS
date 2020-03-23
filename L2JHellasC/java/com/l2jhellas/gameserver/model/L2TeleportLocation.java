package com.l2jhellas.gameserver.model;

import java.util.List;

public class L2TeleportLocation
{
	private int _teleId;
	private int _locX;
	private int _locY;
	private int _locZ;
	private List<int[]> _itemList;
	private boolean _forNoble;
	private boolean _forGM;
	private boolean _forClanHall;
	private boolean _forFort;
	private boolean _forCastle;
	private int _minLevel;
	private int _maxLevel;
	
	public void setItemsList(List<int[]> itemList)
	{
		_itemList = itemList;
	}
	
	public void setIsForGM(boolean val)
	{
		_forGM = val;
	}
	
	public void setIsForClanHall(boolean val)
	{
		_forClanHall = val;
	}
	
	public void setIsForFort(boolean val)
	{
		_forFort = val;
	}
	
	public void setIsForCastle(boolean val)
	{
		_forCastle = val;
	}
	
	public void setTeleId(int id)
	{
		_teleId = id;
	}
	
	public void setLocX(int locX)
	{
		_locX = locX;
	}
	
	public void setLocY(int locY)
	{
		_locY = locY;
	}
	
	public void setLocZ(int locZ)
	{
		_locZ = locZ;
	}
	
	public List<int[]> getItemsList()
	{
		return _itemList;
	}
	
	public void setIsForNoble(boolean val)
	{
		_forNoble = val;
	}
	
	public int getTeleId()
	{
		return _teleId;
	}
	
	public int getLocX()
	{
		return _locX;
	}
	
	public int getLocY()
	{
		return _locY;
	}
	
	public int getLocZ()
	{
		return _locZ;
	}
	
	public boolean getIsForNoble()
	{
		return _forNoble;
	}
	
	public boolean getIsForGM()
	{
		return _forGM;
	}
	
	public boolean getIsForClanHall()
	{
		return _forClanHall;
	}
	
	public boolean getIsForFort()
	{
		return _forFort;
	}
	
	public boolean getIsForCastle()
	{
		return _forCastle;
	}
	
	public void setMinLevel(int val)
	{
		_minLevel = val;
	}
	
	public int getMinLevel()
	{
		return _minLevel;
	}
	
	public void setMaxLevel(int val)
	{
		_maxLevel = val;
	}
	
	public int getMaxLevel()
	{
		return _maxLevel;
	}
}