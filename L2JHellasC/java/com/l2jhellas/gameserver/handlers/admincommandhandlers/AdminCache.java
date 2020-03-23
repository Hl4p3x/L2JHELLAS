package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import com.l2jhellas.gameserver.cache.CrestCache;
import com.l2jhellas.gameserver.cache.HtmCache;
import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class AdminCache implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_reload_cache_path",
		"admin_reload_cache_file",
		"admin_fix_cache_crest"
	};
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_reload_cache_path "))
		{
			try
			{
				final String path = command.split(" ")[1];
				HtmCache.getInstance().reloadPath(path);
				activeChar.sendMessage("HTM paths' cache have been reloaded.");
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //reload_cache_path <path>");
			}
		}
		else if (command.startsWith("admin_reload_cache_file "))
		{
			try
			{
				String path = command.split(" ")[1];
				if (HtmCache.getInstance().isLoadable(path))
					activeChar.sendMessage("Cache[HTML]: requested file was loaded.");
				else
					activeChar.sendMessage("Cache[HTML]: requested file couldn't be loaded.");
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //reload_cache_file <relative_path/file>");
			}
		}
		else if (command.startsWith("admin_fix_cache_crest"))
		{
			CrestCache.convertOldPledgeFiles();
			activeChar.sendMessage("Cache[Crest]: crests have been fixed.");
		}
		return true;
	}
}