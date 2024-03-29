package com.l2jhellas.gameserver.cache;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.PackRoot;
import com.l2jhellas.gameserver.datatables.sql.ClanTable;
import com.l2jhellas.gameserver.idfactory.IdFactory;
import com.l2jhellas.gameserver.model.L2Clan;
import com.l2jhellas.util.database.L2DatabaseFactory;
import com.l2jhellas.util.filters.file.BmpFilter;
import com.l2jhellas.util.filters.file.OldPledgeFilter;

public class CrestCache
{
	private static final Logger _log = Logger.getLogger(CrestCache.class.getName());
	
	private final static ArrayList<CrestData> cache = new ArrayList<>();
	
	private final static String CRESTS_DIR = PackRoot.DATAPACK_ROOT + "/data/crests/";
	
	private final static FileFilter bmpFilter = new BmpFilter();
	private final static FileFilter oldPledgeFilter = new OldPledgeFilter();
	
	public static enum CrestType
	{
		PLEDGE("Crest_"),
		PLEDGE_LARGE("LargeCrest_"),
		PLEDGE_OLD("Pledge_"),
		ALLY("AllyCrest_");
		
		private final String _dirPrefix;
		
		CrestType(String dirPrefix)
		{
			_dirPrefix = dirPrefix;
		}
		
		public String getDirPrefix()
		{
			return _dirPrefix;
		}
	}
	
	static
	{
		if (!new File(CRESTS_DIR).mkdirs())
			convertOldPledgeFiles();
	}
	
	public static void load()
	{
		cache.clear();
		
		File[] files = new File(CRESTS_DIR).listFiles(bmpFilter);
		if (files == null)
			files = new File[0];
		
		String fileName;
		byte[] content;
		
		CrestType crestType = null;
		int crestId = 0;
		
		for (File file : files)
		{
			fileName = file.getName();
			try (RandomAccessFile f = new RandomAccessFile(file, "r"))
			{
				content = new byte[(int) f.length()];
				f.readFully(content);
				
				for (CrestType type : CrestType.values())
				{
					if (fileName.startsWith(type.getDirPrefix()))
					{
						crestType = type;
						crestId = Integer.valueOf(fileName.substring(type.getDirPrefix().length(), fileName.length() - 4));
					}
				}
				cache.add(new CrestData(crestType, crestId, content));
			}
			catch (Exception e)
			{
				_log.warning(CrestCache.class.getSimpleName() + ": Problem with loading crest bmp file: " + file);
			}
		}
		
		_log.info("Cache[Crest]: " + cache.size() + " files loaded.");
	}
	
	public static void convertOldPledgeFiles()
	{
		int clanId, newId;
		L2Clan clan;
		
		final File[] files = new File(CRESTS_DIR).listFiles(oldPledgeFilter);
		for (File file : files)
		{
			clanId = Integer.parseInt(file.getName().substring(7, file.getName().length() - 4));
			newId = IdFactory.getInstance().getNextId();
			clan = ClanTable.getInstance().getClan(clanId);
			
			_log.info("Found old crest file \"" + file.getName() + "\" for clanId " + clanId);
			
			if (clan != null)
			{
				removeCrest(CrestType.PLEDGE_LARGE, clan.getCrestId());
				
				file.renameTo(new File(CRESTS_DIR + "Crest_" + newId + ".bmp"));
				_log.info("Renamed Clan crest to new format: Crest_" + newId + ".bmp");
				
				try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				  PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET crest_id = ? WHERE clan_id = ?"))
				{
					statement.setInt(1, newId);
					statement.setInt(2, clan.getClanId());
					statement.executeUpdate();
					statement.close();
				}
				catch (SQLException e)
				{
					_log.warning(CrestCache.class.getSimpleName() + ": Could not update the crest id:");
				}
				
				clan.setCrestId(newId);
			}
			else
			{
				_log.info("Clan Id: " + clanId + " does not exist in table.. deleting.");
				file.delete();
			}
		}
	}
	
	public static byte[] getCrest(CrestType crestType, int id)
	{
		for (CrestData crest : cache)
			if (crest.getCrestType() == crestType && crest.getCrestId() == id)
				return crest.getHash();
		
		return null;
	}
	
	public static void removeCrest(CrestType crestType, int id)
	{
		String crestDirPrefix = crestType.getDirPrefix();
		
		if (!crestDirPrefix.equals("Pledge_"))
		{
			for (CrestData crestData : cache)
				if (crestData.getCrestType() == crestType && crestData.getCrestId() == id)
				{
					cache.remove(crestData);
					break;
				}
		}
		
		File crestFile = new File(CRESTS_DIR + crestDirPrefix + id + ".bmp");
		if (!crestFile.delete())
			_log.warning(CrestCache.class.getSimpleName() + ": CrestCache: Failed to delete " + crestDirPrefix + id + ".bmp");
	}
	
	public static boolean saveCrest(CrestType crestType, int newId, byte[] data)
	{
		File crestFile = new File(CRESTS_DIR + crestType.getDirPrefix() + newId + ".bmp");
		try (FileOutputStream out = new FileOutputStream(crestFile))
		{
			out.write(data);
			
			cache.add(new CrestData(crestType, newId, data));
			
			return true;
		}
		catch (IOException e)
		{
			_log.info(CrestCache.class.getName() + "Error saving pledge crest" + crestFile + ":");
			return false;
		}
	}
	
	private static class CrestData
	{
		private final CrestType _crestType;
		private final int _crestId;
		private final byte[] _hash;
		
		CrestData(CrestType crestType, int crestId, byte[] hash)
		{
			_crestType = crestType;
			_crestId = crestId;
			_hash = hash;
		}
		
		public CrestType getCrestType()
		{
			return _crestType;
		}
		
		public int getCrestId()
		{
			return _crestId;
		}
		
		public byte[] getHash()
		{
			return _hash;
		}
	}
}