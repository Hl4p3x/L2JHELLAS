package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class AdminBanChat implements IAdminCommandHandler
{
	private static String[] ADMIN_COMMANDS =
	{
		"admin_banchat",
		"admin_unbanchat",
		"admin_unbanchat_all",
		"admin_banchat_all"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance admin)
	{
		String[] cmdParams = command.split(" ");
		
		// checking syntax
		if (cmdParams.length < 3 && command.startsWith("admin_banchat"))
		{
			admin.sendMessage("usage:");
			admin.sendMessage("  //banchat [<player_name>] [<time_in_seconds>]");
			admin.sendMessage("  //banchat [<player_name>] [<time_in_seconds>] [<ban_chat_reason>]");
			return false;
		}
		else if (cmdParams.length < 2 && command.startsWith("admin_unbanchat"))
		{
			admin.sendMessage("UnBanChat Syntax:");
			admin.sendMessage("  //unbanchat [<player_name>]");
			return false;
		}
		else if (command.startsWith("admin_banchat_all"))
		{
			try
			{
				
				L2World.getInstance().forEachVisibleObject(admin, L2PcInstance.class, player ->
				{
					if (!player.isGM())
					{
						player.setBanChatTimer(120 * 60000); // setting max 2 min
						player.setChatBannedForAnnounce(true);
					}
				});
				
			}
			catch (Exception e)
			{
			}
		}
		else if (command.startsWith("admin_unbanchat_all"))
		{
			try
			{
				L2World.getInstance().forEachVisibleObject(admin, L2PcInstance.class, player ->
				{
					player.setChatBannedForAnnounce(false);
				});
			}
			catch (Exception e)
			{
			}
		}
		// void vars
		long banLength = -1;
		String banReason = "";
		// L2Object targetObject = null;
		L2PcInstance targetPlayer = null;
		
		// chat instance
		targetPlayer = L2World.getInstance().getPlayer(cmdParams[1]);
		
		if (targetPlayer == null)
		{
			admin.sendMessage("Incorrect parameter or target.");
			return false;
		}
		
		// what is our actions?
		if (command.startsWith("admin_banchat"))
		{
			// ban chat length (seconds)
			try
			{
				banLength = Integer.parseInt(cmdParams[2]);
			}
			catch (NumberFormatException nfe)
			{
			}
			
			// ban chat reason
			if (cmdParams.length > 3)
				banReason = cmdParams[3];
			
			// apply ban chat
			admin.sendMessage(targetPlayer.getName() + "'s chat is banned for " + banLength + " seconds.");
			targetPlayer.setChatBanned(true, banLength, banReason);
		}
		else
		
		if (command.startsWith("admin_unbanchat"))
		{
			admin.sendMessage(targetPlayer.getName() + "'s chat ban has now been lifted.");
			targetPlayer.setChatBanned(false, 0, "");
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}