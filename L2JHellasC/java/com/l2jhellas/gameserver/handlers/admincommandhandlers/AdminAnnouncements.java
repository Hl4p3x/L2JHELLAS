package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import com.l2jhellas.gameserver.Announcements;
import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class AdminAnnouncements implements IAdminCommandHandler
{
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_list_announcements",
		"admin_reload_announcements",
		"admin_announce_announcements",
		"admin_add_announcement",
		"admin_del_announcement",
		"admin_announce",
		"admin_announce_menu"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.equals("admin_list_announcements"))
			Announcements.getInstance().listAnnouncements(activeChar);
		else if (command.equals("admin_reload_announcements"))
		{
			Announcements.getInstance().loadAnnouncements();
			Announcements.getInstance().listAnnouncements(activeChar);
		}
		else if (command.startsWith("admin_announce_menu"))
		{
			Announcements sys = new Announcements();
			sys.handleAnnounce(command, 20);
			Announcements.getInstance().listAnnouncements(activeChar);
		}
		else if (command.equals("admin_announce_announcements"))
		{
			for (L2PcInstance player : L2World.getInstance().getAllPlayers().values())
			{
				Announcements.getInstance().showAnnouncements(player);
			}
			Announcements.getInstance().listAnnouncements(activeChar);
		}
		else if (command.startsWith("admin_add_announcement"))
		{
			if (!command.equals("admin_add_announcement"))
			{
				try
				{
					String val = command.substring(23);
					
					if (!Announcements.getInstance().addAnnouncement(val))
						activeChar.sendMessage("Invalid //announce message content,can't be empty.");
					
					Announcements.getInstance().listAnnouncements(activeChar);
				}
				catch (StringIndexOutOfBoundsException e)
				{
				}
			}
		}
		else if (command.startsWith("admin_del_announcement"))
		{
			try
			{
				int val = new Integer(command.substring(23)).intValue();
				Announcements.getInstance().delAnnouncement(val);
				Announcements.getInstance().listAnnouncements(activeChar);
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.startsWith("admin_announce"))
			Announcements.getInstance().handleAnnounce(command, 15);
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}