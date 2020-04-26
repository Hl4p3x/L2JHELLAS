package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import java.util.Collection;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import com.l2jhellas.gameserver.Announcements;
import com.l2jhellas.gameserver.datatables.sql.ItemTable;
import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2Item;
import com.l2jhellas.gameserver.network.SystemMessageId;
import com.l2jhellas.gameserver.network.serverpackets.ItemList;

public class AdminCreateItem implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_itemcreate",
		"admin_create_item"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.equals("admin_itemcreate"))
			AdminHelpPage.showHelpPage(activeChar, "itemcreation.htm");
		else if (command.startsWith("admin_create_item"))
		{
			try
			{
				String val = command.substring(17);
				StringTokenizer st = new StringTokenizer(val);
				
				final L2PcInstance  player = activeChar.getTarget() != null ? activeChar.getTarget().getActingPlayer() : activeChar;
				
				if (player == null)
				{
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
					return false;
				}
				
				if (st.countTokens() == 3)
				{
					String id = st.nextToken();
					int idval = Integer.parseInt(id);
					String num = st.nextToken();
					int numval = Integer.parseInt(num);
					createItem(player, idval, numval,true);
				}
				else if (st.countTokens() == 2)
				{
					String id = st.nextToken();
					int idval = Integer.parseInt(id);
					String num = st.nextToken();
					int numval = Integer.parseInt(num);
					createItem(player, idval, numval,false);
				}
				else if (st.countTokens() == 1)
				{
					String id = st.nextToken();
					int idval = Integer.parseInt(id);
					createItem(player, idval, 1,false);
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Usage: //itemcreate <itemId> [amount]");
			}
			catch (NumberFormatException nfe)
			{
				activeChar.sendMessage("Specify a valid number.");
			}
			AdminHelpPage.showHelpPage(activeChar, "itemcreation.htm");
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private static void createItem(L2PcInstance activeChar, int id, int num,boolean all)
	{
		if (num > 20)
		{
			L2Item template = ItemTable.getInstance().getTemplate(id);
			if (!template.isStackable())
			{
				activeChar.sendMessage("This item does not stack - Creation aborted.");
				return;
			}
		}
		
		if (all)
		{
			final Collection<L2PcInstance> players = L2World.getInstance().getAllPlayers().values().stream()
			.filter(Objects::nonNull).collect(Collectors.toList());
			for (L2PcInstance player : players)
			    player.getInventory().addItem("Admin", id, num, player,null);
			
			Announcements.getInstance().announceToAll("Server has been rewarded all online players.");
		}
		else
		{
			activeChar.getInventory().addItem("Admin", id, num, activeChar,null);
			activeChar.sendPacket(new ItemList(activeChar, true));
			activeChar.sendMessage("You have spawned " + num+ " item(s) number " + id + " in your inventory.");
		}
	}
}