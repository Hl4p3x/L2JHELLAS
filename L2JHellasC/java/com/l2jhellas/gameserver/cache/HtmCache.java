package com.l2jhellas.gameserver.cache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.util.UnicodeReader;
import com.l2jhellas.util.filters.file.HtmFilter;

public class HtmCache
{
	private static final Logger _log = Logger.getLogger(HtmCache.class.getName());
	
	private static final Map<Integer, String> _htmCache = new HashMap<>();
	private static final FileFilter _htmFilter = new HtmFilter();
	
	public static HtmCache getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected HtmCache()
	{
		reload();
	}
	
	public void reload()
	{
		_log.info("HtmCache: Cache cleared, had " + _htmCache.size() + " entries.");
		_htmCache.clear();
	}
	
	public void reloadPath(String path)
	{
		parseDir(new File(path));
		_log.info("HtmCache: Reloaded specified " + path + " path.");
	}
	
	private static void parseDir(File dir)
	{
		for (File file : dir.listFiles(_htmFilter))
		{
			if (file.isDirectory())
				parseDir(file);
			else
				loadFile(file);
		}
	}
	
	private static String loadFile(File file)
	{
		if (file.exists() && _htmFilter.accept(file) && !file.isDirectory())
		{
			try (FileInputStream fis = new FileInputStream(file);
				UnicodeReader ur = new UnicodeReader(fis, "UTF-8");
				BufferedReader br = new BufferedReader(ur))
			{
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null)
					sb.append(line).append('\n');
				
				String content = sb.toString().replaceAll("\r\n", "\n");
				sb = null;
				
				_htmCache.put(file.getPath().replace("\\", "/").hashCode(), content);
				return content;
			}
			catch (IOException e)
			{
				_log.warning(HtmCache.class.getSimpleName() + ": HtmCache: problem with loading file");
				if (Config.DEVELOPER)
					e.printStackTrace();
			}
		}
		
		return null;
	}
	
	public boolean isLoadable(String path)
	{
		return loadFile(new File(path)) != null;
	}
	
	public String getHtm(String filename)
	{
		if (filename == null || filename.isEmpty())
			return "";
		
		String content = _htmCache.get(filename.hashCode());
		if (content == null)
			content = loadFile(new File(filename));
		
		return content;
	}
	
	public String getHtmForce(String filename)
	{
		String content = getHtm(filename);
		if (content == null)
		{
			content = "<html><body>My html is missing:<br>" + filename + "</body></html>";
			_log.warning(HtmCache.class.getName() + ": HtmCache: " + filename + " is missing.");
		}
		
		return content;
	}
	
	private static class SingletonHolder
	{
		protected static final HtmCache _instance = new HtmCache();
	}
}