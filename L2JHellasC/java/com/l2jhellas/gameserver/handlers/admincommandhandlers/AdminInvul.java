package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public class AdminInvul implements IAdminCommandHandler
{
	private static Logger _log = Logger.getLogger(AdminInvul.class.getName());
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_invul",
		"admin_setinvul"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.equals("admin_invul"))
		{
			handleInvul(activeChar);
			AdminHelpPage.showHelpPage(activeChar, "main_menu.htm");
		}
		if (command.equals("admin_setinvul"))
		{
			L2Object target = activeChar.getTarget();
			if (target instanceof L2PcInstance)
			{
				handleInvul((L2PcInstance) target);
			}
		}
		return true;
	}
	
	private static void handleInvul(L2PcInstance activeChar)
	{
		String text;
		if (activeChar.isInvul())
		{
			activeChar.setIsInvul(false);
			text = activeChar.getName() + " is now mortal";
			if (Config.DEBUG)
				_log.config(AdminInvul.class.getName() + ": Gm removed invul mode from character " + activeChar.getName() + "(" + activeChar.getObjectId() + ")");
		}
		else
		{
			activeChar.setIsInvul(true);
			text = activeChar.getName() + " is now invulnerable";
			if (Config.DEBUG)
				_log.config(AdminInvul.class.getName() + ": Gm activated invul mode for character " + activeChar.getName() + "(" + activeChar.getObjectId() + ")");
		}
		activeChar.sendMessage(text);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}