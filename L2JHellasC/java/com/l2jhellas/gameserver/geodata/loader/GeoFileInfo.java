package com.l2jhellas.gameserver.geodata.loader;

public class GeoFileInfo
{
	
	private int x;
	
	private int y;
	
	private byte[][] data;
	
	public GeoFileInfo()
	{
	}
	
	public GeoFileInfo(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	
	public int getX()
	{
		return x;
	}
	
	public void setX(int x)
	{
		this.x = x;
	}
	
	public int getY()
	{
		return y;
	}
	
	public void setY(int y)
	{
		this.y = y;
	}
	
	public byte[][] getData()
	{
		return data;
	}
	
	public void setData(byte[][] data)
	{
		this.data = data;
	}
}
