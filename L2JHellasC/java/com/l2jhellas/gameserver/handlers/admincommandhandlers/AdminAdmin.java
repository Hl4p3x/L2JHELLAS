package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import java.util.StringTokenizer;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.communitybbs.Manager.RegionBBSManager;
import com.l2jhellas.gameserver.datatables.xml.AdminData;
import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.entity.olympiad.Olympiad;

public class AdminAdmin implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_admin",
		"admin_admin1",
		"admin_admin2",
		"admin_admin3",
		"admin_admin4",
		"admin_admin5",
		"admin_gmliston",
		"admin_gmlistoff",
		"admin_silence",
		"admin_invis",
		"admin_vis",
		"admin_inv_menu",
		"admin_diet",
		"admin_tradeoff",
		"admin_set",
		"admin_set_menu",
		"admin_set_mod",
		"admin_saveolymp",
		"admin_manualhero"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_admin"))
			showMainPage(activeChar, command);	
		else if (command.startsWith("admin_gmliston"))
		{
			AdminData.getInstance().showGm(activeChar);
			activeChar.sendMessage("Registerd into gm list");
			AdminHelpPage.showHelpPage(activeChar, "main_menu.htm");
		}
		else if (command.startsWith("admin_gmlistoff"))
		{
			AdminData.getInstance().hideGm(activeChar);
			activeChar.sendMessage("Removed from gm list");
			AdminHelpPage.showHelpPage(activeChar, "main_menu.htm");
		}
		else if (command.equals("admin_inv_menu"))
		{
			if (!activeChar.getAppearance().getInvisible())
			{
				activeChar.getAppearance().setInvisible();
				activeChar.broadcastUserInfo();
				activeChar.decayMe();
				activeChar.spawnMe();
			}
			else
			{
				activeChar.getAppearance().setVisible();
				activeChar.broadcastUserInfo();
			}
			RegionBBSManager.getInstance().changeCommunityBoard();
			AdminHelpPage.showHelpPage(activeChar, "main_menu.htm");
		}
		else if (command.startsWith("admin_invis"))
		{
			activeChar.getAppearance().setInvisible();
			activeChar.broadcastUserInfo();
			activeChar.decayMe();
			activeChar.spawnMe();
			RegionBBSManager.getInstance().changeCommunityBoard();
			AdminHelpPage.showHelpPage(activeChar, "main_menu.htm");
		}
		else if (command.startsWith("admin_vis"))
		{
			activeChar.getAppearance().setVisible();
			activeChar.broadcastUserInfo();
			RegionBBSManager.getInstance().changeCommunityBoard();
			AdminHelpPage.showHelpPage(activeChar, "main_menu.htm");
		}
		else if (command.startsWith("admin_silence"))
		{
			activeChar.setMessageRefusal(activeChar.getMessageRefusal() ? false : true);
			AdminHelpPage.showHelpPage(activeChar, "main_menu.htm");
		}
		else if (command.startsWith("admin_saveolymp"))
		{
			try
			{
				Olympiad.getInstance().saveOlympiadStatus();
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Typed wrong command!");
			}
			activeChar.sendMessage("Olympiad data is saved!");
			AdminHelpPage.showHelpPage(activeChar, "game_menu.htm");
		}
		else if (command.startsWith("admin_manualhero"))
		{
			try
			{
				Olympiad.getInstance().manualSelectHeroes();
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Typed wrong command!");
			}
			activeChar.sendMessage("Heroes are formed");
			AdminHelpPage.showHelpPage(activeChar, "game_menu.htm");
		}
		else if (command.startsWith("admin_diet"))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command);
				st.nextToken();
				if (st.nextToken().equalsIgnoreCase("on"))
				{
					activeChar.setDietMode(true);
					activeChar.sendMessage("Diet mode ON");
				}
				else if (st.nextToken().equalsIgnoreCase("off"))
				{
					activeChar.setDietMode(false);
					activeChar.sendMessage("Diet mode OFF");
				}
			}
			catch (Exception ex)
			{
				if (activeChar.getDietMode())
				{
					activeChar.setDietMode(false);
					activeChar.sendMessage("Diet mode OFF");
				}
				else
				{
					activeChar.setDietMode(true);
					activeChar.sendMessage("Diet mode ON");
				}
			}
			finally
			{
				activeChar.refreshOverloaded();
			}
			AdminHelpPage.showHelpPage(activeChar, "main_menu.htm");
		}
		else if (command.startsWith("admin_tradeoff"))
		{
			try
			{
				String mode = command.substring(15);
				activeChar.setTradeRefusal(mode.equalsIgnoreCase("on") ? true : false);
			}
			catch (Exception ex)
			{
				activeChar.setTradeRefusal(activeChar.getTradeRefusal() ? false : true);
			}
			AdminHelpPage.showHelpPage(activeChar, "main_menu.htm");
		}
		else if (command.startsWith("admin_set"))
		{
			StringTokenizer st = new StringTokenizer(command);
			String[] cmd = st.nextToken().split("_");
			try
			{
				String[] parameter = st.nextToken().split("=");
				String pName = parameter[0].trim();
				String pValue = parameter[1].trim();
				
				if (Config.setParameterValue(pName, pValue))
					activeChar.sendMessage("parameter " + pName + " succesfully set to " + pValue);
				else
					activeChar.sendMessage("Invalid parameter!");
			}
			catch (Exception e)
			{
				if (cmd.length == 2)
					activeChar.sendMessage("Usage: //set parameter=vaue");
			}
			finally
			{
				if (cmd.length == 3)
				{
					if (cmd[2].equalsIgnoreCase("menu"))
						AdminHelpPage.showHelpPage(activeChar, "settings.htm");
					else if (cmd[2].equalsIgnoreCase("mod"))
						AdminHelpPage.showHelpPage(activeChar, "mods_menu.htm");
				}
			}
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private static void showMainPage(L2PcInstance activeChar, String command)
	{
		int mode = 0;
		String filename = null;
		try
		{
			mode = Integer.parseInt(command.substring(11));
		}
		catch (Exception e)
		{
		}
		switch (mode)
		{
			case 1:
				filename = "main";
				break;
			case 2:
				filename = "game";
				break;
			case 3:
				filename = "effects";
				break;
			case 4:
				filename = "server";
				break;
			case 5:
				filename = "mods";
				break;
			default:
				filename = "main";
				break;
		}
		AdminHelpPage.showHelpPage(activeChar, filename + "_menu.htm");
	}
}