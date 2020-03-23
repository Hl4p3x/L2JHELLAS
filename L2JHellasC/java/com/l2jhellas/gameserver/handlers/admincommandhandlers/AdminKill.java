package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import java.util.StringTokenizer;

import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2ControllableMobInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;

public class AdminKill implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_kill",
		"admin_kill_monster"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_kill"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken(); // skip command
			
			if (st.hasMoreTokens())
			{
				String firstParam = st.nextToken();
				L2PcInstance plyr = L2World.getInstance().getPlayer(firstParam);
				if (plyr != null)
				{
					if (st.hasMoreTokens())
					{
						try
						{
							int radius = Integer.parseInt(st.nextToken());
							for (L2Character knownChar : L2World.getInstance().getVisibleObjects(activeChar, L2Character.class, radius))
							{
								if ((knownChar == null) || (knownChar instanceof L2ControllableMobInstance) || knownChar.equals(activeChar))
									continue;
								
								kill(activeChar, knownChar);
							}
							
							activeChar.sendMessage("Killed all characters within a " + radius + " unit radius.");
							return true;
						}
						catch (NumberFormatException e)
						{
							activeChar.sendMessage("Invalid radius.");
							return false;
						}
					}
					kill(activeChar, plyr);
				}
				else
				{
					try
					{
						int radius = Integer.parseInt(firstParam);
						for (L2Character knownChar : L2World.getInstance().getVisibleObjects(activeChar, L2Character.class, radius))
						{
							if ((knownChar == null) || (knownChar instanceof L2ControllableMobInstance) || knownChar.equals(activeChar))
							{
								continue;
							}
							kill(activeChar, knownChar);
						}
						
						activeChar.sendMessage("Killed all characters within a " + radius + " unit radius.");
						return true;
					}
					catch (NumberFormatException e)
					{
						activeChar.sendMessage("Usage: //kill <player_name | radius>");
						return false;
					}
				}
			}
			else
			{
				L2Object obj = activeChar.getTarget();
				if ((obj == null) || (obj instanceof L2ControllableMobInstance) || !(obj instanceof L2Character))
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				else
					kill(activeChar, (L2Character) obj);
			}
		}
		return true;
	}
	
	private static void kill(L2PcInstance activeChar, L2Character target)
	{
		if (target instanceof L2PcInstance)
		{
			if (!((L2PcInstance) target).isGM())
				target.stopAllEffects(); // e.g. invincibility effect
			target.reduceCurrentHp(target.getMaxHp() + target.getMaxCp() + 1, activeChar);
		}
		else if (target instanceof L2MonsterInstance)
			target.reduceCurrentHp(target.getMaxHp() + 1, activeChar);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}