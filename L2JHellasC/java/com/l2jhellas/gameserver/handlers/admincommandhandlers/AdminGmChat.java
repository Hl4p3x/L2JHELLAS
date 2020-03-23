package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import com.l2jhellas.gameserver.datatables.xml.AdminData;
import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.CreatureSay;

public class AdminGmChat implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_gmchat",
		"admin_snoop",
		"admin_gmchat_menu"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_gmchat"))
			handleGmChat(command, activeChar);
		if (command.startsWith("admin_snoop"))
			snoop(command, activeChar);
		if (command.startsWith("admin_gmchat_menu"))
			AdminHelpPage.showHelpPage(activeChar, "main_menu.htm");
		return true;
	}
	
	private static void snoop(String command, L2PcInstance activeChar)
	{
		L2Object target = activeChar.getTarget();
		if (target == null)
		{
			activeChar.sendPacket(SystemMessageId.SELECT_TARGET);
			return;
		}
		if (!(target instanceof L2PcInstance))
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		L2PcInstance player = (L2PcInstance) target;
		player.addSnooper(activeChar);
		activeChar.addSnooped(player);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private static void handleGmChat(String command, L2PcInstance activeChar)
	{
		try
		{
			int offset = 0;
			String text;
			if (command.contains("menu"))
				offset = 17;
			else
				offset = 13;
			text = command.substring(offset);
			CreatureSay cs = new CreatureSay(0, 9, activeChar.getName(), text);
			AdminData.getInstance().broadcastToGMs(cs);
		}
		catch (StringIndexOutOfBoundsException e)
		{
			// empty message.. ignore
		}
	}
}