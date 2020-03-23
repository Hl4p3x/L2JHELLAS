package com.l2jhellas.gameserver.geodata.loader;

import java.io.File;

public interface GeoLoader
{
	
	public boolean isAcceptable(File file);
	
	public GeoFileInfo readFile(File file);
}