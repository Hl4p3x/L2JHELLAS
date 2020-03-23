package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import java.util.logging.Logger;

import com.l2jhellas.gameserver.datatables.xml.AdminData;
import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public class AdminNoble implements IAdminCommandHandler
{
	protected static final Logger _log = Logger.getLogger(AdminNoble.class.getName());
	
	private static String[] _adminCommands =
	{
		"admin_setnoble"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_setnoble"))
		{
			final L2PcInstance  player = activeChar.getTarget() != null ? activeChar.getTarget().getActingPlayer() : activeChar;

			if (player == null)
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
				return false;
			}
			
			if (player.isNoble())
			{
				player.setNoble(false);
				player.sendMessage("You are no longer a server noble.");
				AdminData.getInstance().broadcastMessageToGMs("GM " + activeChar.getName() + " removed noble stat of player" + player.getName());
			}
			else
			{
				player.setNoble(true);
				player.sendMessage("You are now a server noble, congratulations!");
				AdminData.getInstance().broadcastMessageToGMs("GM " + activeChar.getName() + " has given noble stat for player " + player.getName() + ".");
			}
			
			player.store();
			player.broadcastUserInfo();
		}
		return false;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return _adminCommands;
	}
}