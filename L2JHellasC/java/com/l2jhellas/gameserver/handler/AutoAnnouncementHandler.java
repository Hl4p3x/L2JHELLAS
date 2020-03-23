package com.l2jhellas.gameserver.handler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.Announcements;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class AutoAnnouncementHandler
{
	protected static final Logger _log = Logger.getLogger(AutoAnnouncementHandler.class.getName());
	
	private static AutoAnnouncementHandler _instance;
	
	private static final long DEFAULT_ANNOUNCEMENT_DELAY = 180000; // 3 mins by default
	
	protected Map<Integer, AutoAnnouncementInstance> _registeredAnnouncements;
	
	protected AutoAnnouncementHandler()
	{
		_registeredAnnouncements = new HashMap<>();
		restoreAnnouncementData();
	}
	
	private void restoreAnnouncementData()
	{
		int numLoaded = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT * FROM auto_announcements ORDER BY id");
			ResultSet rs = statement.executeQuery();
			
			while (rs.next())
			{
				numLoaded++;
				
				registerGlobalAnnouncement(rs.getInt("id"), rs.getString("announcement"), rs.getLong("delay"));
			}
			
			rs.close();
			statement.close();
			_log.info(AutoAnnouncementHandler.class.getSimpleName() + ": Loaded " + numLoaded + " Auto Announcements.");
		}
		catch (Exception e)
		{
			_log.warning(AutoAnnouncementHandler.class.getName() + ": Error cant find DB");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
	}
	
	public void listAutoAnnouncements(L2PcInstance activeChar)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		
		StringBuilder replyMSG = new StringBuilder("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40></td>");
		replyMSG.append("<button value=\"Main\" action=\"bypass -h admin_admin\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>");
		replyMSG.append("<td width=180><center>Auto Announcement Menu</center></td>");
		replyMSG.append("<td width=40></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<center>Add new auto announcement:</center>");
		replyMSG.append("<center><multiedit var=\"new_autoannouncement\" width=240 height=30></center><br>");
		replyMSG.append("<br><br>");
		replyMSG.append("<center>Delay: <edit var=\"delay\" width=70></center>");
		replyMSG.append("<center>Note: Time in Seconds 60s = 1 min.</center>");
		replyMSG.append("<br><br>");
		replyMSG.append("<center><table><tr><td>");
		replyMSG.append("<button value=\"Add\" action=\"bypass -h admin_add_autoannouncement $delay $new_autoannouncement\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
		replyMSG.append("</td></tr></table></center>");
		replyMSG.append("<br>");
		
		for (AutoAnnouncementInstance announcementInst : AutoAnnouncementHandler.getInstance().values())
		{
			replyMSG.append("<table width=260><tr><td width=220>[" + announcementInst.getDefaultDelay() + "s] " + announcementInst.getDefaultTexts().toString() + "</td><td width=40>");
			replyMSG.append("<button value=\"Delete\" action=\"bypass -h admin_del_autoannouncement " + announcementInst.getDefaultId() + "\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table>");
		}
		
		replyMSG.append("</body></html>");
		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
	
	public static AutoAnnouncementHandler getInstance()
	{
		if (_instance == null)
			_instance = new AutoAnnouncementHandler();
		
		return _instance;
	}
	
	public int size()
	{
		return _registeredAnnouncements.size();
	}
	
	public AutoAnnouncementInstance registerGlobalAnnouncement(int id, String announcementTexts, long announcementDelay)
	{
		return registerAnnouncement(id, announcementTexts, announcementDelay);
	}
	
	public AutoAnnouncementInstance registerAnnouncment(int id, String announcementTexts, long announcementDelay)
	{
		return registerAnnouncement(id, announcementTexts, announcementDelay);
	}
	
	public AutoAnnouncementInstance registerAnnouncment(String announcementTexts, long announcementDelay)
	{
		int nextId = nextAutoAnnouncmentId();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("INSERT INTO auto_announcements (id,announcement,delay) VALUES (?,?,?)");
			statement.setInt(1, nextId);
			statement.setString(2, announcementTexts);
			statement.setLong(3, announcementDelay);
			
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning(AutoAnnouncementHandler.class.getName() + ": Could Not Insert Auto Announcment into DataBase: Reason: Duplicate Id");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
		
		return registerAnnouncement(nextId, announcementTexts, announcementDelay);
	}
	
	public int nextAutoAnnouncmentId()
	{
		int nextId = 0;
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT id FROM auto_announcements ORDER BY id");
			ResultSet rs = statement.executeQuery();
			
			while (rs.next())
			{
				if (rs.getInt("id") > nextId)
					nextId = rs.getInt("id");
			}
			rs.close();
			statement.close();
			
			nextId++;
		}
		catch (Exception e)
		{
			_log.warning(AutoAnnouncementHandler.class.getName() + ": Error cant select from DB ");
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
		
		return nextId;
	}
	
	private final AutoAnnouncementInstance registerAnnouncement(int id, String announcementTexts, long chatDelay)
	{
		AutoAnnouncementInstance announcementInst = null;
		
		if (chatDelay < 0)
			chatDelay = DEFAULT_ANNOUNCEMENT_DELAY;
		
		if (_registeredAnnouncements.containsKey(id))
			announcementInst = _registeredAnnouncements.get(id);
		else
			announcementInst = new AutoAnnouncementInstance(id, announcementTexts, chatDelay);
		
		_registeredAnnouncements.put(id, announcementInst);
		
		return announcementInst;
	}
	
	public Collection<AutoAnnouncementInstance> values()
	{
		return _registeredAnnouncements.values();
	}
	
	public boolean removeAnnouncement(int id)
	{
		AutoAnnouncementInstance announcementInst = _registeredAnnouncements.get(id);
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("DELETE FROM auto_announcements WHERE id=?");
			statement.setInt(1, announcementInst.getDefaultId());
			statement.executeUpdate();
			
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning(AutoAnnouncementHandler.class.getName() + ": Could not Delete Auto Announcement in Database, Reason:");
			if (Config.DEVELOPER)
			{
				e.printStackTrace();
			}
		}
		
		return removeAnnouncement(announcementInst);
	}
	
	public boolean removeAnnouncement(AutoAnnouncementInstance announcementInst)
	{
		if (announcementInst == null)
			return false;
		
		_registeredAnnouncements.remove(announcementInst.getDefaultId());
		announcementInst.setActive(false);
		
		return true;
	}
	
	public AutoAnnouncementInstance getAutoAnnouncementInstance(int id)
	{
		return _registeredAnnouncements.get(id);
	}
	
	public void setAutoAnnouncementActive(boolean isActive)
	{
		for (AutoAnnouncementInstance announcementInst : _registeredAnnouncements.values())
			announcementInst.setActive(isActive);
	}
	
	public class AutoAnnouncementInstance
	{
		private long _defaultDelay = DEFAULT_ANNOUNCEMENT_DELAY;
		private String _defaultTexts;
		private boolean _defaultRandom = false;
		private final Integer _defaultId;
		
		private boolean _isActive;
		
		public ScheduledFuture<?> _chatTask;
		
		protected AutoAnnouncementInstance(int id, String announcementTexts, long announcementDelay)
		{
			_defaultId = id;
			_defaultTexts = announcementTexts;
			_defaultDelay = (announcementDelay * 1000);
			
			setActive(true);
		}
		
		public boolean isActive()
		{
			return _isActive;
		}
		
		public boolean isDefaultRandom()
		{
			return _defaultRandom;
		}
		
		public long getDefaultDelay()
		{
			return _defaultDelay;
		}
		
		public String getDefaultTexts()
		{
			return _defaultTexts;
		}
		
		public Integer getDefaultId()
		{
			return _defaultId;
		}
		
		public void setDefaultChatDelay(long delayValue)
		{
			_defaultDelay = delayValue;
		}
		
		public void setDefaultChatTexts(String textsValue)
		{
			_defaultTexts = textsValue;
		}
		
		public void setDefaultRandom(boolean randValue)
		{
			_defaultRandom = randValue;
		}
		
		public void setActive(boolean activeValue)
		{
			if (_isActive == activeValue)
				return;
			
			_isActive = activeValue;
			
			if (isActive())
			{
				AutoAnnouncementRunner acr = new AutoAnnouncementRunner(_defaultId);
				_chatTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(acr, _defaultDelay, _defaultDelay);
			}
			else
			{
				_chatTask.cancel(false);
			}
		}
		
		private class AutoAnnouncementRunner implements Runnable
		{
			protected int id;
			
			protected AutoAnnouncementRunner(int pId)
			{
				id = pId;
			}
			
			@Override
			public synchronized void run()
			{
				AutoAnnouncementInstance announcementInst = _registeredAnnouncements.get(id);
				
				String text;
				
				text = announcementInst.getDefaultTexts();
				
				if (text == null)
					return;
				
				Announcements.getInstance().announceToAll(text);
			}
		}
	}
}