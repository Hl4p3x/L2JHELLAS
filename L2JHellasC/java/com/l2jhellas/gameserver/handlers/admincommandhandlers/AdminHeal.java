package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import java.util.StringTokenizer;

import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.L2Character;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.util.StringUtil;

public class AdminHeal implements IAdminCommandHandler
{	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_heal"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{	
		if (command.startsWith("admin_heal"))
		{
			L2Object object = activeChar.getTarget();		
			final StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();	
			
			if (st.hasMoreTokens())
			{
				final String nameOrRadius = st.nextToken();				
				final L2PcInstance target = L2World.getInstance().getPlayer(nameOrRadius);
				
				if (target != null)
					object = target;
				else if (StringUtil.isDigit(nameOrRadius))
				{
					final int radius = Integer.parseInt(nameOrRadius);
					
					L2World.getInstance().forEachVisibleObjectInRange(activeChar, L2Character.class,radius, character ->
					{
						if (!character.isDead())
						{
							character.setCurrentHpMp(character.getMaxHp(),character.getMaxMp());

							if (character.isPlayer())
								character.setCurrentCp(character.getMaxCp());
						}
					});
					
					activeChar.sendMessage("You instant healed all characters within " + radius + " unit radius.");
					return true;
				}
			}
			
			if (object == null)
				object = activeChar;
			
			if (object instanceof L2Character)
			{
				final L2Character character = (L2Character) object;
				
				if (!character.isDead())
				{
					character.setCurrentHpMp(character.getMaxHp(),character.getMaxMp());

					if (character.isPlayer())
						character.setCurrentCp(character.getMaxCp());

					activeChar.sendMessage("You instant healed " + character.getName() + ".");
				}
			}
			else
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}