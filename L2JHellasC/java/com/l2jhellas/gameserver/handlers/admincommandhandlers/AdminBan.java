package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.StringTokenizer;

import Extensions.IpCatcher;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.LoginServerThread;
import com.l2jhellas.gameserver.communitybbs.Manager.RegionBBSManager;
import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ServerClose;
import com.l2jhellas.util.database.L2DatabaseFactory;

public class AdminBan implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_ban", // returns ban commands
		"admin_ban_acc",
		"admin_ban_char",
		"admin_unban", // returns unban commands
		"admin_unban_acc",
		"admin_unban_char",
		"admin_jail",
		"admin_unjail",
		"admin_permaban",
		"admin_removeperma"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(command);
		st.nextToken();
		String player = "";
		int duration = -1;
		L2PcInstance targetPlayer = null;
		
		if (st.hasMoreTokens())
		{
			player = st.nextToken();
			targetPlayer = L2World.getInstance().getPlayer(player);
			
			if (st.hasMoreTokens())
			{
				try
				{
					duration = Integer.parseInt(st.nextToken());
				}
				catch (NumberFormatException nfe)
				{
					activeChar.sendMessage("Invalid number format used: " + nfe);
					return false;
				}
			}
		}
		else
		{
			if (activeChar.getTarget() != null && activeChar.getTarget() instanceof L2PcInstance)
				targetPlayer = (L2PcInstance) activeChar.getTarget();
		}
		
		if (targetPlayer != null && targetPlayer.equals(activeChar))
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_USE_ON_YOURSELF);
			return false;
		}
		
		if (command.startsWith("admin_ban ") || command.equalsIgnoreCase("admin_ban"))
		{
			activeChar.sendMessage("Available ban commands: //ban_acc, //ban_char, //ban_chat");
			return false;
		}
		else if (command.startsWith("admin_ban_acc"))
		{
			// May need to check usage in admin_ban_menu as well.
			
			if (targetPlayer == null && player.isEmpty())
			{
				activeChar.sendMessage("Usage: //ban_acc <account_name> (if none, target char's account gets banned)");
				return false;
			}
			else if (targetPlayer == null)
			{
				LoginServerThread.getInstance().sendAccessLevel(player, -1);
				activeChar.sendMessage("Ban request sent for account " + player);
			}
			else
			{
				targetPlayer.setPunishLevel(L2PcInstance.PunishLevel.ACC, 0);
				activeChar.sendMessage("Account " + targetPlayer.getAccountName() + " banned.");
			}
		}
		else if (command.startsWith("admin_ban_char"))
		{
			if (targetPlayer == null && player.isEmpty())
			{
				activeChar.sendMessage("Usage: //ban_char <char_name> (if none, target char is banned)");
				return false;
			}
			return changeCharAccessLevel(targetPlayer, player, activeChar, -1);
		}
		else if (command.startsWith("admin_unban ") || command.equalsIgnoreCase("admin_unban"))
		{
			activeChar.sendMessage("Available unban commands: //unban_acc, //unban_char, //unban_chat");
			return false;
		}
		else if (command.startsWith("admin_unban_acc"))
		{
			// Need to check admin_unban_menu command as well in AdminMenu.java handler.
			
			if (targetPlayer != null)
			{
				activeChar.sendMessage(targetPlayer.getName() + " is currently online so must not be banned.");
				return false;
			}
			else if (!player.equals(""))
			{
				LoginServerThread.getInstance().sendAccessLevel(player, 0);
				activeChar.sendMessage("Unban request sent for account " + player);
			}
			else
			{
				activeChar.sendMessage("Usage: //unban_acc <account_name>");
				return false;
			}
		}
		else if (command.startsWith("admin_unban_char"))
		{
			if (targetPlayer == null && player.isEmpty())
			{
				activeChar.sendMessage("Usage: //unban_char <char_name>");
				return false;
			}
			else if (targetPlayer != null)
			{
				activeChar.sendMessage(targetPlayer.getName() + " is currently online so must not be banned.");
				return false;
			}
			else
				return changeCharAccessLevel(null, player, activeChar, 0);
		}
		else if (command.startsWith("admin_jail"))
		{
			if (targetPlayer == null && player.isEmpty())
			{
				activeChar.sendMessage("Usage: //jail <charname> [penalty_minutes] (if no name is given, selected target is jailed indefinitely)");
				return false;
			}
			if (targetPlayer != null)
			{
				targetPlayer.setPunishLevel(L2PcInstance.PunishLevel.JAIL, duration);
				
				activeChar.sendMessage("Character " + targetPlayer.getName() + " jailed for " + (duration > 0 ? duration + " minutes." : "ever!"));
				targetPlayer.setInJail(true, duration);
				
				if (targetPlayer.getParty() != null)
					targetPlayer.getParty().removePartyMember(targetPlayer);
			}
			else
				jailOfflinePlayer(activeChar, player, duration);
		}
		else if (command.startsWith("admin_unjail"))
		{
			if (targetPlayer == null && player.isEmpty())
			{
				activeChar.sendMessage("Usage: //unjail <charname> (If no name is given target is used)");
				return false;
			}
			else if (targetPlayer != null)
			{
				targetPlayer.setInJail(false);
				targetPlayer.setPunishLevel(L2PcInstance.PunishLevel.NONE, 0);
				activeChar.sendMessage("Character " + targetPlayer.getName() + " removed from jail");
			}
			else
				unjailOfflinePlayer(activeChar, player);
		}
		else if (command.startsWith("admin_permaban"))
		{
			if (targetPlayer == null && player.isEmpty())
			{
				activeChar.sendMessage("Usage: //permaban <char_name> (if none, target char is banned)");
				return false;
			}
			final IpCatcher ipc = new IpCatcher();
			if (targetPlayer != null && !targetPlayer.isGM())
			{
				ipc.addIp(targetPlayer);
				activeChar.sendMessage(targetPlayer.getName() + " banned permanently");
				targetPlayer.sendMessage("You are banned permanently from " + activeChar.getName() + "!");
				targetPlayer.sendMessage("if you will log out you won't be able to log in again!server gave you a opportunity to stay and ask for forgiveness!");
				// targetPlayer.logout();
			}
		}
		else if (command.startsWith("admin_removeperma"))
		{
			if (targetPlayer == null && player.isEmpty())
			{
				activeChar.sendMessage("Usage: //removeperma <char_name> (if none, target char is unbanned)");
				return false;
			}
			final IpCatcher ipc = new IpCatcher();
			if (targetPlayer != null)
			{
				ipc.removeIp(targetPlayer);
				activeChar.sendMessage(targetPlayer.getName() + " permanently ban has been removed!");
				targetPlayer.sendMessage("Your permanently ban has been removed from " + activeChar.getName() + "!");
			}
		}
		return true;
	}
	
	private static void jailOfflinePlayer(L2PcInstance activeChar, String name, int delay)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=?, in_jail=?, jail_timer=? WHERE char_name=?"))
		{
			statement.setInt(1, -114356);
			statement.setInt(2, -249645);
			statement.setInt(3, -2984);
			statement.setInt(4, L2PcInstance.PunishLevel.JAIL.value());
			statement.setLong(5, (delay > 0 ? delay * 60000L : 0));
			statement.setString(6, name);
			
			statement.execute();
			int count = statement.getUpdateCount();
			
			if (count == 0)
				activeChar.sendMessage("Character not found!");
			else
				activeChar.sendMessage("Character " + name + " jailed for " + (delay > 0 ? delay + " minutes." : "ever!"));
		}
		catch (SQLException e)
		{
			activeChar.sendMessage("SQLException while jailing player");
			if (Config.DEBUG)
				e.printStackTrace();
		}
	}
	
	private static void unjailOfflinePlayer(L2PcInstance activeChar, String name)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=?, y=?, z=?, in_jail=?, jail_timer=? WHERE char_name=?"))
		{
			statement.setInt(1, 17836);
			statement.setInt(2, 170178);
			statement.setInt(3, -3507);
			statement.setInt(4, 0);
			statement.setLong(5, 0);
			statement.setString(6, name);
			statement.execute();
			int count = statement.getUpdateCount();
			if (count == 0)
				activeChar.sendMessage("Character not found!");
			else
				activeChar.sendMessage("Character " + name + " removed from jail");
		}
		catch (SQLException se)
		{
			activeChar.sendMessage("SQLException while jailing player");
			if (Config.DEBUG)
				se.printStackTrace();
		}
	}
	
	private static boolean changeCharAccessLevel(L2PcInstance targetPlayer, String player, L2PcInstance activeChar, int lvl)
	{
		boolean output = false;
		
		if (targetPlayer != null)
		{
			targetPlayer.setAccessLevel(lvl);
			targetPlayer.sendMessage("Your character has been banned. Contact the administrator for more informations.");
			
			try
			{
				// Save player status
				targetPlayer.store();
				
				// Player Disconnect like L2OFF, no client crash.
				if (targetPlayer.getClient() != null)
				{
					targetPlayer.getClient().sendPacket(ServerClose.STATIC_PACKET);
					targetPlayer.getClient().setActiveChar(null);
					targetPlayer.setClient(null);
				}
			}
			catch (Throwable t)
			{
				if (Config.DEVELOPER)
					t.printStackTrace();
			}
			
			targetPlayer.setOnlineStatus(false);
			targetPlayer.deleteMe();
			
			RegionBBSManager.getInstance().changeCommunityBoard();
			activeChar.sendMessage("The character " + targetPlayer.getName() + " has now been banned.");
			
			output = true;
		}
		else
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("UPDATE characters SET accesslevel=? WHERE char_name=?"))
			{
				statement.setInt(1, lvl);
				statement.setString(2, player);
				statement.execute();
				int count = statement.getUpdateCount();
				if (count == 0)
				{
					activeChar.sendMessage("Character not found or access level unaltered.");
				}
				else
				{
					activeChar.sendMessage(player + " now has an access level of " + lvl);
					output = true;
				}
			}
			catch (SQLException se)
			{
				activeChar.sendMessage("SQLException while changing character's access level");
				if (Config.DEBUG)
					se.printStackTrace();
			}
		}
		return output;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}