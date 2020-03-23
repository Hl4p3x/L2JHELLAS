package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import java.util.logging.Logger;

import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public class AdminUnblockIp implements IAdminCommandHandler
{
	private static final Logger _log = Logger.getLogger(IAdminCommandHandler.class.getName());
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_unblockip"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_unblockip "))
		{
			try
			{
				String ipAddress = command.substring(16);
				if (unblockIp(ipAddress, activeChar))
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_S2);
					sm.addString("Removed IP " + ipAddress + " from blocklist!");
					activeChar.sendPacket(sm);
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				// Send syntax to the user
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_S2);
				sm.addString("Usage mode: //unblockip <ip>");
				activeChar.sendPacket(sm);
			}
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private static boolean unblockIp(String ipAddress, L2PcInstance activeChar)
	{
		_log.warning(AdminUnblockIp.class.getSimpleName() + ": Banned IP:" + ipAddress + " removed by GM " + activeChar.getName());
		return true;
	}
}