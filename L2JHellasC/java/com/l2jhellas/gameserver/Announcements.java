package com.l2jhellas.gameserver;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.PackRoot;
import com.l2jhellas.Config;
import com.l2jhellas.gameserver.cache.HtmCache;
import com.l2jhellas.gameserver.enums.player.ChatType;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.CreatureSay;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;
import com.l2jhellas.util.Broadcast;

public class Announcements
{
	private static Logger _log = Logger.getLogger(Announcements.class.getName());
	
	private final List<String> _announcements = new ArrayList<>();
	private final List<List<Object>> _eventAnnouncements = new ArrayList<>();
	
	public Announcements()
	{
		loadAnnouncements();
	}
	
	public void loadAnnouncements()
	{
		_announcements.clear();
		File file = new File(PackRoot.DATAPACK_ROOT, "data/announcements.txt");
		if (!file.exists())
			_log.warning(Announcements.class.getName() + ": data/announcements.txt doesn't exist.");
		readFromDisk(file);
	}
	
	public void showAnnouncements(L2PcInstance activeChar)
	{
		for (int i = 0; i < _announcements.size(); i++)
		{
			CreatureSay cs = new CreatureSay(0, ChatType.ANNOUNCEMENT.getClientId(), activeChar.getName(), _announcements.get(i));
			activeChar.sendPacket(cs);
		}
		
		for (int i = 0; i < _eventAnnouncements.size(); i++)
		{
			List<Object> entry = _eventAnnouncements.get(i);
			
			DateRange validDateRange = (DateRange) entry.get(0);
			String[] msg = (String[]) entry.get(1);
			Date currentDate = new Date();
			
			if (!validDateRange.isValid() || validDateRange.isWithinRange(currentDate))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_S2);
				for (String element : msg)
				{
					sm.addString(element);
				}
				activeChar.sendPacket(sm);
			}
		}
	}
	
	public void addEventAnnouncement(DateRange validDateRange, String[] msg)
	{
		List<Object> entry = new ArrayList<>();
		entry.add(validDateRange);
		entry.add(msg);
		_eventAnnouncements.add(entry);
	}
	
	public void listAnnouncements(L2PcInstance activeChar)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setHtml(HtmCache.getInstance().getHtm("data/html/admin/announce.htm"));
		StringBuilder replyMSG = new StringBuilder("<br>");
		for (int i = 0; i < _announcements.size(); i++)
		{
			replyMSG.append("<table width=260><tr><td width=220>" + _announcements.get(i) + "</td><td width=40>");
			replyMSG.append("<button value=\"Delete\" action=\"bypass -h admin_del_announcement " + i + "\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table>");
		}
		adminReply.replace("%announces%", replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	public boolean addAnnouncement(String text)
	{
		if (text == null || text.isEmpty())
			return false;
		
		_announcements.add(text);
		
		saveToDisk();
		return true;
	}
	
	public void delAnnouncement(int line)
	{
		_announcements.remove(line);
		saveToDisk();
	}
	
	private void readFromDisk(File file)
	{
		try (LineNumberReader lnr = new LineNumberReader(new FileReader(file)))
		{
			int i = 0;
			String line = null;
			while ((line = lnr.readLine()) != null)
			{
				StringTokenizer st = new StringTokenizer(line, "\n\r");
				if (st.hasMoreTokens())
				{
					String announcement = st.nextToken();
					_announcements.add(announcement);
					
					i++;
				}
			}
			_log.info(Announcements.class.getSimpleName() + ": Loaded " + i + " Announcements.");
		}
		catch (IOException e1)
		{
			_log.severe(Announcements.class.getName() + ": Error reading announcements");
			if (Config.DEVELOPER)
				e1.printStackTrace();
		}
	}
	
	private void saveToDisk()
	{
		try (FileWriter save = new FileWriter(new File("data/announcements.txt")))
		{
			for (int i = 0; i < _announcements.size(); i++)
			{
				save.write(_announcements.get(i));
				save.write("\r\n");
			}
		}
		catch (IOException e)
		{
			_log.warning(Announcements.class.getName() + ": saving the announcements file has failed: ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	// Colored Announcements 8D
	public void gameAnnounceToAll(String text)
	{
		announceToAll(text);
	}
	
	public void announceToAll(String text)
	{
		Broadcast.announceToOnlinePlayers(text);
	}
	
	public void announceToAll(SystemMessage sm)
	{
		
		Broadcast.toAllOnlinePlayers(sm);
	}
	
	// Method fo handling announcements from admin
	public void handleAnnounce(String command, int lengthToTrim)
	{
		try
		{
			// Announce string to everyone on server
			String text = command.substring(lengthToTrim);
			announceToAll(text);
		}
		// No body cares!
		catch (StringIndexOutOfBoundsException e)
		{
			// empty message.. ignore
		}
	}
	
	public static Announcements getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final Announcements INSTANCE = new Announcements();
	}
}
