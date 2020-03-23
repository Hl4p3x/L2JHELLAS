package com.l2jhellas.gameserver.geodata.loader;

import java.io.File;

public class GeoLoaderFactory
{
	
	private static GeoLoaderFactory instance;
	
	private final GeoLoader[] geoLoaders;
	
	public static GeoLoaderFactory getInstance()
	{
		
		if (instance == null)
		{
			instance = new GeoLoaderFactory();
		}
		
		return instance;
	}
	
	private GeoLoaderFactory()
	{
		geoLoaders = new GeoLoader[]
		{
			new L2JGeoLoader(),
			new OffGeoLoader()
		};
	}
	
	public GeoLoader getGeoLoader(File file)
	{
		if (file == null)
		{
			return null;
		}
		
		for (GeoLoader geoLoader : geoLoaders)
		{
			if (geoLoader.isAcceptable(file))
			{
				return geoLoader;
			}
		}
		
		return null;
	}
}
