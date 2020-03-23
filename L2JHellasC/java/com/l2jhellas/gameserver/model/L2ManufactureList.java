package com.l2jhellas.gameserver.model;

import java.util.ArrayList;
import java.util.List;

public class L2ManufactureList
{
	private List<L2ManufactureItem> _list;
	private boolean _confirmed;
	private String _manufactureStoreName;
	
	public L2ManufactureList()
	{
		_list = new ArrayList<>();
		_confirmed = false;
	}
	
	public int size()
	{
		return _list.size();
	}
	
	public void setConfirmedTrade(boolean x)
	{
		_confirmed = x;
	}
	
	public boolean hasConfirmed()
	{
		return _confirmed;
	}
	
	public void setStoreName(String manufactureStoreName)
	{
		_manufactureStoreName = manufactureStoreName;
	}
	
	public String getStoreName()
	{
		return _manufactureStoreName;
	}
	
	public void add(L2ManufactureItem item)
	{
		_list.add(item);
	}
	
	public List<L2ManufactureItem> getList()
	{
		return _list;
	}
	
	public void setList(List<L2ManufactureItem> list)
	{
		_list = list;
	}
	
	public void clear()
	{
		_list.clear();
	}
}