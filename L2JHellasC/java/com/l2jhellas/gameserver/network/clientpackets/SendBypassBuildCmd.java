package com.l2jhellas.gameserver.network.clientpackets;

import com.l2jhellas.gameserver.ThreadPoolManager;
import com.l2jhellas.gameserver.datatables.xml.AdminData;
import com.l2jhellas.gameserver.handler.AdminCommandHandler;
import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;

public final class SendBypassBuildCmd extends L2GameClientPacket
{
	private static final String _C__5B_SENDBYPASSBUILDCMD = "[C] 5b SendBypassBuildCmd";
	public final static int GM_MESSAGE = 9;
	public final static int ANNOUNCEMENT = 10;
	
	private String _command;
	
	@Override
	protected void readImpl()
	{
		_command = readS();
		if (_command != null)
			_command = _command.trim();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
			return;
		
		final String command = "admin_" + _command.split(" ")[0];
		
		final IAdminCommandHandler ach = AdminCommandHandler.getInstance().getHandler(command);
		
		if (ach == null)
		{
			if (activeChar.isGM())
			{
				activeChar.sendMessage("The command " + command.substring(6) + " doesn't exist.");
				_log.warning(SendBypassBuildCmd.class.getName() + ": No handler registered for admin command '" + command + "'");
			}
			
			return;
		}
		if (!AdminData.getInstance().hasAccess(command, activeChar.getAccessLevel()))
		{
			activeChar.sendMessage("You don't have the access right to use this command.");
			_log.warning(SendBypassBuildCmd.class.getName() + ": " + activeChar.getName() + " tried to use admin command " + command + ", but have no access to use it.");
			return;
		}
		
		ThreadPoolManager.getInstance().executeTask(() ->
		{
			try
			{
				ach.useAdminCommand("admin_" + _command, activeChar);
			}
			catch (final RuntimeException e)
			{
			}
		});	
	}
	
	@Override
	public String getType()
	{
		return _C__5B_SENDBYPASSBUILDCMD;
	}
}