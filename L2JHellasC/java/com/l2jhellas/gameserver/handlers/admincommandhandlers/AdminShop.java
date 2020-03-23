package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import java.util.logging.Logger;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.controllers.TradeController;
import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.model.L2TradeList;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.BuyList;

public class AdminShop implements IAdminCommandHandler
{
	private static Logger _log = Logger.getLogger(AdminShop.class.getName());
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_buy",
		"admin_gmshop"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_buy"))
		{
			try
			{
				handleBuyRequest(activeChar, command.substring(10));
			}
			catch (IndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Please specify buylist.");
			}
		}
		else if (command.equals("admin_gmshop"))
		{
			AdminHelpPage.showHelpPage(activeChar, "gmshops.htm");
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private static void handleBuyRequest(L2PcInstance activeChar, String command)
	{
		int val = -1;
		try
		{
			val = Integer.parseInt(command);
		}
		catch (Exception e)
		{
			_log.warning(AdminShop.class.getName() + ": admin buylist failed:" + command);
			if (Config.DEVELOPER)
			{
				e.printStackTrace();
			}
		}
		
		L2TradeList list = TradeController.getInstance().getBuyList(val);
		
		if (list != null)
		{
			activeChar.sendPacket(new BuyList(list, activeChar.getAdena()));
			if (Config.DEBUG)
				_log.fine("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") opened GM shop id " + val);
		}
		else
		{
			_log.warning(AdminShop.class.getName() + ": no buylist with id:" + val);
		}
		activeChar.sendPacket(ActionFailed.STATIC_PACKET);
	}
}