package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public class AdminTest implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_test",
		"admin_stats",
		"admin_skill_test",
		"admin_mp",
		"admin_known",
		"admin_oly_obs_mode",
		"admin_obs_mode",
		"admin_msg"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.equals("admin_stats"))
		{
			for (String line : ThreadPoolManager.getInstance().getStats())
			{
				activeChar.sendMessage(line);
			}
		}
		else if (command.startsWith("admin_skill_test"))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command);
				st.nextToken();
				
				int id = Integer.parseInt(st.nextToken());
				
				adminTestSkill(activeChar, id);
				
				st = null;
			}
			catch (NumberFormatException e)
			{
				if (Config.DEVELOPER)
				{
					e.printStackTrace();
				}
				
				activeChar.sendMessage("Command format is //skill_test <ID>");
			}
			catch (NoSuchElementException nsee)
			{
				if (Config.DEVELOPER)
				{
					nsee.printStackTrace();
				}
				
				activeChar.sendMessage("Command format is //skill_test <ID>");
			}
		}
		else if (command.equals("admin_mp on"))
		{
			// .startPacketMonitor();
			activeChar.sendMessage("command not working");
		}
		else if (command.equals("admin_mp off"))
		{
			// .stopPacketMonitor();
			activeChar.sendMessage("command not working");
		}
		else if (command.equals("admin_mp dump"))
		{
			// .dumpPacketHistory();
			activeChar.sendMessage("command not working");
		}
		else if (command.equals("admin_test"))
		{
			activeChar.sendMessage("command removed");
		}
		else if (command.startsWith("admin_oly_obs_mode"))
		{
			if (!activeChar.inObserverMode())
			{
				activeChar.enterOlympiadObserverMode(activeChar.getX(), activeChar.getY(), activeChar.getZ(), -1);
			}
			else
			{
				activeChar.leaveOlympiadObserverMode();
			}
		}
		else if (command.startsWith("admin_obs_mode"))
		{
			if (!activeChar.inObserverMode())
			{
				activeChar.enterObserverMode(activeChar.getX(), activeChar.getY(), activeChar.getZ());
			}
			else
			{
				activeChar.leaveObserverMode();
			}
		}
		// Used for testing SystemMessage IDs - Use //msg <ID>
		else if (command.startsWith("admin_msg"))
		{
			try
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(Integer.parseInt(command.substring(10).trim())));
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Command format: //msg <SYSTEM_MSG_ID>");
				return true;
			}
			AdminHelpPage.showHelpPage(activeChar, "server_menu.htm");
		}
		return true;
	}
	
	private static void adminTestSkill(L2PcInstance activeChar, int id)
	{
		L2Character player;
		L2Object target = activeChar.getTarget();
		
		if ((target == null) || !(target instanceof L2Character))
		{
			player = activeChar;
		}
		else
		{
			player = (L2Character) target;
		}
		
		player.broadcastPacket(new MagicSkillUse(activeChar, player, id, 1, 1, 1));
		
		target = null;
		player = null;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}