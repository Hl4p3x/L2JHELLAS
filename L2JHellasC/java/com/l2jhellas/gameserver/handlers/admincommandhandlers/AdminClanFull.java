package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.EtcStatusUpdate;
import com.l2jhellas.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import com.l2jhellas.gameserver.network.serverpackets.SystemMessage;

public class AdminClanFull implements IAdminCommandHandler
{
	private static final String ADMIN_COMMANDS[] =
	{
		"admin_clanfull"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_clanfull"))
		{
			final L2PcInstance player = activeChar.getTarget().getActingPlayer();
			
			if (player == null)
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
				return false;
			}
					
			if (!player.isClanLeader())
			{
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_NOT_A_CLAN_LEADER).addCharName(player));
				return false;
			}
			
			if (player.getClan().getLevel() == Config.CLAN_LEVEL)
			{
				activeChar.sendMessage(player.getName() + " clan is already " + Config.CLAN_LEVEL + " level.");
				return false;
			}
			
			player.getClan().changeLevel(Config.CLAN_LEVEL);
			player.ClanSkills();
			player.sendPacket(new EtcStatusUpdate(activeChar));
			player.sendPacket(new PledgeShowInfoUpdate(player.getClan()));			
			player.getClan().updateClanInDB();
			player.store();
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}