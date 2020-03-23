package com.l2jhellas.gameserver.model;

public class PcColorContainer
{
	private final int _color;
	private final long _regTime;
	private final long _time;
	
	public PcColorContainer(int color, long regTime, long time)
	{
		_color = color;
		_regTime = regTime;
		_time = time;
	}
	
	public int getColor()
	{
		return _color;
	}
	
	public long getRegTime()
	{
		return _regTime;
	}
	
	public long getTime()
	{
		return _time;
	}
}