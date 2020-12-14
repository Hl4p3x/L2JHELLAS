package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.l2jhellas.gameserver.datatables.xml.AdminData;
import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class AdminDonator implements IAdminCommandHandler
{
	protected static final Logger _log = Logger.getLogger(AdminDonator.class.getName());
	
	private static String[] _adminCommands =
	{
		"admin_setdonator"
	};

	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_setdonator"))
		{
			final L2PcInstance  player = activeChar.getTarget() != null ? activeChar.getTarget().getActingPlayer() : activeChar;
			if (player == null || !player.isbOnline())
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return false;
			}

			if (player.isDonator())
			{
				player.setDonator(false);
				player.updateNameTitleColor();
				player.sendMessage("You are no longer a server donator.");
				AdminData.getInstance().broadcastMessageToGMs("GM " + activeChar.getName() + " removed donator stat of player" + player.getName());
				try (Connection con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement = con.prepareStatement("UPDATE characters SET donator=0 WHERE obj_Id=?"))
				{
					statement.setInt(1, player.getObjectId());
					statement.execute();
				}
				catch (SQLException e)
				{
					_log.warning(AdminDonator.class.getName() + ": could not set donator stats of char: " + e);
				}
			}
			else
			{
				player.setDonator(true);
				player.updateNameTitleColor();
				player.sendMessage("You are now a server donator, congratulations!");
				AdminData.getInstance().broadcastMessageToGMs("GM " + activeChar.getName() + " has given donator stat for player " + player.getName() + ".");
				try (Connection con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement statement = con.prepareStatement("UPDATE characters SET donator=1 WHERE obj_Id=?"))
				{

					statement.setInt(1, player.getObjectId());
					statement.execute();

				}
				catch (SQLException e)
				{
					_log.warning(AdminDonator.class.getName() + ": could not set donator stats of char: " + e);
				}
			}
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