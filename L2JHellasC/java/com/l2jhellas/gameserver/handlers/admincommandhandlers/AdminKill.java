package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import java.util.StringTokenizer;

import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.util.StringUtil;

public class AdminKill implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {"admin_kill", "admin_kill_monster"};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_kill"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken(); // skip command

			if (!st.hasMoreTokens())
			{
				final L2Object obj = activeChar.getTarget();
				if (!(obj instanceof L2Character))
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				else
					kill(activeChar, (L2Character) obj);

				return true;
			}

			String firstParam = st.nextToken();
			L2PcInstance player = L2World.getInstance().getPlayer(firstParam);
			if (player != null)
			{
				if (st.hasMoreTokens())
				{
					String secondParam = st.nextToken();
					if (StringUtil.isDigit(secondParam))
					{
						int radius = Integer.parseInt(secondParam);

						L2World.getInstance().forEachVisibleObjectInRange(player, L2Character.class, radius, knownChar ->
						{
							if (knownChar.equals(activeChar))
								return;

							kill(activeChar, knownChar);
						});
						activeChar.sendMessage("Killed all characters within a "+ radius + " unit radius around "+ player.getName() + ".");
					}
					else
						activeChar.sendMessage("Invalid radius.");
				}
				else
					kill(activeChar, player);
			}
			else if (StringUtil.isDigit(firstParam))
			{
				int radius = Integer.parseInt(firstParam);

				L2World.getInstance().forEachVisibleObjectInRange(activeChar,L2Character.class, radius, knownChar ->
				{
					kill(activeChar, knownChar);
				});

				activeChar.sendMessage("Killed all characters within a "+ radius + " unit radius.");
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
			target.reduceCurrentHp(target.getMaxHp() + target.getMaxCp() + 1,activeChar);
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