package com.l2jhellas.gameserver.model.actor.instance;

import com.l2jhellas.gameserver.model.entity.events.engines.EventManager;
import com.l2jhellas.gameserver.network.serverpackets.ActionFailed;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jhellas.gameserver.templates.L2NpcTemplate;

public class L2EventManagerInstance extends L2NpcInstance
{
	public L2EventManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.startsWith("reg"))
			EventManager.getInstance().registerPlayer(player);
		else if (command.startsWith("unreg"))
			EventManager.getInstance().unregisterPlayer(player);
		else if (command.startsWith("list"))
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
         	StringBuilder sb = new StringBuilder();
            sb.append("<html><body><center>Select an event to vote for:<br>");
        	int i = 0;
        	for (String name: EventManager.getInstance().getEventNames())
        	{
        		i++;
        		sb.append (" <a action=\"bypass -h npc_"+getObjectId()+"_"+i+"\">- "+name+" -</a>  <br>");
        	}
        	sb.append("</center></body></html>");
        	html.setHtml(sb.toString());
            player.sendPacket(html);
		}
	}
	
	@Override
	public void showChatWindow(L2PcInstance player, int val)
	{
		EventManager.getInstance().showFirstHtml(player, getObjectId());
		player.sendPacket(ActionFailed.STATIC_PACKET);
		return;
	}
}