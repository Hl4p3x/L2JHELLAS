package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import java.util.StringTokenizer;

import com.l2jhellas.gameserver.communitybbs.Manager.RegionBBSManager;
import com.l2jhellas.gameserver.enums.player.ChatType;
import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.CreatureSay;

public class AdminHide implements IAdminCommandHandler
{	
	private static final String[] ADMIN_COMMANDS ={"admin_hide"};	
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance player)
	{
		final StringTokenizer st = new StringTokenizer(command);
		st.nextToken();
		
		try
		{
			final String param = st.nextToken();
			switch (param)
			{
				case "on":
					if (!tryToHide(player, true))
					{
						sendMessage(player, "Currently, you cannot be seen.");
						return true;
					}					
					sendMessage(player, "Now, you cannot be seen.");
					return true;				
				case "off":
					if (!tryToHide(player, false))
					{
						sendMessage(player, "Currently, you can be seen.");
						return true;
					}				
					sendMessage(player, "Now, you can be seen.");
					return true;				
				default:
					sendMessage(player, "//hide [on|off]");
					return true;
			}
		}
		catch (final Exception e)
		{
			sendMessage(player, "//hide [on|off]");
			return true;
		}
	}
		
	public static boolean tryToHide(L2PcInstance player, boolean hide)
	{
		if (!player.getAppearance().isVisible() && hide || player.getAppearance().isVisible() && !hide)
			return false;
	
		if(hide)
		{		   
			player.getAppearance().setIsVisible(false);
			player.broadcastUserInfo();
			player.decayMe();
			player.spawnMe();
			RegionBBSManager.getInstance().changeCommunityBoard();
		}
		else
		{		   
			player.getAppearance().setIsVisible(true);
			player.broadcastUserInfo();
			RegionBBSManager.getInstance().changeCommunityBoard();
		}
		
		return true;
	}
	
	public static void sendMessage(L2PcInstance player, String message)
	{
		player.sendPacket(new CreatureSay(0, ChatType.GENERAL, "SYS", message));
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}