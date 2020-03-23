package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import java.util.StringTokenizer;

import com.l2jhellas.gameserver.handler.AutoAnnouncementHandler;
import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class AdminAutoAnnouncements implements IAdminCommandHandler
{
	private static String[] ADMIN_COMMANDS =
	{
		"admin_list_autoannouncements",
		"admin_add_autoannouncement",
		"admin_del_autoannouncement",
		"admin_autoannounce"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance admin)
	{
		if (command.equals("admin_list_autoannouncements"))
			AutoAnnouncementHandler.getInstance().listAutoAnnouncements(admin);
		else if (command.startsWith("admin_add_autoannouncement"))
		{
			if (!command.equals("admin_add_autoannouncement"))
			{
				try
				{
					StringTokenizer st = new StringTokenizer(command.substring(27));

					int delay = Integer.parseInt(st.nextToken().trim());
					String autoAnnounce = st.nextToken();
						
					if (delay > 30)
					{
						while (st.hasMoreTokens())
						{
							autoAnnounce = autoAnnounce + " " + st.nextToken();
						}
						
						AutoAnnouncementHandler.getInstance().registerAnnouncment(autoAnnounce, delay);
						AutoAnnouncementHandler.getInstance().listAutoAnnouncements(admin);
					}
					else
						admin.sendMessage("Delay must be > 30");
						
				}
				catch (StringIndexOutOfBoundsException e)
				{
				}
			}
		}
		else if (command.startsWith("admin_del_autoannouncement"))
		{
			try
			{
				int val = new Integer(command.substring(27)).intValue();
				AutoAnnouncementHandler.getInstance().removeAnnouncement(val);
				AutoAnnouncementHandler.getInstance().listAutoAnnouncements(admin);
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.startsWith("admin_autoannounce"))
			AutoAnnouncementHandler.getInstance().listAutoAnnouncements(admin);
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}