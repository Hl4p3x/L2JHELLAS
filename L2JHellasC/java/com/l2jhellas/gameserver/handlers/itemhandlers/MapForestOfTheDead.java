package com.l2jhellas.gameserver.handlers.itemhandlers;

import com.l2jhellas.gameserver.handler.IItemHandler;
import com.l2jhellas.gameserver.model.actor.L2Playable;
import com.l2jhellas.gameserver.model.actor.instance.L2PcInstance;
import com.l2jhellas.gameserver.model.actor.item.L2ItemInstance;
import com.l2jhellas.gameserver.network.serverpackets.NpcHtmlMessage;

public class MapForestOfTheDead implements IItemHandler
{
	public MapForestOfTheDead()
	{
	}
	
	private static int _itemIds[] =
	{
		7063
	};
	
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
		{
			return;
		}
		
		int itemId = item.getItemId();
		if (itemId == 7063)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(5);
			StringBuilder map = new StringBuilder("<html><title>Map - Forest of the Dead</title>");
			map.append("<body>");
			map.append("<br>");
			map.append("Map :");
			map.append("<br>");
			map.append("<table>");
			map.append("<tr><td>");
			map.append("<img src=\"icon.Quest_deadperson_forest_t00\" width=255 height=255>");
			map.append("</td></tr>");
			map.append("</table>");
			map.append("</body></html>");
			html.setHtml(map.toString());
			playable.sendPacket(html);
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return _itemIds;
	}
}