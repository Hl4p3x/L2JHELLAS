package com.l2jhellas.gameserver.geodata;

import com.l2jhellas.gameserver.geometry.Polygon;

public interface GeoControl
{
	public abstract Polygon getGeoPos();
	
	public abstract void setGeoPos(Polygon value);
	
	public abstract byte[][] getGeoAround();
	
	public abstract void setGeoAround(byte[][] around);
	
	public abstract boolean isGeoCloser();
}
