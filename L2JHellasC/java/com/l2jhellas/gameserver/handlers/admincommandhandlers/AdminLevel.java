package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import java.util.StringTokenizer;

import com.l2jhellas.Config;
import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.base.Experience;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.UserInfo;

public class AdminLevel implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_add_level",
		"admin_set_level"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		L2Object targetChar = activeChar.getTarget();
		
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command
		
		String val = "";
		if (st.countTokens() >= 1)
		{
			val = st.nextToken();
		}
		
		if (actualCommand.equalsIgnoreCase("admin_add_level"))
		{
			try
			{
				if (targetChar instanceof L2Playable)
				{
					((L2Playable) targetChar).getStat().addLevel(Byte.parseByte(val));
					
					if (targetChar instanceof L2PcInstance)
						((L2PcInstance) targetChar).sendPacket(new UserInfo(((L2PcInstance) targetChar).getActingPlayer()));
	
				}
			}
			catch (NumberFormatException e)
			{
				activeChar.sendMessage("Wrong Number Format.");
			}
		}
		else if (actualCommand.equalsIgnoreCase("admin_set_level"))
		{
			try
			{
				if (targetChar == null || !(targetChar instanceof L2Playable))
				{
					activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
					return false;
				}
				
				final L2Playable targetPlayer = (L2Playable) targetChar;
				
				final byte lvl = Byte.parseByte(val);
				int max_level = Experience.MAX_LEVEL;
				
				if (targetChar instanceof L2PcInstance && ((L2PcInstance) targetPlayer).isSubClassActive())
				{
					max_level = Config.MAX_SUBCLASS_LEVEL;
				}
				
				if (lvl >= 1 && lvl <= max_level)
				{
					final long pXp = targetPlayer.getStat().getExp();
					final long tXp = Experience.LEVEL[lvl];
					if (pXp > tXp)
					{
						targetPlayer.getStat().removeExpAndSp(pXp - tXp, 0);
					}
					else if (pXp < tXp)
					{
						targetPlayer.getStat().addExpAndSp(tXp - pXp, 0);
					}
					
					if (targetChar instanceof L2PcInstance)
					    targetPlayer.sendPacket(new UserInfo(targetPlayer.getActingPlayer()));
				}
				else
				{
					activeChar.sendMessage("You must specify level between 1 and " + Experience.MAX_LEVEL + ".");
					return false;
				}
			}
			catch (NumberFormatException e)
			{
				activeChar.sendMessage("You must specify level between 1 and " + Experience.MAX_LEVEL + ".");
				return false;
			}
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}