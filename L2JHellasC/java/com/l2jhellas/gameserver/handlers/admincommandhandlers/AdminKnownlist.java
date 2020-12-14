package com.l2jhellas.gameserver.handlers.admincommandhandlers;

import java.util.List;
import java.util.StringTokenizer;

import com.l2jhellas.gameserver.handler.IAdminCommandHandler;
import com.l2jhellas.gameserver.model.L2Object;
import com.l2jhellas.gameserver.model.L2World;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.util.MathUtil;
import com.l2jhellas.util.StringUtil;

public class AdminKnownlist implements IAdminCommandHandler
{
	private static final int PAGE_LIMIT = 60;
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_knownlist",
		"admin_knownlist_page",
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance player)
	{
		if (command.startsWith("admin_knownlist"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			
			L2Object target = null;
			
			if (st.hasMoreTokens())
			{
				final String par = st.nextToken();
				
				try
				{
					final int objectId = Integer.parseInt(par);
					target = player.getTarget() != null && player.getTarget().getObjectId() == objectId ? player.getTarget() : L2World.getInstance().findObject(objectId);
				}
				catch (NumberFormatException nfe)
				{
					target = player.getTarget() != null && player.getTarget().getName().equalsIgnoreCase(par) ? player.getTarget() : L2World.getInstance().getPlayer(par);
				}
			}
			
			if (target == null)
				target = player.getTarget() != null ? player.getTarget() : player;
			
			int page = 1;
			
			if (command.startsWith("admin_knownlist_page") && st.hasMoreTokens())
			{
				try
				{
					page = Integer.parseInt(st.nextToken());
				}
				catch (NumberFormatException nfe)
				{
				}
			}
			
			showObjects(player, target, page);
		}
		return true;
	}
	
	private static void showObjects(L2PcInstance activeChar, L2Object target, int page)
	{
		 List<L2Object> knownlist = L2World.getInstance().getVisibleObjects(target, L2Object.class);
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/admin/knownlist.htm");
		html.replace("%target%", target.getName());
		html.replace("%size%", knownlist.size());
		
		if (knownlist.isEmpty())
		{
			html.replace("%knownlist%", "<tr><td>No objects around that object.</td></tr>");
			html.replace("%pages%", 0);
			activeChar.sendPacket(html);
			return;
		}
		
		final int max = MathUtil.countPagesNumber(knownlist.size(), PAGE_LIMIT);
		if (page > max)
			page = max;
		
		knownlist = knownlist.subList((page - 1) * PAGE_LIMIT, Math.min(page * PAGE_LIMIT, knownlist.size()));
		
		final StringBuilder sb = new StringBuilder(knownlist.size() * 150);
		for (L2Object object : knownlist)
			StringUtil.append(sb, "<tr><td>", object.getName(), "</td><td>", object.getClass().getSimpleName(), "</td></tr>");
		
		html.replace("%knownlist%", sb.toString());
		
		sb.setLength(0);
		
		for (int i = 0; i < max; i++)
		{
			final int pagenr = i + 1;
			if (page == pagenr)
				StringUtil.append(sb, pagenr, "&nbsp;");
			else
				StringUtil.append(sb, "<a action=\"bypass -h admin_knownlist_page ", target.getObjectId(), " ", pagenr, "\">", pagenr, "</a>&nbsp;");
		}
		
		html.replace("%pages%", sb.toString());
		
		activeChar.sendPacket(html);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}