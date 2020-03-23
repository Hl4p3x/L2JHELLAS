package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.datatables.xml.DoorData;
import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.instancemanager.CastleManager;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.entity.Castle;

public class AdminDoorControl implements IAdminCommandHandler
{
	private static DoorData _doorTable;
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_open",
		"admin_close",
		"admin_openall",
		"admin_closeall"
	};
	
	// private static final Map<String, Integer> doorMap = new FastMap<String, Integer>(); //FIXME: should we jute remove this?
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		_doorTable = DoorData.getInstance();
		
		try
		{
			if (command.startsWith("admin_open "))
			{
				int doorId = Integer.parseInt(command.substring(11));
				if (_doorTable.getDoor(doorId) != null)
					_doorTable.getDoor(doorId).openMe();
				else
				{
					for (Castle castle : CastleManager.getInstance().getCastles())
						if (castle.getDoor(doorId) != null)
							castle.getDoor(doorId).openMe();
				}
			}
			else if (command.startsWith("admin_close "))
			{
				int doorId = Integer.parseInt(command.substring(12));
				if (_doorTable.getDoor(doorId) != null)
					_doorTable.getDoor(doorId).closeMe();
				else
				{
					for (Castle castle : CastleManager.getInstance().getCastles())
						if (castle.getDoor(doorId) != null)
							castle.getDoor(doorId).closeMe();
				}
			}
			if (command.equals("admin_closeall"))
			{
				for (L2DoorInstance door : _doorTable.getDoors())
					door.closeMe();
				for (Castle castle : CastleManager.getInstance().getCastles())
					for (L2DoorInstance door : castle.getDoors())
						door.closeMe();
				AdminHelpPage.showHelpPage(activeChar, "server_menu.htm");
			}
			if (command.equals("admin_openall"))
			{
				for (L2DoorInstance door : _doorTable.getDoors())
					door.openMe();
				for (Castle castle : CastleManager.getInstance().getCastles())
					for (L2DoorInstance door : castle.getDoors())
						door.openMe();
				AdminHelpPage.showHelpPage(activeChar, "server_menu.htm");
			}
			if (command.equals("admin_open"))
			{
				L2Object target = activeChar.getTarget();
				if (target instanceof L2DoorInstance)
					((L2DoorInstance) target).openMe();
				else
					activeChar.sendMessage("Incorrect target.");
			}
			
			if (command.equals("admin_close"))
			{
				L2Object target = activeChar.getTarget();
				if (target instanceof L2DoorInstance)
					((L2DoorInstance) target).closeMe();
				else
					activeChar.sendMessage("Incorrect target.");
			}
		}
		catch (Exception e)
		{
			if (Config.DEVELOPER)
				e.printStackTrace();
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}